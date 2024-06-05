package com.example.bigdata;

import com.example.bigdata.extractor.MoviesTimeExtractor;
import com.example.bigdata.extractor.ViewTimeExtractor;
import com.example.bigdata.model.MovieTitle;
import com.example.bigdata.model.NetflixAggregate;
import com.example.bigdata.model.NetflixView;
import com.example.bigdata.serde.NetflixAggregateSerde;
import com.example.bigdata.serde.NetflixViewSerde;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;

import org.apache.kafka.streams.kstream.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ApacheLogToAlertRequests {

    public static void main(String[] args) throws Exception {
        Properties config = new Properties();
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, args[0]);
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "alert-requests-application");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        int _D = Integer.parseInt(args[1]);
        int _L = Integer.parseInt(args[2]);
        double _O = Double.parseDouble(args[3]);
        System.out.println(_D);
        System.out.println(_L);
        System.out.println(_O);

        final Serde<String> stringSerde = Serdes.String();
        Serde<NetflixAggregate> aggSerde = new NetflixAggregateSerde();
        Serde<NetflixView> viewSerde = new NetflixViewSerde();
        ViewTimeExtractor netflixExtractor = new ViewTimeExtractor();
        MoviesTimeExtractor moviesExtractor = new MoviesTimeExtractor();

        Calculator calculator = new Calculator();


        final StreamsBuilder builder = new StreamsBuilder();
        KStream<String, NetflixView> netflixLines = builder
                .stream("netflix-in", Consumed.with(stringSerde, stringSerde).withTimestampExtractor(netflixExtractor))
                .mapValues(value -> NetflixView.parseFromLogLine(value));


        KTable<String, String> movieTitles = builder
                .stream("movies-in", Consumed.with(stringSerde, stringSerde).withTimestampExtractor(moviesExtractor))
                .map((k, v) -> {
                    String[] vals = v.split(",");
                    return KeyValue.pair(vals[0], vals[2].strip());
                }).toTable();

//        TODO: który join. Inner raczej nie jest ok. Lewy ma bugi
        KStream<String, NetflixView> joinedLines = netflixLines
                .map((key, value) -> KeyValue.pair(value.getFilm_id(), value))
                .join(movieTitles, (NetflixView val1, String val2) -> {
//                .leftJoin(movieTitles, (NetflixView val1, String val2) -> {
                            try {
                                if (val2 == null) { // is it length problem?
                                    val2 = "";
                                }
                                val1.title = val2;
                                return val1;
                            } catch (Exception e) {
                                System.out.println("join error");
                                System.out.println(e.getMessage());
                                System.out.println(e.getClass());
                                return new NetflixView();
                            }

                        }
                        , Joined.with(stringSerde, viewSerde, stringSerde)
                );

        KTable<Windowed<String>, NetflixAggregate> groupedFilms = joinedLines
                .groupByKey(Grouped.with("name", Serdes.String(), viewSerde))
                .windowedBy(TimeWindows.of(Duration.ofDays(30)) /* time-based window */)
                .aggregate(
                        calculator::initAgg,
                        calculator::aggReducer,
                        Materialized.with(stringSerde, aggSerde)
                );


//        Check KeyValue.pair(k,v), KStream<Windowed<String!!!!
        KStream<String, String> etlResults = groupedFilms.toStream().map((k, v) -> KeyValue.pair(k.key(), String.format("{ \"schema\": { \"type\": \"struct\", \"optional\": false, \"version\": 1, \"fields\": [ {\"field\": \"title\", \"type\": \"string\", \"optional\": true }, { \"field\": \"rating_count\", \"type\":\"int32\", \"optional\": true }, { \"field\": \"rating_sum\", \"type\": \"int32\", \"optional\": true } ] }, \"payload\": { \"title\":\"%s\", \"rating_count\": %s, \"rating_sum\": %s } }",
                cleanString(v.title),
//                "string_example",
                v.ratingCount, v.ratingSum)));


        etlResults.to("etl-out");

//        Czy można czytać 2 x z tego samego topica?
        KTable<Windowed<String>, NetflixAggregate> groupedForAnomaly = joinedLines
                .groupByKey(Grouped.with("name", Serdes.String(), viewSerde))
                .windowedBy(TimeWindows.of(Duration.ofDays(_D)).advanceBy(Duration.ofDays(1)))
                .aggregate(calculator::initAgg,
                        calculator::aggReducer,
                        Materialized.with(stringSerde, aggSerde)
                );

        groupedForAnomaly.mapValues((val) -> {
            val.average = (double) (val.ratingSum / val.ratingCount);
            return val;
        })
        .toStream()
        .filter((key, val) -> {
            if (val == null || val.ratingCount == null || val.ratingSum == null) {
                return false;
            }
            return val.average > _O && val.ratingCount > _L;
        })
        .map((k,v) -> {
            if (v == null) {
                System.out.println("NULL IN MAPPING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                return KeyValue.pair(k, "NULL!!!!!!!!!");
            } else {
                return KeyValue.pair(k.key(), String.format("{ \"schema\": { \"type\": \"struct\", \"optional\": false, \"version\": 1, \"fields\": [ {\"field\": \"title\", \"type\": \"string\", \"optional\": true }, { \"field\": \"rating_count\", \"type\":\"int32\", \"optional\": true }, { \"field\": \"rating_avg\", \"type\": \"double\", \"optional\": true }, { \"field\": \"start_time\", \"type\": \"double\", \"optional\": true }, { \"field\": \"end_time\", \"type\": \"double\", \"optional\": true } ] }, \"payload\": { \"title\":\"%s\", \"rating_count\": %s, \"rating_avg\": %s, \"start_time\": \"%s\", \"end_time\": \"%s\" } }"
                        , cleanString(v.title), v.ratingCount, v.average, String.valueOf(k.window().start()), String.valueOf(k.window().start())));
            }
        })
                .to("anomaly-out");


        final Topology topology = builder.build();
        System.out.println(topology.describe());

        KafkaStreams streams = new KafkaStreams(topology, config);

        streams.setUncaughtExceptionHandler((thread, exception) -> {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
            System.out.println("ERROR!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        });

        final CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);

    }

    public static String cleanString(String str) {
        return str.strip().replaceAll("[^a-zA-Z0-9 ]",  "");
//                .replaceAll("\n","").replaceAll("\r", "");
    }
}


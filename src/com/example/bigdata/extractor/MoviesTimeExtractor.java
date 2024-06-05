package com.example.bigdata.extractor;

import com.example.bigdata.model.NetflixView;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

public class MoviesTimeExtractor implements TimestampExtractor {

    public long extract(final ConsumerRecord<Object, Object> record,
                        final long previousTimestamp) {
        return 100L;
    }
}

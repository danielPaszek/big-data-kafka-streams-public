package com.example.bigdata.extractor;

import com.example.bigdata.model.NetflixView;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

public class ViewTimeExtractor implements TimestampExtractor {
    public long extract(final ConsumerRecord<Object, Object> record,
                        final long previousTimestamp) {
        try {
            long timestamp = -1;
            String stringLine;

            if (record.value() instanceof String) {
                stringLine = (String) record.value();
                    timestamp = NetflixView.parseFromLogLine(stringLine).
                            getJoinedDateInLong();
            }
            return timestamp;
        } catch (Exception e) {
            System.out.println("WTF!!!!!!!");
            System.out.println(record.value());
            long timestamp = NetflixView.parseFromLogLine((String) record.value()).
                    getJoinedDateInLong();
            System.out.println(timestamp);
            return -1;
        }
    }
}

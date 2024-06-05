package com.example.bigdata.serde;

import com.example.bigdata.model.NetflixAggregate;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

public class NetflixAggregateSerde implements Serde<NetflixAggregate> {

    @Override
    public Serializer<NetflixAggregate> serializer() {
        return new Serializer<NetflixAggregate>() {
            @Override
            public byte[] serialize(String topic, NetflixAggregate data) {
                if (data == null) return null;
                ByteBuffer buffer = null;
                try {
                    buffer = ByteBuffer.allocate(2048);
                    buffer.putLong(data.ratingCount);
                    buffer.putLong(data.ratingSum);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
//                    data.title = data.title != null ? data.title : "";
                    oos.writeObject(data.title);
                    byte[] strBuffer = bos.toByteArray();
                    buffer.putInt(strBuffer.length);
                    buffer.put(strBuffer);
//
                } catch (Exception e) {
                    System.out.println("SERIALIZE AGG");
                    System.out.println(data);
                    System.out.println("msg: " + e.getMessage());
                    System.out.println(e.getClass());
                }

                return buffer.array();
            }
        };
    }

    @Override
    public Deserializer<NetflixAggregate> deserializer() {
        return new Deserializer<NetflixAggregate>() {
            @Override
            public NetflixAggregate deserialize(String topic, byte[] data) {
                if (data == null) return null;
                NetflixAggregate agg = new NetflixAggregate();
                int len = 0;
                try {
                    ByteBuffer buffer = ByteBuffer.wrap(data);
                    agg.ratingCount = buffer.getLong();
                    agg.ratingSum = buffer.getLong();
                    len = buffer.getInt();
                    byte[] str = new byte[2048];
                    buffer.get(str, 20, len); //20 - 2longs, 1 int
                    agg.title = new String(str);
                    return agg;
                } catch (Exception e) {
                    System.out.println("DESERIALIZE AGG");
                    System.out.println(agg);
                    System.out.println("LEN: " + len);
                    System.out.println("msg: " + e.getMessage());
                    System.out.println(e.getClass());
                    NetflixAggregate res = new NetflixAggregate();
                    res.ratingCount = agg.ratingCount;
                    res.ratingSum = agg.ratingSum;
                    res.title = "title lost";
                    return res;
                }
            }
        };
    }
}
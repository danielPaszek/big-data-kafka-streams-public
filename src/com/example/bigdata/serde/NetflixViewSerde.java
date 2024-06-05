package com.example.bigdata.serde;

import com.example.bigdata.model.NetflixAggregate;
import com.example.bigdata.model.NetflixView;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

public class NetflixViewSerde implements Serde<NetflixView> {

    @Override
    public Serializer<NetflixView> serializer() {
        return new Serializer<NetflixView>() {
            @Override
            public byte[] serialize(String topic, NetflixView data) {
                if (data == null) return null;
                ByteBuffer buffer = ByteBuffer.allocate(512);
                buffer.putInt(data.getRate());
                buffer.putLong(data.getUser_id());
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    data.title = data.title != null ? data.title : "";
                    oos.writeObject(data.title);
                    byte[] strBuffer = bos.toByteArray();
                    buffer.putInt(strBuffer.length);
                    buffer.put(strBuffer);
                } catch (Exception e) {
                    System.out.println("SERIALIZE VIEW");
                    System.out.println(data.title.length() + ", title" + data.title);
                    System.out.println("msg " + e.getMessage());
                }

                return buffer.array();
            }
        };
    }

    @Override
    public Deserializer<NetflixView> deserializer() {
        return new Deserializer<NetflixView>() {
            @Override
            public NetflixView deserialize(String topic, byte[] data) {
                if (data == null) return null;
                try {

                ByteBuffer buffer = ByteBuffer.wrap(data);
                NetflixView agg = new NetflixView();
                agg.setRate(buffer.getInt());
                agg.setUser_id(buffer.getLong());
                int len = buffer.getInt();
                byte[] str = new byte[512];
                buffer.get(str, 16, len); //16 - 1longs, 2 int
                agg.title = new String(str);
                return agg;
                } catch (Exception e) {
                    System.out.println("DESERIALIZE VIEW");
                    System.out.println(e.getMessage());
                    return new NetflixView();
                }
            }
        };
    }
}

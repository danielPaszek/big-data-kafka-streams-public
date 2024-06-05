package com.example.bigdata;

import com.example.bigdata.model.NetflixAggregate;
import com.example.bigdata.model.NetflixView;

public class Calculator {
    public NetflixAggregate initAgg() {
        NetflixAggregate agg = new NetflixAggregate();
        agg.ratingSum = 0L;
        agg.ratingCount = 0L;
        agg.title = "";
        return agg;
    }
    public NetflixAggregate aggReducer(String key, NetflixView value, NetflixAggregate agg) {
        agg.ratingCount++;
        agg.ratingSum += value.rate;
        if (agg.title == null) {
            agg.title = "";
        }
        if (value.title != null) {
            agg.title = value.title;
        }
        return agg;
    }
}

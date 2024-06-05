package com.example.bigdata.model;

import java.io.Serializable;

public class NetflixAggregate implements Serializable {
    public Long ratingCount;
    public Long ratingSum;
    public Double average;

    public String title;

    public NetflixAggregate() {
//        title = "";
    }

//    public NetflixAggregate(
////            String film_id,
//            Long ratingCount, Long ratingSum, Long distinctPeople) {
////        this.film_id = film_id;
//        this.ratingCount = ratingCount;
//        this.ratingSum = ratingSum;
//        this.distinctPeople = distinctPeople;
////        TODO: can't be here
////        this.distinctMap = new HashMap<>();
//    }

    @Override
    public String toString() {
        return String.format("count %s, sum %s, title: %s", ratingCount, ratingSum, title);
    }
}


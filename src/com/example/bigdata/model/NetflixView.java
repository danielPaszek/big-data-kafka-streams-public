package com.example.bigdata.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

public class NetflixView implements Serializable {
    private static final Logger logger = Logger.getLogger("Access");
    public String dateString;
    public String film_id;
    public long user_id;
    public int rate;

    public String title;

    public NetflixView() {
        title = "";

    }

//    public NetflixView(String dateString, String film_id, String user_id, String rate) {
//        this.dateString = dateString;
//        this.film_id = film_id;
//        this.user_id = Long.parseLong(user_id);
//        this.rate = Integer.parseInt(rate);
//    }

    public static NetflixView parseFromLogLine(String logline) {
        String[] m = logline.split(",");
        NetflixView nv = new NetflixView();
        nv.dateString = m[0];
        nv.film_id = m[1];
        nv.user_id =  Long.parseLong(m[2]);
        nv.rate = Integer.parseInt(m[3]);
        return nv;
    }

    @Override
    public String toString() {
        return String.format("date %s, id %s, user %s, rate %s, title: %s ", dateString, film_id, user_id, rate, title);
    }

    /**
     * TODO:
     * @return
     */
    public long getJoinedDateInLong() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date date;
        try {
            date = sdf.parse(dateString);
            return date.getTime();
        } catch (ParseException e) {
            return -1;
        }
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public String getFilm_id() {
        return film_id;
    }

    public void setFilm_id(String film_id) {
        this.film_id = film_id;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }
}


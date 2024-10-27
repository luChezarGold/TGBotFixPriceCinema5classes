package org.example;

import java.sql.Timestamp;

public class ShowtimeInfo {
    int id;
    String movieTitle;
    Timestamp showtime;
    int availableSeats;
    ShowtimeInfo(int id, String movieTitle, Timestamp showtime, int availableSeats) {
        this.id = id;
        this.movieTitle = movieTitle;
        this.showtime = showtime;
        this.availableSeats = availableSeats;
    }
    @Override
    public String toString() {
        return id + " - " + movieTitle + " - " + showtime + " (Мест: " + availableSeats + ")";
    }
}

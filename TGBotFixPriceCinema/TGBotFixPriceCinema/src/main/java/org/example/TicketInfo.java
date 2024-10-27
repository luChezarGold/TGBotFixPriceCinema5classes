package org.example;

import java.sql.Timestamp;

public class TicketInfo {
    int id;
    String movieTitle;
    Timestamp showtime;
    int availableSeats;

    TicketInfo(int id, String movieTitle, Timestamp showtime, int availableSeats) {
        this.id = id;
        this.movieTitle = movieTitle;
        this.showtime = showtime;
        this.availableSeats = availableSeats;
    }

    @Override
    public String toString() {
        return "Билет № " + id + ": " + movieTitle + " - " + showtime + " (Мест: " + availableSeats + ")";
    }
}

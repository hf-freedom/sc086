package com.sports.venue.model;

import com.sports.venue.enums.SportType;
import com.sports.venue.enums.VenueType;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Stadium implements Serializable {
    private String id;
    private String name;
    private String address;
    private VenueType venueType;
    private List<SportType> supportedSports;
    private String description;

    public Stadium() {
        this.id = UUID.randomUUID().toString();
        this.supportedSports = new ArrayList<>();
    }

    public Stadium(String name, String address, VenueType venueType) {
        this();
        this.name = name;
        this.address = address;
        this.venueType = venueType;
    }
}
package com.sports.venue.enums;

public enum VenueType {
    INDOOR("室内"),
    OUTDOOR("室外"),
    BOTH("室内外均可");

    private final String description;

    VenueType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
package com.example.corporate;

public class Company {
    private String name, logo, location;
    private int numOfReviews;
    private float avgRating;

    public Company(String name, String logo, String location, int numOfReviews, float avgRating) {
        this.name = name;
        this.logo = logo;
        this.location = location;
        this.numOfReviews = numOfReviews;
        this.avgRating = avgRating;
    }

    public Company() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setNumOfReviews(int numOfReviews) {
        this.numOfReviews = numOfReviews;
    }

    public void setAvgRating(float avgRating) {
        this.avgRating = avgRating;
    }

    public String getName() {
        return name;
    }

    public String getLogo() {
        return logo;
    }

    public String getLocation() {
        return location;
    }

    public int getNumOfReviews() {
        return numOfReviews;
    }

    public float getAvgRating() {
        return avgRating;
    }
}

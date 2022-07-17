package com.example.corporate;

import java.util.Comparator;

public class Review implements Comparator<Review> {
    private String company, UID, reviewText, docID;
    private double avgEnvironmental, avgEthics, avgLeadership, avgRating, avgWageEquality,avgWorkingConditions;
    private int  numOfLikes;

    public Review(){
    }

    public Review(String company, String UID, String reviewText, double avgEnvironmental, double avgEthics,
                  double avgLeadership, double avgRating, double avgWageEquality,double avgWorkingConditions,
                  int numOfLikes) {
        this.company = company;
        this.UID = UID;
        this.reviewText = reviewText;
        this.avgEnvironmental = avgEnvironmental;
        this.avgEthics = avgEthics;
        this.avgLeadership = avgLeadership;
        this.avgRating = avgRating;
        this.avgWageEquality = avgWageEquality;
        this.avgWorkingConditions = avgWorkingConditions;
        this.numOfLikes = numOfLikes;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getUID() {
        return UID;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public double getAvgEnvironmental() {
        return avgEnvironmental;
    }

    public void setAvgEnvironmental(double avgEnvironmental) {
        this.avgEnvironmental = avgEnvironmental;
    }

    public double getAvgEthics() {
        return avgEthics;
    }

    public void setAvgEthics(double avgEthics) {
        this.avgEthics = avgEthics;
    }

    public double getAvgLeadership() {
        return avgLeadership;
    }

    public void setAvgLeadership(double avgLeadership) {
        this.avgLeadership = avgLeadership;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }

    public double getAvgWageEquality() {
        return avgWageEquality;
    }

    public void setAvgWageEquality(double avgWageEquality) {
        this.avgWageEquality = avgWageEquality;
    }

    public double getAvgWorkingConditions() {
        return avgWorkingConditions;
    }

    public void setAvgWorkingConditions(double avgWorkingConditions) {
        this.avgWorkingConditions = avgWorkingConditions;
    }

    public int getNumOfLikes() {
        return numOfLikes;
    }

    public void setNumOfLikes(int numOfLikes) {
        this.numOfLikes = numOfLikes;
    }

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    @Override
    public int compare(Review o1, Review o2) {
        return Integer.compare(o2.getNumOfLikes(), o1.getNumOfLikes());
    }
}
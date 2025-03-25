package com.example.stayconnect.models;

public class ModelAd {

    String id;
    String uid;
    String rent;
    String ownerName;
    String hostelName;
    String category;
    String hostelAddress;
    String ownerContactNumber;
    String description;
    long timeStamp;
    double latitude;
    double longitude;
    boolean favorite;

    public ModelAd() {

    }


    public ModelAd(String id, boolean favorite, double longitude, double latitude, long timeStamp, String description, String ownerContactNumber, String hostelAddress, String category, String hostelName, String ownerName, String rent, String uid) {
        this.id = id;
        this.favorite = favorite;
        this.longitude = longitude;
        this.latitude = latitude;
        this.timeStamp = timeStamp;
        this.description = description;
        this.ownerContactNumber = ownerContactNumber;
        this.hostelAddress = hostelAddress;
        this.category = category;
        this.hostelName = hostelName;
        this.ownerName = ownerName;
        this.rent = rent;
        this.uid = uid;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRent() {
        return rent;
    }

    public void setRent(String rent) {
        this.rent = rent;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getHostelName() {
        return hostelName;
    }

    public void setHostelName(String hostelName) {
        this.hostelName = hostelName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getHostelAddress() {
        return hostelAddress;
    }

    public void setHostelAddress(String hostelAddress) {
        this.hostelAddress = hostelAddress;
    }

    public String getOwnerContactNumber() {
        return ownerContactNumber;
    }

    public void setOwnerContactNumber(String ownerContactNumber) {
        this.ownerContactNumber = ownerContactNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}

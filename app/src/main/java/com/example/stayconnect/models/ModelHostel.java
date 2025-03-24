package com.example.stayconnect.models;

public class ModelHostel {

    String id;
    String uid;
    String noOfRooms;
    String roomType;
    String roomCapacity;
    String rent;
    String hostelName;
    String category;
    String hostelAddress;
    String ownerContactNumber;
    String description;
    long timeStamp;
    double latitude;
    double longitude;
    boolean favorite;

    public ModelHostel() {

    }


    public ModelHostel(String id, String category, String uid, String noOfRooms, String roomType, String roomCapacity, String rent, String hostelName, String hostelAddress, String ownerContactNumber, String description, long timeStamp, double latitude, double longitude, boolean favorite) {
        this.id = id;
        this.uid = uid;
        this.category = category;
        this.noOfRooms = noOfRooms;
        this.roomType = roomType;
        this.roomCapacity = roomCapacity;
        this.rent = rent;
        this.hostelName = hostelName;
        this.hostelAddress = hostelAddress;
        this.ownerContactNumber = ownerContactNumber;
        this.description = description;
        this.timeStamp = timeStamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.favorite = favorite;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNoOfRooms() {
        return noOfRooms;
    }

    public void setNoOfRooms(String noOfRooms) {
        this.noOfRooms = noOfRooms;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getRoomCapacity() {
        return roomCapacity;
    }

    public void setRoomCapacity(String roomCapacity) {
        this.roomCapacity = roomCapacity;
    }

    public String getRent() {
        return rent;
    }

    public void setRent(String rent) {
        this.rent = rent;
    }

    public String getHostelName() {
        return hostelName;
    }

    public void setHostelName(String hostelName) {
        this.hostelName = hostelName;
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

    public void setDescription(String descriptionEt) {
        this.description = descriptionEt;
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

package com.example.stayconnect.models;

public class ModelChats {

//    String profileImageUrl;

    String name;

    String chatKey;

    String ownerUid;

    String myUid;

    String messageId;

    String message;

    String messageType;

    String fromUid;

    String toUid;

    long timestamp;


    public ModelChats() {

    }


    public ModelChats(String name, String chatKey, String ownerUid, String myUid, String messageId, String message, String messageType, String fromUid, String toUid, long timestamp) {
        this.name = name;
        this.chatKey = chatKey;
        this.ownerUid = ownerUid;
        this.myUid = myUid;
        this.messageId = messageId;
        this.message = message;
        this.messageType = messageType;
        this.fromUid = fromUid;
        this.toUid = toUid;
        this.timestamp = timestamp;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChatKey() {
        return chatKey;
    }

    public void setChatKey(String chatKey) {
        this.chatKey = chatKey;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    public String getMyUid() {
        return myUid;
    }

    public void setMyUid(String myUid) {
        this.myUid = myUid;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public String getToUid() {
        return toUid;
    }

    public void setToUid(String toUid) {
        this.toUid = toUid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

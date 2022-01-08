package com.hyapp.achat.model.entity;

import com.google.gson.annotations.Expose;

public abstract class Message {

    public static final byte TRANSFER_TYPE_SEND = 1;
    public static final byte TRANSFER_TYPE_RECEIVE = 2;

    public static final byte TYPE_TEXT = 0;
    public static final byte TYPE_IMAGE = 2;
    public static final byte TYPE_VOICE = 4;
    public static final byte TYPE_VIDEO = 6;
    public static final byte TYPE_MUSIC = 8;
    public static final byte TYPE_FILE = 10;
    public static final byte TYPE_DETAILS = 12;
    public static final byte TYPE_PROFILE = 14;
    public static final byte TYPE_LOTTIE = 18;

    @Expose
    protected byte type;
    protected byte transferType;
    @Expose
    protected long timeMillis;

    public Message(){
    }

    public Message(byte type, byte transferType, long timeMillis) {
        this.type = type;
        this.transferType = transferType;
        this.timeMillis = timeMillis;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getTransferType() {
        return transferType;
    }

    public void setTransferType(byte transferType) {
        this.transferType = transferType;
    }


    public long getTimeMillis() {
        return timeMillis;
    }

    public void setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
    }
}

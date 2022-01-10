package com.hyapp.achat.model.entity;

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

    protected byte type;
    protected long time;

    public transient byte transferType;

    public Message(){
    }

    public Message(byte type, byte transferType, long time) {
        this.type = type;
        this.transferType = transferType;
        this.time = time;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}

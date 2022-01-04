package com.hyapp.achat.model;

import com.google.gson.annotations.Expose;
import com.hyapp.achat.bl.utils.TimeUtils;
import com.hyapp.achat.model.utils.MessageUtils;

public abstract class ChatMessage extends com.hyapp.achat.model.Message {

    public static final byte DELIVERY_HIDDEN = 1;
    public static final byte DELIVERY_WAITING = 2;
    public static final byte DELIVERY_UNREAD = 3;
    public static final byte DELIVERY_READ = 4;

    protected transient int bubbleRes;
    protected transient int deliveryRes;
    protected transient String time;

    @Expose
    protected String uid;
    @Expose
    protected Contact sender;
    @Expose
    protected String receiverId;
    protected byte delivery;

    public ChatMessage(){
    }

    public ChatMessage(byte type, byte transferType, long timeMillis, String uid, Contact sender, String receiverId) {
        super(type, transferType, timeMillis);
        this.bubbleRes = MessageUtils.BUBBLE_RES_SEND_START;
        if (transferType == com.hyapp.achat.model.Message.TRANSFER_TYPE_SEND) {
            delivery = DELIVERY_WAITING;
            deliveryRes = MessageUtils.DELIVERY_WAITING_RES;
        }
        this.uid = uid;
        this.sender = sender;
        this.receiverId = receiverId;
        setupTime(timeMillis);
    }

    public void setDelivery(byte delivery) {
        this.delivery = delivery;
        switch (delivery) {
            case DELIVERY_WAITING:
                deliveryRes = MessageUtils.DELIVERY_WAITING_RES;
                break;
            case DELIVERY_UNREAD:
                deliveryRes = MessageUtils.DELIVERY_UNREAD_RES;
                break;
            case DELIVERY_READ:
                deliveryRes = MessageUtils.DELIVERY_READ_RES;
                break;
        }
    }

    public int getBubbleRes() {
        return bubbleRes;
    }

    public void setBubbleRes(int bubbleRes) {
        this.bubbleRes = bubbleRes;
    }

    public int getDeliveryRes() {
        return deliveryRes;
    }

    public void setDeliveryRes(int deliveryRes) {
        this.deliveryRes = deliveryRes;
    }

    public Contact getSender() {
        return sender;
    }

    public void setSender(Contact sender) {
        this.sender = sender;
    }

    public String getTime() {
        return time;
    }

    public void setupTime(long timeMillis) {
        setTimeMillis(timeMillis);
        this.time = TimeUtils.millis2DayTime(timeMillis);
    }

    public byte getDelivery() {
        return delivery;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
}

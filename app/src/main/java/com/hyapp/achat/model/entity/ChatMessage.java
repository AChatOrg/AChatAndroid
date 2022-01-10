package com.hyapp.achat.model.entity;

import android.text.format.DateUtils;

public abstract class ChatMessage extends Message {

    public static final byte DELIVERY_HIDDEN = 1;
    public static final byte DELIVERY_WAITING = 2;
    public static final byte DELIVERY_UNREAD = 3;
    public static final byte DELIVERY_READ = 4;

    public static final byte BUBBLE_START = 1;
    public static final byte BUBBLE_MIDDLE = 2;
    public static final byte BUBBLE_END = 3;
    public static final byte BUBBLE_SINGLE = 4;

    protected String uid;
    protected Contact sender;
    protected String receiverUid;

    public transient byte delivery;
    public transient byte bubble;

    public ChatMessage() {
    }

    public ChatMessage(byte type, byte transferType, long timeMillis, String uid, Contact sender, String receiverUid) {
        super(type, transferType, timeMillis);
        this.uid = uid;
        this.sender = sender;
        this.receiverUid = receiverUid;
        bubble = BUBBLE_START;
        delivery = DELIVERY_WAITING;
    }

    public Contact getSender() {
        return sender;
    }

    public void setSender(Contact sender) {
        this.sender = sender;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getReceiverUid() {
        return receiverUid;
    }

    public void setReceiverUid(String receiverUid) {
        this.receiverUid = receiverUid;
    }

    private boolean setupBubble(ChatMessage message) {
        boolean haveDateSeparatorPrev = false;
        if (message.getTransferType() == Message.TRANSFER_TYPE_SEND) {
            if (messages.size() == 1) {
                message.setBubbleRes(MessageUtils.BUBBLE_RES_SEND_SINGLE);
                haveDateSeparatorPrev = true;
            } else {
                Message prev = messages.get(messages.size() - 1);
                if (prev instanceof ChatMessage
                        && prev.getTransferType() == Message.TRANSFER_TYPE_SEND
                        && message.getTime() - prev.getTime() < 60000
                ) {
                    message.setBubbleRes(MessageUtils.BUBBLE_RES_SEND_END);
                    if (messages.size() >= 3) {
                        Message prevPrev = messages.get(messages.size() - 2);
                        if (prevPrev instanceof ChatMessage
                                && prevPrev.getTransferType() == Message.TRANSFER_TYPE_SEND
                                && prev.getTime() - prevPrev.getTime() < 60000
                        ) {
                            ((ChatMessage) prev).setBubbleRes(MessageUtils.BUBBLE_RES_SEND_MIDDLE);
                        } else {
                            ((ChatMessage) prev).setBubbleRes(MessageUtils.BUBBLE_RES_SEND_START);
                        }
                    } else {
                        ((ChatMessage) prev).setBubbleRes(MessageUtils.BUBBLE_RES_SEND_START);
                    }
                    notifyItemChanged(messages.indexOf(prev), PAYLOAD_BUBBLE);
                } else {
                    if (!DateUtils.isToday(prev.getTime())) {
                        haveDateSeparatorPrev = true;
                    }
                    message.setBubbleRes(MessageUtils.BUBBLE_RES_SEND_SINGLE);
                }
            }
        } else {
            if (messages.size() == 1) {
                message.setBubbleRes(MessageUtils.BUBBLE_RES_RECEIVE_SINGLE);
                haveDateSeparatorPrev = true;
            } else {
                Message prev = messages.get(messages.size() - 1);
                if (prev instanceof ChatMessage
                        && prev.getTransferType() == Message.TRANSFER_TYPE_RECEIVE
                        && message.getTime() - prev.getTime() < 60000
                ) {
                    message.setBubbleRes(MessageUtils.BUBBLE_RES_RECEIVE_END);
                    if (messages.size() >= 3) {
                        Message prevPrev = messages.get(messages.size() - 2);
                        if (prevPrev instanceof ChatMessage
                                && prevPrev.getTransferType() == Message.TRANSFER_TYPE_RECEIVE
                                && prev.getTime() - prevPrev.getTime() < 60000
                        ) {
                            ((ChatMessage) prev).setBubbleRes(MessageUtils.BUBBLE_RES_RECEIVE_MIDDLE);
                        } else {
                            ((ChatMessage) prev).setBubbleRes(MessageUtils.BUBBLE_RES_RECEIVE_START);
                        }
                    } else {
                        ((ChatMessage) prev).setBubbleRes(MessageUtils.BUBBLE_RES_RECEIVE_START);
                    }
                    notifyItemChanged(messages.indexOf(prev), PAYLOAD_BUBBLE);
                } else {
                    if (!DateUtils.isToday(prev.getTime())) {
                        haveDateSeparatorPrev = true;
                    }
                    message.setBubbleRes(MessageUtils.BUBBLE_RES_RECEIVE_SINGLE);
                }
            }
        }
        return haveDateSeparatorPrev;
    }

}

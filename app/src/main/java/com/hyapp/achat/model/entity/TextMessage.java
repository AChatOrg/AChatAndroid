package com.hyapp.achat.model.entity;

import com.google.gson.annotations.Expose;

public class TextMessage extends ChatMessage {

    @Expose
    protected String text;
    @Expose
    protected int ExtraTextSize;

    public TextMessage(){
    }

    public TextMessage(byte transferType, long timeMillis, String uid, Contact sender, String receiverId, String text, int extraTextSize) {
        super(TYPE_TEXT, transferType, timeMillis, uid, sender, receiverId);
        this.text = text;
        ExtraTextSize = extraTextSize;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getExtraTextSize() {
        return ExtraTextSize;
    }

    public void setExtraTextSize(int extraTextSize) {
        this.ExtraTextSize = extraTextSize;
    }
}

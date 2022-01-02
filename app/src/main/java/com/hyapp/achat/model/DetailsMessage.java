package com.hyapp.achat.model;

import android.text.format.DateUtils;

public class DetailsMessage extends Message {

    protected String details;

    public DetailsMessage(long timeMillis, String details) {
        super(TYPE_DETAILS, TRANSFER_TYPE_RECEIVE, timeMillis);
        this.details = details;
    }

    public DetailsMessage(long timeMillis) {
        super(TYPE_DETAILS, TRANSFER_TYPE_RECEIVE, timeMillis);
        setDetails(timeMillis);
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setDetails(long timeMillis) {
        details = DateUtils.getRelativeTimeSpanString(
                timeMillis
                , System.currentTimeMillis()
                , DateUtils.DAY_IN_MILLIS
                , DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString();
    }
}

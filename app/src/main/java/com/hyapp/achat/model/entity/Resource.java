package com.hyapp.achat.model.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Resource<T> {

    public static final int INDEX_ALL = -1;
    public static final int INDEX_NEW = -2;

    public enum Status {SUCCESS, ERROR, LOADING}

    public enum Action {ADD, REMOVE, UPDATE, ADD_PAGING, ADD_UNREAD}

    @NonNull
    public final Status status;
    @Nullable
    public final T data;
    @NonNull
    public final String message;

    public Action action;
    public int index;
    public boolean bool;
    public boolean bool2;
    public boolean bool3;

    private Resource(@NonNull Status status, @Nullable T data, @Nullable String message, Action action, int index, boolean bool, boolean bool2, boolean bool3) {
        this.status = status;
        this.data = data;
        this.message = message == null ? "" : message;
        this.action = action;
        this.index = index;
        this.bool = bool;
        this.bool2 = bool2;
        this.bool3 = bool3;
    }

    public static <T> Resource<T> success(@NonNull T data) {
        return new Resource<>(Status.SUCCESS, data, null, null, 0, false, false, false);
    }

    public static <T> Resource<T> error(@Nullable String msg, @Nullable T data) {
        return new Resource<>(Status.ERROR, data, msg, null, 0, false, false, false);
    }

    public static <T> Resource<T> loading(@Nullable T data) {
        return new Resource<>(Status.LOADING, data, null, null, 0, false, false, false);
    }

    public static <T> Resource<T> add(@NonNull T data, int index) {
        return new Resource<>(Status.SUCCESS, data, null, Action.ADD, index, false, false, false);
    }

    public static <T> Resource<T> add(@NonNull T data, int index, boolean bool) {
        return new Resource<>(Status.SUCCESS, data, null, Action.ADD, index, bool, false, false);
    }

    public static <T> Resource<T> addPaging(@NonNull T data, int index, boolean bool, boolean bool2) {
        return new Resource<>(Status.SUCCESS, data, null, Action.ADD_PAGING, index, bool, bool2, false);
    }

    public static <T> Resource<T> addUnread(@NonNull T data, int index) {
        return new Resource<>(Status.SUCCESS, data, null, Action.ADD_UNREAD, index, false, false, false);
    }

    public static <T> Resource<T> remove(@NonNull T data, int index) {
        return new Resource<>(Status.SUCCESS, data, null, Action.REMOVE, index, false, false, false);
    }

    public static <T> Resource<T> update(@NonNull T data, int index) {
        return new Resource<>(Status.SUCCESS, data, null, Action.UPDATE, index, false, false, false);

    }
}
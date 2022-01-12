package com.hyapp.achat.model.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Resource<T> {

    public static final int INDEX_ALL = -1;
    public static final int INDEX_NEW = -2;

    @NonNull
    public final Status status;
    @Nullable
    public final T data;
    @NonNull
    public final String message;

    public Action action;
    public int index;
    public boolean bool;

    private Resource(@NonNull Status status, @Nullable T data,
                     @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message == null ? "" : message;
    }

    private Resource(@NonNull Status status, @Nullable T data, String message, Action action) {
        this(status, data, message);
        this.action = action;
    }

    private Resource(@NonNull Status status, @Nullable T data, String message, Action action, int index) {
        this(status, data, message, action);
        this.index = index;
    }

    public Resource(@NonNull Status status, @Nullable T data, @NonNull String message, Action action, int index, boolean bool) {
        this(status, data, message, action, index);
        this.bool = bool;
    }

    public static <T> Resource<T> success(@NonNull T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <T> Resource<T> error(@Nullable String msg, @Nullable T data) {
        return new Resource<>(Status.ERROR, data, msg, null);
    }

    public static <T> Resource<T> loading(@Nullable T data) {
        return new Resource<>(Status.LOADING, data, null, null);
    }

    public enum Status {SUCCESS, ERROR, LOADING}

    public enum Action {ADD, REMOVE, UPDATE}

    public static <T> Resource<T> add(@NonNull T data, int index) {
        return new Resource<>(Status.SUCCESS, data, null, Action.ADD, index);
    }

    public static <T> Resource<T> add(@NonNull T data, int index, boolean bool) {
        return new Resource<>(Status.SUCCESS, data, null, Action.ADD, index, bool);
    }

    public static <T> Resource<T> remove(@NonNull T data, int index) {
        return new Resource<>(Status.SUCCESS, data, null, Action.REMOVE, index);
    }

    public static <T> Resource<T> update(@NonNull T data, int index) {
        return new Resource<>(Status.SUCCESS, data, null, Action.UPDATE, index);
    }
}
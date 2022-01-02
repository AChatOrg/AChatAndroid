package com.hyapp.achat.model.gson;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.hyapp.achat.model.ChatMessage;
import com.hyapp.achat.model.Message;

import java.lang.reflect.Type;

public class MessageDeserializer implements JsonDeserializer<Message> {
    @Override
    public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Message message = new GsonBuilder()
                .registerTypeAdapter(Message.class, new InterfaceAdapter<Message>())
                .create()
                .fromJson(json, typeOfT);
        if (message instanceof ChatMessage) {
            ((ChatMessage) message).setTime(message.getTimeMillis());
        }
        return message;
    }
}

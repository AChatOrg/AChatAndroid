package com.hyapp.achat.model.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.hyapp.achat.model.entity.Contact;
import com.hyapp.achat.model.entity.Message;

import java.lang.reflect.Type;

public class MessageAdapter implements JsonSerializer<Message>, JsonDeserializer<Message> {
    @Override
    public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonElement senderJson = json.getAsJsonObject().get("sender");
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        Message message = gson.fromJson(json, typeOfT);
        Contact sender = gson.fromJson(senderJson, Contact.class);
        message.getSender().setTarget(sender);
        return message;
    }

    @Override
    public JsonElement serialize(Message src, Type typeOfSrc, JsonSerializationContext context) {
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        JsonElement messageJson = gson.toJsonTree(src, typeOfSrc);
        JsonElement senderJson = gson.toJsonTree(src.getSender().getTarget());
        messageJson.getAsJsonObject().add("sender", senderJson);
        return messageJson;
    }
}

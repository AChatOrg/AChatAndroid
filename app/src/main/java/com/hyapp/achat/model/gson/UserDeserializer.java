package com.hyapp.achat.model.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.hyapp.achat.model.entity.Contact;
import com.hyapp.achat.model.entity.Message;
import com.hyapp.achat.model.entity.User;

import java.lang.reflect.Type;

public class UserDeserializer implements JsonDeserializer<User> {

    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject keyJson = json.getAsJsonObject().get("key").getAsJsonObject();
        User user = new Gson().fromJson(json, typeOfT);
        user.setUid(keyJson.get("uid").getAsString());
        user.setRank(keyJson.get("rank").getAsByte());
        user.setScore(keyJson.get("score").getAsInt());
        user.setLoginTime(keyJson.get("loginTime").getAsLong());
        return user;
    }
}

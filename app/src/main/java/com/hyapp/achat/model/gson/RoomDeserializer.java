package com.hyapp.achat.model.gson;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hyapp.achat.model.entity.Room;
import com.hyapp.achat.model.entity.User;

import java.lang.reflect.Type;

public class RoomDeserializer implements JsonDeserializer<Room> {

    @Override
    public Room deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject keyJson = json.getAsJsonObject().get("key").getAsJsonObject();
        Room room = new Gson().fromJson(json, typeOfT);
        room.setUid(keyJson.get("uid").getAsString());
        room.setMemberCount(keyJson.get("memberCount").getAsInt());
        room.setCreateTime(keyJson.get("createTime").getAsLong());
        return room;
    }
}

package com.hyapp.achat.model.gson;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.hyapp.achat.model.People;

import java.lang.reflect.Type;

public class PeopleDeserializer implements JsonDeserializer<People> {
    @Override
    public People deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        People people = new Gson().fromJson(json, typeOfT);
        people.setupGenderCircleRes();
        people.setupRank();
        return people;
    }
}

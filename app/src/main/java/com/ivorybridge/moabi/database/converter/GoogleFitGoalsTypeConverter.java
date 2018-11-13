package com.ivorybridge.moabi.database.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitGoal;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class GoogleFitGoalsTypeConverter {
    private static Gson gson = new Gson();
    @TypeConverter
    public static List<GoogleFitGoal> stringToList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<GoogleFitGoal>>() {}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String ListToString(List<GoogleFitGoal> someObjects) {
        return gson.toJson(someObjects);
    }
}

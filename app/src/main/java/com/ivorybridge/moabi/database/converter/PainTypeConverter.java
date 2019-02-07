package com.ivorybridge.moabi.database.converter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ivorybridge.moabi.database.entity.pain.Pain;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import androidx.room.TypeConverter;

public class PainTypeConverter {

    private static Gson gson = new Gson();
    @TypeConverter
    public static List<Pain> stringToList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<Pain>>() {}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String ListToString(List<Pain> someObjects) {
        return gson.toJson(someObjects);
    }
}

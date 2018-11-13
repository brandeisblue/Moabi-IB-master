package com.ivorybridge.moabi.database.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ivorybridge.moabi.database.entity.appusage.AppUsage;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class AppUsageTypeConverter {

    private static Gson gson = new Gson();
    @TypeConverter
    public static List<AppUsage> stringToList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<AppUsage>>() {}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String ListToString(List<AppUsage> someObjects) {
        return gson.toJson(someObjects);
    }
}

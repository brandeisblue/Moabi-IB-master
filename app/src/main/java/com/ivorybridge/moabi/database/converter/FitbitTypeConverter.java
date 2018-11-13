package com.ivorybridge.moabi.database.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitActivitySummary;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class FitbitTypeConverter {
    private static Gson gson = new Gson();
    @TypeConverter
    public static List<FitbitActivitySummary.Summary.Distance> stringToList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<FitbitActivitySummary.Summary.Distance>>() {}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String ListToString(List<FitbitActivitySummary.Summary.Distance> someObjects) {
        return gson.toJson(someObjects);
    }
}

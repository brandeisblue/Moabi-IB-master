package com.ivorybridge.moabi.database.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitSummary;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class GoogleFitSummariesTypeConverter {
    private static Gson gson = new Gson();
    @TypeConverter
    public static List<GoogleFitSummary.Summary> stringToList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<GoogleFitSummary.Summary>>() {}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String ListToString(List<GoogleFitSummary.Summary> someObjects) {
        return gson.toJson(someObjects);
    }
}

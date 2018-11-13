package com.ivorybridge.moabi.database.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ivorybridge.moabi.database.entity.moodandenergy.MoodAndEnergy;

import java.lang.reflect.Type;
import java.util.List;

public class MoodAndEnergyTypeConverter {

    @TypeConverter
    public static List<MoodAndEnergy> fromString(String value) {
        Type listType = new TypeToken<List<MoodAndEnergy>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayList(List<MoodAndEnergy> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}

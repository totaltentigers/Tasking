package me.jakemoritz.tasking.api.retrofit;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class PlusPersonDeserializer implements JsonDeserializer<String> {

    @Override
    public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String coverPhotoUrl = null;

        JsonObject jsonObject = json.getAsJsonObject();
        JsonObject coverObject = jsonObject.getAsJsonObject("cover");

        if (coverObject != null) {
            JsonObject coverPhotoElement = coverObject.getAsJsonObject("coverPhoto");

            if (coverPhotoElement != null) {
                JsonElement coverPhotoUrlElement = coverPhotoElement.get("url");

                if (coverPhotoUrlElement != null) {
                    coverPhotoUrl = coverPhotoUrlElement.getAsString();
                }
            }
        }

        return coverPhotoUrl;
    }
}

package morningsage.telecraft.data.parsers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Consumer;

public final class TypeParser {

    public static void parseType(JsonObject jsonObject, Consumer<String> callback) {
        JsonElement typeElement = jsonObject.get("type");

        if (typeElement != null && typeElement.isJsonPrimitive()) {
            callback.accept(typeElement.getAsString());
        }
    }
}

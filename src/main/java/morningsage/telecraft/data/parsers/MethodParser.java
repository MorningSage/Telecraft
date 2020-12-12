package morningsage.telecraft.data.parsers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Consumer;

public final class MethodParser {

    public static void parseMethod(JsonObject jsonObject, Consumer<String> callback) {
        JsonElement methodElement = jsonObject.get("method");

        if (methodElement != null && methodElement.isJsonPrimitive()) {
            callback.accept(methodElement.getAsString());
        }
    }
}

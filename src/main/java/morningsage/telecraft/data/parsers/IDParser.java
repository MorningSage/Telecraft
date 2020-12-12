package morningsage.telecraft.data.parsers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Consumer;

public final class IDParser {

    public static void parseID(JsonObject jsonObject, Consumer<Integer> callback) {
        JsonElement idElement = jsonObject.get("id");

        if (idElement != null && idElement.isJsonPrimitive()) {
            callback.accept(Integer.parseInt(idElement.getAsString()));
        }
    }
}

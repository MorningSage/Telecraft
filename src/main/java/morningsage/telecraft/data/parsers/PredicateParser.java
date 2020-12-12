package morningsage.telecraft.data.parsers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Consumer;

public final class PredicateParser {

    public static void parsePredicate(JsonObject jsonObject, Consumer<String> callback) {
        JsonElement predicateElement = jsonObject.get("predicate");

        if (predicateElement != null && predicateElement.isJsonPrimitive()) {
            callback.accept(predicateElement.getAsString());
        }
    }
}

package morningsage.telecraft.data.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Optional;

public final class JsonUtils {
    public static Optional<JsonElement> ifHas(JsonElement jsonElement, String memberName) {
        if (!jsonElement.isJsonObject()) return Optional.empty();

        return ifHas(((JsonObject) jsonElement), memberName);
    }
    public static Optional<JsonElement> ifHas(JsonObject jsonObject, String memberName) {
        if (jsonObject.has(memberName)) {
            return Optional.of(jsonObject.get(memberName));
        }

        return Optional.empty();
    }
}

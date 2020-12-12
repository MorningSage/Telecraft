package morningsage.telecraft.data.parsers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import morningsage.telecraft.data.builders.ParamCallback;

public final class ParamsParser {

    public static void parseParams(JsonObject jsonObject, ParamCallback callback) {
        JsonElement paramsElement = jsonObject.get("params");

        if (paramsElement != null && paramsElement.isJsonArray()) {
            for (JsonElement param : paramsElement.getAsJsonArray()) {
                if (!param.isJsonObject()) continue;
                JsonObject paramObject = param.getAsJsonObject();

                JsonElement nameElement = paramObject.get("name");
                JsonElement typeElement = paramObject.get("type");

                if (nameElement == null || !nameElement.isJsonPrimitive()) continue;
                if (typeElement == null || !typeElement.isJsonPrimitive()) continue;

                callback.onParamFound(nameElement.getAsString(), typeElement.getAsString());
            }
        }
    }
}

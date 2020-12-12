package morningsage.telecraft.data.parsers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import morningsage.telecraft.data.builders.ParamPair;
import morningsage.telecraft.data.builders.ParsedTLObject;
import morningsage.telecraft.data.builders.TLType;

import java.util.function.Consumer;

import static morningsage.telecraft.data.parsers.IDParser.parseID;
import static morningsage.telecraft.data.parsers.MethodParser.parseMethod;
import static morningsage.telecraft.data.parsers.ParamsParser.parseParams;
import static morningsage.telecraft.data.parsers.PredicateParser.parsePredicate;
import static morningsage.telecraft.data.parsers.TypeParser.parseType;

public final class CollectionParser {

    public static void parseCollection(JsonObject jsonObject, TLType collectionType, Consumer<ParsedTLObject> callback) {
        JsonElement jsonElement = jsonObject.get(collectionType.getParseString());
        if (jsonElement == null || !jsonElement.isJsonArray()) return;

        for (JsonElement element : jsonElement.getAsJsonArray()) {
            if (!element.isJsonObject()) continue;
            JsonObject elementObject = element.getAsJsonObject();

            ParsedTLObject.Builder tlObject = ParsedTLObject.builder(collectionType, elementObject);

            parseID(elementObject, tlObject::id);
            parseMethod(elementObject, tlObject::method);
            parsePredicate(elementObject, tlObject::name);
            parseType(elementObject, tlObject::returnType);

            parseParams(elementObject, (name, type) ->
                tlObject.param(new ParamPair(name, type))
            );

            callback.accept(tlObject.build());
        }
    }

}

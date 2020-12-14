package morningsage.telecraft.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import morningsage.telecraft.data.builders.ParsedTLObject;
import morningsage.telecraft.data.builders.TLType;
import morningsage.telecraft.data.utils.WebUtils;
import net.minecraft.data.DataCache;
import net.minecraft.data.DataProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Optional;

import static morningsage.telecraft.data.parsers.CollectionParser.parseCollection;

@RequiredArgsConstructor
public class TLParser implements DataProvider {
    @Getter @NonNull private final Path output;
    @Getter private final String name = "TLParser";

    public static final Hashtable<Integer, ParsedTLObject> TL_OBJECTS = new Hashtable<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String[] TL_SOURCE_URLS = {
        "https://core.telegram.org/schema/mtproto-json",   // api.tl
        "https://core.telegram.org/schema/json",           // mtproto.tl
        "https://core.telegram.org/schema/end-to-end-json" // secrets.tl
    };

    @Override
    public void run(DataCache cache) {
        for (String sourceUrl : TL_SOURCE_URLS) {
            final JsonObject jsonObject = GSON.fromJson(
                WebUtils.downloadURLAsString(sourceUrl),
                JsonObject.class
            );

            parseCollection(jsonObject, TLType.CONSTRUCTOR, TLParser::addParsedObject);
            parseCollection(jsonObject, TLType.METHOD, TLParser::addParsedObject);
        }

        createObjectFiles(this.output, cache);
    }

    private static void createObjectFiles(Path path, DataCache cache) {
        final String parentPackage = "morningsage.telecraft.tlobjects";

        for (ParsedTLObject tlObject : TL_OBJECTS.values()) {
            //if (tlObject.getCollectionType() == TLType.CONSTRUCTOR) {
            try {
                tlObject.saveAsClass(
                    path, cache, TL_OBJECTS.values(),
                    parentPackage
                );
            } catch (Exception exception) {
                Generator.LOGGER.error("Failed to create class", exception);
            }
            //}
        }

        ParsedTLObject.createAbstractClasses(path, cache, parentPackage);
    }

    private static void addParsedObject(ParsedTLObject tlObject) {
        TL_OBJECTS.put(tlObject.getId(), tlObject);
    }
}
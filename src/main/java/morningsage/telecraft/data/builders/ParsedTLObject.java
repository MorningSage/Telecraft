package morningsage.telecraft.data.builders;

import com.google.common.base.CaseFormat;
import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import morningsage.telecraft.data.utils.FileUtils;
import net.minecraft.data.DataCache;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Builder(builderClassName = "Builder")
public class ParsedTLObject {
    private final String name;
    @Getter private final String method;
    @Getter private final int id;
    @Getter private final String returnType;
    @Singular() private final List<ParamPair> params;

    @Getter private final TLType collectionType;
    @Getter private final JsonObject rawData;

    final List<String> imports = new ArrayList<String>() {{
        add("lombok.Getter;");
    }};

    public static Builder builder(TLType collectionType, JsonObject rawData) {
        return new Builder().collectionType(collectionType).rawData(rawData);
    }

    public void saveAsClass(Path parent, DataCache cache, Collection<ParsedTLObject> values) {
        final StringBuilder classText = new StringBuilder();
        final String classPath = getClassPackage(getName());
        final boolean hasExtends = extendsAbstractClass();
        final boolean isExtended = isAbstractClass(values);
        final String className = getClassName(getName());
        if (hasExtends) saveAbstractClass(parent, cache);
        
        classText.append("package morningsage.telecraft.tlobjects");
        if (classPath.length() > 0) classText.append(".").append(classPath);
        classText.append(";\n\n");
        if (classPath.length() > 0 && !hasExtends) {
            imports.add("morningsage.telecraft.tlobjects.TLObject;");
        }
        for (ParamPair param : params) {
            param.provideImports(imports, classPath, className);
        }
        Collections.sort(imports);
        for (String importLine : imports) {
            classText.append("import ").append(importLine).append("\n");
        }
        classText.append("\npublic ");
        if (isExtended) classText.append("abstract ");
        classText.append("class ").append(className);
        if (id == 0x1CB5C415) classText.append("<T>");
        if (isExtended) classText.append("<T extends ").append(className).append("<T>>");
        if (hasExtends) {
            classText.append(" extends ").append(getClassName(returnType)).append("<").append(className).append(">");
        } else {
            classText.append(" extends TLObject<");
            if (isExtended) {
                classText.append("T");
            } else {
                classText.append(className);
            }
            if (id == 0x1CB5C415) classText.append("<T>");
            classText.append(">");
        }
        classText.append(" {\n\t@Getter private static final int ID = ").append(getHexID()).append(";\n\n");
        for (ParamPair param : params) {
            param.declareParam(classText, classPath, className);
        }
        classText.append("\n}");

        FileUtils.writeToPath(classText.toString(), cache, getClassFilePath(parent, getName()));
    }

    public void saveAbstractClass(Path parent, DataCache cache) {
        final Path filePath = getClassFilePath(parent, returnType);

        if (!Files.exists(filePath)) {
            final StringBuilder classText = new StringBuilder();
            final String classPath = getClassPackage(returnType);

            classText.append("package morningsage.telecraft.tlobjects");
            if (!classPath.equals("")) {
                classText.append(".").append(classPath).append(";\n\n");
                classText.append("import morningsage.telecraft.tlobjects.TLObject");
            }
            classText.append(";\n\n");
            classText.append("public abstract class ")
                .append(getClassName(returnType))
                .append("<T extends ")
                .append(getClassName(returnType))
                .append("<T>> extends TLObject<T> {\n\n}");

            FileUtils.writeToPath(classText.toString(), cache, filePath);
        }
    }

    public static void createAbstractTL(Path parent, DataCache cache) {
        final Path filePath = getClassFilePath(parent, "TLObject");

        if (!Files.exists(filePath)) {
            final StringBuilder classText = new StringBuilder();

            classText.append("package morningsage.telecraft.tlobjects;\n\n");
            classText.append("public abstract class TLObject<T extends TLObject<T>> {\n");
            classText.append("\tpublic abstract T deserialize();\n");
            classText.append("\tpublic abstract byte[] serialize();\n");

            classText.append("\n}");

            FileUtils.writeToPath(classText.toString(), cache, filePath);
        }
    }


    public boolean extendsAbstractClass() {
        return id != 0x1CB5C415 && !getClassName(getName()).toLowerCase().equals(getClassName(getReturnType()).toLowerCase());
    }
    public boolean isAbstractClass(Collection<ParsedTLObject> values) {
        String safeClassName = getName().toLowerCase();

        // If cache this value, but we will only only need this once...

        for (ParsedTLObject object : values) {
            if (object.getCollectionType() == TLType.METHOD || !object.extendsAbstractClass()) continue;
            if (object.getReturnType().toLowerCase().equals(safeClassName)) {
                return true;
            }
        }

        return false;
    }

    private static Path getClassFilePath(Path root, String input) {
        String path = getClassPackage(input);
        if (!path.equals("")) path = path + "/";
        return root.resolve(path + getClassName(input) + ".java");
    }
    public static String getClassPackage(String input) {
        final String[] matches = input.toLowerCase().split("\\.");
        return String.join(".", Arrays.copyOf(matches, matches.length - 1));
    }
    public static String getClassName(String input) {
        if (input.contains("<")) input = input.substring(0, input.indexOf("<"));
        final String[] matches = input.split("\\.");

        if (input.contains("_")) {
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, matches[matches.length - 1]);
        } else {
            return StringUtils.capitalize(matches[matches.length - 1]);
        }
    }
    public static String getFieldName(String input) {
        final String[] matches = input.split("\\.");

        if (input.contains("_")) {
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, matches[matches.length - 1]);
        } else {
            return matches[matches.length - 1];
        }
    }

    public String getHexID() {
        return "0x" + Integer.toHexString(id).toUpperCase();
    }

    public String getKey() {
        if (collectionType == TLType.CONSTRUCTOR) {
            return name;
        }

        if (collectionType == TLType.METHOD) {
            return method;
        }

        return String.valueOf(id);
    }

    public String getName() {
        if (id == 0x1CB5C415) return "Vector";
        return name;
    }
}

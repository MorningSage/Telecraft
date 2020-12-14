package morningsage.telecraft.data.builders;

import com.google.common.base.CaseFormat;
import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import morningsage.telecraft.data.utils.FileUtils;
import net.minecraft.data.DataCache;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Builder(builderClassName = "Builder")
public class ParsedTLObject {
    private final String name;
    @Getter private final String method;
    @Getter private final int id;
    @Getter private final String returnType;
    @Singular() private final List<ParamPair> params;
    @Nullable private Boolean isExtended;

    @Getter private final TLType collectionType;
    @Getter private final JsonObject rawData;

    final List<String> imports = new ArrayList<String>() {{
        add("lombok.Getter;");
        add("java.io.DataInputStream;");
        add("java.io.DataOutputStream;");
        add("java.io.IOException;");
        add("morningsage.telecraft.utils.StreamUtils;");
    }};

    public static Builder builder(TLType collectionType, JsonObject rawData) {
        return new Builder().collectionType(collectionType).rawData(rawData);
    }

    public void saveAsClass(Path parent, DataCache cache, Collection<ParsedTLObject> values, String parentPackage) throws Exception {
        if (getCollectionType() == TLType.METHOD) saveTLMethod(parent.resolve("methods"), cache, values, parentPackage + ".methods");
        else if (getCollectionType() == TLType.CONSTRUCTOR) saveTLObject(parent.resolve("types"), cache, values, parentPackage + ".types");
        else throw new Exception("Unknown TL Type");
    }
    private void saveTLMethod(Path parent, DataCache cache, Collection<ParsedTLObject> values, String parentPackage) {

    }
    private void saveTLObject(Path parent, DataCache cache, Collection<ParsedTLObject> values, String parentPackage) {
        final StringBuilder classText = new StringBuilder();
        final String classPath = getClassPackage(getName());
        final boolean hasExtends = extendsAbstractClass();
        final boolean isExtended = isAbstractClass(values);
        final String className = getClassName(getName());

        final Function<String, String> qualifier = input -> parentPackage + "." + input;

        if (hasExtends) saveAbstractClass(parent, cache, parentPackage);

        classText.append("package ").append(parentPackage);
        if (classPath.length() > 0) classText.append(".").append(classPath);
        classText.append(";\n\n");
        if (classPath.length() > 0 && !hasExtends) {
            imports.add(qualifier.apply("TLObject;"));
        }
        for (ParamPair param : params) {
            param.provideImports(imports, classPath, className, qualifier);
        }
        Collections.sort(imports);
        boolean hasOptional = false;
        for (String importLine : imports) {
            if (importLine.contains("Optional")) hasOptional = true;
            classText.append("import ").append(importLine).append("\n");
        }
        if (hasOptional) classText.append("\n@SuppressWarnings(\"OptionalUsedAsFieldOrParameterType\")");
        classText.append("\npublic class ").append(className);
        if (isVector()) classText.append("<T>");
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
            if (isVector()) classText.append("<T>");
            classText.append(">");
        }
        classText.append(" {\n\t@Getter private static final int ID = ").append(getHexID()).append(";\n\n");
        if (params.size() > 0) {
            for (ParamPair param : params) {
                param.declareParam(classText, classPath, className, values, qualifier);
            }
            classText.append("\n");
        }
        addDeserializationLogic(classText, classPath, className, values, qualifier);
        classText.append("\n\n");
        addSerializationLogic(classText);
        classText.append("\n}");

        FileUtils.writeToPath(classText.toString(), cache, getClassFilePath(parent, getName()));
    }
    public void saveAbstractClass(Path parent, DataCache cache, String parentPackage) {
        final Path filePath = getClassFilePath(parent, returnType);

        if (!Files.exists(filePath)) {
            final StringBuilder classText = new StringBuilder();
            final String classPath = getClassPackage(returnType);

            classText.append("package ").append(parentPackage);
            if (!classPath.equals("")) {
                classText.append(".").append(classPath).append(";\n\n");
                classText.append("import ").append(parentPackage).append(".TLObject");
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

    public static void createAbstractClasses(Path parent, DataCache cache, String parentPackage) {
        createAbstractTL(parent.resolve("types"), cache, parentPackage + ".types");
    }
    private static void createAbstractTL(Path parent, DataCache cache, String parentPackage) {
        final Path filePath = getClassFilePath(parent, "TLObject");

        if (!Files.exists(filePath)) {
            final StringBuilder classText = new StringBuilder();

            classText.append("package ").append(parentPackage).append(";\n\n");
            classText.append("import org.reflections.Reflections;\n");
            classText.append("import java.io.DataInputStream;\n");
            classText.append("import java.io.DataOutputStream;\n");
            classText.append("import java.io.IOException;\n");
            classText.append("import java.lang.reflect.Constructor;\n");
            classText.append("import java.lang.reflect.InvocationTargetException;\n");
            classText.append("import java.lang.reflect.Method;\n");
            classText.append("import java.util.HashMap;\n");
            classText.append("import java.util.Set;\n");
            classText.append("import javax.annotation.Nullable;\n\n");
            classText.append("public abstract class TLObject<T extends TLObject<T>> {\n");
            classText.append("\tprivate static final HashMap<Integer, Class<?>> ID_MAP = new HashMap<>();\n\n");
            classText.append("\tpublic abstract T deserialize(DataInputStream data) throws IOException;\n");
            classText.append("\tpublic abstract DataOutputStream serialize(DataOutputStream data);\n\n");
            classText.append("\tprivate static void refreshObjectMap() {\n");
            classText.append("\t\tID_MAP.clear();\n\n");
            classText.append("\t\t@SuppressWarnings(\"rawtypes\")\n");
            classText.append("\t\tSet<Class<? extends TLObject>> subTypes = new Reflections(\"");
            classText.append(parentPackage);
            classText.append("\").getSubTypesOf(TLObject.class);\n\n");
            classText.append("\t\tfor (Class<?> clazz : subTypes) {\n");
            classText.append("\t\t\ttry {\n");
            classText.append("\t\t\t\tfor (Method method : clazz.getMethods()) {\n");
            classText.append("\t\t\t\t\tif (method.getName().equals(\"getID\") && method.getReturnType() == int.class) {\n");
            classText.append("\t\t\t\t\t\tID_MAP.put((int) method.invoke(null), clazz);\n");
            classText.append("\t\t\t\t\t}\n");
            classText.append("\t\t\t\t}\n");
            classText.append("\t\t\t} catch (Exception ignored) { }\n");
            classText.append("\t\t}\n");
            classText.append("\t}\n\n");
            classText.append("\t@Nullable @SuppressWarnings(\"unchecked\")\n");
            classText.append("\tpublic static TLObject<?> deserializeObject(DataInputStream data) {\n");
            classText.append("\t\tif (ID_MAP.isEmpty()) refreshObjectMap();\n");
            classText.append("\t\tException error;\n");
            classText.append("\t\tint id = -1;\n");
            classText.append("\t\tClass<? extends TLObject<?>> clazz = null;\n\n");
            classText.append("\t\ttry {\n");
            classText.append("\t\t\tid = data.readInt();\n");
            classText.append("\t\t\tclazz = (Class<? extends TLObject<?>>) ID_MAP.get(id);\n");
            classText.append("\t\t\tConstructor<? extends TLObject<?>> constructor = clazz.getConstructor();\n");
            classText.append("\t\t\tTLObject<?> instance = constructor.newInstance();\n");
            classText.append("\t\t\treturn instance.deserialize(data);\n");
            classText.append("\t\t} catch (IOException exception) {\n");
            classText.append("\t\t\tSystem.err.println(\"Failed to read a TLObject id!\");\n");
            classText.append("\t\t\terror = exception;\n");
            classText.append("\t\t} catch (NoSuchMethodException exception) {\n");
            classText.append("\t\t\tSystem.err.println(\"Failed to find a constructor for TLObject: \" + id);\n");
            classText.append("\t\t\terror = exception;\n");
            classText.append("\t\t} catch (InstantiationException exception) {\n");
            classText.append("\t\t\tSystem.err.println(\"Failed to instantiate of abstract TLObject: \" + clazz.toString());\n");
            classText.append("\t\t\terror = exception;\n");
            classText.append("\t\t} catch (IllegalAccessException| InvocationTargetException exception) {\n");
            classText.append("\t\t\tSystem.err.println(\"Failed to create a new instance of: \" + clazz.toString());\n");
            classText.append("\t\t\terror = exception;\n");
            classText.append("\t\t}\n\n");
            classText.append("\t\terror.printStackTrace(System.err);\n");
            classText.append("\t\treturn null;\n");
            classText.append("\t}\n\n");
            classText.append("\t@Nullable @SuppressWarnings(\"unchecked\")\n");
            classText.append("\tpublic static <T extends TLObject<?>> T deserializeObject(Class<? super T> clazz, DataInputStream data) {\n");
            classText.append("\t\tTLObject<?> tlObject = deserializeObject(data);\n\n");
            classText.append("\t\tif (tlObject != null && tlObject.getClass() == clazz) {\n");
            classText.append("\t\t\treturn (T) tlObject;\n");
            classText.append("\t\t}\n\n");
            classText.append("\t\treturn null;\n");
            classText.append("\t}\n");
            classText.append("\n}");

            FileUtils.writeToPath(classText.toString(), cache, filePath);
        }
    }


    public boolean extendsAbstractClass() {
        return !isVector() && !getClassName(getName()).toLowerCase().equals(getClassName(getReturnType()).toLowerCase());
    }
    public boolean isAbstractClass(Collection<ParsedTLObject> values) {
        if (isExtended == null) {
            String safeClassName = formatClassName(getName()).toLowerCase();

            for (ParsedTLObject object : values) {
                if (object.getCollectionType() == TLType.METHOD || !object.extendsAbstractClass()) continue;
                if (formatClassName(object.getReturnType()).toLowerCase().equals(safeClassName)) {
                    isExtended = true;
                    break;
                }
            }

            if (isExtended == null) isExtended = false;
        }

        return isExtended;
    }

    public void addDeserializationLogic(StringBuilder classText, String classPackage, String parentClassName, Collection<ParsedTLObject> values, Function<String, String> qualifier) {
        boolean isAbstract = isAbstractClass(values);

        if (isAbstract && params.size() < 1) return;

        classText.append("\t@Override\n");
        classText.append("\tpublic ");

        if (isAbstract) {
            classText.append("T");
        } else {
            classText.append(getClassName(getName()));
        }

        classText.append(" deserialize(DataInputStream data) throws IOException {\n");
        if (params.size() > 0) {
            for (ParamPair param : params) {
                param.addDeserializationLogic(classText, classPackage, parentClassName, qualifier);
            }
            classText.append("\n");
        }
        classText.append("\t\treturn ");
        if (isAbstract) classText.append("(T) ");
        classText.append("this;\n\t}");
    }
    public void addSerializationLogic(StringBuilder classText) {
        classText.append("\t@Override\n");
        classText.append("\tpublic DataOutputStream serialize(DataOutputStream data) {\n");
        if (params.size() > 0) {
            for (ParamPair param : params) {
                param.addSerializationLogic(classText, params);
            }
            classText.append("\n");
        }
        classText.append("\t\treturn data;\n\t}");
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

        return formatClassName(matches[matches.length - 1]);
    }
    public static String getFieldName(String input) {
        final String[] matches = input.split("\\.");

        if (input.contains("_")) {
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, matches[matches.length - 1]);
        } else {
            return matches[matches.length - 1];
        }
    }
    public static String formatSnakeGenerics(String input) {
        // Specifically match a generic
        String genericPattern = "<(\\w+_\\w+)>";
        Pattern compiledPattern = Pattern.compile(genericPattern);
        Matcher matcher = compiledPattern.matcher(input);

        // Matches this input
        if (matcher.find()) {
            String matched = matcher.group(1);
            Matcher innerMatcher = compiledPattern.matcher(matched);

            // Matches a match (nested Generics like Vector<Vector<future_salts>>)
            if (innerMatcher.find()) {
                // Recursive calls to match (almost) unlimited nests
                return formatSnakeGenerics(matched);
            }

            // Update the input
            input = matcher.replaceFirst("<" + ParsedTLObject.getClassName(matched) + ">");
        }

        // Return the input (changed or original)
        return input;
    }
    public static String formatClassName(String input) {
        if (input.contains("_")) {
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, input);
        } else {
            return StringUtils.capitalize(input);
        }
    }

    public boolean isVector() {
        return id == 0x1CB5C415;
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
        if (isVector()) return "Vector";
        return name;
    }
}

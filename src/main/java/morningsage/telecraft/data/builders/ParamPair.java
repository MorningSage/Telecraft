package morningsage.telecraft.data.builders;

import lombok.Data;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class ParamPair {
    private final String name;
    private final String type;

    private static final String SETTER_IMPORT = "lombok.Setter;";
    private static final String BIGINT_IMPORT = "java.math.BigInteger;";
    private static final String OPTIONAL_IMPORT = "java.util.Optional;";
    private static final String[] IGNORED_IMPORTS = {
        "string", "flags.", "int", "byte[]", "long", "true", "boolean"
    };
    private static final Pattern FLAGS_TYPE = Pattern.compile("flags\\.(\\d)\\?true", Pattern.CASE_INSENSITIVE);

    public void provideImports(List<String> imports, String classPackage, String parentClassName, Function<String, String> qualifier) {
        // In the presence of flags, we use Optional<T>
        if (getName().equals("flags") && !imports.contains(OPTIONAL_IMPORT)) imports.add(OPTIONAL_IMPORT);

        // At this point, there will be a property, and we'll need the Setter imported
        if (!imports.contains(SETTER_IMPORT)) imports.add(SETTER_IMPORT);

        /*
         * Types could be (but not limited to):
         *
         * flags.X?Type
         * package.Type
         * package.Type<Type>
         * Type<Type>
         * Type<package.Type>
         */

        // A list of types used for this param
        List<String> typeStrings = getTypeString(true, classPackage, parentClassName, qualifier);

        // TLObject needs to be imported if not local for deserializing if a class is part of Telegram
        if (typeStrings.size() > 0 && !classPackage.equals("")) {
            List<String> tlobjectImports = getTypeStringInternal("TLObject", true, classPackage, parentClassName, qualifier);
            if (tlobjectImports.size() > 0) {
                String tlobjectImport = tlobjectImports.get(0) + ";";
                if (!imports.contains(tlobjectImport)) imports.add(tlobjectImport);
            }
        }

        // Loop through the different parts
        for (String part : typeStrings) {
            // Only for case insensitive checks
            final String typeClassName = ParsedTLObject.getClassName(part);

            // Only import if qualified the classes are not the same
            if (part.startsWith("morningsage") && !typeClassName.equals(parentClassName)) {
                part = part + ";";
                if (!imports.contains(part)) imports.add(part);
                continue;
            }

            // The BigInteger import is added manually
            if (part.equals("BigInteger") && !imports.contains(BIGINT_IMPORT)) {
                imports.add(BIGINT_IMPORT);
            }
        }
    }

    public void declareParam(StringBuilder classText, String classPackage, String parentClassName, Collection<ParsedTLObject> values, Function<String, String> qualifier) {
        String typeString = getTypeString(false, classPackage, parentClassName, qualifier).get(0);

        // ToDo: determine if raw and append "<?>" if necessary

        classText.append(
            MessageFormat.format(
                "\t@Getter @Setter private {0} {1};\n",
                typeString, ParsedTLObject.getFieldName(getName())
            )
        );
    }

    public void addDeserializationLogic(StringBuilder classText, String classPackage, String parentClassName, Function<String, String> qualifier) {
        String type = getType(false).toLowerCase();
        String printableName = ParsedTLObject.getFieldName(getName());

        classText.append("\t\tthis.");

        if (type.startsWith("flags.")) {
            int i = Integer.parseInt(type.substring(6, type.indexOf("?")));

            if (type.endsWith("true")) {
                classText.append(printableName).append(" = (flags & ").append((int) Math.pow(2, i)).append(") != 0;\n");
                return;
            }

            classText.append(printableName).append(" = (flags & ").append((int) Math.pow(2, i)).append(") == 0 ? ").append(getDefaultValueCode()).append(" : ").append(getAssignmentCode(classPackage, parentClassName, qualifier)).append(";\n");
            return;
        }

        classText.append(printableName).append(" = ").append(getAssignmentCode(classPackage, parentClassName, qualifier)).append(";\n");
    }
    private String getAssignmentCode(String classPackage, String parentClassName, Function<String, String> qualifier) {
        String typeString = getTypeString(false, classPackage, parentClassName, qualifier).get(0);
        return getAssignmentCode(typeString);
    }
    private String getAssignmentCode(String typeString) {
        switch (typeString) {
            case "boolean":
                return "StreamUtils.readBoolean(data)";
            case "int":
            case "Integer":
                return "data.readInt()";
            case "String":
                return "StreamUtils.readString(data)";
            case "long":
                return "data.readLong()";
            case "byte[]":
                return "StreamUtils.readBytes(data)";
            default:
                if (typeString.startsWith("Optional") || typeString.startsWith("Vector")) {
                    String type1 = typeString.substring(0, typeString.indexOf("<"));
                    String type2 = typeString.substring(typeString.indexOf("<") + 1, typeString.lastIndexOf(">"));
                    String assignment = getAssignmentCode(type2);

                    if (assignment.startsWith("TLObject")) {
                        return type1 + ".ofNullable(" + assignment + ")";
                    } else {
                        return type1 + ".of(" + assignment + ")";
                    }
                }
        }

        return "TLObject.deserializeObject(" + typeString + ".class, data)";
    }
    private String getDefaultValueCode() {
        String typeString = getTypeString(false, "", "", s -> s).get(0);

        switch (typeString) {
            case "boolean":
                return "false";
            case "int":
            case "Integer":
            case "long":
                return "-1";
            case "String":
                return "\"\"";
            case "byte[]":
                return "new byte[0]";
            default:
                if (typeString.startsWith("Optional") || typeString.startsWith("Vector")) {
                    return typeString.substring(0, typeString.indexOf("<")) + ".empty()";
                }
        }

        return "null";
    }

    public void addSerializationLogic(StringBuilder classText, List<ParamPair> allParams) {
        if (getType().startsWith("flag")) return;



        classText.append("\t\t// ToDo: ").append(this).append("\n");
    }

    public List<String> getTypeString(boolean forImport, String classPackage, String parentClassName, Function<String, String> qualifier) {
        return getTypeStringInternal(getType(), forImport, classPackage, parentClassName, qualifier);
    }

    private List<String> getTypeStringInternal(String input, boolean forImport, String parentClassPackage, String parentClassName, Function<String, String> qualifier) {
        // Only for case insensitive checks
        final String safeType = input.toLowerCase();
        final List<String> returns = new ArrayList<>();

        // Special check for boolean flag values
        if (safeType.equals("optional<true>")) {
            // Replace with the boolean type
            returns.add("boolean");
            // Return early
            return returns;
        }

        /*
         * Below is determining the type.  It may differ based
         * on if we are importing the class or declaring a field.
         */

        // If this has a generic type, handle slightly differently
        if (safeType.startsWith("vector<") || safeType.startsWith("optional<")) {
            // Capture the Vector or Optional
            String type = input.substring(0, input.indexOf("<"));

            // Remove that part of the type
            input = input.substring(input.indexOf("<") + 1, input.lastIndexOf(">"));

            // Determine the qualified type if it's part of telegram
            if (!type.toLowerCase().equals("optional")) {
                type = getSafeType(type, forImport, parentClassPackage, parentClassName, qualifier);
            }

            // Determine the qualified generic
            input = getSafeType(input, forImport, parentClassPackage, parentClassName, qualifier);

            // Determine what exactly to return
            if (forImport) {
                // For importing we may need both types
                if (!type.toLowerCase().equals("optional")) returns.add(type);
                returns.add(input);
            } else {
                // When declaring, we just combine them again
                returns.add(type + "<" + input + ">");
            }

            // return early
            return returns;
        }

        // Flagged/Optional fields contain their type
        if (safeType.startsWith("flags.")) {
            // Remove the flag info as it's not needed for the type
            input = input.substring(input.indexOf("?") + 1);
            // Recursive call to process the remaining bit
            returns.addAll(getTypeStringInternal(input, forImport, parentClassPackage, parentClassName, qualifier));
            // Return early
            return returns;
        }

        // Special conditioning for ignored core types so we don't qualify "true" or something
        if (forImport) {
            for (String ignoredImport : IGNORED_IMPORTS) {
                if (safeType.startsWith(ignoredImport.toLowerCase())) {
                    // Return early
                    return returns;
                }
            }
        }

        /*
         * At this point, we can assume that whatever we are processing
         * is just a plain type.  It could be in the format of:
         *
         * - package.Type
         * - Type
         *
         * Let's break those down
         */

        // Determine the class package of the type
        String typePackage = ParsedTLObject.getClassPackage(input);
        String typeClassName = input;

        // Remove the package (if it exists) from the name
        if (typePackage.length() > 0) {
            typeClassName = typeClassName.substring(typePackage.length() + 1);
        }

        /*
         * We may also need to qualify the type in different situations:
         *
         * - Class names are the same
         *   - Implies NOT in the same package
         *   - Return qualified
         * - Class packages are the same
         *   - Implies names are not the same
         *   - Implies no need to import
         *   - return NOT qualified
         * - Classes are not in the same package and not the same name
         *   - return qualified if importing
         *
         * The following could be simplified, but it's left but it's
         * left as-is to make it more understandable.
         */

        // Class names are the same
        if (typeClassName.equals(parentClassName)) {
            /*
             * Implies NOT in the same package
             */

            // Add the qualified type
            returns.add(qualifier.apply(input));
            // Return early
            return returns;
        }

        // Class packages are the same
        if (typePackage.equals(parentClassPackage)) {
            /*
             * Implies names are not the same
             * Implies no need to import
             */

            // Add the NON qualified type
            returns.add(typeClassName);
            // Return early
            return returns;
        }

        /*
         * Classes are not in the same package and not the same name
         */

        if (forImport) {
            // Add the qualified type
            returns.add(qualifier.apply(input));
        } else {
            // Add the NON qualified type
            returns.add(typeClassName);
        }

        return returns;
    }

    private String getSafeType(String input, boolean forImport, String parentClassPackage, String parentClassName, Function<String, String> qualifier) {
        // Get this type individually so we can figure out what to do with it
        List<String> qualifiedType = getTypeStringInternal(input, forImport, parentClassPackage, parentClassName, qualifier);
        // Replace with the correct one if we found it
        if (qualifiedType.size() > 0) input = qualifiedType.get(0);
        // Return what was passed or the new value
        return input;
    }

    public String getName() {
        switch (name) {
            case "public":
                return "isPublic";
            case "static":
                return "isStatic";
            case "lat":
                return "latitude";
            case "long":
                return "longitude";
            case "default":
                return "isDefault";
            case "final":
                return "isFinal";
        }

        return name;
    }


    public String getType(boolean formatted) {
        if (!formatted) return type;

        String formattedType = type;

        // Fix vector<%Message>
        formattedType = formattedType.replace("<%M", "<M");
        // Flag types are really integers
        formattedType = formattedType.replace("#", "int");
        // Ensure correct capitalization
        formattedType = formattedType.replace("vector", "Vector");
        formattedType = formattedType.replace("string", "String");
        formattedType = formattedType.replace("JSONObjectValue", "JsonObjectValue");
        // There's no such thing as bytes...
        formattedType = formattedType.replace("bytes", "byte[]");
        // Generics (Vector<T> or Optional<T> in this case) must be Objects and primitives are not Objects
        formattedType = formattedType.replace("<long>", "<Long>");
        formattedType = formattedType.replace("<int>", "<Integer>");
        formattedType = formattedType.replace("?int", "?Integer");
        formattedType = formattedType.replace("?long", "?Long");
        // Using BigIntegers for now, but that may change
        formattedType = formattedType.replace("int128", "BigInteger");
        formattedType = formattedType.replace("int256", "BigInteger");

        // For Optional/Flag types, we need a bit more handling...
        if (formattedType.startsWith("flags.")) {
            formattedType = "Optional<" + formattedType.substring(formattedType.indexOf("?") + 1) + ">";
        }

        formattedType = ParsedTLObject.formatSnakeGenerics(formattedType);

        return formattedType;
    }

    public String getType() {
        return getType(true);
    }
}

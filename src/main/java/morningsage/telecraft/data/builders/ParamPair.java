package morningsage.telecraft.data.builders;

import lombok.Data;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class ParamPair {
    private final String name;
    private final String type;

    private static final String SETTER_IMPORT = "lombok.Setter;";
    private static final String BIGINT_IMPORT = "java.math.BigInteger;";
    private static final String[] IGNORED_IMPORTS = {
        "string", "flags.", "int", "byte[]", "long", "true", "boolean"
    };
    //private static final Pattern FLAGS_TYPE = Pattern.compile("flags\\.(\\d)\\?true", Pattern.CASE_INSENSITIVE);

    public void provideImports(List<String> imports, String classPackage, String parentClassName) {
        // Ignore the flag variable
        if (getName().equals("flags")) return;

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

        // Loop through the different parts
        for (String part : getTypeString(true, classPackage, parentClassName)) {
            // Only for case insensitive checks
            final String typeClassName = ParsedTLObject.getClassName(part);

            // Only import if qualified and the classes are not the same
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

    public void declareParam(StringBuilder classText, String classPackage, String parentClassName) {
        // No access to flags directly
        if (getName().equals("flags")) return;

        classText.append(
            MessageFormat.format(
                "\t@Getter @Setter private {0} {1};\n",
                getTypeString(false, classPackage, parentClassName).get(0),
                ParsedTLObject.getFieldName(getName())
            )
        );
    }

    public void addDeserializationLogic(StringBuilder classText) {
        if (getName().equals("flags")) return;
        classText.append("\t\t// ToDo: ").append(this).append("\n");
    }
    public void addSerializationLogic(StringBuilder classText, List<ParamPair> allParams) {
        if (getType().startsWith("flag")) return;
        classText.append("\t\t// ToDo: ").append(this).append("\n");
    }

    public List<String> getTypeString(boolean forImport, String classPackage, String parentClassName) {
        return getTypeStringInternal(getType(), forImport, classPackage, parentClassName);
    }

    private List<String> getTypeStringInternal(String input, boolean forImport, String parentClassPackage, String parentClassName) {
        // Only for case insensitive checks
        final String safeType = input.toLowerCase();
        final List<String> returns = new ArrayList<>();

        // Special check for boolean flag values
        if (safeType.equals("true")) {
            // Replace with the boolean type
            returns.add("boolean");
            // Return early
            return returns;
        }

        /*
         * Below is determining the type.  It may differ based
         * on if we are importing the class or declaring a field.
         */

        // If this is a vector, we handle differently
        if (safeType.startsWith("vector<")) {
            // Remove that part of the type
            input = input.substring(7, input.lastIndexOf(">"));

            // Get these types individually so we can figure out what to do with them
            List<String> vectorType = getTypeStringInternal("Vector", forImport, parentClassPackage, parentClassName);
            List<String> genericType = getTypeStringInternal(input, forImport, parentClassPackage, parentClassName);

            // Sanity Check default to expected values
            if (vectorType.size() < 1) vectorType.add("Vector");
            if (genericType.size() < 1) genericType.add(input);

            // Determine what exactly to return
            if (forImport) {
                // For importing we need both types
                returns.add(vectorType.get(0));
                returns.add(genericType.get(0));
            } else {
                // When declaring, we just combine them again
                returns.add(vectorType.get(0) + "<" + genericType.get(0) + ">");
            }

            // return early
            return returns;
        }

        // Flagged fields contain their type
        if (safeType.startsWith("flags.")) {
            // Remove the flag info as it's not needed for the type
            input = input.substring(input.indexOf("?") + 1);
            // Recursive call to process the remaining bit
            returns.addAll(getTypeStringInternal(input, forImport, parentClassPackage, parentClassName));
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
            returns.add("morningsage.telecraft.tlobjects." + input);
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
            returns.add("morningsage.telecraft.tlobjects." + input);
        } else {
            // Add the NON qualified type
            returns.add(typeClassName);
        }

        return returns;
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

    public String getType() {
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
        // Generics (Vector<T> in this case) must be Objects and primitives are not Objects
        formattedType = formattedType.replace("<long>", "<Long>");
        formattedType = formattedType.replace("<int>", "<Integer>");
        // Using BigIntegers for now, but that may change
        formattedType = formattedType.replace("int128", "BigInteger");
        formattedType = formattedType.replace("int256", "BigInteger");

        formattedType = ParsedTLObject.formatSnakeGenerics(formattedType);

        return formattedType;
    }
}

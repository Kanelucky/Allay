package org.allaymc.codegen;

import com.google.gson.JsonParser;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;
import lombok.SneakyThrows;

import javax.lang.model.element.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author IWareQ
 */
public class ComplexRecipeIdsGen {
    static final Path RECIPES_PATH = Path.of(CodeGenConstants.DATA_PATH + "recipes.json");
    static final Path OUTPUT_PATH = Path.of("server/src/main/java/org/allaymc/server/item/recipe/ComplexRecipeIds.java");

    private static final Map<String, String> NAMES_BY_UUID = Map.ofEntries(
            Map.entry("442d85ed-8272-4543-a6f1-418f90ded05d", "CLONING_CARTOGRAPHY"),
            Map.entry("8b36268c-1829-483c-a0f1-993b7156a8f2", "EXTENDING_CARTOGRAPHY"),
            Map.entry("602234e4-cac1-4353-8bb7-b1ebff70024b", "LOCKING_CARTOGRAPHY"),
            Map.entry("98c84b38-1085-46bd-b1ce-dd38c159e6cc", "UPGRADING_CARTOGRAPHY"),
            Map.entry("d81aaeaf-e172-4440-9225-868df030d27b", "BANNER_ADD_PATTERN"),
            Map.entry("b5c5d105-75a2-4076-af2b-923ea2bf4bf0", "BANNER_DUPLICATE"),
            Map.entry("00000000-0000-0000-0000-000000000002", "BANNER_FIREWORKS"),
            Map.entry("d1ca6b84-338e-4f2f-9c6b-76cc8b4bd98d", "BOOK_CLONING"),
            Map.entry("85939755-ba10-4d9d-a4cc-efb7a8e943c4", "CLONING_CRAFTING"),
            Map.entry("685a742a-c42e-4a4e-88ea-5eb83fc98e5b", "DECORATED_POT"),
            Map.entry("d392b075-4ba1-40ae-8789-af868d56f6ce", "EXTENDING_CRAFTING"),
            Map.entry("00000000-0000-0000-0000-000000000001", "REPAIR_ITEM"),
            Map.entry("00000000-0000-0000-0000-0000000000c8", "SHIELD_BANNER"),
            Map.entry("aecd2294-4b94-434b-8667-4499bb2c9327", "UPGRADING_CRAFTING")
    );

    @SneakyThrows
    public static void main(String[] args) {
        var recipesJson = JsonParser.parseReader(Files.newBufferedReader(RECIPES_PATH)).getAsJsonObject();

        var uuidClass = ClassName.get(UUID.class);

        var codeBuilder = TypeSpec.interfaceBuilder("ComplexRecipeIds").addModifiers(Modifier.PUBLIC).addAnnotation(TypeNames.MINECRAFT_VERSION_SENSITIVE);

        List<FieldSpec> fields = new ArrayList<>();
        var seenUuids = new HashSet<String>();
        for (var jsonElement : recipesJson.getAsJsonArray("multi")) {
            var obj = jsonElement.getAsJsonObject();

            var uuid = obj.get("uuid").getAsString();
            var constName = NAMES_BY_UUID.get(uuid.toLowerCase(Locale.ROOT));
            if (constName == null) {
                throw new IllegalStateException("Unknown multi recipe uuid " + uuid + " (netId=" + obj.get("netId").getAsInt() + "): please name it and add it to ComplexRecipeIdsGen.NAMES_BY_UUID");
            }
            seenUuids.add(uuid.toLowerCase(Locale.ROOT));

            var field = FieldSpec
                    .builder(uuidClass, constName).addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer("$T.fromString($S)", uuidClass, uuid.toUpperCase(Locale.ROOT)).build();

            fields.add(field);
        }

        for (var known : NAMES_BY_UUID.keySet()) {
            if (!seenUuids.contains(known)) {
                System.err.println("Warning: previously known multi recipe " + NAMES_BY_UUID.get(known) + " (" + known + ") is missing from recipes.json");
            }
        }

        fields.sort(Comparator.comparing(FieldSpec::name));
        fields.forEach(codeBuilder::addField);

        var javaFile = JavaFile
                .builder("org.allaymc.server.item.recipe", codeBuilder.build()).indent(CodeGenConstants.INDENT).skipJavaLangImports(true).build();
        Files.deleteIfExists(OUTPUT_PATH);
        Files.createFile(OUTPUT_PATH);
        Utils.writeFileWithCRLF(OUTPUT_PATH, javaFile.toString());
    }
}
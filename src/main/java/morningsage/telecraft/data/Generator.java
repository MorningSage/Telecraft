package morningsage.telecraft.data;

import net.minecraft.data.DataGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public final class Generator {

    public static final Logger LOGGER = LogManager.getLogger();

    public static void generate() {
        //if (!"true".equals(System.getProperty("telecraft.generateData"))) {
        //    System.out.println("Telecraft: Skipping data generation. System property not set.");
        //    return;
        //}

        try {
            Path output = Paths.get("../src/generated/resources/morningsage/telecraft/tlobjects");
            if (Files.exists(output)) FileUtils.deleteDirectory(output.toFile());
            DataGenerator generator = new DataGenerator(output, Collections.emptyList());
            generator.install(new TLParser(output));
            generator.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.exit(0);
    }
}



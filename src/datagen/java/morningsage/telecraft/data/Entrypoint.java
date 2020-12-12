package morningsage.telecraft.data;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.Bootstrap;
import net.minecraft.data.DataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class Entrypoint implements PreLaunchEntrypoint {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void dump(int layer) throws Exception {
        Path output = Paths.get("../src/generated/resources");

        LOGGER.info("TL Parsing for Layer {}", layer);

        DataGenerator generator = new DataGenerator(output, Collections.emptyList());
        generator.install(new TLParser(output));
        generator.run();
    }

    @Override
    public void onPreLaunch() {
        if (!"true".equals(System.getProperty("telecraft.generateData"))) {
            System.out.println("Telecraft: Skipping data generation. System property not set.");
            return;
        }

        try {
            Path output = Paths.get("../src/generated/resources");
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

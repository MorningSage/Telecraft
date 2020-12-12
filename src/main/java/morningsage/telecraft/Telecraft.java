package morningsage.telecraft;

import lombok.var;
import morningsage.telecraft.data.Generator;
import net.fabricmc.api.ClientModInitializer;

import java.nio.file.Paths;

public class Telecraft implements ClientModInitializer {
	public static final String MOD_ID = "telecraft";

	@Override
	public void onInitializeClient() {
		Generator.generate();
	}

}

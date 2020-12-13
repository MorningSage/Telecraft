package morningsage.telecraft;

import morningsage.telecraft.data.Generator;
import morningsage.telecraft.tlobjects.TLObject;
import morningsage.telecraft.tlobjects.WebPage;
import morningsage.telecraft.utils.ByteUtils;
import net.fabricmc.api.ClientModInitializer;

public class Telecraft implements ClientModInitializer {
	public static final String MOD_ID = "telecraft";

	@Override
	public void onInitializeClient() {
		WebPage<?> asdf = TLObject.deserializeObject(WebPage.class, ByteUtils.forReading(new byte[] {(byte) 0xE8, (byte) 0x9C, 0x45, (byte) 0xB2}));

		Generator.generate();
	}

	//public static <TLOBJECT extends TLObject<TLOBJECT>> TLOBJECT asdf(Class<? super TLOBJECT> clazz) {
//
	//}
}

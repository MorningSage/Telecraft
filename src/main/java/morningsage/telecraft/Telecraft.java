package morningsage.telecraft;

import morningsage.telecraft.data.Generator;
import morningsage.telecraft.network.connection.events.TransportEventHub;
import morningsage.telecraft.network.connection.protocols.TransportProtocol;
import morningsage.telecraft.network.connection.transports.TCPImpl;
import morningsage.telecraft.network.connection.transports.TransportImpl;
import morningsage.telecraft.network.connection.transports.Transports;
import morningsage.telecraft.utils.ByteUtils;
import net.fabricmc.api.ClientModInitializer;

public class Telecraft implements ClientModInitializer {
	public static final String MOD_ID = "telecraft";

	@Override
	public void onInitializeClient() {
		TransportImpl asdf = Transports.TCP.create();

		TransportEventHub.EXCEPTION_EVENT.register(exception -> {
			System.out.println(exception.getMessage());
		});

		TransportEventHub.DATA_RECEIVED_EVENT.register(payload -> {
			System.out.println("got payload");
		});

		//asdf.connect("149.154.175.55", 443, false, 1);


		//WebPage<?> asdf = TLObject.deserializeObject(WebPage.class, ByteUtils.forReading(new byte[] {(byte) 0xE8, (byte) 0x9C, 0x45, (byte) 0xB2}));

		Generator.generate();
	}

	//public static <TLOBJECT extends TLObject<TLOBJECT>> TLOBJECT asdf(Class<? super TLOBJECT> clazz) {
//
	//}
}

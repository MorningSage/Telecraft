package morningsage.telecraft.network.connection.protocols;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.function.Consumer;
import java.util.logging.Logger;

public interface ProtocolImpl {
    byte[] wrapPayload(byte[] payload);
    byte[] unwrapPayload(byte[] payload);

    default void logToConsole(Consumer<Logger> callback) {
        callback.accept(Logger.getLogger(this.getClass().getName()));
    }
}

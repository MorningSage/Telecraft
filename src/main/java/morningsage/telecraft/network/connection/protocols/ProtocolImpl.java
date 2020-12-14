package morningsage.telecraft.network.connection.protocols;

import java.io.InputStream;
import java.util.function.Consumer;
import java.util.logging.Logger;

public interface ProtocolImpl {
    default byte[] getIdentifier() {
        return new byte[0];
    }

    byte[] wrapPayload(byte[] payload);
    byte[] unwrapPayload(byte[] payload);
    byte[] unwrapStream(InputStream stream);
    void reset();

    default void logToConsole(Consumer<Logger> callback) {
        callback.accept(Logger.getLogger(this.getClass().getName()));
    }
}

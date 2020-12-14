package morningsage.telecraft.network.connection.transports;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import morningsage.telecraft.network.connection.protocols.ProtocolImpl;

import java.util.function.Supplier;

@RequiredArgsConstructor
public enum Transports {
    TCP("TCP", TCPImpl::new),
    WEBSOCKET("Websocket", null),
    WEBSOCKET_OVER_HTTPS("Websocket Over HTTPS", null),
    HTTP("HTTP", null),
    HTTPS("HTTPS", null);

    @Getter private final String name;
    @Getter private final Supplier<TransportImpl> implementationSupplier;

    public TransportImpl create() {
        return implementationSupplier.get();
    }
}

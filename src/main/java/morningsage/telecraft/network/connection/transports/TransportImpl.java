package morningsage.telecraft.network.connection.transports;

import morningsage.telecraft.exceptions.InvalidOperationException;
import morningsage.telecraft.exceptions.InvalidProtocolException;
import morningsage.telecraft.network.connection.events.TransportEventHub;
import morningsage.telecraft.network.connection.protocols.TransportProtocol;

public interface TransportImpl {
    void setProtocol(TransportProtocol protocol) throws InvalidProtocolException, InvalidOperationException;
    void connect(String ipAddress, int port, boolean reconnect, int retryCount);
    void disconnect();
    void send(byte[] payload) throws InvalidOperationException;
}

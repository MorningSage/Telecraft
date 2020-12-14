package morningsage.telecraft.network.connection.transports;

import lombok.Getter;
import morningsage.telecraft.exceptions.InvalidOperationException;
import morningsage.telecraft.exceptions.InvalidProtocolException;
import morningsage.telecraft.network.connection.events.TransportEventHub;
import morningsage.telecraft.network.connection.protocols.ProtocolImpl;
import morningsage.telecraft.network.connection.protocols.TransportProtocol;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class TCPImpl implements TransportImpl {
    @Getter private ProtocolImpl protocol = TransportProtocol.FULL.create();
    private Socket socket = new Socket();

    private final AtomicBoolean shouldDisconnect = new AtomicBoolean();

    @Override
    public void setProtocol(TransportProtocol protocol) throws InvalidProtocolException, InvalidOperationException {
        if (protocol == null) throw new InvalidProtocolException("Cannot use a null protocol");
        if (isConnected()) throw new InvalidOperationException("Cannot change protocols while connected");

        this.protocol = protocol.create();
    }

    @Override
    public void connect(String ipAddress, int port, boolean reconnect, int retryCount) {
        try {
            socket.connect(new InetSocketAddress(ipAddress, port));

            final Thread incoming = new Thread(() -> {
                while (!shouldDisconnect.get()) {
                    byte[] payload;

                    try {
                        payload = protocol.unwrapStream(socket.getInputStream());
                    } catch (IOException exception) {
                        TransportEventHub.EXCEPTION_EVENT.invoker().onException(exception);
                        break;
                    }

                    if (payload.length > 0) {
                        TransportEventHub.DATA_RECEIVED_EVENT.invoker().onDataReceived(payload);
                    }
                }

                try {
                    socket.close();
                } catch (IOException exception) {
                    TransportEventHub.EXCEPTION_EVENT.invoker().onException(exception);
                }
            });

            incoming.start();
        } catch (Exception e) {
            TransportEventHub.EXCEPTION_EVENT.invoker().onException(e);
        }
    }

    @Override
    public void send(byte[] payload) throws InvalidOperationException {
        if (!isConnected()) throw new InvalidOperationException("Not connected");

        payload = protocol.wrapPayload(payload);

        try {
            OutputStream stream = socket.getOutputStream();

            stream.write(payload);
            stream.flush();
        } catch (IOException exception) {
            TransportEventHub.EXCEPTION_EVENT.invoker().onException(exception);
        }
    }

    @Override
    public void disconnect() {
        synchronized (shouldDisconnect) {
            shouldDisconnect.set(true);
        }
    }

    public boolean isConnected() {
        synchronized (shouldDisconnect) {
            return shouldDisconnect.get() && socket.isConnected();
        }
    }
}

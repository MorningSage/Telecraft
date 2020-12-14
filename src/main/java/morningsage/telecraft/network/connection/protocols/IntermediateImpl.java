package morningsage.telecraft.network.connection.protocols;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.InputStream;

public final class IntermediateImpl implements ProtocolImpl {
    @Override
    public byte[] wrapPayload(byte[] payload) {
        throw new NotImplementedException();
    }

    @Override
    public byte[] unwrapPayload(byte[] payload) {
        throw new NotImplementedException();
    }

    @Override
    public byte[] unwrapStream(InputStream stream) {
        throw new NotImplementedException();
    }

    @Override
    public void reset() {

    }
}

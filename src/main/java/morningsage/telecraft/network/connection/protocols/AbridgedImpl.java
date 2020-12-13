package morningsage.telecraft.network.connection.protocols;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public final class AbridgedImpl implements ProtocolImpl {
    @Override
    public byte[] wrapPayload(byte[] payload) {
        throw new NotImplementedException();
    }

    @Override
    public byte[] unwrapPayload(byte[] payload) {
        throw new NotImplementedException();
    }
}

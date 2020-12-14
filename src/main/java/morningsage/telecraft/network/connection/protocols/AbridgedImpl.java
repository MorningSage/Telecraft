package morningsage.telecraft.network.connection.protocols;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.InputStream;

public final class AbridgedImpl implements ProtocolImpl {

    @Override
    public byte[] getIdentifier() {
        return new byte[] { (byte) 0xEF };
    }

    /*
     *
     * +-+----...----+
     * |l|  payload  |
     * +-+----...----+
     *
     * OR
     *
     * +-+---+----...----+
     * |h|len|  payload  +
     * +-+---+----...----+
     */

    @Override
    public byte[] wrapPayload(byte[] payload) {
        //ByteArrayBuffer byteArrayBuffer = new ByteArrayBuffer(payload.length + 4);
        //DataOutputStream stream = new DataOutputStream(byteArrayBuffer);
//
        //try {
        //    stream.writeInt(Math.min((payload.length + 1) / 4, 0x7F));
//
        //    if (byteArrayBuffer.getRawData()[0] == 0x7F) {
        //        stream.write();
        //    }
//
        //} catch (Exception exception) {
//
        //}

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

package morningsage.telecraft.network.connection.protocols;

import com.sun.xml.internal.ws.util.ByteArrayBuffer;
import lombok.Getter;
import morningsage.telecraft.utils.CrcUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public final class FullImpl implements ProtocolImpl {
    @Getter private int sequenceNumber = 0;

    /*
     *    int          int           byte[]    int
     * +--------+----------------+----...----+-----+
     * | length | sequenceNumber |  payload  | crc |
     * +--------+----------------+----...----+-----+
     */

    @Override
    public byte[] wrapPayload(byte[] payload) {
        ByteArrayBuffer byteArrayBuffer = new ByteArrayBuffer(payload.length + 12);
        DataOutputStream stream = new DataOutputStream(byteArrayBuffer);

        try {
            // payload Length + overhead
            stream.write(payload.length + 12);
            stream.write(sequenceNumber);
            stream.write(payload);
            // CRC should include length, sequence number, and payload
            stream.write(CrcUtils.computeHash(byteArrayBuffer.getRawData(), 0, payload.length + 8));

            // Always increment sequence number
            sequenceNumber = sequenceNumber + 1;
        } catch (Exception exception) {
            logToConsole(logger -> {
                logger.severe("Failed to wrap payload");
                logger.severe(exception.getMessage());
            });
        }

        return byteArrayBuffer.getRawData();
    }

    @Override
    public byte[] unwrapPayload(byte[] payload) {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(payload));
        ByteArrayBuffer byteArrayBuffer;

        try {
            if (payload.length < 12) throw new Exception("Payload has invalid size");

            int length = stream.readInt();
            if (length < 12) throw new Exception("Invalid length in payload");

            int sequenceNumber = stream.readInt();
            if (sequenceNumber != this.sequenceNumber) throw new Exception("Payload contains invalid sequence number");

            byteArrayBuffer = new ByteArrayBuffer(length);
            if (stream.read(byteArrayBuffer.getRawData(), 0, byteArrayBuffer.size()) != byteArrayBuffer.size()) {
                throw new Exception("Unable to read " + byteArrayBuffer.size() + "bytes from the payload");
            }

            int crc = stream.readInt();
            if (CrcUtils.computeHash(payload, 0, payload.length - 4) != crc) {
                throw new Exception("Payload hash mismatch");
            }

            return byteArrayBuffer.getRawData();
        } catch (Exception exception) {
            logToConsole(logger -> {
                logger.severe("Failed to unwrap payload");
                logger.severe(exception.getMessage());
            });
        }

        return new byte[0];
    }
}

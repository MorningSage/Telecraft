package morningsage.telecraft.network.connection.protocols;

import lombok.Getter;
import morningsage.telecraft.utils.CrcUtils;

import java.io.*;

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
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(payload.length + 12);
        DataOutputStream stream = new DataOutputStream(byteStream);

        try {
            // payload Length + overhead
            stream.write(payload.length + 12);
            stream.write(sequenceNumber);
            stream.write(payload);
            // CRC should include length, sequence number, and payload
            stream.write(CrcUtils.computeHash(byteStream.toByteArray(), 0, payload.length + 8));

            // Always increment sequence number
            sequenceNumber = sequenceNumber + 1;
        } catch (Exception exception) {
            logToConsole(logger -> {
                logger.severe("Failed to wrap payload");
                logger.severe(exception.getMessage());
            });
        }

        return byteStream.toByteArray();
    }

    @Override
    public byte[] unwrapPayload(byte[] payload) {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(payload));
        byte[] internalPayload;

        try {
            if (payload.length < 12) throw new Exception("Payload has invalid size");

            int length = stream.readInt();
            if (length < 12) throw new Exception("Invalid length in payload");

            int sequenceNumber = stream.readInt();
            if (sequenceNumber != this.sequenceNumber) throw new Exception("Payload contains invalid sequence number");

            internalPayload = new byte[length];
            if (stream.read(internalPayload, 0, length) != length) {
                throw new Exception("Unable to read " + length + "bytes from the payload");
            }

            int crc = stream.readInt();
            if (CrcUtils.computeHash(payload, 0, payload.length - 4) != crc) {
                throw new Exception("Payload hash mismatch");
            }

            return internalPayload;
        } catch (Exception exception) {
            logToConsole(logger -> {
                logger.severe("Failed to unwrap payload");
                logger.severe(exception.getMessage());
            });
        }

        return new byte[0];
    }

    @Override
    public byte[] unwrapStream(InputStream stream) {
        DataInputStream dataStream = new DataInputStream(stream);
        byte[] payload;

        try {
            int length = dataStream.readInt();
            if (length < 12) throw new Exception("Invalid length in payload");

            int sequenceNumber = dataStream.readInt();
            if (sequenceNumber != this.sequenceNumber) throw new Exception("Payload contains invalid sequence number");

            payload = new byte[length];
            if (stream.read(payload, 0, payload.length) != length) {
                throw new Exception("Unable to read " + length + "bytes from the payload");
            }

            //int crc = dataStream.readInt();
            //if (CrcUtils.computeHash(payload, 0, length + 8) != crc) {
            //    throw new Exception("Payload hash mismatch");
            //}

            return payload;
        } catch (Exception exception) {
            logToConsole(logger -> {
                logger.severe("Failed to unwrap payload");
                logger.severe(exception.getMessage());
            });
        }

        return new byte[0];
    }

    @Override
    public void reset() {
        sequenceNumber = 0;
    }
}

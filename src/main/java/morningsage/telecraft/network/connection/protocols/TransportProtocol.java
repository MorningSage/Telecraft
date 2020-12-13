package morningsage.telecraft.network.connection.protocols;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor @ToString
public enum TransportProtocol implements ProtocolImpl {
    /**
     * The lightest protocol available.
     *
     * - Overhead: Very small
     * - Minimum envelope length: 1 byte
     * - Maximum envelope length: 4 bytes
     *
     * For more information, see:
     * https://core.telegram.org/mtproto/mtproto-transports#abridged
     */
    ABRIDGED("Abridged", new AbridgedImpl()),
    /**
     * In case 4-byte data alignment is needed, an intermediate version of the original protocol may be used.
     *
     * - Overhead: small
     * - Minimum envelope length: 4 bytes
     * - Maximum envelope length: 4 bytes
     *
     * For more information, see:
     * https://core.telegram.org/mtproto/mtproto-transports#intermediate
     */
    INTERMEDIATE("Intermediate", new IntermediateImpl()),
    /**
     * Padded version of the intermediate protocol, to use with obfuscation enabled to bypass ISP blocks.
     *
     * - Overhead: small-medium
     * - Minimum envelope length: random
     * - Maximum envelope length: random
     *
     * For more information, see:
     * https://core.telegram.org/mtproto/mtproto-transports#padded-intermediate
     */
    PADDED_INTERMEDIATE("Padded Intermediate", new PaddedIntermediateImpl()),
    /**
     * The basic MTProto transport protocol
     *
     * - Overhead: medium
     * - Minimum envelope length: 12 bytes (length+seqno+crc)
     * - Maximum envelope length: 12 bytes (length+seqno+crc)
     *
     * For more information, see:
     * https://core.telegram.org/mtproto/mtproto-transports#full
     */
    FULL("Full", new FullImpl());

    @Getter private final String name;
    @Getter private final ProtocolImpl implementation;

    @Override
    public byte[] wrapPayload(byte[] payload) {
        return implementation.wrapPayload(payload);
    }

    @Override
    public byte[] unwrapPayload(byte[] payload) {
        return implementation.unwrapPayload(payload);
    }
}

package morningsage.telecraft.network.connection.protocols;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Supplier;

@RequiredArgsConstructor @ToString
public enum TransportProtocol {
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
    ABRIDGED("Abridged", AbridgedImpl::new),
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
    INTERMEDIATE("Intermediate", IntermediateImpl::new),
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
    PADDED_INTERMEDIATE("Padded Intermediate", PaddedIntermediateImpl::new),
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
    FULL("Full", FullImpl::new);

    @Getter private final String name;
    @Getter private final Supplier<ProtocolImpl> implementationSupplier;

    public ProtocolImpl create() {
        return implementationSupplier.get();
    }
}

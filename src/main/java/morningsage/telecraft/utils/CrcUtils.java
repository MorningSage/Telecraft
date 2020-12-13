package morningsage.telecraft.utils;

import java.util.zip.CRC32;

public final class CrcUtils {
    private static final CRC32 crcInstance = new CRC32();
    public static int computeHash(byte[] input, int index, int length) {
        crcInstance.reset();
        crcInstance.update(input, index, length);
        return (int) (crcInstance.getValue() & 0x7FFFFFFF);
    }
}

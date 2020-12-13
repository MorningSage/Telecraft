package morningsage.telecraft.utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public final class ByteUtils {
    public static DataInputStream forReading(byte[] bytes) {
        return new DataInputStream(new ByteArrayInputStream(bytes));
    }
}

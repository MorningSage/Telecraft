package morningsage.telecraft.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public final class ByteReader {
    public static DataInputStream forReading(byte[] bytes) {
        return new DataInputStream(new ByteArrayInputStream(bytes));
    }
}

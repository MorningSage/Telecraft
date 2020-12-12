package morningsage.telecraft.data.utils;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import morningsage.telecraft.data.Generator;
import net.minecraft.data.DataCache;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public final class FileUtils {
    private static final HashFunction SHA1 = Hashing.sha1();

    public static void writeToPath(String fileContents, DataCache cache, Path path) {
        String newHash = SHA1.hashUnencodedChars(fileContents).toString();

        if (!Objects.equals(cache.getOldSha1(path), newHash) || !Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
                BufferedWriter bufferedWriter = Files.newBufferedWriter(path);
                bufferedWriter.write(fileContents);
                bufferedWriter.close();
            } catch (IOException exception) {
                Generator.LOGGER.error(exception);
            }
        }

        cache.updateSha1(path, newHash);
    }
}

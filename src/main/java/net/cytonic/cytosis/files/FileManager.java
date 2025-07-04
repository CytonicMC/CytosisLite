package net.cytonic.cytosis.files;

import lombok.NoArgsConstructor;
import net.cytonic.cytosis.logging.Logger;

import java.io.*;
import java.nio.file.Path;

/**
 * A class handling IO and files
 */
@NoArgsConstructor
public class FileManager {

    /**
     * Extracts a resource file from the classpath and writes it to the specified path.
     *
     * @param resource The name of the resource file to extract.
     * @param path     The path where the extracted file will be written.
     * @return A CompletableFuture representing the completion of the file extraction process.
     */
    public File extractResource(String resource, Path path) {
        try {
            InputStream stream = FileManager.class.getClassLoader().getResourceAsStream(resource);
            if (stream == null) {
                throw new IllegalStateException("The resource \"" + resource + "\" does not exist!");
            }
            OutputStream outputStream = new FileOutputStream(path.toFile());
            byte[] buffer = new byte[1024];
            int length;
            while ((length = stream.read(buffer)) > 0) outputStream.write(buffer, 0, length);
            outputStream.close();
            stream.close();
        } catch (IOException e) {
            Logger.error("An error occurred whilst extracting the resource \"" + resource + "\"!", e);
        }
        return path.toFile();
    }
}
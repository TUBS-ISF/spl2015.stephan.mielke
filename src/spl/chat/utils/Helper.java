package spl.chat.utils;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Helper {

    /**
     * Gibt die .properties zurück
     *
     * @param path der Pfad
     * @return die config.properties
     * @throws IOException
     */
    public static Properties getConfig(Path path) throws IOException {
        Properties properties = new Properties();

        InputStream inputStream = Files.newInputStream(path);
        properties.load(inputStream);
        inputStream.close();

        return properties;
    }
}

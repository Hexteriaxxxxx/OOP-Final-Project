package utils;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = ConfigLoader.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new RuntimeException("❌ config.properties not found!");
            }
            props.load(input);
            System.out.println("✅ Config loaded successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}
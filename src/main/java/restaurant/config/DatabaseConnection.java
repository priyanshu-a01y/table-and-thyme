package restaurant.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final String PROPERTIES_FILE = "db.properties";
    private static DatabaseConnection instance;
    private Properties properties;

    private DatabaseConnection() {
        properties = new Properties();
        String configuredPath = System.getProperty("restaurant.db.config");
        Path configPath = configuredPath == null || configuredPath.isBlank()
                ? resolveConfigPath() : Path.of(configuredPath).toAbsolutePath().normalize();
        try (InputStream file = Files.newInputStream(configPath)) {
            properties.load(file);
            System.out.println("[DB] Loaded configuration file: " + configPath);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load database configuration from " + configPath, e);
        }
    }

    private Path resolveConfigPath() {
        try {
            Path codeLocation = Path.of(DatabaseConnection.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).toAbsolutePath().normalize();
            Path applicationDirectory = Files.isDirectory(codeLocation) ? codeLocation.getParent() : codeLocation.getParent();
            if (applicationDirectory != null && applicationDirectory.getFileName() != null
                    && "target".equals(applicationDirectory.getFileName().toString())) {
                applicationDirectory = applicationDirectory.getParent();
            }
            Path besideApplication = applicationDirectory == null ? null : applicationDirectory.resolve(PROPERTIES_FILE);
            if (besideApplication != null && Files.isRegularFile(besideApplication)) {
                return besideApplication;
            }
        } catch (URISyntaxException | SecurityException e) {
            System.err.println("[DB] Could not resolve application location; trying working directory.");
        }
        return Path.of(PROPERTIES_FILE).toAbsolutePath().normalize();
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public static Connection getConnection() throws SQLException {
        DatabaseConnection dbConn = getInstance();
        
        String url = dbConn.properties.getProperty("db.url");
        String username = dbConn.properties.getProperty("db.username");
        String password = System.getenv().getOrDefault("RESTAURANT_DB_PASSWORD",
                dbConn.properties.getProperty("db.password"));
        
        if (url == null || username == null || password == null) {
            throw new SQLException("Database connection properties are missing or incomplete.");
        }

        System.out.println("[DB] Configured JDBC URL: " + url);
        System.out.println("[DB] Configured database user: " + username + " (password omitted)");
        Connection connection = DriverManager.getConnection(url, username, password);
        System.out.println("[DB] Connected server URL: " + connection.getMetaData().getURL());
        System.out.println("[DB] Connected catalog/database: " + connection.getCatalog());
        return connection;
    }
}

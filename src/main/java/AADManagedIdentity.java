import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class AADManagedIdentity {

    private static final Logger logger = LogManager.getLogger(AADManagedIdentity.class);

    private static String serverName;
    private static String databaseName;
    private static String userClientId;

    public static void main(String[] args) throws Exception {

        logger.info("Azure Active Directory Managed Identity");

        readProperties(args);

        javax.sql.DataSource datasource = createDataSource(serverName, databaseName, userClientId);

        System.out.println("Connecting to '" + databaseName + "' @ '" + serverName +
                "' using user managed identity '" + userClientId + "'...");
        try (Connection connection = datasource.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUSER_SNAME()")) {
            if (rs.next()) {
                System.out.println("\n\nYou have successfully logged on as: '" + rs.getString(1) + "'\n\n");
            }
        }
    }

    private static javax.sql.DataSource createDataSource(String serverName,
                                                                           String databaseName,
                                                                           String userClientId) {

        org.apache.tomcat.jdbc.pool.DataSource datasource = new org.apache.tomcat.jdbc.pool.DataSource();
        datasource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		logger.info("Driver Class Name: 'com.microsoft.sqlserver.jdbc.SQLServerDriver'.");

        Properties properties = new Properties();
        properties.put("authentication", "ActiveDirectoryManagedIdentity");
        properties.put("user", userClientId);

        datasource.setDbProperties(properties);
        String baseUrl = "jdbc:sqlserver://" + serverName + ";database=" + databaseName;
        datasource.setUrl(baseUrl);

        return datasource;
    }

    private static void readProperties(String[] args) throws IOException {
        Properties properties = new Properties();
        InputStream in;
        if (args.length == 1) {
            String applicationPropertiesFilename = args[0];
            File file = new File(applicationPropertiesFilename);
            if (!file.exists() || file.isDirectory()) {
                String errorMessage = "Failed to read '" + applicationPropertiesFilename + "' properties file.";
                System.err.println(errorMessage);
                throw new IOException(errorMessage);
            }
            System.out.println("Using '" + applicationPropertiesFilename + "' properties file...");
            in = Files.newInputStream(Paths.get(applicationPropertiesFilename));
        } else {
            in = AADManagedIdentity.class.getResourceAsStream("application.properties");
        }
        if (in != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            properties.load(reader);
        } else {
            String errorMessage = "Failed to read '" + "application.properties" + "' properties file.";
            System.err.println(errorMessage);
            throw new IOException(errorMessage);
        }
        serverName = properties.getProperty("SERVER_NAME");
        databaseName = properties.getProperty("DATABASE_NAME");
        userClientId = properties.getProperty("CLIENT_ID");
    }
}
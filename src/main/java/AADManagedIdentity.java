import com.microsoft.sqlserver.jdbc.SQLServerXADataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.io.*;
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

        org.apache.tomcat.jdbc.pool.DataSource datasource = createDataSource(serverName, databaseName, userClientId);

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

    private static org.apache.tomcat.jdbc.pool.DataSource createDataSource(String serverName,
                                                                           String databaseName,
                                                                           String userClientId) {

        SQLServerXADataSource ds = new SQLServerXADataSource();
        ds.setServerName(serverName);
        ds.setDatabaseName(databaseName);
        ds.setAuthentication("ActiveDirectoryManagedIdentity");
        if (userClientId != null && !userClientId.isEmpty()) {
            ds.setUser(userClientId);
        } else {
            System.out.println("Using the default user managed identity of the current Azure Virtual Machine...");
        }

        PoolConfiguration p = new PoolProperties();
        p.setDataSource(ds);

        org.apache.tomcat.jdbc.pool.DataSource datasource = new org.apache.tomcat.jdbc.pool.DataSource();
        datasource.setPoolProperties(p);
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
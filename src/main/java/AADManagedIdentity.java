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

    private static final String AUTHENTICATION="AUTHENTICATION";
    private static String SERVER_NAME = "SERVER_NAME";
    private static String DATABASE_NAME = "DATABASE_NAME";
    private static String CLIENT_ID = "CLIENT_ID";
    private static String USER = "USER";
    private static String PASSWORD = "PASSWORD";
	
	private static final String ACTIVE_DIRECTORY_MANAGED_IDENTITY = "ActiveDirectoryManagedIdentity";
	private static final String SQL_PASSWORD = "SqlPassword";

    private static String authentication;
    private static String serverName;
    private static String databaseName;
    private static String clientId;
    private static String user;
    private static String password;

    public static void main(String[] args) throws Exception {

        logger.info("Azure Active Directory Managed Identity");

        readProperties(args);

        javax.sql.DataSource datasource = createDataSource(authentication, serverName, databaseName,
            clientId, user, password);

        System.out.println("Connecting to '" + databaseName + "' @ '" + serverName +
            "' using user managed identity '" + clientId + "'...");
        try (Connection connection = datasource.getConnection(); 
				Statement stmt = connection.createStatement(); 
				ResultSet rs = stmt.executeQuery("SELECT SUSER_SNAME()")) {
            if (rs.next()) {
                System.out.println("\n\nYou have successfully logged on as: '" + rs.getString(1) + "'\n\n");
            }
        }
    }

    private static javax.sql.DataSource createDataSource(String authentication,
        String serverName,
        String databaseName,
        String clientId,
        String user,
        String password) throws IOException {

        boolean encrypt = false;
        boolean trustServerCertificate = true;
        int loginTimeout = 120;
        org.apache.tomcat.jdbc.pool.DataSource datasource = new org.apache.tomcat.jdbc.pool.DataSource();
        datasource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        logger.info("Driver Class Name: 'com.microsoft.sqlserver.jdbc.SQLServerDriver'.");

        Properties properties = new Properties();
        String baseUrl = "jdbc:sqlserver://" + serverName + ";database=" + databaseName;
        baseUrl += ";encrypt=" + encrypt + ";trustServerCertificate=" + trustServerCertificate + 
					";hostNameInCertificate=*.database.windows.net;loginTimeout=" + loginTimeout + ";";

        if (authentication.equals(SQL_PASSWORD)) {
            properties.put(AUTHENTICATION, authentication);
            properties.put(USER, user);
            properties.put(PASSWORD, password);
        } else {
            if (authentication.equals(ACTIVE_DIRECTORY_MANAGED_IDENTITY)) {
                properties.put(AUTHENTICATION, authentication);
                properties.put(USER, clientId);
            } else {
                String errorMessage = "Invalid AUTHENTICATION '" + authentication + "' in properties file.";
                System.err.println(errorMessage);
                throw new IOException(errorMessage);
            }
        }

        logger.info("Url: '" + baseUrl + "'");
        datasource.setDbProperties(properties);
        datasource.setUrl(baseUrl);

        return datasource;
    }

    private static void readProperties(String[] args) throws IOException {
        Properties properties = new Properties();
        InputStream in ;
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
        if ( in != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader( in ));
            properties.load(reader);
        } else {
            String errorMessage = "Failed to read '" + "application.properties" + "' properties file.";
            System.err.println(errorMessage);
            throw new IOException(errorMessage);
        }
		
        authentication = properties.getProperty(AUTHENTICATION);
        serverName = properties.getProperty(SERVER_NAME);
        databaseName = properties.getProperty(DATABASE_NAME);
        clientId = properties.getProperty(CLIENT_ID);
        user = properties.getProperty(USER);
        password = properties.getProperty(PASSWORD);
		
		if ( (authentication == null) || (user == null) ) {
            String errorMessage = "Failed to read '" + AUTHENTICATION + "' or '" + USER + "' from properties file.";
            System.err.println(errorMessage);
            throw new IOException(errorMessage);
		}
    }
}

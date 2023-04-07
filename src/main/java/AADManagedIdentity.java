import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class AADManagedIdentity {
	
	
	private static String serverName;
	private static String databaseName;
	private static String userClientId;
	
    public static void main(String[] args) throws Exception {

        readProperties();
		
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setServerName(serverName);
        ds.setDatabaseName(databaseName);
        ds.setAuthentication("ActiveDirectoryManagedIdentity");
        ds.setUser(userClientId);

        System.out.println("Connecting to '" + databaseName + "' @ '" + serverName + "' using user managed identity '" + userClientId + "'...");
        try (Connection connection = ds.getConnection();
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT SUSER_SNAME()")) {
            if (rs.next()) {
                System.out.println("You have successfully logged on as: " + rs.getString(1));
            }
        }
    }
	
	private static void readProperties() throws IOException {
        Properties properties = new Properties();
		InputStream in = AADManagedIdentity.class.getResourceAsStream("application.properties");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        properties.load(reader);
        serverName = properties.getProperty("SERVER_NAME");
		databaseName = properties.getProperty("DATABASE_NAME");
        userClientId = properties.getProperty("CLIENT_ID");
    }
}
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerXADataSource;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AADManagedIdentity {

  private static final Logger logger = LogManager.getLogger(AADManagedIdentity.class);

  private static String serverName;
  private static String databaseName;
  private static String userClientId;

  public static void main(String[] args) throws Exception {

    logger.info("Azure Active Directory Managed Identity");

    readProperties(args);

    org.apache.tomcat.jdbc.pool.DataSource datasource = createDataSource(serverName, databaseName, userClientId);

    System.out.println("Connecting to '" + databaseName + "' @ '" + serverName + "' using user managed identity '" + userClientId + "'...");
    try (Connection connection = datasource.getConnection(); Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT SUSER_SNAME()")) {
      if (rs.next()) {
        System.out.println("\n\nYou have successfully logged on as: '" + rs.getString(1) + "'\n\n");
      }
    }
  }

  private static org.apache.tomcat.jdbc.pool.DataSource createDataSource(String serverName, String databaseName, String userClientId) {

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
      in = new FileInputStream(applicationPropertiesFilename);
    } else {
      in = AADManagedIdentity.class.getResourceAsStream("application.properties");
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    properties.load(reader);
    serverName = properties.getProperty("SERVER_NAME");
    databaseName = properties.getProperty("DATABASE_NAME");
    userClientId = properties.getProperty("CLIENT_ID");
  }
}
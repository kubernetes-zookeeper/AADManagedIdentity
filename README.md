# Azure Active Directory Managed User Identity Authentication

## Overview

This sample shows how to use the Azure [managed user identity](https://learn.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/overview) to sign from an Azure resource (Azure Windows virtual machine) to a remote Azure service (Azure SQL database) over a secured channel without using any secrets, credentials, certificates or keys.
<br>
<br>
A common challenge for developers is the management of secrets, credentials, certificates, and keys used to secure communication between services. 
<br>
Managed identities eliminate the need for developers to manage these credentials.
<br>
Managed identities provide an automatically managed identity in Azure Active Directory (Azure AD) for applications to use when connecting to services that support Azure AD authentication.
<br>
<br>
Applications can use managed identities to automatically obtain Azure AD tokens without having to manage any credentials.
<br>
<br>
When using managed identities, you don't need to manage credentials. Credentials aren’t even accessible to you.
<br>
## How to run this sample

To run this sample, you'll need:

- An Azure Active Directory (Azure AD) tenant. For more information on how to get an Azure AD tenant, see [How to get an Azure AD tenant](https://azure.microsoft.com/documentation/articles/active-directory-howto-tenant/).
- One or more user accounts in your Azure AD tenant.
- [Azure Windows virtual machine](https://learn.microsoft.com/en-us/azure/virtual-machines/windows/quick-create-portal#create-virtual-machine).
- Optionally, working installation of [Java 11](https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.18%2B10/OpenJDK11U-jdk_x64_windows_hotspot_11.0.18_10.msi) or greater and [Maven](https://maven.apache.org/).

In the following sample, you will create the following:
- [Azure User Managed Identity](https://learn.microsoft.com/en-us/azure/azure-sql/database/authentication-azure-ad-user-assigned-managed-identity?view=azuresql#creating-a-user-assigned-managed-identity).
- [Azure SQL database](https://learn.microsoft.com/en-us/azure/azure-sql/database/authentication-azure-ad-user-assigned-managed-identity-create-server?view=azuresql&tabs=azure-portal).
### Step 1: Download or clone this repository

From a [Windows PowerShell](http://%SystemRoot%\system32\WindowsPowerShell\v1.0\powershell.exe):


1. Create the `AADManagedIdentity` folder:

```Shell
mkdir AADManagedIdentity
cd AADManagedIdentity
```

2. Download and extract the repository [zip](https://github.com/kubernetes-zookeeper/AADManagedIdentity/archive/refs/heads/main.zip) file:
```Shell
Invoke-WebRequest https://github.com/kubernetes-zookeeper/AADManagedIdentity/archive/refs/heads/main.zip -UseBasicParsing -OutFile AADManagedIdentity.zip
Expand-Archive .\AADManagedIdentity.zip
```
Alternatively, Clone this repository:
```Shell
git clone https://github.com/kubernetes-zookeeper/AADManagedIdentity.git
```

3. Expand the following java `modules.zip`:

```Shell
Expand-Archive .\AADManagedIdentity\AADManagedIdentity-main\java\lib\modules.zip -DestinationPath .\AADManagedIdentity\AADManagedIdentity-main\java\lib\
```

### Step 2: Create the Managed User Identity and Azure SQL database

#### Choose the Azure AD tenant where you want to create your applications

As a first step you'll need to:

1. Sign in to the [Azure portal](https://portal.azure.com) using either a work or school account or a personal Microsoft account.
2. If your account is present in more than one Azure AD tenant, select your profile at the top right corner in the menu on top of the page, and then **switch directory**.
   Change your portal session to the desired Azure AD tenant.
   
#### Create an Azure User Managed Identity and an Azure SQL Database

3. In the portal menu, select the **Managed Identities** service, and then select **Create**.
4. [Create User Assigned Managed Identity](https://learn.microsoft.com/en-us/azure/azure-sql/database/authentication-azure-ad-user-assigned-managed-identity?view=azuresql)
5. To allow the User Managed Identity to read from Microsoft Active Directory Graph as the server identity, you need to grant the following [permissions](https://learn.microsoft.com/en-us/azure/azure-sql/database/authentication-azure-ad-user-assigned-managed-identity?view=azuresql#permissions), or give the UMI the [Directory Readers](https://learn.microsoft.com/en-us/azure/azure-sql/database/authentication-aad-directory-readers-role-tutorial?view=azuresql) role.<br>These permissions should be granted <b>before</b> you provision a logical Azure SQL server or managed Azure SQL instance.<br>
<li><b>User.Read.All</b>: Allows access to Azure AD user information.
<li><b>GroupMember.Read.All</b>: Allows access to Azure AD group information.
<li><b>Application.Read.ALL</b>: Allows access to Azure AD service principal (application) information.


6. [Configure managed identities for Azure resources on a VM using the Azure portal](https://learn.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/qs-configure-portal-windows-vm#user-assigned-managed-identity)
7. To assign a user-assigned identity to a VM, your account needs the `Virtual Machine Contributor` and `Managed Identity Operator` role assignments.
8. [Create an Azure SQL Database server with a user-assigned managed identity](https://learn.microsoft.com/en-us/azure/azure-sql/database/authentication-azure-ad-user-assigned-managed-identity-create-server?view=azuresql&tabs=azure-portal)

### Step 3: Configure the client sample application to use your managed user identity

>In the steps below, "CLIENT_ID" is the same as "Client ID", "Client id", "Application ID" or "AppId".

[application.properties](https://github.com/kubernetes-zookeeper/AADManagedIdentity/blob/main/src/main/resources/application.properties)
```
SERVER_NAME=tdmmsi.database.windows.net
DATABASE_NAME=tdmmsi
CLIENT_ID=f330da90-xxxx-xxxx-xxxx-b5aa3508fa25
```

1. Open the [src\main\resources\application.properties](https://github.com/kubernetes-zookeeper/AADManagedIdentity/blob/main/src/main/resources/application.properties) configuration file
2. Find the variable `SERVER_NAME` and replace the existing value with the name of the azure sql server that you created in earlier steps.
3. Find the variable `DATABASE_NAME` and replace the existing value with the name of the azure sql database that you recorded in earlier steps.
4. Find the variable `CLIENT_ID` and replace the existing value with the `Client ID` of the user managed identity that you created in earlier steps.

### Step 4: Run the sample

From a [Windows PowerShell](http://%SystemRoot%\system32\WindowsPowerShell\v1.0\powershell.exe), run the sample using the following command:
```
.\AADManagedIdentity\AADManagedIdentity-main\java\bin\java.exe -jar .\AADManagedIdentity\AADManagedIdentity-main\target\AADManagedIdentity-1.0.0.jar .\AADManagedIdentity\AADManagedIdentity-main\src\main\resources\application.properties
```

The sample application will start and it will display similar as below:
<br>
`You have successfully logged on as: 'f330da90-d9b9-4433-92e6-b4aa3508fa25@1194df16-3ae0-49aa-b48b-5c4da6e13689'`

### Build: re-Build the sample

The sample is already built and the `AADManagedIdentity-1.0.0.jar` is already stored in the `target` folder in git.
<br>
Optionally, you may want to update and re-build the sample using the following `maven` command:
```
mvn clean compile assembly:single
```

This will generate a `AADManagedIdentity-1.0.0.jar` file in your `target` folder. 

## More information

For more information, see:

- [MSAL4J](https://github.com/AzureAD/microsoft-authentication-library-for-java/wiki) conceptual documentation.
- [Microsoft identity platform (Azure Active Directory for developers)](https://docs.microsoft.com/azure/active-directory/develop/)
- [Java support on Azure and Azure Stack](https://learn.microsoft.com/en-us/azure/developer/java/fundamentals/java-support-on-azure?source=recommendations)
- [Java JDK 11](https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.18%2B10/OpenJDK11U-jdk_x64_windows_hotspot_11.0.18_10.msi)
- [Feature dependencies of the Microsoft JDBC Driver for SQL Server](https://learn.microsoft.com/en-us/sql/connect/jdbc/feature-dependencies-of-microsoft-jdbc-driver-for-sql-server?view=sql-server-ver16)
- [git for windows](https://git-scm.com/download/win)
- [maven](https://maven.apache.org/)
- [Az.Sql](https://www.powershellgallery.com/packages/Az.Sql/3.4.0) module 3.4 or higher is required when using PowerShell for user-assigned managed identities.
- [Connection pooling](https://learn.microsoft.com/en-us/sql/connect/jdbc/using-connection-pooling?view=sql-server-ver16) scenarios require the connection pool implementation to use the standard JDBC connection pooling classes.
  <br>
  The `SQLServerXADataSource` class provides database connections for use in distributed (XA) transactions. 
  <br>
  The `SQLServerXADataSource` class also supports connection pooling of physical connections. 
  <br>
  The `SQLServerXADataSource` and `SQLServerXAConnection` interfaces, which are defined in the package `javax.sql`, are implemented by SQL Server.
  <br>
- [Create an Azure SQL Managed Instance with a user-assigned managed identity](https://learn.microsoft.com/en-us/azure/azure-sql/managed-instance/authentication-azure-ad-user-assigned-managed-identity-create-managed-instance?source=recommendations&view=azuresql&tabs=azure-portal)
- [Create an Azure SQL Database server with a user-assigned managed identity](https://learn.microsoft.com/en-us/azure/azure-sql/database/authentication-azure-ad-user-assigned-managed-identity-create-server?view=azuresql&tabs=azure-portal)
- [Assign Azure roles to a managed identity (Preview)](https://learn.microsoft.com/en-us/azure/role-based-access-control/role-assignments-portal-managed-identity#user-assigned-managed-identity)
- To assign a user-assigned identity to a VM, your account needs the `Virtual Machine Contributor` and `Managed Identity Operator` role assignments. 
- You need to create a SQL user from the managed identity in the target database by using the `CREATE USER` statement. For more information, see Using Azure Active Directory authentication with SqlClient.
- [Connect to Azure SQL with Azure AD authentication and SqlClient](https://learn.microsoft.com/en-us/sql/connect/ado-net/sql/azure-active-directory-authentication?view=sql-server-ver16)
- [User Assigned Managed Identity for Azure Sql Server](https://stackoverflow.com/questions/74119989/user-assigned-managed-identity-for-azure-sql-server)
- [Create a user-assigned managed identity](https://learn.microsoft.com/en-us/azure/azure-sql/database/authentication-azure-ad-user-assigned-managed-identity?source=recommendations&view=azuresql#creating-a-user-assigned-managed-identity)
- [Using Active Directory Managed Identity authentication](https://learn.microsoft.com/en-us/sql/connect/ado-net/sql/azure-active-directory-authentication?view=sql-server-ver16#using-active-directory-managed-identity-authentication)
- [Using Active Directory Managed Identity authentication](https://learn.microsoft.com/en-us/sql/connect/ado-net/sql/azure-active-directory-authentication?view=sql-server-ver16#using-active-directory-managed-identity-authentication)
- [Using a user-assigned managed identity for an Azure Automation account](https://learn.microsoft.com/en-us/azure/automation/add-user-assigned-identity)
- [Managed identities in Azure AD for Azure SQL - Set a managed identity in the Azure portal](https://learn.microsoft.com/en-us/azure/azure-sql/database/authentication-azure-ad-user-assigned-managed-identity?view=azuresql#set-a-managed-identity-in-the-azure-portal)
- [Managed identities in Azure AD for Azure SQL - Permissions](https://learn.microsoft.com/en-us/azure/azure-sql/database/authentication-azure-ad-user-assigned-managed-identity?view=azuresql#permissions)
- [Managed identities in Azure AD for Azure SQL](https://learn.microsoft.com/en-us/azure/azure-sql/database/authentication-azure-ad-user-assigned-managed-identity?view=azuresql)
- [Managed identities in Azure AD for Azure SQL](https://learn.microsoft.com/en-us/azure/azure-sql/database/authentication-azure-ad-user-assigned-managed-identity?view=azuresql)
- [Tutorial: Use a Windows VM system-assigned managed identity to access Azure Key Vault](https://learn.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/tutorial-windows-vm-access-nonaad)
- [Configure managed identities for Azure resources on a VM using the Azure portal](https://learn.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/qs-configure-portal-windows-vm#user-assigned-managed-identity)
- [Tutorial: Use a Windows VM system-assigned managed identity to access Azure SQL](https://learn.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/tutorial-windows-vm-access-sql)
<br>
For more information about the Microsoft identity platform, see:

- [https://aka.ms/aadv2](https://aka.ms/aadv2)

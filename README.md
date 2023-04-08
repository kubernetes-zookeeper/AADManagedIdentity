# Azure Active Directory Managed User Identity Authentication

## How to run this sample

To run this sample, you'll need:

- [Azure User Managed Identity](https://learn.microsoft.com/en-us/azure/azure-sql/database/authentication-azure-ad-user-assigned-managed-identity?view=azuresql#creating-a-user-assigned-managed-identity)
- [Azure Windows virtual machine](https://learn.microsoft.com/en-us/azure/virtual-machines/windows/quick-create-portal#create-virtual-machine)
- [Azure SQL database](https://learn.microsoft.com/en-us/azure/azure-sql/database/authentication-azure-ad-user-assigned-managed-identity-create-server?view=azuresql&tabs=azure-portal)
- An Azure Active Directory (Azure AD) tenant. For more information on how to get an Azure AD tenant, see [How to get an Azure AD tenant](https://azure.microsoft.com/documentation/articles/active-directory-howto-tenant/).
- One or more user accounts in your Azure AD tenant.
- Optionally, working installation of [Java 11](https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.18%2B10/OpenJDK11U-jdk_x64_windows_hotspot_11.0.18_10.msi) or greater and [Maven](https://maven.apache.org/).

### Step 1:  Clone or download this repository

From a [Windows PowerShell](%SystemRoot%\system32\WindowsPowerShell\v1.0\powershell.exe) or [command line](%windir%\system32\cmd.exe):

```Shell
mkdir AADManagedIdentity
cd AADManagedIdentity
```
```Shell
git clone https://github.com/kubernetes-zookeeper/AADManagedIdentity.git
```
or download and extract the repository [.zip](https://github.com/kubernetes-zookeeper/AADManagedIdentity/archive/refs/heads/main.zip) file.
```Shell
Invoke-WebRequest  https://github.com/kubernetes-zookeeper/AADManagedIdentity/archive/refs/heads/main.zip -UseBasicParsing -OutFile AADManagedIdentity.zip
```
```Shell
Expand-Archive  .\AADManagedIdentity.zip
```

#### Choose the Azure AD tenant where you want to create your applications

As a first step you'll need to:

1. Sign in to the [Azure portal](https://portal.azure.com) using either a work or school account or a personal Microsoft account.
2. If your account is present in more than one Azure AD tenant, select your profile at the top right corner in the menu on top of the page, and then **switch directory**.
   Change your portal session to the desired Azure AD tenant.
3. In the portal menu, select the **Managed Identities** service, and then select **Create**.
4. [Create User Assigned Managed Identity](https://learn.microsoft.com/en-us/azure/azure-sql/database/authentication-azure-ad-user-assigned-managed-identity?view=azuresql)
5. To allow the User Managed Identity to read from Microsoft Active Directory Graph as the server identity, you need to grant the following [permissions](https://learn.microsoft.com/en-us/azure/azure-sql/database/authentication-azure-ad-user-assigned-managed-identity?view=azuresql#permissions), or give the UMI the [Directory Readers](https://learn.microsoft.com/en-us/azure/azure-sql/database/authentication-aad-directory-readers-role-tutorial?view=azuresql) role.<br>These permissions should be granted before you provision a logical Azure SQL server or managed Azure SQL instance.<br>
<li><b>User.Read.All</b>: Allows access to Azure AD user information.
<li><b>GroupMember.Read.All</b>: Allows access to Azure AD group information.
<li><b>Application.Read.ALL</b>: Allows access to Azure AD service principal (application) information.
6. [Configure managed identities for Azure resources on a VM using the Azure portal](https://learn.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/qs-configure-portal-windows-vm#user-assigned-managed-identity)
7. To assign a user-assigned identity to a VM, your account needs the "Virtual Machine Contributor" and "Managed Identity Operator" role assignments.
8. [Create an Azure SQL Database server with a user-assigned managed identity](https://learn.microsoft.com/en-us/azure/azure-sql/database/authentication-azure-ad-user-assigned-managed-identity-create-server?view=azuresql&tabs=azure-portal)

##### Configure the  client app (active-directory-java-deviceprofile) to use your app registration

>In the steps below, "CLIENT_ID" is the same as "Application ID" or "AppId".

[application.properties](https://github.com/kubernetes-zookeeper/AADManagedIdentity/blob/main/src/main/resources/application.properties)
```
SERVER_NAME=tdmmsi.database.windows.net
DATABASE_NAME=tdmmsi
CLIENT_ID=f330da90-xxxx-xxxx-xxxx-b5aa3508fa25
```

1. Open the `src\main\resources\application.properties` configuration file
2. Find the variable `CLIENT_ID` and replace the existing value with the application ID (clientId) that you recorded in earlier steps.
3. Find the variable `SERVER_NAME` and replace the existing value with the application ID (clientId) that you recorded in earlier steps.
4. Find the variable `DATABASE_NAME` and replace the existing value with the application ID (clientId) that you recorded in earlier steps.

### Step 4: Run the sample

From a [Windows PowerShell](%SystemRoot%\system32\WindowsPowerShell\v1.0\powershell.exe) or [command line](%windir%\system32\cmd.exe):

- `$ mvn clean compile assembly:single`

This will generate a `AADManagedIdentity-1.0.0.jar` file in your /targets directory. Run this using your Java executable like below:

- `$ java -jar AADManagedIdentity-1.0.0.jar`


Application will start and it will display similar as below:
`You have successfully logged on as: 'f330da90-d9b9-4433-92e6-b4aa3508fa25@1194df16-3ae0-49aa-b48b-5c4da6e13689'`


## More information

For more information, see:

- MSAL4J [conceptual documentation](https://github.com/AzureAD/microsoft-authentication-library-for-java/wiki).
- [Device Code Flow for devices without a Web browser](https://github.com/AzureAD/microsoft-authentication-library-for-java/wiki/Device-Code-Flow).
- [In-memory Token cache](https://github.com/AzureAD/microsoft-authentication-library-for-java/wiki/Token-Cache)
- [Microsoft identity platform (Azure Active Directory for developers)](https://docs.microsoft.com/azure/active-directory/develop/)
- [Quickstart: Register an application with the Microsoft identity platform (Preview)](https://docs.microsoft.com/azure/active-directory/develop/quickstart-register-app)
- [Quickstart: Configure a client application to access web APIs (Preview)](https://docs.microsoft.com/azure/active-directory/develop/quickstart-configure-app-access-web-apis)

- [Understanding Azure AD application consent experiences](https://docs.microsoft.com/azure/active-directory/develop/application-consent-experience)
- [Understand user and admin consent](https://docs.microsoft.com/azure/active-directory/develop/howto-convert-app-to-be-multi-tenant#understand-user-and-admin-consent)
- [Application and service principal objects in Azure Active Directory](https://docs.microsoft.com/azure/active-directory/develop/app-objects-and-service-principals)

- [National Clouds](https://docs.microsoft.com/azure/active-directory/develop/authentication-national-cloud#app-registration-endpoints)

For more information about the Microsoft identity platform, see:

- [https://aka.ms/aadv2](https://aka.ms/aadv2)

For more information about how OAuth 2.0 protocols work in this scenario and other scenarios, see [Authentication Scenarios for Azure AD](http://go.microsoft.com/fwlink/?LinkId=394414)

---
id: javadoc_Other
title: Other
sidebar_label: Other
---
                                                
```
     __        __           ___    ____  ____
  /\ \ \ _  __/ /______    /   |  / __ \/  _/
 /  \/ / / / / __/ ___/   / /| | / /_/ // /   
/ /\  / /_/ / /_(__  )   / ___ |/ ____// /       
\_\ \/\__,_/\__/____/   /_/  |_/_/   /___/  version 0.7.0
```

## ☕ NutsConstants.BootstrapURLs
```java
public static final net.vpc.app.nuts.NutsConstants.BootstrapURLs
```

### 📢❄ Constant Fields
#### 📢❄ LOCAL_MAVEN_CENTRAL
```java
public static final String LOCAL_MAVEN_CENTRAL = "~/.m2/repository"
```
#### 📢❄ LOCAL_NUTS_FOLDER
```java
public static final String LOCAL_NUTS_FOLDER = "${home.config}/.vpc-public-nuts"
```
#### 📢❄ REMOTE_MAVEN_CENTRAL
```java
public static final String REMOTE_MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2"
```
#### 📢❄ REMOTE_MAVEN_GIT
```java
public static final String REMOTE_MAVEN_GIT = "https://raw.githubusercontent.com/thevpc/vpc-public-maven/master"
```
#### 📢❄ REMOTE_NUTS_GIT
```java
public static final String REMOTE_NUTS_GIT = "https://raw.githubusercontent.com/thevpc/vpc-public-nuts/master"
```
## ☕ NutsConstants.Files
```java
public static final net.vpc.app.nuts.NutsConstants.Files
```
 file related constants

### 📢❄ Constant Fields
#### 📢❄ DESCRIPTOR_FILE_EXTENSION
```java
public static final String DESCRIPTOR_FILE_EXTENSION = ".nuts"
```
#### 📢❄ DESCRIPTOR_FILE_NAME
```java
public static final String DESCRIPTOR_FILE_NAME = "nuts.json"
```
#### 📢❄ NUTS_COMMAND_FILE_EXTENSION
```java
public static final String NUTS_COMMAND_FILE_EXTENSION = ".nuts-cmd-alias.json"
```
#### 📢❄ REPOSITORY_CONFIG_FILE_NAME
```java
public static final String REPOSITORY_CONFIG_FILE_NAME = "nuts-repository.json"
```
#### 📢❄ WORKSPACE_API_CONFIG_FILE_NAME
```java
public static final String WORKSPACE_API_CONFIG_FILE_NAME = "nuts-api-config.json"
```
#### 📢❄ WORKSPACE_CONFIG_FILE_NAME
```java
public static final String WORKSPACE_CONFIG_FILE_NAME = "nuts-workspace.json"
```
#### 📢❄ WORKSPACE_EXTENSION_CACHE_FILE_NAME
```java
public static final String WORKSPACE_EXTENSION_CACHE_FILE_NAME = "nuts-extension-cache.json"
```
#### 📢❄ WORKSPACE_RUNTIME_CACHE_FILE_NAME
```java
public static final String WORKSPACE_RUNTIME_CACHE_FILE_NAME = "nuts-runtime-cache.json"
```
## ☕ NutsConstants.Folders
```java
public static final net.vpc.app.nuts.NutsConstants.Folders
```
 default folder names

### 📢❄ Constant Fields
#### 📢❄ BOOT
```java
public static final String BOOT = "boot"
```
#### 📢❄ ID
```java
public static final String ID = "id"
```
#### 📢❄ REPOSITORIES
```java
public static final String REPOSITORIES = "repos"
```
## ☕ NutsConstants.IdProperties
```java
public static final net.vpc.app.nuts.NutsConstants.IdProperties
```
 Nuts Id query parameter names. Nuts id has the following form
 namespace://group:name#version?query where query is in the form
 key=value\{\@literal \@\}key=value...
 \<p\>
 This class defines all standard key names and their default values in the
 query part.

### 📢❄ Constant Fields
#### 📢❄ ARCH
```java
public static final String ARCH = "arch"
```
#### 📢❄ CLASSIFIER
```java
public static final String CLASSIFIER = "classifier"
```
#### 📢❄ EXCLUSIONS
```java
public static final String EXCLUSIONS = "exclusions"
```
#### 📢❄ FACE
```java
public static final String FACE = "face"
```
#### 📢❄ NAMESPACE
```java
public static final String NAMESPACE = "namespace"
```
#### 📢❄ OPTIONAL
```java
public static final String OPTIONAL = "optional"
```
#### 📢❄ OS
```java
public static final String OS = "os"
```
#### 📢❄ OSDIST
```java
public static final String OSDIST = "osdist"
```
#### 📢❄ PACKAGING
```java
public static final String PACKAGING = "packaging"
```
#### 📢❄ PLATFORM
```java
public static final String PLATFORM = "platform"
```
#### 📢❄ SCOPE
```java
public static final String SCOPE = "scope"
```
#### 📢❄ VERSION
```java
public static final String VERSION = "version"
```
## ☕ NutsConstants.Ids
```java
public static final net.vpc.app.nuts.NutsConstants.Ids
```
 identifier related constants

### 📢❄ Constant Fields
#### 📢❄ NUTS_API
```java
public static final String NUTS_API = "net.vpc.app.nuts:nuts"
```
#### 📢❄ NUTS_RUNTIME
```java
public static final String NUTS_RUNTIME = "net.vpc.app.nuts:nuts-core"
```
#### 📢❄ NUTS_SHELL
```java
public static final String NUTS_SHELL = "net.vpc.app.nuts.toolbox:nsh"
```
## ☕ NutsConstants.Names
```java
public static final net.vpc.app.nuts.NutsConstants.Names
```
 name constants

### 📢❄ Constant Fields
#### 📢❄ DEFAULT_REPOSITORY_NAME
```java
public static final String DEFAULT_REPOSITORY_NAME = "local"
```
#### 📢❄ DEFAULT_WORKSPACE_NAME
```java
public static final String DEFAULT_WORKSPACE_NAME = "default-workspace"
```
## ☕ NutsConstants.Permissions
```java
public static final net.vpc.app.nuts.NutsConstants.Permissions
```
 standard right keys for distinct operations in nuts.

### 📢❄ Constant Fields
#### 📢❄ ADD_REPOSITORY
```java
public static final String ADD_REPOSITORY = "add-repo"
```
#### 📢❄ ADMIN
```java
public static final String ADMIN = "admin"
```
#### 📢❄ ALL
```java
public static final Set ALL
```
#### 📢❄ AUTO_INSTALL
```java
public static final String AUTO_INSTALL = "auto-install"
```
#### 📢❄ DEPLOY
```java
public static final String DEPLOY = "deploy"
```
#### 📢❄ EXEC
```java
public static final String EXEC = "exec"
```
#### 📢❄ FETCH_CONTENT
```java
public static final String FETCH_CONTENT = "fetch-content"
```
#### 📢❄ FETCH_DESC
```java
public static final String FETCH_DESC = "fetch-desc"
```
#### 📢❄ INSTALL
```java
public static final String INSTALL = "install"
```
#### 📢❄ PUSH
```java
public static final String PUSH = "push"
```
#### 📢❄ REMOVE_REPOSITORY
```java
public static final String REMOVE_REPOSITORY = "remove-repo"
```
#### 📢❄ SAVE
```java
public static final String SAVE = "save"
```
#### 📢❄ SET_PASSWORD
```java
public static final String SET_PASSWORD = "set-password"
```
#### 📢❄ UNDEPLOY
```java
public static final String UNDEPLOY = "undeploy"
```
#### 📢❄ UNINSTALL
```java
public static final String UNINSTALL = "uninstall"
```
#### 📢❄ UPDATE
```java
public static final String UPDATE = "update"
```
## ☕ NutsConstants.QueryFaces
```java
public static final net.vpc.app.nuts.NutsConstants.QueryFaces
```
 valid values for Query parameter "face"

### 📢❄ Constant Fields
#### 📢❄ CONTENT
```java
public static final String CONTENT = "content"
```
#### 📢❄ CONTENT_HASH
```java
public static final String CONTENT_HASH = "content-hash"
```
#### 📢❄ DESCRIPTOR
```java
public static final String DESCRIPTOR = "descriptor"
```
#### 📢❄ DESCRIPTOR_HASH
```java
public static final String DESCRIPTOR_HASH = "descriptor-hash"
```
## ☕ NutsConstants.RepoTypes
```java
public static final net.vpc.app.nuts.NutsConstants.RepoTypes
```

### 📢❄ Constant Fields
#### 📢❄ MAVEN
```java
public static final String MAVEN = "maven"
```
#### 📢❄ NUTS
```java
public static final String NUTS = "nuts"
```
#### 📢❄ NUTS_SERVER
```java
public static final String NUTS_SERVER = "nuts-server"
```
## ☕ NutsConstants.Users
```java
public static final net.vpc.app.nuts.NutsConstants.Users
```
 nuts standard user names

### 📢❄ Constant Fields
#### 📢❄ ADMIN
```java
public static final String ADMIN = "admin"
```
#### 📢❄ ANONYMOUS
```java
public static final String ANONYMOUS = "anonymous"
```
## ☕ NutsConstants.Versions
```java
public static final net.vpc.app.nuts.NutsConstants.Versions
```
 version special names

### 📢❄ Constant Fields
#### 📢❄ LATEST
```java
public static final String LATEST = "LATEST"
```
#### 📢❄ RELEASE
```java
public static final String RELEASE = "RELEASE"
```
## ☕ NutsExecCommandFormat.ArgEntry
```java
public static interface net.vpc.app.nuts.NutsExecCommandFormat.ArgEntry
```
 argument entry

### 🎛 Instance Properties
#### 📄🎛 index
argument index
```java
[read-only] int public index
public int getIndex()
```
#### 📄🎛 value
argument value
```java
[read-only] String public value
public String getValue()
```
## ☕ NutsExecCommandFormat.EnvEntry
```java
public static interface net.vpc.app.nuts.NutsExecCommandFormat.EnvEntry
```
 env entry

### 🎛 Instance Properties
#### 📄🎛 name
env name
```java
[read-only] String public name
public String getName()
```
#### 📄🎛 value
env value
```java
[read-only] String public value
public String getValue()
```
## ☕ NutsIOCopyValidationException
```java
public net.vpc.app.nuts.NutsIOCopyValidationException
```
 Exception thrown when copy validation fails

### 🪄 Constructors
#### 🪄 NutsIOCopyValidationException(ws)
Constructs a new Validation Exception

```java
NutsIOCopyValidationException(NutsWorkspace ws)
```
- **NutsWorkspace ws** : 

#### 🪄 NutsIOCopyValidationException(ws, cause)
Constructs a new Validation Exception

```java
NutsIOCopyValidationException(NutsWorkspace ws, Throwable cause)
```
- **NutsWorkspace ws** : 
- **Throwable cause** : cause

#### 🪄 NutsIOCopyValidationException(ws, message)
Constructs a new Validation Exception

```java
NutsIOCopyValidationException(NutsWorkspace ws, String message)
```
- **NutsWorkspace ws** : 
- **String message** : message

#### 🪄 NutsIOCopyValidationException(ws, message, cause)
Constructs a new Validation Exception

```java
NutsIOCopyValidationException(NutsWorkspace ws, String message, Throwable cause)
```
- **NutsWorkspace ws** : 
- **String message** : message
- **Throwable cause** : cause

## ☕ NutsRepositoryRef
```java
public net.vpc.app.nuts.NutsRepositoryRef
```

 \@author vpc
 \@since 0.5.4

### 🪄 Constructors
#### 🪄 NutsRepositoryRef()


```java
NutsRepositoryRef()
```

#### 🪄 NutsRepositoryRef(other)


```java
NutsRepositoryRef(NutsRepositoryRef other)
```
- **NutsRepositoryRef other** : 

#### 🪄 NutsRepositoryRef(name, location, deployPriority, enabled)


```java
NutsRepositoryRef(String name, String location, int deployPriority, boolean enabled)
```
- **String name** : 
- **String location** : 
- **int deployPriority** : 
- **boolean enabled** : 

### 🎛 Instance Properties
#### 📄🎛 deployOrder

```java
[read-only] int public deployOrder
public int getDeployOrder()
```
#### 📄🎛 enabled

```java
[read-only] boolean public enabled
public boolean isEnabled()
```
#### 📄🎛 failSafe

```java
[read-only] boolean public failSafe
public boolean isFailSafe()
```
#### 📄🎛 location

```java
[read-only] String public location
public String getLocation()
```
#### 📄🎛 name

```java
[read-only] String public name
public String getName()
```
### ⚙ Instance Methods
#### ⚙ copy()


```java
NutsRepositoryRef copy()
```
**return**:NutsRepositoryRef

#### ⚙ equals(obj)


```java
boolean equals(Object obj)
```
**return**:boolean
- **Object obj** : 

#### ⚙ hashCode()


```java
int hashCode()
```
**return**:int

#### ⚙ setDeployOrder(deployPriority)


```java
NutsRepositoryRef setDeployOrder(int deployPriority)
```
**return**:NutsRepositoryRef
- **int deployPriority** : 

#### ⚙ setEnabled(enabled)


```java
NutsRepositoryRef setEnabled(boolean enabled)
```
**return**:NutsRepositoryRef
- **boolean enabled** : 

#### ⚙ setFailSafe(failSafe)


```java
NutsRepositoryRef setFailSafe(boolean failSafe)
```
**return**:NutsRepositoryRef
- **boolean failSafe** : 

#### ⚙ setLocation(location)


```java
NutsRepositoryRef setLocation(String location)
```
**return**:NutsRepositoryRef
- **String location** : 

#### ⚙ setName(name)


```java
NutsRepositoryRef setName(String name)
```
**return**:NutsRepositoryRef
- **String name** : 

#### ⚙ toString()


```java
String toString()
```
**return**:String

## ☕ NutsRepositorySecurityManager
```java
public interface net.vpc.app.nuts.NutsRepositorySecurityManager
```

 \@author vpc
 \@since 0.5.4

### 🎛 Instance Properties
#### 📄🎛 allowed

```java
[read-only] boolean public allowed
public boolean isAllowed(right)
```
#### 📄🎛 authenticationAgent

```java
[read-only] NutsAuthenticationAgent public authenticationAgent
public NutsAuthenticationAgent getAuthenticationAgent(id)
```
#### 📄🎛 effectiveUser

```java
[read-only] NutsUser public effectiveUser
public NutsUser getEffectiveUser(username)
```
### ⚙ Instance Methods
#### ⚙ addUser(name)


```java
NutsAddUserCommand addUser(String name)
```
**return**:NutsAddUserCommand
- **String name** : 

#### ⚙ checkAllowed(right, operationName)


```java
NutsRepositorySecurityManager checkAllowed(String right, String operationName)
```
**return**:NutsRepositorySecurityManager
- **String right** : 
- **String operationName** : 

#### ⚙ checkCredentials(credentialsId, password)
check if the given \<code\>password\</code\> is valid against the one stored
 by the Authentication Agent for  \<code\>credentialsId\</code\>

```java
void checkCredentials(char[] credentialsId, char[] password)
```
- **char[] credentialsId** : credentialsId
- **char[] password** : password

#### ⚙ createCredentials(credentials, allowRetreive, credentialId)
store credentials in the agent\'s and return the credential id to store
 into the config. if credentialId is not null, the given credentialId will
 be updated and the credentialId is returned. The \{\@code credentialsId\},if
 present or returned, \<strong\>MUST\</strong\> be prefixed with
 AuthenticationAgent\'d id and \':\' character

```java
char[] createCredentials(char[] credentials, boolean allowRetreive, char[] credentialId)
```
**return**:char[]
- **char[] credentials** : credential
- **boolean allowRetreive** : when true {@link #getCredentials(char[])} can be invoked over {@code credentialId}
- **char[] credentialId** : preferred credentialId, if null, a new one is created

#### ⚙ findUsers()


```java
NutsUser[] findUsers()
```
**return**:NutsUser[]

#### ⚙ getCredentials(credentialsId)
get the credentials for the given id. The \{\@code credentialsId\}
 \<strong\>MUST\</strong\> be prefixed with AuthenticationAgent\'d id and \':\'
 character

```java
char[] getCredentials(char[] credentialsId)
```
**return**:char[]
- **char[] credentialsId** : credentials-id

#### ⚙ removeCredentials(credentialsId)
remove existing credentials with the given id The \{\@code credentialsId\}
 \<strong\>MUST\</strong\> be prefixed with AuthenticationAgent\'d id and \':\'
 character

```java
boolean removeCredentials(char[] credentialsId)
```
**return**:boolean
- **char[] credentialsId** : credentials-id

#### ⚙ removeUser(name)


```java
NutsRemoveUserCommand removeUser(String name)
```
**return**:NutsRemoveUserCommand
- **String name** : 

#### ⚙ setAuthenticationAgent(authenticationAgent, options)


```java
NutsRepositorySecurityManager setAuthenticationAgent(String authenticationAgent, NutsUpdateOptions options)
```
**return**:NutsRepositorySecurityManager
- **String authenticationAgent** : 
- **NutsUpdateOptions options** : 

#### ⚙ updateUser(name)


```java
NutsUpdateUserCommand updateUser(String name)
```
**return**:NutsUpdateUserCommand
- **String name** : 

## ☕ NutsTableFormat.Separator
```java
public static final net.vpc.app.nuts.NutsTableFormat.Separator
```

### 📢❄ Constant Fields
#### 📢❄ FIRST_ROW_END
```java
public static final Separator FIRST_ROW_END
```
#### 📢❄ FIRST_ROW_LINE
```java
public static final Separator FIRST_ROW_LINE
```
#### 📢❄ FIRST_ROW_SEP
```java
public static final Separator FIRST_ROW_SEP
```
#### 📢❄ FIRST_ROW_START
```java
public static final Separator FIRST_ROW_START
```
#### 📢❄ LAST_ROW_END
```java
public static final Separator LAST_ROW_END
```
#### 📢❄ LAST_ROW_LINE
```java
public static final Separator LAST_ROW_LINE
```
#### 📢❄ LAST_ROW_SEP
```java
public static final Separator LAST_ROW_SEP
```
#### 📢❄ LAST_ROW_START
```java
public static final Separator LAST_ROW_START
```
#### 📢❄ MIDDLE_ROW_END
```java
public static final Separator MIDDLE_ROW_END
```
#### 📢❄ MIDDLE_ROW_LINE
```java
public static final Separator MIDDLE_ROW_LINE
```
#### 📢❄ MIDDLE_ROW_SEP
```java
public static final Separator MIDDLE_ROW_SEP
```
#### 📢❄ MIDDLE_ROW_START
```java
public static final Separator MIDDLE_ROW_START
```
#### 📢❄ ROW_END
```java
public static final Separator ROW_END
```
#### 📢❄ ROW_SEP
```java
public static final Separator ROW_SEP
```
#### 📢❄ ROW_START
```java
public static final Separator ROW_START
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
Separator valueOf(String name)
```
**return**:Separator
- **String name** : 

#### 📢⚙ values()


```java
Separator[] values()
```
**return**:Separator[]

### ⚙ Instance Methods
#### ⚙ id()
lower cased identifier.

```java
String id()
```
**return**:String


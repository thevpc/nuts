---
id: javadoc_Security
title: Security
sidebar_label: Security
---
                                                
```
     __        __           ___    ____  ____
  /\ \ \ _  __/ /______    /   |  / __ \/  _/
 /  \/ / / / / __/ ___/   / /| | / /_/ // /   
/ /\  / /_/ / /_(__  )   / ___ |/ ____// /       
\_\ \/\__,_/\__/____/   /_/  |_/_/   /___/  version 0.7.0
```

## ☕ NutsAddUserCommand
```java
public interface net.vpc.app.nuts.NutsAddUserCommand
```
 Command class for adding users to workspaces and repositories. All Command
 classes have a \'run\' method to perform the operation.

 \@see NutsWorkspaceSecurityManager#addUser(java.lang.String)
 \@see NutsRepositorySecurityManager#addUser(java.lang.String)

 \@author vpc
 \@since 0.5.5
 \@category Security

### 🎛 Instance Properties
#### 📝🎛 credentials
return credentials
```java
[read-write] NutsAddUserCommand public credentials
public char[] getCredentials()
public NutsAddUserCommand setCredentials(password)
```
#### 📄🎛 groups
group list defined by \{\@link #addGroup\}, \@link \{\@link #addGroups(String...)\}\} and \@link \{\@link #addGroups(Collection)\}\}
```java
[read-only] String[] public groups
public String[] getGroups()
```
#### 📄🎛 permissions
return permissions
```java
[read-only] String[] public permissions
public String[] getPermissions()
```
#### 📝🎛 remoteCredentials
set remote credentials
```java
[read-write] NutsAddUserCommand public remoteCredentials
public char[] getRemoteCredentials()
public NutsAddUserCommand setRemoteCredentials(password)
```
#### 📝🎛 remoteIdentity
set remote identity
```java
[read-write] NutsAddUserCommand public remoteIdentity
public String getRemoteIdentity()
public NutsAddUserCommand setRemoteIdentity(remoteIdentity)
```
#### ✏🎛 session
update session
```java
[write-only] NutsAddUserCommand public session
public NutsAddUserCommand setSession(session)
```
#### 📝🎛 username
set username
```java
[read-write] NutsAddUserCommand public username
public String getUsername()
public NutsAddUserCommand setUsername(username)
```
### ⚙ Instance Methods
#### ⚙ addGroup(group)
add group named \{\@code group\} to the specified user

```java
NutsAddUserCommand addGroup(String group)
```
**return**:NutsAddUserCommand
- **String group** : group name

#### ⚙ addGroups(groups)
add group list named \{\@code groups\} to the specified user

```java
NutsAddUserCommand addGroups(String[] groups)
```
**return**:NutsAddUserCommand
- **String[] groups** : group list

#### ⚙ addGroups(groups)
add group list named \{\@code groups\} to the specified user

```java
NutsAddUserCommand addGroups(Collection groups)
```
**return**:NutsAddUserCommand
- **Collection groups** : group list

#### ⚙ addPermission(permission)
add permission named \{\@code permission\} to the specified user

```java
NutsAddUserCommand addPermission(String permission)
```
**return**:NutsAddUserCommand
- **String permission** : permission name from {@code NutsConstants.Permissions}

#### ⚙ addPermissions(permissions)
add permissions list named \{\@code permissions\} to the specified user

```java
NutsAddUserCommand addPermissions(String[] permissions)
```
**return**:NutsAddUserCommand
- **String[] permissions** : group list

#### ⚙ addPermissions(permissions)
add permissions list named \{\@code permissions\} to the specified user

```java
NutsAddUserCommand addPermissions(Collection permissions)
```
**return**:NutsAddUserCommand
- **Collection permissions** : group list

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsAddUserCommand configure(boolean skipUnsupported, String[] args)
```
**return**:NutsAddUserCommand
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ copySession()
copy session

```java
NutsAddUserCommand copySession()
```
**return**:NutsAddUserCommand

#### ⚙ removeGroups(groups)
remove group

```java
NutsAddUserCommand removeGroups(String[] groups)
```
**return**:NutsAddUserCommand
- **String[] groups** : new value

#### ⚙ removeGroups(groups)
remove groups

```java
NutsAddUserCommand removeGroups(Collection groups)
```
**return**:NutsAddUserCommand
- **Collection groups** : groups to remove

#### ⚙ removePermissions(permissions)
remove permissions

```java
NutsAddUserCommand removePermissions(String[] permissions)
```
**return**:NutsAddUserCommand
- **String[] permissions** : permission to remove

#### ⚙ removePermissions(permissions)
remove permissions

```java
NutsAddUserCommand removePermissions(Collection permissions)
```
**return**:NutsAddUserCommand
- **Collection permissions** : permissions to remove

#### ⚙ run()
execute the command and return this instance

```java
NutsAddUserCommand run()
```
**return**:NutsAddUserCommand

## ☕ NutsAuthenticationAgent
```java
public interface net.vpc.app.nuts.NutsAuthenticationAgent
```
 an Authentication Agent is responsible of storing and retrieving credentials
 in external repository (password manager, kwallet, keypads,
 gnome-keyring...). And Id of the stored password is then saved as plain text
 in nuts config file.
 Criteria type is a string representing authentication agent id
 \@author vpc
 \@since 0.5.4
 \@category Security

### 🎛 Instance Properties
#### 📄🎛 id
agent id;
```java
[read-only] String public id
public String getId()
```
### ⚙ Instance Methods
#### ⚙ checkCredentials(credentialsId, password, envProvider)
check if the given \<code\>password\</code\> is valid against the one stored
 by the Authentication Agent for  \<code\>credentialsId\</code\>

```java
void checkCredentials(char[] credentialsId, char[] password, Map envProvider)
```
- **char[] credentialsId** : credentialsId
- **char[] password** : password
- **Map envProvider** : environment provider, nullable

#### ⚙ createCredentials(credentials, allowRetrieve, credentialId, envProvider)
store credentials in the agent\'s and return the credential id to store
 into the config. if credentialId is not null, the given credentialId will
 be updated and the credentialId is returned. The \{\@code credentialsId\},if
 present or returned, \<strong\>MUST\</strong\> be prefixed with
 AuthenticationAgent\'d id and \':\' character

```java
char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId, Map envProvider)
```
**return**:char[]
- **char[] credentials** : credential
- **boolean allowRetrieve** : when true {@link #getCredentials(char[], Map)}  }
 can be invoked over {@code credentialId}
- **char[] credentialId** : preferred credentialId, if null, a new one is created
- **Map envProvider** : environment provider, nullable

#### ⚙ getCredentials(credentialsId, envProvider)
get the credentials for the given id. The \{\@code credentialsId\}
 \<strong\>MUST\</strong\> be prefixed with AuthenticationAgent\'d id and \':\'
 character

```java
char[] getCredentials(char[] credentialsId, Map envProvider)
```
**return**:char[]
- **char[] credentialsId** : credentials-id
- **Map envProvider** : environment provider, nullable

#### ⚙ removeCredentials(credentialsId, envProvider)
remove existing credentials with the given id The \{\@code credentialsId\}
 \<strong\>MUST\</strong\> be prefixed with AuthenticationAgent\'d id and \':\'
 character

```java
boolean removeCredentials(char[] credentialsId, Map envProvider)
```
**return**:boolean
- **char[] credentialsId** : credentials-id
- **Map envProvider** : environment provider, nullable

## ☕ NutsRemoveUserCommand
```java
public interface net.vpc.app.nuts.NutsRemoveUserCommand
```
 Remove User Command
 \@author vpc
 \@category Security

### 🎛 Instance Properties
#### ✏🎛 session
update session
```java
[write-only] NutsRemoveUserCommand public session
public NutsRemoveUserCommand setSession(session)
```
#### 📝🎛 username
set username of user to remove
```java
[read-write] NutsRemoveUserCommand public username
public String getUsername()
public NutsRemoveUserCommand setUsername(username)
```
### ⚙ Instance Methods
#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsRemoveUserCommand configure(boolean skipUnsupported, String[] args)
```
**return**:NutsRemoveUserCommand
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ copySession()
copy session

```java
NutsRemoveUserCommand copySession()
```
**return**:NutsRemoveUserCommand

#### ⚙ run()
execute the command and return this instance

```java
NutsRemoveUserCommand run()
```
**return**:NutsRemoveUserCommand

#### ⚙ username(username)
set username of user to remove

```java
NutsRemoveUserCommand username(String username)
```
**return**:NutsRemoveUserCommand
- **String username** : user name

## ☕ NutsUpdateUserCommand
```java
public interface net.vpc.app.nuts.NutsUpdateUserCommand
```

 \@author vpc
 \@since 0.5.5
 \@category Security

### 🎛 Instance Properties
#### 📄🎛 addGroups

```java
[read-only] String[] public addGroups
public String[] getAddGroups()
```
#### 📄🎛 addPermissions

```java
[read-only] String[] public addPermissions
public String[] getAddPermissions()
```
#### 📄🎛 credentials

```java
[read-only] char[] public credentials
public char[] getCredentials()
```
#### 📄🎛 oldCredentials

```java
[read-only] char[] public oldCredentials
public char[] getOldCredentials()
```
#### 📄🎛 remoteCredentials

```java
[read-only] char[] public remoteCredentials
public char[] getRemoteCredentials()
```
#### 📄🎛 remoteIdentity

```java
[read-only] String public remoteIdentity
public String getRemoteIdentity()
```
#### 📄🎛 removeGroups

```java
[read-only] String[] public removeGroups
public String[] getRemoveGroups()
```
#### 📄🎛 removePermissions

```java
[read-only] String[] public removePermissions
public String[] getRemovePermissions()
```
#### 📄🎛 resetGroups

```java
[read-only] boolean public resetGroups
public boolean isResetGroups()
```
#### 📄🎛 resetPermissions

```java
[read-only] boolean public resetPermissions
public boolean isResetPermissions()
```
#### ✏🎛 session
update session
```java
[write-only] NutsUpdateUserCommand public session
public NutsUpdateUserCommand setSession(session)
```
#### 📄🎛 username

```java
[read-only] String public username
public String getUsername()
```
### ⚙ Instance Methods
#### ⚙ addGroup(group)


```java
NutsUpdateUserCommand addGroup(String group)
```
**return**:NutsUpdateUserCommand
- **String group** : 

#### ⚙ addGroups(groups)


```java
NutsUpdateUserCommand addGroups(String[] groups)
```
**return**:NutsUpdateUserCommand
- **String[] groups** : 

#### ⚙ addGroups(groups)


```java
NutsUpdateUserCommand addGroups(Collection groups)
```
**return**:NutsUpdateUserCommand
- **Collection groups** : 

#### ⚙ addPermission(permission)


```java
NutsUpdateUserCommand addPermission(String permission)
```
**return**:NutsUpdateUserCommand
- **String permission** : 

#### ⚙ addPermissions(permissions)


```java
NutsUpdateUserCommand addPermissions(String[] permissions)
```
**return**:NutsUpdateUserCommand
- **String[] permissions** : 

#### ⚙ addPermissions(permissions)


```java
NutsUpdateUserCommand addPermissions(Collection permissions)
```
**return**:NutsUpdateUserCommand
- **Collection permissions** : 

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsUpdateUserCommand configure(boolean skipUnsupported, String[] args)
```
**return**:NutsUpdateUserCommand
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ copySession()
copy session

```java
NutsUpdateUserCommand copySession()
```
**return**:NutsUpdateUserCommand

#### ⚙ credentials(password)


```java
NutsUpdateUserCommand credentials(char[] password)
```
**return**:NutsUpdateUserCommand
- **char[] password** : 

#### ⚙ oldCredentials(password)


```java
NutsUpdateUserCommand oldCredentials(char[] password)
```
**return**:NutsUpdateUserCommand
- **char[] password** : 

#### ⚙ remoteCredentials(password)


```java
NutsUpdateUserCommand remoteCredentials(char[] password)
```
**return**:NutsUpdateUserCommand
- **char[] password** : 

#### ⚙ remoteIdentity(remoteIdentity)


```java
NutsUpdateUserCommand remoteIdentity(String remoteIdentity)
```
**return**:NutsUpdateUserCommand
- **String remoteIdentity** : 

#### ⚙ removeGroup(group)


```java
NutsUpdateUserCommand removeGroup(String group)
```
**return**:NutsUpdateUserCommand
- **String group** : 

#### ⚙ removeGroups(groups)


```java
NutsUpdateUserCommand removeGroups(String[] groups)
```
**return**:NutsUpdateUserCommand
- **String[] groups** : 

#### ⚙ removeGroups(groups)


```java
NutsUpdateUserCommand removeGroups(Collection groups)
```
**return**:NutsUpdateUserCommand
- **Collection groups** : 

#### ⚙ removePermission(permission)


```java
NutsUpdateUserCommand removePermission(String permission)
```
**return**:NutsUpdateUserCommand
- **String permission** : 

#### ⚙ removePermissions(permissions)


```java
NutsUpdateUserCommand removePermissions(String[] permissions)
```
**return**:NutsUpdateUserCommand
- **String[] permissions** : 

#### ⚙ removePermissions(permissions)


```java
NutsUpdateUserCommand removePermissions(Collection permissions)
```
**return**:NutsUpdateUserCommand
- **Collection permissions** : 

#### ⚙ resetGroups()


```java
NutsUpdateUserCommand resetGroups()
```
**return**:NutsUpdateUserCommand

#### ⚙ resetGroups(resetGroups)


```java
NutsUpdateUserCommand resetGroups(boolean resetGroups)
```
**return**:NutsUpdateUserCommand
- **boolean resetGroups** : 

#### ⚙ resetPermissions()


```java
NutsUpdateUserCommand resetPermissions()
```
**return**:NutsUpdateUserCommand

#### ⚙ resetPermissions(resetPermissions)


```java
NutsUpdateUserCommand resetPermissions(boolean resetPermissions)
```
**return**:NutsUpdateUserCommand
- **boolean resetPermissions** : 

#### ⚙ run()
execute the command and return this instance

```java
NutsUpdateUserCommand run()
```
**return**:NutsUpdateUserCommand

#### ⚙ setCredentials(password)


```java
NutsUpdateUserCommand setCredentials(char[] password)
```
**return**:NutsUpdateUserCommand
- **char[] password** : 

#### ⚙ setOldCredentials(oldCredentials)


```java
NutsUpdateUserCommand setOldCredentials(char[] oldCredentials)
```
**return**:NutsUpdateUserCommand
- **char[] oldCredentials** : 

#### ⚙ setRemoteCredentials(password)


```java
NutsUpdateUserCommand setRemoteCredentials(char[] password)
```
**return**:NutsUpdateUserCommand
- **char[] password** : 

#### ⚙ setRemoteIdentity(remoteIdentity)


```java
NutsUpdateUserCommand setRemoteIdentity(String remoteIdentity)
```
**return**:NutsUpdateUserCommand
- **String remoteIdentity** : 

#### ⚙ setResetGroups(resetGroups)


```java
NutsUpdateUserCommand setResetGroups(boolean resetGroups)
```
**return**:NutsUpdateUserCommand
- **boolean resetGroups** : 

#### ⚙ setResetPermissions(resetPermissions)


```java
NutsUpdateUserCommand setResetPermissions(boolean resetPermissions)
```
**return**:NutsUpdateUserCommand
- **boolean resetPermissions** : 

#### ⚙ setUsername(login)


```java
NutsUpdateUserCommand setUsername(String login)
```
**return**:NutsUpdateUserCommand
- **String login** : 

#### ⚙ undoAddGroup(group)


```java
NutsUpdateUserCommand undoAddGroup(String group)
```
**return**:NutsUpdateUserCommand
- **String group** : 

#### ⚙ undoAddGroups(groups)


```java
NutsUpdateUserCommand undoAddGroups(String[] groups)
```
**return**:NutsUpdateUserCommand
- **String[] groups** : 

#### ⚙ undoAddGroups(groups)


```java
NutsUpdateUserCommand undoAddGroups(Collection groups)
```
**return**:NutsUpdateUserCommand
- **Collection groups** : 

#### ⚙ undoAddPermission(permissions)


```java
NutsUpdateUserCommand undoAddPermission(String permissions)
```
**return**:NutsUpdateUserCommand
- **String permissions** : 

#### ⚙ undoAddPermissions(permissions)


```java
NutsUpdateUserCommand undoAddPermissions(String[] permissions)
```
**return**:NutsUpdateUserCommand
- **String[] permissions** : 

#### ⚙ undoAddPermissions(permissions)


```java
NutsUpdateUserCommand undoAddPermissions(Collection permissions)
```
**return**:NutsUpdateUserCommand
- **Collection permissions** : 

#### ⚙ undoRemoveGroups(groups)


```java
NutsUpdateUserCommand undoRemoveGroups(String[] groups)
```
**return**:NutsUpdateUserCommand
- **String[] groups** : 

#### ⚙ undoRemoveGroups(groups)


```java
NutsUpdateUserCommand undoRemoveGroups(Collection groups)
```
**return**:NutsUpdateUserCommand
- **Collection groups** : 

#### ⚙ undoRemovePermissions(permissions)


```java
NutsUpdateUserCommand undoRemovePermissions(String[] permissions)
```
**return**:NutsUpdateUserCommand
- **String[] permissions** : 

#### ⚙ undoRemovePermissions(permissions)


```java
NutsUpdateUserCommand undoRemovePermissions(Collection permissions)
```
**return**:NutsUpdateUserCommand
- **Collection permissions** : 

#### ⚙ username(login)


```java
NutsUpdateUserCommand username(String login)
```
**return**:NutsUpdateUserCommand
- **String login** : 

## ☕ NutsWorkspaceSecurityManager
```java
public interface net.vpc.app.nuts.NutsWorkspaceSecurityManager
```
 Workspace Security configuration manager
 \@author vpc
 \@since 0.5.4
 \@category Security

### 🎛 Instance Properties
#### 📄🎛 admin
return true if current user has admin privileges
```java
[read-only] boolean public admin
public boolean isAdmin()
```
#### 📄🎛 currentLoginStack
current user stack.
 this is useful when login with multiple user identities.
```java
[read-only] String[] public currentLoginStack
public String[] getCurrentLoginStack()
```
#### 📄🎛 currentUsername
current user
```java
[read-only] String public currentUsername
public String getCurrentUsername()
```
#### 📄🎛 secure
return true if workspace is running secure mode
```java
[read-only] boolean public secure
public boolean isSecure()
```
### ⚙ Instance Methods
#### ⚙ addUser(name)
create a User Create command.
 No user will be added when simply calling this method.
 You must fill in command parameters then call \{\@link NutsAddUserCommand#run()\}.

```java
NutsAddUserCommand addUser(String name)
```
**return**:NutsAddUserCommand
- **String name** : user name

#### ⚙ checkAllowed(permission, operationName)
check if allowed and throw a Security exception if not.

```java
NutsWorkspaceSecurityManager checkAllowed(String permission, String operationName)
```
**return**:NutsWorkspaceSecurityManager
- **String permission** : permission name. see {@code NutsConstants.Rights } class
- **String operationName** : operation name

#### ⚙ checkCredentials(credentialsId, password)
check if the given \<code\>password\</code\> is valid against the one stored
 by the Authentication Agent for  \<code\>credentialsId\</code\>

```java
void checkCredentials(char[] credentialsId, char[] password)
```
- **char[] credentialsId** : credentialsId
- **char[] password** : password

#### ⚙ createCredentials(credentials, allowRetrieve, credentialId)
store credentials in the agent\'s and return the credential id to store
 into the config. if credentialId is not null, the given credentialId will
 be updated and the credentialId is returned. The \{\@code credentialsId\},if
 present or returned, \<strong\>MUST\</strong\> be prefixed with
 AuthenticationAgent\'d id and \':\' character

```java
char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId)
```
**return**:char[]
- **char[] credentials** : credential
- **boolean allowRetrieve** : when true {@link #getCredentials(char[])} can be
 invoked over {@code credentialId}
- **char[] credentialId** : preferred credentialId, if null, a new one is created

#### ⚙ currentLoginStack()
equivalent to \{\@link #getCurrentLoginStack()\}.

```java
String[] currentLoginStack()
```
**return**:String[]

#### ⚙ currentUsername()
equivalent to \{\@link #getCurrentUsername()\}.

```java
String currentUsername()
```
**return**:String

#### ⚙ findUser(username)
find user with the given name or null.

```java
NutsUser findUser(String username)
```
**return**:NutsUser
- **String username** : user name

#### ⚙ findUsers()
find all registered users

```java
NutsUser[] findUsers()
```
**return**:NutsUser[]

#### ⚙ getAuthenticationAgent(authenticationAgentId)
get authentication agent with id \{\@code authenticationAgentId\}.
 if is blank, return default authentication agent

```java
NutsAuthenticationAgent getAuthenticationAgent(String authenticationAgentId)
```
**return**:NutsAuthenticationAgent
- **String authenticationAgentId** : agent id

#### ⚙ getCredentials(credentialsId)
get the credentials for the given id. The \{\@code credentialsId\}
 \<strong\>MUST\</strong\> be prefixed with AuthenticationAgent\'d id and \':\'
 character

```java
char[] getCredentials(char[] credentialsId)
```
**return**:char[]
- **char[] credentialsId** : credentials-id

#### ⚙ isAllowed(permission)
return true if permission is valid and allowed for the current user.

```java
boolean isAllowed(String permission)
```
**return**:boolean
- **String permission** : permission name. see {@code NutsConstants.Rights } class

#### ⚙ login(handler)
impersonate user and log as a distinct user with the given credentials and stack
 user name so that it can be retrieved using \@\{code getCurrentLoginStack()\}.

```java
NutsWorkspaceSecurityManager login(CallbackHandler handler)
```
**return**:NutsWorkspaceSecurityManager
- **CallbackHandler handler** : security handler

#### ⚙ login(username, password)
impersonate user and log as a distinct user with the given credentials.

```java
NutsWorkspaceSecurityManager login(String username, char[] password)
```
**return**:NutsWorkspaceSecurityManager
- **String username** : user name
- **char[] password** : user password

#### ⚙ logout()
log out from last logged in user (if any) and pop out from user name stack.

```java
NutsWorkspaceSecurityManager logout()
```
**return**:NutsWorkspaceSecurityManager

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
create a Remove Create command.
 No user will be removed when simply calling this method.
 You must fill in command parameters then call \{\@link NutsRemoveUserCommand#run()\}.

```java
NutsRemoveUserCommand removeUser(String name)
```
**return**:NutsRemoveUserCommand
- **String name** : user name

#### ⚙ setAuthenticationAgent(authenticationAgentId, options)
update default authentication agent.

```java
NutsWorkspaceSecurityManager setAuthenticationAgent(String authenticationAgentId, NutsUpdateOptions options)
```
**return**:NutsWorkspaceSecurityManager
- **String authenticationAgentId** : authentication agent id
- **NutsUpdateOptions options** : update options

#### ⚙ setSecureMode(secure, adminPassword, options)
switch from/to secure mode.
 when secure mode is disabled, no authorizations are checked against.

```java
boolean setSecureMode(boolean secure, char[] adminPassword, NutsUpdateOptions options)
```
**return**:boolean
- **boolean secure** : true if secure mode
- **char[] adminPassword** : password for admin user
- **NutsUpdateOptions options** : update options

#### ⚙ updateUser(name)
create a Update Create command.
 No user will be updated when simply calling this method.
 You must fill in command parameters then call \{\@link NutsUpdateUserCommand#run()\}.

```java
NutsUpdateUserCommand updateUser(String name)
```
**return**:NutsUpdateUserCommand
- **String name** : user name


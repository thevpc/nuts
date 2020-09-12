---
id: javadoc_Config
title: Config
sidebar_label: Config
---
                                                
```
     __        __           ___    ____  ____
  /\ \ \ _  __/ /______    /   |  / __ \/  _/
 /  \/ / / / / __/ ___/   / /| | / /_/ // /   
/ /\  / /_/ / /_(__  )   / ___ |/ ____// /       
\_\ \/\__,_/\__/____/   /_/  |_/_/   /___/  version 0.7.0
```

## ☕ NutsAddOptions
```java
public net.vpc.app.nuts.NutsAddOptions
```
 Generic Add options

 \@author vpc
 \@see NutsWorkspaceConfigManager#addSdk(net.vpc.app.nuts.NutsSdkLocation,
 net.vpc.app.nuts.NutsAddOptions)
 \@see NutsWorkspaceConfigManager#addCommandAlias(net.vpc.app.nuts.NutsCommandAliasConfig,
 net.vpc.app.nuts.NutsAddOptions)
 \@see NutsWorkspaceConfigManager#addCommandAliasFactory(net.vpc.app.nuts.NutsCommandAliasFactoryConfig,
 net.vpc.app.nuts.NutsAddOptions)
 \@since 0.5.4
 \@category Config

### 🪄 Constructors
#### 🪄 NutsAddOptions()


```java
NutsAddOptions()
```

### 🎛 Instance Properties
#### 📝🎛 session
update current session
```java
[read-write] NutsAddOptions public session
public NutsSession getSession()
public NutsAddOptions setSession(session)
```
### ⚙ Instance Methods
#### ⚙ equals(o)


```java
boolean equals(Object o)
```
**return**:boolean
- **Object o** : 

#### ⚙ hashCode()


```java
int hashCode()
```
**return**:int

#### ⚙ toString()


```java
String toString()
```
**return**:String

## ☕ NutsAddRepositoryOptions
```java
public net.vpc.app.nuts.NutsAddRepositoryOptions
```
 repository creation options
 \@author vpc
 \@since 0.5.4
 \@category Config

### 🪄 Constructors
#### 🪄 NutsAddRepositoryOptions()
default constructor

```java
NutsAddRepositoryOptions()
```

#### 🪄 NutsAddRepositoryOptions(other)
copy constructor

```java
NutsAddRepositoryOptions(NutsAddRepositoryOptions other)
```
- **NutsAddRepositoryOptions other** : other

### 🎛 Instance Properties
#### 📝🎛 config
repository config information
```java
[read-write] NutsAddRepositoryOptions public config
public NutsRepositoryConfig getConfig()
public NutsAddRepositoryOptions setConfig(value)
```
#### 📝🎛 create
always create. Throw exception if found
```java
[read-write] NutsAddRepositoryOptions public create
public boolean isCreate()
public NutsAddRepositoryOptions setCreate(value)
```
#### 📝🎛 deployOrder
repository deploy order
```java
[read-write] NutsAddRepositoryOptions public deployOrder
public int getDeployOrder()
public NutsAddRepositoryOptions setDeployOrder(value)
```
#### 📝🎛 enabled
enabled repository
```java
[read-write] NutsAddRepositoryOptions public enabled
public boolean isEnabled()
public NutsAddRepositoryOptions setEnabled(value)
```
#### 📝🎛 failSafe
fail safe repository. when fail safe, repository will be ignored
 if the location is not accessible
```java
[read-write] NutsAddRepositoryOptions public failSafe
public boolean isFailSafe()
public NutsAddRepositoryOptions setFailSafe(value)
```
#### 📝🎛 location
repository location
```java
[read-write] NutsAddRepositoryOptions public location
public String getLocation()
public NutsAddRepositoryOptions setLocation(value)
```
#### 📝🎛 name
repository name (should no include special space or characters)
```java
[read-write] NutsAddRepositoryOptions public name
public String getName()
public NutsAddRepositoryOptions setName(value)
```
#### 📝🎛 proxy
create a proxy for the created repository
```java
[read-write] NutsAddRepositoryOptions public proxy
public boolean isProxy()
public NutsAddRepositoryOptions setProxy(value)
```
#### 📝🎛 session
current session
```java
[read-write] NutsAddRepositoryOptions public session
public NutsSession getSession()
public NutsAddRepositoryOptions setSession(value)
```
#### 📝🎛 temporary
temporary repository
```java
[read-write] NutsAddRepositoryOptions public temporary
public boolean isTemporary()
public NutsAddRepositoryOptions setTemporary(value)
```
### ⚙ Instance Methods
#### ⚙ copy()
create a copy of this instance

```java
NutsAddRepositoryOptions copy()
```
**return**:NutsAddRepositoryOptions

#### ⚙ equals(o)


```java
boolean equals(Object o)
```
**return**:boolean
- **Object o** : 

#### ⚙ hashCode()


```java
int hashCode()
```
**return**:int

#### ⚙ toString()


```java
String toString()
```
**return**:String

## ☕ NutsCommandAliasConfig
```java
public net.vpc.app.nuts.NutsCommandAliasConfig
```
 Command Alias definition class Config
 \@author vpc
 \@since 0.5.4
 \@category Config

### 🪄 Constructors
#### 🪄 NutsCommandAliasConfig()


```java
NutsCommandAliasConfig()
```

### 🎛 Instance Properties
#### 📝🎛 command
alias command arguments
```java
[read-write] NutsCommandAliasConfig public command
public String[] getCommand()
public NutsCommandAliasConfig setCommand(value)
```
#### 📝🎛 executorOptions
alias command execution options
```java
[read-write] NutsCommandAliasConfig public executorOptions
public String[] getExecutorOptions()
public NutsCommandAliasConfig setExecutorOptions(value)
```
#### 📝🎛 factoryId
alias factory id
```java
[read-write] NutsCommandAliasConfig public factoryId
public String getFactoryId()
public NutsCommandAliasConfig setFactoryId(value)
```
#### 📝🎛 helpCommand
alias help command (command to display help)
```java
[read-write] NutsCommandAliasConfig public helpCommand
public String[] getHelpCommand()
public NutsCommandAliasConfig setHelpCommand(value)
```
#### 📝🎛 helpText
alias help text (meaningful if helpCommand is not defined)
```java
[read-write] NutsCommandAliasConfig public helpText
public String getHelpText()
public NutsCommandAliasConfig setHelpText(value)
```
#### 📝🎛 name
alias name
```java
[read-write] NutsCommandAliasConfig public name
public String getName()
public NutsCommandAliasConfig setName(value)
```
#### 📝🎛 owner
alias definition
```java
[read-write] NutsCommandAliasConfig public owner
public NutsId getOwner()
public NutsCommandAliasConfig setOwner(value)
```
### ⚙ Instance Methods
#### ⚙ equals(o)


```java
boolean equals(Object o)
```
**return**:boolean
- **Object o** : 

#### ⚙ hashCode()


```java
int hashCode()
```
**return**:int

#### ⚙ toString()


```java
String toString()
```
**return**:String

## ☕ NutsConfigItem
```java
public net.vpc.app.nuts.NutsConfigItem
```
 
 \@author vpc
 \@category Config

### 🪄 Constructors
#### 🪄 NutsConfigItem()


```java
NutsConfigItem()
```

### 🎛 Instance Properties
#### 📄🎛 configVersion

```java
[read-only] String public configVersion
public String getConfigVersion()
```
### ⚙ Instance Methods
#### ⚙ setConfigVersion(configVersion)


```java
void setConfigVersion(String configVersion)
```
- **String configVersion** : 

## ☕ NutsDefaultWorkspaceOptions
```java
public final net.vpc.app.nuts.NutsDefaultWorkspaceOptions
```
 Workspace creation/opening options class.

 \@since 0.5.4
 \@category Config

### 📢⚙ Static Methods
#### 📢⚙ createHomeLocationKey(storeLocationLayout, location)
creates a string key combining layout and location.
 le key has the form of a concatenated layout and location ids separated by \':\'
 where null layout is replaced by \'system\' keyword.
 used in \{\@link NutsWorkspaceOptions#getHomeLocations()\}.

```java
String createHomeLocationKey(NutsOsFamily storeLocationLayout, NutsStoreLocation location)
```
**return**:String
- **NutsOsFamily storeLocationLayout** : layout
- **NutsStoreLocation location** : location

### 🪄 Constructors
#### 🪄 NutsDefaultWorkspaceOptions()


```java
NutsDefaultWorkspaceOptions()
```

#### 🪄 NutsDefaultWorkspaceOptions(args)


```java
NutsDefaultWorkspaceOptions(String[] args)
```
- **String[] args** : 

#### 🪄 NutsDefaultWorkspaceOptions(other)


```java
NutsDefaultWorkspaceOptions(NutsWorkspaceOptions other)
```
- **NutsWorkspaceOptions other** : 

### 🎛 Instance Properties
#### 📝🎛 apiVersion
set apiVersion
```java
[read-write] NutsWorkspaceOptionsBuilder public apiVersion
public String getApiVersion()
public NutsWorkspaceOptionsBuilder setApiVersion(apiVersion)
```
#### 📝🎛 applicationArguments
set applicationArguments
```java
[read-write] NutsWorkspaceOptionsBuilder public applicationArguments
public String[] getApplicationArguments()
public NutsWorkspaceOptionsBuilder setApplicationArguments(applicationArguments)
```
#### 📝🎛 archetype
set archetype
```java
[read-write] NutsWorkspaceOptionsBuilder public archetype
public String getArchetype()
public NutsWorkspaceOptionsBuilder setArchetype(archetype)
```
#### 📄🎛 bootRepositories

```java
[read-only] String public bootRepositories
public String getBootRepositories()
```
#### 📄🎛 cached

```java
[read-only] boolean public cached
public boolean isCached()
```
#### 📝🎛 classLoaderSupplier
set provider
```java
[read-write] NutsWorkspaceOptionsBuilder public classLoaderSupplier
public Supplier getClassLoaderSupplier()
public NutsWorkspaceOptionsBuilder setClassLoaderSupplier(provider)
```
#### 📝🎛 confirm
set confirm
```java
[read-write] NutsWorkspaceOptionsBuilder public confirm
public NutsConfirmationMode getConfirm()
public NutsWorkspaceOptionsBuilder setConfirm(confirm)
```
#### 📝🎛 creationTime
set creationTime
```java
[read-write] NutsWorkspaceOptionsBuilder public creationTime
public long getCreationTime()
public NutsWorkspaceOptionsBuilder setCreationTime(creationTime)
```
#### 📝🎛 credentials
set password
```java
[read-write] NutsWorkspaceOptionsBuilder public credentials
public char[] getCredentials()
public NutsWorkspaceOptionsBuilder setCredentials(credentials)
```
#### 📝🎛 debug
set debug
```java
[read-write] NutsWorkspaceOptionsBuilder public debug
public boolean isDebug()
public NutsWorkspaceOptionsBuilder setDebug(debug)
```
#### 📝🎛 dry
set dry
```java
[read-write] NutsWorkspaceOptionsBuilder public dry
public boolean isDry()
public NutsWorkspaceOptionsBuilder setDry(dry)
```
#### 📝🎛 excludedExtensions
set excludedExtensions
```java
[read-write] NutsWorkspaceOptionsBuilder public excludedExtensions
public String[] getExcludedExtensions()
public NutsWorkspaceOptionsBuilder setExcludedExtensions(excludedExtensions)
```
#### 📝🎛 excludedRepositories
set excludedRepositories
```java
[read-write] NutsWorkspaceOptionsBuilder public excludedRepositories
public String[] getExcludedRepositories()
public NutsWorkspaceOptionsBuilder setExcludedRepositories(excludedRepositories)
```
#### 📝🎛 executionType
set executionType
```java
[read-write] NutsWorkspaceOptionsBuilder public executionType
public NutsExecutionType getExecutionType()
public NutsWorkspaceOptionsBuilder setExecutionType(executionType)
```
#### 📝🎛 executorOptions
set executorOptions
```java
[read-write] NutsWorkspaceOptionsBuilder public executorOptions
public String[] getExecutorOptions()
public NutsWorkspaceOptionsBuilder setExecutorOptions(executorOptions)
```
#### 📄🎛 executorService

```java
[read-only] ExecutorService public executorService
public ExecutorService getExecutorService()
```
#### 📄🎛 fetchStrategy

```java
[read-only] NutsFetchStrategy public fetchStrategy
public NutsFetchStrategy getFetchStrategy()
```
#### 📝🎛 global
set global
```java
[read-write] NutsWorkspaceOptionsBuilder public global
public boolean isGlobal()
public NutsWorkspaceOptionsBuilder setGlobal(global)
```
#### 📝🎛 gui
set gui
```java
[read-write] NutsWorkspaceOptionsBuilder public gui
public boolean isGui()
public NutsWorkspaceOptionsBuilder setGui(gui)
```
#### 📄🎛 homeLocation

```java
[read-only] String public homeLocation
public String getHomeLocation(layout, location)
```
#### 📄🎛 homeLocations

```java
[read-only] Map public homeLocations
public Map getHomeLocations()
```
#### 📄🎛 indexed

```java
[read-only] boolean public indexed
public boolean isIndexed()
```
#### 📝🎛 inherited
set inherited
```java
[read-write] NutsWorkspaceOptionsBuilder public inherited
public boolean isInherited()
public NutsWorkspaceOptionsBuilder setInherited(inherited)
```
#### 📄🎛 javaCommand

```java
[read-only] String public javaCommand
public String getJavaCommand()
```
#### 📝🎛 javaOptions
set javaOptions
```java
[read-write] NutsWorkspaceOptionsBuilder public javaOptions
public String getJavaOptions()
public NutsWorkspaceOptionsBuilder setJavaOptions(javaOptions)
```
#### 📝🎛 logConfig
set logConfig
```java
[read-write] NutsWorkspaceOptionsBuilder public logConfig
public NutsLogConfig getLogConfig()
public NutsWorkspaceOptionsBuilder setLogConfig(logConfig)
```
#### 📝🎛 name
set workspace name
```java
[read-write] NutsWorkspaceOptionsBuilder public name
public String getName()
public NutsWorkspaceOptionsBuilder setName(workspaceName)
```
#### 📝🎛 openMode
set openMode
```java
[read-write] NutsWorkspaceOptionsBuilder public openMode
public NutsWorkspaceOpenMode getOpenMode()
public NutsWorkspaceOptionsBuilder setOpenMode(openMode)
```
#### 📝🎛 outputFormat
set outputFormat
```java
[read-write] NutsWorkspaceOptionsBuilder public outputFormat
public NutsOutputFormat getOutputFormat()
public NutsWorkspaceOptionsBuilder setOutputFormat(outputFormat)
```
#### 📝🎛 outputFormatOptions
set output format options
```java
[read-write] NutsWorkspaceOptionsBuilder public outputFormatOptions
public String[] getOutputFormatOptions()
public NutsWorkspaceOptionsBuilder setOutputFormatOptions(options)
```
#### 📄🎛 progressOptions

```java
[read-only] String public progressOptions
public String getProgressOptions()
```
#### 📝🎛 readOnly
set readOnly
```java
[read-write] NutsWorkspaceOptionsBuilder public readOnly
public boolean isReadOnly()
public NutsWorkspaceOptionsBuilder setReadOnly(readOnly)
```
#### 📝🎛 recover
set recover
```java
[read-write] NutsWorkspaceOptionsBuilder public recover
public boolean isRecover()
public NutsWorkspaceOptionsBuilder setRecover(recover)
```
#### 📝🎛 repositoryStoreLocationStrategy
set repositoryStoreLocationStrategy
```java
[read-write] NutsWorkspaceOptionsBuilder public repositoryStoreLocationStrategy
public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy()
public NutsWorkspaceOptionsBuilder setRepositoryStoreLocationStrategy(repositoryStoreLocationStrategy)
```
#### 📝🎛 reset
set reset
```java
[read-write] NutsWorkspaceOptionsBuilder public reset
public boolean isReset()
public NutsWorkspaceOptionsBuilder setReset(reset)
```
#### 📝🎛 runtimeId
set runtimeId
```java
[read-write] NutsWorkspaceOptionsBuilder public runtimeId
public String getRuntimeId()
public NutsWorkspaceOptionsBuilder setRuntimeId(runtimeId)
```
#### 📝🎛 skipBoot
set skipWelcome
```java
[read-write] NutsWorkspaceOptionsBuilder public skipBoot
public boolean isSkipBoot()
public NutsWorkspaceOptionsBuilder setSkipBoot(skipBoot)
```
#### 📝🎛 skipCompanions
set skipInstallCompanions
```java
[read-write] NutsWorkspaceOptionsBuilder public skipCompanions
public boolean isSkipCompanions()
public NutsWorkspaceOptionsBuilder setSkipCompanions(skipInstallCompanions)
```
#### 📝🎛 skipWelcome
set skipWelcome
```java
[read-write] NutsWorkspaceOptionsBuilder public skipWelcome
public boolean isSkipWelcome()
public NutsWorkspaceOptionsBuilder setSkipWelcome(skipWelcome)
```
#### 📄🎛 stderr

```java
[read-only] PrintStream public stderr
public PrintStream getStderr()
```
#### 📄🎛 stdin

```java
[read-only] InputStream public stdin
public InputStream getStdin()
```
#### 📄🎛 stdout

```java
[read-only] PrintStream public stdout
public PrintStream getStdout()
```
#### 📄🎛 storeLocation

```java
[read-only] String public storeLocation
public String getStoreLocation(folder)
```
#### 📝🎛 storeLocationLayout
set storeLocationLayout
```java
[read-write] NutsWorkspaceOptionsBuilder public storeLocationLayout
public NutsOsFamily getStoreLocationLayout()
public NutsWorkspaceOptionsBuilder setStoreLocationLayout(storeLocationLayout)
```
#### 📝🎛 storeLocationStrategy
set storeLocationStrategy
```java
[read-write] NutsWorkspaceOptionsBuilder public storeLocationStrategy
public NutsStoreLocationStrategy getStoreLocationStrategy()
public NutsWorkspaceOptionsBuilder setStoreLocationStrategy(storeLocationStrategy)
```
#### 📄🎛 storeLocations

```java
[read-only] Map public storeLocations
public Map getStoreLocations()
```
#### 📝🎛 terminalMode
set terminalMode
```java
[read-write] NutsWorkspaceOptionsBuilder public terminalMode
public NutsTerminalMode getTerminalMode()
public NutsWorkspaceOptionsBuilder setTerminalMode(terminalMode)
```
#### 📝🎛 trace
set trace
```java
[read-write] NutsWorkspaceOptionsBuilder public trace
public boolean isTrace()
public NutsWorkspaceOptionsBuilder setTrace(trace)
```
#### 📝🎛 transientRepositories
set transientRepositories
```java
[read-write] NutsWorkspaceOptionsBuilder public transientRepositories
public String[] getTransientRepositories()
public NutsWorkspaceOptionsBuilder setTransientRepositories(transientRepositories)
```
#### 📄🎛 transitive

```java
[read-only] boolean public transitive
public boolean isTransitive()
```
#### 📄🎛 userName

```java
[read-only] String public userName
public String getUserName()
```
#### ✏🎛 username
set login
```java
[write-only] NutsWorkspaceOptionsBuilder public username
public NutsWorkspaceOptionsBuilder setUsername(username)
```
#### 📝🎛 workspace
set workspace
```java
[read-write] NutsWorkspaceOptionsBuilder public workspace
public String getWorkspace()
public NutsWorkspaceOptionsBuilder setWorkspace(workspace)
```
### ⚙ Instance Methods
#### ⚙ addOutputFormatOptions(options)
add output format options

```java
NutsWorkspaceOptionsBuilder addOutputFormatOptions(String[] options)
```
**return**:NutsWorkspaceOptionsBuilder
- **String[] options** : new value

#### ⚙ copy()


```java
NutsWorkspaceOptionsBuilder copy()
```
**return**:NutsWorkspaceOptionsBuilder

#### ⚙ format()


```java
NutsWorkspaceOptionsFormat format()
```
**return**:NutsWorkspaceOptionsFormat

#### ⚙ parse(args)
parse arguments

```java
NutsWorkspaceOptionsBuilder parse(String[] args)
```
**return**:NutsWorkspaceOptionsBuilder
- **String[] args** : arguments

#### ⚙ setBootRepositories(bootRepositories)


```java
NutsWorkspaceOptionsBuilder setBootRepositories(String bootRepositories)
```
**return**:NutsWorkspaceOptionsBuilder
- **String bootRepositories** : 

#### ⚙ setCached(cached)


```java
NutsWorkspaceOptionsBuilder setCached(boolean cached)
```
**return**:NutsWorkspaceOptionsBuilder
- **boolean cached** : 

#### ⚙ setExecutorService(executorService)


```java
NutsWorkspaceOptionsBuilder setExecutorService(ExecutorService executorService)
```
**return**:NutsWorkspaceOptionsBuilder
- **ExecutorService executorService** : 

#### ⚙ setFetchStrategy(fetchStrategy)


```java
NutsWorkspaceOptionsBuilder setFetchStrategy(NutsFetchStrategy fetchStrategy)
```
**return**:NutsWorkspaceOptionsBuilder
- **NutsFetchStrategy fetchStrategy** : 

#### ⚙ setHomeLocation(layout, location, value)
set home location

```java
NutsWorkspaceOptionsBuilder setHomeLocation(NutsOsFamily layout, NutsStoreLocation location, String value)
```
**return**:NutsWorkspaceOptionsBuilder
- **NutsOsFamily layout** : layout
- **NutsStoreLocation location** : location
- **String value** : new value

#### ⚙ setHomeLocations(homeLocations)


```java
NutsWorkspaceOptionsBuilder setHomeLocations(Map homeLocations)
```
**return**:NutsWorkspaceOptionsBuilder
- **Map homeLocations** : 

#### ⚙ setIndexed(indexed)


```java
NutsWorkspaceOptionsBuilder setIndexed(boolean indexed)
```
**return**:NutsWorkspaceOptionsBuilder
- **boolean indexed** : 

#### ⚙ setJavaCommand(javaCommand)


```java
NutsWorkspaceOptionsBuilder setJavaCommand(String javaCommand)
```
**return**:NutsWorkspaceOptionsBuilder
- **String javaCommand** : 

#### ⚙ setProgressOptions(progressOptions)


```java
NutsWorkspaceOptionsBuilder setProgressOptions(String progressOptions)
```
**return**:NutsWorkspaceOptionsBuilder
- **String progressOptions** : 

#### ⚙ setStderr(stderr)


```java
NutsWorkspaceOptionsBuilder setStderr(PrintStream stderr)
```
**return**:NutsWorkspaceOptionsBuilder
- **PrintStream stderr** : 

#### ⚙ setStdin(stdin)


```java
NutsWorkspaceOptionsBuilder setStdin(InputStream stdin)
```
**return**:NutsWorkspaceOptionsBuilder
- **InputStream stdin** : 

#### ⚙ setStdout(stdout)


```java
NutsWorkspaceOptionsBuilder setStdout(PrintStream stdout)
```
**return**:NutsWorkspaceOptionsBuilder
- **PrintStream stdout** : 

#### ⚙ setStoreLocation(location, value)
set store location

```java
NutsWorkspaceOptionsBuilder setStoreLocation(NutsStoreLocation location, String value)
```
**return**:NutsWorkspaceOptionsBuilder
- **NutsStoreLocation location** : location
- **String value** : new value

#### ⚙ setStoreLocations(storeLocations)


```java
NutsWorkspaceOptionsBuilder setStoreLocations(Map storeLocations)
```
**return**:NutsWorkspaceOptionsBuilder
- **Map storeLocations** : 

#### ⚙ setTransitive(transitive)


```java
NutsWorkspaceOptionsBuilder setTransitive(boolean transitive)
```
**return**:NutsWorkspaceOptionsBuilder
- **boolean transitive** : 

#### ⚙ toString()


```java
String toString()
```
**return**:String

## ☕ NutsExtensionInformation
```java
public interface net.vpc.app.nuts.NutsExtensionInformation
```
 Extension information
 \@author vpc
 \@since 0.5.4
 \@category Config

### 🎛 Instance Properties
#### 📄🎛 author
extension main author(s)
```java
[read-only] String public author
public String getAuthor()
```
#### 📄🎛 category
extension category
```java
[read-only] String public category
public String getCategory()
```
#### 📄🎛 description
extension long description
```java
[read-only] String public description
public String getDescription()
```
#### 📄🎛 id
extension id
```java
[read-only] NutsId public id
public NutsId getId()
```
#### 📄🎛 name
extension user name
```java
[read-only] String public name
public String getName()
```
#### 📄🎛 source
extension source
```java
[read-only] String public source
public String getSource()
```
## ☕ NutsRepositoryConfig
```java
public net.vpc.app.nuts.NutsRepositoryConfig
```

 \@author vpc
 \@since 0.5.4
 \@category Config

### 🪄 Constructors
#### 🪄 NutsRepositoryConfig()


```java
NutsRepositoryConfig()
```

### 🎛 Instance Properties
#### 📄🎛 authenticationAgent

```java
[read-only] String public authenticationAgent
public String getAuthenticationAgent()
```
#### 📄🎛 env

```java
[read-only] Map public env
public Map getEnv()
```
#### 📄🎛 groups

```java
[read-only] String public groups
public String getGroups()
```
#### 📄🎛 indexEnabled

```java
[read-only] boolean public indexEnabled
public boolean isIndexEnabled()
```
#### 📄🎛 location

```java
[read-only] String public location
public String getLocation()
```
#### 📄🎛 mirrors

```java
[read-only] List public mirrors
public List getMirrors()
```
#### 📄🎛 name

```java
[read-only] String public name
public String getName()
```
#### 📄🎛 storeLocationStrategy

```java
[read-only] NutsStoreLocationStrategy public storeLocationStrategy
public NutsStoreLocationStrategy getStoreLocationStrategy()
```
#### 📄🎛 storeLocations

```java
[read-only] Map public storeLocations
public Map getStoreLocations()
```
#### 📄🎛 type

```java
[read-only] String public type
public String getType()
```
#### 📄🎛 users

```java
[read-only] List public users
public List getUsers()
```
#### 📄🎛 uuid

```java
[read-only] String public uuid
public String getUuid()
```
### ⚙ Instance Methods
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

#### ⚙ setAuthenticationAgent(authenticationAgent)


```java
NutsRepositoryConfig setAuthenticationAgent(String authenticationAgent)
```
**return**:NutsRepositoryConfig
- **String authenticationAgent** : 

#### ⚙ setEnv(env)


```java
NutsRepositoryConfig setEnv(Map env)
```
**return**:NutsRepositoryConfig
- **Map env** : 

#### ⚙ setGroups(groups)


```java
NutsRepositoryConfig setGroups(String groups)
```
**return**:NutsRepositoryConfig
- **String groups** : 

#### ⚙ setIndexEnabled(indexEnabled)


```java
NutsRepositoryConfig setIndexEnabled(boolean indexEnabled)
```
**return**:NutsRepositoryConfig
- **boolean indexEnabled** : 

#### ⚙ setLocation(location)


```java
NutsRepositoryConfig setLocation(String location)
```
**return**:NutsRepositoryConfig
- **String location** : 

#### ⚙ setMirrors(mirrors)


```java
NutsRepositoryConfig setMirrors(List mirrors)
```
**return**:NutsRepositoryConfig
- **List mirrors** : 

#### ⚙ setName(name)


```java
NutsRepositoryConfig setName(String name)
```
**return**:NutsRepositoryConfig
- **String name** : 

#### ⚙ setStoreLocationStrategy(storeLocationStrategy)


```java
NutsRepositoryConfig setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy)
```
**return**:NutsRepositoryConfig
- **NutsStoreLocationStrategy storeLocationStrategy** : 

#### ⚙ setStoreLocations(storeLocations)


```java
NutsRepositoryConfig setStoreLocations(Map storeLocations)
```
**return**:NutsRepositoryConfig
- **Map storeLocations** : 

#### ⚙ setType(type)


```java
NutsRepositoryConfig setType(String type)
```
**return**:NutsRepositoryConfig
- **String type** : 

#### ⚙ setUsers(users)


```java
NutsRepositoryConfig setUsers(List users)
```
**return**:NutsRepositoryConfig
- **List users** : 

#### ⚙ setUuid(uuid)


```java
NutsRepositoryConfig setUuid(String uuid)
```
**return**:NutsRepositoryConfig
- **String uuid** : 

#### ⚙ toString()


```java
String toString()
```
**return**:String

## ☕ NutsRepositoryConfigManager
```java
public interface net.vpc.app.nuts.NutsRepositoryConfigManager
```
 \@author vpc
 \@since 0.5.4
 \@category Config

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
#### 📄🎛 env

```java
[read-only] String public env
public String getEnv(key, defaultValue, inherit)
```
#### 📄🎛 globalName
global name is independent from workspace
```java
[read-only] String public globalName
public String getGlobalName()
```
#### 📄🎛 groups

```java
[read-only] String public groups
public String getGroups()
```
#### 📄🎛 indexEnabled

```java
[read-only] boolean public indexEnabled
public boolean isIndexEnabled()
```
#### 📄🎛 indexSubscribed

```java
[read-only] boolean public indexSubscribed
public boolean isIndexSubscribed()
```
#### 📄🎛 mirrors

```java
[read-only] NutsRepository[] public mirrors
public NutsRepository[] getMirrors(session)
```
#### 📄🎛 name
name is the name attributed by the containing workspace. It is defined in
 NutsRepositoryRef
```java
[read-only] String public name
public String getName()
```
#### 📄🎛 speed

```java
[read-only] int public speed
public int getSpeed(session)
```
#### 📄🎛 storeLocation

```java
[read-only] Path public storeLocation
public Path getStoreLocation(folderType)
```
#### 📄🎛 storeLocationStrategy

```java
[read-only] NutsStoreLocationStrategy public storeLocationStrategy
public NutsStoreLocationStrategy getStoreLocationStrategy()
```
#### 📄🎛 supportedMirroring

```java
[read-only] boolean public supportedMirroring
public boolean isSupportedMirroring()
```
#### 📄🎛 temporary

```java
[read-only] boolean public temporary
public boolean isTemporary()
```
#### 📄🎛 type

```java
[read-only] String public type
public String getType()
```
#### 📄🎛 uuid

```java
[read-only] String public uuid
public String getUuid()
```
### ⚙ Instance Methods
#### ⚙ addMirror(definition)
add new repository

```java
NutsRepository addMirror(NutsRepositoryDefinition definition)
```
**return**:NutsRepository
- **NutsRepositoryDefinition definition** : repository definition

#### ⚙ addMirror(options)
add new repository

```java
NutsRepository addMirror(NutsAddRepositoryOptions options)
```
**return**:NutsRepository
- **NutsAddRepositoryOptions options** : repository definition

#### ⚙ findMirror(repositoryIdOrName, session)
search for (or return null) a repository with the given repository name or id.

```java
NutsRepository findMirror(String repositoryIdOrName, NutsSession session)
```
**return**:NutsRepository
- **String repositoryIdOrName** : repository name or id
- **NutsSession session** : session

#### ⚙ findMirrorById(repositoryNameOrId, session)


```java
NutsRepository findMirrorById(String repositoryNameOrId, NutsSession session)
```
**return**:NutsRepository
- **String repositoryNameOrId** : 
- **NutsSession session** : 

#### ⚙ findMirrorByName(repositoryNameOrId, session)


```java
NutsRepository findMirrorByName(String repositoryNameOrId, NutsSession session)
```
**return**:NutsRepository
- **String repositoryNameOrId** : 
- **NutsSession session** : 

#### ⚙ getLocation(expand)
return repository configured location as string

```java
String getLocation(boolean expand)
```
**return**:String
- **boolean expand** : when true, location will be expanded (~ and $ params will
               be expanded)

#### ⚙ getMirror(repositoryIdOrName, session)
search for (or throw error) a repository with the given repository name or id.

```java
NutsRepository getMirror(String repositoryIdOrName, NutsSession session)
```
**return**:NutsRepository
- **String repositoryIdOrName** : repository name or id
- **NutsSession session** : session

#### ⚙ name()


```java
String name()
```
**return**:String

#### ⚙ removeMirror(repositoryId, options)


```java
NutsRepositoryConfigManager removeMirror(String repositoryId, NutsRemoveOptions options)
```
**return**:NutsRepositoryConfigManager
- **String repositoryId** : repository id pr id
- **NutsRemoveOptions options** : remove options

#### ⚙ save(session)


```java
void save(NutsSession session)
```
- **NutsSession session** : 

#### ⚙ save(force, session)


```java
boolean save(boolean force, NutsSession session)
```
**return**:boolean
- **boolean force** : 
- **NutsSession session** : 

#### ⚙ setEnabled(enabled, options)


```java
NutsRepositoryConfigManager setEnabled(boolean enabled, NutsUpdateOptions options)
```
**return**:NutsRepositoryConfigManager
- **boolean enabled** : 
- **NutsUpdateOptions options** : 

#### ⚙ setEnv(property, value, options)


```java
void setEnv(String property, String value, NutsUpdateOptions options)
```
- **String property** : 
- **String value** : 
- **NutsUpdateOptions options** : 

#### ⚙ setIndexEnabled(enabled, options)


```java
NutsRepositoryConfigManager setIndexEnabled(boolean enabled, NutsUpdateOptions options)
```
**return**:NutsRepositoryConfigManager
- **boolean enabled** : 
- **NutsUpdateOptions options** : 

#### ⚙ setMirrorEnabled(repoName, enabled, options)


```java
NutsRepositoryConfigManager setMirrorEnabled(String repoName, boolean enabled, NutsUpdateOptions options)
```
**return**:NutsRepositoryConfigManager
- **String repoName** : 
- **boolean enabled** : 
- **NutsUpdateOptions options** : 

#### ⚙ setTemporary(enabled, options)


```java
NutsRepositoryConfigManager setTemporary(boolean enabled, NutsUpdateOptions options)
```
**return**:NutsRepositoryConfigManager
- **boolean enabled** : 
- **NutsUpdateOptions options** : 

#### ⚙ subscribeIndex(session)


```java
NutsRepositoryConfigManager subscribeIndex(NutsSession session)
```
**return**:NutsRepositoryConfigManager
- **NutsSession session** : 

#### ⚙ unsubscribeIndex(session)


```java
NutsRepositoryConfigManager unsubscribeIndex(NutsSession session)
```
**return**:NutsRepositoryConfigManager
- **NutsSession session** : 

#### ⚙ uuid()


```java
String uuid()
```
**return**:String

## ☕ NutsRepositoryDefinition
```java
public net.vpc.app.nuts.NutsRepositoryDefinition
```

 \@author vpc
 \@since 0.5.4
 \@category Config

### 📢❄ Constant Fields
#### 📢❄ ORDER_SYSTEM_LOCAL
```java
public static final int ORDER_SYSTEM_LOCAL = 2000
```
#### 📢❄ ORDER_USER_LOCAL
```java
public static final int ORDER_USER_LOCAL = 1000
```
#### 📢❄ ORDER_USER_REMOTE
```java
public static final int ORDER_USER_REMOTE = 10000
```
### 🪄 Constructors
#### 🪄 NutsRepositoryDefinition()


```java
NutsRepositoryDefinition()
```

#### 🪄 NutsRepositoryDefinition(o)


```java
NutsRepositoryDefinition(NutsRepositoryDefinition o)
```
- **NutsRepositoryDefinition o** : 

### 🎛 Instance Properties
#### 📄🎛 create

```java
[read-only] boolean public create
public boolean isCreate()
```
#### 📄🎛 deployOrder

```java
[read-only] int public deployOrder
public int getDeployOrder()
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
#### 📄🎛 order

```java
[read-only] int public order
public int getOrder()
```
#### 📄🎛 proxy

```java
[read-only] boolean public proxy
public boolean isProxy()
```
#### 📄🎛 reference

```java
[read-only] boolean public reference
public boolean isReference()
```
#### 📄🎛 session

```java
[read-only] NutsSession public session
public NutsSession getSession()
```
#### 📄🎛 storeLocationStrategy

```java
[read-only] NutsStoreLocationStrategy public storeLocationStrategy
public NutsStoreLocationStrategy getStoreLocationStrategy()
```
#### 📄🎛 temporary

```java
[read-only] boolean public temporary
public boolean isTemporary()
```
#### 📄🎛 type

```java
[read-only] String public type
public String getType()
```
### ⚙ Instance Methods
#### ⚙ copy()


```java
NutsRepositoryDefinition copy()
```
**return**:NutsRepositoryDefinition

#### ⚙ setCreate(create)


```java
NutsRepositoryDefinition setCreate(boolean create)
```
**return**:NutsRepositoryDefinition
- **boolean create** : 

#### ⚙ setDeployOrder(deployPriority)


```java
NutsRepositoryDefinition setDeployOrder(int deployPriority)
```
**return**:NutsRepositoryDefinition
- **int deployPriority** : 

#### ⚙ setFailSafe(failSafe)


```java
NutsRepositoryDefinition setFailSafe(boolean failSafe)
```
**return**:NutsRepositoryDefinition
- **boolean failSafe** : 

#### ⚙ setLocation(location)


```java
NutsRepositoryDefinition setLocation(String location)
```
**return**:NutsRepositoryDefinition
- **String location** : 

#### ⚙ setName(name)


```java
NutsRepositoryDefinition setName(String name)
```
**return**:NutsRepositoryDefinition
- **String name** : 

#### ⚙ setOrder(order)


```java
NutsRepositoryDefinition setOrder(int order)
```
**return**:NutsRepositoryDefinition
- **int order** : 

#### ⚙ setProxy(proxy)


```java
NutsRepositoryDefinition setProxy(boolean proxy)
```
**return**:NutsRepositoryDefinition
- **boolean proxy** : 

#### ⚙ setReference(reference)


```java
NutsRepositoryDefinition setReference(boolean reference)
```
**return**:NutsRepositoryDefinition
- **boolean reference** : 

#### ⚙ setSession(session)


```java
NutsRepositoryDefinition setSession(NutsSession session)
```
**return**:NutsRepositoryDefinition
- **NutsSession session** : 

#### ⚙ setStoreLocationStrategy(storeLocationStrategy)


```java
NutsRepositoryDefinition setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy)
```
**return**:NutsRepositoryDefinition
- **NutsStoreLocationStrategy storeLocationStrategy** : 

#### ⚙ setTemporary(temporary)


```java
void setTemporary(boolean temporary)
```
- **boolean temporary** : 

#### ⚙ setType(type)


```java
NutsRepositoryDefinition setType(String type)
```
**return**:NutsRepositoryDefinition
- **String type** : 

#### ⚙ toString()


```java
String toString()
```
**return**:String

## ☕ NutsSdkLocation
```java
public net.vpc.app.nuts.NutsSdkLocation
```
 SDK location
 \@author vpc
 \@since 0.5.4
 \@category Config

### 📢❄ Constant Fields
#### 📢❄ serialVersionUID
```java
public static final long serialVersionUID = 2L
```
### 🪄 Constructors
#### 🪄 NutsSdkLocation(id, product, name, path, version, packaging)
default constructor

```java
NutsSdkLocation(NutsId id, String product, String name, String path, String version, String packaging)
```
- **NutsId id** : id
- **String product** : sdk product. In java this is Oracle JDK or OpenJDK.
- **String name** : sdk name
- **String path** : sdk path
- **String version** : sdk version
- **String packaging** : sdk packaging. for Java SDK this is room to set JRE or JDK.

### 🎛 Instance Properties
#### 📄🎛 id

```java
[read-only] NutsId public id
public NutsId getId()
```
#### 📄🎛 name
sdk name
```java
[read-only] String public name
public String getName()
```
#### 📄🎛 packaging
sdk packaging. for Java SDK this
 is room to set JRE or JDK.
```java
[read-only] String public packaging
public String getPackaging()
```
#### 📄🎛 path
sdk path
```java
[read-only] String public path
public String getPath()
```
#### 📄🎛 product
sdk product. In java this is
 Oracle JDK or OpenJDK.
```java
[read-only] String public product
public String getProduct()
```
#### 📄🎛 version
sdk version
```java
[read-only] String public version
public String getVersion()
```
### ⚙ Instance Methods
#### ⚙ equals(o)


```java
boolean equals(Object o)
```
**return**:boolean
- **Object o** : 

#### ⚙ hashCode()


```java
int hashCode()
```
**return**:int

#### ⚙ toString()


```java
String toString()
```
**return**:String

## ☕ NutsUpdateStatisticsCommand
```java
public interface net.vpc.app.nuts.NutsUpdateStatisticsCommand
```

 \@author vpc
 \@since 0.5.5
 \@category Config

### 🎛 Instance Properties
#### ✏🎛 session
update session
```java
[write-only] NutsUpdateStatisticsCommand public session
public NutsUpdateStatisticsCommand setSession(session)
```
### ⚙ Instance Methods
#### ⚙ add(repoOrPath)
add path if repoOrPath is a path (contains path separator or is \'.\' or \'..\')
 if not add repo name or id

```java
void add(String repoOrPath)
```
- **String repoOrPath** : repo uuid, name or path

#### ⚙ addPath(s)


```java
NutsUpdateStatisticsCommand addPath(Path s)
```
**return**:NutsUpdateStatisticsCommand
- **Path s** : 

#### ⚙ addPaths(all)


```java
NutsUpdateStatisticsCommand addPaths(Path[] all)
```
**return**:NutsUpdateStatisticsCommand
- **Path[] all** : 

#### ⚙ addPaths(all)


```java
NutsUpdateStatisticsCommand addPaths(Collection all)
```
**return**:NutsUpdateStatisticsCommand
- **Collection all** : 

#### ⚙ addRepo(s)


```java
NutsUpdateStatisticsCommand addRepo(String s)
```
**return**:NutsUpdateStatisticsCommand
- **String s** : 

#### ⚙ addRepos(all)


```java
NutsUpdateStatisticsCommand addRepos(String[] all)
```
**return**:NutsUpdateStatisticsCommand
- **String[] all** : 

#### ⚙ addRepos(all)


```java
NutsUpdateStatisticsCommand addRepos(Collection all)
```
**return**:NutsUpdateStatisticsCommand
- **Collection all** : 

#### ⚙ clearPaths()


```java
NutsUpdateStatisticsCommand clearPaths()
```
**return**:NutsUpdateStatisticsCommand

#### ⚙ clearRepos()


```java
NutsUpdateStatisticsCommand clearRepos()
```
**return**:NutsUpdateStatisticsCommand

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsUpdateStatisticsCommand configure(boolean skipUnsupported, String[] args)
```
**return**:NutsUpdateStatisticsCommand
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ copySession()
copy session

```java
NutsUpdateStatisticsCommand copySession()
```
**return**:NutsUpdateStatisticsCommand

#### ⚙ path(s)


```java
NutsUpdateStatisticsCommand path(Path s)
```
**return**:NutsUpdateStatisticsCommand
- **Path s** : 

#### ⚙ removePath(s)


```java
NutsUpdateStatisticsCommand removePath(Path s)
```
**return**:NutsUpdateStatisticsCommand
- **Path s** : 

#### ⚙ removeRepo(s)


```java
NutsUpdateStatisticsCommand removeRepo(String s)
```
**return**:NutsUpdateStatisticsCommand
- **String s** : 

#### ⚙ repo(s)


```java
NutsUpdateStatisticsCommand repo(String s)
```
**return**:NutsUpdateStatisticsCommand
- **String s** : 

#### ⚙ run()
execute the command and return this instance

```java
NutsUpdateStatisticsCommand run()
```
**return**:NutsUpdateStatisticsCommand

## ☕ NutsUser
```java
public interface net.vpc.app.nuts.NutsUser
```
 Effective (including inherited) user information
 \@author vpc
 \@since 0.5.4
 \@category Config

### 🎛 Instance Properties
#### 📄🎛 groups
user groups
```java
[read-only] String[] public groups
public String[] getGroups()
```
#### 📄🎛 inheritedPermissions
user inherited allowed permissions
```java
[read-only] String[] public inheritedPermissions
public String[] getInheritedPermissions()
```
#### 📄🎛 permissions
user allowed permissions
```java
[read-only] String[] public permissions
public String[] getPermissions()
```
#### 📄🎛 remoteIdentity
return remote identity if applicable
```java
[read-only] String public remoteIdentity
public String getRemoteIdentity()
```
#### 📄🎛 user
return user name
```java
[read-only] String public user
public String getUser()
```
### ⚙ Instance Methods
#### ⚙ hasCredentials()
true if the use has some credentials

```java
boolean hasCredentials()
```
**return**:boolean

## ☕ NutsUserConfig
```java
public final net.vpc.app.nuts.NutsUserConfig
```

 \@author vpc
 \@since 0.5.4
 \@category Config

### 🪄 Constructors
#### 🪄 NutsUserConfig()


```java
NutsUserConfig()
```

#### 🪄 NutsUserConfig(other)


```java
NutsUserConfig(NutsUserConfig other)
```
- **NutsUserConfig other** : 

#### 🪄 NutsUserConfig(user, credentials, groups, permissions)


```java
NutsUserConfig(String user, String credentials, String[] groups, String[] permissions)
```
- **String user** : 
- **String credentials** : 
- **String[] groups** : 
- **String[] permissions** : 

### 🎛 Instance Properties
#### 📄🎛 credentials

```java
[read-only] String public credentials
public String getCredentials()
```
#### 📄🎛 groups

```java
[read-only] String[] public groups
public String[] getGroups()
```
#### 📄🎛 permissions

```java
[read-only] String[] public permissions
public String[] getPermissions()
```
#### 📄🎛 remoteCredentials

```java
[read-only] String public remoteCredentials
public String getRemoteCredentials()
```
#### 📄🎛 remoteIdentity

```java
[read-only] String public remoteIdentity
public String getRemoteIdentity()
```
#### 📄🎛 user

```java
[read-only] String public user
public String getUser()
```
### ⚙ Instance Methods
#### ⚙ equals(o)


```java
boolean equals(Object o)
```
**return**:boolean
- **Object o** : 

#### ⚙ hashCode()


```java
int hashCode()
```
**return**:int

#### ⚙ setCredentials(credentials)


```java
void setCredentials(String credentials)
```
- **String credentials** : 

#### ⚙ setGroups(groups)


```java
void setGroups(String[] groups)
```
- **String[] groups** : 

#### ⚙ setPermissions(permissions)


```java
void setPermissions(String[] permissions)
```
- **String[] permissions** : 

#### ⚙ setRemoteCredentials(remoteCredentials)


```java
void setRemoteCredentials(String remoteCredentials)
```
- **String remoteCredentials** : 

#### ⚙ setRemoteIdentity(remoteIdentity)


```java
void setRemoteIdentity(String remoteIdentity)
```
- **String remoteIdentity** : 

#### ⚙ setUser(user)


```java
void setUser(String user)
```
- **String user** : 

#### ⚙ toString()


```java
String toString()
```
**return**:String

## ☕ NutsWorkspaceCommandAlias
```java
public interface net.vpc.app.nuts.NutsWorkspaceCommandAlias
```

 \@author vpc
 \@since 0.5.4
 \@category Config

### 🎛 Instance Properties
#### 📄🎛 command

```java
[read-only] String[] public command
public String[] getCommand()
```
#### 📄🎛 executorOptions

```java
[read-only] String[] public executorOptions
public String[] getExecutorOptions()
```
#### 📄🎛 factoryId

```java
[read-only] String public factoryId
public String getFactoryId()
```
#### 📄🎛 helpText

```java
[read-only] String public helpText
public String getHelpText()
```
#### 📄🎛 name

```java
[read-only] String public name
public String getName()
```
#### 📄🎛 owner

```java
[read-only] NutsId public owner
public NutsId getOwner()
```
### ⚙ Instance Methods
#### ⚙ dryExec(args, options, session)


```java
void dryExec(String[] args, NutsCommandExecOptions options, NutsSession session)
```
- **String[] args** : 
- **NutsCommandExecOptions options** : 
- **NutsSession session** : 

#### ⚙ exec(args, options, session)


```java
void exec(String[] args, NutsCommandExecOptions options, NutsSession session)
```
- **String[] args** : 
- **NutsCommandExecOptions options** : 
- **NutsSession session** : 

## ☕ NutsWorkspaceCommandFactory
```java
public interface net.vpc.app.nuts.NutsWorkspaceCommandFactory
```

 \@author vpc
 \@since 0.5.4
 \@category Config

### 🎛 Instance Properties
#### 📄🎛 factoryId

```java
[read-only] String public factoryId
public String getFactoryId()
```
#### 📄🎛 priority

```java
[read-only] int public priority
public int getPriority()
```
### ⚙ Instance Methods
#### ⚙ configure(config)


```java
void configure(NutsCommandAliasFactoryConfig config)
```
- **NutsCommandAliasFactoryConfig config** : 

#### ⚙ findCommand(name, workspace)


```java
NutsCommandAliasConfig findCommand(String name, NutsWorkspace workspace)
```
**return**:NutsCommandAliasConfig
- **String name** : 
- **NutsWorkspace workspace** : 

#### ⚙ findCommands(workspace)


```java
List findCommands(NutsWorkspace workspace)
```
**return**:List
- **NutsWorkspace workspace** : 

## ☕ NutsWorkspaceConfigManager
```java
public interface net.vpc.app.nuts.NutsWorkspaceConfigManager
```
 \@author vpc
 \@since 0.5.4
 \@category Config

### 🎛 Instance Properties
#### 📄🎛 apiId

```java
[read-only] NutsId public apiId
public NutsId getApiId()
```
#### 📄🎛 apiVersion

```java
[read-only] String public apiVersion
public String getApiVersion()
```
#### 📄🎛 arch

```java
[read-only] NutsId public arch
public NutsId getArch()
```
#### 📄🎛 availableArchetypes

```java
[read-only] Set public availableArchetypes
public Set getAvailableArchetypes(session)
```
#### 📄🎛 bootClassLoader

```java
[read-only] ClassLoader public bootClassLoader
public ClassLoader getBootClassLoader()
```
#### 📄🎛 bootClassWorldURLs

```java
[read-only] URL[] public bootClassWorldURLs
public URL[] getBootClassWorldURLs()
```
#### 📄🎛 bootRepositories

```java
[read-only] String public bootRepositories
public String getBootRepositories()
```
#### 📄🎛 commandFactories

```java
[read-only] NutsCommandAliasFactoryConfig[] public commandFactories
public NutsCommandAliasFactoryConfig[] getCommandFactories(session)
```
#### 📄🎛 creationFinishTimeMillis

```java
[read-only] long public creationFinishTimeMillis
public long getCreationFinishTimeMillis()
```
#### 📄🎛 creationStartTimeMillis

```java
[read-only] long public creationStartTimeMillis
public long getCreationStartTimeMillis()
```
#### 📄🎛 creationTimeMillis

```java
[read-only] long public creationTimeMillis
public long getCreationTimeMillis()
```
#### 📄🎛 defaultIdBasedir

```java
[read-only] String public defaultIdBasedir
public String getDefaultIdBasedir(id)
```
#### 📄🎛 defaultIdContentExtension

```java
[read-only] String public defaultIdContentExtension
public String getDefaultIdContentExtension(packaging)
```
#### 📄🎛 defaultIdExtension

```java
[read-only] String public defaultIdExtension
public String getDefaultIdExtension(id)
```
#### 📄🎛 defaultIdFilename

```java
[read-only] String public defaultIdFilename
public String getDefaultIdFilename(id)
```
#### 📄🎛 defaultRepositories

```java
[read-only] NutsRepositoryDefinition[] public defaultRepositories
public NutsRepositoryDefinition[] getDefaultRepositories()
```
#### 📄🎛 env

```java
[read-only] String public env
public String getEnv(property, defaultValue)
```
#### 📄🎛 global

```java
[read-only] boolean public global
public boolean isGlobal()
```
#### 📄🎛 homeLocation

```java
[read-only] Path public homeLocation
public Path getHomeLocation(layout, location)
```
#### 📄🎛 homeLocations
all home locations key/value map where keys are in the form
 "osfamily:location" and values are absolute paths.
```java
[read-only] Map public homeLocations
public Map getHomeLocations()
```
#### 📄🎛 imports

```java
[read-only] Set public imports
public Set getImports()
```
#### 📄🎛 indexStoreClientFactory

```java
[read-only] NutsIndexStoreFactory public indexStoreClientFactory
public NutsIndexStoreFactory getIndexStoreClientFactory()
```
#### 📄🎛 javaCommand

```java
[read-only] String public javaCommand
public String getJavaCommand()
```
#### 📄🎛 javaOptions

```java
[read-only] String public javaOptions
public String getJavaOptions()
```
#### 📄🎛 name

```java
[read-only] String public name
public String getName()
```
#### 📄🎛 options

```java
[read-only] NutsWorkspaceOptions public options
public NutsWorkspaceOptions getOptions()
```
#### 📄🎛 os

```java
[read-only] NutsId public os
public NutsId getOs()
```
#### 📄🎛 osDist

```java
[read-only] NutsId public osDist
public NutsId getOsDist()
```
#### 📄🎛 osFamily

```java
[read-only] NutsOsFamily public osFamily
public NutsOsFamily getOsFamily()
```
#### 📄🎛 platform

```java
[read-only] NutsId public platform
public NutsId getPlatform()
```
#### 📄🎛 readOnly

```java
[read-only] boolean public readOnly
public boolean isReadOnly()
```
#### 📄🎛 repositories

```java
[read-only] NutsRepository[] public repositories
public NutsRepository[] getRepositories(session)
```
#### 📄🎛 repository

```java
[read-only] NutsRepository public repository
public NutsRepository getRepository(repositoryIdOrName, session)
```
#### 📄🎛 repositoryRefs

```java
[read-only] NutsRepositoryRef[] public repositoryRefs
public NutsRepositoryRef[] getRepositoryRefs(session)
```
#### 📄🎛 repositoryStoreLocationStrategy

```java
[read-only] NutsStoreLocationStrategy public repositoryStoreLocationStrategy
public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy()
```
#### 📄🎛 runtimeId

```java
[read-only] NutsId public runtimeId
public NutsId getRuntimeId()
```
#### 📄🎛 sdk

```java
[read-only] NutsSdkLocation public sdk
public NutsSdkLocation getSdk(sdkType, requestedVersion, session)
```
#### 📄🎛 sdkTypes

```java
[read-only] String[] public sdkTypes
public String[] getSdkTypes()
```
#### 📄🎛 sdks

```java
[read-only] NutsSdkLocation[] public sdks
public NutsSdkLocation[] getSdks(sdkType, session)
```
#### 📄🎛 storeLocation

```java
[read-only] Path public storeLocation
public Path getStoreLocation(id, folderType)
```
#### 📄🎛 storeLocationLayout

```java
[read-only] NutsOsFamily public storeLocationLayout
public NutsOsFamily getStoreLocationLayout()
```
#### 📄🎛 storeLocationStrategy

```java
[read-only] NutsStoreLocationStrategy public storeLocationStrategy
public NutsStoreLocationStrategy getStoreLocationStrategy()
```
#### 📄🎛 storeLocations
all home locations key/value map where keys are in the form "location"
 and values are absolute paths.
```java
[read-only] Map public storeLocations
public Map getStoreLocations()
```
#### 📄🎛 supportedRepositoryType

```java
[read-only] boolean public supportedRepositoryType
public boolean isSupportedRepositoryType(repositoryType)
```
#### 📄🎛 uuid

```java
[read-only] String public uuid
public String getUuid()
```
#### 📄🎛 workspaceLocation

```java
[read-only] Path public workspaceLocation
public Path getWorkspaceLocation()
```
### ⚙ Instance Methods
#### ⚙ addCommandAlias(command, options)


```java
boolean addCommandAlias(NutsCommandAliasConfig command, NutsAddOptions options)
```
**return**:boolean
- **NutsCommandAliasConfig command** : 
- **NutsAddOptions options** : 

#### ⚙ addCommandAliasFactory(commandFactory, options)


```java
void addCommandAliasFactory(NutsCommandAliasFactoryConfig commandFactory, NutsAddOptions options)
```
- **NutsCommandAliasFactoryConfig commandFactory** : 
- **NutsAddOptions options** : 

#### ⚙ addImports(importExpression, options)


```java
void addImports(String[] importExpression, NutsAddOptions options)
```
- **String[] importExpression** : 
- **NutsAddOptions options** : 

#### ⚙ addRepository(definition)


```java
NutsRepository addRepository(NutsRepositoryDefinition definition)
```
**return**:NutsRepository
- **NutsRepositoryDefinition definition** : 

#### ⚙ addRepository(options)


```java
NutsRepository addRepository(NutsAddRepositoryOptions options)
```
**return**:NutsRepository
- **NutsAddRepositoryOptions options** : 

#### ⚙ addRepository(repository, session)
add temporary repository

```java
NutsRepository addRepository(NutsRepositoryModel repository, NutsSession session)
```
**return**:NutsRepository
- **NutsRepositoryModel repository** : temporary repository
- **NutsSession session** : session

#### ⚙ addRepository(repositoryNamedUrl, session)
creates a new repository from the given \{\@code repositoryNamedUrl\}.

 Accepted \{\@code repositoryNamedUrl\} values are :
 \<ul\>
 \<li\>\'local\' : corresponds to a local updatable repository. will be named
 \'local\'\</li\>
 \<li\>\'m2\', \'.m2\', \'maven-local\' : corresponds the local maven folder
 repository. will be named \'local\'\</li\>
 \<li\>\'maven-central\': corresponds the remote maven central repository.
 will be named \'local\'\</li\>
 \<li\>\'maven-git\', \'vpc-public-maven\': corresponds the remote maven
 vpc-public-maven git folder repository. will be named \'local\'\</li\>
 \<li\>\'maven-git\', \'vpc-public-nuts\': corresponds the remote nuts
 vpc-public-nuts git folder repository. will be named \'local\'\</li\>
 \<li\>name=uri-or-path : corresponds the given uri. will be named name.
 Here are some examples:
 \<ul\>
 \<li\>myremote=http://192.168.6.3/folder\</li\>
 \<li\>myremote=/folder/subfolder\</li\>
 \<li\>myremote=c:/folder/subfolder\</li\>
 \</ul\>
 \</li\>
 \<li\>uri-or-path : corresponds the given uri. will be named uri\'s last
 path component name. Here are some examples:
 \<ul\>
 \<li\>http://192.168.6.3/folder : will be named \'folder\'\</li\>
 \<li\>myremote=/folder/subfolder : will be named \'folder\'\</li\>
 \<li\>myremote=c:/folder/subfolder : will be named \'folder\'\</li\>
 \</ul\>
 \</li\>
 \</ul\>

```java
NutsRepository addRepository(String repositoryNamedUrl, NutsSession session)
```
**return**:NutsRepository
- **String repositoryNamedUrl** : repositoryNamedUrl
- **NutsSession session** : 

#### ⚙ addSdk(location, options)


```java
boolean addSdk(NutsSdkLocation location, NutsAddOptions options)
```
**return**:boolean
- **NutsSdkLocation location** : 
- **NutsAddOptions options** : 

#### ⚙ createContentFaceId(id, desc)


```java
NutsId createContentFaceId(NutsId id, NutsDescriptor desc)
```
**return**:NutsId
- **NutsId id** : 
- **NutsDescriptor desc** : 

#### ⚙ createRepository(options, rootFolder, parentRepository)


```java
NutsRepository createRepository(NutsAddRepositoryOptions options, Path rootFolder, NutsRepository parentRepository)
```
**return**:NutsRepository
- **NutsAddRepositoryOptions options** : 
- **Path rootFolder** : 
- **NutsRepository parentRepository** : 

#### ⚙ createWorkspaceListManager(name, session)


```java
NutsWorkspaceListManager createWorkspaceListManager(String name, NutsSession session)
```
**return**:NutsWorkspaceListManager
- **String name** : 
- **NutsSession session** : 

#### ⚙ findCommandAlias(name, session)


```java
NutsWorkspaceCommandAlias findCommandAlias(String name, NutsSession session)
```
**return**:NutsWorkspaceCommandAlias
- **String name** : 
- **NutsSession session** : 

#### ⚙ findCommandAlias(name, forId, forOwner, session)
return alias definition for given name id and owner.

```java
NutsWorkspaceCommandAlias findCommandAlias(String name, NutsId forId, NutsId forOwner, NutsSession session)
```
**return**:NutsWorkspaceCommandAlias
- **String name** : alias name, not null
- **NutsId forId** : if not null, the alias name should resolve to the given id
- **NutsId forOwner** : if not null, the alias name should resolve to the owner
- **NutsSession session** : session

#### ⚙ findCommandAliases(session)


```java
List findCommandAliases(NutsSession session)
```
**return**:List
- **NutsSession session** : 

#### ⚙ findCommandAliases(id, session)


```java
List findCommandAliases(NutsId id, NutsSession session)
```
**return**:List
- **NutsId id** : 
- **NutsSession session** : 

#### ⚙ findRepository(repositoryIdOrName, session)


```java
NutsRepository findRepository(String repositoryIdOrName, NutsSession session)
```
**return**:NutsRepository
- **String repositoryIdOrName** : repository id or name
- **NutsSession session** : session

#### ⚙ findRepositoryById(repositoryIdOrName, session)


```java
NutsRepository findRepositoryById(String repositoryIdOrName, NutsSession session)
```
**return**:NutsRepository
- **String repositoryIdOrName** : 
- **NutsSession session** : 

#### ⚙ findRepositoryByName(repositoryIdOrName, session)


```java
NutsRepository findRepositoryByName(String repositoryIdOrName, NutsSession session)
```
**return**:NutsRepository
- **String repositoryIdOrName** : 
- **NutsSession session** : 

#### ⚙ findSdk(sdkType, location, session)


```java
NutsSdkLocation findSdk(String sdkType, NutsSdkLocation location, NutsSession session)
```
**return**:NutsSdkLocation
- **String sdkType** : 
- **NutsSdkLocation location** : 
- **NutsSession session** : 

#### ⚙ findSdkByName(sdkType, locationName, session)


```java
NutsSdkLocation findSdkByName(String sdkType, String locationName, NutsSession session)
```
**return**:NutsSdkLocation
- **String sdkType** : 
- **String locationName** : 
- **NutsSession session** : 

#### ⚙ findSdkByPath(sdkType, path, session)


```java
NutsSdkLocation findSdkByPath(String sdkType, Path path, NutsSession session)
```
**return**:NutsSdkLocation
- **String sdkType** : 
- **Path path** : 
- **NutsSession session** : 

#### ⚙ findSdkByVersion(sdkType, version, session)


```java
NutsSdkLocation findSdkByVersion(String sdkType, String version, NutsSession session)
```
**return**:NutsSdkLocation
- **String sdkType** : 
- **String version** : 
- **NutsSession session** : 

#### ⚙ name()


```java
String name()
```
**return**:String

#### ⚙ options()


```java
NutsWorkspaceOptions options()
```
**return**:NutsWorkspaceOptions

#### ⚙ removeAllImports(options)


```java
void removeAllImports(NutsRemoveOptions options)
```
- **NutsRemoveOptions options** : 

#### ⚙ removeCommandAlias(name, options)


```java
boolean removeCommandAlias(String name, NutsRemoveOptions options)
```
**return**:boolean
- **String name** : 
- **NutsRemoveOptions options** : 

#### ⚙ removeCommandAliasFactory(name, options)


```java
boolean removeCommandAliasFactory(String name, NutsRemoveOptions options)
```
**return**:boolean
- **String name** : 
- **NutsRemoveOptions options** : 

#### ⚙ removeImports(importExpression, options)


```java
void removeImports(String[] importExpression, NutsRemoveOptions options)
```
- **String[] importExpression** : 
- **NutsRemoveOptions options** : 

#### ⚙ removeRepository(locationOrRepositoryId, options)


```java
NutsWorkspaceConfigManager removeRepository(String locationOrRepositoryId, NutsRemoveOptions options)
```
**return**:NutsWorkspaceConfigManager
- **String locationOrRepositoryId** : 
- **NutsRemoveOptions options** : 

#### ⚙ removeSdk(location, options)


```java
NutsSdkLocation removeSdk(NutsSdkLocation location, NutsRemoveOptions options)
```
**return**:NutsSdkLocation
- **NutsSdkLocation location** : 
- **NutsRemoveOptions options** : 

#### ⚙ resolveRepositoryPath(repositoryLocation)


```java
Path resolveRepositoryPath(String repositoryLocation)
```
**return**:Path
- **String repositoryLocation** : 

#### ⚙ resolveSdkLocation(sdkType, path, preferredName, session)
verify if the path is a valid sdk path and return null if not

```java
NutsSdkLocation resolveSdkLocation(String sdkType, Path path, String preferredName, NutsSession session)
```
**return**:NutsSdkLocation
- **String sdkType** : sdk type
- **Path path** : sdk path
- **String preferredName** : preferredName
- **NutsSession session** : session

#### ⚙ save(session)


```java
void save(NutsSession session)
```
- **NutsSession session** : 

#### ⚙ save(force, session)
save config file if force is activated or non read only and some changes
 was detected in config file

```java
boolean save(boolean force, NutsSession session)
```
**return**:boolean
- **boolean force** : when true, save will always be performed
- **NutsSession session** : session

#### ⚙ searchSdkLocations(sdkType, session)


```java
NutsSdkLocation[] searchSdkLocations(String sdkType, NutsSession session)
```
**return**:NutsSdkLocation[]
- **String sdkType** : 
- **NutsSession session** : 

#### ⚙ searchSdkLocations(sdkType, path, session)


```java
NutsSdkLocation[] searchSdkLocations(String sdkType, Path path, NutsSession session)
```
**return**:NutsSdkLocation[]
- **String sdkType** : 
- **Path path** : 
- **NutsSession session** : 

#### ⚙ setEnv(property, value, options)


```java
void setEnv(String property, String value, NutsUpdateOptions options)
```
- **String property** : 
- **String value** : 
- **NutsUpdateOptions options** : 

#### ⚙ setHomeLocation(layout, folderType, location, options)


```java
void setHomeLocation(NutsOsFamily layout, NutsStoreLocation folderType, String location, NutsUpdateOptions options)
```
- **NutsOsFamily layout** : 
- **NutsStoreLocation folderType** : 
- **String location** : 
- **NutsUpdateOptions options** : 

#### ⚙ setImports(imports, options)


```java
void setImports(String[] imports, NutsUpdateOptions options)
```
- **String[] imports** : 
- **NutsUpdateOptions options** : 

#### ⚙ setStoreLocation(folderType, location, options)


```java
void setStoreLocation(NutsStoreLocation folderType, String location, NutsUpdateOptions options)
```
- **NutsStoreLocation folderType** : 
- **String location** : 
- **NutsUpdateOptions options** : 

#### ⚙ setStoreLocationLayout(layout, options)


```java
void setStoreLocationLayout(NutsOsFamily layout, NutsUpdateOptions options)
```
- **NutsOsFamily layout** : 
- **NutsUpdateOptions options** : 

#### ⚙ setStoreLocationStrategy(strategy, options)


```java
void setStoreLocationStrategy(NutsStoreLocationStrategy strategy, NutsUpdateOptions options)
```
- **NutsStoreLocationStrategy strategy** : 
- **NutsUpdateOptions options** : 

#### ⚙ stored()


```java
NutsWorkspaceStoredConfig stored()
```
**return**:NutsWorkspaceStoredConfig

## ☕ NutsWorkspaceListConfig
```java
public net.vpc.app.nuts.NutsWorkspaceListConfig
```
 Class for managing a Workspace list

 \@author Nasreddine Bac Ali
 date 2019-03-02
 \@since 0.5.4
 \@category Config

### 🪄 Constructors
#### 🪄 NutsWorkspaceListConfig()


```java
NutsWorkspaceListConfig()
```

#### 🪄 NutsWorkspaceListConfig(other)


```java
NutsWorkspaceListConfig(NutsWorkspaceListConfig other)
```
- **NutsWorkspaceListConfig other** : 

#### 🪄 NutsWorkspaceListConfig(uuid, name)


```java
NutsWorkspaceListConfig(String uuid, String name)
```
- **String uuid** : 
- **String name** : 

### 🎛 Instance Properties
#### 📄🎛 name

```java
[read-only] String public name
public String getName()
```
#### 📄🎛 uuid

```java
[read-only] String public uuid
public String getUuid()
```
#### 📄🎛 workspaces

```java
[read-only] List public workspaces
public List getWorkspaces()
```
### ⚙ Instance Methods
#### ⚙ setName(name)


```java
NutsWorkspaceListConfig setName(String name)
```
**return**:NutsWorkspaceListConfig
- **String name** : 

#### ⚙ setUuid(uuid)


```java
NutsWorkspaceListConfig setUuid(String uuid)
```
**return**:NutsWorkspaceListConfig
- **String uuid** : 

#### ⚙ setWorkspaces(workspaces)


```java
void setWorkspaces(List workspaces)
```
- **List workspaces** : 

#### ⚙ toString()


```java
String toString()
```
**return**:String

## ☕ NutsWorkspaceListManager
```java
public interface net.vpc.app.nuts.NutsWorkspaceListManager
```
 Class for managing a Workspace list

 \@author Nasreddine Bac Ali
 date 2019-03-02
 \@since 0.5.4
 \@category Config

### 🎛 Instance Properties
#### 📄🎛 config

```java
[read-only] NutsWorkspaceListConfig public config
public NutsWorkspaceListConfig getConfig()
```
#### 📄🎛 workspaceLocation

```java
[read-only] NutsWorkspaceLocation public workspaceLocation
public NutsWorkspaceLocation getWorkspaceLocation(uuid)
```
#### 📄🎛 workspaces

```java
[read-only] List public workspaces
public List getWorkspaces()
```
### ⚙ Instance Methods
#### ⚙ addWorkspace(path)


```java
NutsWorkspace addWorkspace(String path)
```
**return**:NutsWorkspace
- **String path** : 

#### ⚙ removeWorkspace(name)


```java
boolean removeWorkspace(String name)
```
**return**:boolean
- **String name** : 

#### ⚙ save()


```java
void save()
```

#### ⚙ setConfig(config)


```java
NutsWorkspaceListManager setConfig(NutsWorkspaceListConfig config)
```
**return**:NutsWorkspaceListManager
- **NutsWorkspaceListConfig config** : 

## ☕ NutsWorkspaceLocation
```java
public net.vpc.app.nuts.NutsWorkspaceLocation
```
 Class for managing a Workspace list

 \@author Nasreddine Bac Ali
 date 2019-03-02
 \@since 0.5.4
 \@category Config

### 🪄 Constructors
#### 🪄 NutsWorkspaceLocation()


```java
NutsWorkspaceLocation()
```

#### 🪄 NutsWorkspaceLocation(other)


```java
NutsWorkspaceLocation(NutsWorkspaceLocation other)
```
- **NutsWorkspaceLocation other** : 

#### 🪄 NutsWorkspaceLocation(uuid, name, location)


```java
NutsWorkspaceLocation(String uuid, String name, String location)
```
- **String uuid** : 
- **String name** : 
- **String location** : 

### 🎛 Instance Properties
#### 📄🎛 enabled

```java
[read-only] boolean public enabled
public boolean isEnabled()
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
#### 📄🎛 uuid

```java
[read-only] String public uuid
public String getUuid()
```
### ⚙ Instance Methods
#### ⚙ copy()


```java
NutsWorkspaceLocation copy()
```
**return**:NutsWorkspaceLocation

#### ⚙ equals(o)


```java
boolean equals(Object o)
```
**return**:boolean
- **Object o** : 

#### ⚙ hashCode()


```java
int hashCode()
```
**return**:int

#### ⚙ setEnabled(enabled)


```java
NutsWorkspaceLocation setEnabled(boolean enabled)
```
**return**:NutsWorkspaceLocation
- **boolean enabled** : 

#### ⚙ setLocation(location)


```java
NutsWorkspaceLocation setLocation(String location)
```
**return**:NutsWorkspaceLocation
- **String location** : 

#### ⚙ setName(name)


```java
NutsWorkspaceLocation setName(String name)
```
**return**:NutsWorkspaceLocation
- **String name** : 

#### ⚙ setUuid(uuid)


```java
NutsWorkspaceLocation setUuid(String uuid)
```
**return**:NutsWorkspaceLocation
- **String uuid** : 

#### ⚙ toString()


```java
String toString()
```
**return**:String

## ☕ NutsWorkspaceOpenMode
```java
public final net.vpc.app.nuts.NutsWorkspaceOpenMode
```

 \@author vpc
 \@since 0.5.4
 \@category Config

### 📢❄ Constant Fields
#### 📢❄ CREATE_NEW
```java
public static final NutsWorkspaceOpenMode CREATE_NEW
```
#### 📢❄ OPEN_EXISTING
```java
public static final NutsWorkspaceOpenMode OPEN_EXISTING
```
#### 📢❄ OPEN_OR_CREATE
```java
public static final NutsWorkspaceOpenMode OPEN_OR_CREATE
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
NutsWorkspaceOpenMode valueOf(String name)
```
**return**:NutsWorkspaceOpenMode
- **String name** : 

#### 📢⚙ values()


```java
NutsWorkspaceOpenMode[] values()
```
**return**:NutsWorkspaceOpenMode[]

### ⚙ Instance Methods
#### ⚙ id()
lower cased identifier.

```java
String id()
```
**return**:String

## ☕ NutsWorkspaceOptions
```java
public interface net.vpc.app.nuts.NutsWorkspaceOptions
```
 Workspace options class that holds command argument information.

 \@since 0.5.4
 \@category Config

### 🎛 Instance Properties
#### 📄🎛 apiVersion
nuts api version to boot.
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child
 workspaces)
```java
[read-only] String public apiVersion
public String getApiVersion()
```
#### 📄🎛 applicationArguments
application arguments.
 \<p\>
 \<strong\>option-type :\</strong\> runtime (available only for the current workspace instance)
```java
[read-only] String[] public applicationArguments
public String[] getApplicationArguments()
```
#### 📄🎛 archetype
workspace archetype to consider when creating a new workspace.
 \<p\>
 \<strong\>option-type :\</strong\> create (used when creating new workspace. will not be
 exported nor promoted to runtime)
```java
[read-only] String public archetype
public String getArchetype()
```
#### 📄🎛 bootRepositories
boot repositories \';\' separated

 \<p\>
 \<strong\>option-type :\</strong\> runtime (available only for the current workspace instance)
```java
[read-only] String public bootRepositories
public String getBootRepositories()
```
#### 📄🎛 cached
when true, use cache
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] boolean public cached
public boolean isCached()
```
#### 📄🎛 classLoaderSupplier
class loader supplier.
 \<p\>
 \<strong\>option-type :\</strong\> runtime (available only for the current workspace instance)
```java
[read-only] Supplier public classLoaderSupplier
public Supplier getClassLoaderSupplier()
```
#### 📄🎛 confirm
confirm mode.
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] NutsConfirmationMode public confirm
public NutsConfirmationMode getConfirm()
```
#### 📄🎛 creationTime
workspace creation evaluated time.
 \<p\>
 \<strong\>option-type :\</strong\> runtime (available only for the current workspace instance)
```java
[read-only] long public creationTime
public long getCreationTime()
```
#### 📄🎛 credentials
credential needed to log into workspace.
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] char[] public credentials
public char[] getCredentials()
```
#### 📄🎛 debug
if true, extra debug information is written to standard output.
 Particularly, exception stack traces are displayed instead of simpler messages.
 \<p\>
 \<strong\>option-type :\</strong\> runtime (available only for the current workspace instance)
```java
[read-only] boolean public debug
public boolean isDebug()
```
#### 📄🎛 dry
if true no real execution, wil dry exec (execute without side effect).
 \<p\>
 \<strong\>option-type :\</strong\> runtime (available only for the current workspace instance)
```java
[read-only] boolean public dry
public boolean isDry()
```
#### 📄🎛 excludedExtensions
extensions to be excluded when opening the workspace.
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] String[] public excludedExtensions
public String[] getExcludedExtensions()
```
#### 📄🎛 excludedRepositories
repository list to be excluded when opening the workspace.
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] String[] public excludedRepositories
public String[] getExcludedRepositories()
```
#### 📄🎛 executionType
execution type.
 \<p\>
 \<strong\>option-type :\</strong\> runtime (available only for the current workspace instance)
```java
[read-only] NutsExecutionType public executionType
public NutsExecutionType getExecutionType()
```
#### 📄🎛 executorOptions
extra executor options.
 \<p\>
 \<strong\>option-type :\</strong\> runtime (available only for the current workspace instance)
```java
[read-only] String[] public executorOptions
public String[] getExecutorOptions()
```
#### 📄🎛 executorService
executor service used to create worker threads. when null, use default.
 this option cannot be defined via arguments.

 \<p\>
 \<strong\>option-type :\</strong\> runtime (available only for the current workspace instance)
```java
[read-only] ExecutorService public executorService
public ExecutorService getExecutorService()
```
#### 📄🎛 fetchStrategy
default fetch strategy
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] NutsFetchStrategy public fetchStrategy
public NutsFetchStrategy getFetchStrategy()
```
#### 📄🎛 global
if true consider global/system repository
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] boolean public global
public boolean isGlobal()
```
#### 📄🎛 gui
if true consider GUI/Swing mode
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] boolean public gui
public boolean isGui()
```
#### 📄🎛 homeLocations
return home locations.
 \<p\>
 \<strong\>option-type :\</strong\> create (used when creating new workspace. will not be
 exported nor promoted to runtime).
```java
[read-only] Map public homeLocations
public Map getHomeLocations()
```
#### 📄🎛 indexed
when true, use index
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] boolean public indexed
public boolean isIndexed()
```
#### 📄🎛 inherited
if true, workspace were invoked from parent process and hence inherits its options.
 \<p\>
 \<strong\>option-type :\</strong\> runtime (available only for the current workspace instance)
```java
[read-only] boolean public inherited
public boolean isInherited()
```
#### 📄🎛 javaCommand
java command (or java home) used to run workspace.
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] String public javaCommand
public String getJavaCommand()
```
#### 📄🎛 javaOptions
java options used to run workspace.
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] String public javaOptions
public String getJavaOptions()
```
#### 📄🎛 logConfig
workspace log configuration.
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] NutsLogConfig public logConfig
public NutsLogConfig getLogConfig()
```
#### 📄🎛 name
user friendly workspace name.
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child
 workspaces)
```java
[read-only] String public name
public String getName()
```
#### 📄🎛 openMode
mode used to open workspace.
 \<p\>
 \<strong\>option-type :\</strong\> runtime (available only for the current workspace instance)
```java
[read-only] NutsWorkspaceOpenMode public openMode
public NutsWorkspaceOpenMode getOpenMode()
```
#### 📄🎛 outputFormat
default output format type.
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] NutsOutputFormat public outputFormat
public NutsOutputFormat getOutputFormat()
```
#### 📄🎛 outputFormatOptions
default output formation options.
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] String[] public outputFormatOptions
public String[] getOutputFormatOptions()
```
#### 📄🎛 progressOptions
return progress options string.
 progress options configures how progress monitors are processed.
 \'no\' value means that progress is disabled.
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] String public progressOptions
public String getProgressOptions()
```
#### 📄🎛 readOnly
if true, workspace configuration are non modifiable.
 However cache stills modifiable so that it is possible to load external libraries.
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] boolean public readOnly
public boolean isReadOnly()
```
#### 📄🎛 recover
if true, boot, cache and temp folder are deleted.
 \<p\>
 \<strong\>option-type :\</strong\> runtime (available only for the current workspace instance)
```java
[read-only] boolean public recover
public boolean isRecover()
```
#### 📄🎛 repositoryStoreLocationStrategy
repository store location strategy to consider when creating new repositories
 for a new workspace.
 \<p\>
 \<strong\>option-type :\</strong\> create (used when creating new workspace. will not be
 exported nor promoted to runtime)
```java
[read-only] NutsStoreLocationStrategy public repositoryStoreLocationStrategy
public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy()
```
#### 📄🎛 reset
if true, workspace will be reset (all configuration and runtime files deleted).
 \<p\>
 \<strong\>option-type :\</strong\> runtime (available only for the current workspace instance)
```java
[read-only] boolean public reset
public boolean isReset()
```
#### 📄🎛 runtimeId
nuts runtime id (or version) to boot.

 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] String public runtimeId
public String getRuntimeId()
```
#### 📄🎛 skipBoot
if true, do not bootstrap workspace after reset/recover.
 When reset/recover is not active this option is not accepted and an error will be thrown
 \<p\>
 defaults to false.
 \<p\>
 \<strong\>option-type :\</strong\> runtime (available only for the current workspace instance)
```java
[read-only] boolean public skipBoot
public boolean isSkipBoot()
```
#### 📄🎛 skipCompanions
if true, do not install nuts companion tools upon workspace creation.
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] boolean public skipCompanions
public boolean isSkipCompanions()
```
#### 📄🎛 skipWelcome
if true, do not run welcome when no application arguments were resolved.
 \<p\>
 defaults to false.
 \<p\>
 \<strong\>option-type :\</strong\>  exported (inherited in child workspaces)
```java
[read-only] boolean public skipWelcome
public boolean isSkipWelcome()
```
#### 📄🎛 stderr
default standard error. when null, use \{\@code System.err\}
 this option cannot be defined via arguments.

 \<p\>
 \<strong\>option-type :\</strong\> runtime (available only for the current workspace instance)
```java
[read-only] PrintStream public stderr
public PrintStream getStderr()
```
#### 📄🎛 stdin
default standard input. when null, use \{\@code System.in\}
 this option cannot be defined via arguments.

 \<p\>
 \<strong\>option-type :\</strong\> runtime (available only for the current workspace instance)
```java
[read-only] InputStream public stdin
public InputStream getStdin()
```
#### 📄🎛 stdout
default standard output. when null, use \{\@code System.out\}
 this option cannot be defined via arguments.

 \<p\>
 \<strong\>option-type :\</strong\> runtime (available only for the current workspace instance)
```java
[read-only] PrintStream public stdout
public PrintStream getStdout()
```
#### 📄🎛 storeLocationLayout
store location layout to consider when creating a new workspace.
 \<p\>
 \<strong\>option-type :\</strong\> create (used when creating new workspace. will not be
 exported nor promoted to runtime)
```java
[read-only] NutsOsFamily public storeLocationLayout
public NutsOsFamily getStoreLocationLayout()
```
#### 📄🎛 storeLocationStrategy
store location strategy for creating a new workspace.
 \<p\>
 \<strong\>option-type :\</strong\> create (used when creating new workspace. will not be
 exported nor promoted to runtime)
```java
[read-only] NutsStoreLocationStrategy public storeLocationStrategy
public NutsStoreLocationStrategy getStoreLocationStrategy()
```
#### 📄🎛 storeLocations
store locations map to consider when creating a new workspace.
 \<p\>
 \<strong\>option-type :\</strong\> create (used when creating new workspace. will not be
 exported nor promoted to runtime)
```java
[read-only] Map public storeLocations
public Map getStoreLocations()
```
#### 📄🎛 terminalMode
terminal mode (inherited, formatted, filtered) to use.
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] NutsTerminalMode public terminalMode
public NutsTerminalMode getTerminalMode()
```
#### 📄🎛 trace
when true, extra trace user-friendly information is written to standard output.
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] boolean public trace
public boolean isTrace()
```
#### 📄🎛 transientRepositories
repositories to register temporarily when running the workspace.
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] String[] public transientRepositories
public String[] getTransientRepositories()
```
#### 📄🎛 transitive
when true, use transitive repositories
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] boolean public transitive
public boolean isTransitive()
```
#### 📄🎛 userName
username to log into when running workspace.
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child workspaces)
```java
[read-only] String public userName
public String getUserName()
```
#### 📄🎛 workspace
workspace folder location path.
 \<p\>
 \<strong\>option-type :\</strong\> exported (inherited in child
 workspaces)
```java
[read-only] String public workspace
public String getWorkspace()
```
### ⚙ Instance Methods
#### ⚙ copy()
create a \<strong\>mutable\</strong\> copy of this instance

```java
NutsWorkspaceOptionsBuilder copy()
```
**return**:NutsWorkspaceOptionsBuilder

#### ⚙ format()
create a new instance of options formatter that help formatting this instance.

```java
NutsWorkspaceOptionsFormat format()
```
**return**:NutsWorkspaceOptionsFormat

#### ⚙ getHomeLocation(layout, location)
return home location.
 \<p\>
 \<strong\>option-type :\</strong\> create (used when creating new workspace. will not be
 exported nor promoted to runtime).

```java
String getHomeLocation(NutsOsFamily layout, NutsStoreLocation location)
```
**return**:String
- **NutsOsFamily layout** : layout
- **NutsStoreLocation location** : location

#### ⚙ getStoreLocation(folder)
store location for the given folder.
 \<p\>
 \<strong\>option-type :\</strong\> create (used when creating new workspace. will not be
 exported nor promoted to runtime)

```java
String getStoreLocation(NutsStoreLocation folder)
```
**return**:String
- **NutsStoreLocation folder** : folder type

## ☕ NutsWorkspaceOptionsBuilder
```java
public interface net.vpc.app.nuts.NutsWorkspaceOptionsBuilder
```
 Mutable Workspace options
 \@category Config

### 🎛 Instance Properties
#### ✏🎛 homeLocations
set home locations.
 \<p\>
 \<strong\>option-type :\</strong\> create (used when creating new workspace. will not be
 exported nor promoted to runtime).
```java
[write-only] NutsWorkspaceOptionsBuilder public homeLocations
public NutsWorkspaceOptionsBuilder setHomeLocations(homeLocations)
```
#### ✏🎛 skipBoot
if true, do not bootstrap workspace after reset/recover.
 When reset/recover is not active this option is not accepted and an error will be thrown
 \<p\>
 defaults to false.
 \<p\>
 \<strong\>option-type :\</strong\> runtime (available only for the current workspace instance)
```java
[write-only] NutsWorkspaceOptionsBuilder public skipBoot
public NutsWorkspaceOptionsBuilder setSkipBoot(skipBoot)
```
#### ✏🎛 storeLocations
set store location strategy for creating a new workspace.
 \<p\>
 \<strong\>option-type :\</strong\> create (used when creating new workspace. will not be
 exported nor promoted to runtime)
```java
[write-only] NutsWorkspaceOptionsBuilder public storeLocations
public NutsWorkspaceOptionsBuilder setStoreLocations(storeLocations)
```
### ⚙ Instance Methods
#### ⚙ addOutputFormatOptions(options)


```java
NutsWorkspaceOptionsBuilder addOutputFormatOptions(String[] options)
```
**return**:NutsWorkspaceOptionsBuilder
- **String[] options** : 

#### ⚙ setApiVersion(apiVersion)


```java
NutsWorkspaceOptionsBuilder setApiVersion(String apiVersion)
```
**return**:NutsWorkspaceOptionsBuilder
- **String apiVersion** : 

#### ⚙ setApplicationArguments(applicationArguments)


```java
NutsWorkspaceOptionsBuilder setApplicationArguments(String[] applicationArguments)
```
**return**:NutsWorkspaceOptionsBuilder
- **String[] applicationArguments** : 

#### ⚙ setArchetype(archetype)


```java
NutsWorkspaceOptionsBuilder setArchetype(String archetype)
```
**return**:NutsWorkspaceOptionsBuilder
- **String archetype** : 

#### ⚙ setBootRepositories(bootRepositories)


```java
NutsWorkspaceOptionsBuilder setBootRepositories(String bootRepositories)
```
**return**:NutsWorkspaceOptionsBuilder
- **String bootRepositories** : 

#### ⚙ setCached(cached)


```java
NutsWorkspaceOptionsBuilder setCached(boolean cached)
```
**return**:NutsWorkspaceOptionsBuilder
- **boolean cached** : 

#### ⚙ setClassLoaderSupplier(provider)


```java
NutsWorkspaceOptionsBuilder setClassLoaderSupplier(Supplier provider)
```
**return**:NutsWorkspaceOptionsBuilder
- **Supplier provider** : 

#### ⚙ setConfirm(confirm)


```java
NutsWorkspaceOptionsBuilder setConfirm(NutsConfirmationMode confirm)
```
**return**:NutsWorkspaceOptionsBuilder
- **NutsConfirmationMode confirm** : 

#### ⚙ setCreationTime(creationTime)


```java
NutsWorkspaceOptionsBuilder setCreationTime(long creationTime)
```
**return**:NutsWorkspaceOptionsBuilder
- **long creationTime** : 

#### ⚙ setCredentials(credentials)


```java
NutsWorkspaceOptionsBuilder setCredentials(char[] credentials)
```
**return**:NutsWorkspaceOptionsBuilder
- **char[] credentials** : 

#### ⚙ setDebug(debug)


```java
NutsWorkspaceOptionsBuilder setDebug(boolean debug)
```
**return**:NutsWorkspaceOptionsBuilder
- **boolean debug** : 

#### ⚙ setDry(dry)


```java
NutsWorkspaceOptionsBuilder setDry(boolean dry)
```
**return**:NutsWorkspaceOptionsBuilder
- **boolean dry** : 

#### ⚙ setExcludedExtensions(excludedExtensions)


```java
NutsWorkspaceOptionsBuilder setExcludedExtensions(String[] excludedExtensions)
```
**return**:NutsWorkspaceOptionsBuilder
- **String[] excludedExtensions** : 

#### ⚙ setExcludedRepositories(excludedRepositories)


```java
NutsWorkspaceOptionsBuilder setExcludedRepositories(String[] excludedRepositories)
```
**return**:NutsWorkspaceOptionsBuilder
- **String[] excludedRepositories** : 

#### ⚙ setExecutionType(executionType)


```java
NutsWorkspaceOptionsBuilder setExecutionType(NutsExecutionType executionType)
```
**return**:NutsWorkspaceOptionsBuilder
- **NutsExecutionType executionType** : 

#### ⚙ setExecutorOptions(executorOptions)


```java
NutsWorkspaceOptionsBuilder setExecutorOptions(String[] executorOptions)
```
**return**:NutsWorkspaceOptionsBuilder
- **String[] executorOptions** : 

#### ⚙ setExecutorService(executorService)


```java
NutsWorkspaceOptionsBuilder setExecutorService(ExecutorService executorService)
```
**return**:NutsWorkspaceOptionsBuilder
- **ExecutorService executorService** : 

#### ⚙ setFetchStrategy(fetchStrategy)


```java
NutsWorkspaceOptionsBuilder setFetchStrategy(NutsFetchStrategy fetchStrategy)
```
**return**:NutsWorkspaceOptionsBuilder
- **NutsFetchStrategy fetchStrategy** : 

#### ⚙ setGlobal(global)


```java
NutsWorkspaceOptionsBuilder setGlobal(boolean global)
```
**return**:NutsWorkspaceOptionsBuilder
- **boolean global** : 

#### ⚙ setGui(gui)


```java
NutsWorkspaceOptionsBuilder setGui(boolean gui)
```
**return**:NutsWorkspaceOptionsBuilder
- **boolean gui** : 

#### ⚙ setHomeLocation(layout, location, value)


```java
NutsWorkspaceOptionsBuilder setHomeLocation(NutsOsFamily layout, NutsStoreLocation location, String value)
```
**return**:NutsWorkspaceOptionsBuilder
- **NutsOsFamily layout** : 
- **NutsStoreLocation location** : 
- **String value** : 

#### ⚙ setIndexed(indexed)


```java
NutsWorkspaceOptionsBuilder setIndexed(boolean indexed)
```
**return**:NutsWorkspaceOptionsBuilder
- **boolean indexed** : 

#### ⚙ setInherited(inherited)


```java
NutsWorkspaceOptionsBuilder setInherited(boolean inherited)
```
**return**:NutsWorkspaceOptionsBuilder
- **boolean inherited** : 

#### ⚙ setJavaCommand(javaCommand)


```java
NutsWorkspaceOptionsBuilder setJavaCommand(String javaCommand)
```
**return**:NutsWorkspaceOptionsBuilder
- **String javaCommand** : 

#### ⚙ setJavaOptions(javaOptions)


```java
NutsWorkspaceOptionsBuilder setJavaOptions(String javaOptions)
```
**return**:NutsWorkspaceOptionsBuilder
- **String javaOptions** : 

#### ⚙ setLogConfig(logConfig)


```java
NutsWorkspaceOptionsBuilder setLogConfig(NutsLogConfig logConfig)
```
**return**:NutsWorkspaceOptionsBuilder
- **NutsLogConfig logConfig** : 

#### ⚙ setName(workspaceName)


```java
NutsWorkspaceOptionsBuilder setName(String workspaceName)
```
**return**:NutsWorkspaceOptionsBuilder
- **String workspaceName** : 

#### ⚙ setOpenMode(openMode)


```java
NutsWorkspaceOptionsBuilder setOpenMode(NutsWorkspaceOpenMode openMode)
```
**return**:NutsWorkspaceOptionsBuilder
- **NutsWorkspaceOpenMode openMode** : 

#### ⚙ setOutputFormat(outputFormat)


```java
NutsWorkspaceOptionsBuilder setOutputFormat(NutsOutputFormat outputFormat)
```
**return**:NutsWorkspaceOptionsBuilder
- **NutsOutputFormat outputFormat** : 

#### ⚙ setOutputFormatOptions(options)


```java
NutsWorkspaceOptionsBuilder setOutputFormatOptions(String[] options)
```
**return**:NutsWorkspaceOptionsBuilder
- **String[] options** : 

#### ⚙ setProgressOptions(progressOptions)


```java
NutsWorkspaceOptionsBuilder setProgressOptions(String progressOptions)
```
**return**:NutsWorkspaceOptionsBuilder
- **String progressOptions** : 

#### ⚙ setReadOnly(readOnly)


```java
NutsWorkspaceOptionsBuilder setReadOnly(boolean readOnly)
```
**return**:NutsWorkspaceOptionsBuilder
- **boolean readOnly** : 

#### ⚙ setRecover(recover)


```java
NutsWorkspaceOptionsBuilder setRecover(boolean recover)
```
**return**:NutsWorkspaceOptionsBuilder
- **boolean recover** : 

#### ⚙ setRepositoryStoreLocationStrategy(repositoryStoreLocationStrategy)


```java
NutsWorkspaceOptionsBuilder setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy)
```
**return**:NutsWorkspaceOptionsBuilder
- **NutsStoreLocationStrategy repositoryStoreLocationStrategy** : 

#### ⚙ setReset(reset)


```java
NutsWorkspaceOptionsBuilder setReset(boolean reset)
```
**return**:NutsWorkspaceOptionsBuilder
- **boolean reset** : 

#### ⚙ setRuntimeId(runtimeId)


```java
NutsWorkspaceOptionsBuilder setRuntimeId(String runtimeId)
```
**return**:NutsWorkspaceOptionsBuilder
- **String runtimeId** : 

#### ⚙ setSkipCompanions(skipInstallCompanions)


```java
NutsWorkspaceOptionsBuilder setSkipCompanions(boolean skipInstallCompanions)
```
**return**:NutsWorkspaceOptionsBuilder
- **boolean skipInstallCompanions** : 

#### ⚙ setSkipWelcome(skipWelcome)


```java
NutsWorkspaceOptionsBuilder setSkipWelcome(boolean skipWelcome)
```
**return**:NutsWorkspaceOptionsBuilder
- **boolean skipWelcome** : 

#### ⚙ setStderr(stderr)


```java
NutsWorkspaceOptionsBuilder setStderr(PrintStream stderr)
```
**return**:NutsWorkspaceOptionsBuilder
- **PrintStream stderr** : 

#### ⚙ setStdin(stdin)


```java
NutsWorkspaceOptionsBuilder setStdin(InputStream stdin)
```
**return**:NutsWorkspaceOptionsBuilder
- **InputStream stdin** : 

#### ⚙ setStdout(stdout)


```java
NutsWorkspaceOptionsBuilder setStdout(PrintStream stdout)
```
**return**:NutsWorkspaceOptionsBuilder
- **PrintStream stdout** : 

#### ⚙ setStoreLocation(location, value)


```java
NutsWorkspaceOptionsBuilder setStoreLocation(NutsStoreLocation location, String value)
```
**return**:NutsWorkspaceOptionsBuilder
- **NutsStoreLocation location** : 
- **String value** : 

#### ⚙ setStoreLocationLayout(storeLocationLayout)


```java
NutsWorkspaceOptionsBuilder setStoreLocationLayout(NutsOsFamily storeLocationLayout)
```
**return**:NutsWorkspaceOptionsBuilder
- **NutsOsFamily storeLocationLayout** : 

#### ⚙ setStoreLocationStrategy(storeLocationStrategy)


```java
NutsWorkspaceOptionsBuilder setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy)
```
**return**:NutsWorkspaceOptionsBuilder
- **NutsStoreLocationStrategy storeLocationStrategy** : 

#### ⚙ setTerminalMode(terminalMode)


```java
NutsWorkspaceOptionsBuilder setTerminalMode(NutsTerminalMode terminalMode)
```
**return**:NutsWorkspaceOptionsBuilder
- **NutsTerminalMode terminalMode** : 

#### ⚙ setTrace(trace)


```java
NutsWorkspaceOptionsBuilder setTrace(boolean trace)
```
**return**:NutsWorkspaceOptionsBuilder
- **boolean trace** : 

#### ⚙ setTransientRepositories(transientRepositories)


```java
NutsWorkspaceOptionsBuilder setTransientRepositories(String[] transientRepositories)
```
**return**:NutsWorkspaceOptionsBuilder
- **String[] transientRepositories** : 

#### ⚙ setTransitive(transitive)


```java
NutsWorkspaceOptionsBuilder setTransitive(boolean transitive)
```
**return**:NutsWorkspaceOptionsBuilder
- **boolean transitive** : 

#### ⚙ setUsername(username)


```java
NutsWorkspaceOptionsBuilder setUsername(String username)
```
**return**:NutsWorkspaceOptionsBuilder
- **String username** : 

#### ⚙ setWorkspace(workspace)


```java
NutsWorkspaceOptionsBuilder setWorkspace(String workspace)
```
**return**:NutsWorkspaceOptionsBuilder
- **String workspace** : 

## ☕ NutsWorkspaceStoredConfig
```java
public interface net.vpc.app.nuts.NutsWorkspaceStoredConfig
```
 Nuts read-only configuration

 \@author vpc
 \@since 0.5.4
 \@category Config

### 🎛 Instance Properties
#### 📄🎛 apiId

```java
[read-only] NutsId public apiId
public NutsId getApiId()
```
#### 📄🎛 bootRepositories

```java
[read-only] String public bootRepositories
public String getBootRepositories()
```
#### 📄🎛 global

```java
[read-only] boolean public global
public boolean isGlobal()
```
#### 📄🎛 homeLocation

```java
[read-only] String public homeLocation
public String getHomeLocation(layout, location)
```
#### 📄🎛 homeLocations
all home locations key/value map where keys are in the form
 "osfamily:location" and values are absolute paths.
```java
[read-only] Map public homeLocations
public Map getHomeLocations()
```
#### 📄🎛 javaCommand

```java
[read-only] String public javaCommand
public String getJavaCommand()
```
#### 📄🎛 javaOptions

```java
[read-only] String public javaOptions
public String getJavaOptions()
```
#### 📄🎛 name

```java
[read-only] String public name
public String getName()
```
#### 📄🎛 repositoryStoreLocationStrategy

```java
[read-only] NutsStoreLocationStrategy public repositoryStoreLocationStrategy
public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy()
```
#### 📄🎛 runtimeDependencies

```java
[read-only] String public runtimeDependencies
public String getRuntimeDependencies()
```
#### 📄🎛 runtimeId

```java
[read-only] NutsId public runtimeId
public NutsId getRuntimeId()
```
#### 📄🎛 storeLocation

```java
[read-only] String public storeLocation
public String getStoreLocation(folderType)
```
#### 📄🎛 storeLocationLayout

```java
[read-only] NutsOsFamily public storeLocationLayout
public NutsOsFamily getStoreLocationLayout()
```
#### 📄🎛 storeLocationStrategy

```java
[read-only] NutsStoreLocationStrategy public storeLocationStrategy
public NutsStoreLocationStrategy getStoreLocationStrategy()
```
#### 📄🎛 storeLocations
all home locations key/value map where keys are in the form "location"
 and values are absolute paths.
```java
[read-only] Map public storeLocations
public Map getStoreLocations()
```

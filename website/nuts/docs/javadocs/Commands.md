---
id: javadoc_Commands
title: Commands
sidebar_label: Commands
---
                                                
```
     __        __           ___    ____  ____
  /\ \ \ _  __/ /______    /   |  / __ \/  _/
 /  \/ / / / / __/ ___/   / /| | / /_/ // /   
/ /\  / /_/ / /_(__  )   / ___ |/ ____// /       
\_\ \/\__,_/\__/____/   /_/  |_/_/   /___/  version 0.7.0
```

## ☕ NutsDeployCommand
```java
public interface net.vpc.app.nuts.NutsDeployCommand
```
 Nuts deploy command
 \@author vpc
 \@since 0.5.4
 \@category Commands

### 🎛 Instance Properties
#### ✏🎛 content
set content
```java
[write-only] NutsDeployCommand public content
public NutsDeployCommand setContent(url)
```
#### ✏🎛 descSha1
set descriptor sha1 hash
```java
[write-only] NutsDeployCommand public descSha1
public NutsDeployCommand setDescSha1(descSHA1)
```
#### ✏🎛 descriptor
set descriptor
```java
[write-only] NutsDeployCommand public descriptor
public NutsDeployCommand setDescriptor(descriptor)
```
#### 📄🎛 ids
return ids to deploy from source repository
```java
[read-only] NutsId[] public ids
public NutsId[] getIds()
```
#### ✏🎛 repository
set target repository to deploy to
```java
[write-only] NutsDeployCommand public repository
public NutsDeployCommand setRepository(repository)
```
#### 📄🎛 result
run command (if not yet run) and return result
```java
[read-only] NutsId[] public result
public NutsId[] getResult()
```
#### ✏🎛 session
update session
```java
[write-only] NutsDeployCommand public session
public NutsDeployCommand setSession(session)
```
#### 📝🎛 sha1
set content sha1 hash
```java
[read-write] NutsDeployCommand public sha1
public String getSha1()
public NutsDeployCommand setSha1(sha1)
```
#### ✏🎛 sourceRepository
set source repository to deploy from the given ids
```java
[write-only] NutsDeployCommand public sourceRepository
public NutsDeployCommand setSourceRepository(repository)
```
#### 📝🎛 targetRepository
set target repository to deploy to
```java
[read-write] NutsDeployCommand public targetRepository
public String getTargetRepository()
public NutsDeployCommand setTargetRepository(repository)
```
### ⚙ Instance Methods
#### ⚙ addId(id)
add id to deploy from source repository

```java
NutsDeployCommand addId(String id)
```
**return**:NutsDeployCommand
- **String id** : id to deploy from source repository

#### ⚙ addId(id)
add id to deploy from source repository

```java
NutsDeployCommand addId(NutsId id)
```
**return**:NutsDeployCommand
- **NutsId id** : id to deploy from source repository

#### ⚙ addIds(values)
add ids to deploy from source repository

```java
NutsDeployCommand addIds(NutsId[] values)
```
**return**:NutsDeployCommand
- **NutsId[] values** : ids to deploy from source repository

#### ⚙ addIds(values)
add ids to deploy from source repository

```java
NutsDeployCommand addIds(String[] values)
```
**return**:NutsDeployCommand
- **String[] values** : ids to deploy from source repository

#### ⚙ clearIds()
reset ids list to deploy

```java
NutsDeployCommand clearIds()
```
**return**:NutsDeployCommand

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsDeployCommand configure(boolean skipUnsupported, String[] args)
```
**return**:NutsDeployCommand
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ copySession()
copy session

```java
NutsDeployCommand copySession()
```
**return**:NutsDeployCommand

#### ⚙ from(repository)
set source repository to deploy from the given ids

```java
NutsDeployCommand from(String repository)
```
**return**:NutsDeployCommand
- **String repository** : source repository to deploy from

#### ⚙ removeId(id)
remove id to deploy from source repository

```java
NutsDeployCommand removeId(String id)
```
**return**:NutsDeployCommand
- **String id** : id to undo deploy from source repository

#### ⚙ removeId(id)
remove id to deploy from source repository

```java
NutsDeployCommand removeId(NutsId id)
```
**return**:NutsDeployCommand
- **NutsId id** : id to undo deploy from source repository

#### ⚙ run()
execute the command and return this instance

```java
NutsDeployCommand run()
```
**return**:NutsDeployCommand

#### ⚙ to(repository)
set target repository to deploy to

```java
NutsDeployCommand to(String repository)
```
**return**:NutsDeployCommand
- **String repository** : target repository to deploy to

## ☕ NutsExecCommand
```java
public interface net.vpc.app.nuts.NutsExecCommand
```
 Execute command.
 This class helps executing all types of executables : internal, external, alias and system

 \@author vpc
 \@since 0.5.4
 \@category Commands

### 🎛 Instance Properties
#### 📝🎛 command
set command artifact definition.
 The definition is expected to include content, dependencies, effective descriptor and install information.
```java
[read-write] NutsExecCommand public command
public String[] getCommand()
public NutsExecCommand setCommand(definition)
```
#### 📝🎛 directory
set execution directory
```java
[read-write] NutsExecCommand public directory
public String getDirectory()
public NutsExecCommand setDirectory(directory)
```
#### 📝🎛 dry
if true set dry execution
```java
[read-write] NutsExecCommand public dry
public boolean isDry()
public NutsExecCommand setDry(value)
```
#### 📝🎛 env
clear existing env and set new env
```java
[read-write] NutsExecCommand public env
public Map getEnv()
public NutsExecCommand setEnv(env)
```
#### 📝🎛 err
set new command error stream (standard error destination)
```java
[read-write] NutsExecCommand public err
public PrintStream getErr()
public NutsExecCommand setErr(err)
```
#### 📄🎛 errorString
return grabbed error after command execution
```java
[read-only] String public errorString
public String getErrorString()
```
#### 📝🎛 executionType
set execution type
```java
[read-write] NutsExecCommand public executionType
public NutsExecutionType getExecutionType()
public NutsExecCommand setExecutionType(executionType)
```
#### 📄🎛 executorOptions
return executor options
```java
[read-only] String[] public executorOptions
public String[] getExecutorOptions()
```
#### 📝🎛 failFast
when the execution returns a non zero result, an exception is
 thrown.Particularly, if grabOutputString is used, error exception will
 state the output message
```java
[read-write] NutsExecCommand public failFast
public boolean isFailFast()
public NutsExecCommand setFailFast(failFast)
```
#### 📝🎛 in
set new command input stream (standard input source)
```java
[read-write] NutsExecCommand public in
public InputStream getIn()
public NutsExecCommand setIn(in)
```
#### 📝🎛 out
set new command output stream (standard output destination)
```java
[read-write] NutsExecCommand public out
public PrintStream getOut()
public NutsExecCommand setOut(out)
```
#### 📄🎛 outputString
return grabbed output after command execution
```java
[read-only] String public outputString
public String getOutputString()
```
#### 📝🎛 redirectErrorStream
if true redirect standard error is redirected to standard output
```java
[read-write] NutsExecCommand public redirectErrorStream
public boolean isRedirectErrorStream()
public NutsExecCommand setRedirectErrorStream(redirectErrorStream)
```
#### 📄🎛 result
return result value. if not yet executed, will execute first.
```java
[read-only] int public result
public int getResult()
```
#### 📄🎛 resultException
return result exception or null
```java
[read-only] NutsExecutionException public resultException
public NutsExecutionException getResultException()
```
#### ✏🎛 session
update session
```java
[write-only] NutsExecCommand public session
public NutsExecCommand setSession(session)
```
### ⚙ Instance Methods
#### ⚙ addCommand(command)
append command arguments

```java
NutsExecCommand addCommand(String[] command)
```
**return**:NutsExecCommand
- **String[] command** : command

#### ⚙ addCommand(command)
append command arguments

```java
NutsExecCommand addCommand(Collection command)
```
**return**:NutsExecCommand
- **Collection command** : command

#### ⚙ addEnv(env)
merge env properties

```java
NutsExecCommand addEnv(Map env)
```
**return**:NutsExecCommand
- **Map env** : env properties

#### ⚙ addExecutorOption(executorOption)
append executor options

```java
NutsExecCommand addExecutorOption(String executorOption)
```
**return**:NutsExecCommand
- **String executorOption** : executor options

#### ⚙ addExecutorOptions(executorOptions)
append executor options

```java
NutsExecCommand addExecutorOptions(String[] executorOptions)
```
**return**:NutsExecCommand
- **String[] executorOptions** : executor options

#### ⚙ addExecutorOptions(executorOptions)
append executor options

```java
NutsExecCommand addExecutorOptions(Collection executorOptions)
```
**return**:NutsExecCommand
- **Collection executorOptions** : executor options

#### ⚙ clearCommand()
clear command

```java
NutsExecCommand clearCommand()
```
**return**:NutsExecCommand

#### ⚙ clearEnv()
clear env

```java
NutsExecCommand clearEnv()
```
**return**:NutsExecCommand

#### ⚙ clearExecutorOptions()
clear executor options

```java
NutsExecCommand clearExecutorOptions()
```
**return**:NutsExecCommand

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsExecCommand configure(boolean skipUnsupported, String[] args)
```
**return**:NutsExecCommand
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ copy()
create a copy of \{\@code this\} instance

```java
NutsExecCommand copy()
```
**return**:NutsExecCommand

#### ⚙ copyFrom(other)
copy all field from the given command into \{\@code this\} instance

```java
NutsExecCommand copyFrom(NutsExecCommand other)
```
**return**:NutsExecCommand
- **NutsExecCommand other** : command to copy from

#### ⚙ copySession()
copy session

```java
NutsExecCommand copySession()
```
**return**:NutsExecCommand

#### ⚙ embedded()
set embedded execution type

```java
NutsExecCommand embedded()
```
**return**:NutsExecCommand

#### ⚙ format()


```java
NutsExecCommandFormat format()
```
**return**:NutsExecCommandFormat

#### ⚙ grabErrorString()
grab to memory standard error

```java
NutsExecCommand grabErrorString()
```
**return**:NutsExecCommand

#### ⚙ grabOutputString()
grab to memory standard output

```java
NutsExecCommand grabOutputString()
```
**return**:NutsExecCommand

#### ⚙ rootCmd()
set root command execution type

```java
NutsExecCommand rootCmd()
```
**return**:NutsExecCommand

#### ⚙ run()
execute the command and return this instance

```java
NutsExecCommand run()
```
**return**:NutsExecCommand

#### ⚙ setEnv(key, value)
set or unset env property.
 the property is unset if the value is null.

```java
NutsExecCommand setEnv(String key, String value)
```
**return**:NutsExecCommand
- **String key** : env key
- **String value** : env value

#### ⚙ spawn()
set spawn execution type

```java
NutsExecCommand spawn()
```
**return**:NutsExecCommand

#### ⚙ userCmd()
set user command execution type

```java
NutsExecCommand userCmd()
```
**return**:NutsExecCommand

#### ⚙ which()
return executable information

```java
NutsExecutableInformation which()
```
**return**:NutsExecutableInformation

## ☕ NutsExecutionType
```java
public final net.vpc.app.nuts.NutsExecutionType
```
 Command execution type.
 \@author vpc
 \@since 0.5.4
 \@category Commands

### 📢❄ Constant Fields
#### 📢❄ EMBEDDED
```java
public static final NutsExecutionType EMBEDDED
```
#### 📢❄ ROOT_CMD
```java
public static final NutsExecutionType ROOT_CMD
```
#### 📢❄ SPAWN
```java
public static final NutsExecutionType SPAWN
```
#### 📢❄ USER_CMD
```java
public static final NutsExecutionType USER_CMD
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
NutsExecutionType valueOf(String name)
```
**return**:NutsExecutionType
- **String name** : 

#### 📢⚙ values()


```java
NutsExecutionType[] values()
```
**return**:NutsExecutionType[]

### ⚙ Instance Methods
#### ⚙ id()
lower cased identifier.

```java
String id()
```
**return**:String

## ☕ NutsFetchCommand
```java
public interface net.vpc.app.nuts.NutsFetchCommand
```
 Fetch command class helps fetching/retrieving a artifact with all of its
 files.

 \@author vpc
 \@since 0.5.4
 \@category Commands

### 🎛 Instance Properties
#### 📝🎛 cached
enable/disable retrieval from cache
```java
[read-write] NutsFetchCommand public cached
public boolean isCached()
public NutsFetchCommand setCached(enable)
```
#### 📝🎛 content
enable/disable retrieval of content info
```java
[read-write] NutsFetchCommand public content
public boolean isContent()
public NutsFetchCommand setContent(enable)
```
#### 📝🎛 dependencies
enable/disable dependencies list retrieval
```java
[read-write] NutsFetchCommand public dependencies
public boolean isDependencies()
public NutsFetchCommand setDependencies(enable)
```
#### 📝🎛 dependenciesTree
enable/disable dependencies tree retrieval
```java
[read-write] NutsFetchCommand public dependenciesTree
public boolean isDependenciesTree()
public NutsFetchCommand setDependenciesTree(enable)
```
#### 📝🎛 effective
enable/disable effective descriptor evaluation
```java
[read-write] NutsFetchCommand public effective
public boolean isEffective()
public NutsFetchCommand setEffective(enable)
```
#### 📝🎛 failFast
set armed (or disarmed) fail safe mode. if true, null replaces
 NutsNotFoundException.
```java
[read-write] NutsFetchCommand public failFast
public boolean isFailFast()
public NutsFetchCommand setFailFast(enable)
```
#### 📝🎛 fetchStrategy
set fetch strategy.
```java
[read-write] NutsFetchCommand public fetchStrategy
public NutsFetchStrategy getFetchStrategy()
public NutsFetchCommand setFetchStrategy(fetchStrategy)
```
#### 📝🎛 id
set id to fetch.
```java
[read-write] NutsFetchCommand public id
public NutsId getId()
public NutsFetchCommand setId(id)
```
#### 📝🎛 indexed
set index filter.if null index is removed. if false do not consider index. 
 if true, consider index.
```java
[read-write] NutsFetchCommand public indexed
public boolean isIndexed()
public NutsFetchCommand setIndexed(enable)
```
#### 📝🎛 installed
search for installed/non installed packages
```java
[read-write] NutsFetchCommand public installed
public Boolean getInstalled()
public NutsFetchCommand setInstalled(value)
```
#### 📝🎛 location
set locating where to fetch the artifact. If the location is a folder, a
 new name will be generated.
```java
[read-write] NutsFetchCommand public location
public Path getLocation()
public NutsFetchCommand setLocation(fileOrFolder)
```
#### 📝🎛 optional
set option filter. if null filter is removed. if false only non optional
 will be retrieved. if true, only optional will be retrieved.
```java
[read-write] NutsFetchCommand public optional
public Boolean getOptional()
public NutsFetchCommand setOptional(enable)
```
#### 📄🎛 resultContent
return result as content
```java
[read-only] NutsContent public resultContent
public NutsContent getResultContent()
```
#### 📄🎛 resultContentHash
return result as content hash string
```java
[read-only] String public resultContentHash
public String getResultContentHash()
```
#### 📄🎛 resultDefinition
return result as artifact definition
```java
[read-only] NutsDefinition public resultDefinition
public NutsDefinition getResultDefinition()
```
#### 📄🎛 resultDescriptor
return result as descriptor
```java
[read-only] NutsDescriptor public resultDescriptor
public NutsDescriptor getResultDescriptor()
```
#### 📄🎛 resultDescriptorHash
return result as descriptor hash string
```java
[read-only] String public resultDescriptorHash
public String getResultDescriptorHash()
```
#### 📄🎛 resultId
return result as id
```java
[read-only] NutsId public resultId
public NutsId getResultId()
```
#### 📄🎛 resultPath
return result as content path
```java
[read-only] Path public resultPath
public Path getResultPath()
```
#### 📄🎛 scope
dependencies scope filters
```java
[read-only] Set public scope
public Set getScope()
```
#### ✏🎛 session
update session
```java
[write-only] NutsFetchCommand public session
public NutsFetchCommand setSession(session)
```
#### 📝🎛 transitive
set or unset transitive mode
```java
[read-write] NutsFetchCommand public transitive
public boolean isTransitive()
public NutsFetchCommand setTransitive(enable)
```
### ⚙ Instance Methods
#### ⚙ addRepositories(value)
add repository filter

```java
NutsFetchCommand addRepositories(Collection value)
```
**return**:NutsFetchCommand
- **Collection value** : repository filter

#### ⚙ addRepositories(values)
add repository filter

```java
NutsFetchCommand addRepositories(String[] values)
```
**return**:NutsFetchCommand
- **String[] values** : repository filter

#### ⚙ addRepository(value)
add repository filter

```java
NutsFetchCommand addRepository(String value)
```
**return**:NutsFetchCommand
- **String value** : repository filter

#### ⚙ addScope(scope)
add dependency scope filter. Only relevant with \{\@link #setDependencies(boolean)
 \} and \{\@link #setDependenciesTree(boolean)\}

```java
NutsFetchCommand addScope(NutsDependencyScopePattern scope)
```
**return**:NutsFetchCommand
- **NutsDependencyScopePattern scope** : scope filter

#### ⚙ addScope(scope)
add dependency scope filter. Only relevant with \{\@link #setDependencies(boolean)
 \} and \{\@link #setDependenciesTree(boolean)\}

```java
NutsFetchCommand addScope(NutsDependencyScope scope)
```
**return**:NutsFetchCommand
- **NutsDependencyScope scope** : scope filter

#### ⚙ addScopes(scope)
add dependency scope filter. Only relevant with \{\@link #setDependencies(boolean)
 \} and \{\@link #setDependenciesTree(boolean)\}

```java
NutsFetchCommand addScopes(NutsDependencyScope[] scope)
```
**return**:NutsFetchCommand
- **NutsDependencyScope[] scope** : scope filter

#### ⚙ addScopes(scope)
add dependency scope filter. Only relevant with \{\@link #setDependencies(boolean)\}
 and \{\@link #setDependenciesTree(boolean)\}

```java
NutsFetchCommand addScopes(NutsDependencyScopePattern[] scope)
```
**return**:NutsFetchCommand
- **NutsDependencyScopePattern[] scope** : scope filter

#### ⚙ clearRepositories()
remove all repository filters

```java
NutsFetchCommand clearRepositories()
```
**return**:NutsFetchCommand

#### ⚙ clearScopes()
remove all dependency scope filters.

```java
NutsFetchCommand clearScopes()
```
**return**:NutsFetchCommand

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...)
 \}
 to help return a more specific return type;

```java
NutsFetchCommand configure(boolean skipUnsupported, String[] args)
```
**return**:NutsFetchCommand
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ copy()
create copy (new instance) of \{\@code this\} command

```java
NutsFetchCommand copy()
```
**return**:NutsFetchCommand

#### ⚙ copyFrom(other)
copy into \{\@code this\} from \{\@code other\} fetch command

```java
NutsFetchCommand copyFrom(NutsFetchCommand other)
```
**return**:NutsFetchCommand
- **NutsFetchCommand other** : copy into {@code this} from {@code other} fetch command

#### ⚙ copySession()
copy session

```java
NutsFetchCommand copySession()
```
**return**:NutsFetchCommand

#### ⚙ installed()
search for non installed packages

```java
NutsFetchCommand installed()
```
**return**:NutsFetchCommand

#### ⚙ installed(value)
search for installed/non installed packages

```java
NutsFetchCommand installed(Boolean value)
```
**return**:NutsFetchCommand
- **Boolean value** : new value

#### ⚙ notInstalled()
search for non installed packages

```java
NutsFetchCommand notInstalled()
```
**return**:NutsFetchCommand

#### ⚙ removeRepository(value)
remove repository filter

```java
NutsFetchCommand removeRepository(String value)
```
**return**:NutsFetchCommand
- **String value** : repository filter

#### ⚙ removeScope(scope)
remove dependency scope filter.

```java
NutsFetchCommand removeScope(NutsDependencyScope scope)
```
**return**:NutsFetchCommand
- **NutsDependencyScope scope** : scope filter

#### ⚙ removeScope(scope)
remove dependency scope filter.

```java
NutsFetchCommand removeScope(NutsDependencyScopePattern scope)
```
**return**:NutsFetchCommand
- **NutsDependencyScopePattern scope** : scope filter

#### ⚙ run()
execute the command and return this instance

```java
NutsFetchCommand run()
```
**return**:NutsFetchCommand

#### ⚙ setAnyWhere()
all artifacts (local and remote). If local result found will any way
 fetch remote.

```java
NutsFetchCommand setAnyWhere()
```
**return**:NutsFetchCommand

#### ⚙ setDefaultLocation()
unset location to store to fetched id and fall back to default location.

```java
NutsFetchCommand setDefaultLocation()
```
**return**:NutsFetchCommand

#### ⚙ setNutsApi()
set id to fetch to nuts-api (api artifact)

```java
NutsFetchCommand setNutsApi()
```
**return**:NutsFetchCommand

#### ⚙ setNutsRuntime()
set id to fetch to nuts-core (runtime artifact)

```java
NutsFetchCommand setNutsRuntime()
```
**return**:NutsFetchCommand

#### ⚙ setOffline()
local only (installed or not)

```java
NutsFetchCommand setOffline()
```
**return**:NutsFetchCommand

#### ⚙ setOnline()
local or remote. If local result found will not fetch remote.

```java
NutsFetchCommand setOnline()
```
**return**:NutsFetchCommand

#### ⚙ setRemote()
remote only

```java
NutsFetchCommand setRemote()
```
**return**:NutsFetchCommand

## ☕ NutsFetchMode
```java
public final net.vpc.app.nuts.NutsFetchMode
```
 fetch mode defines if the artifact should be looked for withing the "installed" meta repository, "local" (offline)
 machine repositories or over the wire (remote repositories).

 \<p\>
 "installed" artifacts are stored in a pseudo-repository called "installed" which include all installed
 (using command install) artifacts. Effective storage may (should?) remain in a local repository though.
 Actually pseudo-repository "installed" manages references to these storages.
 \</p\>
 \<p\>
 local repositories include all local folder based repositories. Semantically they should define machine/node based
 storage that is independent from LAN/WAN/Cloud networks. A local database based repository may be considered as local
 though not recommended as the server may be down.
 Il all ways, local repositories are considered fast according to fetch/deploy commands.
 \</p\>
 \<p\>
 remote repositories include all non local repositories which may present slow access and connectivity issues.
 Typically this include server based repositories (http, ...).
 \</p\>
 \<p\>
 It is important to say that a repository may serve both local and remote artifacts as usually remote repositories
 enable cache support; in which case, if the artifact si cached, it will be accessed locally.
 \</p\>
 \@since 0.5.4
 \@category Commands

### 📢❄ Constant Fields
#### 📢❄ LOCAL
```java
public static final NutsFetchMode LOCAL
```
#### 📢❄ REMOTE
```java
public static final NutsFetchMode REMOTE
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
NutsFetchMode valueOf(String name)
```
**return**:NutsFetchMode
- **String name** : 

#### 📢⚙ values()


```java
NutsFetchMode[] values()
```
**return**:NutsFetchMode[]

### ⚙ Instance Methods
#### ⚙ id()
lower cased identifier.

```java
String id()
```
**return**:String

## ☕ NutsFetchStrategy
```java
public final net.vpc.app.nuts.NutsFetchStrategy
```
 Fetch strategy defines modes (see \{\@link NutsFetchMode\}) to use when searching for an artifact.
 \@author vpc
 \@since 0.5.4
 \@category Commands

### 📢❄ Constant Fields
#### 📢❄ ANYWHERE
```java
public static final NutsFetchStrategy ANYWHERE
```
#### 📢❄ OFFLINE
```java
public static final NutsFetchStrategy OFFLINE
```
#### 📢❄ ONLINE
```java
public static final NutsFetchStrategy ONLINE
```
#### 📢❄ REMOTE
```java
public static final NutsFetchStrategy REMOTE
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
NutsFetchStrategy valueOf(String name)
```
**return**:NutsFetchStrategy
- **String name** : 

#### 📢⚙ values()


```java
NutsFetchStrategy[] values()
```
**return**:NutsFetchStrategy[]

### 🎛 Instance Properties
#### 📄🎛 stopFast
if true, do not consider next Fetch mode if the latter gives at least one result.
```java
[read-only] boolean public stopFast
public boolean isStopFast()
```
### ⚙ Instance Methods
#### ⚙ id()
lower cased identifier.

```java
String id()
```
**return**:String

#### ⚙ iterator()
ordered fetch modes iterator

```java
Iterator iterator()
```
**return**:Iterator

#### ⚙ modes()
ordered fetch modes

```java
NutsFetchMode[] modes()
```
**return**:NutsFetchMode[]

## ☕ NutsPushCommand
```java
public interface net.vpc.app.nuts.NutsPushCommand
```
 Push command
 \@author vpc
 \@since 0.5.4
 \@category Commands

### 🎛 Instance Properties
#### 📄🎛 args
return all arguments to pass to the push command
```java
[read-only] String[] public args
public String[] getArgs()
```
#### 📄🎛 ids
return ids to push for
```java
[read-only] NutsId[] public ids
public NutsId[] getIds()
```
#### 📄🎛 lockedIds
return locked ids to prevent them to be updated or the force other ids to use them (the installed version).
```java
[read-only] NutsId[] public lockedIds
public NutsId[] getLockedIds()
```
#### 📝🎛 offline
local only (installed or not)
```java
[read-write] NutsPushCommand public offline
public boolean isOffline()
public NutsPushCommand setOffline(offline)
```
#### 📝🎛 repository
repository to push from
```java
[read-write] NutsPushCommand public repository
public String getRepository()
public NutsPushCommand setRepository(repository)
```
#### ✏🎛 session
update session
```java
[write-only] NutsPushCommand public session
public NutsPushCommand setSession(session)
```
### ⚙ Instance Methods
#### ⚙ addArg(arg)
add argument to pass to the push command

```java
NutsPushCommand addArg(String arg)
```
**return**:NutsPushCommand
- **String arg** : argument

#### ⚙ addArgs(args)
add arguments to pass to the push command

```java
NutsPushCommand addArgs(String[] args)
```
**return**:NutsPushCommand
- **String[] args** : argument

#### ⚙ addArgs(args)
add arguments to pass to the push command

```java
NutsPushCommand addArgs(Collection args)
```
**return**:NutsPushCommand
- **Collection args** : argument

#### ⚙ addId(id)
add id to push.

```java
NutsPushCommand addId(NutsId id)
```
**return**:NutsPushCommand
- **NutsId id** : id to push

#### ⚙ addId(id)
add id to push.

```java
NutsPushCommand addId(String id)
```
**return**:NutsPushCommand
- **String id** : id to push

#### ⚙ addIds(ids)
add ids to push.

```java
NutsPushCommand addIds(NutsId[] ids)
```
**return**:NutsPushCommand
- **NutsId[] ids** : id to push

#### ⚙ addIds(ids)
add ids to push.

```java
NutsPushCommand addIds(String[] ids)
```
**return**:NutsPushCommand
- **String[] ids** : id to push

#### ⚙ addLockedId(id)
add locked ids to prevent them to be updated or the force other ids to use them (the installed version).

```java
NutsPushCommand addLockedId(NutsId id)
```
**return**:NutsPushCommand
- **NutsId id** : id to lock

#### ⚙ addLockedId(id)
add locked ids to prevent them to be updated or the force other ids to use them (the installed version).

```java
NutsPushCommand addLockedId(String id)
```
**return**:NutsPushCommand
- **String id** : id to lock

#### ⚙ addLockedIds(values)
add locked ids to prevent them to be updated or the force other ids to use them (the installed version).

```java
NutsPushCommand addLockedIds(NutsId[] values)
```
**return**:NutsPushCommand
- **NutsId[] values** : id to lock

#### ⚙ addLockedIds(values)
define locked ids to prevent them to be updated or the force other ids to use them (the installed version).

```java
NutsPushCommand addLockedIds(String[] values)
```
**return**:NutsPushCommand
- **String[] values** : ids

#### ⚙ arg(arg)
add argument to pass to the push command

```java
NutsPushCommand arg(String arg)
```
**return**:NutsPushCommand
- **String arg** : argument

#### ⚙ args(args)
add arguments to pass to the push command

```java
NutsPushCommand args(String[] args)
```
**return**:NutsPushCommand
- **String[] args** : argument

#### ⚙ args(args)
add arguments to pass to the push command

```java
NutsPushCommand args(Collection args)
```
**return**:NutsPushCommand
- **Collection args** : argument

#### ⚙ clearArgs()
clear all arguments to pass to the push command

```java
NutsPushCommand clearArgs()
```
**return**:NutsPushCommand

#### ⚙ clearIds()
reset ids to push for

```java
NutsPushCommand clearIds()
```
**return**:NutsPushCommand

#### ⚙ clearLockedIds()
reset locked ids

```java
NutsPushCommand clearLockedIds()
```
**return**:NutsPushCommand

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsPushCommand configure(boolean skipUnsupported, String[] args)
```
**return**:NutsPushCommand
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ copySession()
copy session

```java
NutsPushCommand copySession()
```
**return**:NutsPushCommand

#### ⚙ id(id)
add id to push.

```java
NutsPushCommand id(NutsId id)
```
**return**:NutsPushCommand
- **NutsId id** : id to push

#### ⚙ id(id)
add id to push.

```java
NutsPushCommand id(String id)
```
**return**:NutsPushCommand
- **String id** : id to push

#### ⚙ ids(ids)
add ids to push.

```java
NutsPushCommand ids(NutsId[] ids)
```
**return**:NutsPushCommand
- **NutsId[] ids** : id to push

#### ⚙ ids(ids)
add ids to push.

```java
NutsPushCommand ids(String[] ids)
```
**return**:NutsPushCommand
- **String[] ids** : id to push

#### ⚙ lockedId(id)
add locked ids to prevent them to be updated or the force other ids to use them (the installed version).

```java
NutsPushCommand lockedId(NutsId id)
```
**return**:NutsPushCommand
- **NutsId id** : id to lock

#### ⚙ lockedId(id)
add locked ids to prevent them to be updated or the force other ids to use them (the installed version).

```java
NutsPushCommand lockedId(String id)
```
**return**:NutsPushCommand
- **String id** : id to lock

#### ⚙ lockedIds(values)
define locked ids to prevent them to be updated or the force other ids to use them (the installed version).

```java
NutsPushCommand lockedIds(NutsId[] values)
```
**return**:NutsPushCommand
- **NutsId[] values** : ids

#### ⚙ lockedIds(values)
define locked ids to prevent them to be updated or the force other ids to use them (the installed version).

```java
NutsPushCommand lockedIds(String[] values)
```
**return**:NutsPushCommand
- **String[] values** : ids

#### ⚙ offline()
local only (installed or not)

```java
NutsPushCommand offline()
```
**return**:NutsPushCommand

#### ⚙ offline(offline)
local only (installed or not)

```java
NutsPushCommand offline(boolean offline)
```
**return**:NutsPushCommand
- **boolean offline** : enable offline mode

#### ⚙ removeId(id)
remove id to push.

```java
NutsPushCommand removeId(NutsId id)
```
**return**:NutsPushCommand
- **NutsId id** : id to push

#### ⚙ removeId(id)
remove id to push.

```java
NutsPushCommand removeId(String id)
```
**return**:NutsPushCommand
- **String id** : id to push

#### ⚙ removeLockedId(id)
remove locked ids to prevent them to be updated or the force other ids to use them (the installed version).

```java
NutsPushCommand removeLockedId(NutsId id)
```
**return**:NutsPushCommand
- **NutsId id** : id to unlock

#### ⚙ removeLockedId(id)
remove locked ids to prevent them to be updated or the force other ids to use them (the installed version).

```java
NutsPushCommand removeLockedId(String id)
```
**return**:NutsPushCommand
- **String id** : id to unlock

#### ⚙ repository(repository)
repository to push from

```java
NutsPushCommand repository(String repository)
```
**return**:NutsPushCommand
- **String repository** : repository to push from

#### ⚙ run()
execute the command and return this instance

```java
NutsPushCommand run()
```
**return**:NutsPushCommand

## ☕ NutsRemoveOptions
```java
public net.vpc.app.nuts.NutsRemoveOptions
```

 \@author vpc
 \@since 0.5.4
 \@category Commands

### 🪄 Constructors
#### 🪄 NutsRemoveOptions()


```java
NutsRemoveOptions()
```

### 🎛 Instance Properties
#### 📄🎛 erase

```java
[read-only] boolean public erase
public boolean isErase()
```
#### 📄🎛 session

```java
[read-only] NutsSession public session
public NutsSession getSession()
```
### ⚙ Instance Methods
#### ⚙ equals(o)


```java
boolean equals(Object o)
```
**return**:boolean
- **Object o** : 

#### ⚙ erase()


```java
NutsRemoveOptions erase()
```
**return**:NutsRemoveOptions

#### ⚙ erase(erase)


```java
NutsRemoveOptions erase(boolean erase)
```
**return**:NutsRemoveOptions
- **boolean erase** : 

#### ⚙ hashCode()


```java
int hashCode()
```
**return**:int

#### ⚙ setErase(erase)


```java
NutsRemoveOptions setErase(boolean erase)
```
**return**:NutsRemoveOptions
- **boolean erase** : 

#### ⚙ setSession(session)


```java
NutsRemoveOptions setSession(NutsSession session)
```
**return**:NutsRemoveOptions
- **NutsSession session** : 

#### ⚙ toString()


```java
String toString()
```
**return**:String

## ☕ NutsSearchCommand
```java
public interface net.vpc.app.nuts.NutsSearchCommand
```
 Search command class helps searching multiple artifacts with all of their
 files.

 \@author vpc
 \@since 0.5.4
 \@category Commands

### 🎛 Instance Properties
#### 📝🎛 application
set nuts app filter. if true nuts app (implementing NutsApplication) only
 are retrieved.
```java
[read-write] NutsSearchCommand public application
public boolean isApplication()
public NutsSearchCommand setApplication(enable)
```
#### 📄🎛 arch

```java
[read-only] String[] public arch
public String[] getArch()
```
#### 📝🎛 basePackage
include base package when searching for inlined dependencies
```java
[read-write] NutsSearchCommand public basePackage
public boolean isBasePackage()
public NutsSearchCommand setBasePackage(includeBasePackage)
```
#### 📝🎛 cached
enable/disable retrieval from cache
```java
[read-write] NutsSearchCommand public cached
public boolean isCached()
public NutsSearchCommand setCached(enable)
```
#### 📝🎛 companion
set companions filter. if true companions only are retrieved.
```java
[read-write] NutsSearchCommand public companion
public boolean isCompanion()
public NutsSearchCommand setCompanion(enable)
```
#### 📄🎛 comparator
result comparator
```java
[read-only] Comparator public comparator
public Comparator getComparator()
```
#### 📝🎛 content
enable/disable retrieval of content info
```java
[read-write] NutsSearchCommand public content
public boolean isContent()
public NutsSearchCommand setContent(enable)
```
#### 📝🎛 defaultVersions
default version only filter
```java
[read-write] NutsSearchCommand public defaultVersions
public Boolean getDefaultVersions()
public NutsSearchCommand setDefaultVersions(enable)
```
#### 📝🎛 dependencies
enable/disable dependencies list retrieval
```java
[read-write] NutsSearchCommand public dependencies
public boolean isDependencies()
public NutsSearchCommand setDependencies(enable)
```
#### 📝🎛 dependenciesTree
enable/disable dependencies tree retrieval
```java
[read-write] NutsSearchCommand public dependenciesTree
public boolean isDependenciesTree()
public NutsSearchCommand setDependenciesTree(enable)
```
#### 📝🎛 dependencyFilter
define dependency filter. applicable when using \{\@link #setInlineDependencies(boolean)\}
```java
[read-write] NutsSearchCommand public dependencyFilter
public NutsDependencyFilter getDependencyFilter()
public NutsSearchCommand setDependencyFilter(filter)
```
#### 📝🎛 descriptorFilter
define descriptor filter.
```java
[read-write] NutsSearchCommand public descriptorFilter
public NutsDescriptorFilter getDescriptorFilter()
public NutsSearchCommand setDescriptorFilter(filter)
```
#### 📝🎛 distinct
skip duplicates
```java
[read-write] NutsSearchCommand public distinct
public boolean isDistinct()
public NutsSearchCommand setDistinct(distinct)
```
#### 📝🎛 effective
enable/disable effective descriptor evaluation
```java
[read-write] NutsSearchCommand public effective
public boolean isEffective()
public NutsSearchCommand setEffective(enable)
```
#### 📝🎛 exec
set app filter. if true non lib (app) only are retrieved.
```java
[read-write] NutsSearchCommand public exec
public boolean isExec()
public NutsSearchCommand setExec(enable)
```
#### 📝🎛 extension
set extensions filter. if true extensions only are retrieved.
```java
[read-write] NutsSearchCommand public extension
public boolean isExtension()
public NutsSearchCommand setExtension(enable)
```
#### 📝🎛 failFast
set armed (or disarmed) fail safe mode. if true, null replaces
 NutsNotFoundException.
```java
[read-write] NutsSearchCommand public failFast
public boolean isFailFast()
public NutsSearchCommand setFailFast(enable)
```
#### 📝🎛 fetchStrategy
set fetch strategy.
```java
[read-write] NutsSearchCommand public fetchStrategy
public NutsFetchStrategy getFetchStrategy()
public NutsSearchCommand setFetchStrategy(fetchStrategy)
```
#### 📝🎛 idFilter
define id filter.
```java
[read-write] NutsSearchCommand public idFilter
public NutsIdFilter getIdFilter()
public NutsSearchCommand setIdFilter(filter)
```
#### 📄🎛 ids
return ids to search for
```java
[read-only] NutsId[] public ids
public NutsId[] getIds()
```
#### 📝🎛 indexed
set index filter.if null index is removed. if false do not consider
 index. if true, consider index.
```java
[read-write] NutsSearchCommand public indexed
public boolean isIndexed()
public NutsSearchCommand setIndexed(enable)
```
#### 📝🎛 inlineDependencies
enable/disable inlined dependencies list retrieval
```java
[read-write] NutsSearchCommand public inlineDependencies
public boolean isInlineDependencies()
public NutsSearchCommand setInlineDependencies(enable)
```
#### 📝🎛 installStatus
search for non packages with the given \{\@code installStatus\}
```java
[read-write] NutsSearchCommand public installStatus
public NutsInstallStatus getInstallStatus()
public NutsSearchCommand setInstallStatus(installStatus)
```
#### 📝🎛 latest
if true search must return only latest versions for each artifact id
```java
[read-write] NutsSearchCommand public latest
public boolean isLatest()
public NutsSearchCommand setLatest(enable)
```
#### 📝🎛 lib
set lib filter. if true lib (non app) only are retrieved.
```java
[read-write] NutsSearchCommand public lib
public boolean isLib()
public NutsSearchCommand setLib(enable)
```
#### 📝🎛 location
set locating where to fetch the artifact. If the location is a folder, a
 new name will be generated.
```java
[read-write] NutsSearchCommand public location
public Path getLocation()
public NutsSearchCommand setLocation(fileOrFolder)
```
#### 📄🎛 lockedIds
return locked ids to prevent them to be updated or the force other ids to use them (the installed version).
```java
[read-only] NutsId[] public lockedIds
public NutsId[] getLockedIds()
```
#### 📝🎛 optional
set option filter. if null filter is removed. if false only non optional
 will be retrieved. if true, only optional will be retrieved.
```java
[read-write] NutsSearchCommand public optional
public Boolean getOptional()
public NutsSearchCommand setOptional(enable)
```
#### 📄🎛 packaging

```java
[read-only] String[] public packaging
public String[] getPackaging()
```
#### 📝🎛 printResult
enable print search result
```java
[read-write] NutsSearchCommand public printResult
public boolean isPrintResult()
public NutsSearchCommand setPrintResult(enable)
```
#### 📄🎛 repositories

```java
[read-only] String[] public repositories
public String[] getRepositories()
```
#### ✏🎛 repository
define repository filter.
```java
[write-only] NutsSearchCommand public repository
public NutsSearchCommand setRepository(filter)
```
#### 📝🎛 repositoryFilter
define repository filter.
```java
[read-write] NutsSearchCommand public repositoryFilter
public NutsRepositoryFilter getRepositoryFilter()
public NutsSearchCommand setRepositoryFilter(filter)
```
#### 📄🎛 resultArchs
return result as archs
```java
[read-only] NutsResultList public resultArchs
public NutsResultList getResultArchs()
```
#### 📄🎛 resultClassLoader
execute query and return result as class loader
```java
[read-only] ClassLoader public resultClassLoader
public ClassLoader getResultClassLoader()
```
#### 📄🎛 resultClassPath
execute query and return result as class path string
```java
[read-only] String public resultClassPath
public String getResultClassPath()
```
#### 📄🎛 resultDefinitions
execute query and return result as definitions
```java
[read-only] NutsResultList public resultDefinitions
public NutsResultList getResultDefinitions()
```
#### 📄🎛 resultExecutionEntries
return result as execution entries
```java
[read-only] NutsResultList public resultExecutionEntries
public NutsResultList getResultExecutionEntries()
```
#### 📄🎛 resultIds
execute query and return result as ids
```java
[read-only] NutsResultList public resultIds
public NutsResultList getResultIds()
```
#### 📄🎛 resultInstallDates
execute query and return install dates
```java
[read-only] NutsResultList public resultInstallDates
public NutsResultList getResultInstallDates()
```
#### 📄🎛 resultInstallFolders
execute query and return install folders
```java
[read-only] NutsResultList public resultInstallFolders
public NutsResultList getResultInstallFolders()
```
#### 📄🎛 resultInstallUsers
execute query and return install users
```java
[read-only] NutsResultList public resultInstallUsers
public NutsResultList getResultInstallUsers()
```
#### 📄🎛 resultNames
return result as artifact names
```java
[read-only] NutsResultList public resultNames
public NutsResultList getResultNames()
```
#### 📄🎛 resultNutsPath
execute query and return result as nuts path string
```java
[read-only] String public resultNutsPath
public String getResultNutsPath()
```
#### 📄🎛 resultOsdists
return result as osdist names
```java
[read-only] NutsResultList public resultOsdists
public NutsResultList getResultOsdists()
```
#### 📄🎛 resultOses
return result as operating system names
```java
[read-only] NutsResultList public resultOses
public NutsResultList getResultOses()
```
#### 📄🎛 resultPackagings
return result as packagings
```java
[read-only] NutsResultList public resultPackagings
public NutsResultList getResultPackagings()
```
#### 📄🎛 resultPathNames
return result as content path names
```java
[read-only] NutsResultList public resultPathNames
public NutsResultList getResultPathNames()
```
#### 📄🎛 resultPaths
return result as content paths
```java
[read-only] NutsResultList public resultPaths
public NutsResultList getResultPaths()
```
#### 📄🎛 resultPlatforms
return result as platforms
```java
[read-only] NutsResultList public resultPlatforms
public NutsResultList getResultPlatforms()
```
#### 📝🎛 runtime
add runtime id to search
```java
[read-write] NutsSearchCommand public runtime
public boolean isRuntime()
public NutsSearchCommand setRuntime(enable)
```
#### 📄🎛 scope
scope filter filter. applicable with \{\@link #setInlineDependencies(boolean)\}
```java
[read-only] Set public scope
public Set getScope()
```
#### 📄🎛 scripts
return javascript filters
```java
[read-only] String[] public scripts
public String[] getScripts()
```
#### ✏🎛 session
update session
```java
[write-only] NutsSearchCommand public session
public NutsSearchCommand setSession(session)
```
#### 📝🎛 sorted
sort result
```java
[read-write] NutsSearchCommand public sorted
public boolean isSorted()
public NutsSearchCommand setSorted(sort)
```
#### 📝🎛 targetApiVersion
set target api version
```java
[read-write] NutsSearchCommand public targetApiVersion
public String getTargetApiVersion()
public NutsSearchCommand setTargetApiVersion(targetApiVersion)
```
#### 📝🎛 transitive
set or unset transitive mode
```java
[read-write] NutsSearchCommand public transitive
public boolean isTransitive()
public NutsSearchCommand setTransitive(enable)
```
### ⚙ Instance Methods
#### ⚙ addArch(value)
add arch to search

```java
NutsSearchCommand addArch(String value)
```
**return**:NutsSearchCommand
- **String value** : arch to search for

#### ⚙ addArchs(values)
add archs to search

```java
NutsSearchCommand addArchs(Collection values)
```
**return**:NutsSearchCommand
- **Collection values** : arch to search for

#### ⚙ addArchs(values)
add archs to search

```java
NutsSearchCommand addArchs(String[] values)
```
**return**:NutsSearchCommand
- **String[] values** : arch to search for

#### ⚙ addId(id)
add id to search.

```java
NutsSearchCommand addId(String id)
```
**return**:NutsSearchCommand
- **String id** : id to search

#### ⚙ addId(id)
add id to search.

```java
NutsSearchCommand addId(NutsId id)
```
**return**:NutsSearchCommand
- **NutsId id** : id to search

#### ⚙ addIds(ids)
add ids to search.

```java
NutsSearchCommand addIds(String[] ids)
```
**return**:NutsSearchCommand
- **String[] ids** : id to search

#### ⚙ addIds(ids)
add ids to search.

```java
NutsSearchCommand addIds(NutsId[] ids)
```
**return**:NutsSearchCommand
- **NutsId[] ids** : ids to search

#### ⚙ addLockedId(id)
add locked ids to prevent them to be updated or the force other ids to use them (the installed version).

```java
NutsSearchCommand addLockedId(NutsId id)
```
**return**:NutsSearchCommand
- **NutsId id** : id to lock

#### ⚙ addLockedId(id)
add locked ids to prevent them to be updated or the force other ids to use them (the installed version).

```java
NutsSearchCommand addLockedId(String id)
```
**return**:NutsSearchCommand
- **String id** : id to lock

#### ⚙ addLockedIds(values)
define locked ids to prevent them to be updated or the force other ids to use them (the installed version).

```java
NutsSearchCommand addLockedIds(String[] values)
```
**return**:NutsSearchCommand
- **String[] values** : ids

#### ⚙ addLockedIds(values)
define locked ids to prevent them to be updated or the force other ids to use them (the installed version).

```java
NutsSearchCommand addLockedIds(NutsId[] values)
```
**return**:NutsSearchCommand
- **NutsId[] values** : ids

#### ⚙ addPackaging(value)
add packaging to search

```java
NutsSearchCommand addPackaging(String value)
```
**return**:NutsSearchCommand
- **String value** : packaging to search for

#### ⚙ addPackagings(values)
add packagings to search

```java
NutsSearchCommand addPackagings(Collection values)
```
**return**:NutsSearchCommand
- **Collection values** : packagings to search for

#### ⚙ addPackagings(values)
add packagings to search

```java
NutsSearchCommand addPackagings(String[] values)
```
**return**:NutsSearchCommand
- **String[] values** : packagings to search for

#### ⚙ addRepositories(values)
add repositories to search into

```java
NutsSearchCommand addRepositories(Collection values)
```
**return**:NutsSearchCommand
- **Collection values** : repositories to search into

#### ⚙ addRepositories(values)
add repositories to search into

```java
NutsSearchCommand addRepositories(String[] values)
```
**return**:NutsSearchCommand
- **String[] values** : repositories to search into

#### ⚙ addRepository(value)
add repository to search into

```java
NutsSearchCommand addRepository(String value)
```
**return**:NutsSearchCommand
- **String value** : repository to search into

#### ⚙ addScope(scope)
add dependency scope filter. Only relevant with \{\@link #setDependencies(boolean)\}
 and \{\@link #setDependenciesTree(boolean)\}

```java
NutsSearchCommand addScope(NutsDependencyScope scope)
```
**return**:NutsSearchCommand
- **NutsDependencyScope scope** : scope filter

#### ⚙ addScope(scope)
add dependency scope filter. Only relevant with \{\@link #setDependencies(boolean)\}
 and \{\@link #setDependenciesTree(boolean)\}

```java
NutsSearchCommand addScope(NutsDependencyScopePattern scope)
```
**return**:NutsSearchCommand
- **NutsDependencyScopePattern scope** : scope filter

#### ⚙ addScopes(scope)
add dependency scope filter. Only relevant with \{\@link #setDependencies(boolean)\}
 and \{\@link #setDependenciesTree(boolean)\}

```java
NutsSearchCommand addScopes(NutsDependencyScope[] scope)
```
**return**:NutsSearchCommand
- **NutsDependencyScope[] scope** : scope filter

#### ⚙ addScopes(scope)
add dependency scope filter. Only relevant with \{\@link #setDependencies(boolean)\}
 and \{\@link #setDependenciesTree(boolean)\}

```java
NutsSearchCommand addScopes(NutsDependencyScopePattern[] scope)
```
**return**:NutsSearchCommand
- **NutsDependencyScopePattern[] scope** : scope filter

#### ⚙ addScript(value)
add javascript filter.

```java
NutsSearchCommand addScript(String value)
```
**return**:NutsSearchCommand
- **String value** : javascript filter

#### ⚙ addScripts(value)
add javascript filter.

```java
NutsSearchCommand addScripts(Collection value)
```
**return**:NutsSearchCommand
- **Collection value** : javascript filter

#### ⚙ addScripts(value)
add javascript filter.

```java
NutsSearchCommand addScripts(String[] value)
```
**return**:NutsSearchCommand
- **String[] value** : javascript filter

#### ⚙ clearArchs()
reset searched for archs

```java
NutsSearchCommand clearArchs()
```
**return**:NutsSearchCommand

#### ⚙ clearIds()
reset ids to search for

```java
NutsSearchCommand clearIds()
```
**return**:NutsSearchCommand

#### ⚙ clearLockedIds()
reset locked ids

```java
NutsSearchCommand clearLockedIds()
```
**return**:NutsSearchCommand

#### ⚙ clearPackagings()
reset packagings to search

```java
NutsSearchCommand clearPackagings()
```
**return**:NutsSearchCommand

#### ⚙ clearRepositories()
reset repositories to search into

```java
NutsSearchCommand clearRepositories()
```
**return**:NutsSearchCommand

#### ⚙ clearScopes()
remove all dependency scope filters.

```java
NutsSearchCommand clearScopes()
```
**return**:NutsSearchCommand

#### ⚙ clearScripts()
remove all javascript filters

```java
NutsSearchCommand clearScripts()
```
**return**:NutsSearchCommand

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...)
 \}
 to help return a more specific return type;

```java
NutsSearchCommand configure(boolean skipUnsupported, String[] args)
```
**return**:NutsSearchCommand
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ copy()
create new instance copy of this

```java
NutsSearchCommand copy()
```
**return**:NutsSearchCommand

#### ⚙ copyFrom(other)
copy content from given \{\@code other\}

```java
NutsSearchCommand copyFrom(NutsSearchCommand other)
```
**return**:NutsSearchCommand
- **NutsSearchCommand other** : other instance

#### ⚙ copyFrom(other)
copy content from given \{\@code other\}

```java
NutsSearchCommand copyFrom(NutsFetchCommand other)
```
**return**:NutsSearchCommand
- **NutsFetchCommand other** : other instance

#### ⚙ copySession()
copy session

```java
NutsSearchCommand copySession()
```
**return**:NutsSearchCommand

#### ⚙ getResultClassLoader(parent)
execute query and return result as class loader

```java
ClassLoader getResultClassLoader(ClassLoader parent)
```
**return**:ClassLoader
- **ClassLoader parent** : parent class loader

#### ⚙ getResultStoreLocations(location)
execute query and return store location path

```java
NutsResultList getResultStoreLocations(NutsStoreLocation location)
```
**return**:NutsResultList
- **NutsStoreLocation location** : location type to return

#### ⚙ getResultStrings(columns)
execute query and return the selected columns.
 Supported columns are :
 \<ul\>
     \<li\>all\</li\>
     \<li\>long\</li\>
     \<li\>status\</li\>
     \<li\>install-date\</li\>
     \<li\>install-user\</li\>
     \<li\>install-folder\</li\>
     \<li\>repository\</li\>
     \<li\>repository-id\</li\>
     \<li\>id\</li\>
     \<li\>name\</li\>
     \<li\>arch\</li\>
     \<li\>packaging\</li\>
     \<li\>platform\</li\>
     \<li\>os\</li\>
     \<li\>osdist\</li\>
     \<li\>exec-entry\</li\>
     \<li\>file-name\</li\>
     \<li\>file\</li\>
     \<li\>var-location\</li\>
     \<li\>temp-folder\</li\>
     \<li\>config-folder\</li\>
     \<li\>lib-folder\</li\>
     \<li\>log-folder\</li\>
     \<li\>cache-folder\</li\>
     \<li\>apps-folder\</li\>
 \</ul\>

```java
NutsResultList getResultStrings(String[] columns)
```
**return**:NutsResultList
- **String[] columns** : columns to return

#### ⚙ included()
search for included (in other installations as dependency) packages

```java
NutsSearchCommand included()
```
**return**:NutsSearchCommand

#### ⚙ installed()
search for non installed packages

```java
NutsSearchCommand installed()
```
**return**:NutsSearchCommand

#### ⚙ installedOrIncluded()
search for non installed or included (in other installations as dependency) packages

```java
NutsSearchCommand installedOrIncluded()
```
**return**:NutsSearchCommand

#### ⚙ notInstalled()
search for non installed packages

```java
NutsSearchCommand notInstalled()
```
**return**:NutsSearchCommand

#### ⚙ removeArch(value)
remove arch to search

```java
NutsSearchCommand removeArch(String value)
```
**return**:NutsSearchCommand
- **String value** : arch to remove

#### ⚙ removeId(id)
remove id to search.

```java
NutsSearchCommand removeId(String id)
```
**return**:NutsSearchCommand
- **String id** : id to search

#### ⚙ removeId(id)
remove id to search.

```java
NutsSearchCommand removeId(NutsId id)
```
**return**:NutsSearchCommand
- **NutsId id** : id to search

#### ⚙ removeLockedId(id)
remove locked ids to prevent them to be updated or the force other ids to use them (the installed version).

```java
NutsSearchCommand removeLockedId(NutsId id)
```
**return**:NutsSearchCommand
- **NutsId id** : id to unlock

#### ⚙ removeLockedId(id)
remove locked ids to prevent them to be updated or the force other ids to use them (the installed version).

```java
NutsSearchCommand removeLockedId(String id)
```
**return**:NutsSearchCommand
- **String id** : id to unlock

#### ⚙ removePackaging(value)
remove packaging from search

```java
NutsSearchCommand removePackaging(String value)
```
**return**:NutsSearchCommand
- **String value** : packaging to remove

#### ⚙ removeRepository(value)
add repository to search into

```java
NutsSearchCommand removeRepository(String value)
```
**return**:NutsSearchCommand
- **String value** : repository to search into

#### ⚙ removeScope(scope)
add dependency scope filter. Only relevant with \{\@link #setDependencies(boolean)\}
 and \{\@link #setDependenciesTree(boolean)\}

```java
NutsSearchCommand removeScope(NutsDependencyScope scope)
```
**return**:NutsSearchCommand
- **NutsDependencyScope scope** : scope filter

#### ⚙ removeScope(scope)
remove dependency scope filter. Only relevant with \{\@link #setDependencies(boolean)\}
 and \{\@link #setDependenciesTree(boolean)\}

```java
NutsSearchCommand removeScope(NutsDependencyScopePattern scope)
```
**return**:NutsSearchCommand
- **NutsDependencyScopePattern scope** : scope filter

#### ⚙ removeScript(value)
remove javascript filter.

```java
NutsSearchCommand removeScript(String value)
```
**return**:NutsSearchCommand
- **String value** : javascript filter

#### ⚙ run()
execute the command and return this instance

```java
NutsSearchCommand run()
```
**return**:NutsSearchCommand

#### ⚙ setAnyWhere()
all artifacts (local and remote). If local result found will any way
 fetch remote.

```java
NutsSearchCommand setAnyWhere()
```
**return**:NutsSearchCommand

#### ⚙ setDefaultLocation()
unset location to store to fetched id and fall back to default location.

```java
NutsSearchCommand setDefaultLocation()
```
**return**:NutsSearchCommand

#### ⚙ setOffline()
local only (installed or not)

```java
NutsSearchCommand setOffline()
```
**return**:NutsSearchCommand

#### ⚙ setOnline()
local or remote. If local result found will not fetch remote.

```java
NutsSearchCommand setOnline()
```
**return**:NutsSearchCommand

#### ⚙ setRemote()
remote only

```java
NutsSearchCommand setRemote()
```
**return**:NutsSearchCommand

#### ⚙ sort(comparator)
sort results. Comparator should handle types of the result.

```java
NutsSearchCommand sort(Comparator comparator)
```
**return**:NutsSearchCommand
- **Comparator comparator** : result comparator

#### ⚙ toFetch()
create fetch command initialized with this instance options.

```java
NutsFetchCommand toFetch()
```
**return**:NutsFetchCommand

## ☕ NutsUndeployCommand
```java
public interface net.vpc.app.nuts.NutsUndeployCommand
```

 \@author vpc
 \@since 0.5.4
 \@category Commands

### 🎛 Instance Properties
#### 📄🎛 ids

```java
[read-only] NutsId[] public ids
public NutsId[] getIds()
```
#### 📄🎛 offline

```java
[read-only] boolean public offline
public boolean isOffline()
```
#### 📄🎛 repository

```java
[read-only] String public repository
public String getRepository()
```
#### ✏🎛 session
update session
```java
[write-only] NutsUndeployCommand public session
public NutsUndeployCommand setSession(session)
```
#### 📄🎛 transitive

```java
[read-only] boolean public transitive
public boolean isTransitive()
```
### ⚙ Instance Methods
#### ⚙ addId(id)


```java
NutsUndeployCommand addId(NutsId id)
```
**return**:NutsUndeployCommand
- **NutsId id** : 

#### ⚙ addId(id)


```java
NutsUndeployCommand addId(String id)
```
**return**:NutsUndeployCommand
- **String id** : 

#### ⚙ addIds(value)


```java
NutsUndeployCommand addIds(NutsId[] value)
```
**return**:NutsUndeployCommand
- **NutsId[] value** : 

#### ⚙ addIds(values)


```java
NutsUndeployCommand addIds(String[] values)
```
**return**:NutsUndeployCommand
- **String[] values** : 

#### ⚙ clearIds()


```java
NutsUndeployCommand clearIds()
```
**return**:NutsUndeployCommand

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsUndeployCommand configure(boolean skipUnsupported, String[] args)
```
**return**:NutsUndeployCommand
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ copySession()
copy session

```java
NutsUndeployCommand copySession()
```
**return**:NutsUndeployCommand

#### ⚙ run()
execute the command and return this instance

```java
NutsUndeployCommand run()
```
**return**:NutsUndeployCommand

#### ⚙ setOffline(offline)


```java
NutsUndeployCommand setOffline(boolean offline)
```
**return**:NutsUndeployCommand
- **boolean offline** : 

#### ⚙ setRepository(repository)


```java
NutsUndeployCommand setRepository(String repository)
```
**return**:NutsUndeployCommand
- **String repository** : 

#### ⚙ setTransitive(transitive)


```java
NutsUndeployCommand setTransitive(boolean transitive)
```
**return**:NutsUndeployCommand
- **boolean transitive** : 

## ☕ NutsUninstallCommand
```java
public interface net.vpc.app.nuts.NutsUninstallCommand
```

 \@author vpc
 \@since 0.5.4
 \@category Commands

### 🎛 Instance Properties
#### 📄🎛 args

```java
[read-only] String[] public args
public String[] getArgs()
```
#### 📄🎛 erase

```java
[read-only] boolean public erase
public boolean isErase()
```
#### 📄🎛 ids

```java
[read-only] NutsId[] public ids
public NutsId[] getIds()
```
#### ✏🎛 session
update session
```java
[write-only] NutsUninstallCommand public session
public NutsUninstallCommand setSession(session)
```
### ⚙ Instance Methods
#### ⚙ addArg(arg)


```java
NutsUninstallCommand addArg(String arg)
```
**return**:NutsUninstallCommand
- **String arg** : 

#### ⚙ addArgs(args)


```java
NutsUninstallCommand addArgs(Collection args)
```
**return**:NutsUninstallCommand
- **Collection args** : 

#### ⚙ addArgs(args)


```java
NutsUninstallCommand addArgs(String[] args)
```
**return**:NutsUninstallCommand
- **String[] args** : 

#### ⚙ addId(id)


```java
NutsUninstallCommand addId(NutsId id)
```
**return**:NutsUninstallCommand
- **NutsId id** : 

#### ⚙ addId(id)


```java
NutsUninstallCommand addId(String id)
```
**return**:NutsUninstallCommand
- **String id** : 

#### ⚙ addIds(ids)


```java
NutsUninstallCommand addIds(NutsId[] ids)
```
**return**:NutsUninstallCommand
- **NutsId[] ids** : 

#### ⚙ addIds(ids)


```java
NutsUninstallCommand addIds(String[] ids)
```
**return**:NutsUninstallCommand
- **String[] ids** : 

#### ⚙ clearArgs()


```java
NutsUninstallCommand clearArgs()
```
**return**:NutsUninstallCommand

#### ⚙ clearIds()


```java
NutsUninstallCommand clearIds()
```
**return**:NutsUninstallCommand

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsUninstallCommand configure(boolean skipUnsupported, String[] args)
```
**return**:NutsUninstallCommand
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ copySession()
copy session

```java
NutsUninstallCommand copySession()
```
**return**:NutsUninstallCommand

#### ⚙ removeId(id)


```java
NutsUninstallCommand removeId(NutsId id)
```
**return**:NutsUninstallCommand
- **NutsId id** : 

#### ⚙ removeId(id)


```java
NutsUninstallCommand removeId(String id)
```
**return**:NutsUninstallCommand
- **String id** : 

#### ⚙ run()
execute the command and return this instance

```java
NutsUninstallCommand run()
```
**return**:NutsUninstallCommand

#### ⚙ setErase(erase)


```java
NutsUninstallCommand setErase(boolean erase)
```
**return**:NutsUninstallCommand
- **boolean erase** : 

## ☕ NutsUpdateCommand
```java
public interface net.vpc.app.nuts.NutsUpdateCommand
```
 \@author vpc
 \@since 0.5.4
 \@category Commands

### 🎛 Instance Properties
#### 📄🎛 api

```java
[read-only] boolean public api
public boolean isApi()
```
#### 📝🎛 apiVersion
set target api version required for updating other artifacts
```java
[read-write] NutsUpdateCommand public apiVersion
public String getApiVersion()
public NutsUpdateCommand setApiVersion(value)
```
#### 📄🎛 args

```java
[read-only] String[] public args
public String[] getArgs()
```
#### 📄🎛 companions

```java
[read-only] boolean public companions
public boolean isCompanions()
```
#### 📄🎛 enableInstall
if true enable installing new artifacts when an update is request for
 non installed packages.
```java
[read-only] boolean public enableInstall
public boolean isEnableInstall()
```
#### 📄🎛 extensions

```java
[read-only] boolean public extensions
public boolean isExtensions()
```
#### 📄🎛 ids

```java
[read-only] NutsId[] public ids
public NutsId[] getIds()
```
#### 📄🎛 installed

```java
[read-only] boolean public installed
public boolean isInstalled()
```
#### 📄🎛 lockedIds

```java
[read-only] NutsId[] public lockedIds
public NutsId[] getLockedIds()
```
#### 📝🎛 optional
when true include optional dependencies
```java
[read-write] NutsUpdateCommand public optional
public boolean isOptional()
public NutsUpdateCommand setOptional(includeOptional)
```
#### 📄🎛 result
execute update check (if not already performed) then return result
```java
[read-only] NutsWorkspaceUpdateResult public result
public NutsWorkspaceUpdateResult getResult()
```
#### 📄🎛 resultCount

```java
[read-only] int public resultCount
public int getResultCount()
```
#### 📄🎛 runtime

```java
[read-only] boolean public runtime
public boolean isRuntime()
```
#### ✏🎛 session
update session
```java
[write-only] NutsUpdateCommand public session
public NutsUpdateCommand setSession(session)
```
### ⚙ Instance Methods
#### ⚙ addArg(arg)


```java
NutsUpdateCommand addArg(String arg)
```
**return**:NutsUpdateCommand
- **String arg** : 

#### ⚙ addArgs(args)


```java
NutsUpdateCommand addArgs(Collection args)
```
**return**:NutsUpdateCommand
- **Collection args** : 

#### ⚙ addArgs(args)


```java
NutsUpdateCommand addArgs(String[] args)
```
**return**:NutsUpdateCommand
- **String[] args** : 

#### ⚙ addId(id)


```java
NutsUpdateCommand addId(NutsId id)
```
**return**:NutsUpdateCommand
- **NutsId id** : 

#### ⚙ addId(id)


```java
NutsUpdateCommand addId(String id)
```
**return**:NutsUpdateCommand
- **String id** : 

#### ⚙ addIds(ids)


```java
NutsUpdateCommand addIds(NutsId[] ids)
```
**return**:NutsUpdateCommand
- **NutsId[] ids** : 

#### ⚙ addIds(ids)


```java
NutsUpdateCommand addIds(String[] ids)
```
**return**:NutsUpdateCommand
- **String[] ids** : 

#### ⚙ addLockedId(id)


```java
NutsUpdateCommand addLockedId(NutsId id)
```
**return**:NutsUpdateCommand
- **NutsId id** : 

#### ⚙ addLockedId(id)


```java
NutsUpdateCommand addLockedId(String id)
```
**return**:NutsUpdateCommand
- **String id** : 

#### ⚙ addLockedIds(ids)


```java
NutsUpdateCommand addLockedIds(NutsId[] ids)
```
**return**:NutsUpdateCommand
- **NutsId[] ids** : 

#### ⚙ addLockedIds(ids)


```java
NutsUpdateCommand addLockedIds(String[] ids)
```
**return**:NutsUpdateCommand
- **String[] ids** : 

#### ⚙ addScope(scope)


```java
NutsUpdateCommand addScope(NutsDependencyScope scope)
```
**return**:NutsUpdateCommand
- **NutsDependencyScope scope** : 

#### ⚙ addScopes(scopes)


```java
NutsUpdateCommand addScopes(Collection scopes)
```
**return**:NutsUpdateCommand
- **Collection scopes** : 

#### ⚙ addScopes(scopes)


```java
NutsUpdateCommand addScopes(NutsDependencyScope[] scopes)
```
**return**:NutsUpdateCommand
- **NutsDependencyScope[] scopes** : 

#### ⚙ checkUpdates()


```java
NutsUpdateCommand checkUpdates()
```
**return**:NutsUpdateCommand

#### ⚙ checkUpdates(applyUpdates)
check for updates.

```java
NutsUpdateCommand checkUpdates(boolean applyUpdates)
```
**return**:NutsUpdateCommand
- **boolean applyUpdates** : if true updates will be applied

#### ⚙ clearArgs()


```java
NutsUpdateCommand clearArgs()
```
**return**:NutsUpdateCommand

#### ⚙ clearIds()


```java
NutsUpdateCommand clearIds()
```
**return**:NutsUpdateCommand

#### ⚙ clearLockedIds()


```java
NutsUpdateCommand clearLockedIds()
```
**return**:NutsUpdateCommand

#### ⚙ clearScopes()


```java
NutsUpdateCommand clearScopes()
```
**return**:NutsUpdateCommand

#### ⚙ companions()
update workspace companion versions

```java
NutsUpdateCommand companions()
```
**return**:NutsUpdateCommand

#### ⚙ companions(enable)


```java
NutsUpdateCommand companions(boolean enable)
```
**return**:NutsUpdateCommand
- **boolean enable** : 

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsUpdateCommand configure(boolean skipUnsupported, String[] args)
```
**return**:NutsUpdateCommand
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ copySession()
copy session

```java
NutsUpdateCommand copySession()
```
**return**:NutsUpdateCommand

#### ⚙ installed()
update installed artifacts

```java
NutsUpdateCommand installed()
```
**return**:NutsUpdateCommand

#### ⚙ installed(enable)


```java
NutsUpdateCommand installed(boolean enable)
```
**return**:NutsUpdateCommand
- **boolean enable** : 

#### ⚙ lockedId(id)


```java
NutsUpdateCommand lockedId(NutsId id)
```
**return**:NutsUpdateCommand
- **NutsId id** : 

#### ⚙ lockedId(id)


```java
NutsUpdateCommand lockedId(String id)
```
**return**:NutsUpdateCommand
- **String id** : 

#### ⚙ lockedIds(id)


```java
NutsUpdateCommand lockedIds(NutsId[] id)
```
**return**:NutsUpdateCommand
- **NutsId[] id** : 

#### ⚙ lockedIds(id)


```java
NutsUpdateCommand lockedIds(String[] id)
```
**return**:NutsUpdateCommand
- **String[] id** : 

#### ⚙ removeId(id)


```java
NutsUpdateCommand removeId(NutsId id)
```
**return**:NutsUpdateCommand
- **NutsId id** : 

#### ⚙ removeId(id)


```java
NutsUpdateCommand removeId(String id)
```
**return**:NutsUpdateCommand
- **String id** : 

#### ⚙ run()
execute the command and return this instance

```java
NutsUpdateCommand run()
```
**return**:NutsUpdateCommand

#### ⚙ runtime()
update workspace runtime version

```java
NutsUpdateCommand runtime()
```
**return**:NutsUpdateCommand

#### ⚙ runtime(enable)


```java
NutsUpdateCommand runtime(boolean enable)
```
**return**:NutsUpdateCommand
- **boolean enable** : 

#### ⚙ scope(scope)


```java
NutsUpdateCommand scope(NutsDependencyScope scope)
```
**return**:NutsUpdateCommand
- **NutsDependencyScope scope** : 

#### ⚙ scopes(scopes)


```java
NutsUpdateCommand scopes(Collection scopes)
```
**return**:NutsUpdateCommand
- **Collection scopes** : 

#### ⚙ scopes(scopes)


```java
NutsUpdateCommand scopes(NutsDependencyScope[] scopes)
```
**return**:NutsUpdateCommand
- **NutsDependencyScope[] scopes** : 

#### ⚙ setAll()
update api, runtime, extensions, companions and all installed artifacts

```java
NutsUpdateCommand setAll()
```
**return**:NutsUpdateCommand

#### ⚙ setApi(enable)


```java
NutsUpdateCommand setApi(boolean enable)
```
**return**:NutsUpdateCommand
- **boolean enable** : 

#### ⚙ setCompanions(updateCompanions)


```java
NutsUpdateCommand setCompanions(boolean updateCompanions)
```
**return**:NutsUpdateCommand
- **boolean updateCompanions** : 

#### ⚙ setEnableInstall(enableInstall)


```java
NutsUpdateCommand setEnableInstall(boolean enableInstall)
```
**return**:NutsUpdateCommand
- **boolean enableInstall** : 

#### ⚙ setExtensions(enable)


```java
NutsUpdateCommand setExtensions(boolean enable)
```
**return**:NutsUpdateCommand
- **boolean enable** : 

#### ⚙ setInstalled(enable)


```java
NutsUpdateCommand setInstalled(boolean enable)
```
**return**:NutsUpdateCommand
- **boolean enable** : 

#### ⚙ setRuntime(enable)


```java
NutsUpdateCommand setRuntime(boolean enable)
```
**return**:NutsUpdateCommand
- **boolean enable** : 

#### ⚙ update()


```java
NutsUpdateCommand update()
```
**return**:NutsUpdateCommand

#### ⚙ workspace()
update api, runtime, extensions and companions

```java
NutsUpdateCommand workspace()
```
**return**:NutsUpdateCommand

## ☕ NutsUpdateOptions
```java
public net.vpc.app.nuts.NutsUpdateOptions
```
 Generic Add options

 author vpc
 \@since 0.5.7
 \@category Commands

### 🪄 Constructors
#### 🪄 NutsUpdateOptions()


```java
NutsUpdateOptions()
```

### 🎛 Instance Properties
#### 📝🎛 session
update current session
```java
[read-write] NutsUpdateOptions public session
public NutsSession getSession()
public NutsUpdateOptions setSession(session)
```
## ☕ NutsUpdateResult
```java
public interface net.vpc.app.nuts.NutsUpdateResult
```
 component update result

 \@author vpc
 \@since 0.5.4
 \@category Commands

### 🎛 Instance Properties
#### 📄🎛 available
return available definition or null
```java
[read-only] NutsDefinition public available
public NutsDefinition getAvailable()
```
#### 📄🎛 dependencies
return update dependencies
```java
[read-only] NutsId[] public dependencies
public NutsId[] getDependencies()
```
#### 📄🎛 id
artifact id
```java
[read-only] NutsId public id
public NutsId getId()
```
#### 📄🎛 local
return installed/local definition or null
```java
[read-only] NutsDefinition public local
public NutsDefinition getLocal()
```
#### 📄🎛 updateApplied
return true if the update was applied
```java
[read-only] boolean public updateApplied
public boolean isUpdateApplied()
```
#### 📄🎛 updateAvailable
return true if any update is available.
 equivalent to \{\@code isUpdateVersionAvailable() || isUpdateStatusAvailable()\}
```java
[read-only] boolean public updateAvailable
public boolean isUpdateAvailable()
```
#### 📄🎛 updateForced
return true if the update was forced
```java
[read-only] boolean public updateForced
public boolean isUpdateForced()
```
#### 📄🎛 updateStatusAvailable
return true if artifact has no version update
 but still have status (default) to be updated
```java
[read-only] boolean public updateStatusAvailable
public boolean isUpdateStatusAvailable()
```
#### 📄🎛 updateVersionAvailable
return true if artifact has newer available version
```java
[read-only] boolean public updateVersionAvailable
public boolean isUpdateVersionAvailable()
```
## ☕ NutsWorkspaceCommand
```java
public interface net.vpc.app.nuts.NutsWorkspaceCommand
```
 Generic Command for usual workspace operations. All Command classes have a
 \'run\' method to perform the operation.

 \@author vpc
 \@since 0.5.5
 \@category Commands

### 🎛 Instance Properties
#### 📝🎛 session
update session
```java
[read-write] NutsWorkspaceCommand public session
public NutsSession getSession()
public NutsWorkspaceCommand setSession(session)
```
### ⚙ Instance Methods
#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsWorkspaceCommand configure(boolean skipUnsupported, String[] args)
```
**return**:NutsWorkspaceCommand
- **boolean skipUnsupported** : 
- **String[] args** : argument to configure with

#### ⚙ copySession()
copy session

```java
NutsWorkspaceCommand copySession()
```
**return**:NutsWorkspaceCommand

#### ⚙ run()
execute the command and return this instance

```java
NutsWorkspaceCommand run()
```
**return**:NutsWorkspaceCommand

## ☕ NutsWorkspaceUpdateResult
```java
public interface net.vpc.app.nuts.NutsWorkspaceUpdateResult
```
 Created by vpc on 6/23/17.

 \@since 0.5.5
 \@category Commands

### 🎛 Instance Properties
#### 📄🎛 allResults

```java
[read-only] NutsUpdateResult[] public allResults
public NutsUpdateResult[] getAllResults()
```
#### 📄🎛 allUpdates

```java
[read-only] NutsUpdateResult[] public allUpdates
public NutsUpdateResult[] getAllUpdates()
```
#### 📄🎛 api

```java
[read-only] NutsUpdateResult public api
public NutsUpdateResult getApi()
```
#### 📄🎛 artifacts

```java
[read-only] NutsUpdateResult[] public artifacts
public NutsUpdateResult[] getArtifacts()
```
#### 📄🎛 extensions

```java
[read-only] NutsUpdateResult[] public extensions
public NutsUpdateResult[] getExtensions()
```
#### 📄🎛 runtime

```java
[read-only] NutsUpdateResult public runtime
public NutsUpdateResult getRuntime()
```
#### 📄🎛 updatableApi

```java
[read-only] boolean public updatableApi
public boolean isUpdatableApi()
```
#### 📄🎛 updatableExtensions

```java
[read-only] boolean public updatableExtensions
public boolean isUpdatableExtensions()
```
#### 📄🎛 updatableRuntime

```java
[read-only] boolean public updatableRuntime
public boolean isUpdatableRuntime()
```
#### 📄🎛 updateAvailable

```java
[read-only] boolean public updateAvailable
public boolean isUpdateAvailable()
```
#### 📄🎛 updatesCount

```java
[read-only] int public updatesCount
public int getUpdatesCount()
```

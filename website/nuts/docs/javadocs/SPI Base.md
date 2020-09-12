---
id: javadoc_SPI_Base
title: SPI Base
sidebar_label: SPI Base
---
                                                
```
     __        __           ___    ____  ____
  /\ \ \ _  __/ /______    /   |  / __ \/  _/
 /  \/ / / / / __/ ___/   / /| | / /_/ // /   
/ /\  / /_/ / /_(__  )   / ___ |/ ____// /       
\_\ \/\__,_/\__/____/   /_/  |_/_/   /___/  version 0.7.0
```

## ☕ NutsBootWorkspace
```java
public final net.vpc.app.nuts.NutsBootWorkspace
```
 NutsBootWorkspace is responsible of loading initial nuts-core.jar and its
 dependencies and for creating workspaces using the method
 \{\@link #openWorkspace()\} . NutsBootWorkspace is also responsible of managing
 local jar cache folder located at ~/.cache/nuts/default-workspace/boot
 \<p\>
 Default Bootstrap implementation. This class is responsible of loading
 initial nuts-core.jar and its dependencies and for creating workspaces using
 the method \{\@link #openWorkspace()\}.
 \<p\>

 \@author vpc
 \@since 0.5.4
 \@category SPI Base

### 🪄 Constructors
#### 🪄 NutsBootWorkspace(args)


```java
NutsBootWorkspace(String[] args)
```
- **String[] args** : 

#### 🪄 NutsBootWorkspace(options)


```java
NutsBootWorkspace(NutsWorkspaceOptions options)
```
- **NutsWorkspaceOptions options** : 

### 🎛 Instance Properties
#### 📄🎛 contextClassLoader

```java
[read-only] ClassLoader protected contextClassLoader
protected ClassLoader getContextClassLoader()
```
#### 📄🎛 home

```java
[read-only] String protected home
protected String getHome(storeFolder)
```
#### 📄🎛 options

```java
[read-only] NutsWorkspaceOptions public options
public NutsWorkspaceOptions getOptions()
```
### ⚙ Instance Methods
#### ⚙ createProcessBuilder()


```java
ProcessBuilder createProcessBuilder()
```
**return**:ProcessBuilder

#### ⚙ createProcessCommandLine()


```java
String[] createProcessCommandLine()
```
**return**:String[]

#### ⚙ expandPath(path, base)


```java
String expandPath(String path, String base)
```
**return**:String
- **String path** : 
- **String base** : 

#### ⚙ getRequirementsHelpString(unsatisfiedOnly)
return a string representing unsatisfied contrains

```java
String getRequirementsHelpString(boolean unsatisfiedOnly)
```
**return**:String
- **boolean unsatisfiedOnly** : when true return requirements for new instance

#### ⚙ hasUnsatisfiedRequirements()


```java
boolean hasUnsatisfiedRequirements()
```
**return**:boolean

#### ⚙ openWorkspace()


```java
NutsWorkspace openWorkspace()
```
**return**:NutsWorkspace

#### ⚙ resolveBootRepositories()


```java
Collection resolveBootRepositories()
```
**return**:Collection

#### ⚙ runWorkspace()


```java
void runWorkspace()
```

#### ⚙ startNewProcess()


```java
int startNewProcess()
```
**return**:int

## ☕ NutsBootWorkspaceFactory
```java
public interface net.vpc.app.nuts.NutsBootWorkspaceFactory
```
 Class responsible of creating and initializing Workspace
 Created by vpc on 1/5/17.

 \@since 0.5.4
 \@category SPI Base

### ⚙ Instance Methods
#### ⚙ createWorkspace(options)
create workspace with the given options

```java
NutsWorkspace createWorkspace(NutsWorkspaceInitInformation options)
```
**return**:NutsWorkspace
- **NutsWorkspaceInitInformation options** : boot init options

#### ⚙ getBootSupportLevel(options)
when multiple factories are available, the best one is selected according to
 the maximum value of \{\@code getBootSupportLevel(options)\}.
 Note that default value (for the reference implementation) is \{\@code NutsComponent.DEFAULT_SUPPORT\}.
 Any value less or equal to zero is ignored (and the factory is discarded)

```java
int getBootSupportLevel(NutsWorkspaceOptions options)
```
**return**:int
- **NutsWorkspaceOptions options** : command line options

## ☕ NutsCommandAliasFactoryConfig
```java
public net.vpc.app.nuts.NutsCommandAliasFactoryConfig
```
 Command Alias Factory Definition Config

 \@author vpc
 \@since 0.5.4
 \@category SPI Base

### 🪄 Constructors
#### 🪄 NutsCommandAliasFactoryConfig()


```java
NutsCommandAliasFactoryConfig()
```

### 🎛 Instance Properties
#### 📝🎛 factoryId
Factory id (unique identifier in the workspace)
```java
[read-write] NutsCommandAliasFactoryConfig public factoryId
public String getFactoryId()
public NutsCommandAliasFactoryConfig setFactoryId(value)
```
#### 📝🎛 factoryType
Factory Type
```java
[read-write] NutsCommandAliasFactoryConfig public factoryType
public String getFactoryType()
public NutsCommandAliasFactoryConfig setFactoryType(value)
```
#### 📝🎛 parameters
factory parameters
```java
[read-write] NutsCommandAliasFactoryConfig public parameters
public Map getParameters()
public NutsCommandAliasFactoryConfig setParameters(value)
```
#### 📝🎛 priority
priority (the higher the better)
```java
[read-write] NutsCommandAliasFactoryConfig public priority
public int getPriority()
public NutsCommandAliasFactoryConfig setPriority(value)
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

## ☕ NutsComponent
```java
public interface net.vpc.app.nuts.NutsComponent
```
 Top Level extension Point in Nuts. 
 Extension mechanism in nuts is based on a factory thats select the best 
 implementation for a given predefined interface (named Extension Point).
 Such interfaces must extend this \{\@code NutsComponent\} interface.
 Implementations must implement these extension points by providing their 
 best support level (when method \{\@link #getSupportLevel(net.vpc.app.nuts.NutsSupportLevelContext)\} is invoked).
 Only implementations with positive support level are considered.
 Implementations with higher support level are selected first.
 

 \@param \<CriteriaType\> support criteria type
 \@since 0.5.4
 \@category SPI Base

### 📢❄ Constant Fields
#### 📢❄ CUSTOM_SUPPORT
```java
public static final int CUSTOM_SUPPORT = 1000
```
#### 📢❄ DEFAULT_SUPPORT
```java
public static final int DEFAULT_SUPPORT = 10
```
#### 📢❄ NO_SUPPORT
```java
public static final int NO_SUPPORT = -1
```
### ⚙ Instance Methods
#### ⚙ getSupportLevel(context)
evaluate support level (who much this instance should be considered convenient, acceptable)
 for the given arguments (provided in context).

```java
int getSupportLevel(NutsSupportLevelContext context)
```
**return**:int
- **NutsSupportLevelContext context** : evaluation context

## ☕ NutsDefaultSupportLevelContext
```java
public net.vpc.app.nuts.NutsDefaultSupportLevelContext
```
 Default and dummy NutsSupportLevelContext implementation
 \@author vpc
 \@param \<T\> support level type
 \@category SPI Base

### 🪄 Constructors
#### 🪄 NutsDefaultSupportLevelContext(ws, constraints)
default constructor

```java
NutsDefaultSupportLevelContext(NutsWorkspace ws, Object constraints)
```
- **NutsWorkspace ws** : workspace
- **Object constraints** : constraints

### 🎛 Instance Properties
#### 📄🎛 constraints

```java
[read-only] Object public constraints
public Object getConstraints()
```
#### 📄🎛 workspace

```java
[read-only] NutsWorkspace public workspace
public NutsWorkspace getWorkspace()
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

## ☕ NutsDeployRepositoryCommand
```java
public interface net.vpc.app.nuts.NutsDeployRepositoryCommand
```
 Repository Deploy command provided by Repository and used by Workspace.
 This class is part of Nuts SPI and is not to be used by end users.
 \@author vpc
 \@since 0.5.4
 \@category SPI Base

### 🎛 Instance Properties
#### 📝🎛 content
set content to deploy
```java
[read-write] NutsDeployRepositoryCommand public content
public Object getContent()
public NutsDeployRepositoryCommand setContent(content)
```
#### 📝🎛 descriptor
set descriptor to deploy
```java
[read-write] NutsDeployRepositoryCommand public descriptor
public NutsDescriptor getDescriptor()
public NutsDeployRepositoryCommand setDescriptor(descriptor)
```
#### 📝🎛 id
set id to deploy
```java
[read-write] NutsDeployRepositoryCommand public id
public NutsId getId()
public NutsDeployRepositoryCommand setId(id)
```
#### ✏🎛 session
session
```java
[write-only] NutsDeployRepositoryCommand public session
public NutsDeployRepositoryCommand setSession(session)
```
### ⚙ Instance Methods
#### ⚙ run()
run deploy command

```java
NutsDeployRepositoryCommand run()
```
**return**:NutsDeployRepositoryCommand

## ☕ NutsDescriptorContentParserComponent
```java
public interface net.vpc.app.nuts.NutsDescriptorContentParserComponent
```
 Content parser component is responsible of resolving a Nuts descriptor form a content file

 \@since 0.5.4
 \@category SPI Base

### ⚙ Instance Methods
#### ⚙ parse(parserContext)
parse content and return a valid NutsDescriptor or null if not supported.

```java
NutsDescriptor parse(NutsDescriptorContentParserContext parserContext)
```
**return**:NutsDescriptor
- **NutsDescriptorContentParserContext parserContext** : context

## ☕ NutsDescriptorContentParserContext
```java
public interface net.vpc.app.nuts.NutsDescriptorContentParserContext
```
 context holding useful information for \{\@link NutsDescriptorContentParserComponent#parse(NutsDescriptorContentParserContext)\}

 \@since 0.5.4
 \@category SPI Base

### 🎛 Instance Properties
#### 📄🎛 fileExtension
content file extension or null. At least one of file extension or file mime-type is provided.
```java
[read-only] String public fileExtension
public String getFileExtension()
```
#### 📄🎛 fullStream
content stream
```java
[read-only] InputStream public fullStream
public InputStream getFullStream()
```
#### 📄🎛 headStream
return content header stream.
 if the content size is less than 1Mb, then all the content is returned.
 If not, at least 1Mb is returned.
```java
[read-only] InputStream public headStream
public InputStream getHeadStream()
```
#### 📄🎛 mimeType
content mime-type or null. At least one of file extension or file mime-type is provided.
```java
[read-only] String public mimeType
public String getMimeType()
```
#### 📄🎛 name
content name (mostly content file name)
```java
[read-only] String public name
public String getName()
```
#### 📄🎛 parseOptions
command line options that can be parsed to
 configure parsing options.
 A good example of it is the --all-mains option that can be passed
 as executor option which will be catched by parser to force resolution
 of all main classes even though a Main-Class attribute is visited in the MANIFEST.MF
 file.
 This array may continue any non supported options. They should be discarded by the parser.
```java
[read-only] String[] public parseOptions
public String[] getParseOptions()
```
#### 📄🎛 session
return session
```java
[read-only] NutsSession public session
public NutsSession getSession()
```
#### 📄🎛 workspace
return workspace
```java
[read-only] NutsWorkspace public workspace
public NutsWorkspace getWorkspace()
```
## ☕ NutsExecutorComponent
```java
public interface net.vpc.app.nuts.NutsExecutorComponent
```
 An Executor Component is responsible of "executing" a nuts component
 (package) Created by vpc on 1/7/17.

 \@since 0.5.4
 \@category SPI Base

### 🎛 Instance Properties
#### 📄🎛 id
artifact id
```java
[read-only] NutsId public id
public NutsId getId()
```
### ⚙ Instance Methods
#### ⚙ dryExec(executionContext)
performs a dry execution (simulation) avoiding any side effect and issuing trace to standard
 output in order to log simulation workflow.

```java
void dryExec(NutsExecutionContext executionContext)
```
- **NutsExecutionContext executionContext** : executionContext

#### ⚙ exec(executionContext)
execute the artifact

```java
void exec(NutsExecutionContext executionContext)
```
- **NutsExecutionContext executionContext** : executionContext

## ☕ NutsFetchContentRepositoryCommand
```java
public interface net.vpc.app.nuts.NutsFetchContentRepositoryCommand
```
 Repository command bound to FetchCommand used to fetch an artifact content from a specific repository.
 \@author vpc
 \@since 0.5.5
 \@category SPI Base

### 🎛 Instance Properties
#### 📝🎛 descriptor
set descriptor to fetch.
```java
[read-write] NutsFetchContentRepositoryCommand public descriptor
public NutsDescriptor getDescriptor()
public NutsFetchContentRepositoryCommand setDescriptor(descriptor)
```
#### 📝🎛 fetchMode
fetchMode
```java
[read-write] NutsFetchContentRepositoryCommand public fetchMode
public NutsFetchMode getFetchMode()
public NutsFetchContentRepositoryCommand setFetchMode(fetchMode)
```
#### 📝🎛 id
set id to fetch.
```java
[read-write] NutsFetchContentRepositoryCommand public id
public NutsId getId()
public NutsFetchContentRepositoryCommand setId(id)
```
#### 📝🎛 localPath
set localPath to store to.
```java
[read-write] NutsFetchContentRepositoryCommand public localPath
public Path getLocalPath()
public NutsFetchContentRepositoryCommand setLocalPath(localPath)
```
#### 📄🎛 result
return fetch result. if the command is not yet executed, it will be executed first.
```java
[read-only] NutsContent public result
public NutsContent getResult()
```
#### ✏🎛 session
set current session.
```java
[write-only] NutsFetchContentRepositoryCommand public session
public NutsFetchContentRepositoryCommand setSession(session)
```
### ⚙ Instance Methods
#### ⚙ run()
preform command. Should be called after setting all parameters.
 Result is retrievable with \{\@link #getResult()\}.

```java
NutsFetchContentRepositoryCommand run()
```
**return**:NutsFetchContentRepositoryCommand

## ☕ NutsFetchDescriptorRepositoryCommand
```java
public interface net.vpc.app.nuts.NutsFetchDescriptorRepositoryCommand
```
 Repository command used to fetch an artifact descriptor from a specific repository.
 \@author vpc
 \@since 0.5.5
 \@category SPI Base

### 🎛 Instance Properties
#### 📝🎛 fetchMode
fetchMode
```java
[read-write] NutsFetchDescriptorRepositoryCommand public fetchMode
public NutsFetchMode getFetchMode()
public NutsFetchDescriptorRepositoryCommand setFetchMode(fetchMode)
```
#### 📝🎛 id
set id to fetch
```java
[read-write] NutsFetchDescriptorRepositoryCommand public id
public NutsId getId()
public NutsFetchDescriptorRepositoryCommand setId(id)
```
#### 📄🎛 result
return fetch result. if the command is not yet executed, it will be executed first.
```java
[read-only] NutsDescriptor public result
public NutsDescriptor getResult()
```
### ⚙ Instance Methods
#### ⚙ run()
preform command. Should be called after setting all parameters.
 Result is retrievable with \{\@link #getResult()\}.

```java
NutsFetchDescriptorRepositoryCommand run()
```
**return**:NutsFetchDescriptorRepositoryCommand

#### ⚙ setSession(session)


```java
NutsFetchDescriptorRepositoryCommand setSession(NutsSession session)
```
**return**:NutsFetchDescriptorRepositoryCommand
- **NutsSession session** : 

## ☕ NutsInstallerComponent
```java
public interface net.vpc.app.nuts.NutsInstallerComponent
```
 Component responsible of installing other artifacts.

 \@since 0.5.4
 \@category SPI Base

### ⚙ Instance Methods
#### ⚙ install(executionContext)
install artifact

```java
void install(NutsExecutionContext executionContext)
```
- **NutsExecutionContext executionContext** : execution context

#### ⚙ uninstall(executionContext, deleteData)
uninstall artifact

```java
void uninstall(NutsExecutionContext executionContext, boolean deleteData)
```
- **NutsExecutionContext executionContext** : execution context
- **boolean deleteData** : delete data after uninstall

#### ⚙ update(executionContext)
update artifact

```java
void update(NutsExecutionContext executionContext)
```
- **NutsExecutionContext executionContext** : execution context

## ☕ NutsPrototype
```java
public interface net.vpc.app.nuts.NutsPrototype
```
 classes that are marked with this annotation will be created at each call by
 the factory.

 \@since 0.5.4
 \@category SPI Base

## ☕ NutsPushRepositoryCommand
```java
public interface net.vpc.app.nuts.NutsPushRepositoryCommand
```
 Push Command
 \@author vpc
 \@category SPI Base

### 🎛 Instance Properties
#### 📝🎛 args
args args to push
```java
[read-write] NutsPushRepositoryCommand public args
public String[] getArgs()
public NutsPushRepositoryCommand setArgs(args)
```
#### 📝🎛 id
set id to push.
```java
[read-write] NutsPushRepositoryCommand public id
public NutsId getId()
public NutsPushRepositoryCommand setId(id)
```
#### 📝🎛 offline
local only (installed or not)
```java
[read-write] NutsPushRepositoryCommand public offline
public boolean isOffline()
public NutsPushRepositoryCommand setOffline(offline)
```
#### 📝🎛 repository
repository to push from
```java
[read-write] NutsPushRepositoryCommand public repository
public String getRepository()
public NutsPushRepositoryCommand setRepository(repository)
```
#### ✏🎛 session
set session
```java
[write-only] NutsPushRepositoryCommand public session
public NutsPushRepositoryCommand setSession(session)
```
### ⚙ Instance Methods
#### ⚙ run()
run this command and return \{\@code this\} instance

```java
NutsPushRepositoryCommand run()
```
**return**:NutsPushRepositoryCommand

## ☕ NutsRepositoryCommand
```java
public interface net.vpc.app.nuts.NutsRepositoryCommand
```
 Root class for all Repository commands.
 \@author vpc
 \@category SPI Base

### 🎛 Instance Properties
#### 📝🎛 session
set session
```java
[read-write] NutsRepositoryCommand public session
public NutsSession getSession()
public NutsRepositoryCommand setSession(session)
```
### ⚙ Instance Methods
#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsRepositoryCommand configure(boolean skipUnsupported, String[] args)
```
**return**:NutsRepositoryCommand
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ run()
run this command and return \{\@code this\} instance

```java
NutsRepositoryCommand run()
```
**return**:NutsRepositoryCommand

## ☕ NutsRepositoryFactoryComponent
```java
public interface net.vpc.app.nuts.NutsRepositoryFactoryComponent
```
 Created by vpc on 1/15/17.

 \@since 0.5.4
 \@category SPI Base

### 🎛 Instance Properties
#### 📄🎛 defaultRepositories

```java
[read-only] NutsRepositoryDefinition[] public defaultRepositories
public NutsRepositoryDefinition[] getDefaultRepositories(workspace)
```
### ⚙ Instance Methods
#### ⚙ create(options, workspace, parentRepository)


```java
NutsRepository create(NutsAddRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository)
```
**return**:NutsRepository
- **NutsAddRepositoryOptions options** : 
- **NutsWorkspace workspace** : 
- **NutsRepository parentRepository** : 

## ☕ NutsRepositoryUndeployCommand
```java
public interface net.vpc.app.nuts.NutsRepositoryUndeployCommand
```

 \@author vpc
 \@since 0.5.4
 \@category SPI Base

### 🎛 Instance Properties
#### 📄🎛 id

```java
[read-only] NutsId public id
public NutsId getId()
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
#### 📄🎛 transitive

```java
[read-only] boolean public transitive
public boolean isTransitive()
```
### ⚙ Instance Methods
#### ⚙ run()
run this command and return \{\@code this\} instance

```java
NutsRepositoryUndeployCommand run()
```
**return**:NutsRepositoryUndeployCommand

#### ⚙ setId(id)


```java
NutsRepositoryUndeployCommand setId(NutsId id)
```
**return**:NutsRepositoryUndeployCommand
- **NutsId id** : 

#### ⚙ setOffline(offline)


```java
NutsRepositoryUndeployCommand setOffline(boolean offline)
```
**return**:NutsRepositoryUndeployCommand
- **boolean offline** : 

#### ⚙ setRepository(repository)


```java
NutsRepositoryUndeployCommand setRepository(String repository)
```
**return**:NutsRepositoryUndeployCommand
- **String repository** : 

#### ⚙ setSession(session)


```java
NutsRepositoryUndeployCommand setSession(NutsSession session)
```
**return**:NutsRepositoryUndeployCommand
- **NutsSession session** : 

#### ⚙ setTransitive(transitive)


```java
NutsRepositoryUndeployCommand setTransitive(boolean transitive)
```
**return**:NutsRepositoryUndeployCommand
- **boolean transitive** : 

## ☕ NutsSearchRepositoryCommand
```java
public interface net.vpc.app.nuts.NutsSearchRepositoryCommand
```

 \@author vpc
 \@category SPI Base

### 🎛 Instance Properties
#### 📝🎛 fetchMode
fetchMode
```java
[read-write] NutsSearchRepositoryCommand public fetchMode
public NutsFetchMode getFetchMode()
public NutsSearchRepositoryCommand setFetchMode(fetchMode)
```
#### 📄🎛 filter

```java
[read-only] NutsIdFilter public filter
public NutsIdFilter getFilter()
```
#### 📄🎛 result
this method should return immediately and returns valid iterator.
 visiting iterator may be blocking but not this method call.
 If \{\@code run()\} method has not been called yet, it will be called.
```java
[read-only] Iterator public result
public Iterator getResult()
```
### ⚙ Instance Methods
#### ⚙ run()
this method should return immediately after initializing a valid iterator to be
 retrieved by \{\@code getResult()\}

```java
NutsSearchRepositoryCommand run()
```
**return**:NutsSearchRepositoryCommand

#### ⚙ setFilter(filter)


```java
NutsSearchRepositoryCommand setFilter(NutsIdFilter filter)
```
**return**:NutsSearchRepositoryCommand
- **NutsIdFilter filter** : 

#### ⚙ setSession(session)


```java
NutsSearchRepositoryCommand setSession(NutsSession session)
```
**return**:NutsSearchRepositoryCommand
- **NutsSession session** : 

## ☕ NutsSearchVersionsRepositoryCommand
```java
public interface net.vpc.app.nuts.NutsSearchVersionsRepositoryCommand
```

 \@author vpc
 \@category SPI Base

### 🎛 Instance Properties
#### 📝🎛 fetchMode
fetchMode
```java
[read-write] NutsSearchVersionsRepositoryCommand public fetchMode
public NutsFetchMode getFetchMode()
public NutsSearchVersionsRepositoryCommand setFetchMode(fetchMode)
```
#### 📄🎛 filter

```java
[read-only] NutsIdFilter public filter
public NutsIdFilter getFilter()
```
#### 📄🎛 id

```java
[read-only] NutsId public id
public NutsId getId()
```
#### 📄🎛 result

```java
[read-only] Iterator public result
public Iterator getResult()
```
### ⚙ Instance Methods
#### ⚙ run()
run this command and return \{\@code this\} instance

```java
NutsSearchVersionsRepositoryCommand run()
```
**return**:NutsSearchVersionsRepositoryCommand

#### ⚙ setFilter(filter)


```java
NutsSearchVersionsRepositoryCommand setFilter(NutsIdFilter filter)
```
**return**:NutsSearchVersionsRepositoryCommand
- **NutsIdFilter filter** : 

#### ⚙ setId(id)


```java
NutsSearchVersionsRepositoryCommand setId(NutsId id)
```
**return**:NutsSearchVersionsRepositoryCommand
- **NutsId id** : 

#### ⚙ setSession(session)


```java
NutsSearchVersionsRepositoryCommand setSession(NutsSession session)
```
**return**:NutsSearchVersionsRepositoryCommand
- **NutsSession session** : 

## ☕ NutsServiceLoader
```java
public interface net.vpc.app.nuts.NutsServiceLoader
```
 Component service class loader.
 \@author vpc
 \@param \<T\> component type
 \@param \<B\> component support constraint type
 \@since 0.5.4
 \@category SPI Base

### ⚙ Instance Methods
#### ⚙ loadAll(criteria)
load all NutsComponent instances matching criteria

```java
List loadAll(NutsSupportLevelContext criteria)
```
**return**:List
- **NutsSupportLevelContext criteria** : criteria to match

#### ⚙ loadBest(criteria)
load best NutsComponent instance matching criteria

```java
NutsComponent loadBest(NutsSupportLevelContext criteria)
```
**return**:NutsComponent
- **NutsSupportLevelContext criteria** : criteria to match

## ☕ NutsSessionTerminalBase
```java
public interface net.vpc.app.nuts.NutsSessionTerminalBase
```
 Session Terminal Base instance are special Terminal Base classes instances that handle workspace session.

 \@since 0.5.4
 \@category SPI Base

## ☕ NutsSingleton
```java
public interface net.vpc.app.nuts.NutsSingleton
```
 classes that are marked with this annotation will be created once by the
 factory.

 \@since 0.5.4
 \@category SPI Base

## ☕ NutsSupportLevelContext
```java
public interface net.vpc.app.nuts.NutsSupportLevelContext
```

 \@author vpc
 \@category SPI Base

### 🎛 Instance Properties
#### 📄🎛 constraints

```java
[read-only] Object public constraints
public Object getConstraints()
```
#### 📄🎛 workspace

```java
[read-only] NutsWorkspace public workspace
public NutsWorkspace getWorkspace()
```
## ☕ NutsSystemTerminalBase
```java
public interface net.vpc.app.nuts.NutsSystemTerminalBase
```
 Created by vpc on 2/20/17.

 \@since 0.5.4
 \@category SPI Base

## ☕ NutsTerminalBase
```java
public interface net.vpc.app.nuts.NutsTerminalBase
```
 Created by vpc on 2/20/17.

 \@since 0.5.4
 \@category SPI Base

### 🎛 Instance Properties
#### 📄🎛 err

```java
[read-only] PrintStream public err
public PrintStream getErr()
```
#### 📄🎛 errMode

```java
[read-only] NutsTerminalMode public errMode
public NutsTerminalMode getErrMode()
```
#### 📄🎛 in

```java
[read-only] InputStream public in
public InputStream getIn()
```
#### 📄🎛 out

```java
[read-only] PrintStream public out
public PrintStream getOut()
```
#### 📄🎛 outMode

```java
[read-only] NutsTerminalMode public outMode
public NutsTerminalMode getOutMode()
```
#### 📄🎛 parent

```java
[read-only] NutsTerminalBase public parent
public NutsTerminalBase getParent()
```
### ⚙ Instance Methods
#### ⚙ readLine(out, prompt, params)


```java
String readLine(PrintStream out, String prompt, Object[] params)
```
**return**:String
- **PrintStream out** : 
- **String prompt** : 
- **Object[] params** : 

#### ⚙ readPassword(out, prompt, params)


```java
char[] readPassword(PrintStream out, String prompt, Object[] params)
```
**return**:char[]
- **PrintStream out** : 
- **String prompt** : 
- **Object[] params** : 

#### ⚙ setErrMode(mode)


```java
NutsTerminalBase setErrMode(NutsTerminalMode mode)
```
**return**:NutsTerminalBase
- **NutsTerminalMode mode** : 

#### ⚙ setOutMode(mode)


```java
NutsTerminalBase setOutMode(NutsTerminalMode mode)
```
**return**:NutsTerminalBase
- **NutsTerminalMode mode** : 

## ☕ NutsTransportComponent
```java
public interface net.vpc.app.nuts.NutsTransportComponent
```
 Transport component responsible of creating a connexion to remote servers.
 Should handle at least valid http connections.
 \@since 0.5.4
 \@category SPI Base

### ⚙ Instance Methods
#### ⚙ open(url)
open url and return a valid \{\@link NutsTransportConnection\}

```java
NutsTransportConnection open(String url)
```
**return**:NutsTransportConnection
- **String url** : url to open

## ☕ NutsTransportConnection
```java
public interface net.vpc.app.nuts.NutsTransportConnection
```
 Connection to a remote server.

 \@since 0.5.4
 \@category SPI Base

### 🎛 Instance Properties
#### 📄🎛 uRLHeader
parse connection header and return meaningful information
```java
[read-only] NutsURLHeader public uRLHeader
public NutsURLHeader getURLHeader()
```
### ⚙ Instance Methods
#### ⚙ open()
option connection and retrieve input stream

```java
InputStream open()
```
**return**:InputStream

#### ⚙ upload(parts)
parse connection header and return meaningful information

```java
InputStream upload(NutsTransportParamPart[] parts)
```
**return**:InputStream
- **NutsTransportParamPart[] parts** : parts to upload

## ☕ NutsTransportParamBinaryFilePart
```java
public net.vpc.app.nuts.NutsTransportParamBinaryFilePart
```
 Created by vpc on 1/8/17.

 \@since 0.5.4
 \@category SPI Base

### 🪄 Constructors
#### 🪄 NutsTransportParamBinaryFilePart(name, fileName, value)


```java
NutsTransportParamBinaryFilePart(String name, String fileName, Path value)
```
- **String name** : 
- **String fileName** : 
- **Path value** : 

### 🎛 Instance Properties
#### 📄🎛 fileName

```java
[read-only] String public fileName
public String getFileName()
```
#### 📄🎛 name

```java
[read-only] String public name
public String getName()
```
#### 📄🎛 value

```java
[read-only] Path public value
public Path getValue()
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

## ☕ NutsTransportParamBinaryStreamPart
```java
public net.vpc.app.nuts.NutsTransportParamBinaryStreamPart
```
 Created by vpc on 1/8/17.

 \@since 0.5.4
 \@category SPI Base

### 🪄 Constructors
#### 🪄 NutsTransportParamBinaryStreamPart(name, fileName, value)


```java
NutsTransportParamBinaryStreamPart(String name, String fileName, InputStream value)
```
- **String name** : 
- **String fileName** : 
- **InputStream value** : 

### 🎛 Instance Properties
#### 📄🎛 fileName

```java
[read-only] String public fileName
public String getFileName()
```
#### 📄🎛 name

```java
[read-only] String public name
public String getName()
```
#### 📄🎛 value

```java
[read-only] InputStream public value
public InputStream getValue()
```
## ☕ NutsTransportParamParamPart
```java
public net.vpc.app.nuts.NutsTransportParamParamPart
```
 Created by vpc on 1/8/17.

 \@since 0.5.4
 \@category SPI Base

### 🪄 Constructors
#### 🪄 NutsTransportParamParamPart(name, value)


```java
NutsTransportParamParamPart(String name, String value)
```
- **String name** : 
- **String value** : 

### 🎛 Instance Properties
#### 📄🎛 name

```java
[read-only] String public name
public String getName()
```
#### 📄🎛 value

```java
[read-only] String public value
public String getValue()
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

## ☕ NutsTransportParamPart
```java
public net.vpc.app.nuts.NutsTransportParamPart
```
 Created by vpc on 1/8/17.

 \@since 0.5.4
 \@category SPI Base

### 🪄 Constructors
#### 🪄 NutsTransportParamPart()


```java
NutsTransportParamPart()
```

## ☕ NutsTransportParamTextFilePart
```java
public net.vpc.app.nuts.NutsTransportParamTextFilePart
```
 Created by vpc on 1/8/17.

 \@since 0.5.4
 \@category SPI Base

### 🪄 Constructors
#### 🪄 NutsTransportParamTextFilePart(name, fileName, value)


```java
NutsTransportParamTextFilePart(String name, String fileName, Path value)
```
- **String name** : 
- **String fileName** : 
- **Path value** : 

### 🎛 Instance Properties
#### 📄🎛 fileName

```java
[read-only] String public fileName
public String getFileName()
```
#### 📄🎛 name

```java
[read-only] String public name
public String getName()
```
#### 📄🎛 value

```java
[read-only] Path public value
public Path getValue()
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

## ☕ NutsTransportParamTextReaderPart
```java
public net.vpc.app.nuts.NutsTransportParamTextReaderPart
```
 Created by vpc on 1/8/17.

 \@since 0.5.4
 \@category SPI Base

### 🪄 Constructors
#### 🪄 NutsTransportParamTextReaderPart(name, fileName, value)


```java
NutsTransportParamTextReaderPart(String name, String fileName, Reader value)
```
- **String name** : 
- **String fileName** : 
- **Reader value** : 

### 🎛 Instance Properties
#### 📄🎛 fileName

```java
[read-only] String public fileName
public String getFileName()
```
#### 📄🎛 name

```java
[read-only] String public name
public String getName()
```
#### 📄🎛 value

```java
[read-only] Reader public value
public Reader getValue()
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

## ☕ NutsURLHeader
```java
public interface net.vpc.app.nuts.NutsURLHeader
```
 url header meaning ful information
 \@author vpc
 \@since 0.5.4
 \@category SPI Base

### 🎛 Instance Properties
#### 📄🎛 contentEncoding
url content encoding
```java
[read-only] String public contentEncoding
public String getContentEncoding()
```
#### 📄🎛 contentLength
url content length (file size)
```java
[read-only] long public contentLength
public long getContentLength()
```
#### 📄🎛 contentType
url content type (file type)
```java
[read-only] String public contentType
public String getContentType()
```
#### 📄🎛 lastModified
url content last modified
```java
[read-only] Instant public lastModified
public Instant getLastModified()
```
#### 📄🎛 url
url value
```java
[read-only] String public url
public String getUrl()
```
## ☕ NutsUpdateRepositoryStatisticsCommand
```java
public interface net.vpc.app.nuts.NutsUpdateRepositoryStatisticsCommand
```

 \@author vpc
 \@since 0.5.5
 \@category SPI Base

### ⚙ Instance Methods
#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsUpdateRepositoryStatisticsCommand configure(boolean skipUnsupported, String[] args)
```
**return**:NutsUpdateRepositoryStatisticsCommand
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ run()
run this command and return \{\@code this\} instance

```java
NutsUpdateRepositoryStatisticsCommand run()
```
**return**:NutsUpdateRepositoryStatisticsCommand

#### ⚙ setSession(session)


```java
NutsUpdateRepositoryStatisticsCommand setSession(NutsSession session)
```
**return**:NutsUpdateRepositoryStatisticsCommand
- **NutsSession session** : 

## ☕ NutsWorkspaceArchetypeComponent
```java
public interface net.vpc.app.nuts.NutsWorkspaceArchetypeComponent
```
 Created by vpc on 1/23/17.

 \@since 0.5.4
 \@category SPI Base

### 🎛 Instance Properties
#### 📄🎛 name

```java
[read-only] String public name
public String getName()
```
### ⚙ Instance Methods
#### ⚙ initialize(session)


```java
void initialize(NutsSession session)
```
- **NutsSession session** : 

## ☕ NutsWorkspaceAware
```java
public interface net.vpc.app.nuts.NutsWorkspaceAware
```
 classes that implement this class will have their method \{\@link #setWorkspace(NutsWorkspace)\}
 called upon its creation (by factory) with a non \{\@code null\} argument to \<strong\>initialize\</strong\>.
 They \<strong\>may\</strong\> accept a call with a \{\@code null\}
 argument later to \<strong\>dispose\</strong\> the instance.
 \@author vpc
 \@category SPI Base

### 🎛 Instance Properties
#### ✏🎛 workspace
initialize or dispose the instance.
 when workspace is not null, the instance should initialize it values
 accordingly.
 when workspace is null, the instance should dispose resources.
```java
[write-only] void public workspace
public void setWorkspace(workspace)
```
## ☕ NutsWorkspaceInitInformation
```java
public interface net.vpc.app.nuts.NutsWorkspaceInitInformation
```
 workspace initialization options.

 Created by vpc on 1/23/17.

 \@since 0.5.7
 \@category SPI Base

### 🎛 Instance Properties
#### 📄🎛 apiId

```java
[read-only] String public apiId
public String getApiId()
```
#### 📄🎛 apiVersion

```java
[read-only] String public apiVersion
public String getApiVersion()
```
#### 📄🎛 bootRepositories

```java
[read-only] String public bootRepositories
public String getBootRepositories()
```
#### 📄🎛 bootWorkspaceFactory

```java
[read-only] NutsBootWorkspaceFactory public bootWorkspaceFactory
public NutsBootWorkspaceFactory getBootWorkspaceFactory()
```
#### 📄🎛 classWorldLoader

```java
[read-only] ClassLoader public classWorldLoader
public ClassLoader getClassWorldLoader()
```
#### 📄🎛 classWorldURLs

```java
[read-only] URL[] public classWorldURLs
public URL[] getClassWorldURLs()
```
#### 📄🎛 extensionDependencies

```java
[read-only] String public extensionDependencies
public String getExtensionDependencies()
```
#### 📄🎛 extensionDependenciesSet

```java
[read-only] Set public extensionDependenciesSet
public Set getExtensionDependenciesSet()
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
#### 📄🎛 runtimeDependenciesSet

```java
[read-only] Set public runtimeDependenciesSet
public Set getRuntimeDependenciesSet()
```
#### 📄🎛 runtimeId

```java
[read-only] String public runtimeId
public String getRuntimeId()
```
#### 📄🎛 storeLocation

```java
[read-only] String public storeLocation
public String getStoreLocation(location)
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
#### 📄🎛 uuid

```java
[read-only] String public uuid
public String getUuid()
```
#### 📄🎛 workspaceLocation

```java
[read-only] String public workspaceLocation
public String getWorkspaceLocation()
```

---
id: javadoc_Base
title: Base
sidebar_label: Base
---


```
     __        __    
  /\ \ \ _  __/ /______
 /  \/ / / / / __/ ___/
/ /\  / /_/ / /_(__  )
\_\ \/\__,_/\__/____/    version 0.7.0
```

## ☕ Nuts
```java
public final net.vpc.app.nuts.Nuts
```
 Nuts Top Class. Nuts is a Package manager for Java Applications and this class is
 it\'s main class for creating and opening nuts workspaces.

 \@since 0.1.0
 \@category Base

### 📢🎛 Static Properties
#### 📄📢🎛 platformOsFamily
default OS family, resolvable before booting nuts workspace
```java
[read-only] NutsOsFamily public static platformOsFamily
public static NutsOsFamily getPlatformOsFamily()
```
#### 📄📢🎛 version
current Nuts version
```java
[read-only] String public static version
public static String version
public static String getVersion()
```
### 📢⚙ Static Methods
#### 📢⚙ getPlatformHomeFolder(storeLocationLayout, folderType, homeLocations, global, workspaceName)
resolves nuts home folder.Home folder is the root for nuts folders.It
 depends on folder type and store layout. For instance log folder depends
 on on the underlying operating system (linux,windows,...).
 Specifications: XDG Base Directory Specification
 (https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html)

```java
String getPlatformHomeFolder(NutsOsFamily storeLocationLayout, NutsStoreLocation folderType, Map homeLocations, boolean global, String workspaceName)
```
**return**:String
- **NutsOsFamily storeLocationLayout** : location layout to resolve home for
- **NutsStoreLocation folderType** : folder type to resolve home for
- **Map homeLocations** : workspace home locations
- **boolean global** : global workspace
- **String workspaceName** : workspace name or id (discriminator)

#### 📢⚙ main(args)
main method. This Main will call
 \{\@link Nuts#runWorkspace(java.lang.String...)\} then
 \{\@link System#exit(int)\} at completion

```java
void main(String[] args)
```
- **String[] args** : main arguments

#### 📢⚙ openInheritedWorkspace(args)
opens a workspace using "nuts.boot.args" and "nut.args" system
 properties. "nuts.boot.args" is to be passed by nuts parent process.
 "nuts.args" is an optional property that can be \'exec\' method. This
 method is to be called by child processes of nuts in order to inherit
 workspace configuration.

```java
NutsWorkspace openInheritedWorkspace(String[] args)
```
**return**:NutsWorkspace
- **String[] args** : arguments

#### 📢⚙ openWorkspace()
open default workspace (no boot options)

```java
NutsWorkspace openWorkspace()
```
**return**:NutsWorkspace

#### 📢⚙ openWorkspace(args)
open a workspace. Nuts Boot arguments are passed in \<code\>args\</code\>

```java
NutsWorkspace openWorkspace(String[] args)
```
**return**:NutsWorkspace
- **String[] args** : nuts boot arguments

#### 📢⚙ openWorkspace(options)
open a workspace using the given options

```java
NutsWorkspace openWorkspace(NutsWorkspaceOptions options)
```
**return**:NutsWorkspace
- **NutsWorkspaceOptions options** : boot options

#### 📢⚙ parseNutsArguments(bootArguments)
Create a \{\@link NutsWorkspaceOptions\} instance from string array of valid
 nuts options

```java
NutsWorkspaceOptions parseNutsArguments(String[] bootArguments)
```
**return**:NutsWorkspaceOptions
- **String[] bootArguments** : input arguments to parse

#### 📢⚙ runWorkspace(args)
open then run Nuts application with the provided arguments. This Main
 will
 \<strong\>NEVER\</strong\>
 call \{\@link System#exit(int)\}.

```java
void runWorkspace(String[] args)
```
- **String[] args** : boot arguments

## ☕ NutsArtifactCall
```java
public interface net.vpc.app.nuts.NutsArtifactCall
```
 artifact call descriptor used to define executor and installer call definitions.

 \@since 0.5.4
 \@category Base

### 🎛 Instance Properties
#### 📄🎛 arguments
execution arguments
```java
[read-only] String[] public arguments
public String[] getArguments()
```
#### 📄🎛 id
artifact id
```java
[read-only] NutsId public id
public NutsId getId()
```
#### 📄🎛 properties
execution properties
```java
[read-only] Map public properties
public Map getProperties()
```
## ☕ NutsArtifactCallBuilder
```java
public interface net.vpc.app.nuts.NutsArtifactCallBuilder
```
 NutsArtifactCallBuilder is responsible of building instances of \{\@code NutsArtifactCall\} to be used
 as NutsDescriptor executor or installer.
 To get an instance of NutsArtifactCallBuilder you can use \{\@code workspace.descriptor().callBuilder()\}

 \@since 0.5.4
 \@category Base

### 🎛 Instance Properties
#### 📝🎛 arguments
update arguments
```java
[read-write] NutsArtifactCallBuilder public arguments
public String[] getArguments()
public NutsArtifactCallBuilder setArguments(value)
```
#### 📝🎛 id
update artifact id
```java
[read-write] NutsArtifactCallBuilder public id
public NutsId getId()
public NutsArtifactCallBuilder setId(value)
```
#### 📝🎛 properties
update call properties map (replace all existing properties)
```java
[read-write] NutsArtifactCallBuilder public properties
public Map getProperties()
public NutsArtifactCallBuilder setProperties(value)
```
### ⚙ Instance Methods
#### ⚙ build()
create an immutable instance of \{\@link NutsArtifactCall\}
 initialized with all of this attributes.

```java
NutsArtifactCall build()
```
**return**:NutsArtifactCall

#### ⚙ clear()
reset this instance to default (null) values

```java
NutsArtifactCallBuilder clear()
```
**return**:NutsArtifactCallBuilder

#### ⚙ set(value)
initialize this instance from the given value

```java
NutsArtifactCallBuilder set(NutsArtifactCallBuilder value)
```
**return**:NutsArtifactCallBuilder
- **NutsArtifactCallBuilder value** : copy from value

#### ⚙ set(value)
initialize this instance from the given value

```java
NutsArtifactCallBuilder set(NutsArtifactCall value)
```
**return**:NutsArtifactCallBuilder
- **NutsArtifactCall value** : copy from value

## ☕ NutsConfirmationMode
```java
public final net.vpc.app.nuts.NutsConfirmationMode
```
 user interaction mode. Some operations may require user confirmation before
 performing critical operations such as overriding existing values, deleting
 sensitive information ; in such cases several modes are available : either
 to require user interaction (ASK mode, the default value) or force the
 processing (YES mode), or ignoring the processing and continuing the next
 (NO) or cancel the processing and exit with an error message (ERROR)

 \@author vpc
 \@since 0.5.5
 \@category Base

### 📢❄ Constant Fields
#### 📢❄ ASK
```java
public static final NutsConfirmationMode ASK
```
#### 📢❄ ERROR
```java
public static final NutsConfirmationMode ERROR
```
#### 📢❄ NO
```java
public static final NutsConfirmationMode NO
```
#### 📢❄ YES
```java
public static final NutsConfirmationMode YES
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
NutsConfirmationMode valueOf(String name)
```
**return**:NutsConfirmationMode
- **String name** : 

#### 📢⚙ values()


```java
NutsConfirmationMode[] values()
```
**return**:NutsConfirmationMode[]

### ⚙ Instance Methods
#### ⚙ id()
lower cased identifier.

```java
String id()
```
**return**:String

## ☕ NutsContentEvent
```java
public interface net.vpc.app.nuts.NutsContentEvent
```
 Event for \{\@link NutsRepositoryListener\} methods.
 \@author vpc
 \@since 0.5.3
 \@category Base

### 🎛 Instance Properties
#### 📄🎛 path
artifact path
```java
[read-only] Path public path
public Path getPath()
```
#### 📄🎛 repository
current repository
```java
[read-only] NutsRepository public repository
public NutsRepository getRepository()
```
#### 📄🎛 session
current session
```java
[read-only] NutsSession public session
public NutsSession getSession()
```
#### 📄🎛 workspace
current workspace
```java
[read-only] NutsWorkspace public workspace
public NutsWorkspace getWorkspace()
```
## ☕ NutsDefinition
```java
public interface net.vpc.app.nuts.NutsDefinition
```
 Definition is an \<strong\>immutable\</strong\> object that contains all information about a artifact identified by it\'s Id.

 \@since 0.5.4
 \@category Base

### 🎛 Instance Properties
#### 📄🎛 apiId
return target api id (included in dependency) for the current id.
 This is relevant for runtime, extension and companion ids.
 For other regular ids, this returns null.
```java
[read-only] NutsId public apiId
public NutsId getApiId()
```
#### 📄🎛 content
return artifact content file info (including path).
 this is an \<strong\>optional\</strong\> property. It must be requested (see \{\@link NutsSearchCommand#setContent(boolean)\}) to be available.
```java
[read-only] NutsContent public content
public NutsContent getContent()
```
#### 📄🎛 dependencies
return all or some of the transitive dependencies of the current Nuts as List
 result of the search command
 this is an \<strong\>optional\</strong\> property.
 It must be requested (see \{\@link NutsSearchCommand#setDependencies(boolean)\} to be available.
```java
[read-only] NutsDependency[] public dependencies
public NutsDependency[] getDependencies()
```
#### 📄🎛 dependencyNodes
return all of some of the transitive dependencies of the current Nuts as Tree result of the search command
 this is an \<strong\>optional\</strong\> property.
 It must be requested (see \{\@link NutsSearchCommand#setDependenciesTree(boolean)\} to be available.
```java
[read-only] NutsDependencyTreeNode[] public dependencyNodes
public NutsDependencyTreeNode[] getDependencyNodes()
```
#### 📄🎛 descriptor
return artifact descriptor
```java
[read-only] NutsDescriptor public descriptor
public NutsDescriptor getDescriptor()
```
#### 📄🎛 effectiveDescriptor
return artifact effective descriptor.
 this is an \<strong\>optional\</strong\> property.
 It must be requested (see \{\@link NutsSearchCommand#setEffective(boolean)\} to be available).
```java
[read-only] NutsDescriptor public effectiveDescriptor
public NutsDescriptor getEffectiveDescriptor()
```
#### 📄🎛 id
artifact id
```java
[read-only] NutsId public id
public NutsId getId()
```
#### 📄🎛 installInformation
return artifact install information.
```java
[read-only] NutsInstallInformation public installInformation
public NutsInstallInformation getInstallInformation()
```
#### 📄🎛 path
return artifact content file path.
 this is an \<strong\>optional\</strong\> property. It must be requested (see \{\@link NutsSearchCommand#setContent(boolean)\}) to be available.
```java
[read-only] Path public path
public Path getPath()
```
#### 📄🎛 repositoryName
name of the repository providing this id.
```java
[read-only] String public repositoryName
public String getRepositoryName()
```
#### 📄🎛 repositoryUuid
id of the repository providing this id.
```java
[read-only] String public repositoryUuid
public String getRepositoryUuid()
```
#### 📄🎛 setDependencies
true if requested content
```java
[read-only] boolean public setDependencies
public boolean isSetDependencies()
```
#### 📄🎛 setDependencyNodes
true if requested content
```java
[read-only] boolean public setDependencyNodes
public boolean isSetDependencyNodes()
```
#### 📄🎛 setEffectiveDescriptor
true if requested effective descriptor
```java
[read-only] boolean public setEffectiveDescriptor
public boolean isSetEffectiveDescriptor()
```
#### 📄🎛 type
return artifact type
```java
[read-only] NutsIdType public type
public NutsIdType getType()
```
### ⚙ Instance Methods
#### ⚙ compareTo(other)
Compares this object with the specified definition for order.
 This is equivalent to comparing subsequent ids.

```java
int compareTo(NutsDefinition other)
```
**return**:int
- **NutsDefinition other** : other definition to compare with

## ☕ NutsExecutionContext
```java
public interface net.vpc.app.nuts.NutsExecutionContext
```
 execution context used in \{\@link NutsExecutorComponent\} and
 \{\@link NutsInstallerComponent\}.

 \@author vpc
 \@since 0.5.4
 \@category Base

### 🎛 Instance Properties
#### 📄🎛 arguments
command arguments
```java
[read-only] String[] public arguments
public String[] getArguments()
```
#### 📄🎛 commandName
command name
```java
[read-only] String public commandName
public String getCommandName()
```
#### 📄🎛 cwd
current working directory
```java
[read-only] String public cwd
public String getCwd()
```
#### 📄🎛 definition
command definition if any
```java
[read-only] NutsDefinition public definition
public NutsDefinition getDefinition()
```
#### 📄🎛 env
execution environment
```java
[read-only] Map public env
public Map getEnv()
```
#### 📄🎛 execSession
current session
```java
[read-only] NutsSession public execSession
public NutsSession getExecSession()
```
#### 📄🎛 executionType
execution type
```java
[read-only] NutsExecutionType public executionType
public NutsExecutionType getExecutionType()
```
#### 📄🎛 executorDescriptor
executor descriptor
```java
[read-only] NutsArtifactCall public executorDescriptor
public NutsArtifactCall getExecutorDescriptor()
```
#### 📄🎛 executorOptions
executor options
```java
[read-only] String[] public executorOptions
public String[] getExecutorOptions()
```
#### 📄🎛 executorProperties
executor properties
```java
[read-only] Map public executorProperties
public Map getExecutorProperties()
```
#### 📄🎛 failFast
when true, any non 0 exited command will throw an Exception
```java
[read-only] boolean public failFast
public boolean isFailFast()
```
#### 📄🎛 temporary
when true, the component is temporary and is not registered withing the
 workspace
```java
[read-only] boolean public temporary
public boolean isTemporary()
```
#### 📄🎛 traceSession

```java
[read-only] NutsSession public traceSession
public NutsSession getTraceSession()
```
#### 📄🎛 workspace
workspace
```java
[read-only] NutsWorkspace public workspace
public NutsWorkspace getWorkspace()
```
### ⚙ Instance Methods
#### ⚙ workspace()
workspace

```java
NutsWorkspace workspace()
```
**return**:NutsWorkspace

## ☕ NutsIOCompressAction
```java
public interface net.vpc.app.nuts.NutsIOCompressAction
```
 I/O Action that help monitored compress
 of one or multiple resource types.
 Default implementation should handle

 \@author vpc
 \@since 0.5.4
 \@category Base

### 🎛 Instance Properties
#### 📝🎛 format
update format
```java
[read-write] NutsIOCompressAction public format
public String getFormat()
public NutsIOCompressAction setFormat(format)
```
#### 📝🎛 logProgress
switch log progress flag to \{\@code value\}.
```java
[read-write] NutsIOCompressAction public logProgress
public boolean isLogProgress()
public NutsIOCompressAction setLogProgress(value)
```
#### ✏🎛 progressMonitor
set progress monitor. Will create a singleton progress monitor factory
```java
[write-only] NutsIOCompressAction public progressMonitor
public NutsIOCompressAction setProgressMonitor(value)
```
#### 📝🎛 progressMonitorFactory
set progress factory responsible of creating progress monitor
```java
[read-write] NutsIOCompressAction public progressMonitorFactory
public NutsProgressFactory getProgressMonitorFactory()
public NutsIOCompressAction setProgressMonitorFactory(value)
```
#### 📝🎛 safe
switch safe copy flag to \{\@code value\}
```java
[read-write] NutsIOCompressAction public safe
public boolean isSafe()
public NutsIOCompressAction setSafe(value)
```
#### 📝🎛 session
update current session
```java
[read-write] NutsIOCompressAction public session
public NutsSession getSession()
public NutsIOCompressAction setSession(session)
```
#### 📝🎛 skipRoot
set skip root flag to \{\@code value\}
```java
[read-write] NutsIOCompressAction public skipRoot
public boolean isSkipRoot()
public NutsIOCompressAction setSkipRoot(value)
```
#### 📄🎛 sources
sources to compress
```java
[read-only] List public sources
public List getSources()
```
#### 📝🎛 target
update target
```java
[read-write] NutsIOCompressAction public target
public Object getTarget()
public NutsIOCompressAction setTarget(target)
```
### ⚙ Instance Methods
#### ⚙ addSource(source)
add source to compress

```java
NutsIOCompressAction addSource(String source)
```
**return**:NutsIOCompressAction
- **String source** : source

#### ⚙ addSource(source)
add source to compress

```java
NutsIOCompressAction addSource(InputStream source)
```
**return**:NutsIOCompressAction
- **InputStream source** : source

#### ⚙ addSource(source)
add source to compress

```java
NutsIOCompressAction addSource(File source)
```
**return**:NutsIOCompressAction
- **File source** : source

#### ⚙ addSource(source)
add source to compress

```java
NutsIOCompressAction addSource(Path source)
```
**return**:NutsIOCompressAction
- **Path source** : source

#### ⚙ addSource(source)
add source to compress

```java
NutsIOCompressAction addSource(URL source)
```
**return**:NutsIOCompressAction
- **URL source** : source

#### ⚙ getFormatOption(option)
return format option

```java
Object getFormatOption(String option)
```
**return**:Object
- **String option** : option name

#### ⚙ logProgress()
switch log progress flag to to true.

```java
NutsIOCompressAction logProgress()
```
**return**:NutsIOCompressAction

#### ⚙ logProgress(value)
switch log progress flag to \{\@code value\}.

```java
NutsIOCompressAction logProgress(boolean value)
```
**return**:NutsIOCompressAction
- **boolean value** : value

#### ⚙ progressMonitor(value)
set progress monitor. Will create a singleton progress monitor factory

```java
NutsIOCompressAction progressMonitor(NutsProgressMonitor value)
```
**return**:NutsIOCompressAction
- **NutsProgressMonitor value** : new value

#### ⚙ progressMonitorFactory(value)
set progress factory responsible of creating progress monitor

```java
NutsIOCompressAction progressMonitorFactory(NutsProgressFactory value)
```
**return**:NutsIOCompressAction
- **NutsProgressFactory value** : new value

#### ⚙ run()
run this Compress action

```java
NutsIOCompressAction run()
```
**return**:NutsIOCompressAction

#### ⚙ safe()
arm safe copy flag

```java
NutsIOCompressAction safe()
```
**return**:NutsIOCompressAction

#### ⚙ safe(value)
switch safe copy flag to \{\@code value\}

```java
NutsIOCompressAction safe(boolean value)
```
**return**:NutsIOCompressAction
- **boolean value** : value

#### ⚙ setFormatOption(option, value)
update format option

```java
NutsIOCompressAction setFormatOption(String option, Object value)
```
**return**:NutsIOCompressAction
- **String option** : option name
- **Object value** : value

#### ⚙ skipRoot()
set skip root flag to \{\@code true\}

```java
NutsIOCompressAction skipRoot()
```
**return**:NutsIOCompressAction

#### ⚙ skipRoot(value)
set skip root flag to \{\@code value\}

```java
NutsIOCompressAction skipRoot(boolean value)
```
**return**:NutsIOCompressAction
- **boolean value** : new value

#### ⚙ to(target)
update target

```java
NutsIOCompressAction to(OutputStream target)
```
**return**:NutsIOCompressAction
- **OutputStream target** : target

#### ⚙ to(target)
update target

```java
NutsIOCompressAction to(String target)
```
**return**:NutsIOCompressAction
- **String target** : target

#### ⚙ to(target)
update target

```java
NutsIOCompressAction to(Path target)
```
**return**:NutsIOCompressAction
- **Path target** : target

#### ⚙ to(target)
update target

```java
NutsIOCompressAction to(File target)
```
**return**:NutsIOCompressAction
- **File target** : target

#### ⚙ to(target)
update target

```java
NutsIOCompressAction to(Object target)
```
**return**:NutsIOCompressAction
- **Object target** : target

## ☕ NutsIdLocationBuilder
```java
public interface net.vpc.app.nuts.NutsIdLocationBuilder
```
 Mutable IdLocation class that helps creating instance of immutable \{\@link NutsIdLocation\}.
 Instances of \{\@link NutsIdLocation\} are used in \{\@link NutsDescriptor\} (see \{\@link NutsDescriptor#getLocations()\})

 \@category Base

### 🎛 Instance Properties
#### 📝🎛 classifier
update location classifier
```java
[read-write] NutsIdLocationBuilder public classifier
public String getClassifier()
public NutsIdLocationBuilder setClassifier(value)
```
#### 📝🎛 region
update location region
```java
[read-write] NutsIdLocationBuilder public region
public String getRegion()
public NutsIdLocationBuilder setRegion(value)
```
#### 📝🎛 url
update location url
```java
[read-write] NutsIdLocationBuilder public url
public String getUrl()
public NutsIdLocationBuilder setUrl(value)
```
### ⚙ Instance Methods
#### ⚙ build()
create new instance of \{\@link NutsIdLocation\} initialized with this builder values.

```java
NutsIdLocation build()
```
**return**:NutsIdLocation

#### ⚙ classifier(value)
update location classifier

```java
NutsIdLocationBuilder classifier(String value)
```
**return**:NutsIdLocationBuilder
- **String value** : location classifier

#### ⚙ clear()
clear this instance (set null/default all properties)

```java
NutsIdLocationBuilder clear()
```
**return**:NutsIdLocationBuilder

#### ⚙ region(value)
update location region

```java
NutsIdLocationBuilder region(String value)
```
**return**:NutsIdLocationBuilder
- **String value** : location region

#### ⚙ set(value)
update all attributes, copy from \{\@code value\} instance

```java
NutsIdLocationBuilder set(NutsIdLocationBuilder value)
```
**return**:NutsIdLocationBuilder
- **NutsIdLocationBuilder value** : instance to copy from

#### ⚙ set(value)
update all attributes, copy from \{\@code value\} instance

```java
NutsIdLocationBuilder set(NutsIdLocation value)
```
**return**:NutsIdLocationBuilder
- **NutsIdLocation value** : instance to copy from

#### ⚙ url(value)
update location url

```java
NutsIdLocationBuilder url(String value)
```
**return**:NutsIdLocationBuilder
- **String value** : location url

## ☕ NutsIdType
```java
public final net.vpc.app.nuts.NutsIdType
```
 Artifacts are organized according to \{\@code NutsIdType\} to reflect how the artifact
 should be managed by the workspace.
 This information is available in \{\@link NutsDefinition\}
 \@category Base

### 📢❄ Constant Fields
#### 📢❄ API
```java
public static final NutsIdType API
```
#### 📢❄ COMPANION
```java
public static final NutsIdType COMPANION
```
#### 📢❄ EXTENSION
```java
public static final NutsIdType EXTENSION
```
#### 📢❄ REGULAR
```java
public static final NutsIdType REGULAR
```
#### 📢❄ RUNTIME
```java
public static final NutsIdType RUNTIME
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
NutsIdType valueOf(String name)
```
**return**:NutsIdType
- **String name** : 

#### 📢⚙ values()


```java
NutsIdType[] values()
```
**return**:NutsIdType[]

## ☕ NutsIndexStore
```java
public interface net.vpc.app.nuts.NutsIndexStore
```
 Classes implementations of \{\@code NutsIndexStore\} handle
 indexing of repositories to enable faster search.
 \@author vpc
 \@since 0.5.4
 \@category Base

### 🎛 Instance Properties
#### 📝🎛 enabled
enable of disable ot index
```java
[read-write] NutsIndexStore public enabled
public boolean isEnabled()
public NutsIndexStore setEnabled(enabled)
```
#### 📄🎛 subscribed
return true if the current repository is registered
```java
[read-only] boolean public subscribed
public boolean isSubscribed()
```
### ⚙ Instance Methods
#### ⚙ enabled()
enable index

```java
NutsIndexStore enabled()
```
**return**:NutsIndexStore

#### ⚙ enabled(enabled)
enable of disable ot index

```java
NutsIndexStore enabled(boolean enabled)
```
**return**:NutsIndexStore
- **boolean enabled** : new value

#### ⚙ invalidate(id)
invalidate the artifact from the index

```java
NutsIndexStore invalidate(NutsId id)
```
**return**:NutsIndexStore
- **NutsId id** : id to invalidate

#### ⚙ revalidate(id)
invalidate the artifact from the index and re-index it

```java
NutsIndexStore revalidate(NutsId id)
```
**return**:NutsIndexStore
- **NutsId id** : id to re-index

#### ⚙ search(filter, session)
search all artifacts matching the given filter

```java
Iterator search(NutsIdFilter filter, NutsSession session)
```
**return**:Iterator
- **NutsIdFilter filter** : filter or null for all
- **NutsSession session** : current session

#### ⚙ searchVersions(id, session)
search all versions of the given artifact

```java
Iterator searchVersions(NutsId id, NutsSession session)
```
**return**:Iterator
- **NutsId id** : artifact to search for
- **NutsSession session** : current session

#### ⚙ subscribe()
subscribe the current repository so the indexing
 is processed.

```java
NutsIndexStore subscribe()
```
**return**:NutsIndexStore

#### ⚙ unsubscribe()
unsubscribe the current repository so that the indexing
 is disabled and the index is removed.

```java
NutsIndexStore unsubscribe()
```
**return**:NutsIndexStore

## ☕ NutsIndexStoreFactory
```java
public interface net.vpc.app.nuts.NutsIndexStoreFactory
```
 Index Store Factory responsible of creating stores for a given repository
 \@author vpc
 \@since 0.5.4
 \@category Base

### ⚙ Instance Methods
#### ⚙ createIndexStore(repository)
create a new index store implementation or null if not supported

```java
NutsIndexStore createIndexStore(NutsRepository repository)
```
**return**:NutsIndexStore
- **NutsRepository repository** : repository to greate the index store to

## ☕ NutsInputStreamTransparentAdapter
```java
public interface net.vpc.app.nuts.NutsInputStreamTransparentAdapter
```
 Interface to enable marking system streams. When creating new processes nuts
 will dereference NutsInputStreamTransparentAdapter to check if the
 InputStream i a system io. In that case nuts will "inherit" input stream

 \@since 0.5.4
 \@category Base

### ⚙ Instance Methods
#### ⚙ baseInputStream()
de-referenced stream

```java
InputStream baseInputStream()
```
**return**:InputStream

## ☕ NutsInstallCommand
```java
public interface net.vpc.app.nuts.NutsInstallCommand
```
 Command for installing artifacts
 \@author vpc
 \@since 0.5.4
 \@category Base
 \@category Commands

### 🎛 Instance Properties
#### 📄🎛 args
return all arguments to pass to the install command
```java
[read-only] String[] public args
public String[] getArgs()
```
#### 📝🎛 companions
if true update companions
```java
[read-write] NutsInstallCommand public companions
public boolean isCompanions()
public NutsInstallCommand setCompanions(value)
```
#### 📝🎛 defaultVersion
set default version flag. when true, the installed version will be defined as default
```java
[read-write] NutsInstallCommand public defaultVersion
public boolean isDefaultVersion()
public NutsInstallCommand setDefaultVersion(defaultVersion)
```
#### 📄🎛 ids
return all ids to install
```java
[read-only] NutsId[] public ids
public NutsId[] getIds()
```
#### 📝🎛 installed
if true reinstall installed artifacts
```java
[read-write] NutsInstallCommand public installed
public boolean isInstalled()
public NutsInstallCommand setInstalled(value)
```
#### 📄🎛 result
execute installation and return result.
```java
[read-only] NutsResultList public result
public NutsResultList getResult()
```
#### ✏🎛 session
update session
```java
[write-only] NutsInstallCommand public session
public NutsInstallCommand setSession(session)
```
### ⚙ Instance Methods
#### ⚙ addArg(arg)
add argument to pass to the install command

```java
NutsInstallCommand addArg(String arg)
```
**return**:NutsInstallCommand
- **String arg** : argument

#### ⚙ addArgs(args)
add arguments to pass to the install command

```java
NutsInstallCommand addArgs(Collection args)
```
**return**:NutsInstallCommand
- **Collection args** : argument

#### ⚙ addArgs(args)
add arguments to pass to the install command

```java
NutsInstallCommand addArgs(String[] args)
```
**return**:NutsInstallCommand
- **String[] args** : argument

#### ⚙ addId(id)
add artifact id to install

```java
NutsInstallCommand addId(NutsId id)
```
**return**:NutsInstallCommand
- **NutsId id** : id to install

#### ⚙ addId(id)
add artifact id to install

```java
NutsInstallCommand addId(String id)
```
**return**:NutsInstallCommand
- **String id** : id to install

#### ⚙ addIds(ids)
add artifact ids to install

```java
NutsInstallCommand addIds(NutsId[] ids)
```
**return**:NutsInstallCommand
- **NutsId[] ids** : ids to install

#### ⚙ addIds(ids)
add artifact ids to install

```java
NutsInstallCommand addIds(String[] ids)
```
**return**:NutsInstallCommand
- **String[] ids** : ids to install

#### ⚙ arg(arg)
add argument to pass to the install command

```java
NutsInstallCommand arg(String arg)
```
**return**:NutsInstallCommand
- **String arg** : argument

#### ⚙ args(args)
add arguments to pass to the install command

```java
NutsInstallCommand args(Collection args)
```
**return**:NutsInstallCommand
- **Collection args** : argument

#### ⚙ args(args)
add arguments to pass to the install command

```java
NutsInstallCommand args(String[] args)
```
**return**:NutsInstallCommand
- **String[] args** : argument

#### ⚙ clearArgs()
clear all arguments to pass to the install command

```java
NutsInstallCommand clearArgs()
```
**return**:NutsInstallCommand

#### ⚙ clearIds()
clear ids to install

```java
NutsInstallCommand clearIds()
```
**return**:NutsInstallCommand

#### ⚙ companions()
update companions

```java
NutsInstallCommand companions()
```
**return**:NutsInstallCommand

#### ⚙ companions(value)
if true update companions

```java
NutsInstallCommand companions(boolean value)
```
**return**:NutsInstallCommand
- **boolean value** : flag

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsInstallCommand configure(boolean skipUnsupported, String[] args)
```
**return**:NutsInstallCommand
- **boolean skipUnsupported** : when true, all unsupported options are skipped
- **String[] args** : argument to configure with

#### ⚙ copySession()
copy session

```java
NutsInstallCommand copySession()
```
**return**:NutsInstallCommand

#### ⚙ defaultVersion()
set default version flag. the installed version will be defined as default.

```java
NutsInstallCommand defaultVersion()
```
**return**:NutsInstallCommand

#### ⚙ defaultVersion(defaultVersion)
set default version flag. when true, the installed version will be defined as default

```java
NutsInstallCommand defaultVersion(boolean defaultVersion)
```
**return**:NutsInstallCommand
- **boolean defaultVersion** : when true, the installed version will be defined as
 default

#### ⚙ id(id)
add artifact id to install

```java
NutsInstallCommand id(NutsId id)
```
**return**:NutsInstallCommand
- **NutsId id** : id to install

#### ⚙ id(id)
add artifact id to install

```java
NutsInstallCommand id(String id)
```
**return**:NutsInstallCommand
- **String id** : id to install

#### ⚙ ids(ids)
add artifact ids to install

```java
NutsInstallCommand ids(NutsId[] ids)
```
**return**:NutsInstallCommand
- **NutsId[] ids** : id to install

#### ⚙ ids(ids)
add artifact ids to install

```java
NutsInstallCommand ids(String[] ids)
```
**return**:NutsInstallCommand
- **String[] ids** : id to install

#### ⚙ installed()
reinstall installed artifacts

```java
NutsInstallCommand installed()
```
**return**:NutsInstallCommand

#### ⚙ installed(value)
if true reinstall installed artifacts

```java
NutsInstallCommand installed(boolean value)
```
**return**:NutsInstallCommand
- **boolean value** : flag

#### ⚙ removeId(id)
remove artifact id to install

```java
NutsInstallCommand removeId(NutsId id)
```
**return**:NutsInstallCommand
- **NutsId id** : id to install

#### ⚙ removeId(id)
remove artifact id to install

```java
NutsInstallCommand removeId(String id)
```
**return**:NutsInstallCommand
- **String id** : id to install

#### ⚙ run()
execute the command and return this instance

```java
NutsInstallCommand run()
```
**return**:NutsInstallCommand

## ☕ NutsInstallEvent
```java
public interface net.vpc.app.nuts.NutsInstallEvent
```
 Event describing installation of an artifact
 \@author vpc
 \@since 0.5.6
 \@category Base

### 🎛 Instance Properties
#### 📄🎛 definition
return artifact definition
```java
[read-only] NutsDefinition public definition
public NutsDefinition getDefinition()
```
#### 📄🎛 force
return true if installation was forced
```java
[read-only] boolean public force
public boolean isForce()
```
#### 📄🎛 session
return current session
```java
[read-only] NutsSession public session
public NutsSession getSession()
```
#### 📄🎛 workspace
vcurrent workspace
```java
[read-only] NutsWorkspace public workspace
public NutsWorkspace getWorkspace()
```
## ☕ NutsInstallInformation
```java
public interface net.vpc.app.nuts.NutsInstallInformation
```
 Information about installed artifact
 \@author vpc
 \@since 0.5.5
 \@category Base

### 🎛 Instance Properties
#### 📄🎛 defaultVersion
true when the installed artifact is default version
```java
[read-only] boolean public defaultVersion
public boolean isDefaultVersion()
```
#### 📄🎛 id
installation date
```java
[read-only] NutsId public id
public NutsId getId()
```
#### 📄🎛 installDate
installation date
```java
[read-only] Instant public installDate
public Instant getInstallDate()
```
#### 📄🎛 installFolder
installation formation path.
```java
[read-only] Path public installFolder
public Path getInstallFolder()
```
#### 📄🎛 installStatus
return install status
```java
[read-only] NutsInstallStatus public installStatus
public NutsInstallStatus getInstallStatus()
```
#### 📄🎛 installUser
return the user responsible of the installation
```java
[read-only] String public installUser
public String getInstallUser()
```
#### 📄🎛 installedOrIncluded
return true if installed primary or dependency
```java
[read-only] boolean public installedOrIncluded
public boolean isInstalledOrIncluded()
```
#### 📄🎛 justInstalled
true if the installation just occurred in the very last operation
```java
[read-only] boolean public justInstalled
public boolean isJustInstalled()
```
#### 📄🎛 justReInstalled
true if the re-installation just occurred in the very last operation
```java
[read-only] boolean public justReInstalled
public boolean isJustReInstalled()
```
#### 📄🎛 sourceRepositoryName

```java
[read-only] String public sourceRepositoryName
public String getSourceRepositoryName()
```
#### 📄🎛 sourceRepositoryUUID

```java
[read-only] String public sourceRepositoryUUID
public String getSourceRepositoryUUID()
```
## ☕ NutsInstallStatus
```java
public final net.vpc.app.nuts.NutsInstallStatus
```
 Package installation status
 \@category Base

### 📢❄ Constant Fields
#### 📢❄ INCLUDED
```java
public static final NutsInstallStatus INCLUDED
```
#### 📢❄ INSTALLED
```java
public static final NutsInstallStatus INSTALLED
```
#### 📢❄ INSTALLED_OR_INCLUDED
```java
public static final NutsInstallStatus INSTALLED_OR_INCLUDED
```
#### 📢❄ NOT_INSTALLED
```java
public static final NutsInstallStatus NOT_INSTALLED
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
NutsInstallStatus valueOf(String name)
```
**return**:NutsInstallStatus
- **String name** : 

#### 📢⚙ values()


```java
NutsInstallStatus[] values()
```
**return**:NutsInstallStatus[]

### ⚙ Instance Methods
#### ⚙ id()
lower cased identifier.

```java
String id()
```
**return**:String

## ☕ NutsListener
```java
public interface net.vpc.app.nuts.NutsListener
```
 Anchor interface for all Nuts Listeners.
 \@author vpc
 \@since 0.5.5
 \@category Base

## ☕ NutsMapListener
```java
public interface net.vpc.app.nuts.NutsMapListener
```
 Map Listener to catch updates

 \@param \<K\> key type
 \@param \<V\> value type
 \@since 0.2.0
 \@category Base

### ⚙ Instance Methods
#### ⚙ entryAdded(key, value)
Invoked when item added

```java
void entryAdded(Object key, Object value)
```
- **Object key** : key
- **Object value** : value

#### ⚙ entryRemoved(key, value)
Invoked when item removed

```java
void entryRemoved(Object key, Object value)
```
- **Object key** : key
- **Object value** : value

#### ⚙ entryUpdated(key, newValue, oldValue)
Invoked when item updated

```java
void entryUpdated(Object key, Object newValue, Object oldValue)
```
- **Object key** : key
- **Object newValue** : new value
- **Object oldValue** : old value

## ☕ NutsOsFamily
```java
public final net.vpc.app.nuts.NutsOsFamily
```
 Supported Operating System Families
 \@author vpc
 \@since 0.5.4
 \@category Base

### 📢❄ Constant Fields
#### 📢❄ LINUX
```java
public static final NutsOsFamily LINUX
```
#### 📢❄ MACOS
```java
public static final NutsOsFamily MACOS
```
#### 📢❄ UNIX
```java
public static final NutsOsFamily UNIX
```
#### 📢❄ UNKNOWN
```java
public static final NutsOsFamily UNKNOWN
```
#### 📢❄ WINDOWS
```java
public static final NutsOsFamily WINDOWS
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
NutsOsFamily valueOf(String name)
```
**return**:NutsOsFamily
- **String name** : 

#### 📢⚙ values()


```java
NutsOsFamily[] values()
```
**return**:NutsOsFamily[]

### ⚙ Instance Methods
#### ⚙ id()
lower cased identifier.

```java
String id()
```
**return**:String

## ☕ NutsProcessInfo
```java
public interface net.vpc.app.nuts.NutsProcessInfo
```
 System Process Information
 \@category Base

### 🎛 Instance Properties
#### 📄🎛 commandLine
Process command line
```java
[read-only] String public commandLine
public String getCommandLine()
```
#### 📄🎛 name
Process Name.
 This should represent Fully Qualified Java Main Class Name for java processes.
```java
[read-only] String public name
public String getName()
```
#### 📄🎛 pid
Process Id in string representation
```java
[read-only] String public pid
public String getPid()
```
#### 📄🎛 title
Process Title / Window Title if available
```java
[read-only] String public title
public String getTitle()
```
## ☕ NutsProgressEvent
```java
public interface net.vpc.app.nuts.NutsProgressEvent
```
 Progress event
 \@author vpc
 \@since 0.5.8
 \@category Base

### 🎛 Instance Properties
#### 📄🎛 currentValue
progress current value
```java
[read-only] long public currentValue
public long getCurrentValue()
```
#### 📄🎛 error
error or null
```java
[read-only] Throwable public error
public Throwable getError()
```
#### 📄🎛 indeterminate
when true, max value is unknown, and the progress is indeterminate
```java
[read-only] boolean public indeterminate
public boolean isIndeterminate()
```
#### 📄🎛 maxValue
progress max value or -1 if intermediate
```java
[read-only] long public maxValue
public long getMaxValue()
```
#### 📄🎛 message
event message
```java
[read-only] String public message
public String getMessage()
```
#### 📄🎛 partialMillis
progress time from the starting of the last mark point.
```java
[read-only] long public partialMillis
public long getPartialMillis()
```
#### 📄🎛 partialValue
progress value from the last mark point.
 Mark point occurs when \{\@link NutsProgressMonitor#onProgress(NutsProgressEvent)\} return false.
```java
[read-only] long public partialValue
public long getPartialValue()
```
#### 📄🎛 percent
progress percentage ([0..100])
```java
[read-only] float public percent
public float getPercent()
```
#### 📄🎛 session
Nuts Session
```java
[read-only] NutsSession public session
public NutsSession getSession()
```
#### 📄🎛 source
progress source object
```java
[read-only] Object public source
public Object getSource()
```
#### 📄🎛 timeMillis
progress time from the starting of the progress.
```java
[read-only] long public timeMillis
public long getTimeMillis()
```
## ☕ NutsProgressFactory
```java
public interface net.vpc.app.nuts.NutsProgressFactory
```
 NutsProgressFactory is responsible of creating instances of \{\@link NutsProgressMonitor\}
 \@author vpc
 \@since 0.5.8
 \@category Base

### ⚙ Instance Methods
#### ⚙ create(source, sourceOrigin, session)
create a new instance of \{\@link NutsProgressMonitor\}

```java
NutsProgressMonitor create(Object source, Object sourceOrigin, NutsSession session)
```
**return**:NutsProgressMonitor
- **Object source** : source object of the progress. This may be the File for instance
- **Object sourceOrigin** : source origin object of the progress. This may be the NutsId for instance
- **NutsSession session** : workspace session

## ☕ NutsProgressMonitor
```java
public interface net.vpc.app.nuts.NutsProgressMonitor
```
 Monitor handles events from copy, compress and delete actions
 \@author vpc
 \@since 0.5.8
 \@category Base

### ⚙ Instance Methods
#### ⚙ onComplete(event)
called when the action terminates

```java
void onComplete(NutsProgressEvent event)
```
- **NutsProgressEvent event** : event

#### ⚙ onProgress(event)
called when the action does a step forward and return
 true if the progress was handled of false otherwise.

```java
boolean onProgress(NutsProgressEvent event)
```
**return**:boolean
- **NutsProgressEvent event** : event

#### ⚙ onStart(event)
called when the action starts

```java
void onStart(NutsProgressEvent event)
```
- **NutsProgressEvent event** : event

## ☕ NutsQuestion
```java
public interface net.vpc.app.nuts.NutsQuestion
```
 Question is helpful object that permits user interaction by reading a typed object from
 standard input or an equivalent input system.
 \@param \<T\> value type returned by this question object
 \@author vpc
 \@since 0.5.4
 \@category Base

### 🎛 Instance Properties
#### 📄🎛 acceptedValues

```java
[read-only] Object[] public acceptedValues
public Object[] getAcceptedValues()
```
#### 📄🎛 booleanValue
equivalent to (Boolean) getValue() as type dereferencing may cause some
 troubles
```java
[read-only] Boolean public booleanValue
public Boolean getBooleanValue()
```
#### 📄🎛 defaultValue

```java
[read-only] Object public defaultValue
public Object getDefaultValue()
```
#### 📄🎛 format

```java
[read-only] NutsQuestionFormat public format
public NutsQuestionFormat getFormat()
```
#### 📄🎛 hintMessage

```java
[read-only] String public hintMessage
public String getHintMessage()
```
#### 📄🎛 hintMessageParameters

```java
[read-only] Object[] public hintMessageParameters
public Object[] getHintMessageParameters()
```
#### 📄🎛 message

```java
[read-only] String public message
public String getMessage()
```
#### 📄🎛 messageParameters

```java
[read-only] Object[] public messageParameters
public Object[] getMessageParameters()
```
#### 📄🎛 parser

```java
[read-only] NutsQuestionParser public parser
public NutsQuestionParser getParser()
```
#### 📄🎛 session

```java
[read-only] NutsSession public session
public NutsSession getSession()
```
#### 📄🎛 validator

```java
[read-only] NutsQuestionValidator public validator
public NutsQuestionValidator getValidator()
```
#### 📄🎛 value

```java
[read-only] Object public value
public Object getValue()
```
#### 📄🎛 valueType

```java
[read-only] Class public valueType
public Class getValueType()
```
### ⚙ Instance Methods
#### ⚙ acceptedValues(acceptedValues)


```java
NutsQuestion acceptedValues(Object[] acceptedValues)
```
**return**:NutsQuestion
- **Object[] acceptedValues** : 

#### ⚙ configure(skipUnsupported, args)
configure the current command with the given arguments. This is an
 override of the \{\@link NutsConfigurable#configure(boolean, java.lang.String...) \}
 to help return a more specific return type;

```java
NutsQuestion configure(boolean skipUnsupported, String[] args)
```
**return**:NutsQuestion
- **boolean skipUnsupported** : 
- **String[] args** : argument to configure with

#### ⚙ defaultValue(defautValue)


```java
NutsQuestion defaultValue(Object defautValue)
```
**return**:NutsQuestion
- **Object defautValue** : 

#### ⚙ forBoolean(msg, params)


```java
NutsQuestion forBoolean(String msg, Object[] params)
```
**return**:NutsQuestion
- **String msg** : 
- **Object[] params** : 

#### ⚙ forDouble(msg, params)


```java
NutsQuestion forDouble(String msg, Object[] params)
```
**return**:NutsQuestion
- **String msg** : 
- **Object[] params** : 

#### ⚙ forEnum(enumType, msg, params)


```java
NutsQuestion forEnum(Class enumType, String msg, Object[] params)
```
**return**:NutsQuestion
- **Class enumType** : 
- **String msg** : 
- **Object[] params** : 

#### ⚙ forFloat(msg, params)


```java
NutsQuestion forFloat(String msg, Object[] params)
```
**return**:NutsQuestion
- **String msg** : 
- **Object[] params** : 

#### ⚙ forInteger(msg, params)


```java
NutsQuestion forInteger(String msg, Object[] params)
```
**return**:NutsQuestion
- **String msg** : 
- **Object[] params** : 

#### ⚙ forLong(msg, params)


```java
NutsQuestion forLong(String msg, Object[] params)
```
**return**:NutsQuestion
- **String msg** : 
- **Object[] params** : 

#### ⚙ forPassword(msg, params)


```java
NutsQuestion forPassword(String msg, Object[] params)
```
**return**:NutsQuestion
- **String msg** : 
- **Object[] params** : 

#### ⚙ forString(msg, params)


```java
NutsQuestion forString(String msg, Object[] params)
```
**return**:NutsQuestion
- **String msg** : 
- **Object[] params** : 

#### ⚙ format(format)


```java
NutsQuestion format(NutsQuestionFormat format)
```
**return**:NutsQuestion
- **NutsQuestionFormat format** : 

#### ⚙ hintMessage(message, messageParameters)


```java
NutsQuestion hintMessage(String message, Object[] messageParameters)
```
**return**:NutsQuestion
- **String message** : 
- **Object[] messageParameters** : 

#### ⚙ message(message, messageParameters)


```java
NutsQuestion message(String message, Object[] messageParameters)
```
**return**:NutsQuestion
- **String message** : 
- **Object[] messageParameters** : 

#### ⚙ parser(parser)


```java
NutsQuestion parser(NutsQuestionParser parser)
```
**return**:NutsQuestion
- **NutsQuestionParser parser** : 

#### ⚙ run()


```java
NutsQuestion run()
```
**return**:NutsQuestion

#### ⚙ setAcceptedValues(acceptedValues)


```java
NutsQuestion setAcceptedValues(Object[] acceptedValues)
```
**return**:NutsQuestion
- **Object[] acceptedValues** : 

#### ⚙ setDefaultValue(defaultValue)


```java
NutsQuestion setDefaultValue(Object defaultValue)
```
**return**:NutsQuestion
- **Object defaultValue** : 

#### ⚙ setFormat(format)


```java
NutsQuestion setFormat(NutsQuestionFormat format)
```
**return**:NutsQuestion
- **NutsQuestionFormat format** : 

#### ⚙ setHintMessage(message, messageParameters)


```java
NutsQuestion setHintMessage(String message, Object[] messageParameters)
```
**return**:NutsQuestion
- **String message** : 
- **Object[] messageParameters** : 

#### ⚙ setMessage(message, messageParameters)


```java
NutsQuestion setMessage(String message, Object[] messageParameters)
```
**return**:NutsQuestion
- **String message** : 
- **Object[] messageParameters** : 

#### ⚙ setParser(parser)


```java
NutsQuestion setParser(NutsQuestionParser parser)
```
**return**:NutsQuestion
- **NutsQuestionParser parser** : 

#### ⚙ setSession(session)


```java
NutsQuestion setSession(NutsSession session)
```
**return**:NutsQuestion
- **NutsSession session** : 

#### ⚙ setValidator(validator)


```java
NutsQuestion setValidator(NutsQuestionValidator validator)
```
**return**:NutsQuestion
- **NutsQuestionValidator validator** : 

#### ⚙ setValueType(valueType)


```java
NutsQuestion setValueType(Class valueType)
```
**return**:NutsQuestion
- **Class valueType** : 

#### ⚙ validator(validator)


```java
NutsQuestion validator(NutsQuestionValidator validator)
```
**return**:NutsQuestion
- **NutsQuestionValidator validator** : 

#### ⚙ valueType(valueType)


```java
NutsQuestion valueType(Class valueType)
```
**return**:NutsQuestion
- **Class valueType** : 

## ☕ NutsQuestionParser
```java
public interface net.vpc.app.nuts.NutsQuestionParser
```

 \@author vpc
 \@since 0.5.4
 \@category Base

### ⚙ Instance Methods
#### ⚙ parse(response, defaultValue, question)


```java
Object parse(Object response, Object defaultValue, NutsQuestion question)
```
**return**:Object
- **Object response** : 
- **Object defaultValue** : 
- **NutsQuestion question** : 

## ☕ NutsQuestionValidator
```java
public interface net.vpc.app.nuts.NutsQuestionValidator
```

 \@author vpc
 \@since 0.5.4
 \@category Base

### ⚙ Instance Methods
#### ⚙ validate(value, question)


```java
Object validate(Object value, NutsQuestion question)
```
**return**:Object
- **Object value** : 
- **NutsQuestion question** : 

## ☕ NutsRepository
```java
public interface net.vpc.app.nuts.NutsRepository
```
 Nuts repository manages a set of packages

 \@since 0.5.4
 \@category Base

### 📢❄ Constant Fields
#### 📢❄ SPEED_FAST
```java
public static final int SPEED_FAST = 10000
```
#### 📢❄ SPEED_FASTER
```java
public static final int SPEED_FASTER = 100000
```
#### 📢❄ SPEED_FASTEST
```java
public static final int SPEED_FASTEST = 1000000
```
#### 📢❄ SPEED_SLOW
```java
public static final int SPEED_SLOW = 1000
```
#### 📢❄ SPEED_SLOWER
```java
public static final int SPEED_SLOWER = 100
```
#### 📢❄ SPEED_SLOWEST
```java
public static final int SPEED_SLOWEST = 10
```
### 🎛 Instance Properties
#### 📄🎛 name
return repository name.
 equivalent to config().name()
```java
[read-only] String public name
public String getName()
```
#### 📄🎛 parentRepository
return parent repository or null
```java
[read-only] NutsRepository public parentRepository
public NutsRepository getParentRepository()
```
#### 📄🎛 repositoryListeners
Repository Listeners
```java
[read-only] NutsRepositoryListener[] public repositoryListeners
public NutsRepositoryListener[] getRepositoryListeners()
```
#### 📄🎛 repositoryType
return repository type
```java
[read-only] String public repositoryType
public String getRepositoryType()
```
#### 📄🎛 userPropertyListeners
return array of registered user properties listeners
```java
[read-only] NutsMapListener[] public userPropertyListeners
public NutsMapListener[] getUserPropertyListeners()
```
#### 📄🎛 uuid
return repository unique identifier
```java
[read-only] String public uuid
public String getUuid()
```
#### 📄🎛 workspace
return parent workspace
```java
[read-only] NutsWorkspace public workspace
public NutsWorkspace getWorkspace()
```
### ⚙ Instance Methods
#### ⚙ addRepositoryListener(listener)
add repository listener

```java
void addRepositoryListener(NutsRepositoryListener listener)
```
- **NutsRepositoryListener listener** : listener

#### ⚙ addUserPropertyListener(listener)
add listener to user properties

```java
void addUserPropertyListener(NutsMapListener listener)
```
- **NutsMapListener listener** : listener

#### ⚙ config()
return repository configuration manager

```java
NutsRepositoryConfigManager config()
```
**return**:NutsRepositoryConfigManager

#### ⚙ deploy()
create deploy command

```java
NutsDeployRepositoryCommand deploy()
```
**return**:NutsDeployRepositoryCommand

#### ⚙ fetchContent()
create fetchContent command

```java
NutsFetchContentRepositoryCommand fetchContent()
```
**return**:NutsFetchContentRepositoryCommand

#### ⚙ fetchDescriptor()
create fetchDescriptor command

```java
NutsFetchDescriptorRepositoryCommand fetchDescriptor()
```
**return**:NutsFetchDescriptorRepositoryCommand

#### ⚙ name()
return repository name.
 equivalent to config().name()

```java
String name()
```
**return**:String

#### ⚙ parentRepository()
return parent repository or null

```java
NutsRepository parentRepository()
```
**return**:NutsRepository

#### ⚙ push()
create push command

```java
NutsPushRepositoryCommand push()
```
**return**:NutsPushRepositoryCommand

#### ⚙ removeRepositoryListener(listener)
remove repository listener

```java
void removeRepositoryListener(NutsRepositoryListener listener)
```
- **NutsRepositoryListener listener** : listener

#### ⚙ removeUserPropertyListener(listener)
remove listener from user properties

```java
void removeUserPropertyListener(NutsMapListener listener)
```
- **NutsMapListener listener** : listener

#### ⚙ repositoryType()
return repository type

```java
String repositoryType()
```
**return**:String

#### ⚙ search()
create search command

```java
NutsSearchRepositoryCommand search()
```
**return**:NutsSearchRepositoryCommand

#### ⚙ searchVersions()
create searchVersions command

```java
NutsSearchVersionsRepositoryCommand searchVersions()
```
**return**:NutsSearchVersionsRepositoryCommand

#### ⚙ security()
return repository security manager

```java
NutsRepositorySecurityManager security()
```
**return**:NutsRepositorySecurityManager

#### ⚙ undeploy()
create undeploy command

```java
NutsRepositoryUndeployCommand undeploy()
```
**return**:NutsRepositoryUndeployCommand

#### ⚙ updateStatistics()
create update statistics command

```java
NutsUpdateRepositoryStatisticsCommand updateStatistics()
```
**return**:NutsUpdateRepositoryStatisticsCommand

#### ⚙ userProperties()
return mutable instance of user properties

```java
Map userProperties()
```
**return**:Map

#### ⚙ uuid()
return repository unique identifier

```java
String uuid()
```
**return**:String

#### ⚙ workspace()
return parent workspace

```java
NutsWorkspace workspace()
```
**return**:NutsWorkspace

## ☕ NutsRepositoryFilter
```java
public interface net.vpc.app.nuts.NutsRepositoryFilter
```
 Created by vpc on 1/5/17.

 \@since 0.5.4
 \@category Base

### ⚙ Instance Methods
#### ⚙ accept(repository)


```java
boolean accept(NutsRepository repository)
```
**return**:boolean
- **NutsRepository repository** : 

## ☕ NutsResultList
```java
public interface net.vpc.app.nuts.NutsResultList
```
 Find Result items from find command

 \@see NutsSearchCommand#getResultIds()
 \@author vpc
 \@param \<T\> Result Type
 \@since 0.5.4
 \@category Base

### ⚙ Instance Methods
#### ⚙ count()
return elements count of this result.

 consumes the result and returns the number of elements consumed. Calling
 this method twice will result in unexpected behavior (may return 0 as the
 result is already consumed or throw an Exception)

```java
long count()
```
**return**:long

#### ⚙ first()
return the first value or null if none found.

 Calling this method twice will result in unexpected behavior (may return
 an incorrect value such as null as the result is already consumed or
 throw an Exception)

```java
Object first()
```
**return**:Object

#### ⚙ list()
return result as a  java.util.List .

 consumes the result and returns a list Calling this method twice will
 result in unexpected behavior (may return an empty list as the result is
 already consumed or throw an Exception)

```java
List list()
```
**return**:List

#### ⚙ required()
return the first value or NutsNotFoundException if not found.

 Calling this method twice will result in unexpected behavior (may return
 an incorrect value such as null as the result is already consumed or
 throw an Exception)

```java
Object required()
```
**return**:Object

#### ⚙ singleton()
return the first value while checking that there are no more elements.

 Calling this method twice will result in unexpected behavior (may return
 an incorrect value such as null as the result is already consumed or
 throw an Exception)

```java
Object singleton()
```
**return**:Object

#### ⚙ stream()
return result as a  java.util.stream.Stream .

 Calling this method twice will result in unexpected behavior (may return
 0 as the result is already consumed or throw an Exception)

```java
Stream stream()
```
**return**:Stream

## ☕ NutsSearchId
```java
public interface net.vpc.app.nuts.NutsSearchId
```
 Search id defines a uniform interface to ids, versions and descriptors
 \@author vpc
 \@since 0.5.4
 \@category Base

### ⚙ Instance Methods
#### ⚙ getDescriptor(session)
return descriptor

```java
NutsDescriptor getDescriptor(NutsSession session)
```
**return**:NutsDescriptor
- **NutsSession session** : session

#### ⚙ getId(session)
return id

```java
NutsId getId(NutsSession session)
```
**return**:NutsId
- **NutsSession session** : session

#### ⚙ getVersion(session)
return version

```java
NutsVersion getVersion(NutsSession session)
```
**return**:NutsVersion
- **NutsSession session** : session

## ☕ NutsSearchIdFilter
```java
public interface net.vpc.app.nuts.NutsSearchIdFilter
```
 SearchId Filter.
 \@author vpc
 \@since 0.5.4
 \@category Base

### ⚙ Instance Methods
#### ⚙ acceptSearchId(sid, session)
true if search id is accepted

```java
boolean acceptSearchId(NutsSearchId sid, NutsSession session)
```
**return**:boolean
- **NutsSearchId sid** : search id
- **NutsSession session** : session

## ☕ NutsSession
```java
public interface net.vpc.app.nuts.NutsSession
```
 session is context defining common command options and parameters.

 \@author vpc
 \@since 0.5.4
 \@category Base

### 🎛 Instance Properties
#### 📝🎛 ask
equivalent to \{\@code setConfirm(enable?ASK:null)\}
```java
[read-write] NutsSession public ask
public boolean isAsk()
public NutsSession setAsk(enable)
```
#### 📝🎛 cached
use cache
```java
[read-write] NutsSession public cached
public boolean isCached()
public NutsSession setCached(value)
```
#### 📝🎛 confirm
set confirm mode.
```java
[read-write] NutsSession public confirm
public NutsConfirmationMode getConfirm()
public NutsSession setConfirm(confirm)
```
#### 📝🎛 fetchStrategy
change fetch strategy
```java
[read-write] NutsSession public fetchStrategy
public NutsFetchStrategy getFetchStrategy()
public NutsSession setFetchStrategy(mode)
```
#### 📝🎛 force
change force flag value. some operations may require user confirmation
 before performing critical operations such as overriding existing values,
 deleting sensitive information ; in such cases, arming force flag will
 provide an implicit confirmation.
```java
[read-write] NutsSession public force
public boolean isForce()
public NutsSession setForce(enable)
```
#### 📝🎛 indexed
use index
```java
[read-write] NutsSession public indexed
public boolean isIndexed()
public NutsSession setIndexed(value)
```
#### 📝🎛 iterableFormat
set iterable output format
```java
[read-write] NutsSession public iterableFormat
public NutsIterableFormat getIterableFormat()
public NutsSession setIterableFormat(value)
```
#### 📄🎛 iterableOut
true if iterable format is armed. equivalent to
 \{\@code  getIterableFormat()!=null\}
```java
[read-only] boolean public iterableOut
public boolean isIterableOut()
```
#### 📄🎛 iterableOutput
return iterable output
```java
[read-only] NutsIterableOutput public iterableOutput
public NutsIterableOutput getIterableOutput()
```
#### 📄🎛 iterableTrace
true if iterable format and trace flag are armed. equivalent to \{\@code isTrace()
 && isIterableOut()\}
```java
[read-only] boolean public iterableTrace
public boolean isIterableTrace()
```
#### 📄🎛 listeners
return all registered listeners.
```java
[read-only] NutsListener[] public listeners
public NutsListener[] getListeners()
```
#### 📝🎛 no
change no flag value. some operations may require user confirmation
 before performing critical operations such as overriding existing values,
 deleting sensitive information ; in such cases, arming no flag will
 provide an implicit negative confirmation.
```java
[read-write] NutsSession public no
public boolean isNo()
public NutsSession setNo(enable)
```
#### 📝🎛 outputFormat
set output format
```java
[read-write] NutsSession public outputFormat
public NutsOutputFormat getOutputFormat()
public NutsSession setOutputFormat(outputFormat)
```
#### 📝🎛 outputFormatOptions
set output format options (clear and add)
```java
[read-write] NutsSession public outputFormatOptions
public String[] getOutputFormatOptions()
public NutsSession setOutputFormatOptions(options)
```
#### 📄🎛 plainOut
true if NON iterable and plain format are armed.
```java
[read-only] boolean public plainOut
public boolean isPlainOut()
```
#### 📄🎛 plainTrace
true if non iterable and plain formats along with trace flag are armed.
 equivalent to \{\@code isTrace()
 && !isIterableOut()
 && getOutputFormat() == NutsOutputFormat.PLAIN\}
```java
[read-only] boolean public plainTrace
public boolean isPlainTrace()
```
#### 📝🎛 progressOptions
change progress options
```java
[read-write] NutsSession public progressOptions
public String getProgressOptions()
public NutsSession setProgressOptions(progressOptions)
```
#### 📝🎛 properties
add session properties
```java
[read-write] NutsSession public properties
public Map getProperties()
public NutsSession setProperties(properties)
```
#### 📄🎛 structuredOut
true if NON iterable and NON plain formats are armed. equivalent to \{\@code !isIterableOut()
 && getOutputFormat() != NutsOutputFormat.PLAIN\}
```java
[read-only] boolean public structuredOut
public boolean isStructuredOut()
```
#### 📄🎛 structuredTrace
true if NON iterable and NON plain formats along with trace flag are
 armed. equivalent to \{\@code isTrace()
 && !isIterableOut()
 && getOutputFormat() == NutsOutputFormat.PLAIN\}
```java
[read-only] boolean public structuredTrace
public boolean isStructuredTrace()
```
#### 📝🎛 terminal
set session terminal
```java
[read-write] NutsSession public terminal
public NutsSessionTerminal getTerminal()
public NutsSession setTerminal(terminal)
```
#### 📝🎛 trace
change trace flag value. When true, operations are invited to print to
 output stream information about processing. Output may be in different
 formats according to \{\@link #getOutputFormat()\} and
 \{\@link #getIterableFormat()\}
```java
[read-write] NutsSession public trace
public boolean isTrace()
public NutsSession setTrace(trace)
```
#### 📝🎛 transitive
consider transitive repositories
```java
[read-write] NutsSession public transitive
public boolean isTransitive()
public NutsSession setTransitive(value)
```
#### 📄🎛 workspace
current workspace
```java
[read-only] NutsWorkspace public workspace
public NutsWorkspace getWorkspace()
```
#### 📝🎛 yes
change YES flag value. some operations may require user confirmation
 before performing critical operations such as overriding existing values,
 deleting sensitive information ; in such cases, arming yes flag will
 provide an implicit confirmation.
```java
[read-write] NutsSession public yes
public boolean isYes()
public NutsSession setYes(enable)
```
### ⚙ Instance Methods
#### ⚙ addListener(listener)
add session listener. supported listeners are instances of:
 \<ul\>
 \<li\>\{\@link NutsWorkspaceListener\}\</li\>
 \<li\>\{\@link NutsInstallListener\}\</li\>
 \<li\>\{\@link NutsMapListener\}\</li\>
 \<li\>\{\@link NutsRepositoryListener\}\</li\>
 \</ul\>

```java
NutsSession addListener(NutsListener listener)
```
**return**:NutsSession
- **NutsListener listener** : listener

#### ⚙ addOutputFormatOptions(options)
add output format options

```java
NutsSession addOutputFormatOptions(String[] options)
```
**return**:NutsSession
- **String[] options** : output format options.

#### ⚙ ask()
equivalent to \{\@code setAsk(true)\}

```java
NutsSession ask()
```
**return**:NutsSession

#### ⚙ confirm(confirm)
set confirm mode.

```java
NutsSession confirm(NutsConfirmationMode confirm)
```
**return**:NutsSession
- **NutsConfirmationMode confirm** : confirm type.

#### ⚙ copy()
return new instance copy of \{\@code this\} session

```java
NutsSession copy()
```
**return**:NutsSession

#### ⚙ copyFrom(other)
copy into this instance from the given value

```java
NutsSession copyFrom(NutsSession other)
```
**return**:NutsSession
- **NutsSession other** : other session to copy from

#### ⚙ err()
current error stream

```java
PrintStream err()
```
**return**:PrintStream

#### ⚙ fetchAnyWhere()
change fetch strategy to ANYWHERE

```java
NutsSession fetchAnyWhere()
```
**return**:NutsSession

#### ⚙ fetchOffline()
change fetch strategy to OFFLINE

```java
NutsSession fetchOffline()
```
**return**:NutsSession

#### ⚙ fetchOnline()
change fetch strategy to ONLINE

```java
NutsSession fetchOnline()
```
**return**:NutsSession

#### ⚙ fetchRemote()
change fetch strategy to REMOTE

```java
NutsSession fetchRemote()
```
**return**:NutsSession

#### ⚙ fetchStrategy(mode)
change fetch strategy

```java
NutsSession fetchStrategy(NutsFetchStrategy mode)
```
**return**:NutsSession
- **NutsFetchStrategy mode** : new strategy or null

#### ⚙ formatObject(any)
This is a helper method to create and Object format initialized with this
 session instance and the given object to print.
 \{\@code thisSession.getWorkspace().object().setSession(thisSession).value(any)\}
 \<p\>
 Using this method is recommended to print objects to default format (json, xml,...)

```java
NutsObjectFormat formatObject(Object any)
```
**return**:NutsObjectFormat
- **Object any** : any object to print in the configured/default format

#### ⚙ getListeners(type)
return registered listeners for the given type. Supported types are :
 \<ul\>
 \<li\>\{\@link NutsWorkspaceListener\}\</li\>
 \<li\>\{\@link NutsInstallListener\}\</li\>
 \<li\>\{\@link NutsMapListener\}\</li\>
 \<li\>\{\@link NutsRepositoryListener\}\</li\>
 \</ul\>

```java
NutsListener[] getListeners(Class type)
```
**return**:NutsListener[]
- **Class type** : listener type class

#### ⚙ getOutputFormat(defaultValue)
return current Output Format or \{\@code defaultValue\} if null

```java
NutsOutputFormat getOutputFormat(NutsOutputFormat defaultValue)
```
**return**:NutsOutputFormat
- **NutsOutputFormat defaultValue** : value when Output Format is not set

#### ⚙ getProperty(key)
return property value or null

```java
Object getProperty(String key)
```
**return**:Object
- **String key** : property key

#### ⚙ json()
set json output format

```java
NutsSession json()
```
**return**:NutsSession

#### ⚙ no()
equivalent to \{\@code setNo(true)\}

```java
NutsSession no()
```
**return**:NutsSession

#### ⚙ no(enable)
equivalent to \{\@code setNo(enable)\}

```java
NutsSession no(boolean enable)
```
**return**:NutsSession
- **boolean enable** : new value

#### ⚙ out()
current output stream

```java
PrintStream out()
```
**return**:PrintStream

#### ⚙ plain()
set plain text (default) output format

```java
NutsSession plain()
```
**return**:NutsSession

#### ⚙ props()
set properties output format

```java
NutsSession props()
```
**return**:NutsSession

#### ⚙ removeListener(listener)
remove session listener. supported listeners are instances of:
 \<ul\>
 \<li\>\{\@link NutsWorkspaceListener\}\</li\>
 \<li\>\{\@link NutsInstallListener\}\</li\>
 \<li\>\{\@link NutsMapListener\}\</li\>
 \<li\>\{\@link NutsRepositoryListener\}\</li\>
 \</ul\>

```java
NutsSession removeListener(NutsListener listener)
```
**return**:NutsSession
- **NutsListener listener** : listener

#### ⚙ setProperty(key, value)
set session property

```java
NutsSession setProperty(String key, Object value)
```
**return**:NutsSession
- **String key** : property key
- **Object value** : property value

#### ⚙ setSilent()
equivalent to \{\@code setTrace(false)\}

```java
NutsSession setSilent()
```
**return**:NutsSession

#### ⚙ table()
set table output format

```java
NutsSession table()
```
**return**:NutsSession

#### ⚙ terminal()
current terminal

```java
NutsSessionTerminal terminal()
```
**return**:NutsSessionTerminal

#### ⚙ tree()
set tree output format

```java
NutsSession tree()
```
**return**:NutsSession

#### ⚙ workspace()
current workspace

```java
NutsWorkspace workspace()
```
**return**:NutsWorkspace

#### ⚙ xml()
set xml output format

```java
NutsSession xml()
```
**return**:NutsSession

#### ⚙ yes()
equivalent to \{\@code setYes(true)\}

```java
NutsSession yes()
```
**return**:NutsSession

#### ⚙ yes(enable)
equivalent to \{\@code setYes(enable)\}

```java
NutsSession yes(boolean enable)
```
**return**:NutsSession
- **boolean enable** : new value

## ☕ NutsSessionTerminal
```java
public interface net.vpc.app.nuts.NutsSessionTerminal
```
 Created by vpc on 2/20/17.

 \@since 0.5.4
 \@category Base

### 🎛 Instance Properties
#### 📄🎛 parent

```java
[read-only] NutsTerminalBase public parent
public NutsTerminalBase getParent()
```
### ⚙ Instance Methods
#### ⚙ copy()


```java
NutsSessionTerminal copy()
```
**return**:NutsSessionTerminal

#### ⚙ setErr(out)


```java
void setErr(PrintStream out)
```
- **PrintStream out** : 

#### ⚙ setIn(in)


```java
void setIn(InputStream in)
```
- **InputStream in** : 

#### ⚙ setOut(out)


```java
void setOut(PrintStream out)
```
- **PrintStream out** : 

#### ⚙ setParent(parent)


```java
void setParent(NutsTerminalBase parent)
```
- **NutsTerminalBase parent** : 

## ☕ NutsStoreLocation
```java
public final net.vpc.app.nuts.NutsStoreLocation
```

 \@author vpc
 \@since 0.5.4
 \@category Base

### 📢❄ Constant Fields
#### 📢❄ APPS
```java
public static final NutsStoreLocation APPS
```
#### 📢❄ CACHE
```java
public static final NutsStoreLocation CACHE
```
#### 📢❄ CONFIG
```java
public static final NutsStoreLocation CONFIG
```
#### 📢❄ LIB
```java
public static final NutsStoreLocation LIB
```
#### 📢❄ LOG
```java
public static final NutsStoreLocation LOG
```
#### 📢❄ RUN
```java
public static final NutsStoreLocation RUN
```
#### 📢❄ TEMP
```java
public static final NutsStoreLocation TEMP
```
#### 📢❄ VAR
```java
public static final NutsStoreLocation VAR
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
NutsStoreLocation valueOf(String name)
```
**return**:NutsStoreLocation
- **String name** : 

#### 📢⚙ values()


```java
NutsStoreLocation[] values()
```
**return**:NutsStoreLocation[]

### ⚙ Instance Methods
#### ⚙ id()
lower cased identifier.

```java
String id()
```
**return**:String

## ☕ NutsStoreLocationStrategy
```java
public final net.vpc.app.nuts.NutsStoreLocationStrategy
```

 \@since 0.5.4
 \@category Base

### 📢❄ Constant Fields
#### 📢❄ EXPLODED
```java
public static final NutsStoreLocationStrategy EXPLODED
```
#### 📢❄ STANDALONE
```java
public static final NutsStoreLocationStrategy STANDALONE
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
NutsStoreLocationStrategy valueOf(String name)
```
**return**:NutsStoreLocationStrategy
- **String name** : 

#### 📢⚙ values()


```java
NutsStoreLocationStrategy[] values()
```
**return**:NutsStoreLocationStrategy[]

### ⚙ Instance Methods
#### ⚙ id()
lower cased identifier.

```java
String id()
```
**return**:String

## ☕ NutsTerminalMode
```java
public final net.vpc.app.nuts.NutsTerminalMode
```

 \@author vpc
 \@since 0.5.4
 \@category Base

### 📢❄ Constant Fields
#### 📢❄ FILTERED
```java
public static final NutsTerminalMode FILTERED
```
#### 📢❄ FORMATTED
```java
public static final NutsTerminalMode FORMATTED
```
#### 📢❄ INHERITED
```java
public static final NutsTerminalMode INHERITED
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
NutsTerminalMode valueOf(String name)
```
**return**:NutsTerminalMode
- **String name** : 

#### 📢⚙ values()


```java
NutsTerminalMode[] values()
```
**return**:NutsTerminalMode[]

### ⚙ Instance Methods
#### ⚙ id()
lower cased identifier.

```java
String id()
```
**return**:String

## ☕ NutsTokenFilter
```java
public interface net.vpc.app.nuts.NutsTokenFilter
```

 \@author vpc
 \@since 0.5.5
 \@category Base

### 🎛 Instance Properties
#### 📄🎛 blank

```java
[read-only] boolean public blank
public boolean isBlank()
```
#### 📄🎛 null

```java
[read-only] boolean public null
public boolean isNull()
```
### ⚙ Instance Methods
#### ⚙ contains(substring)


```java
boolean contains(String substring)
```
**return**:boolean
- **String substring** : 

#### ⚙ like(pattern)


```java
boolean like(String pattern)
```
**return**:boolean
- **String pattern** : 

#### ⚙ matches(pattern)


```java
boolean matches(String pattern)
```
**return**:boolean
- **String pattern** : 

## ☕ NutsVersionInterval
```java
public interface net.vpc.app.nuts.NutsVersionInterval
```
 Created by vpc on 2/1/17.

 \@since 0.5.4
 \@category Base

### 🎛 Instance Properties
#### 📄🎛 fixedValue

```java
[read-only] boolean public fixedValue
public boolean isFixedValue()
```
#### 📄🎛 includeLowerBound

```java
[read-only] boolean public includeLowerBound
public boolean isIncludeLowerBound()
```
#### 📄🎛 includeUpperBound

```java
[read-only] boolean public includeUpperBound
public boolean isIncludeUpperBound()
```
#### 📄🎛 lowerBound

```java
[read-only] String public lowerBound
public String getLowerBound()
```
#### 📄🎛 upperBound

```java
[read-only] String public upperBound
public String getUpperBound()
```
### ⚙ Instance Methods
#### ⚙ acceptVersion(version)


```java
boolean acceptVersion(NutsVersion version)
```
**return**:boolean
- **NutsVersion version** : 

## ☕ NutsWorkspace
```java
public interface net.vpc.app.nuts.NutsWorkspace
```
 Created by vpc on 1/5/17.

 \@since 0.5.4
 \@category Base

### 🎛 Instance Properties
#### 📄🎛 installListeners

```java
[read-only] NutsInstallListener[] public installListeners
public NutsInstallListener[] getInstallListeners()
```
#### 📄🎛 name
Workspace name
```java
[read-only] String public name
public String getName()
```
#### 📄🎛 repositoryListeners

```java
[read-only] NutsRepositoryListener[] public repositoryListeners
public NutsRepositoryListener[] getRepositoryListeners()
```
#### 📄🎛 userPropertyListeners

```java
[read-only] NutsMapListener[] public userPropertyListeners
public NutsMapListener[] getUserPropertyListeners()
```
#### 📄🎛 uuid
Workspace identifier, guaranteed to be unique cross machines
```java
[read-only] String public uuid
public String getUuid()
```
#### 📄🎛 workspaceListeners

```java
[read-only] NutsWorkspaceListener[] public workspaceListeners
public NutsWorkspaceListener[] getWorkspaceListeners()
```
### ⚙ Instance Methods
#### ⚙ addInstallListener(listener)


```java
void addInstallListener(NutsInstallListener listener)
```
- **NutsInstallListener listener** : 

#### ⚙ addRepositoryListener(listener)


```java
void addRepositoryListener(NutsRepositoryListener listener)
```
- **NutsRepositoryListener listener** : 

#### ⚙ addUserPropertyListener(listener)


```java
void addUserPropertyListener(NutsMapListener listener)
```
- **NutsMapListener listener** : 

#### ⚙ addWorkspaceListener(listener)


```java
void addWorkspaceListener(NutsWorkspaceListener listener)
```
- **NutsWorkspaceListener listener** : 

#### ⚙ commandLine()


```java
NutsCommandLineFormat commandLine()
```
**return**:NutsCommandLineFormat

#### ⚙ config()


```java
NutsWorkspaceConfigManager config()
```
**return**:NutsWorkspaceConfigManager

#### ⚙ createSession()


```java
NutsSession createSession()
```
**return**:NutsSession

#### ⚙ dependency()
create dependency format instance

```java
NutsDependencyFormat dependency()
```
**return**:NutsDependencyFormat

#### ⚙ deploy()


```java
NutsDeployCommand deploy()
```
**return**:NutsDeployCommand

#### ⚙ descriptor()
create descriptor format instance

```java
NutsDescriptorFormat descriptor()
```
**return**:NutsDescriptorFormat

#### ⚙ element()
create element format instance

```java
NutsElementFormat element()
```
**return**:NutsElementFormat

#### ⚙ exec()


```java
NutsExecCommand exec()
```
**return**:NutsExecCommand

#### ⚙ extensions()


```java
NutsWorkspaceExtensionManager extensions()
```
**return**:NutsWorkspaceExtensionManager

#### ⚙ fetch()


```java
NutsFetchCommand fetch()
```
**return**:NutsFetchCommand

#### ⚙ id()
create id format instance

```java
NutsIdFormat id()
```
**return**:NutsIdFormat

#### ⚙ info()
create info format instance

```java
NutsInfoFormat info()
```
**return**:NutsInfoFormat

#### ⚙ install()


```java
NutsInstallCommand install()
```
**return**:NutsInstallCommand

#### ⚙ io()


```java
NutsIOManager io()
```
**return**:NutsIOManager

#### ⚙ iter()
create iterable format instance

```java
NutsIterableOutput iter()
```
**return**:NutsIterableOutput

#### ⚙ json()
create json format instance

```java
NutsJsonFormat json()
```
**return**:NutsJsonFormat

#### ⚙ log()


```java
NutsLogManager log()
```
**return**:NutsLogManager

#### ⚙ name()
equivalent to \{\@link #getName()\}

```java
String name()
```
**return**:String

#### ⚙ object()
create object format instance

```java
NutsObjectFormat object()
```
**return**:NutsObjectFormat

#### ⚙ props()
create properties format instance

```java
NutsPropertiesFormat props()
```
**return**:NutsPropertiesFormat

#### ⚙ push()


```java
NutsPushCommand push()
```
**return**:NutsPushCommand

#### ⚙ removeInstallListener(listener)


```java
void removeInstallListener(NutsInstallListener listener)
```
- **NutsInstallListener listener** : 

#### ⚙ removeRepositoryListener(listener)


```java
void removeRepositoryListener(NutsRepositoryListener listener)
```
- **NutsRepositoryListener listener** : 

#### ⚙ removeUserPropertyListener(listener)


```java
void removeUserPropertyListener(NutsMapListener listener)
```
- **NutsMapListener listener** : 

#### ⚙ removeWorkspaceListener(listener)


```java
void removeWorkspaceListener(NutsWorkspaceListener listener)
```
- **NutsWorkspaceListener listener** : 

#### ⚙ search()


```java
NutsSearchCommand search()
```
**return**:NutsSearchCommand

#### ⚙ security()


```java
NutsWorkspaceSecurityManager security()
```
**return**:NutsWorkspaceSecurityManager

#### ⚙ str()
create string format instance

```java
NutsStringFormat str()
```
**return**:NutsStringFormat

#### ⚙ table()
create table format instance

```java
NutsTableFormat table()
```
**return**:NutsTableFormat

#### ⚙ tree()
create tree format instance

```java
NutsTreeFormat tree()
```
**return**:NutsTreeFormat

#### ⚙ undeploy()


```java
NutsUndeployCommand undeploy()
```
**return**:NutsUndeployCommand

#### ⚙ uninstall()


```java
NutsUninstallCommand uninstall()
```
**return**:NutsUninstallCommand

#### ⚙ update()


```java
NutsUpdateCommand update()
```
**return**:NutsUpdateCommand

#### ⚙ updateStatistics()


```java
NutsUpdateStatisticsCommand updateStatistics()
```
**return**:NutsUpdateStatisticsCommand

#### ⚙ userProperties()


```java
Map userProperties()
```
**return**:Map

#### ⚙ uuid()
equivalent to \{\@link #getUuid()\}

```java
String uuid()
```
**return**:String

#### ⚙ version()
create version format instance

```java
NutsVersionFormat version()
```
**return**:NutsVersionFormat

#### ⚙ xml()
create xml format instance

```java
NutsXmlFormat xml()
```
**return**:NutsXmlFormat


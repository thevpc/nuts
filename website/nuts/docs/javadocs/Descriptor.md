---
id: javadoc_Descriptor
title: Descriptor
sidebar_label: Descriptor
---
                                                
```
     __        __           ___    ____  ____
  /\ \ \ _  __/ /______    /   |  / __ \/  _/
 /  \/ / / / / __/ ___/   / /| | / /_/ // /   
/ /\  / /_/ / /_(__  )   / ___ |/ ____// /       
\_\ \/\__,_/\__/____/   /_/  |_/_/   /___/  version 0.7.0
```

## ☕ NutsClassifierMapping
```java
public interface net.vpc.app.nuts.NutsClassifierMapping
```
 classifier selector immutable class.
 Nuts can select artifact classifier according to filters based on arch, os, os dist and platform.
 This class defines the mapping to classifier to consider if all the filters.
 When multiple selectors match, the first on prevails.
 \@since 0.5.7
 \@category Descriptor

### 🎛 Instance Properties
#### 📄🎛 arch
arch list filter.
 al least one of the list must match.
```java
[read-only] String[] public arch
public String[] getArch()
```
#### 📄🎛 classifier
classifier to select
```java
[read-only] String public classifier
public String getClassifier()
```
#### 📄🎛 os
os list filter.
 al least one of the list must match.
```java
[read-only] String[] public os
public String[] getOs()
```
#### 📄🎛 osdist
os distribution list filter.
 al least one of the list must match.
```java
[read-only] String[] public osdist
public String[] getOsdist()
```
#### 📄🎛 packaging
packaging to select
```java
[read-only] String public packaging
public String getPackaging()
```
#### 📄🎛 platform
platform list filter.
 al least one of the list must match.
```java
[read-only] String[] public platform
public String[] getPlatform()
```
## ☕ NutsClassifierMappingBuilder
```java
public interface net.vpc.app.nuts.NutsClassifierMappingBuilder
```
 classifier selector builder class.
 Nuts can select artifact classifier according to filters based on arch, os, os dist and platform.
 This class defines the mapping to classifier to consider if all the filters.
 When multiple selectors match, the first on prevails.

 \@since 0.5.7
 \@category Descriptor

### 🎛 Instance Properties
#### 📝🎛 arch
set archs
```java
[read-write] NutsClassifierMappingBuilder public arch
public String[] getArch()
public NutsClassifierMappingBuilder setArch(value)
```
#### 📝🎛 classifier
set classifier
```java
[read-write] NutsClassifierMappingBuilder public classifier
public String getClassifier()
public NutsClassifierMappingBuilder setClassifier(value)
```
#### 📝🎛 os
set oses
```java
[read-write] NutsClassifierMappingBuilder public os
public String[] getOs()
public NutsClassifierMappingBuilder setOs(value)
```
#### 📝🎛 osdist
set os dists
```java
[read-write] NutsClassifierMappingBuilder public osdist
public String[] getOsdist()
public NutsClassifierMappingBuilder setOsdist(value)
```
#### 📝🎛 packaging
set packaging
```java
[read-write] NutsClassifierMappingBuilder public packaging
public String getPackaging()
public NutsClassifierMappingBuilder setPackaging(value)
```
#### 📝🎛 platform
set platforms
```java
[read-write] NutsClassifierMappingBuilder public platform
public String[] getPlatform()
public NutsClassifierMappingBuilder setPlatform(value)
```
### ⚙ Instance Methods
#### ⚙ build()
create new instance of \{\@link NutsClassifierMapping\} initialized with this builder\'s values.

```java
NutsClassifierMapping build()
```
**return**:NutsClassifierMapping

#### ⚙ clear()
clear all values / reset builder

```java
NutsClassifierMappingBuilder clear()
```
**return**:NutsClassifierMappingBuilder

#### ⚙ set(value)
copy all values from the given builder

```java
NutsClassifierMappingBuilder set(NutsClassifierMappingBuilder value)
```
**return**:NutsClassifierMappingBuilder
- **NutsClassifierMappingBuilder value** : builder to copy from

#### ⚙ set(value)
copy all values from the given instance

```java
NutsClassifierMappingBuilder set(NutsClassifierMapping value)
```
**return**:NutsClassifierMappingBuilder
- **NutsClassifierMapping value** : instance to copy from

## ☕ NutsContent
```java
public interface net.vpc.app.nuts.NutsContent
```
 Content describes a artifact file location and its characteristics.
 \@author vpc
 \@since 0.5.4
 \@category Descriptor

### 🎛 Instance Properties
#### 📄🎛 cached
when true, the content was retrieved from cache rather then from remote location.
```java
[read-only] boolean public cached
public boolean isCached()
```
#### 📄🎛 path
artifact local path
```java
[read-only] Path public path
public Path getPath()
```
#### 📄🎛 temporary
when true, the path location is temporary and should be deleted after usage
```java
[read-only] boolean public temporary
public boolean isTemporary()
```
## ☕ NutsDefaultContent
```java
public net.vpc.app.nuts.NutsDefaultContent
```
 Default Content implementation.
 \@author vpc
 \@since 0.5.4
 \@category Descriptor

### 🪄 Constructors
#### 🪄 NutsDefaultContent(file, cached, temporary)
Default Content implementation constructor

```java
NutsDefaultContent(Path file, boolean cached, boolean temporary)
```
- **Path file** : content file path
- **boolean cached** : true if the file is cached (may be not up to date)
- **boolean temporary** : true if file is temporary (should be deleted later)

### 🎛 Instance Properties
#### 📄🎛 cached
true if the file is cached (may be not up to date)
```java
[read-only] boolean public cached
public boolean isCached()
```
#### 📄🎛 path
content path location
```java
[read-only] Path public path
public Path getPath()
```
#### 📄🎛 temporary
true if file is temporary (should be deleted later)
```java
[read-only] boolean public temporary
public boolean isTemporary()
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

## ☕ NutsDependency
```java
public interface net.vpc.app.nuts.NutsDependency
```
 NutsDependency is an \<strong\>immutable\</strong\> object that contains all information about a component\'s dependency.
 \@author vpc
 \@since 0.5.3
 \@category Descriptor

### 🎛 Instance Properties
#### 📄🎛 artifactId
return artifact id (aka artifactId)
```java
[read-only] String public artifactId
public String getArtifactId()
```
#### 📄🎛 classifier
get classifier string value (may be $ var)
```java
[read-only] String public classifier
public String getClassifier()
```
#### 📄🎛 exclusions
dependency exclusions
```java
[read-only] NutsId[] public exclusions
public NutsId[] getExclusions()
```
#### 📄🎛 fullName
return dependency full name in the form
 namespace://group:name#version?scope=&lt;scope&gt;\{\@code &\}optional=&lt;optional&gt;
```java
[read-only] String public fullName
public String getFullName()
```
#### 📄🎛 groupId
return artifact group id (aka groupId in maven)
```java
[read-only] String public groupId
public String getGroupId()
```
#### 📄🎛 id
convert to NutsId
```java
[read-only] NutsId public id
public NutsId getId()
```
#### 📄🎛 longName
return dependency full name in the form
 group:name#version
```java
[read-only] String public longName
public String getLongName()
```
#### 📄🎛 namespace
return namespace
```java
[read-only] String public namespace
public String getNamespace()
```
#### 📄🎛 optional
Indicates the dependency is optional for use of this library.
```java
[read-only] String public optional
public String getOptional()
```
#### 📄🎛 properties
properties in the url query form
```java
[read-only] Map public properties
public Map getProperties()
```
#### 📄🎛 propertiesQuery
properties in the url query form
```java
[read-only] String public propertiesQuery
public String getPropertiesQuery()
```
#### 📄🎛 scope
get scope string value (may be $ var).
```java
[read-only] String public scope
public String getScope()
```
#### 📄🎛 simpleName
return dependency full name in the form
 group:name
```java
[read-only] String public simpleName
public String getSimpleName()
```
#### 📄🎛 version
return dependency version
```java
[read-only] NutsVersion public version
public NutsVersion getVersion()
```
### ⚙ Instance Methods
#### ⚙ builder()
return mutable id builder instance initialized with \{\@code this\} instance.

```java
NutsDependencyBuilder builder()
```
**return**:NutsDependencyBuilder

## ☕ NutsDependencyBuilder
```java
public interface net.vpc.app.nuts.NutsDependencyBuilder
```
 Dependency Builder (mutable).
 User should use available \'set\' method and finally call \{\@link #build()\}
 to get an instance of immutable NutsDependency

 \@author vpc
 \@since 0.5.4
 \@category Descriptor

### 🎛 Instance Properties
#### 📝🎛 artifactId
set name value
```java
[read-write] NutsDependencyBuilder public artifactId
public String getArtifactId()
public NutsDependencyBuilder setArtifactId(artifactId)
```
#### 📝🎛 classifier
set classifier value
```java
[read-write] NutsDependencyBuilder public classifier
public String getClassifier()
public NutsDependencyBuilder setClassifier(classifier)
```
#### ✏🎛 dependency
reset this instance with value
```java
[write-only] NutsDependencyBuilder public dependency
public NutsDependencyBuilder setDependency(value)
```
#### 📝🎛 exclusions
set exclusions value
```java
[read-write] NutsDependencyBuilder public exclusions
public NutsId[] getExclusions()
public NutsDependencyBuilder setExclusions(exclusions)
```
#### 📄🎛 fullName
return full name
```java
[read-only] String public fullName
public String getFullName()
```
#### 📝🎛 groupId
set group value
```java
[read-write] NutsDependencyBuilder public groupId
public String getGroupId()
public NutsDependencyBuilder setGroupId(groupId)
```
#### 📝🎛 id
set id value
```java
[read-write] NutsDependencyBuilder public id
public NutsId getId()
public NutsDependencyBuilder setId(id)
```
#### 📝🎛 namespace
set namespace value
```java
[read-write] NutsDependencyBuilder public namespace
public String getNamespace()
public NutsDependencyBuilder setNamespace(namespace)
```
#### 📝🎛 optional
set optional value
```java
[read-write] NutsDependencyBuilder public optional
public String getOptional()
public NutsDependencyBuilder setOptional(optional)
```
#### 📄🎛 properties

```java
[read-only] Map public properties
public Map getProperties()
```
#### 📄🎛 propertiesQuery

```java
[read-only] String public propertiesQuery
public String getPropertiesQuery()
```
#### 📝🎛 scope
set scope value
```java
[read-write] NutsDependencyBuilder public scope
public String getScope()
public NutsDependencyBuilder setScope(scope)
```
#### 📝🎛 version
set version value
```java
[read-write] NutsDependencyBuilder public version
public NutsVersion getVersion()
public NutsDependencyBuilder setVersion(version)
```
### ⚙ Instance Methods
#### ⚙ addProperties(propertiesQuery)


```java
NutsDependencyBuilder addProperties(String propertiesQuery)
```
**return**:NutsDependencyBuilder
- **String propertiesQuery** : 

#### ⚙ addProperties(queryMap)


```java
NutsDependencyBuilder addProperties(Map queryMap)
```
**return**:NutsDependencyBuilder
- **Map queryMap** : 

#### ⚙ build()
build new instance of NutsDependencies

```java
NutsDependency build()
```
**return**:NutsDependency

#### ⚙ clear()
reset this instance

```java
NutsDependencyBuilder clear()
```
**return**:NutsDependencyBuilder

#### ⚙ set(value)
reset this instance with value

```java
NutsDependencyBuilder set(NutsDependencyBuilder value)
```
**return**:NutsDependencyBuilder
- **NutsDependencyBuilder value** : new value

#### ⚙ set(value)
reset this instance with value

```java
NutsDependencyBuilder set(NutsDependency value)
```
**return**:NutsDependencyBuilder
- **NutsDependency value** : new value

#### ⚙ setProperties(propertiesQuery)


```java
NutsDependencyBuilder setProperties(String propertiesQuery)
```
**return**:NutsDependencyBuilder
- **String propertiesQuery** : 

#### ⚙ setProperties(queryMap)


```java
NutsDependencyBuilder setProperties(Map queryMap)
```
**return**:NutsDependencyBuilder
- **Map queryMap** : 

#### ⚙ setProperty(property, value)


```java
NutsDependencyBuilder setProperty(String property, String value)
```
**return**:NutsDependencyBuilder
- **String property** : 
- **String value** : 

## ☕ NutsDependencyFilter
```java
public interface net.vpc.app.nuts.NutsDependencyFilter
```
 Dependency filter

 \@since 0.5.4
 \@category Descriptor

### ⚙ Instance Methods
#### ⚙ accept(from, dependency, session)
return true if the \{\@code dependency\} is accepted

```java
boolean accept(NutsId from, NutsDependency dependency, NutsSession session)
```
**return**:boolean
- **NutsId from** : parent (dependent) id
- **NutsDependency dependency** : dependency id
- **NutsSession session** : session

## ☕ NutsDependencyScope
```java
public final net.vpc.app.nuts.NutsDependencyScope
```
 Supported dependency scope lists
 \@author vpc
 \@since 0.5.4
 \@category Descriptor

### 📢❄ Constant Fields
#### 📢❄ API
```java
public static final NutsDependencyScope API
```
#### 📢❄ IMPLEMENTATION
```java
public static final NutsDependencyScope IMPLEMENTATION
```
#### 📢❄ IMPORT
```java
public static final NutsDependencyScope IMPORT
```
#### 📢❄ OTHER
```java
public static final NutsDependencyScope OTHER
```
#### 📢❄ PROVIDED
```java
public static final NutsDependencyScope PROVIDED
```
#### 📢❄ RUNTIME
```java
public static final NutsDependencyScope RUNTIME
```
#### 📢❄ SYSTEM
```java
public static final NutsDependencyScope SYSTEM
```
#### 📢❄ TEST_COMPILE
```java
public static final NutsDependencyScope TEST_COMPILE
```
#### 📢❄ TEST_PROVIDED
```java
public static final NutsDependencyScope TEST_PROVIDED
```
#### 📢❄ TEST_RUNTIME
```java
public static final NutsDependencyScope TEST_RUNTIME
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
NutsDependencyScope valueOf(String name)
```
**return**:NutsDependencyScope
- **String name** : 

#### 📢⚙ values()


```java
NutsDependencyScope[] values()
```
**return**:NutsDependencyScope[]

### ⚙ Instance Methods
#### ⚙ id()
lower cased identifier.

```java
String id()
```
**return**:String

## ☕ NutsDependencyScopePattern
```java
public final net.vpc.app.nuts.NutsDependencyScopePattern
```
 Supported dependency scope pattern.
 A dependency scope pattern
 \@author vpc
 \@since 0.5.6
 \@category Descriptor

### 📢❄ Constant Fields
#### 📢❄ ALL
```java
public static final NutsDependencyScopePattern ALL
```
#### 📢❄ API
```java
public static final NutsDependencyScopePattern API
```
#### 📢❄ COMPILE
```java
public static final NutsDependencyScopePattern COMPILE
```
#### 📢❄ IMPLEMENTATION
```java
public static final NutsDependencyScopePattern IMPLEMENTATION
```
#### 📢❄ IMPORT
```java
public static final NutsDependencyScopePattern IMPORT
```
#### 📢❄ OTHER
```java
public static final NutsDependencyScopePattern OTHER
```
#### 📢❄ PROVIDED
```java
public static final NutsDependencyScopePattern PROVIDED
```
#### 📢❄ RUN
```java
public static final NutsDependencyScopePattern RUN
```
#### 📢❄ RUNTIME
```java
public static final NutsDependencyScopePattern RUNTIME
```
#### 📢❄ RUN_TEST
```java
public static final NutsDependencyScopePattern RUN_TEST
```
#### 📢❄ SYSTEM
```java
public static final NutsDependencyScopePattern SYSTEM
```
#### 📢❄ TEST
```java
public static final NutsDependencyScopePattern TEST
```
#### 📢❄ TEST_COMPILE
```java
public static final NutsDependencyScopePattern TEST_COMPILE
```
#### 📢❄ TEST_PROVIDED
```java
public static final NutsDependencyScopePattern TEST_PROVIDED
```
#### 📢❄ TEST_RUNTIME
```java
public static final NutsDependencyScopePattern TEST_RUNTIME
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
NutsDependencyScopePattern valueOf(String name)
```
**return**:NutsDependencyScopePattern
- **String name** : 

#### 📢⚙ values()


```java
NutsDependencyScopePattern[] values()
```
**return**:NutsDependencyScopePattern[]

### ⚙ Instance Methods
#### ⚙ id()
lower cased identifier.

```java
String id()
```
**return**:String

## ☕ NutsDependencyTreeNode
```java
public interface net.vpc.app.nuts.NutsDependencyTreeNode
```
 Dependency tree node
 \@author vpc
 \@since 0.5.5
 \@category Descriptor

### 🎛 Instance Properties
#### 📄🎛 children
node children
```java
[read-only] NutsDependencyTreeNode[] public children
public NutsDependencyTreeNode[] getChildren()
```
#### 📄🎛 dependency
node dependency
```java
[read-only] NutsDependency public dependency
public NutsDependency getDependency()
```
#### 📄🎛 partial
true if the node is partial filled (not all children are considered)
```java
[read-only] boolean public partial
public boolean isPartial()
```
## ☕ NutsDescriptor
```java
public interface net.vpc.app.nuts.NutsDescriptor
```
 Nuts descriptors define an \<strong\>immutable\</strong\> image to all information needed to execute an artifact.
 It resembles to maven\'s pom file but it focuses on execution information
 rather then build information. Common features are inheritance
 dependencies, standard dependencies, exclusions and properties.
 However nuts descriptor adds new features such as :
 \<ul\>
     \<li\>multiple parent inheritance\</li\>
     \<li\>executable/nuts-executable flag\</li\>
     \<li\>environment (arch, os, dist,platform) filters\</li\>
     \<li\>classifiers may be mapped to environment (think of dlls for windows and so for linux)\</li\>
 \</ul\>
 A versatile way to change descriptor is to use builder (\{\@link #builder()\}).

 \@since 0.1.0
 \@category Descriptor

### 🎛 Instance Properties
#### 📄🎛 application
true if the artifact is a java executable that implements \{\@link NutsApplication\} interface.
```java
[read-only] boolean public application
public boolean isApplication()
```
#### 📄🎛 arch
supported archs. if empty, all arch are supported (for example for java, all arch are supported).
```java
[read-only] String[] public arch
public String[] getArch()
```
#### 📄🎛 classifierMappings
ordered list of classifier mapping used to resolve valid classifier to use of ra given environment.
```java
[read-only] NutsClassifierMapping[] public classifierMappings
public NutsClassifierMapping[] getClassifierMappings()
```
#### 📄🎛 dependencies
list of immediate (non inherited and non transitive dependencies
```java
[read-only] NutsDependency[] public dependencies
public NutsDependency[] getDependencies()
```
#### 📄🎛 description
long description for the artifact
```java
[read-only] String public description
public String getDescription()
```
#### 📄🎛 executable
true if the artifact is executable and is considered an application. if not it is a library.
```java
[read-only] boolean public executable
public boolean isExecutable()
```
#### 📄🎛 executor
descriptor of artifact responsible of running this artifact
```java
[read-only] NutsArtifactCall public executor
public NutsArtifactCall getExecutor()
```
#### 📄🎛 id
artifact full id (groupId+artifactId+version)
```java
[read-only] NutsId public id
public NutsId getId()
```
#### 📄🎛 installer
descriptor of artifact responsible of installing this artifact
```java
[read-only] NutsArtifactCall public installer
public NutsArtifactCall getInstaller()
```
#### 📄🎛 locations
list of available mirror locations from which nuts can download artifact content.
 location can be mapped to a classifier.
```java
[read-only] NutsIdLocation[] public locations
public NutsIdLocation[] getLocations()
```
#### 📄🎛 name
user friendly name, a short description for the artifact
```java
[read-only] String public name
public String getName()
```
#### 📄🎛 os
supported operating systems. if empty, all oses are supported (for example for java, all arch are supported).
```java
[read-only] String[] public os
public String[] getOs()
```
#### 📄🎛 osdist
supported operating system distributions (mostly for linux systems). if empty, all distributions are supported.
```java
[read-only] String[] public osdist
public String[] getOsdist()
```
#### 📄🎛 packaging
return descriptor packaging (used to resolve file extension)
```java
[read-only] String public packaging
public String getPackaging()
```
#### 📄🎛 parents
descriptor parent list (may be empty)
```java
[read-only] NutsId[] public parents
public NutsId[] getParents()
```
#### 📄🎛 platform
supported platforms (java, dotnet, ...). if empty patform is not relevant.
 This is helpful to bind application to a jdk version for instance (in that case platform may be in the form java#8 for instance)
```java
[read-only] String[] public platform
public String[] getPlatform()
```
#### 📄🎛 properties
custom properties that can be used as place holders (int $\{name\} form) in other fields.
```java
[read-only] Map public properties
public Map getProperties()
```
#### 📄🎛 standardDependencies
The dependencies specified here are not used until they are referenced in
 a POM within the group. This allows the specification of a
 &quot;standard&quot; version for a particular. This corresponds to
 "dependencyManagement.dependencies" in maven
```java
[read-only] NutsDependency[] public standardDependencies
public NutsDependency[] getStandardDependencies()
```
### ⚙ Instance Methods
#### ⚙ builder()
create new builder filled with this descriptor fields.

```java
NutsDescriptorBuilder builder()
```
**return**:NutsDescriptorBuilder

## ☕ NutsDescriptorBuilder
```java
public interface net.vpc.app.nuts.NutsDescriptorBuilder
```
 Nuts descriptors define a \<strong\>mutable\</strong\> image to all information needed to execute an artifact.
 It help creating an instance of \{\@link NutsDescriptor\} by calling \{\@link #build()\}

 \@since 0.5.4
 \@category Descriptor

### 🎛 Instance Properties
#### ✏🎛 application
set nutsApp flag
```java
[write-only] NutsDescriptorBuilder public application
public NutsDescriptorBuilder setApplication(nutsApp)
```
#### 📝🎛 arch
set archs
```java
[read-write] NutsDescriptorBuilder public arch
public String[] getArch()
public NutsDescriptorBuilder setArch(archs)
```
#### 📝🎛 classifierMappings
set classifier mappings
```java
[read-write] NutsDescriptorBuilder public classifierMappings
public NutsClassifierMapping[] getClassifierMappings()
public NutsDescriptorBuilder setClassifierMappings(value)
```
#### 📝🎛 dependencies
set dependencies
```java
[read-write] NutsDescriptorBuilder public dependencies
public NutsDependency[] getDependencies()
public NutsDescriptorBuilder setDependencies(dependencies)
```
#### 📝🎛 description
set description
```java
[read-write] NutsDescriptorBuilder public description
public String getDescription()
public NutsDescriptorBuilder setDescription(description)
```
#### 📝🎛 executable
set executable flag
```java
[read-write] NutsDescriptorBuilder public executable
public boolean isExecutable()
public NutsDescriptorBuilder setExecutable(executable)
```
#### 📝🎛 executor
set executor flag
```java
[read-write] NutsDescriptorBuilder public executor
public NutsArtifactCall getExecutor()
public NutsDescriptorBuilder setExecutor(executor)
```
#### 📝🎛 id
set id
```java
[read-write] NutsDescriptorBuilder public id
public NutsId getId()
public NutsDescriptorBuilder setId(id)
```
#### 📝🎛 installer
set installer
```java
[read-write] NutsDescriptorBuilder public installer
public NutsArtifactCall getInstaller()
public NutsDescriptorBuilder setInstaller(installer)
```
#### 📝🎛 locations
set locations
```java
[read-write] NutsDescriptorBuilder public locations
public NutsIdLocation[] getLocations()
public NutsDescriptorBuilder setLocations(locations)
```
#### 📝🎛 name
set name
```java
[read-write] NutsDescriptorBuilder public name
public String getName()
public NutsDescriptorBuilder setName(name)
```
#### 📄🎛 nutsApplication
true if the artifact is a java executable that implements \{\@link NutsApplication\} interface.
```java
[read-only] boolean public nutsApplication
public boolean isNutsApplication()
```
#### 📝🎛 os
set os
```java
[read-write] NutsDescriptorBuilder public os
public String[] getOs()
public NutsDescriptorBuilder setOs(os)
```
#### 📝🎛 osdist
set osdist
```java
[read-write] NutsDescriptorBuilder public osdist
public String[] getOsdist()
public NutsDescriptorBuilder setOsdist(osdist)
```
#### 📝🎛 packaging
set packaging
```java
[read-write] NutsDescriptorBuilder public packaging
public String getPackaging()
public NutsDescriptorBuilder setPackaging(packaging)
```
#### 📝🎛 parents
set parents
```java
[read-write] NutsDescriptorBuilder public parents
public NutsId[] getParents()
public NutsDescriptorBuilder setParents(parents)
```
#### 📝🎛 platform
set platform
```java
[read-write] NutsDescriptorBuilder public platform
public String[] getPlatform()
public NutsDescriptorBuilder setPlatform(platform)
```
#### 📝🎛 properties
set properties
```java
[read-write] NutsDescriptorBuilder public properties
public Map getProperties()
public NutsDescriptorBuilder setProperties(properties)
```
#### 📝🎛 standardDependencies
set standard dependencies
```java
[read-write] NutsDescriptorBuilder public standardDependencies
public NutsDependency[] getStandardDependencies()
public NutsDescriptorBuilder setStandardDependencies(dependencies)
```
### ⚙ Instance Methods
#### ⚙ addArch(arch)
add arch

```java
NutsDescriptorBuilder addArch(String arch)
```
**return**:NutsDescriptorBuilder
- **String arch** : new value to add

#### ⚙ addClassifierMapping(mapping)
add classifier mapping

```java
NutsDescriptorBuilder addClassifierMapping(NutsClassifierMapping mapping)
```
**return**:NutsDescriptorBuilder
- **NutsClassifierMapping mapping** : classifier mapping

#### ⚙ addDependencies(dependencies)
add dependencies

```java
NutsDescriptorBuilder addDependencies(NutsDependency[] dependencies)
```
**return**:NutsDescriptorBuilder
- **NutsDependency[] dependencies** : new value to add

#### ⚙ addDependency(dependency)
add dependency

```java
NutsDescriptorBuilder addDependency(NutsDependency dependency)
```
**return**:NutsDescriptorBuilder
- **NutsDependency dependency** : new value to add

#### ⚙ addLocation(location)
add location

```java
NutsDescriptorBuilder addLocation(NutsIdLocation location)
```
**return**:NutsDescriptorBuilder
- **NutsIdLocation location** : location to add

#### ⚙ addOs(os)
add os

```java
NutsDescriptorBuilder addOs(String os)
```
**return**:NutsDescriptorBuilder
- **String os** : new value to add

#### ⚙ addOsdist(osdist)
add os dist

```java
NutsDescriptorBuilder addOsdist(String osdist)
```
**return**:NutsDescriptorBuilder
- **String osdist** : new value to add

#### ⚙ addPlatform(platform)
add platform

```java
NutsDescriptorBuilder addPlatform(String platform)
```
**return**:NutsDescriptorBuilder
- **String platform** : new value to add

#### ⚙ addProperties(properties)
merge properties

```java
NutsDescriptorBuilder addProperties(Map properties)
```
**return**:NutsDescriptorBuilder
- **Map properties** : new value

#### ⚙ addStandardDependencies(dependencies)
add standard dependencies

```java
NutsDescriptorBuilder addStandardDependencies(NutsDependency[] dependencies)
```
**return**:NutsDescriptorBuilder
- **NutsDependency[] dependencies** : value to add

#### ⚙ addStandardDependency(dependency)
add standard dependency

```java
NutsDescriptorBuilder addStandardDependency(NutsDependency dependency)
```
**return**:NutsDescriptorBuilder
- **NutsDependency dependency** : value to add

#### ⚙ application()


```java
NutsDescriptorBuilder application()
```
**return**:NutsDescriptorBuilder

#### ⚙ application(nutsApp)


```java
NutsDescriptorBuilder application(boolean nutsApp)
```
**return**:NutsDescriptorBuilder
- **boolean nutsApp** : 

#### ⚙ applyParents(parentDescriptors)
merge parent and child information (apply inheritance)

```java
NutsDescriptorBuilder applyParents(NutsDescriptor[] parentDescriptors)
```
**return**:NutsDescriptorBuilder
- **NutsDescriptor[] parentDescriptors** : parent descriptors

#### ⚙ applyProperties()
replace placeholders with the corresponding property value in properties list

```java
NutsDescriptorBuilder applyProperties()
```
**return**:NutsDescriptorBuilder

#### ⚙ applyProperties(properties)
replace placeholders with the corresponding property value in the given properties list and return a new instance.

```java
NutsDescriptorBuilder applyProperties(Map properties)
```
**return**:NutsDescriptorBuilder
- **Map properties** : properties

#### ⚙ arch(archs)
set archs

```java
NutsDescriptorBuilder arch(String[] archs)
```
**return**:NutsDescriptorBuilder
- **String[] archs** : value to set

#### ⚙ build()
create new Descriptor filled with this builder fields.

```java
NutsDescriptor build()
```
**return**:NutsDescriptor

#### ⚙ classifierMappings(value)


```java
NutsDescriptorBuilder classifierMappings(NutsClassifierMapping[] value)
```
**return**:NutsDescriptorBuilder
- **NutsClassifierMapping[] value** : 

#### ⚙ clear()
clear this instance (set null/default all properties)

```java
NutsDescriptorBuilder clear()
```
**return**:NutsDescriptorBuilder

#### ⚙ dependencies(dependencies)
set dependencies

```java
NutsDescriptorBuilder dependencies(NutsDependency[] dependencies)
```
**return**:NutsDescriptorBuilder
- **NutsDependency[] dependencies** : new value

#### ⚙ description(description)


```java
NutsDescriptorBuilder description(String description)
```
**return**:NutsDescriptorBuilder
- **String description** : 

#### ⚙ descriptor(other)


```java
NutsDescriptorBuilder descriptor(NutsDescriptor other)
```
**return**:NutsDescriptorBuilder
- **NutsDescriptor other** : 

#### ⚙ descriptor(other)


```java
NutsDescriptorBuilder descriptor(NutsDescriptorBuilder other)
```
**return**:NutsDescriptorBuilder
- **NutsDescriptorBuilder other** : 

#### ⚙ executable()


```java
NutsDescriptorBuilder executable()
```
**return**:NutsDescriptorBuilder

#### ⚙ executable(executable)


```java
NutsDescriptorBuilder executable(boolean executable)
```
**return**:NutsDescriptorBuilder
- **boolean executable** : 

#### ⚙ executor(executor)


```java
NutsDescriptorBuilder executor(NutsArtifactCall executor)
```
**return**:NutsDescriptorBuilder
- **NutsArtifactCall executor** : 

#### ⚙ id(id)


```java
NutsDescriptorBuilder id(NutsId id)
```
**return**:NutsDescriptorBuilder
- **NutsId id** : 

#### ⚙ installer(installer)


```java
NutsDescriptorBuilder installer(NutsArtifactCall installer)
```
**return**:NutsDescriptorBuilder
- **NutsArtifactCall installer** : 

#### ⚙ locations(locations)


```java
NutsDescriptorBuilder locations(NutsIdLocation[] locations)
```
**return**:NutsDescriptorBuilder
- **NutsIdLocation[] locations** : 

#### ⚙ name(name)
set name

```java
NutsDescriptorBuilder name(String name)
```
**return**:NutsDescriptorBuilder
- **String name** : value to set

#### ⚙ os(os)
set os

```java
NutsDescriptorBuilder os(String[] os)
```
**return**:NutsDescriptorBuilder
- **String[] os** : value to set

#### ⚙ osdist(osdist)
set osdist

```java
NutsDescriptorBuilder osdist(String[] osdist)
```
**return**:NutsDescriptorBuilder
- **String[] osdist** : value to set

#### ⚙ packaging(packaging)
set packaging

```java
NutsDescriptorBuilder packaging(String packaging)
```
**return**:NutsDescriptorBuilder
- **String packaging** : new value

#### ⚙ parents(parents)
set parents

```java
NutsDescriptorBuilder parents(NutsId[] parents)
```
**return**:NutsDescriptorBuilder
- **NutsId[] parents** : value to set

#### ⚙ platform(platform)
set platform

```java
NutsDescriptorBuilder platform(String[] platform)
```
**return**:NutsDescriptorBuilder
- **String[] platform** : value to set

#### ⚙ properties(properties)
set properties

```java
NutsDescriptorBuilder properties(Map properties)
```
**return**:NutsDescriptorBuilder
- **Map properties** : new value

#### ⚙ property(name, value)


```java
NutsDescriptorBuilder property(String name, String value)
```
**return**:NutsDescriptorBuilder
- **String name** : 
- **String value** : 

#### ⚙ removeArch(arch)
remove arch

```java
NutsDescriptorBuilder removeArch(String arch)
```
**return**:NutsDescriptorBuilder
- **String arch** : value to remove

#### ⚙ removeDependency(dependency)
remove dependency

```java
NutsDescriptorBuilder removeDependency(NutsDependency dependency)
```
**return**:NutsDescriptorBuilder
- **NutsDependency dependency** : value to remove

#### ⚙ removeDependency(dependency)
create a new instance of descriptor with removed dependencies that match the predicate

```java
NutsDescriptorBuilder removeDependency(Predicate dependency)
```
**return**:NutsDescriptorBuilder
- **Predicate dependency** : predicate to test against

#### ⚙ removeOs(os)
remove os

```java
NutsDescriptorBuilder removeOs(String os)
```
**return**:NutsDescriptorBuilder
- **String os** : value to remove

#### ⚙ removeOsdist(osdist)
remove osdist

```java
NutsDescriptorBuilder removeOsdist(String osdist)
```
**return**:NutsDescriptorBuilder
- **String osdist** : value to remove

#### ⚙ removePlatform(platform)
remove platform

```java
NutsDescriptorBuilder removePlatform(String platform)
```
**return**:NutsDescriptorBuilder
- **String platform** : value to remove

#### ⚙ removeStandardDependency(dependency)
remove standard dependency

```java
NutsDescriptorBuilder removeStandardDependency(NutsDependency dependency)
```
**return**:NutsDescriptorBuilder
- **NutsDependency dependency** : value to remove

#### ⚙ replaceDependency(filter, converter)
create a new instance of descriptor with added/merged dependencies

```java
NutsDescriptorBuilder replaceDependency(Predicate filter, UnaryOperator converter)
```
**return**:NutsDescriptorBuilder
- **Predicate filter** : properties entry that match the update
- **UnaryOperator converter** : function to provide new value to replace with

#### ⚙ replaceProperty(filter, converter)
create a new instance of descriptor with added/merged properties

```java
NutsDescriptorBuilder replaceProperty(Predicate filter, Function converter)
```
**return**:NutsDescriptorBuilder
- **Predicate filter** : properties entry that match the update
- **Function converter** : function to provide new value to replace with

#### ⚙ set(other)
set all fields from \{\@code other\}

```java
NutsDescriptorBuilder set(NutsDescriptorBuilder other)
```
**return**:NutsDescriptorBuilder
- **NutsDescriptorBuilder other** : builder to copy from

#### ⚙ set(other)
set all fields from \{\@code other\}

```java
NutsDescriptorBuilder set(NutsDescriptor other)
```
**return**:NutsDescriptorBuilder
- **NutsDescriptor other** : descriptor to copy from

#### ⚙ setProperty(name, value)
set or unset property.
 if the value is null, the property is removed.

```java
NutsDescriptorBuilder setProperty(String name, String value)
```
**return**:NutsDescriptorBuilder
- **String name** : property name
- **String value** : new value

#### ⚙ standardDependencies(dependencies)
set standard dependencies

```java
NutsDescriptorBuilder standardDependencies(NutsDependency[] dependencies)
```
**return**:NutsDescriptorBuilder
- **NutsDependency[] dependencies** : value to set

## ☕ NutsDescriptorFilter
```java
public interface net.vpc.app.nuts.NutsDescriptorFilter
```
 Descriptor filter

 \@since 0.5.4
 \@category Descriptor

### ⚙ Instance Methods
#### ⚙ accept(descriptor, session)
return true if descriptor is accepted

```java
boolean accept(NutsDescriptor descriptor, NutsSession session)
```
**return**:boolean
- **NutsDescriptor descriptor** : descriptor
- **NutsSession session** : session

#### ⚙ acceptSearchId(sid, session)
default implementation of \{\@link NutsSearchIdFilter\}

```java
boolean acceptSearchId(NutsSearchId sid, NutsSession session)
```
**return**:boolean
- **NutsSearchId sid** : search id
- **NutsSession session** : session

## ☕ NutsExecutableInformation
```java
public interface net.vpc.app.nuts.NutsExecutableInformation
```
 Class describing executable command.
 \@author vpc
 \@since 0.5.4
 \@category Descriptor

### 🎛 Instance Properties
#### 📄🎛 description
executable description
```java
[read-only] String public description
public String getDescription()
```
#### 📄🎛 helpText
executable help string
```java
[read-only] String public helpText
public String getHelpText()
```
#### 📄🎛 id
executable artifact id
```java
[read-only] NutsId public id
public NutsId getId()
```
#### 📄🎛 name
executable name
```java
[read-only] String public name
public String getName()
```
#### 📄🎛 type
return executable type
```java
[read-only] NutsExecutableType public type
public NutsExecutableType getType()
```
#### 📄🎛 value
versatile executable name
```java
[read-only] String public value
public String getValue()
```
## ☕ NutsExecutableType
```java
public final net.vpc.app.nuts.NutsExecutableType
```
 Executable command type returned by which internal command
 \@author vpc
 \@since 0.5.4
 \@category Descriptor

### 📢❄ Constant Fields
#### 📢❄ ALIAS
```java
public static final NutsExecutableType ALIAS
```
#### 📢❄ ARTIFACT
```java
public static final NutsExecutableType ARTIFACT
```
#### 📢❄ INTERNAL
```java
public static final NutsExecutableType INTERNAL
```
#### 📢❄ SYSTEM
```java
public static final NutsExecutableType SYSTEM
```
### 📢⚙ Static Methods
#### 📢⚙ valueOf(name)


```java
NutsExecutableType valueOf(String name)
```
**return**:NutsExecutableType
- **String name** : 

#### 📢⚙ values()


```java
NutsExecutableType[] values()
```
**return**:NutsExecutableType[]

### ⚙ Instance Methods
#### ⚙ id()
lower cased identifier.

```java
String id()
```
**return**:String

## ☕ NutsExecutionEntry
```java
public interface net.vpc.app.nuts.NutsExecutionEntry
```
 Execution entry is a class that can be executed.
 \@author vpc
 \@since 0.5.4
 \@category Descriptor

### 🎛 Instance Properties
#### 📄🎛 app
true if the entry resolved to a valid nuts application
```java
[read-only] boolean public app
public boolean isApp()
```
#### 📄🎛 defaultEntry
true if the class if registered as main class in META-INF
```java
[read-only] boolean public defaultEntry
public boolean isDefaultEntry()
```
#### 📄🎛 name
class name
```java
[read-only] String public name
public String getName()
```
## ☕ NutsId
```java
public interface net.vpc.app.nuts.NutsId
```
 Immutable Artifact id information.
 \@author vpc
 \@since 0.1.0
 \@category Descriptor

### 🎛 Instance Properties
#### 📄🎛 arch
hardware architecture supported by the artifact
```java
[read-only] String public arch
public String getArch()
```
#### 📄🎛 artifactId
return name part of this id
```java
[read-only] String public artifactId
public String getArtifactId()
```
#### 📄🎛 classifier
tag used to distinguish between different artifacts that were built from the same source code
```java
[read-only] String public classifier
public String getClassifier()
```
#### 📄🎛 face
id face define is a release file type selector of the id.
 It helps discriminating content (jar) from descriptor, from other (hash,...)
 files released for the very same  artifact.
```java
[read-only] String public face
public String getFace()
```
#### 📄🎛 fullName
return a string representation of this id. All of group, name, version,
 namespace, queryMap values are printed. This method is equivalent to
 \{\@link Object#toString()\}
```java
[read-only] String public fullName
public String getFullName()
```
#### 📄🎛 groupId
artifact group which identifies uniquely projects and group of projects.
```java
[read-only] String public groupId
public String getGroupId()
```
#### 📄🎛 longName
return a string concatenation of group, name and version,
 ignoring namespace, and queryMap values. An example of long name is
 \<code\>my-group:my-artifact#my-version?alt\</code\>
```java
[read-only] String public longName
public String getLongName()
```
#### 📄🎛 longNameId
return a new instance of NutsId defining only group, name and version,
 ignoring namespace, and queryMap values.
```java
[read-only] NutsId public longNameId
public NutsId getLongNameId()
```
#### 📄🎛 namespace
artifact namespace (usually repository name or id)
```java
[read-only] String public namespace
public String getNamespace()
```
#### 📄🎛 os
os supported by the artifact
```java
[read-only] String public os
public String getOs()
```
#### 📄🎛 osdist
os distribution supported by the artifact
```java
[read-only] String public osdist
public String getOsdist()
```
#### 📄🎛 platform
platform supported by the artifact
```java
[read-only] String public platform
public String getPlatform()
```
#### 📄🎛 properties
properties as map.
```java
[read-only] Map public properties
public Map getProperties()
```
#### 📄🎛 propertiesQuery
properties in the url query form
```java
[read-only] String public propertiesQuery
public String getPropertiesQuery()
```
#### 📄🎛 shortName
returns a string concatenation of group and name (\':\' separated) ignoring
 version,namespace, and queryMap values. In group is empty or null, name
 is returned. Ann null values are trimmed to "" An example of simple name
 is \<code\>my-group:my-artifact\</code\>
```java
[read-only] String public shortName
public String getShortName()
```
#### 📄🎛 shortNameId
return a new instance of NutsId defining only group and name ignoring
 version,namespace, and queryMap values.
```java
[read-only] NutsId public shortNameId
public NutsId getShortNameId()
```
#### 📄🎛 version
artifact version (never null)
```java
[read-only] NutsVersion public version
public NutsVersion getVersion()
```
### ⚙ Instance Methods
#### ⚙ anyToken()
non null token filter that searches in all id fields

```java
NutsTokenFilter anyToken()
```
**return**:NutsTokenFilter

#### ⚙ artifactIdToken()
non null artifact id token

```java
NutsTokenFilter artifactIdToken()
```
**return**:NutsTokenFilter

#### ⚙ builder()
create a builder (mutable id) based on this id

```java
NutsIdBuilder builder()
```
**return**:NutsIdBuilder

#### ⚙ equalsShortName(other)
true if other has exact shot name than \{\@code this\}

```java
boolean equalsShortName(NutsId other)
```
**return**:boolean
- **NutsId other** : other id

#### ⚙ filter()
create a filter based on this id

```java
NutsIdFilter filter()
```
**return**:NutsIdFilter

#### ⚙ groupIdToken()
non null group id token

```java
NutsTokenFilter groupIdToken()
```
**return**:NutsTokenFilter

#### ⚙ namespaceToken()
non null namespace non null namespace token

```java
NutsTokenFilter namespaceToken()
```
**return**:NutsTokenFilter

#### ⚙ propertiesToken()
non null properties query token

```java
NutsTokenFilter propertiesToken()
```
**return**:NutsTokenFilter

#### ⚙ versionToken()
non null version token

```java
NutsTokenFilter versionToken()
```
**return**:NutsTokenFilter

## ☕ NutsIdBuilder
```java
public interface net.vpc.app.nuts.NutsIdBuilder
```
 Mutable Artifact id information used to create instance of \{\@link NutsId\}
 \@author vpc
 \@since 0.5.4
 \@category Descriptor

### 🎛 Instance Properties
#### 📝🎛 arch
update arch
```java
[read-write] NutsIdBuilder public arch
public String getArch()
public NutsIdBuilder setArch(value)
```
#### 📝🎛 artifactId
update artifactId
```java
[read-write] NutsIdBuilder public artifactId
public String getArtifactId()
public NutsIdBuilder setArtifactId(value)
```
#### 📝🎛 classifier
update classifier
```java
[read-write] NutsIdBuilder public classifier
public String getClassifier()
public NutsIdBuilder setClassifier(value)
```
#### 📝🎛 face
update id face which defines is a release file type selector
```java
[read-write] NutsIdBuilder public face
public String getFace()
public NutsIdBuilder setFace(value)
```
#### 📄🎛 fullName
return a string representation of this id. All of group, name, version,
 namespace, queryMap values are printed. This method is equivalent to
 \{\@link Object#toString()\}
```java
[read-only] String public fullName
public String getFullName()
```
#### 📝🎛 groupId
update groupId
```java
[read-write] NutsIdBuilder public groupId
public String getGroupId()
public NutsIdBuilder setGroupId(value)
```
#### 📄🎛 longName
return a string concatenation of group, name and version,
 ignoring namespace, and queryMap values. An example of long name is
 \<code\>my-group:my-artifact#my-version?alt\</code\>
```java
[read-only] String public longName
public String getLongName()
```
#### 📝🎛 namespace
update namespace
```java
[read-write] NutsIdBuilder public namespace
public String getNamespace()
public NutsIdBuilder setNamespace(value)
```
#### 📝🎛 os
update os
```java
[read-write] NutsIdBuilder public os
public String getOs()
public NutsIdBuilder setOs(value)
```
#### 📝🎛 osdist
update osdist
```java
[read-write] NutsIdBuilder public osdist
public String getOsdist()
public NutsIdBuilder setOsdist(value)
```
#### ✏🎛 packaging
update packaging
```java
[write-only] NutsIdBuilder public packaging
public NutsIdBuilder setPackaging(packaging)
```
#### 📝🎛 platform
update platform
```java
[read-write] NutsIdBuilder public platform
public String getPlatform()
public NutsIdBuilder setPlatform(value)
```
#### 📝🎛 properties
update all properties property.
```java
[read-write] NutsIdBuilder public properties
public Map getProperties()
public NutsIdBuilder setProperties(query)
```
#### 📄🎛 propertiesQuery
properties in the url query form
```java
[read-only] String public propertiesQuery
public String getPropertiesQuery()
```
#### 📄🎛 shortName
returns a string concatenation of group and name (\':\' separated) ignoring
 version,namespace, and queryMap values. In group is empty or null, name
 is returned. Ann null values are trimmed to "" An example of simple name
 is \<code\>my-group:my-artifact\</code\>
```java
[read-only] String public shortName
public String getShortName()
```
#### 📝🎛 version
update setVersion
```java
[read-write] NutsIdBuilder public version
public NutsVersion getVersion()
public NutsIdBuilder setVersion(value)
```
### ⚙ Instance Methods
#### ⚙ addProperties(query)
update all properties property while retaining old,
 non overridden properties.

```java
NutsIdBuilder addProperties(String query)
```
**return**:NutsIdBuilder
- **String query** : new value

#### ⚙ addProperties(queryMap)
update all properties property while retaining old,
 non overridden properties.

```java
NutsIdBuilder addProperties(Map queryMap)
```
**return**:NutsIdBuilder
- **Map queryMap** : new value

#### ⚙ apply(properties)
replace dollar based variables with the given properties

```java
NutsIdBuilder apply(Function properties)
```
**return**:NutsIdBuilder
- **Function properties** : to replace

#### ⚙ build()
create new instance of \{\@link NutsId\} initialized with this builder values.

```java
NutsId build()
```
**return**:NutsId

#### ⚙ clear()
clear this instance (set null/default all properties)

```java
NutsIdBuilder clear()
```
**return**:NutsIdBuilder

#### ⚙ set(id)
update all arguments

```java
NutsIdBuilder set(NutsId id)
```
**return**:NutsIdBuilder
- **NutsId id** : new value

#### ⚙ set(id)
update all arguments

```java
NutsIdBuilder set(NutsIdBuilder id)
```
**return**:NutsIdBuilder
- **NutsIdBuilder id** : new value

#### ⚙ setFaceContent()
equivalent to \{\@code setFace(NutsConstants.QueryFaces.CONTENT)\}

```java
NutsIdBuilder setFaceContent()
```
**return**:NutsIdBuilder

#### ⚙ setFaceDescriptor()
equivalent to \{\@code setFace(NutsConstants.QueryFaces.DESCRIPTOR)\}

```java
NutsIdBuilder setFaceDescriptor()
```
**return**:NutsIdBuilder

#### ⚙ setProperty(property, value)
update property.
 When \{\@code value\} is null, property will be removed.

```java
NutsIdBuilder setProperty(String property, String value)
```
**return**:NutsIdBuilder
- **String property** : name
- **String value** : new value

## ☕ NutsIdFilter
```java
public interface net.vpc.app.nuts.NutsIdFilter
```
 Class for filtering Artifact Ids

 \@since 0.5.4
 \@category Descriptor

### ⚙ Instance Methods
#### ⚙ accept(id, session)
return true when the id is to be accepted

```java
boolean accept(NutsId id, NutsSession session)
```
**return**:boolean
- **NutsId id** : id to check
- **NutsSession session** : current workspace session

#### ⚙ acceptSearchId(sid, session)


```java
boolean acceptSearchId(NutsSearchId sid, NutsSession session)
```
**return**:boolean
- **NutsSearchId sid** : 
- **NutsSession session** : 

## ☕ NutsIdLocation
```java
public interface net.vpc.app.nuts.NutsIdLocation
```
 This class is used in \{\@link NutsDescriptor\} to describe
 locations/mirrors to download artifact content instead of the
 regular location.
 \@category Descriptor

### 🎛 Instance Properties
#### 📄🎛 classifier
classifier for the artifact
```java
[read-only] String public classifier
public String getClassifier()
```
#### 📄🎛 region
location (geographic) region that may be used to select
 the most effective mirror
```java
[read-only] String public region
public String getRegion()
```
#### 📄🎛 url
location url of the artifact content
```java
[read-only] String public url
public String getUrl()
```
## ☕ NutsVersion
```java
public interface net.vpc.app.nuts.NutsVersion
```
 this class represents an \<strong\>immutable\</strong\> string representation of a version parsed as a suite of alternating numbers and words.
 Parsing algorithm is simply to split whenever word type changes.
 Examples:
 \<ul\>
     \<li\>1 = [1]\</li\>
     \<li\>1.2 = [1,\'.\',2]\</li\>
     \<li\>10.20update3 = [10,\'.\',20,\'update\',3]\</li\>
 \</ul\>
 \@author vpc
 \@since 0.5.4
 \@category Descriptor

### 🎛 Instance Properties
#### 📄🎛 singleValue
return true if this version denotes as single value and does not match an interval.
```java
[read-only] boolean public singleValue
public boolean isSingleValue()
```
#### 📄🎛 value
return string representation of the version
```java
[read-only] String public value
public String getValue()
```
### ⚙ Instance Methods
#### ⚙ compareTo(other)
compare this version to the other version

```java
int compareTo(String other)
```
**return**:int
- **String other** : other version

#### ⚙ compareTo(other)


```java
int compareTo(NutsVersion other)
```
**return**:int
- **NutsVersion other** : 

#### ⚙ filter()
parse the current version as new instance of \{\@link NutsVersionFilter\}

```java
NutsVersionFilter filter()
```
**return**:NutsVersionFilter

#### ⚙ get(index)
element at given index. if the index is negative will return from right.
 \<ul\>
     \<li\>(1.a22).get(0)=1\</li\>
     \<li\>(1.a22).get(1)=a\</li\>
     \<li\>(1.a22).get(-1)=22\</li\>
 \</ul\>

```java
String get(int index)
```
**return**:String
- **int index** : version part index

#### ⚙ getNumber(index)
number element at given index. if the index is negative will return from right (-1 is the first starting from the right).
 The version is first split (as a suite of number and words) then all words are discarded.
 \<ul\>
     \<li\>size(1.22)=3 \{\'1\',\'.\',\'22\'\}\</li\>
     \<li\>size(1.22_u1)=5 \{\'1\',\'.\',\'22\',\'_u\',\'1\'\}\</li\>
 \</ul\>
 \<ul\>
     \<li\>(1.a22).getNumber(0)=1\</li\>
     \<li\>(1.a22).getNumber(1)=22\</li\>
     \<li\>(1.a22).getNumber(-1)=22\</li\>
 \</ul\>

```java
int getNumber(int index)
```
**return**:int
- **int index** : version part index

#### ⚙ getNumber(index, defaultValue)
return number element at position or default value. if the index is negative will return from right (-1 is the first starting from the right).
 The version is first split (as a suite of number and words) then all words are discarded.

```java
int getNumber(int index, int defaultValue)
```
**return**:int
- **int index** : position
- **int defaultValue** : default value

#### ⚙ inc()
increment the last number in the version with 1

```java
NutsVersion inc()
```
**return**:NutsVersion

#### ⚙ inc(position)
increment the number at \{\@code position\}  in the version with 1

```java
NutsVersion inc(int position)
```
**return**:NutsVersion
- **int position** : number position

#### ⚙ inc(position, amount)
increment the last number in the version with the given \{\@code amount\}

```java
NutsVersion inc(int position, int amount)
```
**return**:NutsVersion
- **int position** : number position
- **int amount** : amount of the increment

#### ⚙ intervals()
parse the current version as an interval array

```java
NutsVersionInterval[] intervals()
```
**return**:NutsVersionInterval[]

#### ⚙ numberSize()
number of elements in the version.
 \<ul\>
     \<li\>numberSize(1.22)=2 \{1,22\}\</li\>
     \<li\>numberSize(1.22_u1)=3 \{1,22,1\}\</li\>
 \</ul\>

```java
int numberSize()
```
**return**:int

#### ⚙ size()
number of elements in the version.
 \<ul\>
     \<li\>size(1.22)=3 \{\'1\',\'.\',\'22\'\}\</li\>
     \<li\>size(1.22_u1)=5 \{\'1\',\'.\',\'22\',\'_u\',\'1\'\}\</li\>
 \</ul\>

```java
int size()
```
**return**:int

## ☕ NutsVersionFilter
```java
public interface net.vpc.app.nuts.NutsVersionFilter
```
 version interval is a version filter that accepts interval ranges of versions.
 
 version intervals can be in one of the following forms
 \<pre\>
 [ version, ]
 ] version, ] or ( version, ]
 [ version, [ or [ version, )
 ] version, [ or ] version, [

 [ ,version ]
 ] ,version ] or ( ,version ]
 [ ,version [ or [ ,version )
 ] ,version [ or ] ,version [

 [ version1 , version2 ]
 ] version1 , version2 ] or ( version1 , version2 ]
 [ version1 , version2 [ or [ version1 , version2 )
 ] version1 , version2 [ or ] version1 , version2 [

 comma or space separated intervals such as :
   [ version1 , version2 ], [ version1 , version2 ]
   [ version1 , version2 ]  [ version1 , version2 ]
 \</pre\>

 Created by vpc on 1/8/17.
 \@since 0.5.4
 \@category Descriptor

### ⚙ Instance Methods
#### ⚙ accept(version, session)
true if the version is accepted by this instance filter

```java
boolean accept(NutsVersion version, NutsSession session)
```
**return**:boolean
- **NutsVersion version** : version to check
- **NutsSession session** : current session instance

#### ⚙ acceptSearchId(sid, session)
true if the version is accepted by this instance filter

```java
boolean acceptSearchId(NutsSearchId sid, NutsSession session)
```
**return**:boolean
- **NutsSearchId sid** : search id
- **NutsSession session** : current session instance


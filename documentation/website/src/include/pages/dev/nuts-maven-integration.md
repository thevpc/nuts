---
id: nutsDescriptorIntegration 
title: Nuts Descriptor Integration 
sidebar_label: Nuts Descriptor Integration
---


## Nuts Descriptor Integration

* Seamless integration
* Maven Solver

### Nuts and Maven

* `nuts.executable=<true|false>` : when true the artifact is an executable (contains main class)
* `nuts.application=<true|false>` : when true the artifact is an executable application (implements NutsApplication)
* `nuts.gui=<true|false>` : when true the requires a gui environment to execute
* `nuts.term=<true|false>` : when true the artifact is a command line executable
* `nuts.icons=<icon-path-string-array>` : an array (separated with ',' or new lines) of icon paths (url in the NPath
  format)
* `nuts.genericName=<genericNameString>` : a generic name for the application like 'Text Editor'
    * `nuts.categories=<categories-string-array>` : an array (separated with ',' or new lines) of categories. the
      categories should be compatible with Free Desktop Menu
      specification (https://specifications.freedesktop.org/menu-spec/menu-spec-1.0.html)
* `nuts.<os>-os-dependencies` : list (':',';' or line separated) of short ids of dependencies that shall be appended to
  classpath only if running on the given os (see NutsOsFamily). This is a ways more simple than using the builtin '
  profile' concept of Maven (which is of course supported as well)
* `nuts.<arch>-arch-dependencies` : list (':',';' or line separated) of short ids of dependencies that shall be appended
  to classpath only if running on the given hardware architecture (see NutsArchFamily). This is a ways more simple than
  using the builtin 'profile' concept of Maven (which is of course supported as well)
* `nuts.<os>-os-<arch>-arch-dependencies` : list (':',';' or line separated) of short ids of dependencies that shall be
  appended to classpath only if running on the given hardware architecture and os family

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/POM/4.0.0"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>your-group</groupId>
    <artifactId>your-project</artifactId>
    <version>1.2.3</version>
    <packaging>jar</packaging>
    <properties>
        <!--properties having special meanings in Nuts-->
        <maven.compiler.target>1.8</maven.compiler.target>

        <!--properties specific to nuts for developers extending nuts-->
        <nuts.runtime>true</nuts.runtime> <!--if you implement a whole new runtime-->
        <nuts.extension>true</nuts.extension> <!--if you implement an extension-->

        <!--other properties specific to nuts-->
        <nuts.genericName>A Generic Name</nuts.genericName>
        <nuts.executable>true</nuts.executable>
        <nuts.application>true</nuts.application>
        <nuts.gui>true</nuts.gui>
        <nuts.term>true</nuts.term>

        <nuts.categories>
            /Settings/YourCategory
        </nuts.categories>
        <nuts.icons>
            classpath://net/yourpackage/yourapp/icon.svg
            classpath://net/yourpackage/yourapp/icon.png
            classpath://net/yourpackage/yourapp/icon.ico
        </nuts.icons>
        <nuts.windows-os-dependencies>
            org.fusesource.jansi:jansi
            com.github.vatbub:mslinks
        </nuts.windows-os-dependencies>
        <nuts.windows-os-x86_32-arch-dependencies>
            org.fusesource.jansi:jansi
            com.github.vatbub:mslinks
        </nuts.windows-os-x86_32-arch-dependencies>
    </properties>

    <dependencies>
    </dependencies>
</project>

```

### Nuts and Java MANIFEST.MF

```manifest

Manifest-Version: 1.0
Archiver-Version: Plexus Archiver
Built-By: vpc
Created-By: Apache Maven 3.8.1
Build-Jdk: 1.8.0_302

Nuts-Id: groupid:artifactid#version
Nuts-Dependencies: org.fusesource.jansi:jansi#1.2?os=windows;com.github.vatbub:mslinks#1.3?os=windows
Nuts-Name: Your App Name
Nuts-Generic-Name: Your App Generic Name
Nuts-Description: Your App Description
Nuts-Categories: /Settings/YourCategory;/Settings/YourCategory2
Nuts-Icons: classpath://net/yourpackage/yourapp/icon.svg;classpath://net/yourpackage/yourapp/icon.png
Nuts-Property-YourProp: YourValue

Comment: if the Nuts-Id could not be found, best effort will be used from the following
Automatic-Module-Name: yourgroupid.yourartifactid.YourClass
Main-Class: groupid.artifactid.YourClass
Implementation-Version: 1.2.3

```

### Nuts and Java 9 (jdeps)

Nuts supports `Automatic-Module-Name`.

```manifest
Automatic-Module-Name: yourgroupid.yourartifactid.YourClass

```

### Nuts and Gradle (TODO)


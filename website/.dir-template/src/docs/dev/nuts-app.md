---
id: nutsApp
title: Nuts Applications
sidebar_label: Nuts Applications
---
${include($"${resources}/header.md")}

# Making you first nuts application

Lets take, step by step, an example of an application that you will run using ```nuts``` package manager

1. create a java maven project First you will create the project using you favourite IDE or using mvn

```
mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-simple -DarchetypeVersion=1.4 -DinteractiveMode=false
```

you will have a fully generated java project

```aidl
vpc@linux-rogue:~/ttt> tree
.
└── my-app
    ├── pom.xml
    └── src
        ├── main
        │   └── java
        │       └── com
        │           └── mycompany
        │               └── app
        │                   └── App.java
        └── test
            └── java
                └── com
                    └── mycompany
                        └── app
                            └── AppTest.java

```

Now we will add some dependency to the project. Lets add `jexcelapi:jxl#2.4.2` and update pom.xml consequently.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.mycompany.app</groupId>
    <artifactId>my-app</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>jexcelapi</groupId>
            <artifactId>jxl</artifactId>
            <version>2.4.2</version>
        </dependency>
    </dependencies>
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>
</project> 
```

Now Update the App.java file

```java
package com.mycompany.app;

import java.io.File;

import jxl.Workbook;
import jxl.write.WritableWorkbook;

public class App {

    public static void main(String[] args) {
        try {
            WritableWorkbook w = Workbook.createWorkbook(new File("a.xls"));
            System.out.println("Workbook just created");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

```

finally compile the app:

```bash
mvn clean install
```

Of course, you won't be able to run the application yet. Would you? For this app to work there are several ways, all of
them are complicated and require modifying the pom and even modifying the output jar. You can for instance generate and
output lib directory and update the META-INF file using maven-dependency-plugin.
(see https://maven.apache.org/plugins/maven-shade-plugin ; https://www.baeldung.com/executable-jar-with-maven). You
could also use ```maven-assembly-plugin``` to include the dependencies into the jar itself ('what the fat' jar!).
Another alternative is to use an uglier solution with ```maven-shade-plugin``` and blend libraries into the main jar. In
all cases you need as well to configure maven-jar-plugin to specify the main class file.

I am not exposing all solutions here. You can read this article for more
details (https://www.baeldung.com/executable-jar-with-maven) but trust me, they all stink.

Instead of that we will use nuts. In that case, actually you are already done, the app is already okkay! you do not need
to specify the main class neither are your required to bundle jxl and its dependencies. you only need to run the app.
That's it.

```bash
nuts -y com.mycompany.app:my-app
```

this will install the application and run it on the fly. Dependencies will be detected, resolved and downloaded. The
application is installed from your local maven repository. It needs to be deployed to a public repository for it to be
publicly accessible, however.

You can also choose not to install the app and bundle it as a jar. No need for a public repository in that case:

```bash
nuts -y com my-app-1.0.0-SNAPSHOT.jar
```

As we can see, nuts provides the simplest and thee most elegant way to deploy your application.

One question though. what happens if we define multiple main methods (in multiple public classes). It is handled as well
by ```nuts``` seamlessly. It just asks, at runtime, for the appropriate class to run.

# Using Nuts Application Framework

Using ```nuts``` if transparent as we have seen so far. It is transparent both at build time and run time.
However, ```nuts``` can provide our application a set of unique helpful features, such as install adn uninstall hooks,
comprehensive command line support and so on.

To create your first ```NAF``` application, you will need to add nuts as a dependency and change your main class as
follows:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.mycompany.app</groupId>
    <artifactId>my-app</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>net.thevpc.nuts</groupId>
            <artifactId>nuts</artifactId>
            <version>0.8.3</version>
        </dependency>
        <dependency>
            <groupId>jexcelapi</groupId>
            <artifactId>jxl</artifactId>
            <version>2.4.2</version>
        </dependency>
    </dependencies>
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <nuts.application>true</nuts.application>
    </properties>
</project> 
```
Please take note that we have added a property 'nuts.application=true'. 
Actually this is no mandatory, but this will help ```nuts``` package manager detect that this application uses NAF before
downloading it jar (the information will be available in the ```pom.xml``` descriptor on the remote repository).

Then we will add some cool features to our application. We write a dummy message whenever the application is installed, uninstalled or updated.
We will also add support to "--file=[path]" argument to specify the workbook path.

```java
package com.mycompany.app;

import java.io.File;

import jxl.Workbook;
import jxl.write.WritableWorkbook;

public class App implements NutsApplication {

    public static void main(String[] args) {
        // just create an instance and call runAndExit in the main method
        new App().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext applicationContext) {
        NutsSession s = applicationContext.getSession();
        NutsCommandLine cmd = applicationContext.getCommandLine();
        File file = new File("a.xls");
        while (cmd.hasNext()) {
            switch (cmd.getKeyString()) {
                case "--file": {
                    NutsArgument a = cmd.nextString();
                    file = new File(a.getStringValue());
                    break;
                }
                default: {
                    cmd.unexpectedArgument();
                }
            }
        }
        try {
            WritableWorkbook w = Workbook.createWorkbook(file);
            s.out().printf("Workbook just created at %s%n", file);
        } catch (Exception ex) {
            ex.printStackTrace(s.err());
        }
    }

    @Override
    public void onInstallApplication(NutsApplicationContext applicationContext) {
        NutsSession s = applicationContext.getSession();
        s.out().printf("we are installing My Application : %s%n", applicationContext.getId());
    }

    @Override
    public void onUninstallApplication(NutsApplicationContext applicationContext) {
        NutsSession s = applicationContext.getSession();
        s.out().printf("we are uninstalling My Application : %s%n", applicationContext.getId());
    }

    @Override
    public void onUpdateApplication(NutsApplicationContext applicationContext) {
        NutsSession s = applicationContext.getSession();
        s.out().printf("we are updating My Application : %s%n", applicationContext.getId());
    }
}

```

now we can install or uninstall  the application and see the expected message.

```bash
nuts -y install com.mycompany.app:my-app
nuts -y uninstall com.mycompany.app:my-app
```



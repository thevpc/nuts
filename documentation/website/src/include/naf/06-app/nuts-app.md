---
id: nutsApp
title: Your first Application using nuts
---


## Running your application with Nuts

Lets take, step by step, an example of an application that you will run using ```nuts``` package manager

First we can create the project using your favourite IDE or using simply `mvn` command

```
mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-simple -DarchetypeVersion=1.4 -DinteractiveMode=false
```

We will have a fully generated java project

```bash
~/> tree
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

Now we will add some dependencies to the project. Let's add `jexcelapi:jxl#2.4.2` and update `pom.xml` consequently.

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

Now we update the App.java file

```java
package com.mycompany.app;

import java.io.File;

import jxl.Workbook;
import jxl.write.WritableWorkbook;

public class App {

    public static void main(String[] args) {
        try {
            WritableWorkbook w = Workbook.createWorkbook(new File("any-file.xls"));
            System.out.println("Workbook just created");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

```

finally we compile the app:

```bash
mvn clean install
```

Of course, we won't be able to run the application yet. Would we? For this app to work there are several ways, all of
them are complicated and require modifying the `pom.xml`  and even modifying the output jar. we can for instance generate an
output lib directory and update the `META-INF` file using `maven-dependency-plugin`.
(see https://maven.apache.org/plugins/maven-shade-plugin ; https://www.baeldung.com/executable-jar-with-maven). We
could also use `maven-assembly-plugin` to include the dependencies into the jar itself ('what the fat' jar!).
Another alternative is to use an uglier solution with ```maven-shade-plugin``` and blend libraries into the main jar. In
all cases we need as well to configure `maven-jar-plugin` to specify the main class file.

I am not exposing all solutions here. You can read this article for more
details (https://www.baeldung.com/executable-jar-with-maven) but trust me, they all stink.

Instead of that we will use `nuts`. In that case, actually we are already done, the app is already OK! We do not need
to specify the main class neither are we required to bundle `jxl` and its dependencies. We only need to run the app.
That's it.

Basically, you can install the application using its identifier `com.mycompany.app:my-app`. The latest version will be resolved.

```bash
nuts install com.mycompany.app:my-app
nuts my-app
```

This will install the application and run it on the fly. Dependencies will be detected, resolved and downloaded. The
application is installed from local maven repository. It needs to be deployed to a public repository for it to be
publicly accessible, however.

We can also choose not to install the app and bundle it as a jar. No need for a public repository in that case:

```bash
nuts -y com my-app-1.0.0-SNAPSHOT.jar
```

As we can see, `nuts` provides the simplest and the most elegant way to deploy your application.

One question though. what happens if we define multiple main methods (in multiple public classes). It's handled as well
by ```nuts``` seamlessly. It just asks, at runtime, for the appropriate class to run.

# Using Nuts Application Framework

Using ```nuts``` is transparent as we have seen so far. It's transparent both at build time and runtime.
However, ```nuts``` can provide our application a set of unique helpful features, such as install and uninstall hooks,
comprehensive command line support and so on.

To create your first ```NAF``` application, you will need to add nuts as a dependency and change your `pom.xml` as follows:

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
            <artifactId>nuts-lib</artifactId>
            <version>0.8.6.0</version>
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
Please take note that we have added a property `nuts.application=true`. 
Actually this is not mandatory, but this will help ```nuts``` package manager detect that this application uses NAF before downloading its jar (the information will be available in the ```pom.xml``` descriptor on the remote repository).

Then we will add some cool features to our application. We write a dummy message whenever the application is installed, uninstalled or updated.
We will also add support to "--file=[path]" argument to specify the workbook path.

```java
package com.mycompany.app;

import java.io.File;

import jxl.Workbook;
import jxl.write.WritableWorkbook;

public class App implements NApplication {

    public static void main(String[] args) {
        // just create an instance and call runAndExit in the main method
        // this method ensures that exist code is well propagated
        // from exceptions to caller processes
        new App().run(NAppRunOptions.ofExit(args));
    }

    @Override
    public void run() {
        NCmdLine cmd = NApp.of().getCmdLine();
        File file = new File("file.xls");
        while (cmd.hasNext()) {
            switch (cmd.getKey().getString()) {
                case "--file": {
                    NArg a = cmd.nextEntry().get();
                    file = new File(a.getStringValue());
                    break;
                }
                case "--fill": {
                    // process other options here ...
                    break;
                }
                default: {
                    s.configureLast(cmd);
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

    @Override // this method is not required, implement when needed
    public void onInstallApplication() {
        NOut.println(NMsg.ofC("we are installing My Application : %s%n", NApp.of().getId()));
    }

    @Override // this method is not required, implement when needed
    public void onUninstallApplication(NSession s) {
        NOut.println(NMsg.ofC("we are uninstalling My Application : %s%n", NApp.of().getId()));
    }

    @Override // this method is not required, implement when needed
    public void onUpdateApplication(NSession s) {
        NOut.println(NMsg.ofC("we are updating My Application : %s%n", NApp.of().getId()));
    }
}

```

Now we can install or uninstall the application and see the expected messages.

```bash
nuts -y install com.mycompany.app:my-app
nuts -y uninstall com.mycompany.app:my-app
```



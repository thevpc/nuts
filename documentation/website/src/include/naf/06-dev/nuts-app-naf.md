---
id: nutsAppNAF
title: Nuts Application Framework
sidebar_label: Your first Application using NAF
---


# Using Nuts Application Framework (NAF)

Using ```nuts``` is transparent as we have seen so far. It's transparent both at build time and runtime.
However, ```nuts``` can provide our application a set of unique helpful features, such as install and uninstall hooks, comprehensive command line support and so on.

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
            <artifactId>nuts-api</artifactId>
            <version>0.8.5</version>
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
        // this method ensures that exist code is well propagted
        // from exceptions to caller processes
        new App().run(NAppRunOptions.ofExit(args));
    }

    @Override
    public void run(NSession session) {
        NCmdLine cmd = session.getAppCmdLine();
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
    public void onInstallApplication(NSession s) {
        s.out().printf("we are installing My Application : %s%n", s.getAppId());
    }

    @Override // this method is not required, implement when needed
    public void onUninstallApplication(NSession s) {
        s.out().printf("we are uninstalling My Application : %s%n", s.getAppId());
    }

    @Override // this method is not required, implement when needed
    public void onUpdateApplication(NSession s) {
        s.out().printf("we are updating My Application : %s%n", s.getAppId());
    }
}

```

Now we can install or uninstall the application and see the expected messages.

```bash
nuts -y install com.mycompany.app:my-app
nuts -y uninstall com.mycompany.app:my-app
```



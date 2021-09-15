---
id: building
title: Building
sidebar_label: Building Nuts Projects
---

${{include($"${resources}/header.md")}}

To build`nuts` Package Management you need the following software installed on your machine:
* java JDK 8 (`nuts` is still compatible with java 8)
* maven 3.8+
* GnuPG (gpg) 2+

## Preparing the development environment

If you do not have a private key, just create one:
```bash
gpg --gen-key
```

then update (or create) `~/.m2/settings.xml` file with the following content (make sure to put your own keyname and passphrase:

```xml
<settings xmlns="http://maven.apache.org/settings/1.0.0">
  <profile>
      <id>ossrh</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.executable>gpg2</gpg.executable>
        <gpg.keyname>YOUR-KEY-444B05CFD2263E2EB91FD083C7E3C476060E40DD</gpg.keyname>
        <gpg.passphrase>YOUR PASSWORD</gpg.passphrase>
      </properties>
    </profile>
  </settings>
```

## Compiling with maven

assuming your have downloaded to sources using git as follows:

```bash
git clone https://github.com/thevpc/nuts.git
cd nuts
```

you just invoke mvn install on the project to compile all of the project:

```bash
mvn clean install
```

That being done, nuts will be compiled and installed into your local maven repository

## Building website

The next thing we need to worry about is the building of nuts community website.
To do so we will need to install locally ```nuts``` and ```nsh``` and run a
In the root directory issue the following command

```sh
    ./nuts-build-website
```

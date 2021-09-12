# Contributing to Nuts

Thanks for your interest in `nuts`. 
Our goal is to leverage the power of Java, Maven and Gradle to build a rock solid package manager.

## Getting Started

`nuts`'s [open issues are here](https://github.com/thevpc/nuts/issues). 
In time, we'll tag issues that would make a good first pull request for new contributors. 
An easy way to get started helping the project is to *file an issue*. 
You can do that on the `nuts` issues page by clicking on the green button at the right. 
Issues can include bugs to fix, features to add, or documentation that looks outdated.

For some tips on contributing to open source, this [post is helpful](http://blog.smartbear.com/programming/14-ways-to-contribute-to-open-source-without-being-a-programming-genius-or-a-rock-star/).

## Contributions

`nuts` welcomes contributions from everyone.

Contributions to `nuts` should be made in the form of GitHub pull requests. Each pull request will
be reviewed by a core contributor (someone with permission to land patches) and either landed in the
main tree or given feedback for changes that would be required.

---------------
## Preparing Dev Environment
To contribute to `nuts` Package Management Development you need the following software installed on your machine:
* java JDK 8 (`nuts` is still compatible with java 8)
* maven 3.8+
* GnuPG (gpg) 2+
* You favorite IDE (I'm using Netbeans and sometimes IntellijIdea and very sporadically Eclipse)

create a key:
```bash
gpg --gen-key
```

update (or create) `~/.m2/settings.xml` file with the following content:

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

## Compiling Nuts
Here is the typical commands to get your own local copy of `nuts` sources and to compile them:

```bash
git clone https://github.com/thevpc/nuts.git
cd nuts
mvn clean install
```

and here is how to generate the website and documentation (assuming you are under `nuts` repo root folder)

```bash
./nuts-build-website
```

## Pull Request Checklist

- Branch from the master branch and, if needed, rebase to the current master
  branch before submitting your pull request. If it doesn't merge cleanly with
  master you may be asked to rebase your changes.

- Commits should be as small as possible, while ensuring that each commit is
  correct independently (i.e., each commit should compile and pass tests).

- Don't put sub-module updates in your pull request unless they are to landed
  commits.

- If your patch is not getting reviewed or you need a specific person to review
  it, you can @-reply a reviewer asking for a review in the pull request or a
  comment.

- Add tests relevant to the fixed bug or new feature.


## Quick Look on sources organization
The repository is organized in several folders described here after:

* **[.dir-template]** : contains template files for generating `README.md` and `METADATA` (among other) files according to the current `nuts` development version
* **[core]**          : contains the core of `nuts` package manager (and the only required pieces for `nuts` to work). Practically this contains the Bootstrap (and API) project (called `nuts`) and the Runtime (Implementation) project (called `nuts-runtime`)
* **[docs]**          : contains a generated (using docusaurus) web site that is deployed to github pages (https://thevpc.github.io/nuts/)
* **[ext]**           : contains some `nuts` extensions/plugins
* **[install]**       : ignore this for the moment :), it is a work on progress and an attempt to simplify `nuts` installation process. Still very embryonic though.
* **[lib]**           : contains a suite of libraries that are based on `nuts` and that can be used by other applications.
* **[test]**          : contains unit test projects
* **[toolbox]**       : contains a suite of applications that are based on `nuts` and that complement `nuts` features
* **[web-toolbox]**   : contains a suite of web applications that are based on `nuts` and that complement `nuts` features
* **[website]**       : contains the sources of `nuts`'s docusaurus based website.
  * **[website/.dir-template]**       : contains the effective sources of `nuts`'s documentation (used to create the website as well).

--------------------------------

  This CONTRIBUTING.md file is adapted from the [DeepLearning4j CONTRIBUTING.md](https://alvinalexander.com/java/jwarehouse/deeplearning4j/CONTRIBUTING.md.shtml)

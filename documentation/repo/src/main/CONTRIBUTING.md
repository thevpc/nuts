 # Contributing to Nuts

Thanks for your interest in `nuts`. 
Our goal is to leverage the power of Java, Maven and Gradle to build a rock solid package manager.

## Getting Started

`nuts` is [open issues are here](https://github.com/thevpc/nuts/issues). 
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
## Preparing your Dev Environment
To contribute to `nuts` Package Management Development you need the following software installed on your machine:
* java JDK 8 (`nuts` is still compatible with java 8)
* maven 3.8+
* You favorite IDE (I'm using Netbeans and sometimes IntellijIdea and very sporadically Eclipse)

## Compiling Nuts
Here is the typical commands to get your own local copy of `nuts` sources and to compile them:

```bash
git clone https://github.com/thevpc/nuts.git
cd nuts
mvn clean install
```

and here is how to compile/generate the website and documentation (assuming you are under `nuts` repo root folder)

```bash
cd installers/nuts-release-tool
java -jar target/nuts-release-tool-${{runtimeVersion}}.jar
```

You can now play with your development version of nuts using the generated `nuts-dev` script.
You may want to update the following line to match your java 8 JDK install location or simply add it to your shell `rcfile`. 

```sh
NUTS_JAVA_HOME=/usr/lib64/jvm/java-1.8.0-openjdk-1.8.0
```

Indeed, you must compile `nuts` with java 8 because nuts needs to be working on Java 8 and all later java versions (
this means that compiling on more recent versions of java should pass successfully as well). So you must
not use deprecated features (in java9+) in nuts source code (examples : js nashorn, rmi activation, etc...) 

## Pull Request Checklist

- Branch from the master branch and, if needed, rebase to the current master
  branch before submitting your pull request. If it doesn't merge cleanly with
  master you may be asked to rebase your changes.

- Commits should be as small as possible, while ensuring that each commIt's
  correct independently (i.e., each commit should compile and pass tests).

- Don't put sub-module updates in your pull request unless they are to landed
  commits.

- If your patch is not getting reviewed or you need a specific person to review
  it, you can @-reply a reviewer asking for a review in the pull request or a
  comment.

- Add tests relevant to the fixed bug or new feature.


## How to contribute
You can contribute in a myriad of ways:

* submitting issues on [github issues corner](https://github.com/thevpc/nuts/issues) and adding any information you judge important for the maintainers. 
  please mark them with 'bug' label. `nuts` should make best effort to work on any environment. So if you encounter any malfunctioning, please contribute with submitting the issue. We are actually unable to test on all environments, so you really are our best hope!   
* submitting a feature request again on [github issues corner](https://github.com/thevpc/nuts/issues)
  please detail your idea and mark it with 'enhancement' label.
* working on existing issues. The issues are marked with labels. The priority is given always to the current version milestone (example 0.8.3).
  The complexity of the issue is estimated with the `T-shirt sizing` approach: `size-xxs` is the simplest, `size-m` is medium sized and `size-xxl` is the more complex one.
  Complexity is relative to both required time to do the task and experience on ```nuts``` project to do the work. So please start with smallest (simplest) issues. 
* working on media and UX by submitting enhancements/replacements of existing website/icons/themes, etc... 
* writing in press about nuts 

## Quick Look on sources organization
The repository is organized in several folders described here after:

* **[.dir-template]** : contains template files for generating `README.md` and `METADATA` (among other) files according to the current `nuts` development version
* **[core]**          : contains the core of `nuts` package manager (and the only required pieces for `nuts` to work). Practically this contains the Bootstrap (and API) project (called `nuts`) and the Runtime (Implementation) project (called `nuts-runtime`)
* **[companions]**    : contains the 'companion' aka main applications bundled with `nuts` (mainly `nsh`).
* **[docs]**          : contains a generated (using docusaurus) web site that is deployed to github pages (https://thevpc.github.io/nuts/)
* **[extensions]**    : contains some `nuts` extensions/plugins. as an example it includes and extension for nuts terminal features implemented using `jline` library
* **[incubating]**    : ignore this for the moment :), It's a work in progress and an attempt to simplify `nuts` installation process and other frozen features. Still very embryonic.
* **[libraries]**     : contains a suite of libraries that are based on `nuts` and that can be used by other applications. This includes markdown parsers, ssh support, etc...
* **[test]**          : contains unit test projects
* **[toolbox]**       : contains a suite of applications that are based on `nuts` and that complement `nuts` features. This includes `nsh` the shell companion
* **[tutorials]**     : contains a suite of tutorial applications on how to use `nuts` and a framework and its integration with other frameworks such as `spring`
* **[web-toolbox]**   : contains a suite of web applications that are based on `nuts` and that complement `nuts` features. This includes `nwar`, a servlet implementation to serve `nuts` workspaces.
* **[documentation]** : contains the sources of `nuts`'s documentations
  * **[website]**                     : contains the sources of `nuts`'s docusaurus based website.
  * **[website/.dir-template]**       : contains the effective sources of `nuts`'s documentation (used to create the website as well). To be more precise, the website is built using a two steps process: first we pre-process the markdown files with template processing (using `ndoc`) that will handle things like `nuts` version variables, documentation structure etc and hence prepare the `docusaurus` base folder. And then, we run `docusaurus` to generate the effective `html`/`js` for the statically compiled website.
  * **[presentations]**               : contains several presentations about `nuts`
  * **[media]**                       : contains images/logos/icons

## Running, testing and Working with nuts-dev, in development environment

Here are some tips when working on nuts project or even working on an application that builds on `nuts` using NAF (aka Nuts Application Framework) for example:

* ```nuts-dev``` is the script you are most of the time using when developing `nuts` project.

* ```nuts-dev``` script uses a special workspace called ```development```, so it does not interfere with your local `nuts` installation.

* you can always change the workspace in ```nuts-dev``` using ```-w``` option
```sh 
./nuts-dev -w test
```


* You can run nuts in debug mode with `--debug` that shall be the very **FIRST** option. The following example will spawn a jvm listening on the 5005/tcp port you can attach to your favorite IDE.

```sh 
./nuts-dev --debug
```

* Always make sure you are working on a clean workspace, to have a reproducible environment,
```sh 
./nuts-dev -Zy
```

* If you are willing to run directly from your IDE, make sure you add `-w=development` or `-w=test` to 
the program command line arguments as an example to work on a separate workspace than the one used in production or locally
 
* If you want to debug an application running under `nuts`, you can just debug `nuts` project using the embedded flag
( `-b` or `--embedded`) to run that application in the same virtual machine
```sh 
nuts -w test -b my-app
```

* If you want to debug an application running under `nuts` in a separate virtual machine, use the option `--debug` option to run the application in debug mode listening to the 5005 tcp port, then attach it to your IDE. This will debug  `nuts` itself but will make possible running spawn jvm as well, the effective debugging port will be randomly selected and  displayed on your stdout. You will need to attach another jvm to your IDE using that port.

```sh 
nuts -w test --debug my-app
# or
nuts -w test --debug=5010 my-app
```

* When you need to have more information about what `nuts` is doing under the hood, just run it in verbose mode

```sh 
./nuts-dev --verbose install some-application
```

* You may want to disable creation of shortcuts and desktop icons in development mode:
```sh 
./nuts-dev --!init-launchers
```


* You may want to disable all repositories and use solely your local maven repo:

```sh 
./nuts-dev -r=maven-local
```

## Some ideas on how to contribute
* To contribute start by subscribing as contributor in github, fork the repository and push pull-requests 
* You can contribute by: 
  * issuing bug reports to github issues, particularly, we are looking for people who can use different environments (operating system, java version, architecture etc...)
  * providing medias/icons for the website
  * reimplement the website
  * fixing issues that are pushed to github issues
  * implementing new features in `nuts`
  * updating the documentation (README and Docusaurus WebSite)
  * becoming `nuts` module maintainer, here are modules that you can focus on
    * nsh (a bash compatible tool), basically you will implement new commands and fix existing commands (such as cd, zip etc...) 
    * ncode  (a tool that searches into  jars for classes)
    * ndoc  (a tool that generates source documentation from Markdown enabled javadocs)
    * njob (a task list commandline tool)
    * noapi (a tool that generated pdf from OpenAPI documentation)
    * nserver (a tool that runs a `nuts` web repository)
    * ndoc  (a tool that generates folders and files from templates)
    * nversion (a tool that parses jars, zips, pom files and folders to detect versions)
    * `nuts` Installer (a Swing GUI Installer App)
    * `nuts` Store (a JavaFX GUI App for installing apps using `nuts` without commandline)
  * creating a new application for `nuts`
  * finding and reporting any interesting application available on maven (or not) that can be (or should be) installable using `nuts`
  * creating some tutorial projects (source code) on how to use NAF (nuts application Framework)
  * building your very own java app and push it to maven central (no constraints)



--------------------------------

  This CONTRIBUTING.md file is adapted from the [DeepLearning4j CONTRIBUTING.md](https://alvinalexander.com/java/jwarehouse/deeplearning4j/CONTRIBUTING.md.shtml)

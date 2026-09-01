 # Contributing to Nuts

Thanks for your interest in `nuts`. 
Our goal is to leverage the power of Java, Maven and Gradle to build a rock solid package manager.

## Getting Started

`nuts` is [open issues are here](https://github.com/thevpc/nuts/issues). 
In time, we'll tag issues that would make a good first pull request for new contributors. 
An easy way to get started helping the project is to *file an issue*. 
You can do that on the `nuts` issues page by clicking on the green button at the right. 
Issues can include bugs to fix, features to add, or documentation that looks outdated.

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

Before running release tools or testing local builds, compile the repository using Maven:

```bash
git clone https://github.com/thevpc/nuts.git
cd nuts
mvn clean install
```

> **Target Compatibility**: You must compile `nuts` targeting Java 8 (`nuts` must remain compatible from Java 8 through Java 24+). Do not use features or APIs deprecated/removed in Java 9+ (e.g., Nashorn JS engine, RMI activation).

---

## Updating Documentation & Website (`nuts-release-tool` & `nsite`)

The website and root Markdown files (`README.md`, `CONTRIBUTING.md`, etc.) are generated using [nsite](https://github.com/thevpc/nsite) via [nuts-release-tool](https://github.com/thevpc/nuts-release-tool).

⚠️ **CRITICAL RULES**:
- **Do NOT edit root `README.md` or `CONTRIBUTING.md` directly!** They are generated from template files in `documentation/repo/src/main/`.
- **Do NOT edit the `docs/` directory directly!** The root `docs/` folder contains generated static HTML output. Any changes in `docs/` will be overwritten when `nuts-release-tool` runs.
- For complete details on website source folders, templates, and deprecated folders (such as `documentation/website/archive/`), see **[documentation/website/README.md](documentation/website/README.md)**.

### How to update website and root docs:
1. First, ensure the project is compiled: `mvn clean install`.
2. Make your changes in the template source files inside `documentation/repo/src/main/` (or website files in `documentation/website/src/`).
3. Run `nuts-release-tool` **directly from the repository root**:

```bash
# On Linux / macOS (executed under nuts repo root):
./nuts-release-tool

# On Windows (Command Prompt / PowerShell under nuts repo root):
nuts-release-tool.bat
```

The script directly invokes `java -jar` using your locally compiled Maven artifacts (`~/.m2/repository` or `target/`), downloading the release runtime if no local build is found.

`nuts-release-tool` will:
- Parse `nuts-release-tool.tson` configuration at repository root.
- Pre-process markdown templates using `nsite` (replacing variables like `1.0.0.0` and `0.8.9.0`).
- Generate root `README.md`, `CONTRIBUTING.md`, and update the static HTML site in `docs/` (including `docs/download.html`, published at [https://thevpc.github.io/nuts/download.html](https://thevpc.github.io/nuts/download.html)).

---

## Pull Request Checklist

- Branch from the `master` branch and ensure commits compile and pass tests cleanly (`mvn clean test`).
- Commits should be as small as possible while ensuring each commit builds independently.
- If updating documentation or root README, ensure changes are made in `documentation/repo/src/main/` or `documentation/website/` and verified by running `./nuts-release-tool` at repo root.
- Add tests relevant to the fixed bug or new feature.


## How to contribute
You can contribute in a myriad of ways:

* submitting issues on [github issues corner](https://github.com/thevpc/nuts/issues) and adding any information you judge important for the maintainers. 
  please mark them with 'bug' label. `nuts` should make best effort to work on any environment. So if you encounter any malfunctioning, please contribute with submitting the issue. We are actually unable to test on all environments, so you really are our best hope!   
* submitting a feature request again on [github issues corner](https://github.com/thevpc/nuts/issues)
  please detail your idea and mark it with 'enhancement' label.
* working on existing issues. The issues are marked with labels. The priority is given always to the current version milestone (example 1.0.0).
  The complexity of the issue is estimated with the `T-shirt sizing` approach: `size-xxs` is the simplest, `size-m` is medium sized and `size-xxl` is the more complex one.
  Complexity is relative to both required time to do the task and experience on ```nuts``` project to do the work. So please start with smallest (simplest) issues. 
* working on media and UX by submitting enhancements/replacements of existing website/icons/themes, etc... 
* writing in press about nuts 

## Quick Look on sources organization
The repository is organized into several key sub-projects:

* **[core]**          : contains the core of `nuts` package manager:
  * `nuts-boot`: Zero-dependency workspace bootstrapper library.
  * `nuts-api`: Core public API contracts and SPI interfaces.
  * `nuts-runtime`: Execution engine loaded dynamically at runtime by `nuts-boot`.
  * `nuts-app`: Lightweight CLI launcher JAR.
  * `nuts-app-full`: Standalone fat binary with embedded runtime.
* **[installers]**    : contains GUI installers and `nuts-release-tool`.
* **[libraries]**     : contains standard integration libraries (`nuts-spring-boot`, `nuts-slf4j`, `nuts-swing`, etc.).
* **[extensions]**    : contains optional plugins (e.g. `nuts-term`, `nuts-ssh`).
* **[toolbox]** & **[companions]**: contains CLI applications built on top of `nuts` (e.g. `nsh` shell).
* **[documentation]** : contains site sources and template files:
  * `documentation/repo/src/main/`: Templates for root `README.md`, `CONTRIBUTING.md`, and dev scripts.
  * `documentation/website/src/main/`: Source HTML pages (`download.html`, `doc-nuts.html`, `index.html`) processed by `nsite`.
  * `documentation/website/src/include/`: Modular markdown inclusions (`include/download/`, `include/doc-nuts/`, etc.).
* **[docs]**          : static HTML site output generated by `nsite` via `nuts-release-tool` and published on GitHub Pages.
* **[test]**          : integration and unit test projects.

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

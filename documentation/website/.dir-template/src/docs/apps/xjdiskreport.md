---
id: app-xjdiskreport
title: JDiskReport
sidebar_label: JDiskReport
order: 2
---

${{include($"${resources}/header.md")}}

## Nuts Bash Like Shell
This is a list of applications that can be installed and run using ```nuts``` 

### Available Terminal Applications

This is a list of terminal applications that can/could be installed using ```nuts```

* T0001- Nuts shell (Bash compatible Shell)
```
nuts install nsh
# Example of usage
nuts nsh
```
* T0002- Nuts Version (File/Folder/Project version detector)
```
nuts install nversion
# Example of usage
nuts nversion ./your-maven-project-folder
nuts nversion your-jar-file.jar
```
* T0003- Nuts Templater (Folder Templating Tool)
```
nuts install ntemplate
# Example of usage
nuts ntemplate -p .dir-template -t .
```
* T0004- Nuts Repository (Nuts Repository Server)
```
nuts install nserver
# Example of usage
nuts nserver --http
```
* T0005- Nuts Open Api (Open Api Pdf Generator)
```
nuts install noapi
# Example of usage
nuts noapi your-apis.yaml
```
* T0006- Docusaurus (Templater Companion for Facebook's Docusaurus)
```
nuts install ndocusaurus
# Example of usage
nuts ndocusaurus -d ./website pdf build
```
* T0007- Tomcat Web Sever
```
nuts install ntomcat
# Example of usage
nuts ntomcat --start
```
* T0008- Derby DB
```
nuts install ndb
# Example of usage
nuts ndb derby --start
```
* T0009- MySQL Controller
```
nuts install ndb
# Example of usage
nuts ndb mysql --backup
```
* T0010- Maven (Maven Build Tool)
```
nuts install nmvn
# Example of usage
nuts nmvn clean install
```
* T0011- Nuts Job (Task List App)
```
nuts install njob
# Example of usage
nuts njob tasks add 'I will by milk' --on afternoon
```
* T0012- Nuts Diff (Jar Diff Tool)
```
nuts install ndiff
# Example of usage
nuts ndiff my-app-v1.jar my-app-v2.jar
```
* T0013- Nuts Code Search (Code Search Tool)
```
nuts install ncode
# Example of usage
nuts ncode -t Repo*Manager nuts-runtime-0.8.3.0.jar
```
* T0014- Spring Cli (Spring Boot Client App)
```
nuts install org.springframework.boot:spring-boot-cli
nuts settings add alias spring="--main-class=1 spring-boot-cli"
# Examples of usage
nuts spring --version
nuts spring init --dependencies=web,data-jpa my-project
``` 

* T0015- Google Tsunami (Spring Boot Client App)
```
nuts com.google.tsunami:tsunami-main
nuts settings add alias tsunami='--cp=${NUTS_ID_APPS}/your-plugins-folder/*.jar tsunami-main'
# Example of usage
nuts tsunami --ip-v4-target=127.0.0.1
``` 

### Available GUI Applications

This is a list of GUI applications that can be installed and run using ```nuts```

* G0001- Netbeans Launcher (Netbeans IDE multi-workspace Launcher)
```
nuts install netbeans-launcher
# Example of usage
nuts netbeans-launcher
```
* G0002- Pangaea Note (Note Taking Application)
```
nuts install pnote
# Example of usage
nuts pnote
```
* G0003- JMeld (Diff Tool)
```
nuts install org.jmeld:jmeld
# Example of usage
nuts jmeld
```
* G0004- Binjr (Time Series Dashboard)
```
nuts install eu.binjr:binjr-core
# Example of usage
nuts binjr-core
```
* G0005- OmegaT (Translation Tool)
```
nuts install org.omegat:omegat
# Example of usage
nuts omegat
```
* G0006- kifkif (File/Folder Duplicates finder)
```
  nuts install kifkif
# Example of usage
  nuts kifkif
```
* G0007- jpass (Password app)
```
  nuts install jpass:jpass
# Example of usage
  nuts jpass
```
* G0008- omnigraph (Graph Editor)
```
  nuts install com.github.todense:omnigraph
# Example of usage
  nuts omnigraph
```
* G0009- jedit (JEdit Text Editor)
```
  nuts install org.jedit:jedit
# Example of usage
  nuts jedit
```
* G0010- mucommander (File Manager)
```
  nuts install com.mucommander:mucommander
# Example of usage
  nuts mucommander
```
* G0011- Java Decompiler GUI (Java Decompiler)
```
  nuts install org.jd:jd-gui
# Example of usage
  nuts jd-gui
```
* G0012- Flappy Bird (Java Decompiler)
```
  nuts install io.github.jiashunx:masker-flappybird
# Example of usage
  nuts masker-flappybird
```
* G0013- Mindustry Desktop (Game)
```
  nuts install com.github.anuken:mindustry-desktop
# Example of usage
  nuts mindustry-desktop
```
* G0013- Mindustry Server (Game Server)
```
  nuts install com.github.anuken:mindustry-server
# Example of usage
  nuts mindustry-server
```
* G0014- JDiskReport (Disk Usage Utility)
```
  nuts install com.jgoodies:jdiskreport
# Example of usage
  nuts jdiskreport
```

### Not yet available GUI Applications

* dbclient (Jdbc client)
```
  nuts install dbclient
# Example of usage
  nuts dbclient
```
* netbeans (Netbeans IDE)
```
  nuts install netbeans
# Example of usage
  nuts netbeans
```
* eclipse (Eclipse IDE)
```
  nuts install eclipse
# Example of usage
  nuts eclipse
```


### Other Pending Applications

* Nuts Talk Agent (Client To Client Communication)
```
nuts install ntalk-agent
nuts ntalk-agent
```
* Nuts Dev Tool (Developer Toolbox)
```
nuts install nwork
nuts nwork
```


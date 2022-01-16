---
id: apps
title: Available Apps
sidebar_label: Available Apps
order: 2
---
${{include($"${resources}/header.md")}}

## Java Applications
This is a list of applications that can be installed and run using ```nuts``` 

### Available Terminal Applications

This is a list of terminal applications that can/could be installed using ```nuts```

* T0001- Nuts shell (Bash compatible Shell)
```
nuts install nsh
nuts nsh
```
* T0002- Nuts Version (File/Folder/Project version detector)
```
nuts install nversion
nuts nversion
```
* T0003- Nuts Templater (Folder Templating Tool)
```
nuts install ntemplate
nuts ntemplate
```
* T0004- Nuts Repository (Nuts Repository Server)
```
nuts install nserver
nuts nserver
```
* T0005- Nuts Open Api (Open Api Pdf Generator)
```
nuts install noapi
nuts noapi
```
* T0006- Docusaurus (Templater Companion for Facebook's Docusaurus)
```
nuts install ndocusaurus
nuts ndocusaurus
```
* T0007- Tomcat Web Sever
```
nuts install ntomcat
nuts ntomcat
```
* T0008- Derby DB
```
nuts install ndb
nuts ndb
```
* T0009- MySQL Controller
```
nuts install ndb
nuts ndb
```
* T0010- Maven (Maven Build Tool)
```
nuts install nmvn
nuts nmvn
```
* T0011- Nuts Job (Task List App)
```
nuts install njob
nuts njob
```
* T0012- Nuts Diff (Jar Diff Tool)
```
nuts install ndiff
nuts ndiff
```
* T0013- Nuts Code Search (Code Search Tool)
```
nuts install ncode
nuts ncode
```
* T0014- Spring Cli (Spring Boot Client App)
```
nuts install org.springframework.boot:spring-boot-cli
nuts settings add alias spring="--main-class=1 spring-boot-cli"
nuts spring --version
nuts spring init --dependencies=web,data-jpa my-project
``` 

### Available GUI Applications

This is a list of GUI applications that can be installed and run using ```nuts```

* G0001- Netbeans Launcher (Netbeans IDE multi-workspace Launcher)
```
nuts install netbeans-launcher
nuts netbeans-launcher
```
* G0002- Pangaea Note (Note Taking Application)
```
nuts install pnote
nuts pnote
```
* G0003- JMeld (Diff Tool)
```
nuts install org.jmeld:jmeld
nuts jmeld
```
* G0004- Binjr (Time Series Dashboard)
```
nuts install eu.binjr:binjr-core
nuts binjr-core
```
* G0005- OmegaT (Translation Tool)
```
nuts install org.omegat:omegat
nuts omegat
```
* G0006- kifkif (File/Folder Duplicates finder)
```
  nuts install kifkif
  nuts kifkif
```
* G0007- jpass (Password app)
```
  nuts settings add repo --name=goodies
  nuts install jpass:jpass
  nuts jpass
```
* G0008- omnigraph (Graph Editor)
```
  nuts settings add repo --name=goodies
  nuts install omnigraph:omnigraph
  nuts omnigraph
```
* G0009- jedit (JEdit Text Editor)
```
  nuts settings add repo --name=goodies
  nuts install org.jedit:jedit
  nuts jedit
```
* G0010- mucommander (File Manager)
```
  nuts settings add repo --name=goodies
  nuts install com.mucommander:mucommander
  nuts mucommander
```

### Not yet available GUI Applications

* dbclient (Jdbc client)
```
  nuts install dbclient
  nuts dbclient
```
* netbeans (Netbeans IDE)
```
  nuts install netbeans
  nuts netbeans
```
* eclipse (Eclipse IDE)
```
  nuts install eclipse
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


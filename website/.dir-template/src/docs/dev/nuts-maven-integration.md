---
id: nutsMavenIntegration
title: Nuts Maven Integration
sidebar_label: Nuts Maven Integration
---
${{include($"${resources}/header.md")}}

## Nuts Maven Integration

* Seamless integration
* Maven Solver
* 
### Maven Descriptor pom.xml

* `nuts.executable=<true|false>` : when true the artifact is an executable (contains main class) 
* `nuts.application=<true|false>` : when true the artifact is an executable application (implements NutsApplication)
* `nuts.gui=<true|false>` : when true the requires a gui environment to execute
* `nuts.term=<true|false>` : when true the artifact is a command line executable
* `nuts.icons=<icon-path-string-array>` : an array (separated with ',' or new lines) of icon paths (url in the NutsPath format)
* `nuts.genericName=<genericNameString>` : a generic name for the application like 'Text Editor'
  * `nuts.categories=<categories-string-array>` : an array (separated with ',' or new lines) of categories. the categories should be compatible with Free Desktop Menu specification (https://specifications.freedesktop.org/menu-spec/menu-spec-1.0.html)



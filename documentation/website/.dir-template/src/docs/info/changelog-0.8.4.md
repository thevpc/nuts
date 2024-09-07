---
id: changelog
title: Change Log
sidebar_label: Change Log
order: 50
---
${{include($"${resources}/header.md")}}

View Official releases [here](https://github.com/thevpc/nuts/releases) :
Starred releases are most stable ones.

## nuts 0.8.4.0 (DEVELOPMENT VERSION)
- ```2024/04/25 	nuts 0.8.4.0``` not released yet nuts-runtime-0.8.4.0-rc1.jar
- WARNING: The OSS License has changed from APACHE2 to LGPL3
- WARNING : ```api```  API has evolved with incompatibilities with previous versions
- CHANGED: renamed nlib-ssh to next-ssh et is promoted as extension
- FIXED: Fixed maven url parsing on Windows platforms that prevented nsh from being installed
- CHANGED: Prefix is now 'N' instead of 'Nuts'
- ADDED: Add native installers for windows, linux and macOS
- ADDED: Add links to pdf docs in website and offline documentation
- ADDED: Add new nuts LOGO
- ADDED: Add NOptional a feature riche replacement of java's Optional
- ADDED: Add NEnvCondition/NEnvConditionBuilder::setProperties/getProperties
- ADDED: Add NDescriptor/NDescriptorBuilder::setLicenses
- ADDED: Add NDescriptor/NDescriptorBuilder::setDevelopers
- ADDED: Add    NConstants.IdProperties.DESKTOP_ENVIRONMENT -> NConstants.IdProperties.PROPERTIES
- ADDED: Add NPath::getLongBaseName // longest file name before last '.'
- ADDED: Add NMessageFormattable
- ADDED: Add NDescribable now takes a Session instead of NElements
- ADDED: Add cmdline arguments : --isolation,--confined, --sandbox
- ADDED: promote cmdline arguments : --init-platforms,--init-launchers,--init-scripts, --init-java
- ADDED: promote cmdline arguments : --desktop-launcher,--menu-launcher,--user-launcher
- ADDED: Add 30+ unit tests
- ADDED: NSession::isProgress
- ADDED: NCommandline now extends NBlankable
- ADDED: Added/Exposed Bean Reflection API : NReflectRepository,NReflectType,NReflectProperty
- ADDED: Added NChronometer, NDuration
- ADDED: Added Var Style (dollar based placeholder) in NMessage
- ADDED: added NProgressMonitor and monitoring api
- ADDED: NMemorySize and memory size api
- ADDED: --progress=log now prints progress to log instead of stderr
- CHANGED: Enum.parseLenient replaced by NOptional
- CHANGED: Rename NConstants.IdProperties.DESKTOP_ENVIRONMENT -> NConstants.IdProperties.DESKTOP
- CHANGED: Now NWorkspaceOptionsBuilder extends NWorkspaceOptions
- CHANGED: Rename NRepositoryDB::getRepositoryNameByURL -> NRepositoryDB::getRepositoryNameByLocation
- CHANGED: Rename NRepositoryDB::getRepositoryURLByName -> NRepositoryDB::getRepositoryLocationByName
- CHANGED: Change type to long in NExecCmd::getSleepMillis()/setSleepMillis(int sleepMillis);
- CHANGED: complete rewrite of NTF parser
- CHANGED: classes refactored to be repackaged in inner packages
- REMOVED: Remove session dependency from NVersion,NId and NDescriptor
- REMOVED: Remove NBootOptions
- REMOVED: removed CoreNumberUtils
- REMOVED: NMessageFormatted

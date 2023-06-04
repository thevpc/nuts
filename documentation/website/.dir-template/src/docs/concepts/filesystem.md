---
id: filesystem
title: File system
sidebar_label: File system
---

${{include($"${resources}/header.md")}}

**```nuts```** manages multiple workspaces. It has a default one located at ~/.config/nuts (~ is the user home directory). Each workspace handles a database and files related to the installed applications. The workspace has a specific layout to store different types of files relatives to your applications. **nuts** is largely inspired by [XDG Base Directory Specification](https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html) and hence defines several  store locations for each file type. Such organization of folders is called Layout and is dependent on the current operating system, the layout strategy and any custom configuration.

## Store Locations
Supported Store Locations are : 
**```nuts```** File System defines the following folders :
* **config** : defines the base directory relative to which application specific configuration files should be stored.
* **apps** : defines the base directory relative to which application executable binaries should be stored 
* **lib** : defines the base directory relative to which application non executable binaries should be stored 
* **var** : defines the base directory relative to which application specific data files (other than config) should be stored
* **log** : defines the base directory relative to which application specific log and trace files should be stored
* **temp** : defines the base directory relative to which application specific temporary files should be stored
* **cache** : defines the base directory relative to which application non-essential data and binary files should be stored to optimize bandwidth or performance
* **run** : defines the base directory relative to which application-specific non-essential runtime files and other file objects (such as sockets, named pipes, ...) should be stored

**```nuts```** defines such distinct folders (named Store Locations) for storing different types of application data according to your operating system.

On Windows Systems the default locations are :

        * apps     : "$HOME/AppData/Roaming/nuts/apps"
        * lib      : "$HOME/AppData/Roaming/nuts/lib"
        * config   : "$HOME/AppData/Roaming/nuts/config"
        * var      : "$HOME/AppData/Roaming/nuts/var"
        * log      : "$HOME/AppData/Roaming/nuts/log"
        * temp     : "$HOME/AppData/Local/nuts/temp"
        * cache    : "$HOME/AppData/Local/nuts/cache"
        * run      : "$HOME/AppData/Local/nuts/run"

On Linux, Unix, MacOS and any POSIX System the default locations are :

        * config   : "$HOME/.config/nuts"
        * apps     : "$HOME/.local/share/nuts/apps"
        * lib      : "$HOME/.local/share/nuts/lib"
        * var      : "$HOME/.local/share/nuts/var"
        * log      : "$HOME/.local/log/nuts"
        * cache    : "$HOME/.cache/nuts"
        * temp     : "$java.io.tmpdir/$username/nuts"
        * run      : "/run/user/$USER_ID/nuts"

As an example, the configuration folder for the artifact net.vpc.app:netbeans-launcher#1.2.4 in the default workspace in a Linux environment is

```
home/me/.config/nuts/default-workspace/config/id/net/vpc/app/netbeans-launcher/1.2.4/
```

And the log file "app.log" for the same artifact in the workspace named "personal" in a Windows environment is located at

```
C:/Users/me/AppData/Roaming/nuts/log/nuts/personal/config/id/net/vpc/app/netbeans-launcher/1.2.4/app.log
```

## Store Location Strategies
When you install any application using the **```nuts```** command a set of specific folders for the presented Store Locations are created. For that, 
two strategies exist : **Exploded strategy** (the default) and **Standalone strategy**.  

In **Exploded strategy**  **```nuts```** defines top level folders (in linux ~/.config for config Store Location etc), and then creates withing each top level Store Location a sub folder for the given application (or application version to be more specific). This helps putting all your config files in a SSD partition for instance and make **nuts** run faster. However if you are interested in the backup or roaming of your workspace, this may be not the best approach.

The **Standalone strategy**   is indeed provided mainly for Roaming workspaces that can be shared, copied, moved to other locations. A single root folder will contain all of the Store Locations.

As an example, in "Standalone Strategy", the configuration folder for the artifact net.vpc.app:netbeans-launcher#1.2.4 in the default workspace in a Linux environment is

```
home/me/.config/nuts/default-workspace/config/id/net/vpc/app/netbeans-launcher/1.2.4/
```

And the log file "app.log" for the same artifact in the workspace named "personal" in the same Linux environment is located at

```
/home/me/.config/nuts/default-workspace/log/id/net/vpc/app/netbeans-launcher/1.2.4/
```

You can see here that the following folder will contain ALL the data files of the workspace.

```
/home/me/.config/nuts/default-workspace
```

whereas in the **Exploded strategy** the Store Location are "exploded" into multiple root folders.

## Custom Store Locations
Of course, you are able to configure separately each Store Location to meet your needs.

### Selecting strategies
The following command will create an exploded workspace

```
nuts -w my-workspace --exploded
```

The following command will create a standalone workspace

```
nuts -w my-workspace --standalone
```

### Finer Customization
The following command will create an exploded workspace and moves all config files to the SSD partition folder /myssd/myconfig

```
nuts -w my-workspace --system-conf-home=/myssd/myconfig
```

You can type help for more details.

```
nuts help
```

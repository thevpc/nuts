---
id: filesystem
title: File system
sidebar_label: File system
---

**nuts** is capable of managing multiple independent workspaces on a single machine. The default workspace is located at `~/.config/nuts` (where `~` is the user's home directory). 

Each workspace manages an internal database and strict directory layouts for configurations, applications, and logs. To ensure standard behavior across environments, **nuts** implements file system strategies heavily inspired by the [XDG Base Directory Specification](https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html).

## Store Locations

The **nuts** file system divides workspace data into specific functional categories, known as **Store Locations**:

| Store Location | Description |
|---|---|
| **config** | Base directory for application-specific configuration files (XML, properties, YAML). |
| **apps** | Base directory for executable application binaries and native wrappers. |
| **lib** | Central cache for non-executable binaries (JARs, POMs, shared libraries). |
| **var** | Persistent application data files (embedded databases, indices). |
| **log** | Centralized directory for application and system trace/log files. |
| **temp** | Ephemeral application-specific temporary files. |
| **cache** | Download caches and non-essential binary data used to optimize network bandwidth. |
| **run** | Runtime files, sockets, and named pipes (often cleared on reboot). |

## Path Construction

Within these store locations, **nuts** structures files predictably using the artifact's Maven coordinates. Paths are constructed by expanding the `groupId` into directory segments, followed by the `artifactId` and `version`.

Format: `<Store-Location>/id/<group-id-path>/<artifact-id>/<version>/`

For example, the configuration folder for the artifact `net.thevpc.app:netbeans-launcher#1.2.4` is constructed as:
`<Config-Store>/id/net/vpc/app/netbeans-launcher/1.2.4/`

## Platform Default Paths

**nuts** adapts its Store Locations automatically based on the host operating system conventions.

### Linux, Unix, macOS, and POSIX
| Store  | Default Path                     |
|--------|----------------------------------|
| config | `$HOME/.config/nuts`             |
| bin      | `$HOME/.local/share/nuts/bin`    |
| lib    | `$HOME/.local/share/nuts/lib`    |
| var    | `$HOME/.local/share/nuts/var`    |
| log    | `$HOME/.local/log/nuts`          |
| cache  | `$HOME/.cache/nuts`              |
| temp   | `$java.io.tmpdir/$username/nuts` |
| run    | `/run/user/$USER_ID/nuts`        |

### Windows
| Store  | Default Path                                |
|--------|---------------------------------------------|
| bin    | `%USERPROFILE%\AppData\Roaming\nuts\bin`    |
| lib    | `%USERPROFILE%\AppData\Roaming\nuts\lib`    |
| config | `%USERPROFILE%\AppData\Roaming\nuts\config` |
| var    | `%USERPROFILE%\AppData\Roaming\nuts\var`    |
| log    | `%USERPROFILE%\AppData\Roaming\nuts\log`    |
| temp   | `%USERPROFILE%\AppData\Local\nuts\temp`     |
| cache  | `%USERPROFILE%\AppData\Local\nuts\cache`    |
| run    | `%USERPROFILE%\AppData\Local\nuts\run`      |

## Visual Directory Tree

A standard Exploded workspace on a Linux machine roughly visualizes as:

```text
~ (Home Directory)
├── .config/
│   └── nuts/
│       └── default-workspace/
│           └── config/id/net/vpc/app/... (Configurations)
├── .local/
│   ├── share/nuts/bin/ws/default-workspace/id/...       (Binaries & Launchers)
│   ├── share/nuts/lib/ws/default-workspace/id/...       (JARs and dependencies)
│   ├── share/nuts/var/ws/default-workspace/id/...       (Application Data)
│   └── log/nuts/ws/default-workspace/id/...             (Application Logs)
└── .cache/nuts/ws/default-workspace/
    └──id/...  (Downloads & Indexes)
    └──repos/... (Repos Downloads & Indexes)
```

## Store Location Strategies

When you initialize a workspace, you can define how these Store Locations are mapped to the file system using two strategies:

### 1. Exploded Strategy (Default)
In the Exploded strategy, **nuts** scatters the top-level folders across the host system according to XDG/AppData specifications (as shown above). 

*Advantage:* This optimizes performance by aligning with OS expectations. For example, `.cache` can be excluded from cloud backups, and `.local/share` can reside on a high-speed SSD partition.

### 2. Standalone Strategy
In the Standalone strategy, the entire workspace is contained within a single root folder. 

*Advantage:* Ideal for portability. You can create a roaming workspace on a USB thumb drive or easily mount it as a Docker volume. 

Example path for a log file in a Standalone workspace:
`/home/me/.config/nuts/default-workspace/log/id/net/vpc/app/netbeans-launcher/1.2.4/app.log`

Notice that the `log` folder resides *inside* the `default-workspace` directory, rather than being mapped to `.local/log`.

## Custom Store Locations

You can override the default layouts to meet specialized infrastructure requirements.

**Selecting Strategies:**
```bash
# Create an exploded workspace (Default)
nuts -w my-workspace --exploded

# Create a standalone workspace
nuts -w my-workspace --standalone
```

**Finer Customization:**
You can individually remap specific stores. For instance, to keep an exploded workspace but map configurations to a dedicated mounted SSD:

```bash
nuts -w my-workspace --system-conf-home=/mnt/fast-ssd/configs
```

To see all available store customization arguments, consult the built-in help:
```bash
nuts help
```

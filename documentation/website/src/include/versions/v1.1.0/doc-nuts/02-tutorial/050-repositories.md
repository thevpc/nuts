---
title: Repositories
---

:::tip What You'll Learn
In this section, you will learn how **nuts** resolves and fetches artifacts. We will cover:
* What repositories are and the default configuration.
* How to add and remove repositories.
* Supported repository types (Local, HTTP/HTTPS, SSH).
* How repository search priority works.
* Using proxy repositories and authenticated private repositories.
:::

## What are Repositories?

Repositories are the source locations where **nuts** looks for artifacts (JAR files, POM descriptors, and other dependencies) when you attempt to install or run an application. Because **nuts** uses standard Maven POM descriptors, any Maven repository can act as a **nuts** repository.

By default, every **nuts** workspace is pre-configured with two standard repositories:

- 1. **Local Maven Repository**: Your local `~/.m2/repository` directory (if it exists).
- 2. **Maven Central**: The public global Maven repository.

This means that out of the box, any Java application or library published to Maven Central is immediately executable via **nuts**.

## Listing Repositories

To see the repositories currently configured in your workspace, use the `settings list repos` command:

```bash
nuts settings list repos
```

This will output a list showing the internal name of each repository, its URL or path, and its active status.

## Adding a Repository

If your team uses a private Nexus, Artifactory, or a custom remote repository, you can add it to your workspace. Use the `settings add repo` command, providing a unique name and the repository's URL:

```bash
# Add a public remote repository
nuts settings add repo my-company-repo https://repo.mycompany.com/maven2/

# Add a local directory as a repository
nuts settings add repo my-local-repo /path/to/local/repo/
```

Once added, **nuts** will immediately begin querying this new repository whenever you search for, install, or run an artifact.

## Removing a Repository

To remove a repository from your workspace configuration, use its assigned name with the `remove` command:

```bash
nuts settings remove repo my-company-repo
```

## Repository Types

**nuts** supports several repository protocols to accommodate different network environments and deployment strategies:

* **Local Folders**: Standard file paths on your local machine (e.g., `/var/lib/maven/repo`). Ideal for offline development or testing locally built artifacts.
* **HTTP/HTTPS**: Standard web-based Maven repositories (e.g., Nexus, Artifactory, Maven Central). This is the most common format.
* **SSH Remote Paths**: You can configure **nuts** to fetch artifacts securely over SSH. This is highly useful for small teams or private deployments that do not want to manage a full Nexus server.

## Repository Priority

When you request an artifact, **nuts** does not broadcast the request to all repositories simultaneously. Instead, it searches them in a strict order of priority.

- 1. **Local Workspace Cache**: First, it checks if the artifact is already cached in the current workspace.
- 2. **Local Repositories**: Next, it checks local directory repositories (like `~/.m2/repository`).
- 3. **Remote Repositories**: Finally, it queries remote HTTP/HTTPS/SSH repositories in the order they were added to the workspace.

This priority system ensures that local builds are always preferred over remote downloads, saving bandwidth and speeding up execution.

## Proxy Repositories

**nuts** natively caches downloaded artifacts in your workspace. When it downloads an artifact from a remote HTTP repository, that artifact is stored locally. This effectively makes the workspace act as a proxy cache. If you run the command again, or if another application depends on the same library, **nuts** will serve it from the local cache rather than re-downloading it from the remote repository.

## Private and Authenticated Repositories

Many corporate environments secure their repositories behind authentication. **nuts** supports credential management for accessing these private repositories. 

While the deep dive into the security model is covered in the Security documentation, you can securely configure credentials for a repository so that **nuts** can authenticate seamlessly:

```bash
nuts settings add repo private-nexus https://nexus.corp.com/repository/maven-releases/
nuts security add credentials --repo private-nexus --username myuser --password mytoken
```

Once configured, **nuts** will automatically inject these credentials whenever it communicates with `private-nexus`.

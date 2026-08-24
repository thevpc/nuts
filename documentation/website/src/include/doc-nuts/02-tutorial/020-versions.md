---
title: Versions
---

## Multiple Artifact version Installation
One of the key features of **```nuts```** is the ability to install multiple versions of the same application.
We can for instance type :
```bash
  nuts install netbeans-launcher#1.2.2
  # then
  nuts install netbeans-launcher#1.2.0
```
Now we have two versions installed, the last one always is considered default one.
you can run either, using it's version
```bash
  nuts netbeans-launcher#1.2.2 &
  # or
  nuts netbeans-launcher#1.2.0 &
```
Actually, when you have many versions installed for the same artifact and you try to run it without specifying the version, the last one installed will be considered. To be more precise, an artifact has a default version when It's installed. This default version is considered when no explicit version is typed.
In our example, when we type
```
  nuts netbeans-launcher &
```
the 1.2.0 version will be invoked because the artifact is already installed and the default version points to the last one installed. So if you want to switch back to version 1.2.2 you just have to re-install it. Don't worry, no file will be downloaded again, nuts will detect that the version is not marked as default and will switch it to.


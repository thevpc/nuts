---
id: NPath
title: NPath
sidebar_label: NPath
---



**nuts** introduces a concept very similar to java's URL but with better extension builtin mechanisms and helper methods : ```NPath```

supported formats/protocols are:

* file format   ```/path/to/to/resource``` or ``` c:\path\to\resource```
* file URL ``` file:/path/to/to/resource``` or ``` file:c:/path/to/resource```
* http/https URLs (or any other Java supported URL) ```http://some-url``` or ```https://some-url```
* classpath ``` classpath:/path/to/to/resource``` (you must provide the used classpath upon creation)
* resource Path ``` resource://groupId1:artifactId1#version1;groupId2:artifactId2#version2/path/to/resource``` or ```resource://(groupId1:artifactId1#version1;groupId2:artifactId2#version2)/path/to/resource``` in that case the resource is looked up in any of the artifact classpaths (including dependencies)  


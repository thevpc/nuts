---
title: Format Elements
---


## Format element

```java

// Parse JSON into Java object
Person person = ...;

// Format and write XML to file
NElementFormat.ofPlainXml(document).println(NPath.of("person.xml"));

// Print TSON to terminal with colors
NElementFormat.ofNtfTson(document).println();
```

```java

// Parse JSON into Java object
Person person = ...;

// Print TSON to terminal with colors
        NElementFormat.ofNtfTson(person).println(NPath.of("/some/path/file.tson"));
```

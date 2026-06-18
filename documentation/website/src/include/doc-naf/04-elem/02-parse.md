---
title: Parse Elements
---

use `ofJson`, `ofTson`, `ofYaml`, and `ofXml` to parse into `NElement`

```java

// Parse JSON into Java object
NElement personJson = NElementReader.ofJson().read(NPath.of("person.json"));
NElement personXml = NElementReader.ofXml().read(NPath.of("person.xml"));

```

You can parse into a Java class as well :

```java

// Parse JSON into Java object
Person person = NElementReader.ofJson().read(NPath.of("person.json"), Person.class);

// Format and write XML to file
NElementWriter.ofPlainXml(document).println(NPath.of("person.xml"));

// Print TSON to terminal with colors
        NElementFormat.ofNtfTson(document).println();
```

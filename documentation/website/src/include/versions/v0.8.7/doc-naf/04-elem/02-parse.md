---
title: Parse Elements
---

use `ofJson`, `ofTson`, `ofYaml`, and `ofXml` to parse into `NElement`

```java

// Parse JSON into Java object
NElement personJson = NElementParser.ofJson().parse(NPath.of("person.json"));
NElement personXml = NElementParser.ofXml().parse(NPath.of("person.xml"));

```

You can parse into a Java class as well :

```java

// Parse JSON into Java object
Person person = NElementParser.ofJson().parse(NPath.of("person.json"), Person.class);

// Format and write XML to file
NElementFormat.ofPlainXml(document).println(NPath.of("person.xml"));

// Print TSON to terminal with colors
        NElementFormat.ofNtfTson(document).println();
```

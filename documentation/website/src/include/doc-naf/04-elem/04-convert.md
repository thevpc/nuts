---
title: Convert Elements
---

`NElement` is an excellent way to convert between text formats (`json` to `tson` etc.)

```java

// Parse JSON into Element
NElement personJson = NElementReader.ofJson().parse(NPath.of("person.json"));

// Format and write XML to file
NElementWriter.ofPlainXml(personJson).println(NPath.of("person.xml"));
```


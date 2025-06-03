---
title: NMsg : Messages and text formatting
---


**nuts** Library allows multiple variants of string interpolation


```java
    NSession session=NSession.of();
    session.setConfirm(NConfirmationMode.ASK);
    session.setOutputFormat(NContentType.XML);

    NOut.println("Hello");
    NOut.printlnf("Hello");
    NOut.println(Arrays.asList("Hello"));
    NOut.printlnf("Hello %s","world");
    NOut.println(NMsg.ofC("Hello %s","world"));
    NOut.println(NMsg.ofJ("Hello {0}","world"));
    NOut.println(NMsg.ofV("Hello $v",NMaps.of("v","world"));
```



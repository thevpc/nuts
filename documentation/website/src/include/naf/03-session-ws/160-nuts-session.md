---
title: NSession
---


**nuts** session holds current configuration and options

To get the current session instance :
```java
    NSession session=NSession.of();
```


To run with a different session instance :
```java
    NSession session=NSession.of();
    session=session.copy().setConfirm(NConfirmationMode.ASK);
    session.runWith(()->{
        // here run with new options
    });
```

Here another example
```java
    // share allows NSession/NWorkspace to be accessible globally as a singleton
    Nuts.require();
    // then you can get the current session anywhere in your code
    NSession session=NSession.of();
    session.setConfirm(NConfirmationMode.ASK);
    session.setOutputFormat(NContentType.XML);

    NOut.println("Hello");
    NOut.println("Hello");

    NOut.println(Arrays.asList("Hello"));
    NOut.println("Hello %s","world");
    NOut.println(NMsg.ofC("Hello %s","world"));
    NOut.println(NMsg.ofJ("Hello {0}","world"));
    NOut.println(NMsg.ofV("Hello $v",NMaps.of("v","world"));
```

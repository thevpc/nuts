---
title: Create Elements
---



## Create a structured element

```java
// Build an element
NElement document = NElement.ofObjectBuilder()
    .set("app-id", NApp.of().getId().get())
    .set("error", messageString)
    .build();

```


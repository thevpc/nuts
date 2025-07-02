---
title: NIn : simplified input
---


**nuts** Library suppors colorful and structured output


```java
    Nuts.require();
NSession session=NSession.of();
    NOut.println("Hello");
    NOut.println("Hello");

    NOut.println(Arrays.asList("Hello"));
        NOut.println("Hello");

    session.err()....;
    session.in()....;
```


```java
    NOut.println("#Hello1# ##Hello2## ##:_:Hello3## ");
    NOut.println("```java public static class MyClass {}```");
    NOut.println("```js public static class MyClass {}```");
    NOut.println("```xml <a>hello</a>```");
    NOut.println("```json {a:'hello'}```");
```

```java
    NErr.println("#Hello1# ##Hello2## ##:_:Hello3## ");
    NErr.println("```java public static class MyClass {}```");
    NErr.println("```js public static class MyClass {}```");
    NErr.println("```xml <a>hello</a>```");
    NErr.println("```json {a:'hello'}```");
```

```java
    NSession session=...;
    class Customer{String id;String name;}
    Customer customer1,customer2,customer3; ...
    //
    session.setOutputFormat(NContentType.JSON).out().println(Arrays.asList(customer1,customer2,customer3))
    session.setOutputFormat(NContentType.TREE).out().println(Arrays.asList(customer1,customer2,customer3))
    session.setOutputFormat(NContentType.PLAIN).out().println(Arrays.asList(customer1,customer2,customer3))
    session.setOutputFormat(NContentType.XML).out().println(Arrays.asList(customer1,customer2,customer3))
    session.setOutputFormat(NContentType.PROPS).out().println(Arrays.asList(customer1,customer2,customer3))
    NOut.println(Arrays.asList(customer1,customer2,customer3))
```



## Working with Tables

```java
    NSession session=...;
    Object a,b,c,d; ...
    NMutableTableModel m = NMutableTableModel.of();
    m.newRow().addCells(a,b,c,d);
    NOut.println(m);
```

## Working with Trees

```java
    NSession session=...;
    Object a,b,c,d; ...
    NMutableTableModel m = NMutableTableModel.of();
    m.newRow().addCells(a,b,c,d);
    NOut.printlnf(m);
```

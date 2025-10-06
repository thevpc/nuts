---
id: NOptional
title: NOptional
sidebar_label: NOptional
---

NOptional is a powerful alternative to Java’s Optional that goes beyond being just a nullable container. It’s designed to make null handling more expressive, 
composable, and safer — closer to the expressive capabilities of TypeScript or Kotlin.

In addition to the standard of(), get(), and orElse() methods, it introduces:

- Named values for better error messages (ofNamed()).
- Optional chaining (`then(...)`) to safely traverse object graphs.
- Nullish coalescing (`orElse()`), similar to ??.
- Assertion-style accessors (`get()` vs `orNull()`).

These features make it ideal for writing expressive, `null-safe` code in complex object hierarchies — without endless `if (x != null)` checks.

## Non Null Assertion and Coalescing

Java has a builtin null Check mechanism but it does not enable customized messages or exceptions.
Optional are described as per Java's (c) Documentation "A container object which may or may not contain a non-null value".
NOptional is more of an Object Wrapper than addes several useful null related operators like '??' '?.' and '!' in typescript. 


| Concept                   |                    Java                    |                       NAF (NOptional)                       |        Equivalent TS         |
|:--------------------------|:------------------------------------------:|:-----------------------------------------------------------:|:----------------------------:|
| Null check with exception |   ```if (user == null) throw new ...```    |         ```NOptional.ofNamed(user,"user").get()```          |         ```user!```          |
| Null-safe call            | ```if (user != null) user.toUpperCase()``` | ```NOptional.of(user).map(String::toUpperCase).orNull()```  |  ```user?.toUpperCase()```   |
| user?.toUpperCase()       |   ```user != null ? user : "default"```    |         ```NOptional.of(user).orElse("default")```          |   ```user ?? "default"```    |


## Optional Chaining 

One of the most useful features of NOptional is the ability to traverse deep object graphs safely without repetitive null checks.

With `then(...)`, you can chain field access or method calls, and NOptional automatically short-circuits the chain if any part is null.

<Tabs
defaultValue="NAF"
values={[
{ label: 'NAF', value: 'NAF', },
{ label: 'Java', value: 'Java', },
{ label: 'Typescript', value: 'typescript', }
]
}>

<TabItem value="java">

```java
    Number roadNumber=(app!=null && app.person!=null && app.person.road!=null)? app.person.address.road.number:null;
```

</TabItem>

<TabItem value="NAF">

```java 
    // expected var roadNumber=app?.person?.address?.road?.number;
    Number roadNumber=NOptional.of(app).then(v->v.person).then(v->v.road).then(v->v.number).orNull();
```

</TabItem>

</Tabs>


## Combining Optional Chaining

<Tabs
defaultValue="NAF"
values={[
{ label: 'NAF', value: 'NAF', },
{ label: 'Java', value: 'Java', },
{ label: 'Typescript', value: 'typescript', }
]
}>

<TabItem value="java">

```java
    Address address=(app!=null && app.person!=null)?app.person.address:null;
    if(address==null){
       throw new IllegalArgumentException("missing address"); 
    }
    Number roadNumber=(address!=null 
        && address.road!=null)
        ? address.road.number:0;
```

</TabItem>

<TabItem value="NAF">

```java 
    // expected : var roadNumber=app?.person?.address!.road?.number??0;
    Number roadNumber=NOptional.of(app).then(v->v.person).then(v->v.address).get().then(v->v.road).then(v->v.number).orElse(0);
```

</TabItem>

</Tabs>

## When to use get(), orNull(), and orElse()

- `get()` – returns the value or throws an exception if absent (assertive).
- `orNull()` – returns null if absent (passive).
- `orElse(value)` – returns a fallback if absent (coalescing).

## Why not just use Optional

While Java’s built-in Optional is useful, it lacks several features needed for expressive, null-safe code in complex applications:

- ❌ No support for named values or custom exception messages.
- ❌ No built-in chaining (Optional chaining is verbose with flatMap).
- ❌ No nullish coalescing equivalent.
- ❌ No describe() or integration with structured diagnostics.

`NOptional` solves all of these while remaining compatible with functional idioms.

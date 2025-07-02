---
title: NIn for simplified Input
---

# `NIn` — Structured Input Utility

The `NIn` class is the **interactive input** utility of the **Nuts** platform. It provides a simple and consistent interface to read from the **standard input stream** (`NSession.in()`), with built-in support for prompts, password masking, and type-safe values.

## Basic Input Reading

### Reading a Line

```java
String line = NIn.readLine();
```

Reads a full line from the user input.

You can also provide a prompt using an NMsg:

```java
String name = NIn.readLine(NMsg.ofPlain("Enter your ##name##: "));
```


### Reading a Password

```java
char[] pwd = NIn.readPassword();
```

Reads a password without echoing characters  (as far as the required extensions are loaded) to the terminal.

With prompt:

```java
char[] pwd = NIn.readPassword(NMsg.ofPlain("Password: "));
```

## Reading a Literal

```java
NLiteral lit = NIn.readLiteral();
```

Reads a string input and wraps it in an `NLiteral`, allowing you to safely extract typed values.

```java
NLiteral lit = NIn.readLiteral(NMsg.ofPlain("Enter a number: "));
int value = lit.asInt().get();
```
use the `NLiteral::asXYZ` to convert the string input to common types like `double`, `boolean`, etc...

```java
NLiteral lit = NIn.readLiteral(NMsg.ofPlain("Enter a number: "));
double value = lit.asDouble().get();
```

## Interactive and Typed Input with NAsk<T>
For complex or type-safe input, NIn.ask() (or NAsk.of()) provides a fluent API to build interactive prompts with support for:

- Custom messages,
- Typed inputs (String, int, boolean, enum, etc.),
- Default values,
- Validators,
- Accepted values,
- Password input,
- "Remember me" options,
- Custom parsing and formatting.

It will re-prompt indefinitely until a valid input is provided, based on type and validation constraints, or until the user cancels the prompt (e.g., by sending an interrupt like Ctrl+C or entering an empty value when allowed).

NAsk will re-prompt indefinitely in an interactive loop until:
- A valid value is provided (based on expected type and validation),
- Or the user explicitly cancels the prompt (e.g., by interrupting input or when input is blank and optional).
This ensures reliable and robust user interaction with clear guidance and fallback behavior.

### Password Input Example

```java
char[] password = NAsk.of()
.forPassword(NMsg.ofPlain("Password for user " + user))
.getValue();
```

Prompts for a password securely (input not echoed), and returns a char[].

### Boolean Confirmation with Context

```java
boolean usePcp = NAsk.of()
.forBoolean(
NMsg.ofPlain(
remote
? "Use PCP users the same as the instances hosts users?"
: "Use PCP user as the same as the current user?"
)
)
.getValue();
```

Prompts the user with a yes/no question.


### "Remember Me" with Default


```java
boolean override = NAsk.of()
    .setDefaultValue(true)
    .setRememberMeKey(
        rememberMeKey == null ? null : ("Override." + rememberMeKey)
    )
    .forBoolean(
        NMsg.ofC("Override %s?",
            NText.ofStyled(
                betterPath(out.toString()),
                NTextStyle.path()
            )
        )
    )
    .getBooleanValue();
```

This example:

- Proposes a default answer (true),
- Persists the answer under the given key (rememberMeKey), so the question may be skipped next time,
- Uses styled output in the question message.


### Custom Validation Example

```java
String mainClass = NAsk.of()
    .forString(NMsg.ofNtf("Enter the name or index:"))
    .setValidator((value, question) -> {
        Integer index = NLiteral.of(value).asInt().orNull();
        if (index != null && index >= 1 && index <= possibleClasses.size()) {
            return possibleClasses.get(index - 1);
        }
        if (possibleClasses.contains(value)) {
            return value;
        }
        throw new NValidationException(); // Triggers re-prompt
    })
    .getValue();
```
### NAsk Supported NAsk Features

- `forString(...)`	Prompt for a String
- `forInt(...)`	Prompt for an int
- `forDouble(...)`	Prompt for a double
- `forBoolean(...)`	Prompt for a boolean (yes/no, true/false)
- `forEnum(Class<E> enumType, ...)`	Prompt for an enum value
- `forPassword(...)`	Prompt for a password (char[])
- `setDefaultValue(T)`	Sets a default value used when input is blank
- `setHintMessage(NMsg)`	Displays hint under the question
- `setAcceptedValues(List<Object>)`	Restricts accepted values and may display suggestions
- `setRememberMeKey(String)`	Automatically stores and reuses the answer based on a key
- `setValidator(NAskValidator<T>)`	Adds input validation logic
- `setParser(NAskParser<T>)`	Custom parsing from String to T
- `setFormat(NAskFormat<T>)`	Custom formatting of expected values for user display

### NAsk Re-prompting Behavior

NAsk will loop until a valid answer is provided, according to:
- Type expectations (e.g., integer, enum),
- Custom validators (if any),
- Accepted values (if defined).

This ensures robust, user-friendly interaction without premature failure.

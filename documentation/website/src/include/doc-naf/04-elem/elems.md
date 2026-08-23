---
title: NElement API Documentation
---



## 1. Introduction

`NElement` is the foundational agnostic object model for structured data in the Nuts Application Framework (NAF). It is designed to act as a universal pivot format for transforming data between JSON, XML, YAML, TSON, and other structured representations.

While it supports multiple formats, it is primarily grounded in TSON (Typed JSON), which is inherently a superset of JSON, YAML, and XML. Because of this rich foundation, NElement offers two massive advantages:

- Universal Pivot: Parse from one format, manipulate in a unified model, and serialize to another.
- True Roundtrip Fidelity: The TSON parser is a true roundtrip parser. It can read a configuration file, allow you to update specific values programmatically, and write it back without dropping comments, punctuation, spaces, or original formatting.

### Core Design Principles

- *Immutable Construction*: Strongly prefers Builder patterns (`NObjectElementBuilder`, `NArrayElementBuilder`) to construct elements safely and predictably.
- *Roundtrip Awareness*: Captures and preserves `NElementComment`, `NElementLine`, and `NNewLineMode` metadata during parsing.
- *Fail-Never-Again* Navigation: Uses is*() checks and NOptional returning as*() methods to prevent ClassCastException. Errors and warnings are attached directly to the tree as `NElementDiagnostic` rather than halting execution.

## 2. The Roundtrip Capability (Preserving Formatting)

One of the most powerful features of NElement is its ability to update configuration files while acting as a "good citizen"—leaving the developer's comments, spacing, and layout completely intact.

The secret behind NElement's ability to preserve formatting lies in its Affix System. Every element can hold NBoundAffix objects anchored to specific NAffixAnchor positions (e.g., START, PRE_1, POST_1, SEP_1, END).

Affixes include:
- `NElementSpace` / `NElementNewLine`: Preserves exact whitespace and line breaks.
- `NElementSeparator`: Preserves commas, semicolons, etc.
- `NElementComment`: Preserves block (/* */) and line (//) comments.
- `NElementAnnotation`: Preserves custom metadata (e.g., @deprecated).

### Example: Updating a Config File Without Losing Comments

Imagine a config.tson (or .json) file:

```tson
{
  // Database connection settings
  host: "localhost", // Default to local
  port: 8080,
  
  /* Feature flags */
  features: {
    darkMode: true
  }
}
```
#### Step 1: Parse with Roundtrip Preservation

```java
// The TSON reader captures comments, whitespace, and structure into the NElement tree
NElement config = NElementReader.ofTson().read(NPath.of("config.tson"));
```

#### Step 2: Safely Navigate and Modify

```java
// Safely find and update the port, attaching a diagnostic if something is wrong
NElement updatedConfig = config.transformOptional(new NElementTransform() {
    @Override
    public List<NElement> preTransform(NElementTransformContext context) {
        NElement current = context.element();
        
        // Look for the 'port' field
        if (current.isNamedPair("port")) {
            // Safely update the value to 9090
            return Collections.singletonList(
                NElement.ofPair("port", NElement.ofInt(9090))
            );
        }
        
        // Return unmodified element for everything else (preserving comments/structure)
        return Collections.singletonList(current);
    }
}).orElse(config);
```

#### Step 3: Write Back with Fidelity

```java
// The writer reconstructs the file, keeping the comments and original layout
String updatedTson = NElementWriter.ofTson()
    .formatter(NElementFormatter.ofPretty()) // Respects original spacing where possible
    .formatPlain(updatedConfig);

// Output will still contain "// Database connection settings" and "/* Feature flags */"
System.out.println(updatedTson);
```
>> Note: The underlying tree retains NElementComment and NElementLine nodes attached to their respective parents, ensuring the roundtrip is lossless regarding human-readable metadata.

## 3. Expression Handling & Reshaping (NFlatExprElement)

`NElement` does not wire operator precedence rules by default. When parsing expressions, it initially creates an `NFlatExprElement` (a flat list of operands and operators). It is up to the developer to choose how to resolve precedence.

This is done via the `NExprElementReshaper`, which transforms a flat expression into a structured `NOperatorElement` tree.

```java
NFlatExprElement flatExpr = /* parsed flat expression: "a + b * c" */;

// Choose a reshaping strategy
NElement structuredTree = flatExpr.reshape(NExprElementReshaperType.JAVA); 
// Applies standard Java precedence (* before +)

// Available Reshaper Types:
// - DEFAULT: Standard fallback precedence.
// - JAVA: Standard Java/C-like precedence.
// - LEFT_ASSOCIATIVE: Evaluates strictly left-to-right.
// - LOGICAL: Prioritizes logical operators (AND, OR).
// - EMPTY: Returns the flat structure as-is.
```

>> Note: The API supports a massive NOperatorSymbol enum, including standard math (+, -, *, /), logical (&&, ||), arrows (->, =>), and advanced mathematical Unicode symbols (∫, ∑, ∈, ⊆), complete with lexeme aliases.

## 4. NElement as a Pivot Format

Because `NElement` abstracts the underlying syntax, you can use it to translate between formats. While `TSON`/`JSON` roundtrip is fully supported today, the model is designed to accommodate `XML` and `YAML` as they are integrated.

```java
// 1. Parse from JSON
String jsonInput = "{\"name\": \"app\", \"version\": 1}";
NElement pivot = NElementReader.ofJson().read(jsonInput);

// 2. Manipulate using the agnostic NElement API
NElement enhanced = NElement.ofObjectBuilder()
    .addAll(pivot.asObject().get().entries()) // Copy existing
    .set("environment", NElement.ofString("production"))
    .build();

// 3. Serialize to TSON (or future XML/YAML writers)
String tsonOutput = NElementWriter.ofTson().formatPlain(enhanced);
```


## 4. Creating Elements (The Builder Pattern)

To maintain immutability and avoid the pitfalls of mutable state, always prefer Builder static factory methods.

```java
// Primitives
NElement str = NElement.ofString("Hello World");
NElement num = NElement.ofInt(42, NNumberLayout.DECIMAL, "ms"); // With layout/suffix
NElement bool = NElement.ofTrue();

// Complex Structures (Builder Pattern)
NElement complexStructure = NElement.ofArrayBuilder()
    .add(
        NElement.ofObjectBuilder()
            .set("name", NElement.ofString("service-a"))
            .set("active", NElement.ofTrue())
            .set("endpoints", NElement.ofArrayBuilder()
                .add(NElement.ofString("http://localhost:8080"))
                .add(NElement.ofString("http://localhost:8081"))
                .build())
            .build()
    )
    .build(); // Returns an immutable NElement
```

## 5. Safe Inspection and Extraction

Avoid `ClassCastException` entirely by using the `is*()` and `as*()` method pairs. The `as*()` methods return an `NOptional`, enabling functional, fail-safe data extraction.

```java
NElement elem = /* ... parsed element ... */;

// 1. Type Checking
if (elem.isObject() && elem.isNamedObject("database")) {
    // ...
}

// 2. Safe Casting and Extraction (Functional Style)
NOptional<String> hostOpt = elem.asObject()
    .flatMap(obj -> obj.get("host"))      // Get the 'host' pair
    .flatMap(NElement::asStringValue);    // Extract the string value

if (hostOpt.isPresent()) {
    System.out.println("Connecting to: " + hostOpt.get());
} else {
    // Fail-never-again: handle the absence gracefully
}

// 3. Direct Typed Extraction
NOptional<LocalDate> date = elem.asLocalDateValue();
NOptional<Boolean> flag = elem.asBooleanValue();
```

## 6. Serialization & Object "Destruction"

When converting arbitrary Java objects into NElement, the framework follows a strict, predictable fallback chain known as "Destruction":

- Explicit Serializer: If an `NElementSerializer` is registered for the class/interface in the NElementMapperStore, it is used.
- `NToElement` Interface: If the object implements `NToElement`, its toElement() method is called.
- Recursive Destruction: For any other object, the framework uses reflection to navigate its fields/getters, recursively building an NObjectElement or NArrayElement.
- Undestructable Types: The recursion stops when it hits a "simple" or "atomic" type (e.g., `String`, `Number`, `Boolean`, `Instant`, `Path`).
- `NCustomElement` Fallback: If a type is explicitly marked as undestructable (or cannot be destructed), it is wrapped in an `NCustomElement`.

### The Power of NCustomElement
You can attach any Java object directly into the NElement tree as an NCustomElement. This allows for a powerful mixture of structured, serializable data and rich, domain-specific Java objects that should not be flattened.

```java
// Prevent a specific rich type from being destructed into a plain String/Map
NElements elements = NElements.of();
elements.mapperStore()
    .removeAllSimpleTypesFilters()
    .addSimpleTypesFilter(c -> MyRichDomainObject.class.isAssignableFrom(c));

Map<String, Object> data = Map.of("id", 1, "payload", new MyRichDomainObject());
NElement tree = elements.toElement(data); 
// 'payload' is now safely stored as an NCustomElement, preserving its identity.
```

## 7. Deserialization & Contextual Mapping
Deserializers are highly flexible and can be registered in the NElementMapperStore based on multiple contextual dimensions. The framework will automatically select the most specific match:

```java
NElementMapperStore ms = parser.mapperStore();

// 1. By Java Type
ms.setDeserializer(MyConfig.class, myCustomDeserializer);

// 2. By Element Type (e.g., force all OBJECTs to deserialize a certain way)
ms.setDeserializer(NElementType.OBJECT, MyConfig.class, myObjectDeserializer);

// 3. By Named Element (e.g., only when the key/name is "database")
ms.setDeserializer(NElementType.OBJECT, "database", NNameSelectorStrategy.CASE_INSENSITIVE, MyConfig.class, myDbDeserializer);
```

You can register custom deserializers for specific Java classes to handle complex parsing logic, such as accumulating repeated fields into an array or applying lenient parsing rules.

```java

NElementMapperStore ms = parser.mapperStore();
ms.setDeserializer(NElementType.OBJECT, MyConfig.class, 
    ms.deserializerBuilderOf(MyConfig.class)
        .configureLenient()
        .booleanDefaultTrue()
        .onUnsupportedChild(context -> {
            // Custom logic to handle unexpected child elements gracefully
            MyConfig instance = context.instance();
            // ... accumulate or transform ...
            return true; // indicate handled, preventing a parse failure
        }).build()
);

```

## 8. Diagnostics and Error Handling

Aligning with robust error-handling design, `NElement` supports attaching diagnostics directly to the tree. This allows the system to distinguish between recoverable warnings and non-recoverable errors (like an `NErrorElement`) without throwing exceptions that tear down the parsing process.

```java
// Check for non-recoverable structural errors
if (elem.isErrorTree()) {
    List<NElementDiagnostic> fatalIssues = elem.treeDiagnostics();
    log.error("Element tree contains fatal errors: {}", fatalIssues);
    // Handle gracefully, perhaps by falling back to defaults
} else {
    // Process normally, but still check for warnings
    List<NElementDiagnostic> warnings = elem.diagnostics();
    if (!warnings.isEmpty()) {
        log.warn("Configuration has warnings: {}", warnings);
    }
}
```

## 9. Core Element Type Hierarchy
All elements implement NElement. They are categorized into specialized interfaces for type-safe operations:

| Interface                                     | Description                                                                                                          |
|:----------------------------------------------|:---------------------------------------------------------------------------------------------------------------------|
| `NPrimitiveElement`                           | Atomic values (String, Int, Boolean, Instant, etc.). Provides value().                                               |
| `NObjectElement`                              | Key-value container. Extends NNamedElement, NListContainerElement, and NParametrizedContainerElement.                |
| `NArrayElement`                               | Ordered list of elements. Supports named arrays and parameterized arrays (e.g., func(arg1, arg2)[item1, item2]).     |
| `NFragmentElement`                            | A generic, flexible container. Provides extensive path-based querying (getByPath, getIntValueByPath, etc.).          |
| `NListElement`                                | Represents ordered/unordered lists with depth, markers, and marker variants (ideal for Markdown-like structures).    |
| `NTupleElement`                               | Positional parameter container, optionally named.                                                                    |
| `NFlatExprElement`                            | A flat sequence of operands and operators, awaiting reshaping via NExprElementReshaper.                              |
| `NOperatorElement`                            | A structured expression node with NOperatorPosition, operands, and NOperatorSymbols.                                 |
| `NCustomElement`                              | Wraps an arbitrary Java Object that should not be destructed.                                                        |
| `NBinaryStreamElement` / `NCharStreamElement` | Represents lazy or large data payloads via NInputStreamProvider / NReaderProvider.                                   |
| `NEmptyElement`                               | Represents an explicitly empty state.                                                                                |

## 10. Formatting Styles (NElementFormatterStyle)
The formatter defines a layered hierarchy of rules, moving from raw data preservation to total structural reconstruction:

| Style      | Intervention                                                                                                                                           | Use Case                                                      |
|:-----------|:-------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------|
| `VERBATIM` | Low. Writes exactly what is stored in affixes. Only injects whitespace for Fatal collisions (where tokens would merge).                                | Maintaining Git history or manual formatting in config files. |
| `STABLE`   | Medium-Low. Fixes fatal collisions + injects spacing for Unpretty collisions (e.g., between quotes and identifiers).                                   | Standard programmatic serialization and logging.              |
| `SIMPLE`   | Medium. Builds on STABLE, injects missing structural separators (commas), and strips Root Garbage (parent separators).                                 | CLI output, displaying individual property values, UI labels. |
| `COMPACT`  | High. Strips all optional whitespace and non-essential separators. Reapplies only absolute minimum Fatal disambiguation.                               | Network transmission or high-density data storage.            |
| `PRETTY`   | Total. Ignores existing affixes. Performs structural reconstruction with consistent indentation, column alignment, and complexity-based line wrapping. | Generating documentation, example files, auto-formatting.     |
| `CUSTOM`   | Variable. Reserved for user-defined formatting logic via NElementFormatterAction.                                                                      | Specialized domain-specific rendering.                        |

## 11. Tree Traversal & Safe Navigation

Avoid ClassCastException by using the is*() and as*() method pairs, which return NOptional.

```java
NElement elem = /* ... */;

// Safe, functional extraction
NOptional<String> host = elem.asObject()
    .flatMap(obj -> obj.get("network"))
    .flatMap(obj -> obj.get("host"))
    .flatMap(NElement::asStringValue);

// Path-based querying (via NFragmentElement / NListContainerElement)
NOptional<Integer> port = elem.getIntValueByPath("network", "port");
```

## 12. Custom Traversal with NElementVisitor

```java
elem.traverse(new NElementVisitor() {
    @Override
    public NTreeVisitResult enter(NElement element) {
        if (element.isErrorTree()) {
            return NTreeVisitResult.TERMINATE; // Stop traversal on non-recoverable error
        }
        return NTreeVisitResult.CONTINUE;
    }

    @Override
    public NTreeVisitResult visitAnnotation(NElementAnnotation annotation) {
        // Annotations are not NElements, handled separately
        return NTreeVisitResult.CONTINUE;
    }

    @Override
    public void exit(NElement element) {
        // Post-order processing
    }
});
```

## 13. Readers & Writers (Multi-Format Support)

`NElementReader` and `NElementWriter` provide a unified API for multiple content types, with optional `NTF` (Nuts Text Format) support for enriched terminal output.

```java
// Reading
NElement fromJson = NElementReader.ofJson().read("{\"a\": 1}");
NElement fromYaml = NElementReader.ofYaml().read(Path.of("config.yml"));
NElement fromTson = NElementReader.ofTson().read("a: 1 # with comment");

// Writing
String compactJson = NElementWriter.ofPlainJson().compact(true).formatPlain(elem);
String prettyYaml = NElementWriter.ofYaml().formatter(NElementFormatter.ofPretty()).formatPlain(elem);

// NTF (Rich Terminal) Output
NElementWriter.ofNtfTson().format(iterable, NPrintStream.of(System.out));
```


## 14. Best Practices Summary

- Treat NElement as the Pivot: Use it as the central, format-agnostic representation when translating between JSON, TSON, XML, or YAML.
- Leverage Roundtrip Parsing: Rely on NElementReader.ofTson() and NElementFormatter.ofVerbatim() to preserve NAffix metadata (comments, spacing) when patching configuration files.
- Reshape Expressions Explicitly: Remember that NFlatExprElement has no inherent precedence. Always call .reshape(NExprElementReshaperType.JAVA) (or another strategy) before evaluating.
- Prefer Builders: Always use NElement.ofObjectBuilder(), NElement.ofArrayBuilder(), etc., terminating with .build().
- Use NOptional: Rely on as*Value() and as*() methods for fail-safe navigation.
- Mix Structured and Custom Data: Don't be afraid to use NCustomElement to embed rich Java objects directly into the tree, preventing destructive reflection mapping.
- Attach, Don't Throw: Use NElementDiagnostic and isErrorTree() to handle parsing anomalies gracefully without tearing down the application.


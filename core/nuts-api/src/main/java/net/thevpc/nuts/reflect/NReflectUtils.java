package net.thevpc.nuts.reflect;

import net.thevpc.nuts.log.NLogUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class NReflectUtils {
    private static final Map<Class<?>, Object> DEFAULTS_CACHE = new ConcurrentHashMap<>();
    private static final Comparator<Class> CLASS_HIERARCHY_COMPARATOR = new Comparator<Class>() {
        @Override
        public int compare(Class o1, Class o2) {
            if (o1.isAssignableFrom(o2)) {
                return 1;
            } else if (o2.isAssignableFrom(o1)) {
                return -1;
            }
            if (o1.isInterface() && !o2.isInterface()) {
                return 1;
            }
            if (o2.isInterface() && !o1.isInterface()) {
                return -1;
            }
            return 0;
        }
    };


    private NReflectUtils() {
    }


    /**
     * Resolves the default value for a type annotated with {@code @DefaultsTo}.
     * <p>
     * This method checks for default values in the following order:
     * <ol>
     *   <li><strong>Primitive defaults</strong> — Java primitive types return their language defaults
     *       (e.g., 0 for int, false for boolean, 0.0 for double)</li>
     *   <li><strong>Cache</strong> — previously resolved defaults to avoid repeated reflection</li>
     *   <li><strong>@DefaultsTo annotation</strong> — if present on the type, resolves the specified member</li>
     * </ol>
     * <p>
     * <strong>Resolution strategy for @DefaultsTo:</strong>
     * <p>
     * For enum types, treats the annotation value as an enum constant name.
     * For other types, attempts to resolve in order:
     * <ul>
     *   <li>Public static field with the given name</li>
     *   <li>Public static no-arg method with the given name</li>
     * </ul>
     * <p>
     * <strong>Caching:</strong> Results (including {@code null}) are cached to avoid repeated reflection.
     * This includes cases where no annotation is present. Cached nulls represent "no annotation found",
     * not resolution failures.
     * <p>
     * <strong>Error handling:</strong>
     * <ul>
     *   <li>If {@code @DefaultsTo} annotation is present but the specified member cannot be found
     *       or has the wrong signature, a {@link NoSuchElementException} is thrown.</li>
     *   <li>If member invocation/field access throws an exception, it is wrapped in a
     *       {@link NoSuchElementException} with context.</li>
     * </ul>
     * <p>
     * <strong>Examples:</strong>
     * <pre>
     *   {@code @DefaultsTo("UNKNOWN")}
     *   enum Status { ACTIVE, INACTIVE, UNKNOWN }
     *   Status s = getDefaultValue(Status.class);  // returns Status.UNKNOWN
     *
     *   {@code @DefaultsTo("DEFAULT_INSTANCE")}
     *   class Config {
     *       public static final Config DEFAULT_INSTANCE = new Config();
     *   }
     *   Config c = getDefaultValue(Config.class);  // returns Config.DEFAULT_INSTANCE
     *
     *   {@code @DefaultsTo("create")}
     *   class Factory {
     *       public static Factory create() { return new Factory(); }
     *   }
     *   Factory f = getDefaultValue(Factory.class);  // returns result of Factory.create()
     * </pre>
     *
     * @param <T> the type to resolve the default for
     * @param type the class to check for a default value; may be null
     * @return the resolved default value, or a Java primitive default if no annotation present,
     *         or {@code null} if type is null or no {@code @DefaultsTo} annotation is found
     * @throws NoSuchElementException if {@code @DefaultsTo} annotation is present but:
     *         <ul>
     *           <li>For enum: the specified constant name does not exist</li>
     *           <li>For class: the specified field or method is not found</li>
     *           <li>For method: it has parameters (only no-arg methods are supported)</li>
     *           <li>For field/method: invocation or access throws an exception</li>
     *         </ul>
     * @see NDefaultsTo
     * @see #getJavaDefaultValue(Class)
     */
    @SuppressWarnings("unchecked")
    public static <T> T getDefaultValue(Class<T> type) {
        if (type == null) return null;
        Object d = getJavaDefaultValue(type);
        if (d != null) {
            return (T) d;
        }

        // Check cache first
        if (DEFAULTS_CACHE.containsKey(type)) {
            return (T) DEFAULTS_CACHE.get(type);
        }

        NDefaultsTo annotation = type.getAnnotation(NDefaultsTo.class);
        if (annotation != null) {
            String valueName = annotation.value();
            Object resolved = null;
            try {
                // Enum constant
                if (type.isEnum()) {
                    resolved = Enum.valueOf((Class<Enum>) type.asSubclass(Enum.class), valueName);
                } else {
                    // Static field
                    try {
                        java.lang.reflect.Field field = type.getField(valueName);
                        if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                            resolved = field.get(null);
                        }
                    } catch (NoSuchFieldException ignored) {
                    }

                    // Static method
                    if (resolved == null) {
                        try {
                            java.lang.reflect.Method method = type.getMethod(valueName);
                            if (java.lang.reflect.Modifier.isStatic(method.getModifiers()) && method.getParameterCount()==0) {
                                resolved = method.invoke(null);
                            }
                        } catch (NoSuchMethodException ignored) {
                        }
                    }
                }
            } catch (Exception e) {
                throw NExceptions.ofSafeNoSuchElementException(NMsg.ofC("Failed to resolve @DefaultsTo('%') for %s : %s", valueName, type.getName(), e).asError(e));
            }
            if (resolved == null) {
                throw NExceptions.ofSafeNoSuchElementException(NMsg.ofC("Missing value as @DefaultsTo('%') for %s", valueName, type.getName()));
            }
            // Cache result (even nulls)
            DEFAULTS_CACHE.put(type, resolved);
            return (T) resolved;
        }

        // Cache null if no annotation
        DEFAULTS_CACHE.put(type, null);
        return null;
    }

    /**
     * Validates whether a string is a valid identifier for use in code or configuration.
     * <p>
     * An identifier is considered valid if:
     * <ul>
     *   <li>Non-null and non-empty</li>
     *   <li>First character is a valid Java identifier start character (letter, underscore, or dollar sign),
     *       or present in {@code extraWordChars} if provided</li>
     *   <li>Subsequent characters are valid Java identifier parts (letters, digits, underscores, or dollar signs),
     *       or present in {@code extraWordChars}</li>
     *   <li>Does not start or end with underscore, hyphen, or dot</li>
     * </ul>
     * <p>
     * <strong>Extra characters:</strong> The {@code extraWordChars} parameter allows additional characters
     * to be considered valid at any position (e.g., hyphens for kebab-case identifiers in configuration files).
     * If {@code extraWordChars} is null or doesn't contain a character, that character is rejected unless
     * it's a valid Java identifier character.
     * <p>
     * <strong>Examples:</strong>
     * <pre>
     *   isValidIdentifier("myVariable", null)           // true
     *   isValidIdentifier("_myVariable", null)          // false (starts with underscore)
     *   isValidIdentifier("my-variable", "-")           // true (hyphen in extraWordChars)
     *   isValidIdentifier("my.property.", ".")          // false (ends with dot)
     *   isValidIdentifier("2ndVariable", null)          // false (starts with digit)
     * </pre>
     *
     * @param anyType the string to validate as an identifier; may be null
     * @param extraWordChars additional characters to permit in the identifier, or null
     *                       for strict Java identifier rules only
     * @return {@code true} if the string is a valid identifier, {@code false} otherwise
     */
    public static boolean isValidIdentifier(String anyType, String extraWordChars) {
        if (anyType == null) {
            return false;
        }
        char[] chars = anyType.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (i == 0) {
                if (c == '_' || c == '-' || c == '.') {
                    return false;
                }
                if (!Character.isJavaIdentifierStart(c)) {
                    if ((extraWordChars == null || extraWordChars.indexOf(c) < 0)) {
                        return false;
                    }
                }
            }
            if (!Character.isJavaIdentifierPart(c)) {
                if ((extraWordChars == null || extraWordChars.indexOf(c) < 0)) {
                    return false;
                }
            }
            if (i == chars.length - 1) {
                if (c == '_' || c == '-' || c == '.') {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determines whether a class represents a Java primitive type or its boxed (wrapper) equivalent.
     * <p>
     * Primitive types checked: {@code boolean}, {@code byte}, {@code short}, {@code int},
     * {@code long}, {@code char}, {@code float}, {@code double}, and {@code void}.
     * <p>
     * Boxed types checked: {@code Boolean}, {@code Byte}, {@code Short}, {@code Integer},
     * {@code Long}, {@code Character}, {@code Float}, {@code Double}, and {@code Void}.
     *
     * @param anyType the class to check; may be null
     * @return {@code true} if the type is a primitive or boxed type, {@code false} otherwise
     * @see #isPrimitiveOrBoxed(Class, boolean)
     */
    public static boolean isPrimitiveOrBoxed(Class<?> anyType) {
        return isPrimitiveOrBoxed(anyType, true);
    }

    /**
     * Determines whether a class represents a Java primitive type or its boxed (wrapper) equivalent.
     * <p>
     * Primitive types checked: {@code boolean}, {@code byte}, {@code short}, {@code int},
     * {@code long}, {@code char}, {@code float}, {@code double}, and optionally {@code void}.
     * <p>
     * Boxed types checked: {@code Boolean}, {@code Byte}, {@code Short}, {@code Integer},
     * {@code Long}, {@code Character}, {@code Float}, {@code Double}, and optionally {@code Void}.
     *
     * @param anyType the class to check; may be null
     * @param includeVoid if {@code true}, {@code void} and {@code Void.class} are considered primitive/boxed;
     *                    if {@code false}, they are not
     * @return {@code true} if the type is a primitive or boxed type, {@code false} otherwise
     */
    public static boolean isPrimitiveOrBoxed(Class<?> anyType, boolean includeVoid) {
        if (anyType == null) {
            return false;
        }
        switch (anyType.getName()) {
            case "boolean":
            case "byte":
            case "short":
            case "int":
            case "long":
            case "char":
            case "float":
            case "double":
            case "java.lang.Boolean":
            case "java.lang.Byte":
            case "java.lang.Short":
            case "java.lang.Integer":
            case "java.lang.Long":
            case "java.lang.Character":
            case "java.lang.Float":
            case "java.lang.Double":
                return true;
            case "void":
            case "java.lang.Void":
                return includeVoid;
        }
        return false;
    }

    /**
     * Returns the default value for a Java primitive type.
     * <p>
     * For primitive types, returns the language-defined default:
     * <ul>
     *   <li>{@code boolean} → {@code false}</li>
     *   <li>{@code byte}, {@code short}, {@code int}, {@code long} → {@code 0}</li>
     *   <li>{@code char} → {@code '\0'} (null character)</li>
     *   <li>{@code float}, {@code double} → {@code 0.0}</li>
     *   <li>{@code void} → {@code null}</li>
     * </ul>
     * <p>
     * For non-primitive types, returns {@code null}.
     * <p>
     * <strong>Note:</strong> This method returns defaults for primitive types only, not for boxed wrapper types.
     * To check if a type is primitive or boxed, use {@link #isPrimitiveOrBoxed(Class)}.
     *
     * @param anyType the class to get the default value for; may not be null
     * @return the default value for the type, or {@code null} if the type is not primitive
     * @throws NullPointerException if {@code anyType} is null
     */
    public static Object getJavaDefaultValue(Class<?> anyType) {
        NAssert.requireNamedNonNull(anyType, "type");
        switch (anyType.getName()) {
            case "boolean":
                return false;
            case "byte":
                return (byte) 0;
            case "short":
                return (short) 0;
            case "int":
                return 0;
            case "long":
                return 0L;
            case "char":
                return '\0';
            case "float":
                return 0.0f;
            case "double":
                return 0.0;
            case "void":
                return null;
        }
        return null;
    }

    /**
     * Converts a primitive type to its boxed (wrapper) class equivalent.
     * <p>
     * Supported conversions:
     * <pre>
     *   boolean   → Boolean.class
     *   byte      → Byte.class
     *   short     → Short.class
     *   int       → Integer.class
     *   long      → Long.class
     *   char      → Character.class
     *   float     → Float.class
     *   double    → Double.class
     *   void      → Void.class
     * </pre>
     * <p>
     * If the input is not a primitive type (e.g., already boxed or a custom class),
     * an error is returned.
     * <p>
     * <strong>Example:</strong>
     * <pre>
     *   toBoxedType(int.class).getOptional()     // returns Integer.class
     *   toBoxedType(Integer.class).error()       // error: "not a primitive type..."
     *   toBoxedType(null).error()                // error: "no boxed type for null"
     * </pre>
     *
     * @param anyType the primitive class to box; may be null
     * @return an {@code NOptional} containing the boxed class if input is a primitive,
     *         or an error if input is null, already boxed, or not a recognized type
     * @see #toPrimitiveType(Class)
     */
    public static NOptional<Class<?>> toBoxedType(Class<?> anyType) {
        if (anyType == null) {
            return NOptional.ofNamedError("no boxed type for null");
        }
        switch (anyType.getName()) {
            case "boolean":
                return NOptional.of(Boolean.class);
            case "byte":
                return NOptional.of(Byte.class);
            case "short":
                return NOptional.of(Short.class);
            case "int":
                return NOptional.of(Integer.class);
            case "long":
                return NOptional.of(Long.class);
            case "char":
                return NOptional.of(Character.class);
            case "float":
                return NOptional.of(Float.class);
            case "double":
                return NOptional.of(Double.class);
            case "void":
                return NOptional.of(Void.class);
        }
        return NOptional.ofNamedError(NMsg.ofC("not a primitive type %s", anyType));
    }

    /**
     * Converts a boxed (wrapper) class to its primitive type equivalent.
     * <p>
     * Supported conversions:
     * <pre>
     *   Boolean.class   → boolean
     *   Byte.class      → byte
     *   Short.class     → short
     *   Integer.class   → int
     *   Long.class      → long
     *   Character.class → char
     *   Float.class     → float
     *   Double.class    → double
     *   Void.class      → void
     * </pre>
     * <p>
     * If the input is not a boxed type (e.g., already primitive or a custom class),
     * an error is returned.
     * <p>
     * <strong>Example:</strong>
     * <pre>
     *   toPrimitiveType(Integer.class).getOptional()  // returns int.class (Integer.TYPE)
     *   toPrimitiveType(int.class).error()            // error: "not a primitive type..." (already primitive)
     *   toPrimitiveType(null).error()                 // error: "no boxed type for null"
     * </pre>
     *
     * @param anyType the boxed class to unbox; may be null
     * @return an {@code NOptional} containing the primitive class if input is a boxed type,
     *         or an error if input is null, already primitive, or not a recognized boxed type
     * @see #toBoxedType(Class)
     */
    public static NOptional<Class<?>> toPrimitiveType(Class<?> anyType) {
        if (anyType == null) {
            return NOptional.ofNamedError("no primitive type for null");
        }
        switch (anyType.getName()) {
            case "java.lang.Boolean":
                return NOptional.of(Boolean.TYPE);
            case "java.lang.Byte":
                return NOptional.of(Byte.TYPE);
            case "java.lang.Short":
                return NOptional.of(Short.TYPE);
            case "java.lang.Integer":
                return NOptional.of(Integer.TYPE);
            case "java.lang.Long":
                return NOptional.of(Long.TYPE);
            case "java.lang.Character":
                return NOptional.of(Character.TYPE);
            case "java.lang.Float":
                return NOptional.of(Float.TYPE);
            case "java.lang.Double":
                return NOptional.of(Double.TYPE);
            case "java.lang.Void":
                return NOptional.of(Void.TYPE);
        }
        return NOptional.ofNamedError(NMsg.ofC("not a primitive type %s", anyType));
    }

    /**
     * Finds all superclasses and interfaces in the type hierarchy of a given class.
     * <p>
     * This method performs a breadth-first traversal of the class hierarchy, collecting:
     * <ul>
     *   <li>The input class itself</li>
     *   <li>All superclasses (excluding {@code Object.class})</li>
     *   <li>All interfaces directly and indirectly implemented</li>
     * </ul>
     * <p>
     * <strong>Traversal order:</strong> Results are returned in breadth-first order, meaning
     * direct superclasses and interfaces appear before transitive ones. The exact order within
     * each level is determined by insertion order (LinkedHashSet).
     * <p>
     * <strong>Special case:</strong> {@code Object.class} is excluded from the result.
     * If the input is {@code null}, it is silently skipped.
     * <p>
     * <strong>Example:</strong>
     * <pre>
     *   class C extends B implements I1, I2 { }
     *   class B implements I3 { }
     *
     *   findAllSuperClassesAndInterfaces(C.class)
     *   // Returns: [C, B, I1, I2, I3]
     * </pre>
     *
     * @param clazz the class to analyze; may be null
     * @return a set of all superclasses and interfaces in the hierarchy,
     *         excluding {@code Object.class}; empty set if input is null
     * @see #findClassHierarchy(Class, Class, NTypeNameDomain)
     */
    public static Set<Class<?>> findAllSuperClassesAndInterfaces(Class<?> clazz) {
        if(clazz==null){
            return  Collections.emptySet();
        }
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        Set<Class<?>> nextLevel = new LinkedHashSet<Class<?>>();
        nextLevel.add(clazz);
        while (!nextLevel.isEmpty()) {
            classes.addAll(nextLevel);
            Set<Class<?>> thisLevel = new LinkedHashSet<Class<?>>(nextLevel);
            nextLevel.clear();
            for (Class<?> each : thisLevel) {
                Class<?> superClass = each.getSuperclass();
                if (superClass != null && superClass != Object.class) {
                    nextLevel.add(superClass);
                }
                Collections.addAll(nextLevel, each.getInterfaces());
            }
        }
        return classes;
    }

    /**
     * Finds the common ancestor of multiple classes.
     * <p>
     * Returns a single class from the set of all common ancestors.
     * If multiple common ancestors exist, the choice is arbitrary and implementation-dependent.
     * Use {@link #commonAncestors(Class[])} to retrieve all common ancestors.
     * <p>
     * <strong>Special cases:</strong>
     * <ul>
     *   <li>If input is null or empty, returns {@code Object.class}</li>
     *   <li>If any class is null, it is skipped</li>
     *   <li>If no common ancestor exists besides {@code Object.class}, returns {@code Object.class}</li>
     * </ul>
     * <p>
     * <strong>Example:</strong>
     * <pre>
     *   commonAncestor(ArrayList.class, LinkedList.class)  // returns List or Collection or Iterable or Object
     *   commonAncestor(String.class, StringBuilder.class)  // returns Object
     * </pre>
     *
     * @param classes the classes to find a common ancestor for; may be null or contain nulls
     * @return a class that is a superclass or interface of all inputs,
     *         or {@code Object.class} as a fallback
     * @throws IndexOutOfBoundsException if no common ancestors are found and the list is empty
     * @see #commonAncestors(Class[])
     */
    public static Class<?> commonAncestor(Class<?>... classes) {
        return commonAncestors(classes).get(0);
    }

    /**
     * Finds all common ancestors (superclasses and interfaces) of multiple classes.
     * <p>
     * Returns the intersection of the type hierarchies of all input classes.
     * If multiple classes share the same superclass or interface, it appears only once.
     * <p>
     * <strong>Special cases:</strong>
     * <ul>
     *   <li>If input is null or empty, returns a list containing only {@code Object.class}</li>
     *   <li>If any class is null, it is skipped</li>
     *   <li>If no common ancestors exist, returns a list containing only {@code Object.class}</li>
     *   <li>If only one class is provided, returns its entire type hierarchy</li>
     * </ul>
     * <p>
     * <strong>Example:</strong>
     * <pre>
     *   class A extends Base { }
     *   class B extends Base implements I { }
     *
     *   commonAncestors(A.class, B.class)
     *   // Returns: [Base, Object] (in some order)
     * </pre>
     *
     * @param classes the classes to find common ancestors for; may be null or contain nulls
     * @return a list of all classes that are superclasses or interfaces of all inputs;
     *         at minimum contains {@code Object.class}
     * @see #commonAncestor(Class[])
     * @see #findAllSuperClassesAndInterfaces(Class)
     */
    public static List<Class<?>> commonAncestors(Class<?>... classes) {
        if (classes == null || classes.length == 0) {
            ArrayList<Class<?>> a = new ArrayList<>(1);
            a.add(Object.class);
            return a;
        }
        Set<Class<?>> rollingIntersect = null;
        for (int i = 0; i < classes.length; i++) {
            if (classes[i] != null) {
                if (rollingIntersect == null) {
                    rollingIntersect = new LinkedHashSet<>(findAllSuperClassesAndInterfaces(classes[i]));
                } else {
                    rollingIntersect.retainAll(findAllSuperClassesAndInterfaces(classes[i]));
                }
            }
        }
        if (rollingIntersect == null) {
            ArrayList<Class<?>> a = new ArrayList<>(1);
            a.add(Object.class);
            return a;
        }
        return new ArrayList<>(rollingIntersect);
    }


    /**
     * Loads and lists all available service implementations for a type.
     * <p>
     * Uses Java's {@link ServiceLoader} mechanism to discover implementations of the given service type.
     * Multiple class loaders are searched to maximize coverage (context class loader, sources' loaders, etc.).
     * Duplicate implementations (by fully qualified class name) are filtered out.
     * <p>
     * This is a convenience wrapper around {@link #loadServices(Class, Consumer, Class[])}.
     *
     * @param <T> the service type
     * @param type the service interface or class to load implementations for; may not be null
     * @param sources additional classes whose class loaders to search, or null
     * @return a list of all discovered service instances; empty if none found
     * @throws NullPointerException if {@code type} is null
     * @see #loadServices(Class, Consumer, Class[])
     * @see ServiceLoader#load(Class)
     */
    public static <T> List<T> listServices(Class<T> type, Class<?>... sources) {
        List<T> instances = new ArrayList<>();
        loadServices(type, e -> instances.add(e), sources);
        return instances;
    }

    /**
     * Discovers and loads service implementations using Java's {@link ServiceLoader}.
     * <p>
     * This method searches for service implementations via META-INF/services files
     * using multiple class loaders for maximum coverage:
     * <ul>
     *   <li>Class loaders from {@code sources} parameters</li>
     *   <li>The service type's own class loader</li>
     *   <li>The current thread's context class loader</li>
     *   <li>The class loader of {@code NReflectUtils} (this library)</li>
     * </ul>
     * <p>
     * <strong>Duplicate prevention:</strong> If the same implementation class is discovered
     * multiple times (e.g., via different class loaders), the consumer is called only once per unique class name.
     * <p>
     * <strong>Error handling:</strong> If loading fails for any class loader, a warning is logged
     * and the search continues with other loaders. No exception is thrown.
     * <p>
     * <strong>Example:</strong>
     * <pre>
     *   loadServices(DataSourceFactory.class, factory -> {
     *       System.out.println("Found: " + factory.getClass().getName());
     *   }, DatabaseDriver.class);
     * </pre>
     *
     * @param <T> the service type
     * @param type the service interface or class to load implementations for; may not be null
     * @param consumer callback invoked for each discovered service instance; may not be null
     * @param sources additional classes whose class loaders to search, or null/empty
     * @throws NullPointerException if {@code type} or {@code consumer} is null
     * @see #listServices(Class, Class[])
     * @see ServiceLoader#load(Class, ClassLoader)
     */
    public static <T> void loadServices(Class<T> type, Consumer<T> consumer, Class<?>... sources) {
        NAssert.requireNamedNonNull(type, "serviceType");
        NAssert.requireNamedNonNull(consumer, "consumer");
        LinkedHashSet<Class<?>> uniqueClasses = new LinkedHashSet<>();
        LinkedHashSet<ClassLoader> uniqueClassLoaders = new LinkedHashSet<>();
        Set<String> implementedClasses = new HashSet<>(); // To prevent duplicates
        if (sources != null) {
            for (Class<?> c : sources) {
                if (c != null) {
                    if (uniqueClasses.add(c)) {
                        uniqueClassLoaders.add(c.getClassLoader());
                    }
                }
            }
        }

        if (uniqueClasses.add(type)) {
            uniqueClassLoaders.add(type.getClassLoader());
        }
        uniqueClassLoaders.add(Thread.currentThread().getContextClassLoader());

        uniqueClassLoaders.add(NReflectUtils.class.getClassLoader());
        ClassLoader[] loaders = uniqueClassLoaders.toArray(new ClassLoader[0]);
        for (ClassLoader loader : loaders) {
            if (loader == null) continue;
            try {
                ServiceLoader<T> sl = ServiceLoader.load(type, loader);
                for (T lib : sl) {
                    if (implementedClasses.add(lib.getClass().getName())) {
                        consumer.accept(lib);
                    }
                }
            } catch (Exception e) {
                NLogUtils.safeLog(NMsg.ofC("error loading service %s: %S", type, e).asError(e), type);
            }
        }
    }


    /**
     * Finds the class hierarchy (type ancestor chain) for a class, optionally filtered by base type.
     * <p>
     * Performs a breadth-first search of the class hierarchy (superclasses and interfaces),
     * collecting all classes that are assignable from a given base type (if specified).
     * Results are sorted by a hierarchy comparator for consistent, predictable ordering.
     * <p>
     * <strong>Filtering:</strong>
     * <ul>
     *   <li>If {@code baseType} is null, all superclasses and interfaces are included</li>
     *   <li>If {@code baseType} is provided, only classes assignable from {@code baseType} are included</li>
     *   <li>The {@code domain} parameter is ignored in this overload (for compatibility)</li>
     * </ul>
     * <p>
     * <strong>Special cases:</strong>
     * <ul>
     *   <li>{@code Object.class} is included in the result</li>
     *   <li>Duplicate classes are eliminated during traversal</li>
     * </ul>
     * <p>
     * <strong>Example:</strong>
     * <pre>
     *   findClassHierarchy(ArrayList.class, Collection.class, null)
     *   // Returns: [ArrayList, AbstractList, List, AbstractCollection, Collection, Iterable, ...]
     * </pre>
     *
     * @param clazz the class to analyze; may be null
     * @param baseType optional filter—only ancestors assignable from this type are included, or null
     * @param domain unused in this overload (kept for API compatibility)
     * @return an array of classes in the hierarchy, sorted by hierarchy comparator;
     *         empty array if input is null
     * @see #findClassHierarchy(NTypeName, NTypeName, NTypeNameDomain)
     */
    public static List<Class> findClassHierarchy(Class clazz, Class baseType, NTypeNameDomain domain) {
        if(clazz==null || baseType==null){
            return  Collections.emptyList();
        }
        HashSet<Class> seen = new HashSet<Class>();
        Queue<Class> queue = new LinkedList<Class>();
        List<Class> result = new LinkedList<Class>();
        queue.add(clazz);
        while (!queue.isEmpty()) {
            Class i = queue.remove();
            if (baseType == null || baseType.isAssignableFrom(i)) {
                if (!seen.contains(i)) {
                    seen.add(i);
                    result.add(i);
                    if (i.getSuperclass() != null) {
                        queue.add(i.getSuperclass());
                    }
                    Collections.addAll(queue, i.getInterfaces());
                }
            }
        }
        Collections.sort(result, CLASS_HIERARCHY_COMPARATOR);
        return result;
    }

    /**
     * Finds the type hierarchy (type ancestor chain) for a {@link NTypeName}, optionally filtered by base type.
     * <p>
     * This is the {@link NTypeName}-based equivalent of {@link #findClassHierarchy(Class, Class, NTypeNameDomain)}.
     * Performs a breadth-first search of the type hierarchy (supertypes and interfaces),
     * collecting all types that are assignable from a given base type (if specified).
     * Results are sorted by a hierarchy comparator for consistent, predictable ordering.
     * <p>
     * <strong>Filtering:</strong>
     * <ul>
     *   <li>If {@code baseType} is null, all supertypes and interfaces are included</li>
     *   <li>If {@code baseType} is provided, only types assignable from {@code baseType} (via domain) are included</li>
     * </ul>
     * <p>
     * <strong>Domain dependence:</strong> Type relationships and hierarchy navigation are delegated
     * to the provided {@code NTypeNameDomain}, which defines assignability and type resolution rules.
     * <p>
     * <strong>Example:</strong>
     * <pre>
     *   NTypeName arrayListType = new NTypeName<>(ArrayList.class.getName());
     *   NTypeName[] hierarchy = findClassHierarchy(arrayListType, null, domain);
     *   // Results depend on domain's type resolution and hierarchy rules
     * </pre>
     *
     * @param clazz the type name to analyze; may be null
     * @param baseType optional filter—only ancestors assignable from this type are included, or null
     * @param domain the type name domain defining assignability and hierarchy rules; may not be null
     * @return an array of type names in the hierarchy, sorted by hierarchy comparator;
     *         empty array if input is null
     * @see #findClassHierarchy(Class, Class, NTypeNameDomain)
     */
    public static List<NTypeName> findClassHierarchy(NTypeName clazz, NTypeName baseType, NTypeNameDomain domain) {
        HashSet<NTypeName> seen = new HashSet<NTypeName>();
        Queue<NTypeName> queue = new LinkedList<NTypeName>();
        List<NTypeName> result = new LinkedList<NTypeName>();
        queue.add(clazz);
        while (!queue.isEmpty()) {
            NTypeName i = queue.remove();
            if (baseType == null || domain.isAssignableFrom(baseType, i)) {
                if (!seen.contains(i)) {
                    seen.add(i);
                    result.add(i);
                    NTypeName s = domain.getSuperType(i);
                    if (s != null) {
                        queue.add(s);
                    }
                    NTypeName[] ii = domain.getInterfaces(i);
                    Collections.addAll(queue, ii);
                }
            }
        }
        Collections.sort(result, new NTypeNameHierarchyComparator(domain));
        return result;
    }

    /**
     * Finds the lowest common ancestor (most specific common type) of two type names.
     * <p>
     * This method finds the deepest type in the hierarchy that both input types inherit from or implement.
     * If the types are unrelated (no common ancestor besides Object), returns Object.
     * <p>
     * <strong>Algorithm:</strong>
     * <ol>
     *   <li>If types are equal, returns the type itself</li>
     *   <li>If one type is assignable from the other, returns the more specific type</li>
     *   <li>Otherwise, computes full hierarchies and finds their deepest intersection</li>
     * </ol>
     * <p>
     * <strong>Special cases:</strong>
     * <ul>
     *   <li>If either type is null, behavior depends on domain implementation</li>
     *   <li>If no common ancestor exists, returns Object (as {@code NTypeName})</li>
     * </ul>
     * <p>
     * <strong>Example:</strong>
     * <pre>
     *   interface I { }
     *   class A implements I { }
     *   class B implements I { }
     *
     *   lowestCommonAncestor(A_TYPE, B_TYPE, domain)
     *   // Returns: I (their most specific common ancestor)
     * </pre>
     *
     * @param <A> type parameter for first type name
     * @param <B> type parameter for second type name
     * @param a the first type name; may be null
     * @param b the second type name; may be null
     * @param domain the type name domain defining assignability and hierarchy rules; may not be null
     * @return the most specific type name that both inputs inherit from or implement,
     *         or Object if no better common ancestor exists
     * @see #findClassHierarchy(NTypeName, NTypeName, NTypeNameDomain)
     */
    public static <A, B> NTypeName<?> lowestCommonAncestor(NTypeName<A> a, NTypeName<B> b, NTypeNameDomain domain) {
        if (a.equals(b)) {
            return a;
        }
        if (domain.isAssignableFrom(a, b)) {
            return a;
        }
        if (domain.isAssignableFrom(b, a)) {
            return b;
        }
        List<NTypeName> aHierarchy = findClassHierarchy(a, null, domain);
        List<NTypeName> bHierarchy = findClassHierarchy(b, null, domain);
        int i1 = -1;
        int i2 = -1;
        for (int ii = 0; ii < aHierarchy.size(); ii++) {
            for (int jj = 0; jj < bHierarchy.size(); jj++) {
                if (aHierarchy.get(ii).equals(bHierarchy.get(jj))) {
                    if (i1 < 0 || ii + jj < i1 + i2) {
                        i1 = ii;
                        i2 = jj;
                    }
                }
            }
        }
        if (i1 < 0) {
            return new NTypeName<>(Object.class.getName());
        }
        return aHierarchy.get(i1);
    }

    /**
     * Determines whether a class is a proxy type created by common Java proxy frameworks.
     * <p>
     * This method detects proxies created by:
     * <ul>
     *   <li><strong>JDK Dynamic Proxies</strong> — created via {@link java.lang.reflect.Proxy#newProxyInstance}</li>
     *   <li><strong>Hibernate Proxies</strong> — LazyInitializer proxies for ORM entity lazy-loading</li>
     *   <li><strong>Spring CGLIB Proxies</strong> — method-level AOP proxies via {@code @Transactional}, {@code @Cacheable}, etc.</li>
     *   <li><strong>Plain CGLIB Proxies</strong> — byte-code enhanced subclasses via Enhancer</li>
     * </ul>
     * <p>
     * Detection strategy:
     * <ol>
     *   <li>First checks if the class is a JDK dynamic proxy using {@link java.lang.reflect.Proxy#isProxyClass}</li>
     *   <li>Then inspects the simple class name for known proxy markers (e.g., {@code $$}, {@code $HibernateProxy$})</li>
     * </ol>
     * <p>
     * <strong>Note:</strong> This uses heuristic pattern matching on class names and is not exhaustive.
     * Custom proxy implementations that don't follow these naming conventions will not be detected.
     *
     * @param aClass the class to check; may be null
     * @return {@code true} if the class appears to be a proxy type, {@code false} otherwise
     * @see #unproxyType(Class)
     * @see java.lang.reflect.Proxy#isProxyClass
     */
    public static boolean isProxyType(Class<?> aClass) {
        if (aClass == null) {
            return false;
        }
        if (java.lang.reflect.Proxy.isProxyClass(aClass)) {
            return true;
        }
        String simpleName = aClass.getSimpleName();
        return simpleName.contains("$$")
                || simpleName.contains("$HibernateProxy$")
                || simpleName.contains("$$EnhancerBySpringCGLIB$$")
                || simpleName.contains("$$CGLIB$$")
                || simpleName.contains("$$SpringCGLIB$$");
    }

    /**
     * Unwraps a proxy class to recover the underlying real type.
     * <p>
     * For proxy instances created by common frameworks (Hibernate, Spring CGLIB, JDK proxies),
     * this method recursively traverses the class hierarchy to find the original non-proxy type.
     * <p>
     * <strong>Behavior by proxy kind:</strong>
     * <ul>
     *   <li><strong>JDK Dynamic Proxies:</strong> Returns the first interface implemented by the proxy.
     *       This is a heuristic; if the proxy implements multiple interfaces, one is arbitrarily chosen.
     *       The actual target object type cannot be recovered from the proxy class alone.</li>
     *   <li><strong>Hibernate/CGLIB Proxies:</strong> Walks up the superclass chain via {@link Class#getSuperclass()}
     *       until a non-proxy class is found.</li>
     *   <li><strong>Non-proxy classes:</strong> Returns the input unchanged.</li>
     * </ul>
     * <p>
     * <strong>Example:</strong>
     * <pre>
     *   // Given a CGLIB proxy for class User
     *   Class&lt;?&gt; proxyClass = userProxy.getClass();  // e.g., User$$EnhancerBySpringCGLIB$$abc123
     *   Class&lt;?&gt; real = unproxyType(proxyClass);      // returns User.class
     * </pre>
     *
     * @param aClass the class to unwrap; may be null
     * @return the underlying real type, or the input class if it is not a proxy.
     * Returns {@code null} if input is {@code null}.
     * @see #isProxyType(Class)
     */
    public static Class<?> unproxyType(Class<?> aClass) {
        if (aClass == null) {
            return null;
        }
        // 1. Handle JDK Dynamic Proxies
        // These don't extend your class, they implement your interfaces.
        if (java.lang.reflect.Proxy.isProxyClass(aClass)) {
            // For JDK proxies, we usually take the first interface
            // as the "target" type, though this is a heuristic.
            Class<?>[] interfaces = aClass.getInterfaces();
            return (interfaces.length > 0) ? interfaces[0] : aClass;
        }
        if (isProxyType(aClass)) {
            Class<?> u = unproxyType(aClass.getSuperclass());
            if (u != null) {
                return u;
            }
        }
        return aClass;
    }

    private static class NTypeNameHierarchyComparator implements Comparator<NTypeName> {
        NTypeNameDomain domain;

        public NTypeNameHierarchyComparator(NTypeNameDomain domain) {
            this.domain = domain;
        }

        @Override
        public int compare(NTypeName o1, NTypeName o2) {
            if (domain.isAssignableFrom(o1, o2)) {
                return 1;
            } else if (domain.isAssignableFrom(o2, o1)) {
                return -1;
            }
            if (domain.isInterface(o1) && !domain.isInterface(o2)) {
                return 1;
            }
            if (domain.isInterface(o2) && !domain.isInterface(o1)) {
                return -1;
            }
            return 0;
        }
    }
}

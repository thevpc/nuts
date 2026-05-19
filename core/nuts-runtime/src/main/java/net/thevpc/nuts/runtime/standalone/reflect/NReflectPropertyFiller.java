package net.thevpc.nuts.runtime.standalone.reflect;

import net.thevpc.nuts.reflect.*;
import net.thevpc.nuts.util.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NReflectPropertyFiller {
    private static final Pattern GETTER_SETTER = Pattern.compile("(?<prefix>(get|set|is))(?<suffix>([A-Z].*))");
    // ─────────────────────────────────────────────────────────────────────────────
// Supporting value types  (inner classes of DefaultNReflectType)
// ─────────────────────────────────────────────────────────────────────────────
    private static final GetterNameHeuristicFilterManager getterNameHeuristicFilters = new GetterNameHeuristicFilterManager()
            .addDefaults();

    /**
     * Strategy tag attached to each candidate so we remember where it came from.
     */
    private enum CandidateSource {BEAN, FLUENT, FIELD, RECORD}

    /**
     * A raw getter or setter candidate collected during phase-1 scanning.
     * Immutable; built once per method/field.
     */
    private static final class MethodCandidate {
        final CandidateSource source;
        final Method method;        // null only for field-backed candidates
        final Field field;         // non-null only for field-backed candidates
        final Class<?> propertyType;  // return type (getter) or param type (setter)
        final boolean isGetter;
        // annotation flags (resolved once, never re-queried)
        final boolean forceGetter;   // @NGetter present
        final boolean forceSetter;   // @NSetter present
        final boolean forceInclude;  // @NInclude present
        final boolean forceExclude;  // @NExclude present

        /**
         * Method-backed candidate
         */
        MethodCandidate(CandidateSource source, Method method, Class<?> propertyType, boolean isGetter) {
            this.source = source;
            this.method = method;
            this.field = null;
            this.propertyType = propertyType;
            this.isGetter = isGetter;
            this.forceGetter = method.getAnnotation(NGetter.class) != null;
            this.forceSetter = method.getAnnotation(NSetter.class) != null;
            this.forceInclude = method.getAnnotation(NInclude.class) != null;
            this.forceExclude = method.getAnnotation(NExclude.class) != null;
        }

        /**
         * Field-backed candidate (always both getter+setter unless final)
         */
        MethodCandidate(Field field) {
            this.source = CandidateSource.FIELD;
            this.method = null;
            this.field = field;
            this.propertyType = field.getType();
            this.isGetter = true;   // fields are treated as getter side for pairing
            this.forceGetter = false;
            this.forceSetter = false;
            this.forceInclude = false;
            this.forceExclude = false;
        }
    }

// ─────────────────────────────────────────────────────────────────────────────
// Phase helpers
// ─────────────────────────────────────────────────────────────────────────────

    /**
     * Returns true when a FLUENT getter candidate is confirmed as a real property getter.
     * <p>
     * Rules (in priority order):
     * 1. @NGetter  → always confirmed
     * 2. @NInclude → confirmed (role = getter because it is no-arg non-void)
     * 3. @NExclude → never confirmed  (already filtered before reaching here)
     * 4. BEAN candidates are always confirmed (prefix gives enough signal)
     * 5. FLUENT: confirmed only when a setter candidate exists for the same name
     * OR when a no-arg @NSetter "confirmation token" was found for the same name
     */
    private boolean isConfirmedGetter(MethodCandidate getter,
                                      MethodCandidate setter,
                                      boolean hasConfirmationToken) {
        if (getter.forceGetter) return true;
        if (getter.forceInclude) return true;
        if (getter.source == CandidateSource.BEAN) return true;
        if (getter.source == CandidateSource.FIELD) return true;
        if (getter.source == CandidateSource.RECORD) return true;
        // FLUENT: require a paired setter or an explicit confirmation token
        return setter != null || hasConfirmationToken;
    }

    /**
     * Returns true when the setter's parameter type exactly matches the getter's return type.
     * Falls back gracefully if either side is null.
     */
    private boolean typesMatch(MethodCandidate getter, MethodCandidate setter) {
        if (getter == null || setter == null) return false;
        return setter.propertyType.equals(getter.propertyType);
    }

    /**
     * True if the method's return type is the declaring class or any of its super-types
     * (i.e. it is a fluent self-returning method, not a getter).
     */
    private static boolean isSelfReturning(Method m) {
        Class<?> ret = m.getReturnType();
        if (ret.equals(Void.TYPE)) return false;
        return m.getDeclaringClass().isAssignableFrom(ret);
    }

    /**
     * True if this method implements a well-known functional-interface single-abstract-method
     * and should therefore not be treated as a property accessor.
     */
    private static boolean implementsFunctionalInterface(Method m) {
        // Only no-arg or single-arg methods can be SAMs we care about
        int pc = m.getParameterCount();
        String name = m.getName();
        Class<?> ret = m.getReturnType();
        if (pc == 0) {
            // Supplier.get, Callable.call, Runnable.run
            return name.equals("get") || name.equals("call") || name.equals("run");
        }
        if (pc == 1) {
            // Consumer.accept, Predicate.test, UnaryOperator.apply, Function.apply
            return name.equals("accept") || name.equals("test") || name.equals("apply");
        }
        return false;
    }

    /**
     * True if this no-arg method returns a Stream, NStream, Iterable, or NIterator
     * (data-stream safety guard).
     */
    private static boolean isStreamLike(Method m) {
        if (m.getParameterCount() != 0) return false;
        Class<?> ret = m.getReturnType();
        return java.util.stream.Stream.class.isAssignableFrom(ret)
                || Iterable.class.isAssignableFrom(ret);
        // add NStream / NIterator checks if those are Class objects you can reference here
    }

// ─────────────────────────────────────────────────────────────────────────────
// Phase 1 — BEAN candidate collection
// ─────────────────────────────────────────────────────────────────────────────

    /**
     * Scans {@code methods} for JavaBean-style getters and setters.
     * <p>
     * Tiebreak for same property name:
     * - Two getters (getFoo + isFoo): isX wins for primitive boolean, getX wins otherwise.
     * - Multiple setters for the same name: all collected; pairing will pick the type-matching one.
     */
    private void collectBeanCandidates(
            Method[] methods,
            Map<String, MethodCandidate> getterCandidates,
            Map<String, List<MethodCandidate>> setterCandidates) {

        for (Method m : methods) {
            if (m.isSynthetic() || m.isBridge()) continue;
            if (Modifier.isStatic(m.getModifiers())) continue;

            String name = m.getName();
            if (name.equals("getClass")) continue;

            Matcher mat = GETTER_SETTER.matcher(name);
            if (!mat.find()) continue;

            String prefix = mat.group("prefix");
            char[] suffixChars = mat.group("suffix").toCharArray();
            suffixChars[0] = Character.toLowerCase(suffixChars[0]);
            String propName = new String(suffixChars);

            switch (prefix) {
                case "get": {
                    if (m.getParameterCount() == 0 && !m.getReturnType().equals(Void.TYPE)) {
                        MethodCandidate cand = new MethodCandidate(CandidateSource.BEAN, m, m.getReturnType(), true);
                        mergeBeanGetter(propName, cand, getterCandidates);
                    }
                    break;
                }
                case "is": {
                    Class<?> ret = m.getReturnType();
                    if (m.getParameterCount() == 0
                            && (ret.equals(Boolean.TYPE) || ret.equals(Boolean.class))) {
                        MethodCandidate cand = new MethodCandidate(CandidateSource.BEAN, m, ret, true);
                        mergeBeanGetter(propName, cand, getterCandidates);
                    }
                    break;
                }
                case "set": {
                    if (m.getParameterCount() == 1) {
                        MethodCandidate cand = new MethodCandidate(CandidateSource.BEAN, m, m.getParameterTypes()[0], false);
                        setterCandidates.computeIfAbsent(propName, k -> new ArrayList<>()).add(cand);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Inserts or replaces a bean getter candidate applying the is/get tiebreak rule.
     */
    private static void mergeBeanGetter(String propName,
                                        MethodCandidate incoming,
                                        Map<String, MethodCandidate> getterCandidates) {
        MethodCandidate existing = getterCandidates.get(propName);
        if (existing == null) {
            getterCandidates.put(propName, incoming);
            return;
        }
        // Tiebreak: isX() beats getX() for primitive boolean; getX() beats isX() otherwise
        boolean incomingIsIs = incoming.method.getName().startsWith("is");
        boolean existingIsIs = existing.method.getName().startsWith("is");
        boolean primitiveBoolean = incoming.propertyType.equals(Boolean.TYPE);

        if (primitiveBoolean && incomingIsIs && !existingIsIs) {
            getterCandidates.put(propName, incoming); // is wins for primitive boolean
        }
        // else: keep existing (get wins for boxed Boolean or non-boolean)
    }

// ─────────────────────────────────────────────────────────────────────────────
// Phase 1 — FLUENT candidate collection
// ─────────────────────────────────────────────────────────────────────────────

    /**
     * Scans {@code methods} for fluent-style accessor candidates.
     * <p>
     * A no-arg non-void method becomes a getter candidate.
     * A single-arg method becomes a setter candidate.
     * Methods already claimed by BEAN (matching the GETTER_SETTER pattern) are skipped.
     * <p>
     * Confirmation tokens (@NSetter on a no-arg method) are collected separately;
     * they confirm an unconfirmed getter but do not themselves become setters.
     */
    private void collectFluentCandidates(
            Method[] methods,
            Map<String, MethodCandidate> getterCandidates,
            Map<String, List<MethodCandidate>> setterCandidates,
            Set<String> confirmationTokens) {   // out param

        for (Method m : methods) {
            if (m.isSynthetic() || m.isBridge()) continue;
            if (Modifier.isStatic(m.getModifiers())) continue;
            if (Modifier.isAbstract(m.getModifiers())) continue;

            String name = m.getName();

            // Skip methods already handled by BEAN
            if (GETTER_SETTER.matcher(name).find()) continue;

            // Already claimed by a prior strategy
            if (getterCandidates.containsKey(name) && getterCandidates.get(name).source == CandidateSource.BEAN)
                continue;

            boolean forceGetter = m.getAnnotation(NGetter.class) != null;
            boolean forceSetter = m.getAnnotation(NSetter.class) != null;
            boolean forceInclude = m.getAnnotation(NInclude.class) != null;
            boolean forceExclude = m.getAnnotation(NExclude.class) != null;

            // @NGetter beats @NExclude; @NSetter beats @NExclude
            if (forceExclude && !forceGetter && !forceSetter) continue;

            int pc = m.getParameterCount();

            if (pc == 0) {
                // ── no-arg branch ────────────────────────────────────────────────

                // @NSetter on a no-arg method = confirmation token only
                if (forceSetter && !forceGetter) {
                    confirmationTokens.add(name);
                    continue;
                }

                Class<?> ret = m.getReturnType();
                if (ret.equals(Void.TYPE)) continue; // void no-arg = action, not getter

                // Heuristic guards (skipped when forced)
                if (!forceGetter && !forceInclude) {
                    if (isSelfReturning(m)) continue;
                    if (implementsFunctionalInterface(m)) continue;
                    if (isStreamLike(m)) continue;

                    String[] parts = NNameFormat.parse(name);

                    if (!getterNameHeuristicFilters.accept(name, parts, ret, m.getDeclaringClass())) continue;

                    // ignore intValue, longValue, etc.
                    if (parts.length == 2 && parts[1].equalsIgnoreCase("Value")) continue;
                }

                // Only add as getter if not already a confirmed BEAN getter
                if (!getterCandidates.containsKey(name)) {
                    getterCandidates.put(name, new MethodCandidate(CandidateSource.FLUENT, m, ret, true));
                }

            } else if (pc == 1) {
                // ── single-arg branch ────────────────────────────────────────────
                if (!forceGetter) { // @NGetter on a single-arg is unusual — ignore as setter
                    MethodCandidate cand = new MethodCandidate(CandidateSource.FLUENT, m, m.getParameterTypes()[0], false);
                    setterCandidates.computeIfAbsent(name, k -> new ArrayList<>()).add(cand);
                }
            }
        }
    }

// ─────────────────────────────────────────────────────────────────────────────
// Phase 2 — global pair resolution
// ─────────────────────────────────────────────────────────────────────────────

    /**
     * For each getter candidate, attempts to find a type-matching setter (from any strategy),
     * applies confirmation logic, and builds the NReflectProperty if the pair is valid.
     */
    private void resolveGetterCandidates(
            Map<String, MethodCandidate> getterCandidates,
            Map<String, List<MethodCandidate>> setterCandidates,
            Set<String> confirmationTokens,
            Set<String> ambiguousWrites,
            LinkedHashMap<String, IndexedItem<NReflectProperty>> out,
            int hierarchyIndex,
            Supplier<Object> cleanInstance,
            NReflectPropertyDefaultValueStrategy propertyDefaultValueStrategy, NReflectType THIS) {

        for (Map.Entry<String, MethodCandidate> entry : getterCandidates.entrySet()) {
            String propName = entry.getKey();
            MethodCandidate getter = entry.getValue();

            if (out.containsKey(propName)) continue; // already claimed by higher-priority strategy

            // Find the best setter: type-exact match preferred, from any strategy
            MethodCandidate setter = null;
            List<MethodCandidate> setterList = setterCandidates.get(propName);
            if (setterList != null) {
                for (MethodCandidate sc : setterList) {
                    if (typesMatch(getter, sc)) {
                        setter = sc;
                        break;
                    }
                }
                // If multiple setters and none matches the getter type → ambiguous write
                if (setter == null && setterList.size() > 1) {
                    ambiguousWrites.add(propName);
                }
            }

            boolean hasToken = confirmationTokens.contains(propName);

            if (!isConfirmedGetter(getter, setter, hasToken)) continue;

            // Build the property
            NReflectProperty prop = buildMethodProperty(propName, getter, setter, cleanInstance, propertyDefaultValueStrategy, THIS);
            if (prop != null) {
                out.put(propName, new IndexedItem<>(hierarchyIndex, prop));
            }
        }
    }

// ─────────────────────────────────────────────────────────────────────────────
// Phase 3 — orphan setter resolution (BEAN only, field fallback)
// ─────────────────────────────────────────────────────────────────────────────

    /**
     * Handles setX(v) with no matching getter method.
     * Falls back to a field of the same name and compatible type as the read side.
     * FLUENT orphan setters are always rejected here (they require a confirmed getter).
     */
    private void resolveOrphanSetters(
            Map<String, List<MethodCandidate>> setterCandidates,
            Map<String, MethodCandidate> getterCandidates,
            Field[] fields,
            LinkedHashMap<String, IndexedItem<NReflectProperty>> out,
            int hierarchyIndex,
            Supplier<Object> cleanInstance,
            NReflectPropertyDefaultValueStrategy propertyDefaultValueStrategy,
            NReflectType THIS
    ) {

        for (Map.Entry<String, List<MethodCandidate>> entry : setterCandidates.entrySet()) {
            String propName = entry.getKey();
            if (getterCandidates.containsKey(propName)) continue; // already handled in phase 2
            if (out.containsKey(propName)) continue;

            List<MethodCandidate> list = entry.getValue();
            if (list.size() != 1) continue; // ambiguous — skip

            MethodCandidate setter = list.get(0);
            if (setter.source != CandidateSource.BEAN) continue; // FLUENT orphan setter → always rejected

            // Try field fallback for read side
            for (Field f : fields) {
                if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers())) continue;
                if (f.getName().equals(propName) && f.getType().equals(setter.propertyType)) {
                    f.setAccessible(true);
                    NReflectProperty prop = new MethodReflectProperty3(
                            propName, f, setter.method, cleanInstance, THIS, propertyDefaultValueStrategy);
                    out.put(propName, new IndexedItem<>(hierarchyIndex, prop));
                    break;
                }
            }
        }
    }

// ─────────────────────────────────────────────────────────────────────────────
// Phase 4 — field properties
// ─────────────────────────────────────────────────────────────────────────────

    private void collectFieldProperties(
            Field[] fields,
            LinkedHashMap<String, IndexedItem<NReflectProperty>> out,
            int hierarchyIndex,
            Supplier<Object> cleanInstance,
            NReflectPropertyDefaultValueStrategy propertyDefaultValueStrategy, NReflectType THIS) {

        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            if (Modifier.isTransient(f.getModifiers())) continue;
            if (out.containsKey(f.getName())) continue;

            f.setAccessible(true);
            FieldReflectProperty prop = new FieldReflectProperty(f, cleanInstance, THIS, propertyDefaultValueStrategy);
            out.put(f.getName(), new IndexedItem<>(hierarchyIndex, prop));
        }
    }

// ─────────────────────────────────────────────────────────────────────────────
// Property builder
// ─────────────────────────────────────────────────────────────────────────────

    /**
     * Constructs the appropriate NReflectProperty subtype given a confirmed getter
     * and an optional setter.  Both may independently be method- or field-backed.
     */
    private NReflectProperty buildMethodProperty(
            String propName,
            MethodCandidate getter,
            MethodCandidate setter,
            Supplier<Object> cleanInstance,
            NReflectPropertyDefaultValueStrategy dvs, NReflectType THIS) {

        Method readMethod = getter.method;  // never null here
        Method writeMethod = (setter != null && setter.method != null) ? setter.method : null;

        // BEAN getter + no setter method → check BEAN+FIELD fallback is handled in phase 3;
        // here we just build with whatever we have.
        return new MethodReflectProperty1(propName, readMethod, writeMethod, cleanInstance, THIS, dvs);
    }

    // ─────────────────────────────────────────────────────────────────────────────
// Main entry point  (replaces the old fillProperties)
// ─────────────────────────────────────────────────────────────────────────────
    private void collectRecordCandidates(
            Class<?> clazz,
            Map<String, MethodCandidate> getterCandidates) {
        if (clazz == null) {
            return;
        }
        // java.lang.Class.getRecordComponents() exists only on JDK 16+
        // invoke reflectively to keep source compatibility with JDK 8
        try {
            Method getRecordComponents = Class.class.getMethod("getRecordComponents");
            Object[] components = (Object[]) getRecordComponents.invoke(clazz);
            if (components == null) return;

            Class<?> rcClass = Class.forName("java.lang.reflect.RecordComponent");
            Method getName = rcClass.getMethod("getName");
            Method getType = rcClass.getMethod("getType");
            Method getAccessor = rcClass.getMethod("getAccessor");

            for (Object rc : components) {
                String name = (String) getName.invoke(rc);
                Class<?> type = (Class<?>) getType.invoke(rc);
                Method accessor = (Method) getAccessor.invoke(rc);

                // record accessors are always confirmed — no setter, read-only property
                getterCandidates.put(name,
                        new MethodCandidate(CandidateSource.RECORD, accessor, type, true));
            }
        } catch (Exception e) {
            // JDK 8 or not a record — silently ignore
        }
    }

    public void fillProperties(
            int hierarchyIndex,
            Type clazz,
            LinkedHashMap<String, IndexedItem<NReflectProperty>> declaredProperties,
            Supplier<Object> cleanInstance,
            Set<String> ambiguousWrites,
            Set<NReflectPropertyAccessStrategy> propertyAccessStrategies,
            NReflectPropertyDefaultValueStrategy propertyDefaultValueStrategy, NReflectType THIS) {

        // Resolve active strategies in declaration order from NReflectConfig
        // (falls back to the enum's built-in ordering when ALL is used)
        List<NReflectPropertyAccessStrategy> activeStrategies = toOrderedList(propertyAccessStrategies);

        boolean useBean = activeStrategies.contains(NReflectPropertyAccessStrategy.BEAN);
        boolean useFluent = activeStrategies.contains(NReflectPropertyAccessStrategy.FLUENT);
        boolean useField = activeStrategies.contains(NReflectPropertyAccessStrategy.FIELD);

        Method[] methods = (useBean || useFluent) ? _getMethods(clazz) : new Method[0];
        Field[] fields = (useField || useBean) ? _getFields(clazz) : new Field[0];

        // ── Phase 1: collect candidates ─────────────────────────────────────────

        // Getters: one winner per property name (strategy priority tiebreaks)
        Map<String, MethodCandidate> getterCandidates = new LinkedHashMap<>();

        // Setters: all candidates collected; phase 2 picks the type-matching one
        Map<String, List<MethodCandidate>> setterCandidates = new LinkedHashMap<>();

        // Confirmation tokens: @NSetter on no-arg methods (FLUENT only)
        Set<String> confirmationTokens = new HashSet<>();
        collectRecordCandidates(NReflectUtils.getRawClass(clazz).orNull(), getterCandidates);

        // Collect in strategy priority order so earlier strategy wins on conflict
        for (NReflectPropertyAccessStrategy strategy : activeStrategies) {
            switch (strategy) {
                case BEAN:
                    if (useBean) collectBeanCandidates(methods, getterCandidates, setterCandidates);
                    break;
                case FLUENT:
                    if (useFluent && clazz != Object.class) {
                        collectFluentCandidates(methods, getterCandidates, setterCandidates, confirmationTokens);
                    }
                    break;
                case FIELD:
                    // Fields are handled in phase 4; no getter/setter candidates from fields here
                    // (BEAN+FIELD fallback is handled explicitly in phase 3)
                    break;
            }
        }

        // ── Phase 2: resolve confirmed getter+setter pairs ───────────────────────
        resolveGetterCandidates(
                getterCandidates, setterCandidates, confirmationTokens,
                ambiguousWrites, declaredProperties,
                hierarchyIndex, cleanInstance, propertyDefaultValueStrategy, THIS);

        // ── Phase 3: orphan BEAN setters with field fallback ────────────────────
        if (useBean) {
            resolveOrphanSetters(
                    setterCandidates, getterCandidates, fields,
                    declaredProperties,
                    hierarchyIndex, cleanInstance, propertyDefaultValueStrategy, THIS);
        }

        // ── Phase 4: bare fields ─────────────────────────────────────────────────
        if (useField) {
            collectFieldProperties(fields, declaredProperties, hierarchyIndex, cleanInstance, propertyDefaultValueStrategy, THIS);
        }
    }

// ─────────────────────────────────────────────────────────────────────────────
// Strategy resolution helper
// ─────────────────────────────────────────────────────────────────────────────

//    /**
//     * Expands ALL into [BEAN, FLUENT, FIELD] (the canonical priority order),
//     * otherwise returns the singleton strategy as a list.
//     * <p>
//     * When you wire NReflectConfig.strategy() properly this method should read
//     * the annotation array and return it directly, preserving user-declared order.
//     */
//    private static List<NReflectPropertyAccessStrategy> resolveActiveStrategies(
//            NReflectPropertyAccessStrategy strategy) {
//        if (strategy == NReflectPropertyAccessStrategy.ALL) {
//            return Arrays.asList(
//                    NReflectPropertyAccessStrategy.BEAN,
//                    NReflectPropertyAccessStrategy.FLUENT,
//                    NReflectPropertyAccessStrategy.FIELD);
//        }
//        return Collections.singletonList(strategy);
//    }

    private static List<NReflectPropertyAccessStrategy> toOrderedList(
            Set<NReflectPropertyAccessStrategy> strategies) {
        // always iterate in canonical priority order regardless of set internals
        return NReflectPropertyAccessStrategy.PRIORITY_ORDER.stream()
                .filter(strategies::contains)
                .collect(Collectors.toList());
    }

    static Method[] _getMethods(Type clazz) {
        Method[] declaredMethods = new Method[0];
        if (clazz instanceof Class) {
            declaredMethods = ((Class) clazz).getDeclaredMethods();
        } else if (clazz instanceof ParameterizedType) {
            Class c2 = NReflectUtils.getRawClass(clazz).orNull();
            if (c2 != null) {
                return c2.getDeclaredMethods();
            }
            throw new IllegalArgumentException("TODO");
        } else {
            throw new IllegalArgumentException("TODO");
        }
        return declaredMethods;
    }

    static Field[] _getFields(Type clazz) {
        Field[] declaredFields = new Field[0];
        if (clazz instanceof Class) {
            declaredFields = ((Class) clazz).getDeclaredFields();
        } else if (clazz instanceof ParameterizedType) {
            Class c2 = NReflectUtils.getRawClass(clazz).orNull();
            if (c2 != null) {
                return c2.getDeclaredFields();
            }
            throw new IllegalArgumentException("TODO");
        } else {
            throw new IllegalArgumentException("TODO");
        }
        return declaredFields;
    }
}

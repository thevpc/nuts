/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.reflect;

import net.thevpc.nuts.util.*;
import net.thevpc.nuts.reflect.*;
import net.thevpc.nuts.text.NMsg;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class DefaultNReflectType implements NReflectType {

    private static final Pattern GETTER_SETTER = Pattern.compile("(?<prefix>(get|set|is))(?<suffix>([A-Z].*))");

    private static final GetterNameHeuristicFilterManager getterNameHeuristicFilters = new GetterNameHeuristicFilterManager()
            .addDefaults();


    private final Type javaType;

    private Map<String, NReflectProperty> propertiesDeclaredMap;
    private List<NReflectProperty> propertiesDeclaredList;
    private Map<String, NReflectProperty> propertiesAllMap;
    private List<NReflectProperty> propertiesAllList;

    private Map<String, NReflectMethod> methodsDeclaredMap;
    private List<NReflectMethod> methodsDeclaredList;
    private Map<String, NReflectMethod> methodsAllMap;
    private List<NReflectMethod> methodsAllList;


    private final NReflectRepository repo;
    private final Set<NReflectPropertyAccessStrategy> propertyAccessStrategies;
    private final NReflectPropertyDefaultValueStrategy propertyDefaultValueStrategy;
    private ConstrHolder constrType;
    private ConstrHolder noArgConstr;
    private ConstrHolder specialConstr;
    private boolean cleanInstanceCreated;
    private Object cleanInstance;

    private class ConstrHolder {
        ConstrType type;
        Supplier<Object> supplier;

        public ConstrHolder(ConstrType type, Supplier<Object> supplier) {
            this.type = type;
            this.supplier = supplier;
        }
    }

    private enum ConstrType {
        WORKSPACE, SESSION, DEFAULT, SPECIAL, ERROR
    }

    public DefaultNReflectType(Type javaType, NReflectRepository repo) {
        this.javaType = javaType;
        this.repo = repo;
        Class<?> c2 = javaClass().orNull();
        this.propertyAccessStrategies = resolveAccessStrategies(c2, repo);
        this.propertyDefaultValueStrategy = c2 == null ? NReflectPropertyDefaultValueStrategy.BASE : this.repo.configuration().getDefaultValueStrategy(c2);
    }


    /**
     * Resolves the effective access strategy set for a given class by walking
     * up the hierarchy (BFS: class first, then interfaces), closest wins.
     * Falls back to the repository's global default if nothing is found.
     */
    private static Set<NReflectPropertyAccessStrategy> resolveAccessStrategies(
            Class<?> clazz,
            NReflectRepository repo) {

        if (clazz == null) {
            Set<NReflectPropertyAccessStrategy> a = repo.configuration().getDefaultAccessStrategies(clazz);
            if (a == null || a.isEmpty()) {
                return NReflectPropertyAccessStrategy.all();
            }
        }

        // BFS queue: class before interfaces at each level
        Queue<Class<?>> queue = new ArrayDeque<>();
        queue.add(clazz);

        while (!queue.isEmpty()) {
            Class<?> current = queue.poll();
            NReflectConfig cfg = current.getAnnotation(NReflectConfig.class);
            if (cfg != null && cfg.strategy().length > 0) {
                return EnumSet.copyOf(Arrays.asList(cfg.strategy()));
            }
            // class first, then interfaces
            Class<?> superClass = current.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                queue.add(superClass);
            }
            Collections.addAll(queue, current.getInterfaces());
        }

        Set<NReflectPropertyAccessStrategy> a = repo.configuration().getDefaultAccessStrategies(clazz);
        if (a == null || a.isEmpty()) {
            return NReflectPropertyAccessStrategy.all();
        }
        return a;
    }

    public Set<NReflectPropertyAccessStrategy> accessStrategies() {
        return Collections.unmodifiableSet(propertyAccessStrategies);
    }

    public NReflectPropertyDefaultValueStrategy defaultValueStrategy() {
        return propertyDefaultValueStrategy;
    }

    /**
     * @return direct declared properties
     */
    @Override
    public List<NReflectProperty> declaredProperties() {
        build();
        return propertiesDeclaredList;
    }

    @Override
    public String name() {
        return javaType.getTypeName();
    }

    /**
     * @return all (including inherited) declared properties
     */
    @Override
    public List<NReflectProperty> properties() {
        build();
        return propertiesAllList;
    }

    @Override
    public NOptional<NReflectProperty> getProperty(String name) {
        build();
        return NOptional.ofNamed(propertiesAllMap.get(name), "property " + name() + "::" + name);
    }

    @Override
    public NOptional<NReflectProperty> getDeclaredProperty(String name) {
        return NOptional.ofNamed(propertiesDeclaredMap.get(name), "property " + name);
    }

    public boolean isInterface() {
        return javaType instanceof Class && ((Class<?>) javaType).isInterface();
    }


    private Supplier<Object> resolveNoArgsConstr() {
        if (noArgConstr == null) {
            Supplier<Object> instanceSupplier = null;
            Class<?> jc = javaClass().orNull();
            if (jc != null) {
                if (!jc.isInterface()) {
                    int m = jc.getModifiers();
                    if (!Modifier.isAbstract(m)) {
                        try {
                            Constructor<?> sessionConstr0 = jc.getDeclaredConstructor();
                            sessionConstr0.setAccessible(true);
                            instanceSupplier = () -> {
                                try {
                                    return sessionConstr0.newInstance();
                                } catch (Exception ex) {
                                    throw asRuntimeException(ex);
                                }
                            };
                        } catch (Exception ex) {
                            //
                        }
                    }
                }
            }
            noArgConstr = new ConstrHolder(ConstrType.DEFAULT, instanceSupplier);
        }
        return noArgConstr.supplier;
    }

    public NOptional<Class<?>> javaClass() {
        Class c2;
        if (javaType instanceof Class<?>) {
            return NOptional.of((Class) javaType);
        } else {
            return NReflectUtils.getRawClass(javaType);
        }
    }

    private Supplier<Object> resolveSpecialConstr() {
        if (specialConstr == null) {
            Supplier<Object> sessionConstr1 = null;
            Class c2 = null;
            if (javaType instanceof Class<?>) {
                c2 = (Class) javaType;
            } else {
                c2 = NReflectUtils.getRawClass(javaType).orNull();
            }
            if (c2 != null) {
                if (c2.isInterface()) {
                    if (List.class.equals(c2)) {
                        sessionConstr1 = () -> new ArrayList();
                    } else if (Set.class.equals(c2)) {
                        sessionConstr1 = () -> new HashSet();
                    } else if (Collection.class.equals(c2)) {
                        sessionConstr1 = () -> new ArrayList();
                    } else if (Map.class.equals(c2)) {
                        sessionConstr1 = () -> new LinkedHashMap<>();
                    } else {
                        //
                    }
                }
            }
            specialConstr = new ConstrHolder(
                    ConstrType.SPECIAL,
                    sessionConstr1
            );
        }
        return specialConstr.supplier;
    }

    private ConstrType getConstrType() {
        if (constrType == null) {
            Supplier<Object> s;
            s = resolveNoArgsConstr();
            if (s != null) {
                constrType = noArgConstr;
                return constrType.type;
            }
            s = resolveSpecialConstr();
            if (s != null) {
                constrType = specialConstr;
                return constrType.type;
            }
            constrType = new ConstrHolder(ConstrType.ERROR, null);
        }
        return constrType.type;
    }

    @Override
    public boolean isAssignableFrom(NReflectType type) {
        if (javaType instanceof Class<?>) {
            if (type instanceof DefaultNReflectType) {
                DefaultNReflectType d = (DefaultNReflectType) type;
                if (d.javaType instanceof Class<?>) {
                    return ((Class<?>) javaType).isAssignableFrom((Class<?>) d.javaType);
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasNoArgsConstructor() {
        return resolveNoArgsConstr() != null;
    }

    @Override
    public boolean hasSpecialConstructor() {
        return resolveSpecialConstr() != null;
    }

    private RuntimeException asRuntimeException(Throwable e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        if (e instanceof InstantiationException || e instanceof InvocationTargetException) {
            Throwable c = e.getCause();
            if (c instanceof RuntimeException) {
                return (RuntimeException) c;
            }
            return new IllegalArgumentException(c);
        }
        return new IllegalArgumentException(e);
    }

    @Override
    public NReflectType rawType() {
        if (javaType instanceof ParameterizedType) {
            return repo.getType(((ParameterizedType) javaType).getRawType());
        }
        return null;
    }

    @Override
    public Object newInstance() {
        if (getConstrType() == ConstrType.ERROR) {
            throw new NIllegalArgumentException(NMsg.ofC("not instantiable %s", javaType));
        }
        return constrType.supplier.get();
    }

    @Override
    public NReflectType superType() {
        if (javaType instanceof Class<?>) {
            Type superclass = ((Class<?>) javaType).getGenericSuperclass();
            return superclass == null ? null : repo.getType(superclass);
        }
        if (javaType instanceof ParameterizedType) {
            Type rt = ((ParameterizedType) javaType).getRawType();
            return repo.getType(rt).replaceVars(x -> getActualTypeArgument(x).orElse(x));
        }
        return null;
    }

    @Override
    public List<NReflectType> interfaces() {
        if (javaType instanceof Class<?>) {
            Type[] all = ((Class<?>) javaType).getGenericInterfaces();
            return Arrays.stream(all).map(x -> repo.getType(x)).collect(Collectors.toList());
        }
//        if (javaType instanceof ParameterizedType) {
//            Type rt = ((ParameterizedType) javaType).getRawType();
//            return repo.getType(rt).replaceVars(x -> getActualTypeArgument(x).orElse(x));
//        }
        return Collections.emptyList();
    }

    private synchronized Object _cleanInstance() {
        if (!cleanInstanceCreated) {
            synchronized (this) {
                if (!cleanInstanceCreated) {
                    try {
                        cleanInstance = newInstance();
                    } catch (Exception ex) {
                        //ignore any error...
                    }
                    cleanInstanceCreated = true;
                }
            }
        }
        return cleanInstance;
    }

    private void build() {
        if (propertiesDeclaredMap == null) {
            Supplier<Object> cleanInstance = () -> _cleanInstance();
            LinkedHashMap<String, IndexedItem<NReflectMethod>> declaredMethods = new LinkedHashMap<>();
            LinkedHashMap<String, IndexedItem<NReflectMethod>> allMethods = new LinkedHashMap<>();


            LinkedHashMap<String, IndexedItem<NReflectProperty>> declaredProperties = new LinkedHashMap<>();
            LinkedHashMap<String, IndexedItem<NReflectProperty>> allProperties = new LinkedHashMap<>();
            Set<String> ambiguousWrites = new HashSet<>();
            int hierarchyIndex = 0;

            fillProperties(hierarchyIndex, javaType, declaredProperties, cleanInstance, ambiguousWrites, propertyAccessStrategies, propertyDefaultValueStrategy);
            allProperties.putAll(declaredProperties);

            fillMethods(hierarchyIndex, javaType, declaredMethods);
            allMethods.putAll(declaredMethods);

            NReflectType parent = superType();
            while (parent != null) {
                hierarchyIndex++;
                for (NReflectProperty property : parent.properties()) {
                    if (!allProperties.containsKey(property.name())) {
                        allProperties.put(property.name(), new IndexedItem<>(hierarchyIndex, property));
                    }
                }
                for (NReflectMethod m : parent.declaredMethods()) {
                    String sig = normalizeSig(m.name(), m.signature());
                    if (!allMethods.containsKey(sig)) {
                        allMethods.put(sig, new IndexedItem<>(hierarchyIndex, m));
                    }
                }
                parent = parent.superType();
            }


            this.propertiesDeclaredMap = reorderProperties(declaredProperties);
            this.propertiesAllMap = reorderProperties(allProperties);
            this.propertiesDeclaredList = Collections.unmodifiableList(new ArrayList<>(propertiesDeclaredMap.values()));
            this.propertiesAllList = Collections.unmodifiableList(new ArrayList<>(propertiesAllMap.values()));

            this.methodsDeclaredMap = reorderMethods(declaredMethods);
            this.methodsAllMap = reorderMethods(allMethods);
            this.methodsDeclaredList = Collections.unmodifiableList(new ArrayList<>(methodsDeclaredMap.values()));
            this.methodsAllList = Collections.unmodifiableList(new ArrayList<>(methodsAllMap.values()));

        }
    }

    @Override
    public List<NReflectMethod> methods() {
        build();
        return Collections.unmodifiableList(methodsAllList);
    }

    @Override
    public NOptional<NReflectMethod> getMethod(String name, NReflectSignature signature) {
        if (NBlankable.isBlank(name)) {
            //TODO
            return NOptional.ofNamedEmpty(signature.toString());
        } else {
            build();
            NReflectMethod value = methodsAllMap.get(normalizeSig(name, signature));
            if (value != null) {
                return NOptional.of(value);
            }
            return NOptional.ofNamedEmpty(signature.toString());
        }
    }

    private String normalizeSig(String name, NReflectSignature signature) {
        return name + signature.setVararg(false).toString();
    }

    /**
     * TODO
     *
     * @param signature
     * @return
     */
    @Override
    public List<NReflectMethod> getMatchingMethods(String name, NReflectSignature signature) {
        NOptional<NReflectMethod> m = getMethod(name, signature);
        if (m.isPresent()) {
            return Collections.singletonList(m.get());
        }
        return Collections.emptyList();
    }

    /**
     * TODO
     *
     * @param signature
     * @return
     */
    @Override
    public NOptional<NReflectMethod> getMatchingMethod(String name, NReflectSignature signature) {
        return getMethod(name, signature);
    }

    @Override
    public List<NReflectMethod> declaredMethods() {
        return Collections.unmodifiableList(methodsDeclaredList);
    }

    private void fillMethods(int hierarchyIndex, Type javaType, LinkedHashMap<String, IndexedItem<NReflectMethod>> declaredMethods) {
        Method[] declaredMethods2 = NReflectPropertyFiller._getMethods(javaType);
        for (Method m : declaredMethods2) {
            DefaultNReflectMethod rm = new DefaultNReflectMethod(m, this);
            declaredMethods.put(normalizeSig(m.getName(), rm.signature()), new IndexedItem<>(hierarchyIndex, rm));
        }
    }

    private LinkedHashMap<String, NReflectProperty> reorderProperties(LinkedHashMap<String, IndexedItem<NReflectProperty>> fieldAllProperties) {
        Map.Entry<String, IndexedItem<NReflectProperty>>[] ee = fieldAllProperties.entrySet().toArray(new Map.Entry[0]);
        Arrays.sort(ee, (o1, o2) -> Integer.compare(o2.getValue().index, o1.getValue().index));
        LinkedHashMap<String, NReflectProperty> r = new LinkedHashMap<>();
        for (Map.Entry<String, IndexedItem<NReflectProperty>> entry : ee) {
            r.put(entry.getKey(), entry.getValue().item);
        }
        return r;
    }

    private LinkedHashMap<String, NReflectMethod> reorderMethods(LinkedHashMap<String, IndexedItem<NReflectMethod>> fieldAllProperties) {
        Map.Entry<String, IndexedItem<NReflectMethod>>[] ee = fieldAllProperties.entrySet().toArray(new Map.Entry[0]);
        Arrays.sort(ee, (o1, o2) -> Integer.compare(o2.getValue().index, o1.getValue().index));
        LinkedHashMap<String, NReflectMethod> r = new LinkedHashMap<>();
        for (Map.Entry<String, IndexedItem<NReflectMethod>> entry : ee) {
            r.put(entry.getKey(), entry.getValue().item);
        }
        return r;
    }

    private void fillProperties(int hierarchyIndex, Type clazz, LinkedHashMap<String, IndexedItem<NReflectProperty>> declaredProperties, Supplier<Object> cleanInstance, Set<String> ambiguousWrites,
                                Set<NReflectPropertyAccessStrategy> propertyAccessStrategy,
                                NReflectPropertyDefaultValueStrategy propertyDefaultValueStrategy
    ) {
        new NReflectPropertyFiller().fillProperties(hierarchyIndex, clazz, declaredProperties, cleanInstance, ambiguousWrites, propertyAccessStrategy, propertyDefaultValueStrategy, this);
    }

//    private static Map<TypeVariable<?>, Type> getActualClassArguments0(Type type) {
//        Map<TypeVariable<?>, Type> m = new HashMap<>();
//        if (type instanceof ParameterizedType) {
//            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
//            Type rawType = ((ParameterizedType) type).getRawType();
//            if (rawType instanceof Class) {
//                TypeVariable<? extends Class<?>>[] typeParameters = ((Class<?>) rawType).getTypeParameters();
//                for (int i = 0; i < typeParameters.length; i++) {
//                    m.put(typeParameters[i], actualTypeArguments[i]);
//                }
//            }
//        }
//        return m;
//    }


    @Override
    public boolean isParametrizedType() {
        return javaType instanceof ParameterizedType;
    }

    @Override
    public boolean isTypeVariable() {
        return javaType instanceof TypeVariable;
    }

    @Override
    public List<NReflectType> typeParameters() {
        if (javaType instanceof Class) {
            return Arrays.stream(((Class) javaType).getTypeParameters()).map(x -> repo.getType(x))
                    .collect(Collectors.toList());
        }
        if (javaType instanceof ParameterizedType) {
            return repo.getType(((ParameterizedType) javaType).getRawType()).typeParameters();
        }
        return Collections.emptyList();
    }

    @Override
    public NOptional<NReflectType> getActualTypeArgument(NReflectType type) {
        List<NReflectType> typeParameters = typeParameters();
        List<NReflectType> r = actualTypeArguments();
        if (r.isEmpty()) {
            return NOptional.ofNamedEmpty(NMsg.ofC("actual type argument %s", type).toString());
        }
        for (int i = 0; i < typeParameters.size(); i++) {
            NReflectType typeParameter = typeParameters.get(i);
            if (typeParameter.equals(type)) {
                return NOptional.ofNamed(r.get(i), NMsg.ofC("actual type argument %s", type).toString());
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("actual type argument %s", type).toString());
    }

    @Override
    public List<NReflectType> actualTypeArguments() {
        if (isParametrizedType()) {
            return Arrays.stream(
                            ((ParameterizedType) javaType).getActualTypeArguments()
                    ).map(x -> repo.getType(x))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
    //
//
//
//    // thanks to https://stackoverflow.com/questions/1868333/how-can-i-determine-the-type-of-a-generic-field-in-java
//    public static class TypeFieldTreeNode {
//        public String fieldName;
//        public String typeSimpleName;
//        public String typeCanonicalName;
//        public Type typeGenericName;
//
//        public TypeFieldTreeNode(String fieldName, String typeSimpleName, String typeCanonicalName, Type genericTypeName) {
//            this.fieldName = fieldName;
//            this.typeSimpleName = typeSimpleName;
//            this.typeCanonicalName = typeCanonicalName;
//            this.typeGenericName = genericTypeName;
//        }
//    }
//
//    // thanks to https://stackoverflow.com/questions/1868333/how-can-i-determine-the-type-of-a-generic-field-in-java
//    private static TypeFieldTreeNode getClassFieldData(Field field,
//                                                Map<TypeVariable<?>, Type> actualClassArguments) {
//        Class<?> fieldClass = field.getType();
//        Type fieldGenericType = field.getGenericType();
//        TypeFieldTreeNode result = null;
//
//        // if type of the field is a generic parameter of the class containing the field
//        if(fieldGenericType instanceof TypeVariable<?>) {
//            Type actualFieldType = null;
//            Class<?> actualFieldClass = null;
//            Map<TypeVariable<?>, Type> fieldTypeActualClassArguments = new HashMap<>();
//            TypeVariable<?> fieldTypeVariable = (TypeVariable<?>) fieldGenericType;
//
//            if(actualClassArguments.containsKey(fieldTypeVariable))
//                actualFieldType = actualClassArguments.get(fieldTypeVariable);
//            else
//                throw new RuntimeException(String.format("For a field %s of type %s from class %s, the corresponding actual type of generic parameter was not found",
//                        field.getName(), fieldGenericType.getTypeName(), field.getDeclaringClass().getCanonicalName()));
//
//            // for example, field "myField2" of class MyClass2<MyClass<Integer>> where:
//            // public class MyClass2<T> { public T myField2; }
//            // public class MyClass<T> { public T myField; }
//            if(actualFieldType instanceof ParameterizedType) {
//                actualFieldClass = (Class<?>)((ParameterizedType) actualFieldType).getRawType();
//                result = new TypeFieldTreeNode(field.getName(), actualFieldClass.getSimpleName(),
//                        actualFieldClass.getCanonicalName(), actualFieldType);
//
//                fieldTypeActualClassArguments = mapTypeActualClassArguments(actualFieldClass, actualFieldType, actualClassArguments);
//            }
//            // for example, field "myField" of class MyClass<Integer> where:
//            // public class MyClass<T> { public T myField; }
//            else {
//                actualFieldClass = (Class<?>) actualFieldType;
//                result = new TypeFieldTreeNode(field.getName(), actualFieldClass.getSimpleName(),
//                        actualFieldClass.getCanonicalName(), actualFieldType);
//            }
//        }
//        // if the field is an array and the type of the elements of the array is a generic parameter of the class containing the field
//        // for example, field "myField" of class MyClass<Integer> where:
//        // public class MyClass<T> { public T[] myField; }
//        else if(fieldGenericType instanceof GenericArrayType) {
//            Type genericComponentType = ((GenericArrayType) fieldGenericType).getGenericComponentType();
//            if(genericComponentType instanceof TypeVariable<?>) {
//                if(actualClassArguments.containsKey(genericComponentType)) {
//                    Type actualArrayComponentType = actualClassArguments.get(genericComponentType);
//                    assert !(actualArrayComponentType instanceof ParameterizedType);
//                    Class<?> actualArrayClass = (Class<?>) actualArrayComponentType;
//                    result = new TypeFieldTreeNode(field.getName(), actualArrayClass.getSimpleName() + "[]",
//                            actualArrayClass.getCanonicalName() + "[]", actualArrayComponentType.to);
//                }
//                else
//                    throw new RuntimeException(String.format("For a field %s of type %s from class %s, the corresponding actual type of generic parameter was not found",
//                            field.getName(), fieldGenericType.getTypeName(), field.getDeclaringClass().getCanonicalName()));
//            }
//            else
//                throw new RuntimeException(String.format("Unknown array genericComponentType: %s", genericComponentType.getClass().getCanonicalName()));
//        }
//        else {
//            result = new TypeFieldTreeNode(field.getName(), fieldClass.getSimpleName(), fieldClass.getCanonicalName(), "");
//            Map<TypeVariable<?>, Type> fieldTypeActualClassArguments = new HashMap<>();
//
//            // for example, field "myField2" of class MyClass2<Integer> where:
//            // public class MyClass2<T> { public MyClass<T> myField2; }
//            // public class MyClass<T> { public T myField; }
//            if(fieldGenericType instanceof ParameterizedType) {
//
//                // custom generic type name creator for situations when actual type arguments can be of type TypeVariable
//                result.typeGenericName = getGenericTypeName((ParameterizedType)fieldGenericType, actualClassArguments);
//                fieldTypeActualClassArguments = mapTypeActualClassArguments(fieldClass, fieldGenericType, actualClassArguments);
//            }
//
//            List<Field> childFields = Arrays.stream(fieldClass.getFields()).filter(f -> !Modifier.isFinal(f.getModifiers()))
//                    .collect(Collectors.toList());
//            for (Field childField : childFields) {
//                result.children.add(getClassFieldData(childField, fieldTypeActualClassArguments));
//            }
//        }
//
//        return result;
//    }
//
//    // thanks to https://stackoverflow.com/questions/1868333/how-can-i-determine-the-type-of-a-generic-field-in-java
//    private static Map<TypeVariable<?>, Type> mapTypeActualClassArguments(Class<?> clazz, Type genericType,
//                                                                   Map<TypeVariable<?>, Type> actualClassArguments) {
//        if(!(genericType instanceof ParameterizedType)) {
//            return Collections.emptyMap();
//        }
//
//        Map<TypeVariable<?>, Type> result = new HashMap<>();
//        Type[] actualTypeParametersTypes = ((ParameterizedType) genericType).getActualTypeArguments();
//        TypeVariable<?>[] classTypeParameters = clazz.getTypeParameters();
//
//        for (int i = 0; i < classTypeParameters.length; i++) {
//            if(actualTypeParametersTypes[i] instanceof TypeVariable<?>) {
//                TypeVariable<?> fieldTypeVariable = (TypeVariable<?>) actualTypeParametersTypes[i];
//
//                if(actualClassArguments.containsKey(fieldTypeVariable))
//                    actualTypeParametersTypes[i] = actualClassArguments.get(fieldTypeVariable);
//                else
//                    throw new RuntimeException(String.format("For generic parameter %s of type %s, the corresponding actual type of generic parameter was not found",
//                            classTypeParameters[i].getName(), genericType.getTypeName()));
//            }
//            result.put(classTypeParameters[i], actualTypeParametersTypes[i]);
//        }
//
//        return result;
//    }
//
//    // thanks to https://stackoverflow.com/questions/1868333/how-can-i-determine-the-type-of-a-generic-field-in-java
//    private static String getGenericTypeName(ParameterizedType parameterizedType,
//                                      Map<TypeVariable<?>, Type> actualClassArguments) {
//        List<String> genericParamJavaTypes = new ArrayList<>();
//        for(Type typeArgument : parameterizedType.getActualTypeArguments()) {
//            if (typeArgument instanceof TypeVariable<?>) {
//                TypeVariable<?> typeVariable = (TypeVariable<?>) typeArgument;
//                if(actualClassArguments.containsKey(typeVariable)) {
//                    typeArgument = actualClassArguments.get(typeVariable);
//                } else
//                    throw new RuntimeException(String.format("For generic parameter %s of type %s, the corresponding actual type of generic parameter was not found",
//                            typeArgument.getTypeName(), parameterizedType.getTypeName()));
//            }
//
//            if(typeArgument instanceof ParameterizedType) {
//                ParameterizedType parameterizedTypeArgument = (ParameterizedType) typeArgument;
//                Map<TypeVariable<?>, Type> typeActualClassArguments = mapTypeActualClassArguments(
//                        (Class<?>)parameterizedTypeArgument.getRawType(),
//                        typeArgument, actualClassArguments);
//                genericParamJavaTypes.add(getGenericTypeName((ParameterizedType) typeArgument, typeActualClassArguments));
//            }
//            else if (typeArgument instanceof Class<?>)
//                genericParamJavaTypes.add(((Class<?>) typeArgument).getCanonicalName());
//            else
//                throw new RuntimeException(String.format("For generic parameter %s of type %s, the corresponding actual type of generic parameter was not found", typeArgument.getTypeName()));
//        }
//
//        Class<?> rawType = (Class<?>) parameterizedType.getRawType();
//        return rawType.getCanonicalName() + "<" + String.join(", ", genericParamJavaTypes) + ">";
//    }


    @Override
    public NReflectType replaceVars(Function<NReflectType, NReflectType> mapper) {
        if (javaType instanceof TypeVariable<?>) {
            NReflectType t = mapper.apply(this);
            if (t != null) {
                return t;
            }
            return this;
        }
        if (javaType instanceof Class<?>) {
            return this;
        }
        if (javaType instanceof ParameterizedType) {
            NReflectType[] actualTypeArguments = actualTypeArguments().toArray(new NReflectType[0]);
            NReflectType[] typeParameters = typeParameters().toArray(new NReflectType[0]);
            boolean someUpdates = false;
            for (int i = 0; i < actualTypeArguments.length; i++) {
                NReflectType r = typeParameters[i];
                NReflectType a = actualTypeArguments[i];
                if (a.isTypeVariable()) {
                    NReflectType aa = a.replaceVars(mapper);
                    if (aa != a && aa != null) {
                        actualTypeArguments[i] = aa;
                        someUpdates = true;
                    }
                }
            }
            if (someUpdates) {
                DefaultNReflectType ownerType = (DefaultNReflectType) getOwnerType();
                Type c2 = NReflectUtils.getRawClass(javaType).orNull();
                if (c2 == null) {
                    c2 = javaType;
                }
                return repo.getParametrizedType(c2,
                        ownerType == null ? null : ownerType.javaType,
                        Arrays.stream(actualTypeArguments)
                                .map(x -> ((DefaultNReflectType) x).javaType)
                                .toArray(Type[]::new)
                );
            }
            return this;
        }
        if (javaType instanceof GenericArrayType) {
            NReflectType a = componentType();
            NReflectType b = a.replaceVars(mapper);
            if (a != b && b != null) {
                return b.toArray();
            }
            return this;
        }
        if (javaType instanceof WildcardType) {
            return this;
        }
        return this;
    }

    @Override
    public boolean isArrayType() {
        if (javaType instanceof GenericArrayType) {
            return true;
        }
        if (javaType instanceof Class) {
            return ((Class) javaType).isArray();
        }
        return false;
    }

    @Override
    public NOptional<NReflectType> boxedType() {
        if (javaType instanceof Class) {
            Class c = (Class) javaType;
            if (!c.isPrimitive()) {
                return NOptional.of(this);
            }
            NOptional<Class<?>> b = NReflectUtils.toBoxedType(c);
            return b.map(x -> repo.getType(x));
        }
        return NOptional.ofNamedEmpty("primitive for " + this);
    }

    @Override
    public boolean isDefaultValue(Object value) {
        return Objects.equals(NReflectUtils.getJavaDefaultValue(javaType), value);
    }

    @Override
    public Object defaultValue() {
        if (javaType instanceof Class) {
            Class c = (Class) javaType;
            if (c.isPrimitive()) {
                return NReflectUtils.getDefaultValue(c);
            }
        }
        return null;
    }

    @Override
    public NOptional<NReflectType> primitiveType() {
        if (javaType instanceof Class) {
            Class c = (Class) javaType;
            if (c.isPrimitive()) {
                return NOptional.of(this);
            }
            NOptional<Class<?>> b = NReflectUtils.toPrimitiveType(c);
            return b.map(x -> repo.getType(x));
        }
        return NOptional.ofNamedEmpty("primitive for " + this);
    }

    @Override
    public boolean isPrimitive() {
        if (javaType instanceof Class) {
            Class c = (Class) javaType;
            return c.isPrimitive();
        }
        return false;
    }

    @Override
    public NReflectType toArray() {
        if (javaType instanceof Class) {
            Class c = (Class) javaType;
            try {
                Class<?> c2 = Class.forName(
                        "[L" + c.getCanonicalName() + ";"
                        , false, c.getClassLoader()
                );
                return repo.getType(c2);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return repo.getType(new SimpleGenericArrayType(javaType));
    }

    public NReflectType componentType() {
        if (javaType instanceof GenericArrayType) {
            return repo.getType(((GenericArrayType) javaType).getGenericComponentType());
        }
        if (javaType instanceof Class) {
            Class clazz = (Class) javaType;
            if (clazz.isArray()) {
                return repo.getType(clazz.getComponentType());
            }
        }
        return null;
    }

    public NReflectType getOwnerType() {
        if (javaType instanceof ParameterizedType) {
            Type ownerType = ((ParameterizedType) javaType).getOwnerType();
            if (ownerType != null) {
                return repo.getType(ownerType);
            }
        }
        return null;
    }

    @Override
    public NReflectRepository repository() {
        return repo;
    }

    @Override
    public String toString() {
        return String.valueOf(javaType);
    }

    public Type javaType() {
        return javaType;
    }
}

/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
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
package net.thevpc.nuts.runtime.standalone.util.reflect;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.util.NArrays;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.reflect.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author thevpc
 */
public class DefaultNReflectType implements NReflectType {

    private static final Pattern GETTER_SETTER = Pattern.compile("(?<prefix>(get|set|is))(?<suffix>([A-Z].*))");

    private Type javaType;
    //private Class clazz;
    private Map<String, NReflectProperty> direct;
    private List<NReflectProperty> directList;
    private Map<String, NReflectProperty> all;
    private List<NReflectProperty> allList;
    private NReflectRepository repo;
    private NReflectPropertyAccessStrategy propertyAccessStrategy;
    private NReflectPropertyDefaultValueStrategy propertyDefaultValueStrategy;
    private Constructor noArgConstr;
    private Constructor sessionConstr;
    private Constructor workspaceConstr;
    private NWorkspace workspace;
    private ConstrType constrType;
    private enum ConstrType{
        WORKSPACE,SESSION,DEFAULT,ERROR
    };

    public DefaultNReflectType(NWorkspace workspace,Type javaType, NReflectRepository repo) {
        this.javaType = javaType;
        this.repo = repo;
        this.workspace = workspace;
        Class c2 = ReflectUtils.getRawClass(javaType);
        this.propertyAccessStrategy = this.repo.getConfiguration().getAccessStrategy(c2);
        this.propertyDefaultValueStrategy = this.repo.getConfiguration().getDefaultValueStrategy(c2);
    }

    public NReflectPropertyAccessStrategy getAccessStrategy() {
        return propertyAccessStrategy;
    }

    public NReflectPropertyDefaultValueStrategy getDefaultValueStrategy() {
        return propertyDefaultValueStrategy;
    }

    /**
     * @return direct declared properties
     */
    @Override
    public List<NReflectProperty> getDeclaredProperties() {
        build();
        return directList;
    }

    @Override
    public String getName() {
        return javaType.getTypeName();
    }

    /**
     * @return all (including inherited) declared properties
     */
    @Override
    public List<NReflectProperty> getProperties() {
        build();
        return allList;
    }

    @Override
    public NOptional<NReflectProperty> getProperty(String name) {
        build();
        return NOptional.ofNamed(all.get(name), "property " + name);
    }

    @Override
    public NOptional<NReflectProperty> getDeclaredProperty(String name) {
        return NOptional.ofNamed(direct.get(name), "property " + name);
    }

    private ConstrType getConstrType() {
        if(constrType==null){
            if(hasSessionConstructor()){
                return constrType=ConstrType.SESSION;
            }
            if(hasWorkspaceConstructor()){
                return constrType=ConstrType.WORKSPACE;
            }
            if(hasNoArgsConstructor()){
                return constrType=ConstrType.DEFAULT;
            }
            return constrType=ConstrType.ERROR;
        }
        return constrType;
    }

    @Override
    public boolean hasNoArgsConstructor() {
        if (noArgConstr == null) {
            if (javaType instanceof Class<?>) {
                try {
                    noArgConstr = ((Class) javaType).getDeclaredConstructor();
                    noArgConstr.setAccessible(true);
                } catch (Exception ex) {
                    return false;
                }
            } else {
                Class c2 = ReflectUtils.getRawClass(javaType);
                if (c2 != null) {
                    try {
                        noArgConstr = c2.getDeclaredConstructor();
                        noArgConstr.setAccessible(true);
                    } catch (Exception ex) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean hasSessionConstructor() {
        if (sessionConstr == null) {
            if (javaType instanceof Class<?>) {
                try {
                    sessionConstr = ((Class) javaType).getDeclaredConstructor(NSession.class);
                    sessionConstr.setAccessible(true);
                } catch (Exception ex) {
                    return false;
                }
            } else {
                Class c2 = ReflectUtils.getRawClass(javaType);
                if (c2 != null) {
                    try {
                        sessionConstr = c2.getDeclaredConstructor(NSession.class);
                        sessionConstr.setAccessible(true);
                    } catch (Exception ex) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasWorkspaceConstructor() {
        if (workspaceConstr == null) {
            if (javaType instanceof Class<?>) {
                try {
                    workspaceConstr = ((Class) javaType).getDeclaredConstructor(NWorkspace.class);
                    workspaceConstr.setAccessible(true);
                } catch (Exception ex) {
                    return false;
                }
            } else {
                Class c2 = ReflectUtils.getRawClass(javaType);
                if (c2 != null) {
                    try {
                        workspaceConstr = c2.getDeclaredConstructor(NWorkspace.class);
                        workspaceConstr.setAccessible(true);
                    } catch (Exception ex) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public NReflectType getRawType() {
        if (javaType instanceof ParameterizedType) {
            return repo.getType(((ParameterizedType) javaType).getRawType());
        }
        return null;
    }

    @Override
    public Object newInstance() {
        if (javaType instanceof Class<?>) {
            try {
                switch (getConstrType()){
                    case ERROR:{
                        //resolveSessionConstr(true);
                        resolveNoArgsConstr(true);
                        throw new NIllegalArgumentException(NMsg.ofC("missing constructor for %s",javaType));
                    }
                    case WORKSPACE:{
                        return workspaceConstr.newInstance(workspace);
                    }
                    case SESSION:{
                        return sessionConstr.newInstance(workspace.currentSession());
                    }
                    case DEFAULT:{
                        return noArgConstr.newInstance();
                    }
                }
            } catch (InstantiationException | InvocationTargetException ex) {
                Throwable c = ex.getCause();
                if (c instanceof RuntimeException) {
                    throw (RuntimeException) c;
                }
                throw new IllegalArgumentException(c);
            } catch (IllegalAccessException ex) {
                throw new IllegalArgumentException(ex);
            }
            throw new IllegalArgumentException("not instantiable");
        }else{
            NReflectType r = getRawType();
            if (r == null) {
                throw new IllegalArgumentException("not instantiable");
            }
            return r.newInstance();
        }
    }

    private boolean resolveNoArgsConstr(boolean required) {
        if (noArgConstr == null) {
            if (javaType instanceof Class<?>) {
                try {
                    noArgConstr = ((Class) javaType).getDeclaredConstructor();
                    noArgConstr.setAccessible(true);
                } catch (NoSuchMethodException ex) {
                    if (required) {
                        throw new IllegalArgumentException("unable to resolve default constructor fo " + javaType, ex);
                    }
                } catch (SecurityException ex) {
                    if (required) {
                        throw new IllegalArgumentException("not allowed to access default constructor for " + javaType, ex);
                    }
                }
            }
        }
        return noArgConstr != null;
    }

    private boolean resolveSessionConstr(boolean required) {
        if (sessionConstr == null) {
            if (javaType instanceof Class<?>) {
                try {
                    sessionConstr = ((Class) javaType).getConstructor(NSession.class);
                    sessionConstr.setAccessible(true);
                } catch (NoSuchMethodException ex) {
                    if (required) {
                        throw new IllegalArgumentException("Unable to resolve default constructor fo " + javaType, ex);
                    }
                    return false;
                } catch (SecurityException ex) {
                    if (required) {
                        throw new IllegalArgumentException("Not allowed to access default constructor for " + javaType, ex);
                    }
                    return false;
                }
            }
        }
        return sessionConstr != null;
    }
    private boolean resolveWorkspaceConstr(boolean required) {
        if (workspaceConstr == null) {
            if (javaType instanceof Class<?>) {
                try {
                    workspaceConstr = ((Class) javaType).getConstructor(NWorkspace.class);
                    workspaceConstr.setAccessible(true);
                } catch (NoSuchMethodException ex) {
                    if (required) {
                        throw new IllegalArgumentException("Unable to resolve default constructor fo " + javaType, ex);
                    }
                    return false;
                } catch (SecurityException ex) {
                    if (required) {
                        throw new IllegalArgumentException("Not allowed to access default constructor for " + javaType, ex);
                    }
                    return false;
                }
            }
        }
        return workspaceConstr != null;
    }

    @Override
    public NReflectType getSuperType() {
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

    private void build() {
        if (direct == null) {
            Object cleanInstance = null;
            try {
                cleanInstance = newInstance();
            } catch (Exception ex) {
                //ignore any error...
            }
            LinkedHashMap<String, IndexedItem<NReflectProperty>> declaredProperties = new LinkedHashMap<>();
            LinkedHashMap<String, IndexedItem<NReflectProperty>> fieldAllProperties = new LinkedHashMap<>();
            Set<String> ambiguousWrites = new HashSet<>();
            int hierarchyIndex = 0;
            fillProperties(hierarchyIndex, javaType, declaredProperties, cleanInstance, ambiguousWrites, propertyAccessStrategy, propertyDefaultValueStrategy);
            fieldAllProperties.putAll(declaredProperties);
            NReflectType parent = getSuperType();
            while (parent != null) {
                hierarchyIndex++;
                for (NReflectProperty property : parent.getProperties()) {
                    if (!fieldAllProperties.containsKey(property.getName())) {
                        fieldAllProperties.put(property.getName(), new IndexedItem<>(hierarchyIndex, property));
                    }
                }
                parent = parent.getSuperType();
            }
            this.direct = reorder(declaredProperties);
            this.all = reorder(fieldAllProperties);
            this.directList = Collections.unmodifiableList(new ArrayList<>(direct.values()));
            this.allList = Collections.unmodifiableList(new ArrayList<>(all.values()));

        }
    }

    private LinkedHashMap<String, NReflectProperty> reorder(LinkedHashMap<String, IndexedItem<NReflectProperty>> fieldAllProperties) {
        Map.Entry<String, IndexedItem<NReflectProperty>>[] ee = fieldAllProperties.entrySet().toArray(new Map.Entry[0]);
        Arrays.sort(ee, (o1, o2) -> Integer.compare(o2.getValue().index, o1.getValue().index));
        LinkedHashMap<String, NReflectProperty> r = new LinkedHashMap<>();
        for (Map.Entry<String, IndexedItem<NReflectProperty>> entry : ee) {
            r.put(entry.getKey(), entry.getValue().item);
        }
        return r;
    }

    private void fillProperties(int hierarchyIndex, Type clazz, LinkedHashMap<String, IndexedItem<NReflectProperty>> declaredProperties, Object cleanInstance, Set<String> ambiguousWrites,
                                NReflectPropertyAccessStrategy propertyAccessStrategy,
                                NReflectPropertyDefaultValueStrategy propertyDefaultValueStrategy
    ) {
        if (propertyAccessStrategy == NReflectPropertyAccessStrategy.METHOD || propertyAccessStrategy == NReflectPropertyAccessStrategy.BOTH) {
            LinkedHashMap<String, Method> methodGetters = new LinkedHashMap<>();
            LinkedHashMap<String, List<Method>> methodSetters = new LinkedHashMap<>();
            Method[] declaredMethods = _getMethods(clazz);
            for (Method m : declaredMethods) {
                if (!m.isSynthetic() && !Modifier.isAbstract(m.getModifiers()) && !Modifier.isStatic(m.getModifiers())) {
                    String name = m.getName();
                    Matcher matcher = GETTER_SETTER.matcher(name);
                    if (matcher.find()) {
                        char[] n2c = matcher.group("suffix").toCharArray();
                        n2c[0] = Character.toLowerCase(n2c[0]);
                        String n2 = new String(n2c);
                        switch (matcher.group("prefix")) {
                            case "get": {
                                if (m.getParameterCount() == 0
                                        && !m.getReturnType().equals(Void.TYPE)) {
                                    if (!name.equals("getClass")) {
                                        methodGetters.put(n2, m);
                                    }
                                }
                                break;
                            }
                            case "is": {
                                if (m.getParameterCount() == 0
                                        && (m.getReturnType().equals(Boolean.TYPE)
                                        || m.getReturnType().equals(Boolean.class))) {
                                    methodGetters.put(n2, m);
                                }
                                break;
                            }
                            case "set": {
                                if (m.getParameterCount() == 1) {
                                    List<Method> li = methodSetters.get(n2);
                                    if (li == null) {
                                        li = new ArrayList<>();
                                        methodSetters.put(n2, li);
                                    }
                                    li.add(m);
                                }
                                break;
                            }
                        }
                    }
                }
            }
            for (Map.Entry<String, Method> entry : methodGetters.entrySet()) {
                String propName = entry.getKey();
                if (!declaredProperties.containsKey(propName)) {
                    Method readMethod = entry.getValue();
                    Method writeMethod = null;
                    Field writeField = null;
                    List<Method> possibleSetters = methodSetters.get(propName);
                    if (possibleSetters != null) {
                        for (Method possibleSetter : possibleSetters) {
                            Class ps = possibleSetter.getParameterTypes()[0];
                            if (ps.equals(readMethod.getReturnType())) {
                                writeMethod = possibleSetter;
                                methodSetters.remove(propName);
                                break;
                            }
                        }
                    }
                    if (writeMethod == null) {
                        if (propertyAccessStrategy == NReflectPropertyAccessStrategy.BOTH) {
                            Field[] declaredFields = _getFields(clazz);
                            for (Field f : declaredFields) {
                                if (!Modifier.isStatic(f.getModifiers()) && !Modifier.isTransient(f.getModifiers())) {
                                    if (!declaredProperties.containsKey(f.getName())) {
                                        if (!Modifier.isStatic(f.getModifiers())
                                                && !Modifier.isFinal(f.getModifiers())
                                                && f.getType().equals(readMethod.getReturnType())) {
                                            writeField = f;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (writeMethod != null) {
                        declaredProperties.put(propName,
                                new IndexedItem<>(hierarchyIndex,
                                        new MethodReflectProperty1(propName, readMethod, writeMethod, cleanInstance, this, propertyDefaultValueStrategy)
                                )
                        );
                    } else if (writeField != null) {
                        declaredProperties.put(propName,
                                new IndexedItem<>(hierarchyIndex,
                                        new MethodReflectProperty2(propName, readMethod, writeField, cleanInstance, this, propertyDefaultValueStrategy)
                                )
                        );
                    } else {
                        declaredProperties.put(propName,
                                new IndexedItem<>(hierarchyIndex,
                                        new MethodReflectProperty1(propName, readMethod, null, cleanInstance, this, propertyDefaultValueStrategy)
                                )
                        );
                    }
                }
            }
            for (Map.Entry<String, List<Method>> entry2 : methodSetters.entrySet()) {
                String propName = entry2.getKey();
                if (entry2.getValue().size() == 1) {
                    Method writeMethod = entry2.getValue().get(0);
                    if (!declaredProperties.containsKey(propName)) {
                        Field readField = null;
                        Field[] _fields = _getFields(clazz);
                        try {
                            readField = Arrays.stream(_fields).filter(x -> x.getName().equals(propName)).findAny().orElse(null);
                        } catch (Exception ex) {
                            //
                        }
                        if (readField != null && !Modifier.isStatic(readField.getModifiers()) && readField.getType().equals(writeMethod.getParameterTypes()[0])) {
                            declaredProperties.put(propName,
                                    new IndexedItem<>(hierarchyIndex,
                                            new MethodReflectProperty3(propName, readField, writeMethod, cleanInstance, this, propertyDefaultValueStrategy)
                                    )
                            );
                        }
                    }
                } else if (entry2.getValue().size() > 0) {
                    ambiguousWrites.add(propName);
                }
            }

        }
        if (propertyAccessStrategy == NReflectPropertyAccessStrategy.FIELD || propertyAccessStrategy == NReflectPropertyAccessStrategy.BOTH) {
            Field[] declaredFields = _getFields(clazz);
            for (Field f : declaredFields) {
                if (!declaredProperties.containsKey(f.getName())) {
                    if (!Modifier.isStatic(f.getModifiers()) && !Modifier.isTransient(f.getModifiers())) {
                        //TypeFieldTreeNode classFieldData = getClassFieldData(f, getActualClassArguments0(type));
                        FieldReflectProperty p = new FieldReflectProperty(f, cleanInstance, this, propertyDefaultValueStrategy);
                        declaredProperties.put(p.getName(), new IndexedItem<>(hierarchyIndex, p));
                    }
                }
            }
        }
    }

    private static Field[] _getFields(Type clazz) {
        Field[] declaredFields = new Field[0];
        if (clazz instanceof Class) {
            declaredFields = ((Class) clazz).getDeclaredFields();
        } else if (clazz instanceof ParameterizedType) {
            Class c2 = ReflectUtils.getRawClass(clazz);
            if (c2 != null) {
                return c2.getDeclaredFields();
            }
            throw new IllegalArgumentException("TODO");
        } else {
            throw new IllegalArgumentException("TODO");
        }
        return declaredFields;
    }

    private static Method[] _getMethods(Type clazz) {
        Method[] declaredMethods = new Method[0];
        if (clazz instanceof Class) {
            declaredMethods = ((Class) clazz).getDeclaredMethods();
        } else if (clazz instanceof ParameterizedType) {
            Class c2 = ReflectUtils.getRawClass(clazz);
            if (c2 != null) {
                return c2.getDeclaredMethods();
            }
            throw new IllegalArgumentException("TODO");
        } else {
            throw new IllegalArgumentException("TODO");
        }
        return declaredMethods;
    }

    private static Map<TypeVariable<?>, Type> getActualClassArguments0(Type type) {
        Map<TypeVariable<?>, Type> m = new HashMap<>();
        if (type instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                TypeVariable<? extends Class<?>>[] typeParameters = ((Class<?>) rawType).getTypeParameters();
                for (int i = 0; i < typeParameters.length; i++) {
                    m.put(typeParameters[i], actualTypeArguments[i]);
                }
            }
        }
        return m;
    }


    private static class IndexedItem<T> {
        int index;
        T item;

        public IndexedItem(int index, T item) {
            this.index = index;
            this.item = item;
        }
    }

    @Override
    public boolean isParametrizedType() {
        return javaType instanceof ParameterizedType;
    }

    @Override
    public boolean isTypeVariable() {
        return javaType instanceof TypeVariable;
    }

    @Override
    public NReflectType[] getTypeParameters() {
        if (javaType instanceof Class) {
            return Arrays.stream(((Class) javaType).getTypeParameters()).map(x -> repo.getType(x))
                    .toArray(NReflectType[]::new);
        }
        if (javaType instanceof ParameterizedType) {
            return repo.getType(((ParameterizedType) javaType).getRawType()).getTypeParameters();
        }
        return new NReflectType[0];
    }

    @Override
    public NOptional<NReflectType> getActualTypeArgument(NReflectType type) {
        NReflectType[] typeParameters = getTypeParameters();
        NReflectType[] r = getActualTypeArguments();
        if (r.length == 0) {
            return NOptional.ofNamedEmpty(NMsg.ofC("actual type argument %s", type).toString());
        }
        for (int i = 0; i < typeParameters.length; i++) {
            NReflectType typeParameter = typeParameters[i];
            if (typeParameter.equals(type)) {
                return NOptional.ofNamed(r[i], NMsg.ofC("actual type argument %s", type).toString());
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("actual type argument %s", type).toString());
    }

    @Override
    public NReflectType[] getActualTypeArguments() {
        if (isParametrizedType()) {
            return Arrays.stream(
                            ((ParameterizedType) javaType).getActualTypeArguments()
                    ).map(x -> repo.getType(x))
                    .toArray(NReflectType[]::new);
        }
        return new NReflectType[0];
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
            NReflectType[] actualTypeArguments = NArrays.copyOf(getActualTypeArguments());
            NReflectType[] typeParameters = NArrays.copyOf(getTypeParameters());
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
                Type c2 = ReflectUtils.getRawClass(javaType);
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
            NReflectType a = getComponentType();
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

    public NReflectType getComponentType() {
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
    public NReflectRepository getRepository() {
        return repo;
    }

    @Override
    public String toString() {
        return String.valueOf(javaType);
    }

    public Type getJavaType() {
        return javaType;
    }
}

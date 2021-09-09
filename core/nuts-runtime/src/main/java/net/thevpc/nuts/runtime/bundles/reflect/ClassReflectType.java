/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.bundles.reflect;

import net.thevpc.nuts.NutsSession;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author vpc
 */
public class ClassReflectType implements ReflectType {

    private static final Pattern GETTER_SETTER = Pattern.compile("(?<prefix>(get|set|is))(?<suffix>([A-Z][a-zA-Z_]*))");

    private Type type;
    private Class clazz;
    private Map<String, ReflectProperty> direct;
    private List<ReflectProperty> directList;
    private Map<String, ReflectProperty> all;
    private List<ReflectProperty> allList;
    private ReflectRepository repo;
    private ReflectPropertyAccessStrategy propertyAccessStrategy;
    private ReflectPropertyDefaultValueStrategy propertyDefaultValueStrategy;
    private Constructor noArgConstr;
    private Constructor sessionConstr;

    public ClassReflectType(Type type,
            ReflectPropertyAccessStrategy propertyAccessStrategy,
            ReflectPropertyDefaultValueStrategy propertyDefaultValueStrategy,
            ReflectRepository repo) {
        this.type = type;
        this.repo = repo;
        this.propertyAccessStrategy = propertyAccessStrategy;
        this.propertyDefaultValueStrategy = propertyDefaultValueStrategy;
        clazz = ReflectUtils.getRawClass(type);
    }

    public ReflectPropertyDefaultValueStrategy getDefaultValueStrategy() {
        return propertyDefaultValueStrategy;
    }

    public ReflectPropertyAccessStrategy getAccessStrategy() {
        return propertyAccessStrategy;
    }

    @Override
    public boolean hasNoArgsConstructor() {
        if (noArgConstr == null) {
            try {
                noArgConstr = clazz.getConstructor();
                noArgConstr.setAccessible(true);
            } catch (Exception ex) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasSessionConstructor() {
        if (sessionConstr == null) {
            try {
                sessionConstr = clazz.getConstructor(NutsSession.class);
                sessionConstr.setAccessible(true);
            } catch (Exception ex) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object newInstance() {
        if (noArgConstr == null) {
            try {
                noArgConstr = clazz.getConstructor();
                noArgConstr.setAccessible(true);
            } catch (NoSuchMethodException ex) {
                throw new IllegalArgumentException("Unable to resolve default constructor fo " + clazz, ex);
            } catch (SecurityException ex) {
                throw new IllegalArgumentException("Not allowed to access default constructor for " + clazz, ex);
            }
        }
        try {
            return noArgConstr.newInstance();
        } catch (InstantiationException | InvocationTargetException ex) {
            Throwable c = ex.getCause();
            if (c instanceof RuntimeException) {
                throw (RuntimeException) c;
            }
            throw new IllegalArgumentException(c);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public Object newInstance(NutsSession session) {
        if (sessionConstr == null) {
            try {
                sessionConstr = clazz.getConstructor(NutsSession.class);
                sessionConstr.setAccessible(true);
            } catch (NoSuchMethodException ex) {
                throw new IllegalArgumentException("Unable to resolve default constructor fo " + clazz, ex);
            } catch (SecurityException ex) {
                throw new IllegalArgumentException("Not allowed to access default constructor for " + clazz, ex);
            }
        }
        try {
            return sessionConstr.newInstance(session);
        } catch (InstantiationException | InvocationTargetException ex) {
            Throwable c = ex.getCause();
            if (c instanceof RuntimeException) {
                throw (RuntimeException) c;
            }
            throw new IllegalArgumentException(c);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private void build() {
        if (direct == null) {
            Object cleanInstance=null;
            try{
                cleanInstance = newInstance();
            }catch(Exception ex){
                //ignore any error...
            }
            LinkedHashMap<String, ReflectProperty> declaredProperties = new LinkedHashMap<>();
            LinkedHashMap<String, ReflectProperty> fieldAllProperties = new LinkedHashMap<>();
            Set<String> ambiguousWrites = new HashSet<>();
            fillFields(clazz, declaredProperties, cleanInstance, ambiguousWrites, propertyAccessStrategy, propertyDefaultValueStrategy);
            fieldAllProperties.putAll(declaredProperties);
            Class parent = clazz.getSuperclass();
            while (parent != null) {
                //must reeavliuate for parent classes
                ReflectPropertyAccessStrategy _propertyAccessStrategy = repo.getConfiguration().getAccessStrategy(parent);
                ReflectPropertyDefaultValueStrategy _propertyDefaultValueStrategy = repo.getConfiguration().getDefaultValueStrategy(parent);
                fillFields(parent, fieldAllProperties, cleanInstance, ambiguousWrites, _propertyAccessStrategy, _propertyDefaultValueStrategy);
                parent = parent.getSuperclass();
            }
            this.direct = declaredProperties;
            this.all = fieldAllProperties;
            this.directList = Collections.unmodifiableList(new ArrayList<>(direct.values()));
            this.allList = Collections.unmodifiableList(new ArrayList<>(all.values()));

        }
    }

    private void fillFields(Class clazz, LinkedHashMap<String, ReflectProperty> declaredProperties, Object cleanInstance, Set<String> ambiguousWrites,
            ReflectPropertyAccessStrategy propertyAccessStrategy,
            ReflectPropertyDefaultValueStrategy propertyDefaultValueStrategy
    ) {
        if (propertyAccessStrategy == ReflectPropertyAccessStrategy.METHOD || propertyAccessStrategy == ReflectPropertyAccessStrategy.BOTH) {
            LinkedHashMap<String, Method> methodGetters = new LinkedHashMap<>();
            LinkedHashMap<String, List<Method>> methodSetters = new LinkedHashMap<>();
            for (Method m : clazz.getDeclaredMethods()) {
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
            for (Iterator<Map.Entry<String, Method>> it = methodGetters.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, Method> entry = it.next();
                String propName = entry.getKey();
                if (!declaredProperties.containsKey(propName)) {
                    Method readMethod = entry.getValue();
                    Method writeMethod = null;
                    Field writeField = null;
                    List<Method> possibleSetters = methodSetters.get(propName);
                    if (possibleSetters != null) {
                        for (Method posibleSetter : possibleSetters) {
                            Class ps = posibleSetter.getParameterTypes()[0];
                            if (ps.equals(readMethod.getReturnType())) {
                                writeMethod = posibleSetter;
                                methodSetters.remove(propName);
                                break;
                            }
                        }
                    }
                    if (writeMethod == null) {
                        if (propertyAccessStrategy == ReflectPropertyAccessStrategy.BOTH) {
                            for (Field f : clazz.getDeclaredFields()) {
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
                        declaredProperties.put(propName, new MethodReflectProperty1(propName, readMethod, writeMethod, cleanInstance, this, propertyDefaultValueStrategy));
                    } else if (writeField != null) {
                        declaredProperties.put(propName, new MethodReflectProperty2(propName, readMethod, writeField, cleanInstance, this, propertyDefaultValueStrategy));
                    } else {
                        declaredProperties.put(propName, new MethodReflectProperty1(propName, readMethod, null, cleanInstance, this, propertyDefaultValueStrategy));
                    }
                }
            }
            for (Map.Entry<String, List<Method>> entry2 : methodSetters.entrySet()) {
                String propName = entry2.getKey();
                if (entry2.getValue().size() == 1) {
                    Method writeMethod = entry2.getValue().get(0);
                    if (!declaredProperties.containsKey(propName)) {
                        Field readField = null;
                        try {
                            readField = clazz.getDeclaredField(propName);
                        } catch (Exception ex) {
                            //
                        }
                        if (readField != null && !Modifier.isStatic(readField.getModifiers()) && readField.getType().equals(writeMethod.getParameterTypes()[0])) {
                            declaredProperties.put(propName, new MethodReflectProperty3(propName, readField, writeMethod, cleanInstance, this, propertyDefaultValueStrategy));
                        }
                    }
                } else if (entry2.getValue().size() > 0) {
                    ambiguousWrites.add(propName);
                }
            }

        }
        if (propertyAccessStrategy == ReflectPropertyAccessStrategy.FIELD || propertyAccessStrategy == ReflectPropertyAccessStrategy.BOTH) {
            for (Field f : clazz.getDeclaredFields()) {
                if (!declaredProperties.containsKey(f.getName())) {
                    if (!Modifier.isStatic(f.getModifiers()) && !Modifier.isTransient(f.getModifiers())) {
                        FieldReflectProperty p = new FieldReflectProperty(f, cleanInstance, this, propertyDefaultValueStrategy);
                        declaredProperties.put(p.getName(), p);
                    }
                }
            }
        }
    }

    /**
     *
     * @return direct declared properties
     */
    @Override
    public List<ReflectProperty> getDeclaredProperties() {
        build();
        return directList;
    }

    /**
     *
     * @return all (including inherited) declared properties
     */
    @Override
    public List<ReflectProperty> getProperties() {
        build();
        return allList;
    }

    @Override
    public String getName() {
        return clazz.getName();
    }
}

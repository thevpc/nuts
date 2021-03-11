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

import java.lang.reflect.Field;
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
    private ReflectPropertyStrategy reflectPropertyStrategy;

    public ClassReflectType(Type type, ReflectPropertyStrategy reflectPropertyStrategy, ReflectRepository repo) {
        this.type = type;
        this.repo = repo;
        this.reflectPropertyStrategy = reflectPropertyStrategy;
        clazz = ReflectUtils.getRawClass(type);
    }

    private void build() {
        if (direct == null) {
            LinkedHashMap<String, ReflectProperty> declaredProperties = new LinkedHashMap<>();
            LinkedHashMap<String, ReflectProperty> fieldAllProperties = new LinkedHashMap<>();
            Set<String> ambiguousWrites = new HashSet<>();
            if (reflectPropertyStrategy == ReflectPropertyStrategy.METHOD || reflectPropertyStrategy == ReflectPropertyStrategy.BOTH) {
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
                                        if(!name.equals("getClass")){
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
                            if (reflectPropertyStrategy == ReflectPropertyStrategy.BOTH) {
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
                            declaredProperties.put(propName, new MethodReflectProperty1(propName, readMethod, writeMethod));
                        } else if (writeField != null) {
                            declaredProperties.put(propName, new MethodReflectProperty2(propName, readMethod, writeField));
                        } else {
                            declaredProperties.put(propName, new MethodReflectProperty1(propName, readMethod, null));
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
                                declaredProperties.put(propName, new MethodReflectProperty3(propName, readField, writeMethod));
                            }
                        }
                    } else if (entry2.getValue().size() > 0) {
                        ambiguousWrites.add(propName);
                    }
                }

            }
            if (reflectPropertyStrategy == ReflectPropertyStrategy.FIELD || reflectPropertyStrategy == ReflectPropertyStrategy.BOTH) {
                for (Field f : clazz.getDeclaredFields()) {
                    if (!declaredProperties.containsKey(f.getName())) {
                        if (!Modifier.isStatic(f.getModifiers()) && !Modifier.isTransient(f.getModifiers())) {
                            FieldReflectProperty p = new FieldReflectProperty(f);
                            declaredProperties.put(p.getName(), p);
                        }
                    }
                }
            }
            fieldAllProperties.putAll(declaredProperties);
            if (clazz.getSuperclass() != null) {
                ReflectType t = repo.get(clazz.getSuperclass());
                for (ReflectProperty p2 : t.getProperties()) {
                    if (!fieldAllProperties.containsKey(p2.getName())) {
                        fieldAllProperties.put(p2.getName(), p2);
                    }
                }
            }
            this.direct = declaredProperties;
            this.all = fieldAllProperties;
            this.directList = Collections.unmodifiableList(new ArrayList<>(direct.values()));
            this.allList = Collections.unmodifiableList(new ArrayList<>(all.values()));

        }
    }

    /**
     *
     * @return
     */
    @Override
    public List<ReflectProperty> getDeclaredProperties() {
        build();
        return directList;
    }

    /**
     *
     * @return
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

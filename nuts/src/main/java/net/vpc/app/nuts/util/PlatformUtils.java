/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Created by vpc on 1/12/17.
 */
public class PlatformUtils {

    public static String getterName(String name, Class type) {
        if (Boolean.TYPE.equals(type)) {
            return "is" + suffix(name);
        }
        return "get" + suffix(name);
    }

    public static String setterName(String name) {
        //Class<?> type = field.getDataType();
        return "set" + suffix(name);
    }

    private static String suffix(String s) {
        char[] chars = s.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    public static PlatformBeanProperty[] findPlatformBeanProperties(Class platformType) {
        LinkedHashMap<String, PlatformBeanProperty> visited = new LinkedHashMap<>();
        Class curr = platformType;
        while (curr != null && !curr.equals(Object.class)) {
            for (Method method : curr.getDeclaredMethods()) {
                String n = method.getName();
                String field = null;
                if (method.getParameterTypes().length == 0 && method.getReturnType().equals(Boolean.TYPE) && n.startsWith("is") && n.length() > 2 && Character.isUpperCase(n.charAt(2))) {
                    field = n.substring(2);
                } else if (method.getParameterTypes().length == 0 && !method.getReturnType().equals(Boolean.TYPE) && n.startsWith("get") && n.length() > 3 && Character.isUpperCase(n.charAt(3))) {
                    field = n.substring(3);
                } else if (method.getParameterTypes().length == 1 && method.getReturnType().equals(Void.TYPE) && n.startsWith("set") && n.length() > 3 && Character.isUpperCase(n.charAt(3))) {
                    field = n.substring(3);
                }
                if (field != null) {
                    char[] chars = field.toCharArray();
                    chars[0] = Character.toLowerCase(chars[0]);
                    field = new String(chars);
                    if (!visited.containsKey(field)) {
                        PlatformBeanProperty platformBeanProperty = findPlatformBeanProperty(field, platformType);
                        if (platformBeanProperty != null) {
                            visited.put(field, platformBeanProperty);
                        }
                    }
                }
            }
            curr = curr.getSuperclass();
        }
        return visited.values().toArray(new PlatformBeanProperty[visited.size()]);
    }

    public static PlatformBeanProperty findPlatformBeanProperty(String field, Class platformType) {
        Field jfield = null;
        try {
            jfield = platformType.getDeclaredField(field);
        } catch (NoSuchFieldException e) {
            //ignore
        }
        String g1 = PlatformUtils.getterName(field, Object.class);
        String g2 = PlatformUtils.getterName(field, Boolean.TYPE);
        String s = PlatformUtils.setterName(field);
        Class<?> x = platformType;
        Method getter = null;
        Method setter = null;
        Class propertyType = null;
        LinkedHashMap<Class, Method> setters = new LinkedHashMap<Class, Method>();
        while (x != null) {
            for (Method m : x.getDeclaredMethods()) {
                if (!Modifier.isStatic(m.getModifiers())) {
                    String mn = m.getName();
                    if (getter == null) {
                        if (g1.equals(mn) || g2.equals(mn)) {
                            if (m.getParameterTypes().length == 0 && !Void.TYPE.equals(m.getReturnType())) {
                                getter = m;
                                Class<?> ftype = getter.getReturnType();
                                for (Class key : new HashSet<Class>(setters.keySet())) {
                                    if (!key.equals(ftype)) {
                                        setters.remove(key);
                                    }
                                }
                                if (setter == null) {
                                    setter = setters.get(ftype);
                                }
                            }
                        }
                    }
                    if (setter == null) {
                        if (s.equals(mn)) {
                            if (m.getParameterTypes().length == 1) {
                                Class<?> stype = m.getParameterTypes()[0];
                                if (getter != null) {
                                    Class<?> gtype = getter.getReturnType();
                                    if (gtype.equals(stype)) {
                                        if (!setters.containsKey(stype)) {
                                            setters.put(stype, m);
                                        }
                                        if (setter == null) {
                                            setter = m;
                                        }
                                    }
                                } else {
                                    if (!setters.containsKey(stype)) {
                                        setters.put(stype, m);
                                    }
                                }
                            }
                        }
                    }
                    if (getter != null && setter != null) {
                        break;
                    }
                }
            }
            if (getter != null && setter != null) {
                break;
            }
            x = x.getSuperclass();
        }
        if (getter != null) {
            propertyType = getter.getReturnType();
        }
        if (getter == null && setter == null && setters.size() > 0) {
            Method[] settersArray = setters.values().toArray(new Method[setters.size()]);
            setter = settersArray[0];
            if (settersArray.length > 1) {
                //TODO log?
            }
        }
        if (getter == null && setter != null && propertyType == null) {
            propertyType = setter.getParameterTypes()[0];
        }
        if (getter != null || setter != null) {
            return new DefaultPlatformBeanProperty(field, propertyType, jfield, getter, setter);
        }
        return null;
    }

    public static Boolean getExecutableJar(File file) throws IOException {
        if (file == null || !file.isFile()) {
            return null;
        }
        return getMainClass(file) != null;
    }

    public static boolean isExecutableJar(File file) throws IOException {
        return getMainClass(file) != null;
    }

    public static String getMainClass(File file) throws IOException {
        if (file == null || !file.isFile()) {
            return null;
        }
        try {
            try (JarFile f = new JarFile(file)) {
                Manifest manifest = f.getManifest();
                if (manifest == null) {
                    return null;
                }
                String mainClass = manifest.getMainAttributes().getValue("Main-Class");
//            if(!StringUtils.isEmpty(mainClass)) {
//                System.out.println(">> " + mainClass + " : " + file);
//            }
                return !StringUtils.isEmpty(mainClass) ? mainClass : null;
            }
        } catch (Exception ex) {
            //invalid file
            return null;
        }
    }

    public static <T> List<T> toList(Iterator<T> it) {
        List<T> list = new ArrayList<>();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

}

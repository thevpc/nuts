///**
// * ====================================================================
// * Nuts : Network Updatable Things Service
// * (universal package manager)
// * <br>
// * is a new Open Source Package Manager to help install packages
// * and libraries for runtime execution. Nuts is the ultimate companion for
// * maven (and other build managers) as it helps installing all package
// * dependencies at runtime. Nuts is not tied to java and is a good choice
// * to share shell scripts and other 'things' . Its based on an extensible
// * architecture to help supporting a large range of sub managers / repositories.
// * <br>
// *
// * Copyright [2020] [thevpc]
// * Licensed under the Apache License, Version 2.0 (the "License"); you may
// * not use this file except in compliance with the License. You may obtain a
// * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// * either express or implied. See the License for the specific language
// * governing permissions and limitations under the License.
// * <br>
// * ====================================================================
//*/
//package net.thevpc.nuts;
//
//import java.io.File;
//import java.io.InputStream;
//import java.io.Reader;
//import java.net.URL;
//import java.nio.file.Path;
//
///**
// * Implementation of this interface will provide
// * simple mechanism to write json text from given object.
// * @author thevpc
// * @since 0.5.5
// * @category Format
// */
//public interface NutsJsonFormat extends NutsFormat {
//
//    /**
//     * true is compact json flag is armed
//     * @return true is compact json flag is armed
//     */
//    boolean isCompact();
//
//    /**
//     * enable compact json
//     * @return {@code this} instance
//     */
//    NutsJsonFormat compact();
//
//    /**
//     * enable or disable compact json
//     * @param compact enable when true
//     * @return {@code this} instance
//     */
//    NutsJsonFormat compact(boolean compact);
//
//    /**
//     * enable or disable compact json
//     * @param compact enable when true
//     * @return {@code this} instance
//     */
//    NutsJsonFormat setCompact(boolean compact);
//
//    /**
//     * return value to format
//     * @return value to format
//     * @since 0.5.6
//     */
//    Object getValue();
//
//    /**
//     * @param value value to format
//     * @return {@code this} instance
//     * @since 0.5.6
//     */
//    NutsJsonFormat value(Object value);
//
//    /**
//     * @param value value to format
//     * @return {@code this} instance
//     * @since 0.5.6
//     */
//    NutsJsonFormat setValue(Object value);
//
//    /**
//     * parse url as a valid object of the given type
//     * @param url source url
//     * @param clazz target type
//     * @param <T> target type
//     * @return new instance of the given class
//     */
//    <T> T parse(URL url, Class<T> clazz);
//
//    /**
//     * parse inputStream as a valid object of the given type
//     * @param inputStream source inputStream
//     * @param clazz target type
//     * @param <T> target type
//     * @return new instance of the given class
//     */
//    <T> T parse(InputStream inputStream, Class<T> clazz);
//
//    /**
//     * parse inputStream as a valid object of the given type
//     * @param jsonString source as json string
//     * @param clazz target type
//     * @param <T> target type
//     * @return new instance of the given class
//     */
//    <T> T parse(String jsonString, Class<T> clazz);
//
//    /**
//     * parse bytes as a valid object of the given type
//     * @param bytes source bytes
//     * @param clazz target type
//     * @param <T> target type
//     * @return new instance of the given class
//     */
//    <T> T parse(byte[] bytes, Class<T> clazz);
//
//    /**
//     * parse reader as a valid object of the given type
//     * @param reader source reader
//     * @param clazz target type
//     * @param <T> target type
//     * @return new instance of the given class
//     */
//    <T> T parse(Reader reader, Class<T> clazz);
//
//    /**
//     * parse file as a valid object of the given type
//     * @param file source url
//     * @param clazz target type
//     * @param <T> target type
//     * @return new instance of the given class
//     */
//    <T> T parse(Path file, Class<T> clazz);
//
//    /**
//     * parse file as a valid object of the given type
//     * @param file source url
//     * @param clazz target type
//     * @param <T> target type
//     * @return new instance of the given class
//     */
//    <T> T parse(File file, Class<T> clazz);
//
//    /**
//     * update session
//     *
//     * @param session session
//     * @return {@code this instance}
//     */
//    @Override
//    NutsJsonFormat setSession(NutsSession session);
//
//    /**
//     * configure the current command with the given arguments. This is an
//     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...)
//     * }
//     * to help return a more specific return type;
//     *
//     * @param skipUnsupported when true, all unsupported options are skipped
//     * @param args argument to configure with
//     * @return {@code this} instance
//     */
//    @Override
//    public NutsJsonFormat configure(boolean skipUnsupported, String... args);
//}

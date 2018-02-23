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
package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.extensions.util.JsonStatus;
import net.vpc.app.nuts.extensions.util.SerializeOptions;

import javax.json.*;
import java.io.File;
import java.io.Writer;
import java.util.Map;

public interface JsonSerializer {

    JsonArrayBuilder serializeArr(Object obj, SerializeOptions options);

    JsonObjectBuilder serializeStringsMap(Map<String, String> value, SerializeOptions options);

    void serializeObjProp(String prop, Object value, Class t, JsonObjectBuilder builder, SerializeOptions options);

    void serializeArrProp(Object value, Class t, JsonArrayBuilder builder, SerializeOptions options);

    <T> T loadJson(File file, Class<T> cls);

    void storeJson(JsonStructure structure, File file, boolean pretty);

    void storeJson(JsonStructure structure, Writer writer, boolean pretty);

    <T> void storeJson(T obj, File file, SerializeOptions options);

    String[] getJsonObjectStringArray(JsonObject jsonObject, String param);

    boolean isNull(JsonValue obj);

    Map<String, String> deserializeStringsMap(JsonValue obj, Map<String, String> t);

    <T> T deserialize(JsonValue obj, Class<T> t);

    <T> T deserialize(String obj, Class<T> t);

    JsonObjectBuilder serializeObj(Object obj, SerializeOptions options);

    void readJsonPartialString(String str, JsonStatus s);

    JsonStructure loadJsonStructure(String jsonText);
}

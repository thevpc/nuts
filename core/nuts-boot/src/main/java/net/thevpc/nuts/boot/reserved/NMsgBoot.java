/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.boot.reserved;

import net.thevpc.nuts.boot.reserved.util.NReservedLangUtilsBoot;
import net.thevpc.nuts.boot.reserved.util.NStringUtilsBoot;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

public class NMsgBoot {

    public static final Object[] NO_PARAMS = new Object[0];
    private final String codeLang;
    private final Object message;
    private final Level level;
    private final String format;
    private final Object[] params;

    private static NMsgBoot of(String format, Object message, Object[] params, String codeLang, Level level) {
        return new NMsgBoot(format, message, params, codeLang, level);
    }

    private NMsgBoot(String format, Object message, Object[] params, String codeLang, Level level) {
        NAssertBoot.requireNonNull(message, "message");
        NAssertBoot.requireNonNull(format, "format");
        NAssertBoot.requireNonNull(params, "params");
        this.level = level;
        this.format = format;
        switch (format){
            case "PLAIN":{
                if (params.length > 0) {
                    throw new IllegalArgumentException("arguments are not supported for " + format);
                }
                break;
            }
        }

        this.codeLang = NStringUtilsBoot.trimToNull(codeLang);
        this.message = message;
        this.params = params;
    }

    public static NMsgBoot ofPlain(String message) {
        return of("PLAIN", message, NO_PARAMS, null, null);
    }

    @Deprecated
    public static NMsgBoot ofC(String message) {
        return of("CFORMAT", message, NO_PARAMS, null, null);
    }

    public static NMsgBoot ofC(String message, Object... params) {
        return of("CFORMAT", message, params, null, null);
    }

    public static NMsgBoot ofV(String message, NMsgParamBoot... params) {
        Map<String, NMsgParamBoot> vars = new LinkedHashMap<>();
        if (params != null) {
            for (NMsgParamBoot param : params) {
                String e = param.getName();
                if (vars.containsKey(e)) {
                    throw new IllegalArgumentException("duplicate key " + e);
                }
                vars.put(e, param);
            }
        }
        return ofV(message, s -> {
            NMsgParamBoot p = vars.get(s);
            if (p != null) {
                Supplier<?> ss = p.getValue();
                if (ss != null) {
                    return ss.get();
                }
            }
            return null;
        });
    }

    public static NMsgBoot ofV(String message, Map<String, ?> vars) {
        return of("VFORMAT", message, new Object[]{vars}, null, null);
    }

    public static NMsgBoot ofV(String message, Function<String, ?> vars) {
        return of("VFORMAT", message, new Object[]{vars}, null, null);
    }

    public static NMsgBoot ofJ(String message, NMsgParamBoot... params) {
        if (params == null) {
            return ofJ(message, new Object[]{null});
        }
        Object[] paramsAsObjects = Arrays.stream(params).map(NMsgParamBoot::getValue).toArray();
        return ofJ(message, paramsAsObjects);
    }

    public static NMsgBoot ofC(String message, NMsgParamBoot... params) {
        if (params == null) {
            return ofC(message, new Object[]{null});
        }
        Object[] paramsAsObjects = Arrays.stream(params).map(NMsgParamBoot::getValue).toArray();
        return ofC(message, paramsAsObjects);
    }

    @Deprecated
    public static NMsgBoot ofJ(String message) {
        return of("JFORMAT", message, NO_PARAMS, null, null);
    }

    public static NMsgBoot ofJ(String message, Object... params) {
        return of("JFORMAT", message, params, null, null);
    }

    public String getFormat() {
        return format;
    }

    public Object getMessage() {
        return message;
    }

    public Object[] getParams() {
        return params;
    }

    public String getCodeLang() {
        return codeLang;
    }

    public Level getLevel() {
        return level;
    }

    private Object _preFormatOne(Object o){
        if(o==null){
            return null;
        }
        return o;
    }

    private Object[] _preFormatArr(Object[] o){
        if(o==null){
            return o;
        }
        Object[] r=new Object[o.length];
        for (int i = 0; i < r.length; i++) {
            r[i]=_preFormatOne(o[i]);
        }
        return r;
    }

    @Override
    public String toString() {
        try {
            switch (format) {
                case "CFORMAT": {
                    StringBuilder sb = new StringBuilder();
                    new Formatter(sb).format((String) message, _preFormatArr(params));
                    return sb.toString();
                }
                case "JFORMAT": {
                    //must process special case of {}
                    String sMsg = (String) message;
                    if (sMsg.contains("{}")) {
                        StringBuilder sb = new StringBuilder();
                        char[] chars = sMsg.toCharArray();
                        int currentIndex = 0;
                        for (int i = 0; i < chars.length; i++) {
                            char c = chars[i];
                            if (c == '{') {
                                StringBuilder sb2 = new StringBuilder();
                                i++;
                                while (i < chars.length) {
                                    char c2 = chars[i];
                                    if (c2 == '}') {
                                        break;
                                    } else if (c2 == '\\') {
                                        sb2.append(c2);
                                        i++;
                                        if (i < chars.length) {
                                            c2 = chars[i];
                                            sb2.append(c2);
                                        }
                                    } else {
                                        sb2.append(c2);
                                    }
                                }
                                String s2 = sb2.toString();
                                if (s2.isEmpty()) {
                                    s2 = String.valueOf(currentIndex);
                                } else if (s2.trim().startsWith(":")) {
                                    s2 = String.valueOf(currentIndex) + s2;
                                }
                                sb.append("{").append(s2).append("}");
                                currentIndex++;
                            } else if (c == '\\') {
                                sb.append(c);
                                i++;
                                if (i < chars.length) {
                                    sb.append(c);
                                }
                            } else {
                                sb.append(c);
                            }
                        }
                        sMsg = sb.toString();
                    }
                    return MessageFormat.format(sMsg, _preFormatArr(params));
                }
                case "VFORMAT": {
                    return formatAsV();
                }
                default: {
                    return String.valueOf(message); //ignore any style
                }
            }
        } catch (Exception e) {
            List<Object> a = new ArrayList<>();
            if (params != null) {
                a.add(Arrays.asList(params));
            }
            return NMsgBoot.ofC("[ERROR] Invalid %s message %s with params %s : %s", format, message, a, e).toString();
        }
    }

    private String formatAsV() {
        return NStringUtilsBoot.replaceDollarPlaceHolder((String) message,
                s -> {
                    Object param = params[0];
                    Function<String, ?> m = null;
                    if (param instanceof Map) {
                        m = x -> ((Map<String, ?>) param).get(x);
                    } else {
                        m = (Function<String, ?>) param;
                    }
                    Object v = m.apply(s);
                    if (v != null) {
                        return String.valueOf(v);
                    }
                    return "${" + s + "}";
                }
        );
    }

    public NMsgBoot asSevere(){
        return withLevel(Level.SEVERE);
    }

    public NMsgBoot asInfo(){
        return withLevel(Level.FINE);
    }

    public NMsgBoot asConfig(){
        return withLevel(Level.CONFIG);
    }

    public NMsgBoot asWarning(){
        return withLevel(Level.WARNING);
    }

    public NMsgBoot asFinest(){
        return withLevel(Level.FINEST);
    }

    public NMsgBoot asFine(){
        return withLevel(Level.FINE);
    }

    public NMsgBoot asFiner(){
        return withLevel(Level.FINER);
    }

    public NMsgBoot withLevel(Level level) {
        return of(format, message, params, codeLang, level);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NMsgBoot that = (NMsgBoot) o;
        return Objects.equals(codeLang, that.codeLang)
                && Objects.equals(message, that.message)
                && format == that.format
                && Arrays.deepEquals(params, that.params)
                && Objects.equals(level, that.level)
                ;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(codeLang, message, format, level);
        result = 31 * result + Arrays.hashCode(params);
        return result;
    }


}

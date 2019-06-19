/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

/**
 *
 * @author vpc
 */
public class ObjectOutputFormatWriterHelper {

    public static boolean isMapStringObject(Object m) {
        if (m instanceof Map) {
            Map mm = ((Map) m);
            for (Object object : ((Map) m).keySet()) {
                if (object == null
                        || object instanceof String
                        || object instanceof Enum
                        || object instanceof Number
                        || object instanceof Boolean) {

                } else {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static Map<String, String> indentMap(Map m, String prefix) {
        LinkedHashMap<String, String> res = new LinkedHashMap<>();
        for (Object k : new HashSet(m.keySet())) {
            String sk = CoreCommonUtils.stringValue(k);
            Object v = m.get(k);
            if (isMapStringObject(v)) {
                res.put(prefix + sk, "");
                Map<String, String> c = indentMap((Map) v, prefix + "  ");
                for (Map.Entry<String, String> entry : c.entrySet()) {
                    res.put(entry.getKey(), CoreCommonUtils.stringValue(entry.getValue()));
                }
            } else {
                res.put(prefix + sk, CoreCommonUtils.stringValue(v));
            }
        }
        return res;
    }

    public static Map<String, String> explodeMap(Map m) {
        LinkedHashMap<String, String> res = new LinkedHashMap<>();
        for (Object k : m.keySet()) {
            String sk = CoreCommonUtils.stringValue(k);
            Object v = m.get(k);
            if (isMapStringObject(v)) {
                Map<String, String> c = explodeMap((Map) v);
                for (Map.Entry<String, String> entry : c.entrySet()) {
                    res.put(sk + "." + entry.getKey(), CoreCommonUtils.stringValue(entry.getValue()));
                }
            } else {
                res.put(sk, CoreCommonUtils.stringValue(v));
            }
        }
        return res;
    }

}

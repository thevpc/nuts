/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
            String sk = stringValue(k);
            Object v = m.get(k);
            if (isMapStringObject(v)) {
                res.put(prefix + sk, "");
                Map<String, String> c = indentMap((Map) v, prefix + "  ");
                for (Map.Entry<String, String> entry : c.entrySet()) {
                    res.put(entry.getKey(), stringValue(entry.getValue()));
                }
            } else {
                res.put(prefix + sk, stringValue(v));
            }
        }
        return res;
    }

    public static Map<String, String> explodeMap(Map m) {
        LinkedHashMap<String, String> res = new LinkedHashMap<>();
        for (Object k : new HashSet(m.keySet())) {
            String sk = stringValue(k);
            Object v = m.get(k);
            if (isMapStringObject(v)) {
                Map<String, String> c = explodeMap((Map) v);
                for (Map.Entry<String, String> entry : c.entrySet()) {
                    res.put(sk + "." + entry.getKey(), stringValue(entry.getValue()));
                }
            } else {
                res.put(sk, stringValue(v));
            }
        }
        return res;
    }

    public static String stringValue(Object o) {
        if (o == null) {
            return "";
        }
        if (o.getClass().isEnum()) {
            return o.toString().toLowerCase().replace("_", "-");
        }
        if (o instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format((Date) o);
        }
        return o.toString();
    }

    public static Element createElement(String name, Object o, Document document) {
        // root element
        Element root = document.createElement(name);
        if (o instanceof Map) {
            Map<Object, Object> m = (Map) o;
            for (Map.Entry<Object, Object> entry : m.entrySet()) {
                root.appendChild(createElement(stringValue(entry.getKey()), entry.getValue(), document));
            }
        } else if (o instanceof Collection) {
            Collection m = (Collection) o;
            int index = 0;
            for (Object entry : m) {
                root.appendChild(createElement(stringValue(index), entry, document));
                index++;
            }
        } else {
            root.setTextContent(stringValue(o));
        }
        return root;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
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
        for (Object k : new HashSet(m.keySet())) {
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

    public static String createElementName(String name) {
        if(name==null){
            name="";
        }
        name=name.trim();
        if(name.isEmpty()){
            name="node";
        }
        if(Character.isDigit(name.charAt(0))){
            name="_"+name;
        }
        if(name.toLowerCase().startsWith("xml")){
            name="_"+name;
        }
        char[] r=name.toCharArray();
        for (int i = 0; i < r.length; i++) {
            char c = r[i];
            if(     Character.isDigit(c) 
                    || Character.isLetter(c) 
                    || c=='_'
                    || c=='-'
                    || c=='.'
                            ){
                //ok
            }else{
                r[i]='_';
            }
        }
        return new String(r);
    }
    
    public static Element createElement(String name, Object o, Document document) {
        // root element
        Element root = document.createElement(createElementName(name));
        if (o instanceof Map) {
            Map<Object, Object> m = (Map) o;
            for (Map.Entry<Object, Object> entry : m.entrySet()) {
                root.appendChild(createElement(CoreCommonUtils.stringValue(entry.getKey()), entry.getValue(), document));
            }
        } else if (o instanceof Collection) {
            Collection m = (Collection) o;
            int index = 0;
            for (Object entry : m) {
                root.appendChild(createElement(CoreCommonUtils.stringValue(index), entry, document));
                index++;
            }
        } else {
            root.setTextContent(CoreCommonUtils.stringValue(o));
        }
        return root;
    }
}

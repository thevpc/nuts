package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsUtilStrings;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;

import java.util.*;

public class CommaStringParser {
    public static final String SEP = ",";
    public static final String EQ = "=";
    public static final StringMapParser QPARSER = new StringMapParser(EQ, SEP);
    public interface Processor {
        boolean process(String name, String value);
    }

    private final Map<String, String> properties;
    private final Processor processor;
    private final boolean sorted;
    private static final boolean nullValueIsKey=true;

//    public QueryStringParser() {
//        this(null)
//    }

    public CommaStringParser(boolean sorted, Processor processor) {
        this.processor = processor;
        this.sorted = sorted;
        if (sorted) {
            properties = new TreeMap<>();
        } else {
            properties = new LinkedHashMap<>();
        }
    }

    protected String prepareKey(String key) {
        return NutsUtilStrings.trimToNull(key);
    }

    protected String prepareValue(String value) {
        return NutsUtilStrings.trimToNull(value);
    }

    public void setProperty(String key, String value) {
        key = prepareKey(key);
        if (key == null) {
            return;
        }
        value = prepareValue(value);
        if (processor != null) {
            if (processor.process(key, value)) {
                return;
            }
        }
        if (value == null) {
            properties.remove(key);
        } else {
            properties.put(key, value);
        }
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public int size() {
        return properties.size();
    }

    private void _setProperties(Map<String, String> queryMap, boolean merge) {
        if (!merge) {
            properties.clear();
        }
        if (queryMap != null) {
            for (Map.Entry<String, String> e : queryMap.entrySet()) {
                setProperty(e.getKey(), e.getValue());
            }
        }
    }

    public CommaStringParser setProperties(Map<String, String> queryMap) {
        _setProperties(queryMap, false);
        return this;
    }

    public void addProperties(Map<String, String> queryMap) {
        _setProperties(queryMap, true);
    }

    public CommaStringParser clear() {
        this.properties.clear();
        return this;
    }

    public CommaStringParser setProperties(String propertiesQuery, NutsSession session) {
        Map<String, String> m2 = parseMap(propertiesQuery,session);
        this.properties.clear();
        for (Map.Entry<String, String> e : m2.entrySet()) {
            setProperty(e.getKey(), e.getValue());
        }
        return this;
    }

    public void addProperties(String propertiesQuery,NutsSession session) {
        Map<String, String> m2 = parseMap(propertiesQuery,session);
        for (Map.Entry<String, String> e : m2.entrySet()) {
            setProperty(e.getKey(), e.getValue());
        }
    }

    public String getPropertiesQuery() {
        return formatPropertiesQuery(properties);
    }

    public Map<String, String> getProperties() {
        return new LinkedHashMap<>(properties);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommaStringParser that = (CommaStringParser) o;
        return properties.equals(that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(properties);
    }

    public static String formatPropertiesQuery(Map<String, String> query) {
        StringBuilder sb = new StringBuilder();
        if (query != null) {
            Set<String> sortedKeys = new TreeSet<>(query.keySet());
            for (String k : sortedKeys) {
                String v = query.get(k);
                if (v != null && v.length() > 0) {
                    if (sb.length() > 0) {
                        sb.append(SEP);
                    }
                    sb.append(CoreStringUtils.simpleQuote(k, true, SEP+EQ)).append(EQ).append(
                            CoreStringUtils.simpleQuote(v, true, SEP+EQ)
                    );
                }else if(v==null && nullValueIsKey){
                    sb.append(CoreStringUtils.simpleQuote(k, true, SEP+EQ));
                }
            }
        }
        return NutsUtilStrings.trimToNull(sb.toString());
    }

    public static String formatSortedPropertiesQuery(String query,NutsSession session){
        return new CommaStringParser(true,null).setProperties(query,session).getPropertiesQuery();
    }

    public static String formatSortedPropertiesQuery(Map<String,String> query,NutsSession session){
        return new CommaStringParser(true,null).setProperties(query).getPropertiesQuery();
    }

    public static Map<String, String> parseMap(String text, NutsSession session) {
        return QPARSER.parseMap(text,session);
    }
}

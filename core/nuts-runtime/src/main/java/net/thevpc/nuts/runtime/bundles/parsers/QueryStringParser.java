package net.thevpc.nuts.runtime.bundles.parsers;

import net.thevpc.nuts.NutsUtilStrings;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.util.*;

public class QueryStringParser {
    private static final StringMapParser QPARSER = new StringMapParser("=", "&");
    public interface Processor {
        boolean process(String name, String value);
    }

    private final Map<String, String> properties;
    private final Processor processor;
    private final boolean sorted;

//    public QueryStringParser() {
//        this(null)
//    }

    public QueryStringParser(boolean sorted, Processor processor) {
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

    public QueryStringParser setProperties(Map<String, String> queryMap) {
        _setProperties(queryMap, false);
        return this;
    }

    public void addProperties(Map<String, String> queryMap) {
        _setProperties(queryMap, true);
    }

    public QueryStringParser setProperties(String propertiesQuery) {
        Map<String, String> m2 = parseMap(propertiesQuery);
        this.properties.clear();
        for (Map.Entry<String, String> e : m2.entrySet()) {
            setProperty(e.getKey(), e.getValue());
        }
        return this;
    }

    public void addProperties(String propertiesQuery) {
        Map<String, String> m2 = parseMap(propertiesQuery);
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
        QueryStringParser that = (QueryStringParser) o;
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
//                switch (k) {
//                    case NutsConstants.IdProperties.ALTERNATIVE: {
//                        if (CoreNutsUtils.isDefaultAlternative(v)) {
//                            v = null;
//                        }
//                        break;
//                    }
//                }
                if (v != null && v.length() > 0) {
                    if (sb.length() > 0) {
                        sb.append("&");
                    }
                    sb.append(CoreStringUtils.simpleQuote(k, true, "&=")).append("=").append(
                            CoreStringUtils.simpleQuote(v, true, "&=")
                    );
                }
            }
        }
        return NutsUtilStrings.trimToNull(sb.toString());
    }

    public static String formatSortedPropertiesQuery(String query){
        return new QueryStringParser(true,null).setProperties(query).getPropertiesQuery();
    }

    public static String formatSortedPropertiesQuery(Map<String,String> query){
        return new QueryStringParser(true,null).setProperties(query).getPropertiesQuery();
    }

    public static Map<String, String> parseMap(String text) {
        return QPARSER.parseMap(text);
    }
}

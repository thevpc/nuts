package net.vpc.app.nuts.core.util.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StringKeyValueList implements Iterable<StringKeyValue>{
    private List<StringKeyValue> values = new ArrayList<>();

    public void add(String key, String value) {
        values.add(new StringKeyValue(key, value));
    }

    public void add(Map<String, String> map) {
        for (Map.Entry<String, String> stringStringEntry : map.entrySet()) {
            add(stringStringEntry.getKey(), stringStringEntry.getValue());
        }
    }

    @Override
    public Iterator<StringKeyValue> iterator() {
        return values.iterator();
    }
}

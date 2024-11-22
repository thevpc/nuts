package net.thevpc.nuts.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NStringKeyValueList implements Iterable<NStringKeyValue> {

    private List<NStringKeyValue> values = new ArrayList<>();

    public void add(String key, String value) {
        values.add(new NStringKeyValue(key, value));
    }

    public void add(Map<String, String> map) {
        for (Map.Entry<String, String> stringStringEntry : map.entrySet()) {
            add(stringStringEntry.getKey(), stringStringEntry.getValue());
        }
    }

    @Override
    public Iterator<NStringKeyValue> iterator() {
        return values.iterator();
    }
}

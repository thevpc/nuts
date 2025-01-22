package net.thevpc.nuts.runtime.standalone.web;

import net.thevpc.nuts.util.NCaseInsensitiveStringMap;
import net.thevpc.nuts.util.NMaps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultNWebHeaders {
    private Map<String, List<String>> headers = NMaps.ofCaseInsensitiveKeyMap();

    public void addHeaders(DefaultNWebHeaders others, Mode mode) {
        if (others != null) {
            addHeadersMulti(others.headers, mode);
        }
    }

    public void addHeaders(Map<String, String> others, Mode mode) {
        if (others != null) {
            for (Map.Entry<String, String> e : others.entrySet()) {
                addHeader(e.getKey(), e.getValue(), mode);
            }
        }
    }

    public void addHeadersMulti(Map<String, List<String>> others, Mode mode) {
        if (others != null) {
            for (Map.Entry<String, List<String>> e : others.entrySet()) {
                List<String> eList = e.getValue();
                if (eList != null && !eList.isEmpty()) {
                    switch (mode == null ? Mode.ALWAYS : mode) {
                        case IF_EMPTY: {
                            List<String> l = headers.get(e.getKey());
                            if (l == null || l.isEmpty()) {
                                for (String h : eList) {
                                    addHeader(e.getKey(), h, Mode.ALWAYS);
                                }
                            }
                            break;
                        }
                        case REPLACE: {
                            List<String> l = headers.get(e.getKey());
                            if (l != null) {
                                l.clear();
                            }
                            for (String h : eList) {
                                addHeader(e.getKey(), h, Mode.ALWAYS);
                            }
                            break;
                        }
                        default: {
                            for (String h : eList) {
                                addHeader(e.getKey(), h, mode);
                            }
                        }
                    }
                }
            }
        }
    }

    public void removeHeader(String name) {
        headers.remove(name);
    }

    public void removeHeader(String name, String value) {
        if (value == null) {
            return;
        }
        List<String> l = headers.get(name);
        if (l != null) {
            l.remove(value);
            if (l.isEmpty()) {
                headers.remove(name);
            }
        }
    }

    public void addHeader(String name, String value, Mode mode) {
        if (name == null) {
            return;
        }
        if (value == null) {
            return;
        }
        List<String> l = headers.get(name);
        if (l == null) {
            l = new ArrayList<>();
            headers.put(name, l);
            l.add(value);
        } else {
            switch (mode == null ? Mode.ALWAYS : mode) {
                case ALWAYS: {
                    l.add(value);
                    break;
                }
                case REPLACE: {
                    l.clear();
                    l.add(value);
                    break;
                }
                case IF_EMPTY: {
                    if (l.isEmpty()) {
                        l.add(value);
                    }
                    break;
                }
                case NO_DUPLICATES: {
                    if (!l.contains(value)) {
                        l.add(value);
                    }
                }
            }
        }
    }

    public void addHeaderIfNotExists(String name, String value) {
        if (value == null) {
            return;
        }
        List<String> l = headers.get(name);
        if (l == null) {
            l = new ArrayList<>();
            headers.put(name, l);
            l.add(value);
        } else {
            if (!l.contains(value)) {
                l.add(value);
            }
        }
    }

    public String getFirst(String name) {
        List<String> u = headers.get(name);
        if (u != null) {
            return u.get(0);
        }
        return null;
    }

    public List<String> getOrEmpty(String name) {
        List<String> u = headers.get(name);
        if (u == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(u);
        }
    }

    public List<String> getOrNull(String name) {
        List<String> u = headers.get(name);
        if (u == null) {
            return null;
        } else if (headers.isEmpty()) {
            return null;
        }
        return new ArrayList<>(u);
    }

    public Map<String, List<String>> toMap() {
        Map<String, List<String>> headers2 = NMaps.ofCaseInsensitiveKeyMap();
        for (Map.Entry<String, List<String>> e : this.headers.entrySet()) {
            headers2.put(e.getKey(), new ArrayList<>(e.getValue()));
        }
        return headers2;
    }

    public void clear() {
        headers.clear();
    }

    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    public enum Mode {
        ALWAYS,
        NO_DUPLICATES,
        REPLACE,
        IF_EMPTY
    }
}

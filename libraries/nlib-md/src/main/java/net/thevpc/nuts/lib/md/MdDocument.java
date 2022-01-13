package net.thevpc.nuts.lib.md;

import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MdDocument {
    private Object id;
    private String title;
    private String version;

    private String subTitle;
    private Temporal date;
    private Map<String,Object> properties;
    private MdElement content;

    public MdDocument(Object id,String title, String subTitle,String version, Temporal date, Map<String, Object> properties, MdElement content) {
        this.id = id;
        this.title = title;
        this.subTitle = subTitle;
        this.version = version;
        this.date = date;
        this.properties = Collections.unmodifiableMap(properties==null?new HashMap<>() : new LinkedHashMap<>(properties));
        this.content = content;
    }

    public Object getId() {
        return id;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public MdElement getContent() {
        return content;
    }

    public String getTitle() {
        return title;
    }

    public String getVersion() {
        return version;
    }

    public Temporal getDate() {
        return date;
    }

    public Object getProperty(String k) {
        return properties.get(k);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}

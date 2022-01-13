package net.thevpc.nuts.lib.md;

import java.time.temporal.Temporal;
import java.util.LinkedHashMap;
import java.util.Map;

public class MdDocumentBuilder {
    private Object id;
    private String title;
    private String subTitle;
    private String version;
    private Temporal date;
    private Map<String,Object> properties;
    private MdElement content;

    public Object getId() {
        return id;
    }

    public MdDocumentBuilder setId(Object id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public MdDocumentBuilder setSubTitle(String subTitle) {
        this.subTitle = subTitle;
        return this;
    }

    public MdDocumentBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public MdDocumentBuilder setVersion(String version) {
        this.version = version;
        return this;
    }

    public Temporal getDate() {
        return date;
    }

    public MdDocumentBuilder setDate(Temporal date) {
        this.date = date;
        return this;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public MdDocumentBuilder addProperties(Map<String, Object> properties) {
        if(properties!=null){
           if(this.properties==null){
               this.properties=new LinkedHashMap<>();
           }
           this.properties.putAll(properties);
        }
        return this;
    }

    public MdDocumentBuilder setProperty(String key,Object value) {
        if(value!=null){
           if(this.properties==null){
               this.properties=new LinkedHashMap<>();
           }
           this.properties.put(key,value);
        }else{
            this.properties.remove(key);
        }
        return this;
    }

    public MdDocumentBuilder setProperties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    public MdElement getContent() {
        return content;
    }

    public MdDocumentBuilder setContent(MdElement content) {
        this.content=content;
        return this;
    }

    public MdDocument build(){
        return new MdDocument(
                id,title,subTitle, version, date, properties, content
        );
    }
}

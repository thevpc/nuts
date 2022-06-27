package net.thevpc.nuts.toolbox.noapi.model;

public class TypeCrossRef {
    private String url;
    private String location;
    private String type;

    public TypeCrossRef(String type, String url, String location) {
        this.setUrl(url);
        this.setLocation(location);
        this.setType(type);
    }

    public String getUrl() {
        return url;
    }

    public TypeCrossRef setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public TypeCrossRef setLocation(String location) {
        this.location = location;
        return this;
    }

    public String getType() {
        return type;
    }

    public TypeCrossRef setType(String type) {
        this.type = type;
        return this;
    }
}

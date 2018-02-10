package net.vpc.app.nuts;

public class TypedObject {

    private Class type;
    private String variant;
    private Object value;

    public TypedObject(Class type, Object value, String variant) {
        this.type = type;
        this.value = value;
        this.variant = variant;
    }

    public String getVariant() {
        return variant;
    }

    public Class getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }
}

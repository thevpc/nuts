package net.thevpc.nuts.toolbox.noapi.model;

import java.util.ArrayList;
import java.util.List;

public class TypeInfo {
    private String name;
    private String description;
    private String summary;
    private String type;
    private String userType;
    private String format;
    private String minLength;
    private String maxLength;
    private String ref;
    private String refLong;
    private List<String> enumValues;
    private Object example;
    private List<FieldInfo> fields=new ArrayList<>();

    public String getName() {
        return name;
    }

    public TypeInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public TypeInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public TypeInfo setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public String getType() {
        return type;
    }

    public TypeInfo setType(String type) {
        this.type = type;
        return this;
    }

    public String getUserType() {
        return userType;
    }

    public TypeInfo setUserType(String userType) {
        this.userType = userType;
        return this;
    }

    public String getFormat() {
        return format;
    }

    public TypeInfo setFormat(String format) {
        this.format = format;
        return this;
    }

    public String getMinLength() {
        return minLength;
    }

    public TypeInfo setMinLength(String minLength) {
        this.minLength = minLength;
        return this;
    }

    public String getMaxLength() {
        return maxLength;
    }

    public TypeInfo setMaxLength(String maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    public String getRef() {
        return ref;
    }

    public TypeInfo setRef(String ref) {
        this.ref = ref;
        return this;
    }

    public String getRefLong() {
        return refLong;
    }

    public TypeInfo setRefLong(String refLong) {
        this.refLong = refLong;
        return this;
    }

    public List<String> getEnumValues() {
        return enumValues;
    }

    public TypeInfo setEnumValues(List<String> enumValues) {
        this.enumValues = enumValues;
        return this;
    }

    public Object getExample() {
        return example;
    }

    public TypeInfo setExample(Object example) {
        this.example = example;
        return this;
    }

    public List<FieldInfo> getFields() {
        return fields;
    }

    public TypeInfo setFields(List<FieldInfo> fields) {
        this.fields = fields;
        return this;
    }
}

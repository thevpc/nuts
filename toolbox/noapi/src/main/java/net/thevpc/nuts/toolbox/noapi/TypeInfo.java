package net.thevpc.nuts.toolbox.noapi;

import java.util.ArrayList;
import java.util.List;

class TypeInfo {
    public String name;
    public String description;
    public String summary;
    public String type;
    public String userType;
    public String format;
    public String minLength;
    public String maxLength;
    public String ref;
    public String refLong;
    public List<String> enumValues;
    public Object example;
    public List<FieldInfo> fields=new ArrayList<>();
}

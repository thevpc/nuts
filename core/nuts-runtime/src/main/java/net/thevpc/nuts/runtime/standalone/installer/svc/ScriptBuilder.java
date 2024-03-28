package net.thevpc.nuts.runtime.standalone.installer.svc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ScriptBuilder {
    String name;
    String description;
    StringBuffer sb = new StringBuffer();

    public ScriptBuilder(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public ScriptBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ScriptBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public ScriptBuilder printlnComment(String s) {
        for (String s1 : SvcHelper.splitLines(s)) {
            println("# " + s1);
        }
        return this;
    }

    public ScriptBuilder println(String s) {
        sb.append(s);
        sb.append(System.getProperty("line.separator"));
        return this;
    }

    public ScriptBuilder printlnEcho(String s) {
        if(s!=null) {
            println("echo " + s);
            println(s);
        }
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    public List<String> lines() {
        return new ArrayList<>(Arrays.asList(sb.toString().split("\n")));
    }
}

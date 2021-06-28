/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

/**
 *
 * @author thevpc
 */
public class JShellCommandType {

    private String name;
    private String type;
    private String value;
    private String description;

    public JShellCommandType(String name, String type, String value, String description) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

}

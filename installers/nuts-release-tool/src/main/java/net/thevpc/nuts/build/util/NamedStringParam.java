/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.build.util;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.build.NutsBuildRunnerContext;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

/**
 *
 * @author vpc
 */
public class NamedStringParam {

    private final String name;
    private String value;

    public NamedStringParam(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public NamedStringParam ensureDirectory() {
        ensureNonBlank();
        if (!NPath.of(value).isDirectory()) {
            throw new NIllegalArgumentException(NMsg.ofC("not a valid folder %s : %s", getName(), getValue()));
        }
        return this;
    }

    public NamedStringParam ensureRegularFile() {
        ensureNonBlank();
        if (!NPath.of(value).isRegularFile()) {
            throw new NIllegalArgumentException(NMsg.ofC("not a valid folder %s : %s", getName(), getValue()));
        }
        return this;
    }

    public NamedStringParam ensureNonBlank() {
        if (NBlankable.isBlank(getValue())) {
            throw new NIllegalArgumentException(NMsg.ofC("missing %s", getName()));
        }
        return this;
    }

    public NamedStringParam update(NutsBuildRunnerContext context) {
        if (NBlankable.isBlank(getValue())) {
            String v = context.vars.get(getName());
            if (!NBlankable.isBlank(v)) {
                setValue(v);
            }
        }
        return this;
    }
}

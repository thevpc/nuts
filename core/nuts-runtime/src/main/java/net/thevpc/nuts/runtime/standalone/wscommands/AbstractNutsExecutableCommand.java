/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands;

import net.thevpc.nuts.runtime.core.commands.ws.NutsExecutableInformationExt;
import net.thevpc.nuts.NutsExecutableType;

/**
 *
 * @author thevpc
 */
public abstract class AbstractNutsExecutableCommand implements NutsExecutableInformationExt {

    protected NutsExecutableType type;
    protected String name;
    protected String value;

    public AbstractNutsExecutableCommand(String name, NutsExecutableType type) {
        this.type = type;
        this.name = name;
    }

    public AbstractNutsExecutableCommand(String name, String value, NutsExecutableType type) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NutsExecutableType getType() {
        return type;
    }

    @Override
    public String getDescription() {
        return toString();
    }

    @Override
    public String getHelpText() {
        return "No help available. Try '" + getName() + " --help'";
    }

}

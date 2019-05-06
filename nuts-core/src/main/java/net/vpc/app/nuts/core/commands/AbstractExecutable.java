/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.commands;

import net.vpc.app.nuts.NutsExecutableType;

/**
 *
 * @author vpc
 */
public abstract class AbstractExecutable implements NutsExecutableImpl {
    protected NutsExecutableType type;
    protected String name;

    public AbstractExecutable(String name, NutsExecutableType type) {
        this.type = type;
        this.name = name;
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

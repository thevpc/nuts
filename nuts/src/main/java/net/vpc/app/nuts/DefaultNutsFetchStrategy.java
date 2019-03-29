/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

/**
 *
 * @author vpc
 */
public class DefaultNutsFetchStrategy implements NutsFetchStrategy {
    
    private String name;
    private boolean stopFast;
    private NutsFetchMode[] all;

    public DefaultNutsFetchStrategy(String name, boolean stopFast, NutsFetchMode... all) {
        this.name = name;
        this.stopFast = stopFast;
        this.all = Arrays.copyOf(all, all.length);
    }

    @Override
    public boolean isStopFast() {
        return stopFast;
    }

    public String name() {
        return name;
    }

    public NutsFetchMode[] modes() {
        return Arrays.copyOf(all, all.length);
    }

    @Override
    public Iterator<NutsFetchMode> iterator() {
        return Arrays.asList(all).iterator();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.name());
        hash = 97 * hash + (this.isStopFast() ? 1 : 0);
        for (NutsFetchMode o : this) {
            hash = 97 * hash + o.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultNutsFetchStrategy other = (DefaultNutsFetchStrategy) obj;
        if (this.isStopFast() != other.isStopFast()) {
            return false;
        }
        if (!Objects.equals(this.name, other.name())) {
            return false;
        }
        if (!Arrays.deepEquals(this.all, other.modes())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DefaultNutsFetchModeFiltrer{" + "name=" + name + '}';
    }
    
}

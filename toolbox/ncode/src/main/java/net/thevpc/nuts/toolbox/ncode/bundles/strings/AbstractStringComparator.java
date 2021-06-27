/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ncode.bundles.strings;

import java.util.Objects;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public abstract class AbstractStringComparator implements StringComparator {

    protected StringTransform transform;

    public StringTransform getTransform() {
        return transform;
    }

    abstract AbstractStringComparator copy();

    public StringComparator resetTransform() {
        if (this.transform != null) {
            AbstractStringComparator x = copy();
            x.transform = null;
            return x;
        }
        return this;
    }

    public StringComparator apply(StringTransform tranform) {
        if (this.transform == null) {
            AbstractStringComparator x = copy();
            x.transform = tranform;
            return x;
        } else {
            AbstractStringComparator x = copy();
            x.transform = this.transform.apply(tranform);
            return x;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.transform);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractStringComparator other = (AbstractStringComparator) obj;
        if (!Objects.equals(this.transform, other.transform)) {
            return false;
        }
        return true;
    }
    
}

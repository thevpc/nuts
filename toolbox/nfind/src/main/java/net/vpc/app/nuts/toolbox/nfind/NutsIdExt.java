/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nfind;

import java.util.Objects;
import net.vpc.app.nuts.NutsId;

/**
 *
 * @author vpc
 */
class NutsIdExt implements Comparable<NutsIdExt> {
    
    public NutsId id;
    public String extra;

    public NutsIdExt(NutsId id, String extra) {
        this.id = id;
        this.extra = extra;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NutsIdExt nutsIdExt = (NutsIdExt) o;
        return Objects.equals(id, nutsIdExt.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(NutsIdExt o) {
        int x = this.id.getSimpleName().compareTo(o.id.getSimpleName());
        if (x != 0) {
            return x;
        }
        x = -this.id.getVersion().compareTo(o.id.getVersion());
        return x;
    }
    
}

package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.NutsId;

import java.util.Objects;

class NutsIdExt implements Comparable<NutsIdExt>{
    NutsId id;
    String extra;

    public NutsIdExt(NutsId id, String extra) {
        this.id = id;
        this.extra = extra;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsIdExt nutsIdExt = (NutsIdExt) o;
        return Objects.equals(id, nutsIdExt.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }

    @Override
    public int compareTo(NutsIdExt o) {
        int x = this.id.getFullName().compareTo(o.id.getFullName());
        if(x!=0){
            return x;
        }
        x = - this.id.getVersion().compareTo(o.id.getVersion());
        return x;
    }
}

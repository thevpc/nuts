package net.vpc.app.nuts.extensions.cmd;

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
        return this.id.toString().compareTo(o.id.toString());
    }
}

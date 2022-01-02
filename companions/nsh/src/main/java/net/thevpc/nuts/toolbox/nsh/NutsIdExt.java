package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.nuts.NutsId;

import java.util.Objects;

public class NutsIdExt implements Comparable<NutsIdExt> {

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
        int x = this.id.getShortName().compareTo(o.id.getShortName());
        if (x != 0) {
            return x;
        }
        x = -this.id.getVersion().compareTo(o.id.getVersion());
        return x;
    }
}

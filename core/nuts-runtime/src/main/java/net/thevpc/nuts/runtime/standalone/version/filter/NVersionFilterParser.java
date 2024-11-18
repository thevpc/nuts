package net.thevpc.nuts.runtime.standalone.version.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.base.AbstractVersionFilter;
import net.thevpc.nuts.runtime.standalone.util.filters.NTypedFiltersParser;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NFilterOp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NVersionFilterParser extends NTypedFiltersParser<NVersionFilter> {
    public NVersionFilterParser(String str, NWorkspace workspace) {
        super(str, workspace);
    }

    @Override
    protected NVersionFilters getTManager() {
        return NVersionFilters.of();
    }

    protected NVersionFilter wordToPredicate(String word) {
        switch (word.toLowerCase()) {
            default: {
                return super.wordToPredicate(word);
            }
        }
    }

    private NVersion asVersion() {
        String s = str.getContent().trim();
        if (s.isEmpty()) {
            return null;
        }
        NVersion e = NVersion.of(s).orNull();
        if (e != null) {
            switch (e.getValue()) {
                case "true":
                case "false":
                    return null;
            }
            return e;
        }
        return null;
    }

    public NVersionFilter parse() {
        if (NBlankable.isBlank(str.getContent())) {
            return new NVersionFilterTrue(workspace);
        }
        NVersion v = asVersion();
        if (v != null) {
            List<NVersionInterval> intervals = v.intervals().orNull();
            if (intervals != null && intervals.size() > 0) {
                return new NVersionIntervalsVersionFilter(workspace, v);
            }
        }
        return super.parse();
    }

    private class NVersionIntervalsVersionFilter extends AbstractVersionFilter {
        private final NVersion version;

        public NVersionIntervalsVersionFilter(NWorkspace workspace,NVersion version) {
            super(workspace, NFilterOp.CUSTOM);
            this.version = version;
        }

        @Override
        public NVersionFilter simplify() {
            return this;
        }

        @Override
        public boolean acceptVersion(NVersion version) {
            for (NVersionInterval i : version.intervals().orElse(new ArrayList<>())) {
                if (i.acceptVersion(version)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            List<NVersionInterval> intervals = version.intervals().orElse(new ArrayList<>());
            StringBuffer sb=new StringBuffer();
            for (int i = 0; i < intervals.size(); i++) {
                if(i>0){
                    sb.append(", ");
                }
                sb.append(intervals.get(i));
            }
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NVersionIntervalsVersionFilter that = (NVersionIntervalsVersionFilter) o;
            return Objects.equals(version, that.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(version);
        }
    }
}

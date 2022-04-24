package net.thevpc.nuts.runtime.standalone.version.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.NutsTypedFiltersParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NutsVersionFilterParser extends NutsTypedFiltersParser<NutsVersionFilter> {
    public NutsVersionFilterParser(String str, NutsSession session) {
        super(str, session);
    }

    @Override
    protected NutsVersionFilters getTManager() {
        return NutsVersionFilters.of(getSession());
    }

    protected NutsVersionFilter wordToPredicate(String word) {
        switch (word.toLowerCase()) {
            default: {
                return super.wordToPredicate(word);
            }
        }
    }

    private NutsVersion asVersion() {
        String s = str.getContent().trim();
        if (s.isEmpty()) {
            return null;
        }
        NutsVersion e = NutsVersion.of(s).orNull();
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

    public NutsVersionFilter parse() {
        if (NutsBlankable.isBlank(str.getContent())) {
            return new NutsVersionFilterTrue(getSession());
        }
        NutsVersion v = asVersion();
        if (v != null) {
            List<NutsVersionInterval> intervals = v.intervals().orNull();
            if (intervals != null && intervals.size() > 0) {
                return new NutsVersionIntervalsVersionFilter(v);
            }
        }
        return super.parse();
    }

    private class NutsVersionIntervalsVersionFilter extends AbstractVersionFilter {
        private final NutsVersion version;

        public NutsVersionIntervalsVersionFilter(NutsVersion version) {
            super(NutsVersionFilterParser.this.getSession(), NutsFilterOp.CUSTOM);
            this.version = version;
        }

        @Override
        public NutsVersionFilter simplify() {
            return this;
        }

        @Override
        public boolean acceptVersion(NutsVersion version, NutsSession session) {
            for (NutsVersionInterval i : version.intervals().orElse(new ArrayList<>())) {
                if (i.acceptVersion(version)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            List<NutsVersionInterval> intervals = version.intervals().orElse(new ArrayList<>());
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
            NutsVersionIntervalsVersionFilter that = (NutsVersionIntervalsVersionFilter) o;
            return Objects.equals(version, that.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(version);
        }
    }
}

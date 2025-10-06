package net.thevpc.nuts.runtime.standalone.version.filter;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.spi.base.AbstractVersionFilter;
import net.thevpc.nuts.runtime.standalone.util.filters.NTypedFiltersParser;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NFilterOp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NVersionFilterParser extends NTypedFiltersParser<NVersionFilter> {
    private NVersionComparator comparator;
    public NVersionFilterParser(String str, NVersionComparator comparator) {
        super(str);
        this.comparator=comparator;
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
        NVersion e = NVersion.get(s).orNull();
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
            return new NVersionFilterTrue();
        }
        NVersion v = asVersion();
        if (v != null) {
            List<NVersionInterval> intervals = v.intervals().orNull();
            if (intervals != null && intervals.size() > 0) {
                return new NVersionIntervalsVersionFilter(v,comparator);
            }
        }
        return super.parse();
    }

    private class NVersionIntervalsVersionFilter extends AbstractVersionFilter {
        private final NVersion version;
        private final NVersionComparator versionComparator;

        public NVersionIntervalsVersionFilter(NVersion version,NVersionComparator versionComparator) {
            super(NFilterOp.CUSTOM);
            this.version = version;
            this.versionComparator = versionComparator;
        }

        @Override
        public NVersionFilter simplify() {
            return this;
        }

        @Override
        public boolean acceptVersion(NVersion version) {
            for (NVersionInterval i : version.intervals(versionComparator).orElse(new ArrayList<>())) {
                if (i.acceptVersion(version)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            List<NVersionInterval> intervals = version.intervals(versionComparator).orElse(new ArrayList<>());
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

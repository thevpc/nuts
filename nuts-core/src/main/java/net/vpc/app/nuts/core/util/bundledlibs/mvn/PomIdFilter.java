package net.vpc.app.nuts.core.util.bundledlibs.mvn;

public interface PomIdFilter {
    boolean accept(PomId id);
}

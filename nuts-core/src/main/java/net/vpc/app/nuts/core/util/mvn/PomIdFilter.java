package net.vpc.app.nuts.core.util.mvn;

public interface PomIdFilter {
    boolean accept(PomId id);
}

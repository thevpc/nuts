package net.vpc.app.nuts.core.bridges.maven.mvnutil;

public interface PomIdFilter {

    boolean accept(PomId id);
}

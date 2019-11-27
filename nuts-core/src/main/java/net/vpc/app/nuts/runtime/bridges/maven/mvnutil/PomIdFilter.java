package net.vpc.app.nuts.runtime.bridges.maven.mvnutil;

public interface PomIdFilter {

    boolean accept(PomId id);
}

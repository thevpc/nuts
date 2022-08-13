package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

public interface PomIdFilter {

    boolean accept(NutsPomId id);
}

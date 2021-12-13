package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom;

public interface PomIdFilter {

    boolean accept(PomId id);
}

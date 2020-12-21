package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.NutsAuthenticationAgent;
import net.thevpc.nuts.NutsSession;

public interface NutsAuthenticationAgentProvider {
    NutsAuthenticationAgent create(String name, NutsSession session);
}

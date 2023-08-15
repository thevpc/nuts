package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.security.NAuthenticationAgent;
import net.thevpc.nuts.NSession;

public interface NAuthenticationAgentProvider {
    NAuthenticationAgent create(String name, NSession session);
}

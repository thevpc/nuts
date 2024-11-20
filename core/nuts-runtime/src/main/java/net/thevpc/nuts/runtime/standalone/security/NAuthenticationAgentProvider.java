package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.security.NAuthenticationAgent;

public interface NAuthenticationAgentProvider {
    NAuthenticationAgent create(String name);
}

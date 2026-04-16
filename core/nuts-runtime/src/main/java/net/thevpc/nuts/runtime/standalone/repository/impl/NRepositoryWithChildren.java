package net.thevpc.nuts.runtime.standalone.repository.impl;

import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.util.NOptional;

public interface NRepositoryWithChildren {
    NOptional<NRepository> getChild(String repositoryNameOrId);
}

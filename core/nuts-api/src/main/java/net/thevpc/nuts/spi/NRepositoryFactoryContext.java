package net.thevpc.nuts.spi;

import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.core.NRepositoryConfig;
import net.thevpc.nuts.core.NRepositorySpec;
import net.thevpc.nuts.core.NRepositoryModel;

public interface NRepositoryFactoryContext {
    NRepositorySpec spec();
    NRepositoryConfig config();
    String repositoryType();
    NRepository parentRepository();
    NRepository createDefaultRepository(NRepositoryModel model);
}

package net.thevpc.nuts.spi;

import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.core.NRepositoryConfig;
import net.thevpc.nuts.core.NRepositorySpec;
import net.thevpc.nuts.core.NRepositoryModel;

/**
 * NRepositoryFactoryContext interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NRepositoryFactoryContext {
    /**
     * Spec.
     *
     * @return spec result
     */
    NRepositorySpec spec();
    /**
     * Config.
     *
     * @return config result
     */
    NRepositoryConfig config();
    /**
     * Repository type.
     *
     * @return repository type result
     */
    String repositoryType();
    /**
     * Parent repository.
     *
     * @return parent repository result
     */
    NRepository parentRepository();
    /**
     * Creates a new instance of create default repository.
     *
     * @param model model
     * @return create default repository result
     */
    NRepository createDefaultRepository(NRepositoryModel model);
}

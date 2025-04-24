package net.thevpc.nuts.runtime.standalone.definition;

import net.thevpc.nuts.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenRepositoryFolderHelper;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.xtra.time.NLazySupplier;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.UncheckedException;

import javax.management.Descriptor;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;

public class NDefinitionHelper {

    public static NDefinition ofDefinition(NId id) {
        return new LazyLoadingNDefinition(id);
    }

    public static NDefinition ofIdOnlyFromRepo(NId id, NRepository repo,String callerName) {
        NRepositorySPI repoSPI = NWorkspaceUtils.of(repo.getWorkspace()).repoSPI(repo);
        return ofIdAndLazyDescriptor(id,()->repoSPI.fetchDescriptor().setId(id).getResult(),callerName);
    }

//    public static NDefinition ofIdOnly(NId id, String callerName) {
//        return new DefinitionForIdOnly(id, callerName);
//    }

    public static NDefinition ofIdAndLazyDescriptor(NId id, Supplier<NDescriptor> descriptorSupplier,String callerName) {
        return new DefinitionForIdAndLazyDescriptor(id, descriptorSupplier,callerName);
    }

    public static NDefinition ofDescriptorOnly(NId id, NDescriptor descriptor) {
        return new DefinitionForIdAndDescriptor(id, descriptor);
    }

    public static NDefinition ofDescriptorOnly(NDescriptor descriptor) {
        return new DefinitionForIdAndDescriptor(descriptor.getId(), descriptor);
    }

    private static class DefinitionForIdAndDescriptor extends NDefinitionDelegate {
        private final NId id;
        private final NDescriptor descriptor;

        public DefinitionForIdAndDescriptor(NId id, NDescriptor descriptor) {
            this.id = id;
            this.descriptor = descriptor;
        }

        @Override
        protected NDefinition getBase() {
            throw new IllegalStateException("You are not allowed to load definition");
        }

        @Override
        public NId getId() {
            return id;
        }

        @Override
        public NDescriptor getDescriptor() {
            return descriptor;
        }

        @Override
        public NOptional<NDescriptor> getEffectiveDescriptor() {
            return NOptional.of(getDescriptor());
        }

        @Override
        public NOptional<Set<NDescriptorFlag>> getEffectiveFlags() {
            return getEffectiveDescriptor().map(x -> x.getFlags());
        }
    }

    private static class DefinitionForIdOnly extends NDefinitionDelegate {
        private final NId id;
        private final String caller;

        public DefinitionForIdOnly(NId id, String caller) {
            this.id = id;
            this.caller = caller;
        }

        @Override
        protected NDefinition getBase() {
            throw new IllegalStateException("You are not allowed to load definition");
        }

        @Override
        public NId getId() {
            return id;
        }
    }

    private static class DefinitionForIdAndLazyDescriptor extends NDefinitionDelegate {
        private final NId id;
        private final NLazySupplier<NDescriptor> descriptor;
        private final String caller;

        public DefinitionForIdAndLazyDescriptor(NId id, Supplier<NDescriptor> descriptor, String caller) {
            this.id = id;
            this.caller = caller;
            this.descriptor = new NLazySupplier<>(descriptor);
        }

        @Override
        public NDescriptor getDescriptor() {
            return descriptor.get();
        }

        @Override
        protected NDefinition getBase() {
            throw new IllegalStateException("You are not allowed to load definition");
        }

        @Override
        public NId getId() {
            return id;
        }
    }

    private static class LazyLoadingNDefinition extends NDefinitionDelegate {
        NLog LOG = NLog.of(MavenRepositoryFolderHelper.class);
        NDefinition definition;
        private final NId id;
        boolean loaded;
        RuntimeException replayException;

        public LazyLoadingNDefinition(NId id) {
            this.id = id;
        }

        @Override
        public NId getId() {
            return id;
        }

        protected NDefinition getBase() {
            if (!loaded) {
                if (LOG == null) {
                    LOG = NLog.of(MavenRepositoryFolderHelper.class);
                }
                loaded = true;
                try {
//                descriptor = repository.fetchDescriptor().setId(id).setSession(session).getResult();
                    definition = NFetchCmd.of(id)
                            .setDependencyFilter(NDependencyFilters.of().byRunnable())
                            .getResultDefinition();
                } catch (Exception ex) {
                    //suppose we cannot retrieve descriptor
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.with().level(Level.FINER).verb(NLogVerb.FAIL)
                                .log(
                                        NMsg.ofC("unable to fetch descriptor for %s : %s",
                                                id, ex)
                                );
                    }
                    if (!(ex instanceof RuntimeException)) {
                        ex = new UncheckedException(ex);
                    }
                    replayException = (RuntimeException) ex;
                }
            }
            if (replayException != null) {
                throw replayException;
            }
            return definition;
        }
    }


}

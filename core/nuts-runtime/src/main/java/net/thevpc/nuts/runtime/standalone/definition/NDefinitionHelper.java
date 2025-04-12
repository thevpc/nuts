package net.thevpc.nuts.runtime.standalone.definition;

import net.thevpc.nuts.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.runtime.standalone.definition.filter.NDefinitionFilterAnd;
import net.thevpc.nuts.runtime.standalone.definition.filter.NDefinitionFilterOr;
import net.thevpc.nuts.runtime.standalone.definition.filter.NInstallStatusDefinitionFilter2;
import net.thevpc.nuts.runtime.standalone.definition.filter.NPatternDefinitionFilter;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenRepositoryFolderHelper;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.UncheckedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class NDefinitionHelper {
    public static NOptional<NDefinitionFilter[]> toAndChildren(NDefinitionFilter id) {
        if (id instanceof NDefinitionFilterAnd) {
            return NOptional.of(((NDefinitionFilterAnd) id).getChildren());
        }
        return NOptional.ofEmpty();
    }

    public static NOptional<NDefinitionFilter[]> toOrChildren(NDefinitionFilter id) {
        if (id instanceof NDefinitionFilterOr) {
            return NOptional.of(((NDefinitionFilterOr) id).getChildren());
        }
        return NOptional.ofEmpty();
    }

    public static NOptional<NId> toPatternId(NDefinitionFilter id) {
        if (id instanceof NPatternDefinitionFilter) {
            return NOptional.of(((NPatternDefinitionFilter) id).getId());
        }
        return NOptional.ofEmpty();
    }

    public static NDefinitionFilterToNIdPredicate2 toIdPredicate(NDefinitionFilter filter) {
        return new NDefinitionFilterToNIdPredicate2(filter);
    }

    public static NDefinition ofDefinition(NId id) {
        return new LazyLoadingNDefinition(id);
    }

    public static NDefinition ofIdOnly(NId id) {
        return new DefinitionForIdOnly(id);
    }

    public static NDefinition ofDescriptorOnly(NId id, NDescriptor descriptor) {
        return new DefinitionForIdAndDescriptor(id, descriptor);
    }

    public static NDefinition ofDescriptorOnly(NDescriptor descriptor) {
        return new DefinitionForIdAndDescriptor(descriptor.getId(), descriptor);
    }

    public static NPatternDefinitionFilter[] asPatternDefinitionFilterOrList(NDefinitionFilter defFilter0) {
        if (defFilter0 == null) {
            return new NPatternDefinitionFilter[0];
        }
        List<NPatternDefinitionFilter> orResult = new ArrayList<>();
        if (defFilter0 instanceof NDefinitionFilterOr) {
            for (NDefinitionFilter child : ((NDefinitionFilterOr) defFilter0).getChildren()) {
                if (child instanceof NPatternDefinitionFilter) {
                    orResult.add((NPatternDefinitionFilter) child);
                }
            }
        } else if (defFilter0 instanceof NDefinitionFilterAnd) {
            for (NDefinitionFilter child : ((NDefinitionFilterAnd) defFilter0).getChildren()) {
                NPatternDefinitionFilter[] found = asPatternDefinitionFilterOrList(child);
                if (found.length > 0) {
                    if (orResult.isEmpty()) {
                        orResult.addAll(Arrays.asList(found));
                    } else {
                        // Too complex
                        return new NPatternDefinitionFilter[0];
                    }
                }
            }
        } else if (defFilter0 instanceof NPatternDefinitionFilter) {
            orResult.add((NPatternDefinitionFilter) defFilter0);
        }
        return orResult.toArray(new NPatternDefinitionFilter[0]);
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

        public DefinitionForIdOnly(NId id) {
            this.id = id;
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
                    definition = NFetchCmd.of(id).getResultDefinition();

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


    public static boolean isAlways(NDefinitionFilter any) {
        return any == null || any.getFilterOp() == NFilterOp.TRUE;
    }

    public static boolean isNever(NDefinitionFilter any) {
        return any != null && any.getFilterOp() == NFilterOp.FALSE;
    }

    public static NOptional<Boolean> resolveInstalled(NDefinitionFilter any) {
        for (NDefinitionFilter d : flattenAnd(any)) {
            if (d instanceof NInstallStatusDefinitionFilter2) {
                NInstallStatusDefinitionFilter2 v = (NInstallStatusDefinitionFilter2) d;
                switch (v.getName()) {
                    case "installed": {
                        return NOptional.of(v.isValue());
                    }
                }
            }
        }
        return NOptional.ofNamedEmpty("installed");
    }

    public static NOptional<Boolean> resolveRequired(NDefinitionFilter any) {
        for (NDefinitionFilter d : flattenAnd(any)) {
            if (d instanceof NInstallStatusDefinitionFilter2) {
                NInstallStatusDefinitionFilter2 v = (NInstallStatusDefinitionFilter2) d;
                switch (v.getName()) {
                    case "required": {
                        return NOptional.of(v.isValue());
                    }
                }
            }
        }
        return NOptional.ofNamedEmpty("required");
    }

    public static NOptional<Boolean> resolveDeployed(NDefinitionFilter any) {
        for (NDefinitionFilter d : flattenAnd(any)) {
            if (d instanceof NInstallStatusDefinitionFilter2) {
                NInstallStatusDefinitionFilter2 v = (NInstallStatusDefinitionFilter2) d;
                switch (v.getName()) {
                    case "deployed": {
                        return NOptional.of(v.isValue());
                    }
                }
            }
        }
        return NOptional.ofNamedEmpty("deployed");
    }

    public static NDefinitionFilter[] flattenAnd(NDefinitionFilter any) {
        if (any == null) {
            return new NDefinitionFilter[]{NDefinitionFilters.of().always()};
        }
        any = (NDefinitionFilter) any.simplify();
        if (any == null) {
            return new NDefinitionFilter[]{NDefinitionFilters.of().always()};
        }
        if (any instanceof NDefinitionFilterAnd) {
            return ((NDefinitionFilterAnd) any).getChildren();
        }
        return new NDefinitionFilter[]{any};
    }

}

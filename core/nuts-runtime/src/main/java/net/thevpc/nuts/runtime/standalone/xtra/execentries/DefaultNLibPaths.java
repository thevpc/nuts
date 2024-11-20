package net.thevpc.nuts.runtime.standalone.xtra.execentries;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.NPomXmlParser;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomId;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaJarUtils;
import net.thevpc.nuts.runtime.standalone.xtra.idresolver.NMetaInfIdResolver;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DefaultNLibPaths implements NLibPaths {
    private final NWorkspace workspace;

    public DefaultNLibPaths(NWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public List<NExecutionEntry> parseExecutionEntries(NPath file) {
        if (file.getName().toLowerCase().endsWith(".jar")) {
            try {
                try (InputStream in = file.getInputStream()) {
                    return parseExecutionEntries(in, "jar", file.toAbsolute().normalize().toString());
                }
            } catch (IOException ex) {
                throw new NIOException(ex);
            }
        } else if (file.getName().toLowerCase().endsWith(".class")) {
            try {
                try (InputStream in = file.getInputStream()) {
                    return parseExecutionEntries(in, "class", file.toAbsolute().normalize().toString());
                }
            } catch (IOException ex) {
                throw new NIOException(ex);
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<NExecutionEntry> parseExecutionEntries(InputStream inputStream, String type, String sourceName) {
        if ("jar".equals(type)) {
            return JavaJarUtils.parseJarExecutionEntries(inputStream);
        } else if ("class".equals(type)) {
            NExecutionEntry u = JavaClassUtils.parseClassExecutionEntry(inputStream, sourceName);
            return u == null ? Collections.emptyList() : Arrays.asList(u);
        }
        return Collections.emptyList();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public List<NPath> resolveLibPaths(Class<?> clazz) {
        return JavaClassUtils.resolveURLs(clazz).stream().map(x->NPath.of(x)).collect(Collectors.toList());
    }

    @Override
    public NOptional<NPath> resolveLibPath(Class<?> clazz) {
        List<NPath> c = resolveLibPaths(clazz);
        return c.isEmpty() ?
                NOptional.ofNamedEmpty("LibPath fo "+clazz)
                : NOptional.of(c.get(0));
    }

    @Override
    public NOptional<NId> resolveId(Class<?> clazz) {
        List<NId> pomIds = resolveIds(clazz);
        NId defaultValue = null;
        if (pomIds.isEmpty()) {
            return NOptional.ofNamedEmpty("Id fo "+clazz);
        }
        if (pomIds.size() > 1) {
            NLogOp.of(NPomXmlParser.class)
                    .verb(NLogVerb.WARNING)
                    .level(Level.FINEST)
                    .log(NMsg.ofC(
                            "multiple ids found : %s for class %s and id %s",
                            Arrays.asList(pomIds), clazz, defaultValue
                    ));
        }
        return NOptional.of(pomIds.get(0));
    }

    @Override
    public NOptional<NId> resolveId(NPath path) {
        List<NId> pomIds = resolveIds(path);
        NId defaultValue = null;
        if (pomIds.isEmpty()) {
            return NOptional.ofNamedEmpty("Id fo "+path);
        }
        if (pomIds.size() > 1) {
            NLogOp.of(NPomXmlParser.class)
                    .verb(NLogVerb.WARNING)
                    .level(Level.FINEST)
                    .log(NMsg.ofC(
                            "multiple ids found : %s for path %s and id %s",
                            Arrays.asList(pomIds), path, defaultValue
                    ));
        }
        return NOptional.of(pomIds.get(0));
    }

    @Override
    public List<NId> resolveIds(NPath path) {
        LinkedHashSet<NId> all = new LinkedHashSet<>();
        NPomId[] u = MavenUtils.createPomIdResolver(workspace).resolvePomIds(path);
        all.addAll(
                Arrays.asList(new NMetaInfIdResolver().resolvePomIds(path))
        );
        for (NPomId uu : u) {
            all.add(NId.of(uu.getGroupId() + ":" + uu.getArtifactId() + "#" + uu.getVersion()).get());
        }
        return new ArrayList<>(all);
    }

    @Override
    public List<NId> resolveIds(Class<?> clazz) {
        LinkedHashSet<NId> all = new LinkedHashSet<>();
        NApplicationInfo annotation = (NApplicationInfo) clazz.getAnnotation(NApplicationInfo.class);
        if (annotation != null) {
            if (!NBlankable.isBlank(annotation.id())) {
                all.add(NId.of(annotation.id()).get());
            }
        }
        NPomId[] u = MavenUtils.createPomIdResolver(workspace).resolvePomIds(clazz);
        all.addAll(
                Arrays.asList(new NMetaInfIdResolver().resolvePomIds(clazz))
        );
        for (NPomId uu : u) {
            all.add(NId.of(uu.getGroupId() + ":" + uu.getArtifactId() + "#" + uu.getVersion()).get());
        }
        return new ArrayList<>(all);
    }

}

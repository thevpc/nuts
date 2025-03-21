//package net.thevpc.nuts.runtime.standalone.xtra.idresolver;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.io.NPath;
//import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.NPomXmlParser;
//import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
//import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomId;
//import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
//import net.thevpc.nuts.spi.NSupportLevelContext;
//import net.thevpc.nuts.log.NLogOp;
//import net.thevpc.nuts.log.NLogVerb;
//import net.thevpc.nuts.util.NBlankable;
//import net.thevpc.nuts.util.NMsg;
//
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.stream.Collectors;
//
//public class DefaultNIdResolver implements NIdResolver {
//
//    public DefaultNIdResolver() {
//    }
//
//    @Override
//    public NId resolveId(Class clazz) {
//        List<NId> pomIds = resolveIds(clazz);
//        NId defaultValue = null;
//        if (pomIds.isEmpty()) {
//            return null;
//        }
//        if (pomIds.size() > 1) {
//            NLogOp.of(NPomXmlParser.class, session)
//                    .verb(NLogVerb.WARNING)
//                    .level(Level.FINEST)
//                    .log(NMsg.ofC(
//                            "multiple ids found : %s for class %s and id %s",
//                            Arrays.asList(pomIds), clazz, defaultValue
//                    ));
//        }
//        return pomIds.get(0);
//    }
//
//    @Override
//    public NId resolveId(NPath path) {
//        List<NId> pomIds = resolveIds(path);
//        NId defaultValue = null;
//        if (pomIds.isEmpty()) {
//            return null;
//        }
//        if (pomIds.size() > 1) {
//            NLogOp.of(NPomXmlParser.class, session)
//                    .verb(NLogVerb.WARNING)
//                    .level(Level.FINEST)
//                    .log(NMsg.ofC(
//                            "multiple ids found : %s for path %s and id %s",
//                            Arrays.asList(pomIds), path, defaultValue
//                    ));
//        }
//        return pomIds.get(0);
//    }
//
//    @Override
//    public List<NId> resolveIds(NPath path) {
//        LinkedHashSet<NId> all = new LinkedHashSet<>();
//        NPomId[] u = MavenUtils.createPomIdResolver(session).resolvePomIds(path, session);
//        all.addAll(
//                Arrays.asList(new NMetaInfIdResolver(session).resolvePomIds(path, session))
//        );
//        for (NPomId uu : u) {
//            all.add(NId.of(uu.getGroupId() + ":" + uu.getArtifactId() + "#" + uu.getVersion()).get(session));
//        }
//        return new ArrayList<>(all);
//    }
//
//    @Override
//    public List<NId> resolveIds(Class clazz) {
//        LinkedHashSet<NId> all = new LinkedHashSet<>();
//        NApplicationInfo annotation = (NApplicationInfo) clazz.getAnnotation(NApplicationInfo.class);
//        if (annotation != null) {
//            if (!NBlankable.isBlank(annotation.id())) {
//                all.add(NId.of(annotation.id()).get());
//            }
//        }
//        NPomId[] u = MavenUtils.createPomIdResolver(session).resolvePomIds(clazz);
//        all.addAll(
//                Arrays.asList(new NMetaInfIdResolver(session).resolvePomIds(clazz))
//        );
//        for (NPomId uu : u) {
//            all.add(NId.of(uu.getGroupId() + ":" + uu.getArtifactId() + "#" + uu.getVersion()).get(session));
//        }
//        return new ArrayList<>(all);
//    }
//
//    @Override
//    public List<NPath> resolveSources(Class clazz) {
//        return JavaClassUtils.resolveURLs(clazz).stream().map(x->NPath.of(x,session)).collect(Collectors.toList());
//    }
//
//    @Override
//    public NPath resolveSource(Class clazz) {
//        List<NPath> c = resolveSources(clazz);
//        return c.isEmpty() ? null : c.get(0);
//    }
//
//    @Override
//    public int getSupportLevel(NSupportLevelContext context) {
//        return NConstants.Support.DEFAULT_SUPPORT;
//    }
//}

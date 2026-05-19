package net.thevpc.nuts.runtime.standalone.descriptor.util;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.io.NIn;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorBuilder;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorProperty;
import net.thevpc.nuts.runtime.standalone.DefaultNEnvConditionBuilder;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;

import net.thevpc.nuts.util.*;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.MapToFunction;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class NDescriptorUtils {
    public static Map<String, NDescriptorProperty> getPropertiesMap2(List<NDescriptorProperty> list) {
        Map<String, NDescriptorProperty> m = new LinkedHashMap<>();
        if (list != null) {
            for (NDescriptorProperty property : list) {
                m.put(property.name(), property);
            }
        }
        return m;
    }

    public static Map<String, String> getPropertiesMap(List<NDescriptorProperty> list) {
        Map<String, String> m = new LinkedHashMap<>();
        if (list != null) {
            for (NDescriptorProperty property : list) {
                if (NBlankable.isBlank(property.condition())) {
                    m.put(property.name(), property.value().asString().orNull());
                } else {
                    throw new NIllegalArgumentException(NMsg.ofPlain("unexpected properties with conditions. probably a bug"));
                }
            }
        }
        return m;
    }

    public static NDescriptor checkDescriptor(NDescriptor nutsDescriptor) {
        NId id = nutsDescriptor.id();
        String groupId = id == null ? null : id.groupId();
        String artifactId = id == null ? null : id.artifactId();
        NVersion version = id == null ? null : id.version();
        if (groupId == null || artifactId == null || NBlankable.isBlank(version)) {
            NSession session = NSession.of();
            switch (session.getConfirm().orDefault()) {
                case ASK:
                case ERROR: {
                    if (groupId == null) {
                        groupId = NIn.ask()
                                .forString(NMsg.ofPlain("group id"))
                                .defaultValue(groupId)
                                .hintMessage(NBlankable.isBlank(groupId) ? null : NMsg.ofPlain(groupId))
                                .value();
                    }
                    if (artifactId == null) {
                        artifactId = NIn.ask()
                                .forString(NMsg.ofPlain("artifact id"))
                                .defaultValue(artifactId)
                                .hintMessage(NBlankable.isBlank(artifactId) ? null : NMsg.ofPlain(artifactId))
                                .value();
                    }
                    if (NBlankable.isBlank(version)) {
                        String ov = version == null ? null : version.value();
                        String v = NIn.ask()
                                .forString(NMsg.ofPlain("version"))
                                .defaultValue(ov)
                                .hintMessage(NBlankable.isBlank(ov) ? null : NMsg.ofPlain(ov))
                                .value();
                        version = NVersion.get(v).get();
                    }
                    break;
                }
                case NO:
                case YES: {
                    //silently return null
                }
            }
        }
        if (groupId == null || artifactId == null || NBlankable.isBlank(version)) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid descriptor id %s:%s#%s", groupId, artifactId, version));
        }
        return nutsDescriptor.builder()
                .id(NIdBuilder.of(groupId, artifactId).version(version).build())
                .build();
    }

    public static void checkValidEffectiveDescriptor(NDescriptor effectiveDescriptor) {
        NAssert.requireNamedNonNull(effectiveDescriptor, "effective descriptor");
        boolean topException = false;
        try {
            for (NId parent : effectiveDescriptor.parents()) {
                CoreNIdUtils.checkValidEffectiveId(parent);
            }
            CoreNIdUtils.checkValidEffectiveId(effectiveDescriptor.id());
            for (NDependency dependency : effectiveDescriptor.dependencies()) {
                if (!CoreNIdUtils.isValidEffectiveId(dependency.toId())) {
                    NMsg errMsg = NMsg.ofC("%s is using dependency %s which defines an unresolved variable. This is a potential bug.",
                            effectiveDescriptor.id(),
                            dependency
                    );
                    NLog.of(NDescriptorUtils.class)
                            .log(errMsg.withIntent(NMsgIntent.ALERT).withLevel(Level.FINE));
                    if (!dependency.isOptional()) {
                        topException = true;
                        throw new NArtifactNotFoundException(effectiveDescriptor.id(), errMsg);
                    }
                }
            }
            for (NDependency dependency : effectiveDescriptor.standardDependencies()) {
                // replace direct call to checkValidEffectiveId with the following...
                if (!CoreNIdUtils.isValidEffectiveId(dependency.toId())) {
                    // sometimes the variable is defined later in the pom that uses this POM standard Dependencies
                    // so just log a warning, this is not an error but a very bad practice from the dependency maintainer!
                    NLog.of(NDescriptorUtils.class)

                            .log(NMsg.ofC("%s is using standard-dependency %s which defines an unresolved variable. This is a potential bug.",
                                                    effectiveDescriptor.id(),
                                                    dependency
                                            )
                                            .withIntent(NMsgIntent.ALERT).withLevel(Level.FINE)
                            );
                }
            }
        } catch (NIllegalArgumentException ex) {
            if (topException) {
                throw ex;
            }
            throw new NIllegalArgumentException(NMsg.ofC("unable to evaluate effective descriptor for %s", effectiveDescriptor.id()), ex);
        } catch (NArtifactNotFoundException ex) {
            throw new NArtifactNotFoundException(effectiveDescriptor.id(), NMsg.ofC("unable to evaluate effective descriptor for %s", effectiveDescriptor.id()), ex);
        } catch (Exception ex) {
            throw new NIllegalArgumentException(NMsg.ofC("unable to evaluate effective descriptor for %s", effectiveDescriptor.id()), ex);
        }

    }

    public static boolean isValidEffectiveDescriptor(NDescriptor effectiveDescriptor) {
        try {
            checkValidEffectiveDescriptor(effectiveDescriptor);
            return true;
        } catch (Exception ex) {
            //
        }
        return false;
    }


    public static NEnvConditionBuilder simplifyNutsEnvConditionBuilder(NEnvConditionBuilder c) {
        c.arch(NCollections.toDistinctTrimmedNonEmptyList(c.arch()));
        c.os(NCollections.toDistinctTrimmedNonEmptyList(c.os()));
        c.osDist(NCollections.toDistinctTrimmedNonEmptyList(c.osDist()));
        c.platform(NCollections.toDistinctTrimmedNonEmptyList(c.platform()));
        c.desktopEnvironment(NCollections.toDistinctTrimmedNonEmptyList(c.desktopEnvironment()));
        c.profile(NCollections.toDistinctTrimmedNonEmptyList(c.profiles()));
        return c;
    }

    public static NEnvConditionBuilder applyPropertiesNutsEnvCondition(NEnvConditionBuilder c, Map<String, String> properties) {
        Function<String, String> map = new MapToFunction<>(properties);
        c.arch(CoreNUtils.applyStringPropertiesList(c.arch(), map));
        c.os(CoreNUtils.applyStringPropertiesList(c.os(), map));
        c.osDist(CoreNUtils.applyStringPropertiesList(c.osDist(), map));
        c.platform(CoreNUtils.applyStringPropertiesList(c.platform(), map));
        c.desktopEnvironment(CoreNUtils.applyStringPropertiesList(c.desktopEnvironment(), map));
        c.profile(CoreNUtils.applyStringPropertiesList(c.profiles(), map));
        return c;
    }

    public static NEnvConditionBuilder applyPropertiesNutsEnvCondition2(NEnvConditionBuilder c, Map<String, NDescriptorProperty> properties) {
        Function<String, String> map = new StringStringFunctionFromNDescriptorProperty(properties);
        c.arch(CoreNUtils.applyStringPropertiesList(c.arch(), map));
        c.os(CoreNUtils.applyStringPropertiesList(c.os(), map));
        c.osDist(CoreNUtils.applyStringPropertiesList(c.osDist(), map));
        c.platform(CoreNUtils.applyStringPropertiesList(c.platform(), map));
        c.desktopEnvironment(CoreNUtils.applyStringPropertiesList(c.desktopEnvironment(), map));
        c.profile(CoreNUtils.applyStringPropertiesList(c.profiles(), map));
        return c;
    }

    public static NEnvCondition applyNutsConditionProperties(NEnvCondition child, Function<String, String> properties) {
        return child
                .builder()
                .os(CoreNUtils.applyStringProperties(child.os(), properties))
                .osDist(CoreNUtils.applyStringProperties(child.osDist(), properties))
                .platform(CoreNUtils.applyStringProperties(child.platform(), properties))
                .profile(CoreNUtils.applyStringProperties(child.profiles(), properties))
                .desktopEnvironment(CoreNUtils.applyStringProperties(child.desktopEnvironment(), properties))
                .arch(CoreNUtils.applyStringProperties(child.arch(), properties))
                .build();
    }

    public static NId applyNutsIdProperties(NDescriptor d, NId child, Function<String, String> properties) {
        return NIdBuilder.of()
                .repository(CoreNUtils.applyStringProperties(child.repository(), properties))
                .groupId(CoreNUtils.applyStringProperties(child.groupId(), properties))
                .artifactId(CoreNUtils.applyStringProperties(child.artifactId(), properties))
                .version(CoreNUtils.applyStringProperties(child.version().value(), properties))
                .condition(applyNutsConditionProperties(child.condition(), properties))
                .classifier(CoreNUtils.applyStringProperties(child.classifier(), properties))
                .packaging(CoreNUtils.applyStringProperties(child.packaging(), properties))
                .setProperties(CoreNUtils.applyMapProperties(child.properties(), properties))
                .build();
    }

    public static NDependencyBuilder applyNutsDependencyProperties(NDescriptorBuilder d, NDependency child, Function<String, String> properties) {
        List<NId> exclusions = child.exclusions().stream().map(
                x -> applyNutsIdProperties(d.build(), x, properties)
        ).collect(Collectors.toList());
        return NDependencyBuilder.of()
                .repository(CoreNUtils.applyStringProperties(child.repository(), properties))
                .groupId(CoreNUtils.applyStringProperties(child.groupId(), properties))
                .artifactId(CoreNUtils.applyStringProperties(child.artifactId(), properties))
                .version(CoreNUtils.applyStringProperties(child.version(), properties))
                .classifier(CoreNUtils.applyStringProperties(child.classifier(), properties))
                .scope(CoreNUtils.applyStringProperties(child.scope(), properties))
                .optional(CoreNUtils.applyStringProperties(child.optional(), properties))
                .condition(applyNutsConditionProperties(child.condition(), properties))
                .type(CoreNUtils.applyStringProperties(child.type(), properties))
                .exclusions(exclusions)
                .propertiesQuery(CoreNUtils.applyStringProperties(child.propertiesQuery(), properties))
                ;
    }

    public static NIdBuilder applyProperties(NIdBuilder b, Function<String, String> properties) {
        b.groupId(CoreNUtils.applyStringProperties(b.groupId(), properties));
        b.artifactId(CoreNUtils.applyStringProperties(b.artifactId(), properties));
        b.version(CoreNUtils.applyStringProperties(b.version().value(), properties));
        b.classifier(CoreNUtils.applyStringProperties(b.classifier(), properties));
        b.setProperties(CoreNUtils.applyMapProperties(b.properties(), properties));
        return b;
    }


    public static NDescriptorBuilder applyProperties(NDescriptorBuilder b) {
        Map<String, NDescriptorProperty> propertiesMap = NDescriptorUtils.getPropertiesMap2(b.properties());
        if (b.id() != null) {
            NId id = b.id();
            String gid = id.groupId();
            String version = id.version().value();
            if (gid != null && !propertiesMap.containsKey("groupId")) {
                propertiesMap.put("groupId", DefaultNDescriptorProperty.of("groupId", gid));
            }
            if (version != null && !propertiesMap.containsKey("version")) {
                propertiesMap.put("version", DefaultNDescriptorProperty.of("version", version));
            }
        }
        return applyProperties2(b,
                propertiesMap
        );
    }

    private static String sPropId(NDescriptorProperty d) {
        return NStringUtils.trim(d.name()) + ":" + d.condition().toString();
    }

    private static Map<String, NDescriptorProperty> propsAsMap(List<NDescriptorProperty> arr) {
        Map<String, NDescriptorProperty> m = new LinkedHashMap<>();
        for (NDescriptorProperty p : arr) {
            String s = sPropId(p);
            m.put(s, p);
        }
        return m;
    }

    public static NDescriptorBuilder applyParents(NDescriptorBuilder b, List<NDescriptor> parentDescriptors) {
        NId n_id = b.id();
        String n_packaging = b.packaging();
        LinkedHashSet<NDescriptorFlag> flags = new LinkedHashSet<>(b.flags());
        String n_name = b.name();
        List<String> n_categories = b.categories();
        if (n_categories == null) {
            n_categories = new ArrayList<>();
        } else {
            n_categories = new ArrayList<>(n_categories);
        }
        List<String> n_icons = b.icons();
        if (n_icons == null) {
            n_icons = new ArrayList<>();
        } else {
            n_icons = new ArrayList<>(n_icons);
        }
        String n_genericName = b.genericName();
        String n_desc = b.description();
        NArtifactCall n_executor = b.executor();
        NArtifactCall n_installer = b.installer();
        Map<String, NDescriptorProperty> n_props = new LinkedHashMap<>();
        for (NDescriptor parentDescriptor : parentDescriptors) {
            List<NDescriptorProperty> properties = parentDescriptor.properties();
            if (properties != null) {
                n_props.putAll(propsAsMap(properties));
            }
        }
        List<NDescriptorProperty> properties = b.properties();
        if (properties != null) {
            n_props.putAll(propsAsMap(properties));
        }
        NEnvConditionBuilder b2 = new DefaultNEnvConditionBuilder();

        Map<String, NDependency> n_deps = new LinkedHashMap<>();
        Map<String, NDependency> n_sdeps = new LinkedHashMap<>();
        for (NDescriptor parentDescriptor : parentDescriptors) {
            n_id = CoreNUtils.applyNutsIdInheritance(n_id, parentDescriptor.id());
            flags.addAll(parentDescriptor.flags());
            if (n_executor == null) {
                n_executor = parentDescriptor.executor();
            }
            if (n_installer == null) {
                n_installer = parentDescriptor.installer();
            }

            //packaging is not inherited!!
            //n_packaging = applyStringInheritance(n_packaging, parentDescriptor.getPackaging());
//            n_ext = CoreNutsUtils.applyStringInheritance(n_ext, parentDescriptor.getExt());
            n_name = CoreNUtils.applyStringInheritance(n_name, parentDescriptor.name());
            n_genericName = CoreNUtils.applyStringInheritance(n_genericName, parentDescriptor.genericName());
            n_desc = CoreNUtils.applyStringInheritance(n_desc, parentDescriptor.description());
            n_deps.putAll(depsAsMap(parentDescriptor.dependencies()));
            n_sdeps.putAll(depsAsMap(parentDescriptor.standardDependencies()));
            b2.copyFrom(parentDescriptor.condition());
            n_icons.addAll(parentDescriptor.icons());
            n_categories.addAll(parentDescriptor.categories());
        }
        n_deps.putAll(depsAsMap(b.dependencies()));
        n_sdeps.putAll(depsAsMap(b.standardDependencies()));
        b2.copyFrom(b.condition());
        List<NId> n_parents = new ArrayList<>();

        b.id(n_id);
//        setAlternative(n_alt);
        b.parents(n_parents);
        b.packaging(n_packaging);
        b.flags(flags);
        b.executor(n_executor);
        b.installer(n_installer);
        b.name(n_name);
        b.genericName(n_genericName);
        b.categories(new ArrayList<>(new LinkedHashSet<>(n_categories)));
        b.icons(new ArrayList<>(new LinkedHashSet<>(n_icons)));
        b.description(n_desc);
        b.condition(b2);
        b.dependencies(new ArrayList<>(n_deps.values()));
        b.standardDependencies(new ArrayList<>(n_sdeps.values()));
        b.setProperties(new ArrayList<>(n_props.values()));
        return b;
    }


    public static NDescriptorBuilder applyProperties(NDescriptorBuilder b, Map<String, String> properties) {
        properties = applyPropsToProps(b, properties);
        Function<String, String> map = new MapToFunction<>(properties);

        NId n_id = NDescriptorUtils.applyProperties(b.id().builder(), map).build();
//        String n_alt = CoreNutsUtils.applyStringProperties(getAlternative(), map);
        String n_packaging = CoreNUtils.applyStringProperties(b.packaging(), map);
        String n_name = CoreNUtils.applyStringProperties(b.name(), map);
        String n_desc = CoreNUtils.applyStringProperties(b.description(), map);
        NArtifactCall n_executor = b.executor();
        NArtifactCall n_installer = b.installer();
        DefaultNProperties n_props = new DefaultNProperties();
        for (NDescriptorProperty property : b.properties()) {
            String v = property.value().asString().get();
            if (CoreStringUtils.containsVars("${")) {
                n_props.add(property.builder().value(CoreNUtils.applyStringProperties(v, map))
                        .build());
            } else {
                n_props.add(property);
            }
        }

        LinkedHashSet<NDependency> n_deps = new LinkedHashSet<>();
        for (NDependency d2 : b.dependencies()) {
            n_deps.add(NDescriptorUtils.applyNutsDependencyProperties(b, d2, map).build());
        }

        LinkedHashSet<NDependency> n_sdeps = new LinkedHashSet<>();
        for (NDependency d2 : b.standardDependencies()) {
            n_sdeps.add(applyNutsDependencyProperties(b, d2, map).build());
        }

        b.id(n_id);
//        b.setAlternative(n_alt);
        b.parents(b.parents());
        b.packaging(n_packaging);
        b.executor(n_executor);
        b.installer(n_installer);
        b.name(n_name);
        b.description(n_desc);
        b.genericName(CoreNUtils.applyStringProperties(b.genericName(), map));
        b.icons(
                b.icons().stream()
                        .map(
                                x -> CoreNUtils.applyStringProperties(x, map)
                        ).collect(Collectors.toList())
        );
        b.categories(
                b.categories().stream()
                        .map(
                                x -> CoreNUtils.applyStringProperties(x, map)
                        ).collect(Collectors.toList())
        );
        b.condition(applyPropertiesNutsEnvCondition(b.condition(), properties).build());
        b.dependencies(new ArrayList<>(n_deps));
        b.standardDependencies(new ArrayList<>(n_sdeps));
        b.setProperties(n_props.toList());
        return b;
    }

    public static NDescriptorBuilder applyProperties2(NDescriptorBuilder b, Map<String, NDescriptorProperty> properties) {
        properties = applyPropsToProps2(b, properties);
        Map<String, NDescriptorProperty> finalProperties = properties;
        Function<String, String> map = new StringStringFunctionFromNDescriptorProperty(finalProperties);

        NId n_id = NDescriptorUtils.applyProperties(b.id().builder(), map).build();
//        String n_alt = CoreNutsUtils.applyStringProperties(getAlternative(), map);
        String n_packaging = CoreNUtils.applyStringProperties(b.packaging(), map);
        String n_name = CoreNUtils.applyStringProperties(b.name(), map);
        String n_desc = CoreNUtils.applyStringProperties(b.description(), map);
        NArtifactCall n_executor = b.executor();
        NArtifactCall n_installer = b.installer();
        DefaultNProperties n_props = new DefaultNProperties();
        for (NDescriptorProperty property : b.properties()) {
            String v = property.value().asString().get();
            if (CoreStringUtils.containsVars("${")) {
                n_props.add(property.builder().value(CoreNUtils.applyStringProperties(v, map))
                        .build());
            } else {
                n_props.add(property);
            }
        }

        LinkedHashSet<NDependency> n_deps = new LinkedHashSet<>();
        for (NDependency d2 : b.dependencies()) {
            n_deps.add(NDescriptorUtils.applyNutsDependencyProperties(b, d2, map).build());
        }

        LinkedHashSet<NDependency> n_sdeps = new LinkedHashSet<>();
        for (NDependency d2 : b.standardDependencies()) {
            n_sdeps.add(applyNutsDependencyProperties(b, d2, map).build());
        }

        b.id(n_id);
//        b.setAlternative(n_alt);
        b.parents(b.parents());
        b.packaging(n_packaging);
        b.executor(n_executor);
        b.installer(n_installer);
        b.name(n_name);
        b.description(n_desc);
        b.genericName(CoreNUtils.applyStringProperties(b.genericName(), map));
        b.icons(
                b.icons().stream()
                        .map(
                                x -> CoreNUtils.applyStringProperties(x, map)
                        ).collect(Collectors.toList())
        );
        b.categories(
                b.categories().stream()
                        .map(
                                x -> CoreNUtils.applyStringProperties(x, map)
                        ).collect(Collectors.toList())
        );
        b.condition(applyPropertiesNutsEnvCondition2(b.condition(), properties).build());
        b.dependencies(new ArrayList<>(n_deps));
        b.standardDependencies(new ArrayList<>(n_sdeps));
        b.setProperties(n_props.toList());
        return b;
    }

    private static Map<String, String> prepareGlobalProperties(NDescriptorBuilder b) {
        Map<String, String> global = new LinkedHashMap<>();
        // try to support both new and deprecated property names
        // to support ancient built maven packages!
        global.putAll(((Map) System.getProperties()));
        NId ii = b.id();
        for (String s : new String[]{"project.name", "pom.name"}) {
            global.put(s, b.name());
        }
        if (ii != null) {
            if (ii.version().value() != null) {
                for (String s : new String[]{"project.version", "version", "pom.version"}) {
                    global.put(s, ii.version().value());
                }
            }
            for (String s : new String[]{"project.groupId", "pom.groupId"}) {
                global.put(s, ii.groupId());
            }
            for (String s : new String[]{"project.artifactId", "pom.artifactId"}) {
                global.put(s, ii.artifactId());
            }
        }
        return global;
    }

    private static Map<String, NDescriptorProperty> prepareGlobalProperties2(NDescriptorBuilder b) {
        Map<String, NDescriptorProperty> global = new LinkedHashMap<>();
        // try to support both new and deprecated property names
        // to support ancient built maven packages!
        for (Map.Entry<Object, Object> e : System.getProperties().entrySet()) {
            String k = (String) e.getKey();
            global.put(k, DefaultNDescriptorProperty.of(k, e.getValue()));
        }
        NId ii = b.id();
        for (String s : new String[]{"project.name", "pom.name"}) {
            global.put(s, DefaultNDescriptorProperty.of(s, b.name()));
        }
        if (ii != null) {
            if (ii.version().value() != null) {
                for (String s : new String[]{"project.version", "version", "pom.version"}) {
                    global.put(s, DefaultNDescriptorProperty.of(s, ii.version().value()));
                }
            }
            for (String s : new String[]{"project.groupId", "pom.groupId"}) {
                global.put(s, DefaultNDescriptorProperty.of(s, ii.groupId()));
            }
            for (String s : new String[]{"project.artifactId", "pom.artifactId"}) {
                global.put(s, DefaultNDescriptorProperty.of(s, ii.artifactId()));
            }
        }
        return global;
    }

    private static Map<String, String> applyPropsToProps(NDescriptorBuilder b, Map<String, String> properties) {

        Map<String, String> oldMap = new LinkedHashMap<>(properties);


        for (Map.Entry<String, String> entry : prepareGlobalProperties(b).entrySet()) {
            if (!oldMap.containsKey(entry.getKey())) {
                oldMap.put(entry.getKey(), entry.getValue());
            }
        }
        Set<String> updated = new TreeSet<>();
        for (int i = 0; i < 16; i++) {
            Function<String, String> fct = new MapToFunction<>(oldMap);
            Map<String, String> newMap = new LinkedHashMap<>(oldMap.size());
            updated = new TreeSet<>();
            for (Map.Entry<String, String> entry : oldMap.entrySet()) {
                String v0 = entry.getValue();
                String v1 = CoreNUtils.applyStringProperties(v0, fct);
                if (!Objects.equals(v0, v1)) {
                    updated.add(entry.getKey());
                }
                newMap.put(entry.getKey(), v1);
            }
            if (updated.isEmpty()) {
                return newMap;
            }
            oldMap = newMap;
        }
        throw new NIllegalArgumentException(NMsg.ofC("too many recursion applying properties %s", updated));
    }

    private static Map<String, NDescriptorProperty> applyPropsToProps2(NDescriptorBuilder b, Map<String, NDescriptorProperty> properties) {

        Map<String, NDescriptorProperty> oldMap = new LinkedHashMap<>(properties);


        for (Map.Entry<String, NDescriptorProperty> entry : prepareGlobalProperties2(b).entrySet()) {
            if (!oldMap.containsKey(entry.getKey())) {
                oldMap.put(entry.getKey(), entry.getValue());
            }
        }
        Set<String> updated = new TreeSet<>();
        for (int i = 0; i < 16; i++) {
            Function<String, String> fct = new StringStringFunctionFromNDescriptorProperty(oldMap);
            Map<String, NDescriptorProperty> newMap = new LinkedHashMap<>(oldMap.size());
            updated = new TreeSet<>();
            for (Map.Entry<String, NDescriptorProperty> entry : oldMap.entrySet()) {
                NDescriptorProperty v00 = entry.getValue();
                if (NBlankable.isBlank(v00.condition())) {
                    String v0 = v00.value().asString().orNull();
                    String v1 = CoreNUtils.applyStringProperties(v0, fct);
                    if (!Objects.equals(v0, v1)) {
                        updated.add(entry.getKey());
                    }
                    newMap.put(entry.getKey(), DefaultNDescriptorProperty.of(entry.getKey(), v1));
                } else {
                    newMap.put(entry.getKey(), entry.getValue());
                }
            }
            if (updated.isEmpty()) {
                return newMap;
            }
            oldMap = newMap;
        }
        throw new NIllegalArgumentException(NMsg.ofC("too many recursion applying properties %s", updated));
    }

    private static Map<String, NDependency> depsAsMap(List<NDependency> arr) {
        Map<String, NDependency> m = new LinkedHashMap<>();
        //first is Best
        for (NDependency d : arr) {
            String e = sDepId(d);
            if (!m.containsKey(e)) {
                m.put(e, d);
            } else {
                NDependency a = m.get(e);
                if (a.equals(d)) {
                    NLog.of(DefaultNDescriptorBuilder.class)

                            .log(NMsg.ofC("dependency %s is duplicated", d)
                                    .withLevel(Level.FINER)
                                    .withIntent(NMsgIntent.ALERT)
                            );
                } else {
                    NLog.of(DefaultNDescriptorBuilder.class)

                            .log(NMsg.ofC("dependency %s is overridden by %s", a, d)
                                    .withLevel(Level.FINER)
                                    .withIntent(NMsgIntent.ALERT)
                            );
                }
            }
        }
        return m;
    }

    private static String sDepId(NDependency d) {
        return NStringUtils.trim(d.groupId()) + ":" + NStringUtils.trim(d.artifactId()) + "?" + NStringUtils.trim(d.classifier());
    }


    private static class StringStringFunctionFromNDescriptorProperty implements Function<String, String> {
        private final Map<String, NDescriptorProperty> finalProperties;

        public StringStringFunctionFromNDescriptorProperty(Map<String, NDescriptorProperty> finalProperties) {
            this.finalProperties = finalProperties;
        }

        @Override
        public String apply(String s) {
            NDescriptorProperty p = finalProperties.get(s);
            if (p != null) {
                NEnvCondition cc = p.condition();
                if (isAcceptProfiles(cc.profiles())) {
                    if (NBlankable.isBlank(cc)) {
                        return p.value().asString().orNull();
                    } else {
                        throw new IllegalArgumentException("unsupported condition " + cc + " for " + p);
                    }
                }
            }
            switch (s){
                case "os.detected.name":{
                    return NEnv.of().getOs().artifactId();
                }
                case "os.detected.version":{
                    return NEnv.of().getOs().version().toString();
                }
                case "os.detected.os.release":{
                    return NEnv.of().getOsDist().artifactId();
                }
                case "os.detected.release.version":{
                    return NEnv.of().getOsDist().version().toString();
                }
                case "os.detected.arch":{
                    return NEnv.of().getArch().artifactId();
                }
                case "os.detected.classifier":{
                    return NEnv.of().getOs().artifactId()+"-"+ NEnv.of().getArch().artifactId();
                }
                case "os.detected.bitness":{
                    return String.valueOf(NEnv.of().getArchFamily().getBits());
                }
                case "os.detected.release.like":{
                    return NEnv.of().getOsDist().properties().get("like");
                }
                case "os.detected.release.codename":{
                    return NEnv.of().getOsDist().properties().get("codename");
                }
            }
            return null;
        }

        private boolean isAcceptProfiles(List<String> profiles) {
            return profiles.isEmpty();
        }
    }
}

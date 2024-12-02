package net.thevpc.nuts.runtime.standalone.descriptor.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NEnvCondition;
import net.thevpc.nuts.NEnvConditionBuilder;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorBuilder;
import net.thevpc.nuts.runtime.standalone.DefaultNEnvConditionBuilder;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.MapToFunction;
import net.thevpc.nuts.util.NCoreArrayUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class NDescriptorUtils {

    public static NDescriptor getEffectiveDescriptor(NDefinition def) {
        final NDescriptor d = def.getEffectiveDescriptor().orNull();
        if (d == null) {
            return NWorkspaceExt.of().resolveEffectiveDescriptor(def.getDescriptor());
        }
        return d;
    }

    public static Map<String, String> getPropertiesMap(List<NDescriptorProperty> list) {
        Map<String, String> m = new LinkedHashMap<>();
        if (list != null) {
            for (NDescriptorProperty property : list) {
                if (property.getCondition() == null || property.getCondition().isBlank()) {
                    m.put(property.getName(), property.getValue().asString().orNull());
                } else {
                    throw new NIllegalArgumentException(NMsg.ofPlain("unexpected properties with conditions. probably a bug"));
                }
            }
        }
        return m;
    }

    public static NDescriptor checkDescriptor(NDescriptor nutsDescriptor) {
        NId id = nutsDescriptor.getId();
        String groupId = id == null ? null : id.getGroupId();
        String artifactId = id == null ? null : id.getArtifactId();
        NVersion version = id == null ? null : id.getVersion();
        if (groupId == null || artifactId == null || NBlankable.isBlank(version)) {
            NSession session = NSession.of();
            switch (session.getConfirm().orDefault()) {
                case ASK:
                case ERROR: {
                    if (groupId == null) {
                        groupId = NAsk.of()
                                .forString(NMsg.ofPlain("group id"))
                                .setDefaultValue(groupId)
                                .setHintMessage(NBlankable.isBlank(groupId) ? null : NMsg.ofPlain(groupId))
                                .getValue();
                    }
                    if (artifactId == null) {
                        artifactId = NAsk.of()
                                .forString(NMsg.ofPlain("artifact id"))
                                .setDefaultValue(artifactId)
                                .setHintMessage(NBlankable.isBlank(artifactId) ? null : NMsg.ofPlain(artifactId))
                                .getValue();
                    }
                    if (NBlankable.isBlank(version)) {
                        String ov = version == null ? null : version.getValue();
                        String v = NAsk.of()
                                .forString(NMsg.ofPlain("version"))
                                .setDefaultValue(ov)
                                .setHintMessage(NBlankable.isBlank(ov) ? null : NMsg.ofPlain(ov))
                                .getValue();
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
                .setId(NIdBuilder.of(groupId, artifactId).setVersion(version).build())
                .build();
    }

    public static void checkValidEffectiveDescriptor(NDescriptor effectiveDescriptor) {
        NAssert.requireNonNull(effectiveDescriptor, "effective descriptor");
        boolean topException = false;
        try {
            for (NId parent : effectiveDescriptor.getParents()) {
                CoreNIdUtils.checkValidEffectiveId(parent);
            }
            CoreNIdUtils.checkValidEffectiveId(effectiveDescriptor.getId());
            for (NDependency dependency : effectiveDescriptor.getDependencies()) {
                if (!CoreNIdUtils.isValidEffectiveId(dependency.toId())) {
                    NLogOp.of(NDescriptorUtils.class)
                            .verb(NLogVerb.WARNING).level(Level.FINE)
                            .log(NMsg.ofJ("{0} is using dependency {1} which defines an unresolved variable. This is a potential bug.",
                                    effectiveDescriptor.getId(),
                                    dependency
                            ));
                    if (!dependency.isOptional()) {
                        topException = true;
                        throw new NNotFoundException(effectiveDescriptor.getId(), NMsg.ofJ("{0} is using dependency {1} which defines an unresolved variable. This is a potential bug.",
                                effectiveDescriptor.getId(),
                                dependency
                        ));
                    }
                }
            }
            for (NDependency dependency : effectiveDescriptor.getStandardDependencies()) {
                // replace direct call to checkValidEffectiveId with the following...
                if (!CoreNIdUtils.isValidEffectiveId(dependency.toId())) {
                    // sometimes the variable is defined later in the pom that uses this POM standard Dependencies
                    // so just log a warning, this is not an error but a very bad practice from the dependency maintainer!
                    NLogOp.of(NDescriptorUtils.class)
                            .verb(NLogVerb.WARNING).level(Level.FINE)
                            .log(NMsg.ofJ("{0} is using standard-dependency {1} which defines an unresolved variable. This is a potential bug.",
                                    effectiveDescriptor.getId(),
                                    dependency
                            ));
                }
            }
        } catch (NIllegalArgumentException ex) {
            if (topException) {
                throw ex;
            }
            throw new NIllegalArgumentException(NMsg.ofC("unable to evaluate effective descriptor for %s", effectiveDescriptor.getId()), ex);
        } catch (NNotFoundException ex) {
            throw new NNotFoundException(effectiveDescriptor.getId(), NMsg.ofC("unable to evaluate effective descriptor for %s", effectiveDescriptor.getId()), ex);
        } catch (Exception ex) {
            throw new NIllegalArgumentException(NMsg.ofC("unable to evaluate effective descriptor for %s", effectiveDescriptor.getId()), ex);
        }

    }

    public static boolean isValidEffectiveDescriptor(NDescriptor effectiveDescriptor, NSession session) {
        try {
            checkValidEffectiveDescriptor(effectiveDescriptor);
            return true;
        } catch (Exception ex) {
            //
        }
        return false;
    }


    public static NEnvConditionBuilder simplifyNutsEnvConditionBuilder(NEnvConditionBuilder c) {
        c.setArch(NCoreArrayUtils.toDistinctTrimmedNonEmptyList(c.getArch()));
        c.setOs(NCoreArrayUtils.toDistinctTrimmedNonEmptyList(c.getOs()));
        c.setOsDist(NCoreArrayUtils.toDistinctTrimmedNonEmptyList(c.getOsDist()));
        c.setPlatform(NCoreArrayUtils.toDistinctTrimmedNonEmptyList(c.getPlatform()));
        c.setDesktopEnvironment(NCoreArrayUtils.toDistinctTrimmedNonEmptyList(c.getDesktopEnvironment()));
        c.setProfile(NCoreArrayUtils.toDistinctTrimmedNonEmptyList(c.getProfiles()));
        return c;
    }

    public static NEnvConditionBuilder applyPropertiesNutsEnvCondition(NEnvConditionBuilder c, Map<String, String> properties) {
        Function<String, String> map = new MapToFunction<>(properties);
        c.setArch(CoreNUtils.applyStringPropertiesList(c.getArch(), map));
        c.setOs(CoreNUtils.applyStringPropertiesList(c.getOs(), map));
        c.setOsDist(CoreNUtils.applyStringPropertiesList(c.getOsDist(), map));
        c.setPlatform(CoreNUtils.applyStringPropertiesList(c.getPlatform(), map));
        c.setDesktopEnvironment(CoreNUtils.applyStringPropertiesList(c.getDesktopEnvironment(), map));
        c.setProfile(CoreNUtils.applyStringPropertiesList(c.getProfiles(), map));
        return c;
    }

    public static NEnvCondition applyNutsConditionProperties(NEnvCondition child, Function<String, String> properties) {
        return child
                .builder()
                .setOs(CoreNUtils.applyStringProperties(child.getOs(), properties))
                .setOsDist(CoreNUtils.applyStringProperties(child.getOsDist(), properties))
                .setPlatform(CoreNUtils.applyStringProperties(child.getPlatform(), properties))
                .setProfile(CoreNUtils.applyStringProperties(child.getProfiles(), properties))
                .setDesktopEnvironment(CoreNUtils.applyStringProperties(child.getDesktopEnvironment(), properties))
                .setArch(CoreNUtils.applyStringProperties(child.getArch(), properties))
                .readOnly();
    }

    public static NId applyNutsIdProperties(NDescriptor d, NId child, Function<String, String> properties) {
        return NIdBuilder.of()
                .setRepository(CoreNUtils.applyStringProperties(child.getRepository(), properties))
                .setGroupId(CoreNUtils.applyStringProperties(child.getGroupId(), properties))
                .setArtifactId(CoreNUtils.applyStringProperties(child.getArtifactId(), properties))
                .setVersion(CoreNUtils.applyStringProperties(child.getVersion().getValue(), properties))
                .setCondition(applyNutsConditionProperties(child.getCondition(), properties))
                .setClassifier(CoreNUtils.applyStringProperties(child.getClassifier(), properties))
                .setPackaging(CoreNUtils.applyStringProperties(child.getPackaging(), properties))
                .setProperties(CoreNUtils.applyMapProperties(child.getProperties(), properties))
                .build();
    }

    public static NDependencyBuilder applyNutsDependencyProperties(NDescriptorBuilder d, NDependency child, Function<String, String> properties) {
        List<NId> exclusions = child.getExclusions().stream().map(
                x -> applyNutsIdProperties(d, x, properties)
        ).collect(Collectors.toList());
        return NDependencyBuilder.of()
                .setRepository(CoreNUtils.applyStringProperties(child.getRepository(), properties))
                .setGroupId(CoreNUtils.applyStringProperties(child.getGroupId(), properties))
                .setArtifactId(CoreNUtils.applyStringProperties(child.getArtifactId(), properties))
                .setVersion(CoreNUtils.applyStringProperties(child.getVersion(), properties))
                .setClassifier(CoreNUtils.applyStringProperties(child.getClassifier(), properties))
                .setScope(CoreNUtils.applyStringProperties(child.getScope(), properties))
                .setOptional(CoreNUtils.applyStringProperties(child.getOptional(), properties))
                .setCondition(applyNutsConditionProperties(child.getCondition(), properties))
                .setType(CoreNUtils.applyStringProperties(child.getType(), properties))
                .setExclusions(exclusions)
                .setPropertiesQuery(CoreNUtils.applyStringProperties(child.getPropertiesQuery(), properties))
                ;
    }

    public static NIdBuilder applyProperties(NIdBuilder b, Function<String, String> properties) {
        b.setGroupId(CoreNUtils.applyStringProperties(b.getGroupId(), properties));
        b.setArtifactId(CoreNUtils.applyStringProperties(b.getArtifactId(), properties));
        b.setVersion(CoreNUtils.applyStringProperties(b.getVersion().getValue(), properties));
        b.setClassifier(CoreNUtils.applyStringProperties(b.getClassifier(), properties));
        b.setProperties(CoreNUtils.applyMapProperties(b.getProperties(), properties));
        return b;
    }


    public static NDescriptorBuilder applyProperties(NDescriptorBuilder b) {
        Map<String, String> propertiesMap = NDescriptorUtils.getPropertiesMap(b.getProperties());
        if (b.getId() != null) {
            NId id = b.getId();
            String gid = id.getGroupId();
            String version = id.getVersion().getValue();
            if (gid != null && !propertiesMap.containsKey("groupId")) {
                propertiesMap.put("groupId", gid);
            }
            if (version != null && !propertiesMap.containsKey("version")) {
                propertiesMap.put("version", version);
            }
        }
        return applyProperties(b,
                propertiesMap
        );
    }

    private static String sPropId(NDescriptorProperty d) {
        return NStringUtils.trim(d.getName()) + ":" + d.getCondition().toString();
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
        NId n_id = b.getId();
        String n_packaging = b.getPackaging();
        LinkedHashSet<NDescriptorFlag> flags = new LinkedHashSet<>(b.getFlags());
        String n_name = b.getName();
        List<String> n_categories = b.getCategories();
        if (n_categories == null) {
            n_categories = new ArrayList<>();
        } else {
            n_categories = new ArrayList<>(n_categories);
        }
        List<String> n_icons = b.getIcons();
        if (n_icons == null) {
            n_icons = new ArrayList<>();
        } else {
            n_icons = new ArrayList<>(n_icons);
        }
        String n_genericName = b.getGenericName();
        String n_desc = b.getDescription();
        NArtifactCall n_executor = b.getExecutor();
        NArtifactCall n_installer = b.getInstaller();
        Map<String, NDescriptorProperty> n_props = new LinkedHashMap<>();
        for (NDescriptor parentDescriptor : parentDescriptors) {
            List<NDescriptorProperty> properties = parentDescriptor.getProperties();
            if (properties != null) {
                n_props.putAll(propsAsMap(properties));
            }
        }
        List<NDescriptorProperty> properties = b.getProperties();
        if (properties != null) {
            n_props.putAll(propsAsMap(properties));
        }
        NEnvConditionBuilder b2 = new DefaultNEnvConditionBuilder();

        Map<String, NDependency> n_deps = new LinkedHashMap<>();
        Map<String, NDependency> n_sdeps = new LinkedHashMap<>();
        for (NDescriptor parentDescriptor : parentDescriptors) {
            n_id = CoreNUtils.applyNutsIdInheritance(n_id, parentDescriptor.getId());
            flags.addAll(parentDescriptor.getFlags());
            if (n_executor == null) {
                n_executor = parentDescriptor.getExecutor();
            }
            if (n_installer == null) {
                n_installer = parentDescriptor.getInstaller();
            }

            //packaging is not inherited!!
            //n_packaging = applyStringInheritance(n_packaging, parentDescriptor.getPackaging());
//            n_ext = CoreNutsUtils.applyStringInheritance(n_ext, parentDescriptor.getExt());
            n_name = CoreNUtils.applyStringInheritance(n_name, parentDescriptor.getName());
            n_genericName = CoreNUtils.applyStringInheritance(n_genericName, parentDescriptor.getGenericName());
            n_desc = CoreNUtils.applyStringInheritance(n_desc, parentDescriptor.getDescription());
            n_deps.putAll(depsAsMap(parentDescriptor.getDependencies()));
            n_sdeps.putAll(depsAsMap(parentDescriptor.getStandardDependencies()));
            b2.addAll(parentDescriptor.getCondition());
            n_icons.addAll(parentDescriptor.getIcons());
            n_categories.addAll(parentDescriptor.getCategories());
        }
        n_deps.putAll(depsAsMap(b.getDependencies()));
        n_sdeps.putAll(depsAsMap(b.getStandardDependencies()));
        b2.addAll(b.getCondition());
        List<NId> n_parents = new ArrayList<>();

        b.setId(n_id);
//        setAlternative(n_alt);
        b.setParents(n_parents);
        b.setPackaging(n_packaging);
        b.setFlags(flags);
        b.setExecutor(n_executor);
        b.setInstaller(n_installer);
        b.setName(n_name);
        b.setGenericName(n_genericName);
        b.setCategories(new ArrayList<>(new LinkedHashSet<>(n_categories)));
        b.setIcons(new ArrayList<>(new LinkedHashSet<>(n_icons)));
        b.setDescription(n_desc);
        b.setCondition(b2);
        b.setDependencies(new ArrayList<>(n_deps.values()));
        b.setStandardDependencies(new ArrayList<>(n_sdeps.values()));
        b.setProperties(new ArrayList<>(n_props.values()));
        return b;
    }


    public static NDescriptorBuilder applyProperties(NDescriptorBuilder b, Map<String, String> properties) {
        properties = applyPropsToProps(b, properties);
        Function<String, String> map = new MapToFunction<>(properties);

        NId n_id = NDescriptorUtils.applyProperties(b.getId().builder(), map).build();
//        String n_alt = CoreNutsUtils.applyStringProperties(getAlternative(), map);
        String n_packaging = CoreNUtils.applyStringProperties(b.getPackaging(), map);
        String n_name = CoreNUtils.applyStringProperties(b.getName(), map);
        String n_desc = CoreNUtils.applyStringProperties(b.getDescription(), map);
        NArtifactCall n_executor = b.getExecutor();
        NArtifactCall n_installer = b.getInstaller();
        DefaultNProperties n_props = new DefaultNProperties();
        for (NDescriptorProperty property : b.getProperties()) {
            String v = property.getValue().asString().get();
            if (CoreStringUtils.containsVars("${")) {
                n_props.add(property.builder().setValue(CoreNUtils.applyStringProperties(v, map))
                        .readOnly());
            } else {
                n_props.add(property);
            }
        }

        LinkedHashSet<NDependency> n_deps = new LinkedHashSet<>();
        for (NDependency d2 : b.getDependencies()) {
            n_deps.add(NDescriptorUtils.applyNutsDependencyProperties(b, d2, map));
        }

        LinkedHashSet<NDependency> n_sdeps = new LinkedHashSet<>();
        for (NDependency d2 : b.getStandardDependencies()) {
            n_sdeps.add(applyNutsDependencyProperties(b, d2, map).build());
        }

        b.setId(n_id);
//        b.setAlternative(n_alt);
        b.setParents(b.getParents());
        b.setPackaging(n_packaging);
        b.setExecutor(n_executor);
        b.setInstaller(n_installer);
        b.setName(n_name);
        b.setDescription(n_desc);
        b.setGenericName(CoreNUtils.applyStringProperties(b.getGenericName(), map));
        b.setIcons(
                b.getIcons().stream()
                        .map(
                                x -> CoreNUtils.applyStringProperties(x, map)
                        ).collect(Collectors.toList())
        );
        b.setCategories(
                b.getCategories().stream()
                        .map(
                                x -> CoreNUtils.applyStringProperties(x, map)
                        ).collect(Collectors.toList())
        );
        b.setCondition(applyPropertiesNutsEnvCondition(b.getCondition().builder(), properties).build());
        b.setDependencies(new ArrayList<>(n_deps));
        b.setStandardDependencies(new ArrayList<>(n_sdeps));
        b.setProperties(n_props.toList());
        return b;
    }

    private static Map<String, String> prepareGlobalProperties(NDescriptorBuilder b) {
        Map<String, String> global = new LinkedHashMap<>();
        // try to support both new and deprecated property names
        // to support ancient built maven packages!
        global.putAll(((Map) System.getProperties()));
        NId ii = b.getId();
        for (String s : new String[]{"project.name", "pom.name"}) {
            global.put(s, b.getName());
        }
        if (ii != null) {
            if (ii.getVersion().getValue() != null) {
                for (String s : new String[]{"project.version", "version", "pom.version"}) {
                    global.put(s, ii.getVersion().getValue());
                }
            }
            for (String s : new String[]{"project.groupId", "pom.groupId"}) {
                global.put(s, ii.getGroupId());
            }
            for (String s : new String[]{"project.artifactId", "pom.artifactId"}) {
                global.put(s, ii.getArtifactId());
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
                    NLogOp.of(DefaultNDescriptorBuilder.class)
                            .level(Level.FINER)
                            .verb(NLogVerb.WARNING)
                            .log(NMsg.ofC("dependency %s is duplicated", d));
                } else {
                    NLogOp.of(DefaultNDescriptorBuilder.class)
                            .level(Level.FINER)
                            .verb(NLogVerb.WARNING)
                            .log(NMsg.ofC("dependency %s is overridden by %s", a, d));
                }
            }
        }
        return m;
    }

    private static String sDepId(NDependency d) {
        return NStringUtils.trim(d.getGroupId()) + ":" + NStringUtils.trim(d.getArtifactId()) + "?" + NStringUtils.trim(d.getClassifier());
    }


}

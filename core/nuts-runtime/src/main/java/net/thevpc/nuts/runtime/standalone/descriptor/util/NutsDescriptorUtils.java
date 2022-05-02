package net.thevpc.nuts.runtime.standalone.descriptor.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.reserved.NutsReservedDefaultNutsProperties;
import net.thevpc.nuts.runtime.standalone.id.util.NutsIdUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.MapToFunction;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreArrayUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.util.NutsLoggerOp;
import net.thevpc.nuts.util.NutsLoggerVerb;
import net.thevpc.nuts.util.NutsStringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class NutsDescriptorUtils {
    public static boolean isNoContent(NutsDescriptor desc) {
        return desc!=null && "pom".equals(desc.getPackaging());
    }

    public static NutsDescriptor getEffectiveDescriptor(NutsDefinition def,NutsSession session) {
        final NutsDescriptor d = def.getEffectiveDescriptor().orNull();
        if (d == null) {
            return NutsWorkspaceExt.of(session).resolveEffectiveDescriptor(def.getDescriptor(), session);
        }
        return d;
    }

    public static Map<String, String> getPropertiesMap(List<NutsDescriptorProperty> list, NutsSession session) {
        Map<String, String> m = new LinkedHashMap<>();
        if (list != null) {
            for (NutsDescriptorProperty property : list) {
                if (property.getCondition() == null || property.getCondition().isBlank()) {
                    m.put(property.getName(), property.getValue().asString().orNull());
                } else {
                    throw new NutsIllegalArgumentException(session, NutsMessage.plain("unexpected properties with conditions. probably a bug"));
                }
            }
        }
        return m;
    }

    public static NutsDescriptor checkDescriptor(NutsDescriptor nutsDescriptor, NutsSession session) {
        NutsId id = nutsDescriptor.getId();
        String groupId = id == null ? null : id.getGroupId();
        String artifactId = id == null ? null : id.getArtifactId();
        NutsVersion version = id == null ? null : id.getVersion();
        if (groupId == null || artifactId == null || NutsBlankable.isBlank(version)) {
            switch (session.getConfirm()) {
                case ASK:
                case ERROR: {
                    if (groupId == null) {
                        groupId = session.getTerminal().ask()
                                .forString(NutsMessage.cstyle("group id"))
                                .setDefaultValue(groupId)
                                .setHintMessage(NutsBlankable.isBlank(groupId) ? null : NutsMessage.plain(groupId))
                                .getValue();
                    }
                    if (artifactId == null) {
                        artifactId = session.getTerminal().ask()
                                .forString(NutsMessage.cstyle("artifact id"))
                                .setDefaultValue(artifactId)
                                .setHintMessage(NutsBlankable.isBlank(artifactId) ? null : NutsMessage.plain(artifactId))
                                .getValue();
                    }
                    if (NutsBlankable.isBlank(version)) {
                        String ov = version == null ? null : version.getValue();
                        String v = session.getTerminal().ask()
                                .forString(NutsMessage.cstyle("version"))
                                .setDefaultValue(ov)
                                .setHintMessage(NutsBlankable.isBlank(ov) ? null : NutsMessage.plain(ov))
                                .getValue();
                        version = NutsVersion.of(v).get();
                    }
                    break;
                }
                case NO:
                case YES: {
                    //silently return null
                }
            }
        }
        if (groupId == null || artifactId == null || NutsBlankable.isBlank(version)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid descriptor id %s:%s#%s", groupId, artifactId, version));
        }
        return nutsDescriptor.builder()
                .setId(NutsIdBuilder.of(groupId,artifactId).setVersion(version).build())
                .build();
    }

    public static void checkValidEffectiveDescriptor(NutsDescriptor effectiveDescriptor,NutsSession session) {
        if(effectiveDescriptor==null){
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to evaluate effective null descriptor"));
        }
        try{
            for (NutsId parent : effectiveDescriptor.getParents()) {
                NutsIdUtils.checkValidEffectiveId(parent,session);
            }
            NutsIdUtils.checkValidEffectiveId(effectiveDescriptor.getId(),session);
            for (NutsDependency dependency : effectiveDescriptor.getDependencies()) {
                NutsIdUtils.checkValidEffectiveId(dependency.toId(),session);
            }
            for (NutsDependency dependency : effectiveDescriptor.getStandardDependencies()) {
                //NutsIdUtils.checkValidEffectiveId(dependency.toId(),session);
                // replace direct call to checkValidEffectiveId with the following...
                if (dependency == null) {
                    throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to evaluate effective null id"));
                }
                if (dependency.toString().contains("${")) {
                    // some times the variable is defined later in the pom that uses this POM standard Dependencies
                    // so just log a warning, this is not an error but a very bad practice from the dependency maintainer!
                    NutsLoggerOp.of(NutsDescriptorUtils.class,session)
                            .verb(NutsLoggerVerb.WARNING).level(Level.FINE)
                            .log(NutsMessage.jstyle("{0} is using {1} which defines an unresolved variable. This is a potential bug.",
                                    effectiveDescriptor.getId(),
                                    dependency
                            ));
                }
            }
        }catch (Exception ex) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to evaluate effective descriptor for %s", effectiveDescriptor.getId()),ex);
        }

    }

    public static boolean isValidEffectiveDescriptor(NutsDescriptor effectiveDescriptor,NutsSession session) {
        try{
            checkValidEffectiveDescriptor(effectiveDescriptor,session);
            return true;
        }catch (Exception ex){
            //
        }
        return false;
    }


    public static NutsEnvConditionBuilder simplifyNutsEnvConditionBuilder(NutsEnvConditionBuilder c) {
        c.setArch(CoreArrayUtils.toDistinctTrimmedNonEmptyList(c.getArch()));
        c.setOs(CoreArrayUtils.toDistinctTrimmedNonEmptyList(c.getOs()));
        c.setOsDist(CoreArrayUtils.toDistinctTrimmedNonEmptyList(c.getOsDist()));
        c.setPlatform(CoreArrayUtils.toDistinctTrimmedNonEmptyList(c.getPlatform()));
        c.setDesktopEnvironment(CoreArrayUtils.toDistinctTrimmedNonEmptyList(c.getDesktopEnvironment()));
        c.setProfile(CoreArrayUtils.toDistinctTrimmedNonEmptyList(c.getProfile()));
        return c;
    }
    public static NutsEnvConditionBuilder applyPropertiesNutsEnvCondition(NutsEnvConditionBuilder c,Map<String, String> properties) {
        Function<String, String> map = new MapToFunction<>(properties);
        c.setArch(CoreNutsUtils.applyStringPropertiesList(c.getArch(), map));
        c.setOs(CoreNutsUtils.applyStringPropertiesList(c.getOs(), map));
        c.setOsDist(CoreNutsUtils.applyStringPropertiesList(c.getOsDist(), map));
        c.setPlatform(CoreNutsUtils.applyStringPropertiesList(c.getPlatform(), map));
        c.setDesktopEnvironment(CoreNutsUtils.applyStringPropertiesList(c.getDesktopEnvironment(), map));
        c.setProfile(CoreNutsUtils.applyStringPropertiesList(c.getProfile(), map));
        return c;
    }

    public static NutsEnvCondition applyNutsConditionProperties(NutsEnvCondition child, Function<String, String> properties) {
        return child
                .builder()
                .setOs(CoreNutsUtils.applyStringProperties(child.getOs(), properties))
                .setOsDist(CoreNutsUtils.applyStringProperties(child.getOsDist(), properties))
                .setPlatform(CoreNutsUtils.applyStringProperties(child.getPlatform(), properties))
                .setProfile(CoreNutsUtils.applyStringProperties(child.getProfile(), properties))
                .setDesktopEnvironment(CoreNutsUtils.applyStringProperties(child.getDesktopEnvironment(), properties))
                .setArch(CoreNutsUtils.applyStringProperties(child.getArch(), properties))
                .readOnly();
    }

    public static NutsId applyNutsIdProperties(NutsDescriptor d,NutsId child, Function<String, String> properties) {
        return NutsIdBuilder.of()
                .setRepository(CoreNutsUtils.applyStringProperties(child.getRepository(), properties))
                .setGroupId(CoreNutsUtils.applyStringProperties(child.getGroupId(), properties))
                .setArtifactId(CoreNutsUtils.applyStringProperties(child.getArtifactId(), properties))
                .setVersion(CoreNutsUtils.applyStringProperties(child.getVersion().getValue(), properties))
                .setCondition(applyNutsConditionProperties(child.getCondition(), properties))
                .setClassifier(CoreNutsUtils.applyStringProperties(child.getClassifier(), properties))
                .setPackaging(CoreNutsUtils.applyStringProperties(child.getPackaging(), properties))
                .setProperties(CoreNutsUtils.applyMapProperties(child.getProperties(), properties))
                .build();
    }

    public static NutsDependencyBuilder applyNutsDependencyProperties(NutsDescriptorBuilder d,NutsDependency child, Function<String, String> properties) {
        List<NutsId> exclusions = child.getExclusions().stream().map(
                x->applyNutsIdProperties(d,x, properties)
        ).collect(Collectors.toList());
        return NutsDependencyBuilder.of()
                .setRepository(CoreNutsUtils.applyStringProperties(child.getRepository(), properties))
                .setGroupId(CoreNutsUtils.applyStringProperties(child.getGroupId(), properties))
                .setArtifactId(CoreNutsUtils.applyStringProperties(child.getArtifactId(), properties))
                .setVersion(CoreNutsUtils.applyStringProperties(child.getVersion(), properties))
                .setClassifier(CoreNutsUtils.applyStringProperties(child.getClassifier(), properties))
                .setScope(CoreNutsUtils.applyStringProperties(child.getScope(), properties))
                .setOptional(CoreNutsUtils.applyStringProperties(child.getOptional(), properties))
                .setCondition(applyNutsConditionProperties(child.getCondition(), properties))
                .setType(CoreNutsUtils.applyStringProperties(child.getType(), properties))
                .setExclusions(exclusions)
                .setPropertiesQuery(CoreNutsUtils.applyStringProperties(child.getPropertiesQuery(), properties))
                ;
    }

    public static NutsIdBuilder applyProperties(NutsIdBuilder b,Function<String, String> properties) {
        b.setGroupId(CoreNutsUtils.applyStringProperties(b.getGroupId(), properties));
        b.setArtifactId(CoreNutsUtils.applyStringProperties(b.getArtifactId(), properties));
        b.setVersion(CoreNutsUtils.applyStringProperties(b.getVersion().getValue(), properties));
        b.setClassifier(CoreNutsUtils.applyStringProperties(b.getClassifier(), properties));
        b.setProperties(CoreNutsUtils.applyMapProperties(b.getProperties(), properties));
        return b;
    }


    public static NutsDescriptorBuilder applyProperties(NutsDescriptorBuilder b,NutsSession session) {
        return applyProperties(b,
                NutsDescriptorUtils.getPropertiesMap(b.getProperties(), session),session
        );
    }
    private static String sPropId(NutsDescriptorProperty d) {
        return NutsStringUtils.trim(d.getName()) + ":" + d.getCondition().toString();
    }

    private static Map<String, NutsDescriptorProperty> propsAsMap(List<NutsDescriptorProperty> arr) {
        Map<String, NutsDescriptorProperty> m = new LinkedHashMap<>();
        for (NutsDescriptorProperty p : arr) {
            String s = sPropId(p);
            m.put(s, p);
        }
        return m;
    }

    public static NutsDescriptorBuilder applyParents(NutsDescriptorBuilder b,List<NutsDescriptor> parentDescriptors,NutsSession session) {
        NutsId n_id = b.getId();
        String n_packaging = b.getPackaging();
        LinkedHashSet<NutsDescriptorFlag> flags = new LinkedHashSet<>(b.getFlags());
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
        NutsArtifactCall n_executor = b.getExecutor();
        NutsArtifactCall n_installer = b.getInstaller();
        Map<String, NutsDescriptorProperty> n_props = new LinkedHashMap<>();
        for (NutsDescriptor parentDescriptor : parentDescriptors) {
            List<NutsDescriptorProperty> properties = parentDescriptor.getProperties();
            if (properties != null) {
                n_props.putAll(propsAsMap(properties));
            }
        }
        List<NutsDescriptorProperty> properties = b.getProperties();
        if (properties != null) {
            n_props.putAll(propsAsMap(properties));
        }
        NutsEnvConditionBuilder b2 = new DefaultNutsEnvConditionBuilder();

        Map<String, NutsDependency> n_deps = new LinkedHashMap<>();
        Map<String, NutsDependency> n_sdeps = new LinkedHashMap<>();
        for (NutsDescriptor parentDescriptor : parentDescriptors) {
            n_id = CoreNutsUtils.applyNutsIdInheritance(n_id, parentDescriptor.getId());
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
            n_name = CoreNutsUtils.applyStringInheritance(n_name, parentDescriptor.getName());
            n_genericName = CoreNutsUtils.applyStringInheritance(n_genericName, parentDescriptor.getGenericName());
            n_desc = CoreNutsUtils.applyStringInheritance(n_desc, parentDescriptor.getDescription());
            n_deps.putAll(depsAsMap(parentDescriptor.getDependencies(),session));
            n_sdeps.putAll(depsAsMap(parentDescriptor.getStandardDependencies(),session));
            b2.addAll(parentDescriptor.getCondition());
            n_icons.addAll(parentDescriptor.getIcons());
            n_categories.addAll(parentDescriptor.getCategories());
        }
        n_deps.putAll(depsAsMap(b.getDependencies(),session));
        n_sdeps.putAll(depsAsMap(b.getStandardDependencies(),session));
        b2.addAll(b.getCondition());
        List<NutsId> n_parents = new ArrayList<>();

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


    public static NutsDescriptorBuilder applyProperties(NutsDescriptorBuilder b,Map<String, String> properties,NutsSession session) {
        properties = applyPropsToProps(b,properties,session);
        Function<String, String> map = new MapToFunction<>(properties);

        NutsId n_id = NutsDescriptorUtils.applyProperties(b.getId().builder(), map).build();
//        String n_alt = CoreNutsUtils.applyStringProperties(getAlternative(), map);
        String n_packaging = CoreNutsUtils.applyStringProperties(b.getPackaging(), map);
        String n_name = CoreNutsUtils.applyStringProperties(b.getName(), map);
        String n_desc = CoreNutsUtils.applyStringProperties(b.getDescription(), map);
        NutsArtifactCall n_executor = b.getExecutor();
        NutsArtifactCall n_installer = b.getInstaller();
        NutsReservedDefaultNutsProperties n_props = new NutsReservedDefaultNutsProperties();
        for (NutsDescriptorProperty property : b.getProperties()) {
            String v = property.getValue().asString().get(session);
            if (CoreStringUtils.containsVars("${")) {
                n_props.add(property.builder().setValue(CoreNutsUtils.applyStringProperties(v, map))
                        .readOnly());
            } else {
                n_props.add(property);
            }
        }

        LinkedHashSet<NutsDependency> n_deps = new LinkedHashSet<>();
        for (NutsDependency d2 : b.getDependencies()) {
            n_deps.add(NutsDescriptorUtils.applyNutsDependencyProperties(b, d2, map));
        }

        LinkedHashSet<NutsDependency> n_sdeps = new LinkedHashSet<>();
        for (NutsDependency d2 : b.getStandardDependencies()) {
            n_sdeps.add(applyNutsDependencyProperties(b,d2, map).build());
        }

        b.setId(n_id);
//        b.setAlternative(n_alt);
        b.setParents(b.getParents());
        b.setPackaging(n_packaging);
        b.setExecutor(n_executor);
        b.setInstaller(n_installer);
        b.setName(n_name);
        b.setDescription(n_desc);
        b.setGenericName(CoreNutsUtils.applyStringProperties(b.getGenericName(), map));
        b.setIcons(
                b.getIcons().stream()
                        .map(
                                x -> CoreNutsUtils.applyStringProperties(x, map)
                        ).collect(Collectors.toList())
        );
        b.setCategories(
                b.getCategories().stream()
                        .map(
                                x -> CoreNutsUtils.applyStringProperties(x, map)
                        ).collect(Collectors.toList())
        );
        b.setCondition(applyPropertiesNutsEnvCondition(b.getCondition().builder(),properties).build());
        b.setDependencies(new ArrayList<>(n_deps));
        b.setStandardDependencies(new ArrayList<>(n_sdeps));
        b.setProperties(n_props.getList());
        return b;
    }

    private static Map<String, String> prepareGlobalProperties(NutsDescriptorBuilder b) {
        Map<String, String> global = new LinkedHashMap<>();
        // try to support both new and deprecated property names
        // to support ancient built maven packages!
        global.putAll(((Map) System.getProperties()));
        NutsId ii = b.getId();
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

    private static Map<String, String> applyPropsToProps(NutsDescriptorBuilder b,Map<String, String> properties,NutsSession session) {

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
                String v1 = CoreNutsUtils.applyStringProperties(v0, fct);
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
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("too many recursion applying properties %s", updated));
    }

    private static Map<String, NutsDependency> depsAsMap(List<NutsDependency> arr,NutsSession session) {
        Map<String, NutsDependency> m = new LinkedHashMap<>();
        //first is Best
        for (NutsDependency d : arr) {
            String e = sDepId(d);
            if (!m.containsKey(e)) {
                m.put(e, d);
            } else {
                NutsDependency a = m.get(e);
                if (a.equals(d)) {
                    NutsLoggerOp.of(DefaultNutsDescriptorBuilder.class, session)
                            .level(Level.FINER)
                            .verb(NutsLoggerVerb.WARNING)
                            .log(NutsMessage.cstyle("dependency %s is duplicated", d));
                } else {
                    NutsLoggerOp.of(DefaultNutsDescriptorBuilder.class, session)
                            .level(Level.FINER)
                            .verb(NutsLoggerVerb.WARNING)
                            .log(NutsMessage.cstyle("dependency %s is overridden by %s", a, d));
                }
            }
        }
        return m;
    }

    private static String sDepId(NutsDependency d) {
        return NutsStringUtils.trim(d.getGroupId()) + ":" + NutsStringUtils.trim(d.getArtifactId()) + "?" + NutsStringUtils.trim(d.getClassifier());
    }


}

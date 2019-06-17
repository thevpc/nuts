package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

public class DefaultNutsDescriptorBuilder implements NutsDescriptorBuilder {

    private static final long serialVersionUID = 1L;

    private NutsId id;
    private String alternative;
    private NutsId[] parents;
    private String packaging;
//    private String ext;
    private boolean executable;
    private boolean nutsApplication;
    private NutsExecutorDescriptor executor;
    private NutsExecutorDescriptor installer;
    /**
     * short description
     */
    private String name;
    /**
     * some longer (but not too long) description
     */
    private String description;
    private List<String> arch;
    private List<String> os;
    private List<String> osdist;
    private List<String> platform;
    private List<String> locations;
    private List<NutsDependency> dependencies;
    private List<NutsDependency> standardDependencies;
    private Map<String, String> properties;

    public DefaultNutsDescriptorBuilder() {
    }

    public DefaultNutsDescriptorBuilder(NutsId id, String alternative, NutsId[] parents, String packaging, boolean executable, boolean nutsApplication, String ext,
            NutsExecutorDescriptor executor, NutsExecutorDescriptor installer, String name, String description,
            String[] arch, String[] os, String[] osdist, String[] platform,
            NutsDependency[] dependencies,
            NutsDependency[] standardDependencies,
            String[] locations, Map<String, String> properties) {
        setId(id);
        setAlternative(alternative);
        setPackaging(packaging);
        setParents(parents);
        setExecutable(executable);
        setNutsApplication(nutsApplication);
        setDescription(description);
        setName(name);
        setExecutor(executor);
        setInstaller(installer);
//        setExt(ext);
        setArch(arch);
        setOs(os);
        setOsdist(osdist);
        setPlatform(platform);
        setLocations(locations);
        setDependencies(dependencies);
        setStandardDependencies(standardDependencies);
        setProperties(properties, false);
    }

    public DefaultNutsDescriptorBuilder(NutsDescriptor other) {
        set(other);
    }

    public DefaultNutsDescriptorBuilder(NutsDescriptorBuilder other) {
        set(other);
    }

    @Override
    public NutsDescriptorBuilder set(NutsDescriptorBuilder other) {
        if (other != null) {
            setId(other.getId());
            setAlternative(other.getAlternative());
            setPackaging(other.getPackaging());
            setParents(other.getParents());
            setExecutable(other.isExecutable());
            setNutsApplication(other.isNutsApplication());
            setDescription(other.getDescription());
            setName(other.getName());
            setExecutor(other.getExecutor());
            setInstaller(other.getInstaller());
//            setExt(other.getExt());
            setArch(other.getArch());
            setOs(other.getOs());
            setOsdist(other.getOsdist());
            setPlatform(other.getPlatform());
            setLocations(other.getLocations());
            setDependencies(other.getDependencies());
            setStandardDependencies(other.getStandardDependencies());
            setProperties(other.getProperties(), false);
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder set(NutsDescriptor other) {
        if (other != null) {
            setId(other.getId());
            setAlternative(other.getAlternative());
            setPackaging(other.getPackaging());
            setParents(other.getParents());
            setExecutable(other.isExecutable());
            setNutsApplication(other.isNutsApplication());
            setDescription(other.getDescription());
            setName(other.getName());
            setExecutor(other.getExecutor());
            setInstaller(other.getInstaller());
//            setExt(other.getExt());
            setArch(other.getArch());
            setOs(other.getOs());
            setOsdist(other.getOsdist());
            setPlatform(other.getPlatform());
            setLocations(other.getLocations());
            setDependencies(other.getDependencies());
            setStandardDependencies(other.getStandardDependencies());
            setProperties(other.getProperties(), false);
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder setId(String id) {
        this.id = NutsWorkspaceUtils.parseRequiredNutsId(null, id);
        return this;
    }

    @Override
    public NutsDescriptorBuilder setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public NutsDescriptorBuilder setName(String name) {
        this.name = CoreStringUtils.trim(name);
        return this;
    }

    @Override
    public NutsDescriptorBuilder setExecutor(NutsExecutorDescriptor executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public NutsDescriptorBuilder setInstaller(NutsExecutorDescriptor installer) {
        this.installer = installer;
        return this;
    }

    @Override
    public NutsDescriptorBuilder setAlternative(String alternative) {
        this.alternative = alternative;
        return this;
    }

    @Override
    public NutsDescriptorBuilder setDescription(String description) {
        this.description = CoreStringUtils.trim(description);
        return this;
    }

    @Override
    public NutsDescriptorBuilder setExecutable(boolean executable) {
        this.executable = executable;
        return this;
    }

    @Override
    public NutsDescriptorBuilder setNutsApplication(boolean nutsApp) {
        this.nutsApplication = nutsApp;
        return this;
    }

//    @Override
//    public NutsDescriptorBuilder setExt(String ext) {
//        this.ext = CoreStringUtils.trim(ext);
//        return this;
//    }
    public NutsDescriptorBuilder addPlatform(String platform) {
        if (platform != null) {
            if (this.platform == null) {
                this.platform = new ArrayList<>();
            }
            this.platform.add(platform);
        }
        return this;
    }

    public NutsDescriptorBuilder setPlatform(String[] platform) {
        this.platform = new ArrayList<>(Arrays.asList(CoreCommonUtils.toArraySet(platform)));
        return this;
    }

    public NutsDescriptorBuilder setOs(String[] os) {
        this.os = new ArrayList<>(Arrays.asList(CoreCommonUtils.toArraySet(os)));
        return this;
    }

    public NutsDescriptorBuilder setOsdist(String[] osdist) {
        this.osdist = new ArrayList<>(Arrays.asList(CoreCommonUtils.toArraySet(osdist)));
        return this;
    }

    public NutsDescriptorBuilder setArch(String[] arch) {
        this.arch = new ArrayList<>(Arrays.asList(CoreCommonUtils.toArraySet(arch)));
        return this;
    }

    @Override
    public NutsDescriptorBuilder setProperties(Map<String, String> map) {
        return setProperties(map, false);
    }

    @Override
    public NutsDescriptorBuilder setProperties(Map<String, String> properties, boolean append) {
        if (append) {
            if (properties == null || properties.isEmpty()) {
                //do nothing
            } else {
                HashMap<String, String> p = new HashMap<>(this.properties);
                p.putAll(properties);
                this.properties = p;
            }

        } else {
            if (properties == null || properties.isEmpty()) {
                this.properties = null;
            } else {
                this.properties = new HashMap<>(properties);
            }
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder addLocation(String location) {
        if (this.locations == null) {
            this.locations = new ArrayList<>();
        }
        this.locations.add(location);
        return this;
    }

    @Override
    public NutsDescriptorBuilder setLocations(String[] locations) {
        this.locations = new ArrayList<>(Arrays.asList(locations));
        return this;
    }

    @Override
    public NutsDescriptorBuilder setPackaging(String packaging) {
        this.packaging = CoreStringUtils.trim(packaging);
        return this;
    }

    public NutsDescriptorBuilder setParents(NutsId[] parents) {
        this.parents = parents == null ? new NutsId[0] : new NutsId[parents.length];
        if (parents != null) {
            System.arraycopy(parents, 0, this.parents, 0, this.parents.length);
        }
        return this;
    }

    public String getAlternative() {
        return alternative;
    }

    @Override
    public NutsExecutorDescriptor getInstaller() {
        return installer;
    }

    @Override
    public Map<String, String> getProperties() {
        if (properties == null) {
            properties = new HashMap<>();
        }
        return properties;
    }

    @Override
    public NutsId[] getParents() {
        return parents;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isExecutable() {
        return executable;
    }

    @Override
    public boolean isNutsApplication() {
        return nutsApplication;
    }

    @Override
    public NutsExecutorDescriptor getExecutor() {
        return executor;
    }

//    @Override
//    public String getExt() {
//        return ext;
//    }
    @Override
    public String getPackaging() {
        return packaging;
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public NutsDependency[] getDependencies() {
        return dependencies == null ? new NutsDependency[0] : dependencies.toArray(new NutsDependency[0]);
    }

    @Override
    public NutsDependency[] getStandardDependencies() {
        return standardDependencies == null ? new NutsDependency[0] : standardDependencies.toArray(new NutsDependency[0]);
    }

    @Override
    public String[] getArch() {
        return arch == null ? new String[0]
                : arch.toArray(new String[0]);
    }

    public String[] getOs() {
        return os == null ? new String[0] : os.toArray(new String[0]);
    }

    public String[] getOsdist() {
        return osdist == null ? new String[0] : osdist.toArray(new String[0]);
    }

    public String[] getPlatform() {
        return platform == null ? new String[0] : platform.toArray(new String[0]);
    }

    @Override
    public NutsDescriptorBuilder setDependencies(NutsDependency[] dependencies) {
        this.dependencies = new ArrayList<>();
        for (NutsDependency dependency : dependencies) {
            if (dependency == null) {
                throw new NullPointerException();
            }
            this.dependencies.add(dependency);
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder setStandardDependencies(NutsDependency[] dependencies) {
        this.standardDependencies = new ArrayList<>();
        for (NutsDependency dependency : dependencies) {
            if (dependency == null) {
                throw new NullPointerException();
            }
            this.standardDependencies.add(dependency);
        }
        return this;
    }

    @Override
    public NutsDescriptor build() {
        return new DefaultNutsDescriptor(
                getId(), getAlternative(), getParents(), getPackaging(), isExecutable(), isNutsApplication(),
                //                getExt(),
                getExecutor(), getInstaller(),
                getName(), getDescription(), getArch(), getOs(), getOsdist(), getPlatform(), getDependencies(), getStandardDependencies(),
                getLocations(), getProperties()
        );
    }

    @Override
    public String[] getLocations() {
        return locations == null ? new String[0] : locations.toArray(new String[0]);
    }

    @Override
    public NutsDescriptorBuilder addProperty(String name, String value) {
        properties.put(name, value);
        return this;
    }

    @Override
    public NutsDescriptorBuilder removeProperty(String name) {
        properties.get(name);
        return this;
    }

    @Override
    public NutsDescriptorBuilder addOs(String os) {
        if (this.os == null) {
            this.os = new ArrayList<>();
        }
        this.os.add(os);
        return this;
    }

    @Override
    public NutsDescriptorBuilder addOsdist(String osdist) {
        if (this.osdist == null) {
            this.osdist = new ArrayList<>();
        }
        this.osdist.add(osdist);
        return this;
    }

    @Override
    public NutsDescriptorBuilder addArch(String arch) {
        if (this.arch == null) {
            this.arch = new ArrayList<>();
        }
        this.arch.add(arch);
        return this;
    }

    @Override
    public NutsDescriptorBuilder removeOs(String os) {
        if (this.os != null) {
            this.os.remove(os);
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder removeOsdist(String osdist) {
        if (this.osdist != null) {
            this.osdist.remove(osdist);
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder removeArch(String arch) {
        if (this.arch != null) {
            this.arch.remove(arch);
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder removePlatform(String platform) {
        if (this.platform != null) {
            this.platform.remove(platform);
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder removeDependency(NutsDependency dependency) {
        if (this.dependencies != null) {
            this.dependencies.remove(dependency);
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder removeStandardDependency(NutsDependency dependency) {
        if (this.standardDependencies != null) {
            this.standardDependencies.remove(dependency);
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder addDependency(NutsDependency dependency) {
        if (dependency == null) {
            throw new NullPointerException();
        }
        if (this.dependencies == null) {
            this.dependencies = new ArrayList<>();
        }
        this.dependencies.add(dependency);
        return this;
    }

    @Override
    public NutsDescriptorBuilder addStandardDependency(NutsDependency dependency) {
        if (this.standardDependencies == null) {
            this.standardDependencies = new ArrayList<>();
        }
        this.standardDependencies.add(dependency);
        return this;
    }

    @Override
    public NutsDescriptorBuilder addDependencies(NutsDependency[] dependencies) {
        if (this.dependencies == null) {
            this.dependencies = new ArrayList<>();
        }
        this.dependencies.addAll(Arrays.asList(dependencies));
        return this;
    }

    @Override
    public NutsDescriptorBuilder addStandardDependencies(NutsDependency[] dependencies) {
        if (this.standardDependencies == null) {
            this.standardDependencies = new ArrayList<>();
        }
        this.standardDependencies.addAll(Arrays.asList(dependencies));
        return this;
    }

    @Override
    public NutsDescriptorBuilder applyProperties() {
        return applyProperties(getProperties());
    }

    @Override
    public NutsDescriptorBuilder replaceProperty(Predicate<Map.Entry<String, String>> filter, Function<Map.Entry<String, String>, String> converter) {
        if (converter == null) {
            return this;
        }
        Map<String, String> p = new LinkedHashMap<>();
        boolean someUpdate = false;
        for (Iterator<Map.Entry<String, String>> it = getProperties().entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, String> entry = it.next();
            if (filter == null || filter.test(entry)) {
                String v = converter.apply(entry);
                if (v != null) {
                    p.put(entry.getKey(), entry.getValue());
                    if (!Objects.equals(v, entry.getValue())) {
                        someUpdate = true;
                    }
                } else {
                    it.remove();
                }
            }
        }
        if (someUpdate) {
            for (Map.Entry<String, String> entry : p.entrySet()) {
                getProperties().replace(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder applyParents(NutsDescriptor[] parentDescriptors) {
        NutsId n_id = getId();
        String n_alt = getAlternative();
        String n_packaging = getPackaging();
//        String n_ext = getExt();
        boolean n_executable = isExecutable();
        String n_name = getName();
        String n_desc = getDescription();
        NutsExecutorDescriptor n_executor = getExecutor();
        NutsExecutorDescriptor n_installer = getInstaller();
        Map<String, String> n_props = new HashMap<>();
        for (NutsDescriptor parentDescriptor : parentDescriptors) {
            n_props.putAll(parentDescriptor.getProperties());
        }
        Map<String, String> properties = getProperties();
        if (properties != null) {
            n_props.putAll(properties);
        }
        LinkedHashSet<NutsDependency> n_deps = new LinkedHashSet<>();
        LinkedHashSet<NutsDependency> n_sdeps = new LinkedHashSet<>();
        LinkedHashSet<String> n_archs = new LinkedHashSet<>();
        LinkedHashSet<String> n_os = new LinkedHashSet<>();
        LinkedHashSet<String> n_osdist = new LinkedHashSet<>();
        LinkedHashSet<String> n_platform = new LinkedHashSet<>();
        for (NutsDescriptor parentDescriptor : parentDescriptors) {
            n_id = CoreNutsUtils.applyNutsIdInheritance(n_id, parentDescriptor.getId());
            if (!n_executable && parentDescriptor.isExecutable()) {
                n_executable = true;
            }
            if (n_executor == null) {
                n_executor = parentDescriptor.getExecutor();
            }
            if (n_executor == null) {
                n_installer = parentDescriptor.getInstaller();
            }

            //packaging is not inherited!!
            //n_packaging = applyStringInheritance(n_packaging, parentDescriptor.getPackaging());
//            n_ext = CoreNutsUtils.applyStringInheritance(n_ext, parentDescriptor.getExt());
            n_name = CoreNutsUtils.applyStringInheritance(n_name, parentDescriptor.getName());
            n_desc = CoreNutsUtils.applyStringInheritance(n_desc, parentDescriptor.getDescription());
            n_deps.addAll(Arrays.asList(parentDescriptor.getDependencies()));
            n_sdeps.addAll(Arrays.asList(parentDescriptor.getStandardDependencies()));
            n_archs.addAll(Arrays.asList(parentDescriptor.getArch()));
            n_os.addAll(Arrays.asList(parentDescriptor.getOs()));
            n_osdist.addAll(Arrays.asList(parentDescriptor.getOsdist()));
            n_platform.addAll(Arrays.asList(parentDescriptor.getPlatform()));
        }
        n_deps.addAll(Arrays.asList(getDependencies()));
        n_sdeps.addAll(Arrays.asList(getStandardDependencies()));
        n_archs.addAll(Arrays.asList(getArch()));
        n_os.addAll(Arrays.asList(getOs()));
        n_osdist.addAll(Arrays.asList(getOsdist()));
        n_platform.addAll(Arrays.asList(getPlatform()));
        NutsId[] n_parents = new NutsId[0];
//        if (n_packaging.isEmpty() && n_ext.isEmpty()) {
//            n_packaging = "jar";
//            n_ext = "jar";
//        } else if (n_packaging.isEmpty()) {
//            n_packaging = n_ext;
//        } else {
//            n_ext = n_packaging;
//        }

        setId(n_id);
        setAlternative(n_alt);
        setParents(n_parents);
        setPackaging(n_packaging);
        setExecutable(n_executable);
//        setExt(n_ext);
        setExecutor(n_executor);
        setInstaller(n_installer);
        setName(n_name);
        setDescription(n_desc);
        setArch(n_archs.toArray(new String[0]));
        setOs(n_os.toArray(new String[0]));
        setOsdist(n_osdist.toArray(new String[0]));
        setPlatform(n_platform.toArray(new String[0]));
        setDependencies(n_deps.toArray(new NutsDependency[0]));
        setStandardDependencies(n_sdeps.toArray(new NutsDependency[0]));
        setProperties(n_props);
        return this;
    }

    @Override
    public NutsDescriptorBuilder applyProperties(Map<String, String> properties) {
        Function<String, String> map = new CoreStringUtils.MapToFunction<>(properties);

        NutsId n_id = getId().apply(map);
        String n_alt = CoreNutsUtils.applyStringProperties(getAlternative(), map);
        String n_packaging = CoreNutsUtils.applyStringProperties(getPackaging(), map);
//        String n_ext = CoreNutsUtils.applyStringProperties(getExt(), map);
        String n_name = CoreNutsUtils.applyStringProperties(getName(), map);
        String n_desc = CoreNutsUtils.applyStringProperties(getDescription(), map);
        NutsExecutorDescriptor n_executor = getExecutor();
        NutsExecutorDescriptor n_installer = getInstaller();
        Map<String, String> n_props = new HashMap<>();
        Map<String, String> properties1 = getProperties();
        if (properties1 != null) {
            for (Map.Entry<String, String> ee : properties1.entrySet()) {
                n_props.put(CoreNutsUtils.applyStringProperties(ee.getKey(), map), CoreNutsUtils.applyStringProperties(ee.getValue(), map));
            }
        }

        LinkedHashSet<NutsDependency> n_deps = new LinkedHashSet<>();
        for (NutsDependency d2 : getDependencies()) {
            n_deps.add(applyNutsDependencyProperties(d2, map));
        }

        LinkedHashSet<NutsDependency> n_sdeps = new LinkedHashSet<>();
        for (NutsDependency d2 : getStandardDependencies()) {
            n_sdeps.add(applyNutsDependencyProperties(d2, map));
        }

        this.setId(n_id);
        this.setAlternative(n_alt);
        this.setParents(getParents());
        this.setPackaging(n_packaging);
        this.setExecutable(isExecutable());
//        this.setExt(n_ext);
        this.setExecutor(n_executor);
        this.setInstaller(n_installer);
        this.setName(n_name);
        this.setDescription(n_desc);
        this.setArch(CoreNutsUtils.applyStringProperties(getArch(), map));
        this.setOs(CoreNutsUtils.applyStringProperties(getOs(), map));
        this.setOsdist(CoreNutsUtils.applyStringProperties(getOsdist(), map));
        this.setPlatform(CoreNutsUtils.applyStringProperties(getPlatform(), map));
        this.setDependencies(n_deps.toArray(new NutsDependency[0]));
        this.setStandardDependencies(n_sdeps.toArray(new NutsDependency[0]));
        this.setProperties(n_props);
        return this;
    }

    private NutsId applyNutsIdProperties(NutsId child, Function<String, String> properties) {
        return new DefaultNutsId(
                CoreNutsUtils.applyStringProperties(child.getNamespace(), properties),
                CoreNutsUtils.applyStringProperties(child.getGroup(), properties),
                CoreNutsUtils.applyStringProperties(child.getName(), properties),
                CoreNutsUtils.applyStringProperties(child.getVersion().getValue(), properties),
                CoreNutsUtils.applyMapProperties(child.getQueryMap(), properties)
        );
    }

    private NutsDependency applyNutsDependencyProperties(NutsDependency child, Function<String, String> properties) {
        NutsId[] exclusions = child.getExclusions();
        for (int i = 0; i < exclusions.length; i++) {
            exclusions[i] = applyNutsIdProperties(exclusions[i], properties);
        }
        return new DefaultNutsDependency(
                CoreNutsUtils.applyStringProperties(child.getNamespace(), properties),
                CoreNutsUtils.applyStringProperties(child.getGroup(), properties),
                CoreNutsUtils.applyStringProperties(child.getName(), properties),
                CoreNutsUtils.applyStringProperties(child.getClassifier(), properties),
                CoreNutsUtils.applyStringProperties(child.getVersion(), properties),
                CoreNutsUtils.applyStringProperties(child.getScope(), properties),
                CoreNutsUtils.applyStringProperties(child.getOptional(), properties),
                exclusions
        );
    }

    @Override
    public NutsDescriptorBuilder replaceDependency(Predicate<NutsDependency> filter, UnaryOperator<NutsDependency> converter) {
        if (converter == null) {
            return this;
        }
        ArrayList<NutsDependency> dependenciesList = new ArrayList<>();
        for (NutsDependency d : getDependencies()) {
            if (filter == null || filter.test(d)) {
                d = converter.apply(d);
                if (d != null) {
                    dependenciesList.add(d);;
                }
            } else {
                dependenciesList.add(d);
            }
        }
        this.dependencies = dependenciesList;
        return this;
    }

    @Override
    public NutsDescriptorBuilder removeDependency(Predicate<NutsDependency> dependency) {
        if (dependency == null) {
            return this;
        }
        for (Iterator<NutsDependency> it = dependencies.iterator(); it.hasNext();) {
            NutsDependency d = it.next();
            if (dependency.test(d)) {
                //do not add
                it.remove();
            }
        }
        return this;
    }

}

package net.thevpc.nuts;


import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NBlankable;

import java.util.List;
import java.util.Map;

public interface NEnvConditionBuilder extends NComponent, NBlankable {
    static NEnvConditionBuilder of() {
        return NExtensions.of(NEnvConditionBuilder.class);
    }

    NEnvConditionBuilder setArch(List<String> arch);

    NEnvConditionBuilder setOs(List<String> os);

    NEnvConditionBuilder setOsDist(List<String> osDist);

    NEnvConditionBuilder setPlatform(List<String> platform);

    NEnvConditionBuilder setDesktopEnvironment(List<String> desktopEnvironment);

    NEnvConditionBuilder setProfile(List<String> profiles);

    NEnvConditionBuilder copyFrom(NEnvCondition other);

    NEnvConditionBuilder copyFrom(NEnvConditionBuilder other);

    NEnvConditionBuilder clear();

    NEnvCondition build();

    NEnvConditionBuilder copy();

    NEnvConditionBuilder setProperties(Map<String, String> properties);

    NEnvConditionBuilder addProperties(Map<String, String> properties);

    NEnvConditionBuilder addProperty(String key, String value);

    NEnvConditionBuilder addDesktopEnvironment(String value);

    NEnvConditionBuilder addDesktopEnvironments(String... values);

    NEnvConditionBuilder addArchs(String value);

    NEnvConditionBuilder addArchs(String... values);

    NEnvConditionBuilder addOs(String value);

    NEnvConditionBuilder addOses(String... values);

    NEnvConditionBuilder addOsDist(String value);

    NEnvConditionBuilder addOsDists(String... values);

    NEnvConditionBuilder addPlatform(String value);

    NEnvConditionBuilder addPlatforms(String... values);

    NEnvConditionBuilder addProfile(String value);

    NEnvConditionBuilder addProfiles(String... values);

    NEnvConditionBuilder and(NEnvCondition other);

    NEnvConditionBuilder or(NEnvCondition other);

    /*
     * supported profiles (such as maven profiles)
     *
     * @return supported supported profiles
     */
    List<String> getProfiles();

    /**
     * supported arch list. if empty, all arch are supported (for example for java, all arch are supported).
     *
     * @return supported arch list
     */
    List<String> getArch();

    /**
     * supported operating systems. if empty, all oses are supported (for example for java, all arch are supported).
     *
     * @return supported oses
     */
    List<String> getOs();

    /**
     * supported operating system distributions (mostly for linux systems). if empty, all distributions are supported.
     *
     * @return supported operating system distributions
     */
    List<String> getOsDist();

    /**
     * supported platforms (java, dotnet, ...). if empty platform is not relevant.
     * This is helpful to bind application to a jdk version for instance (in that case platform may be in the form java#8 for instance)
     *
     * @return supported platforms
     */
    List<String> getPlatform();

    /**
     * supported desktop environments (gnome, kde, none, ...). if empty desktop environment is not relevant.
     * This is helpful to bind application to a specific environment
     *
     * @return supported platforms
     */
    List<String> getDesktopEnvironment();

    /**
     * create builder from this instance
     *
     * @return builder copy of this instance
     */
    NEnvConditionBuilder builder();

    NEnvCondition readOnly();

    /**
     * return env properties
     *
     * @return env properties
     * @since 0.8.4
     */
    Map<String, String> getProperties();

    Map<String, String> toMap();

}

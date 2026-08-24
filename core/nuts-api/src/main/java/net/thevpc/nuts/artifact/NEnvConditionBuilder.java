package net.thevpc.nuts.artifact;


import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

import java.util.List;
import java.util.Map;

/**
 * NEnvConditionBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NEnvConditionBuilder extends NComponent, NBlankable {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NEnvConditionBuilder of() {
        return NExtensions.of(NEnvConditionBuilder.class);
    }

    /**
     * Arch.
     *
     * @param arch arch
     * @return arch result
     */
    @NSetter
    NEnvConditionBuilder arch(List<String> arch);

    /**
     * Os.
     *
     * @param os os
     * @return os result
     */
    @NSetter
    NEnvConditionBuilder os(List<String> os);

    /**
     * Os dist.
     *
     * @param osDist os dist
     * @return os dist result
     */
    @NSetter
    NEnvConditionBuilder osDist(List<String> osDist);

    /**
     * Platform.
     *
     * @param platform platform
     * @return platform result
     */
    @NSetter
    NEnvConditionBuilder platform(List<String> platform);

    /**
     * Desktop environment.
     *
     * @param desktopEnvironment desktop environment
     * @return desktop environment result
     */
    @NSetter
    NEnvConditionBuilder desktopEnvironment(List<String> desktopEnvironment);

    /**
     * Profile.
     *
     * @param profiles profiles
     * @return profile result
     */
    @NSetter
    NEnvConditionBuilder profile(List<String> profiles);

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NEnvConditionBuilder copyFrom(NEnvCondition other);

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NEnvConditionBuilder copyFrom(NEnvConditionBuilder other);

    /**
     * Clear.
     *
     * @return clear result
     */
    NEnvConditionBuilder clear();

    /**
     * Build.
     *
     * @return build result
     */
    NEnvCondition build();

    /**
     * Copy.
     *
     * @return copy result
     */
    NEnvConditionBuilder copy();

    /**
     * Sets the properties.
     *
     * @param properties properties
     * @return set properties result
     */
    NEnvConditionBuilder setProperties(Map<String, String> properties);

    /**
     * Adds the specified properties.
     *
     * @param properties properties
     * @return add properties result
     */
    NEnvConditionBuilder addProperties(Map<String, String> properties);

    /**
     * Adds the specified property.
     *
     * @param key key
     * @param value value
     * @return add property result
     */
    NEnvConditionBuilder addProperty(String key, String value);

    /**
     * Adds the specified desktop environment.
     *
     * @param value value
     * @return add desktop environment result
     */
    NEnvConditionBuilder addDesktopEnvironment(String value);

    /**
     * Adds the specified desktop environments.
     *
     * @param values values
     * @return add desktop environments result
     */
    NEnvConditionBuilder addDesktopEnvironments(String... values);

    /**
     * Adds the specified archs.
     *
     * @param value value
     * @return add archs result
     */
    NEnvConditionBuilder addArchs(String value);

    /**
     * Adds the specified archs.
     *
     * @param values values
     * @return add archs result
     */
    NEnvConditionBuilder addArchs(String... values);

    /**
     * Adds the specified os.
     *
     * @param value value
     * @return add os result
     */
    NEnvConditionBuilder addOs(String value);

    /**
     * Adds the specified oses.
     *
     * @param values values
     * @return add oses result
     */
    NEnvConditionBuilder addOses(String... values);

    /**
     * Adds the specified os dist.
     *
     * @param value value
     * @return add os dist result
     */
    NEnvConditionBuilder addOsDist(String value);

    /**
     * Adds the specified os dists.
     *
     * @param values values
     * @return add os dists result
     */
    NEnvConditionBuilder addOsDists(String... values);

    /**
     * Adds the specified platform.
     *
     * @param value value
     * @return add platform result
     */
    NEnvConditionBuilder addPlatform(String value);

    /**
     * Adds the specified platforms.
     *
     * @param values values
     * @return add platforms result
     */
    NEnvConditionBuilder addPlatforms(String... values);

    /**
     * Adds the specified profile.
     *
     * @param value value
     * @return add profile result
     */
    NEnvConditionBuilder addProfile(String value);

    /**
     * Adds the specified profiles.
     *
     * @param values values
     * @return add profiles result
     */
    NEnvConditionBuilder addProfiles(String... values);

    /**
     * And.
     *
     * @param other other
     * @return and result
     */
    NEnvConditionBuilder and(NEnvCondition other);

    /**
     * Or.
     *
     * @param other other
     * @return or result
     */
    NEnvConditionBuilder or(NEnvCondition other);

    /*
     * supported profiles (such as maven profiles)
     *
     * @return supported supported profiles
     */
    @NGetter
    List<String> profiles();

    /**
     * supported arch list. if empty, all arch are supported (for example for java, all arch are supported).
     *
     * @return supported arch list
     */
    @NGetter
    List<String> arch();

    /**
     * supported operating systems. if empty, all oses are supported (for example for java, all arch are supported).
     *
     * @return supported oses
     */
    @NGetter
    List<String> os();

    /**
     * supported operating system distributions (mostly for linux systems). if empty, all distributions are supported.
     *
     * @return supported operating system distributions
     */
    @NGetter
    List<String> osDist();

    /**
     * supported platforms (java, dotnet, ...). if empty platform is not relevant.
     * This is helpful to bind application to a jdk version for instance (in that case platform may be in the form java#8 for instance)
     *
     * @return supported platforms
     */
    @NGetter
    List<String> platform();

    /**
     * supported desktop environments (gnome, kde, none, ...). if empty desktop environment is not relevant.
     * This is helpful to bind application to a specific environment
     *
     * @return supported platforms
     */
    @NGetter
    List<String> desktopEnvironment();

    /**
     * return env properties
     *
     * @return env properties
     * @since 0.8.4
     */
    @NGetter
    Map<String, String> properties();

    /**
     * Converts to map.
     *
     * @return to map result
     */
    Map<String, String> toMap();

}

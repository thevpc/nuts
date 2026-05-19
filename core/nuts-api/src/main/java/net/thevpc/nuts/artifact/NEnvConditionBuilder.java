package net.thevpc.nuts.artifact;


import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

import java.util.List;
import java.util.Map;

public interface NEnvConditionBuilder extends NComponent, NBlankable {
    static NEnvConditionBuilder of() {
        return NExtensions.of(NEnvConditionBuilder.class);
    }

    @NSetter
    NEnvConditionBuilder arch(List<String> arch);

    @NSetter
    NEnvConditionBuilder os(List<String> os);

    @NSetter
    NEnvConditionBuilder osDist(List<String> osDist);

    @NSetter
    NEnvConditionBuilder platform(List<String> platform);

    @NSetter
    NEnvConditionBuilder desktopEnvironment(List<String> desktopEnvironment);

    @NSetter
    NEnvConditionBuilder profile(List<String> profiles);

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

    Map<String, String> toMap();

}

package net.thevpc.nuts;

import java.util.Map;

public interface NutsDescriptorLicense {

    String getName();

    String getUrl();

    String getDistribution();

    String getComments();

    String getId();

    Map<String, String> getProperties();
    NutsDescriptorLicense readOnly();

    NutsDescriptorLicenseBuilder builder();
}

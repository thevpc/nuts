package net.thevpc.nuts;

import java.util.Map;

public interface NutsDescriptorLicenseBuilder extends NutsDescriptorLicense {
    NutsDescriptorLicenseBuilder setName(String name);

    NutsDescriptorLicenseBuilder setUrl(String url);

    NutsDescriptorLicenseBuilder setDistribution(String distribution);

    NutsDescriptorLicenseBuilder setComments(String comments);

    NutsDescriptorLicenseBuilder setId(String id);

    NutsDescriptorLicenseBuilder setProperties(Map<String, String> properties);

    NutsDescriptorLicenseBuilder copy();

    NutsDescriptorLicense build();
}

package net.thevpc.nuts;

import java.util.Map;

public interface NDescriptorLicenseBuilder extends NDescriptorLicense {
    NDescriptorLicenseBuilder setName(String name);

    NDescriptorLicenseBuilder setUrl(String url);

    NDescriptorLicenseBuilder setDistribution(String distribution);

    NDescriptorLicenseBuilder setComments(String comments);

    NDescriptorLicenseBuilder setId(String id);

    NDescriptorLicenseBuilder setProperties(Map<String, String> properties);

    NDescriptorLicenseBuilder copy();

    NDescriptorLicense build();
}

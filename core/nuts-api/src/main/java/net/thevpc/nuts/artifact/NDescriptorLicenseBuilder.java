package net.thevpc.nuts.artifact;

import net.thevpc.nuts.util.NSetter;

import java.util.Map;

public interface NDescriptorLicenseBuilder extends NDescriptorLicense {
    @NSetter
    NDescriptorLicenseBuilder name(String name);

    @NSetter
    NDescriptorLicenseBuilder url(String url);

    @NSetter
    NDescriptorLicenseBuilder distribution(String distribution);

    @NSetter
    NDescriptorLicenseBuilder comments(String comments);

    @NSetter
    NDescriptorLicenseBuilder id(String id);

    @NSetter
    NDescriptorLicenseBuilder properties(Map<String, String> properties);

    NDescriptorLicenseBuilder copy();

    NDescriptorLicense build();
}

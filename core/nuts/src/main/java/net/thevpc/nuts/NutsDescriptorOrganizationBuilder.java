package net.thevpc.nuts;

import java.util.Map;

public interface NutsDescriptorOrganizationBuilder extends NutsDescriptorOrganization {
    NutsDescriptorOrganizationBuilder setName(String name);

    NutsDescriptorOrganizationBuilder setUrl(String url);

    NutsDescriptorOrganizationBuilder setComments(String comments);

    NutsDescriptorOrganizationBuilder setId(String id);

    NutsDescriptorOrganizationBuilder setProperties(Map<String, String> properties);

    NutsDescriptorOrganization build();

    NutsDescriptorOrganizationBuilder copy();
}

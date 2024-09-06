package net.thevpc.nuts;

import java.util.Map;

public interface NDescriptorOrganizationBuilder extends NDescriptorOrganization {
    NDescriptorOrganizationBuilder setName(String name);

    NDescriptorOrganizationBuilder setUrl(String url);

    NDescriptorOrganizationBuilder setComments(String comments);

    NDescriptorOrganizationBuilder setId(String id);

    NDescriptorOrganizationBuilder setProperties(Map<String, String> properties);

    NDescriptorOrganization build();

    NDescriptorOrganizationBuilder copy();
}

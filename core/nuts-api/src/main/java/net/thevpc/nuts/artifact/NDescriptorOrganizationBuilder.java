package net.thevpc.nuts.artifact;

import net.thevpc.nuts.util.NSetter;

import java.util.Map;

public interface NDescriptorOrganizationBuilder extends NDescriptorOrganization {
    @NSetter
    NDescriptorOrganizationBuilder name(String name);

    @NSetter
    NDescriptorOrganizationBuilder url(String url);

    @NSetter
    NDescriptorOrganizationBuilder comments(String comments);

    @NSetter
    NDescriptorOrganizationBuilder id(String id);

    @NSetter
    NDescriptorOrganizationBuilder properties(Map<String, String> properties);

    NDescriptorOrganization build();

    NDescriptorOrganizationBuilder copy();
}

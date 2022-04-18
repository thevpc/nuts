package net.thevpc.nuts;


import java.util.List;
import java.util.Map;

public interface NutsDescriptorContributorBuilder extends NutsDescriptorContributor {
    NutsDescriptorContributorBuilder setId(String id);

    NutsDescriptorContributorBuilder setName(String name);

    NutsDescriptorContributorBuilder setUrl(String url);

    NutsDescriptorContributorBuilder setEmail(String email);

    NutsDescriptorContributorBuilder setRoles(List<String> roles);

    NutsDescriptorContributorBuilder setTimezone(String timezone);

    NutsDescriptorContributorBuilder setIcons(List<String> icons);

    NutsDescriptorContributorBuilder setProperties(Map<String, String> properties);

    NutsDescriptorContributorBuilder setComments(String comments);

    NutsDescriptorContributorBuilder setOrganization(NutsDescriptorOrganization organization);

    NutsDescriptorContributor build();

    NutsDescriptorContributorBuilder copy();
}

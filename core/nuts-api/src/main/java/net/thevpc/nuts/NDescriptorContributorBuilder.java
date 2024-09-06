package net.thevpc.nuts;


import java.util.List;
import java.util.Map;

public interface NDescriptorContributorBuilder extends NDescriptorContributor {
    NDescriptorContributorBuilder setId(String id);

    NDescriptorContributorBuilder setName(String name);

    NDescriptorContributorBuilder setUrl(String url);

    NDescriptorContributorBuilder setEmail(String email);

    NDescriptorContributorBuilder setRoles(List<String> roles);

    NDescriptorContributorBuilder setTimezone(String timezone);

    NDescriptorContributorBuilder setIcons(List<String> icons);

    NDescriptorContributorBuilder setProperties(Map<String, String> properties);

    NDescriptorContributorBuilder setComments(String comments);

    NDescriptorContributorBuilder setOrganization(NDescriptorOrganization organization);

    NDescriptorContributor build();

    NDescriptorContributorBuilder copy();
}

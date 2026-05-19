package net.thevpc.nuts.artifact;


import net.thevpc.nuts.util.NSetter;

import java.util.List;
import java.util.Map;

public interface NDescriptorContributorBuilder extends NDescriptorContributor {
    @NSetter
    NDescriptorContributorBuilder id(String id);

    @NSetter
    NDescriptorContributorBuilder name(String name);

    @NSetter
    NDescriptorContributorBuilder url(String url);

    @NSetter
    NDescriptorContributorBuilder email(String email);

    @NSetter
    NDescriptorContributorBuilder roles(List<String> roles);

    @NSetter
    NDescriptorContributorBuilder timezone(String timezone);

    @NSetter
    NDescriptorContributorBuilder icons(List<String> icons);

    @NSetter
    NDescriptorContributorBuilder properties(Map<String, String> properties);

    @NSetter
    NDescriptorContributorBuilder comments(String comments);

    @NSetter
    NDescriptorContributorBuilder organization(NDescriptorOrganization organization);

    NDescriptorContributor build();

    NDescriptorContributorBuilder copy();
}

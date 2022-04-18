package net.thevpc.nuts;


import java.util.List;
import java.util.Map;

public interface NutsDescriptorContributor {
    String getId();

    String getName();

    String getUrl();

    String getEmail();

    List<String> getRoles();

    String getTimezone();

    List<String> getIcons();

    Map<String, String> getProperties();

    String getComments();

    NutsDescriptorOrganization getOrganization();

    NutsDescriptorContributor readOnly();

    NutsDescriptorContributorBuilder builder();
}

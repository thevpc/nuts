package net.thevpc.nuts;


import java.util.List;
import java.util.Map;

public interface NDescriptorContributor {
    String getId();

    String getName();

    String getUrl();

    String getEmail();

    List<String> getRoles();

    String getTimezone();

    List<String> getIcons();

    Map<String, String> getProperties();

    String getComments();

    NDescriptorOrganization getOrganization();

    NDescriptorContributor readOnly();

    NDescriptorContributorBuilder builder();
}

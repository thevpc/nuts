package net.thevpc.nuts;

import java.util.Map;

public interface NutsDescriptorOrganization {
    String getName();

    String getUrl();

    String getComments();

    String getId();

    Map<String, String> getProperties();

    NutsDescriptorOrganization readOnly();

    NutsDescriptorOrganizationBuilder builder();
}

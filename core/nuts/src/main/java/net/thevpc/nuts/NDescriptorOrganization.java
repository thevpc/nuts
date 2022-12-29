package net.thevpc.nuts;

import java.util.Map;

public interface NDescriptorOrganization {
    String getName();

    String getUrl();

    String getComments();

    String getId();

    Map<String, String> getProperties();

    NDescriptorOrganization readOnly();

    NDescriptorOrganizationBuilder builder();
}

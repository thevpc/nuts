package net.thevpc.nuts.artifact;

import net.thevpc.nuts.util.NGetter;

import java.util.Map;

public interface NDescriptorOrganization {
    @NGetter
    String name();

    @NGetter
    String url();

    @NGetter
    String comments();

    @NGetter
    String id();

    @NGetter
    Map<String, String> properties();

    NDescriptorOrganization readOnly();

    NDescriptorOrganizationBuilder builder();
}

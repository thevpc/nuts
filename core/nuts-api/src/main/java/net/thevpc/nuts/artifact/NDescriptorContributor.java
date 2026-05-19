package net.thevpc.nuts.artifact;


import net.thevpc.nuts.util.NGetter;

import java.util.List;
import java.util.Map;

public interface NDescriptorContributor {
    @NGetter
    String id();

    @NGetter
    String name();

    @NGetter
    String url();

    @NGetter
    String email();

    @NGetter
    List<String> roles();

    @NGetter
    String timezone();

    @NGetter
    List<String> icons();

    @NGetter
    Map<String, String> properties();

    @NGetter
    String comments();

    @NGetter
    NDescriptorOrganization organization();

    NDescriptorContributor readOnly();

    NDescriptorContributorBuilder builder();
}

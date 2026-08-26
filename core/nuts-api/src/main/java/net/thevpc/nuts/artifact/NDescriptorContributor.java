package net.thevpc.nuts.artifact;


import net.thevpc.nuts.util.NGetter;

import java.util.List;
import java.util.Map;

/**
 * NDescriptorContributor interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NDescriptorContributor {
    /**
     * Id.
     *
     * @return id result
     */
    @NGetter
    String id();

    /**
     * Name.
     *
     * @return name result
     */
    @NGetter
    String name();

    /**
     * Url.
     *
     * @return url result
     */
    @NGetter
    String url();

    /**
     * Email.
     *
     * @return email result
     */
    @NGetter
    String email();

    /**
     * Roles.
     *
     * @return roles result
     */
    @NGetter
    List<String> roles();

    /**
     * Timezone.
     *
     * @return timezone result
     */
    @NGetter
    String timezone();

    /**
     * Icons.
     *
     * @return icons result
     */
    @NGetter
    List<String> icons();

    /**
     * Properties.
     *
     * @return properties result
     */
    @NGetter
    Map<String, String> properties();

    /**
     * Comments.
     *
     * @return comments result
     */
    @NGetter
    String comments();

    /**
     * Organization.
     *
     * @return organization result
     */
    @NGetter
    NDescriptorOrganization organization();

    /**
     * Read only.
     *
     * @return read only result
     */
    NDescriptorContributor readOnly();

    /**
     * Builder.
     *
     * @return builder result
     */
    NDescriptorContributorBuilder builder();
}

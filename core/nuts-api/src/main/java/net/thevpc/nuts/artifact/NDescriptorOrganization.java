package net.thevpc.nuts.artifact;

import net.thevpc.nuts.util.NGetter;

import java.util.Map;

/**
 * NDescriptorOrganization interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NDescriptorOrganization {
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
     * Comments.
     *
     * @return comments result
     */
    @NGetter
    String comments();

    /**
     * Id.
     *
     * @return id result
     */
    @NGetter
    String id();

    /**
     * Properties.
     *
     * @return properties result
     */
    @NGetter
    Map<String, String> properties();

    /**
     * Read only.
     *
     * @return read only result
     */
    NDescriptorOrganization readOnly();

    /**
     * Builder.
     *
     * @return builder result
     */
    NDescriptorOrganizationBuilder builder();
}

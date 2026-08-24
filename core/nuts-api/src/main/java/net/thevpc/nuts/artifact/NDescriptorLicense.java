package net.thevpc.nuts.artifact;

import net.thevpc.nuts.util.NGetter;

import java.util.Map;

/**
 * NDescriptorLicense interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NDescriptorLicense {

    /**
     * Name.
     *
     * @return name result
     */
    @NGetter
    String name();

    /**
     * Date.
     *
     * @return date result
     */
    @NGetter
    String date();

    /**
     * Url.
     *
     * @return url result
     */
    @NGetter
    String url();

    /**
     * Distribution.
     *
     * @return distribution result
     */
    @NGetter
    String distribution();

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
    NDescriptorLicense readOnly();

    /**
     * Builder.
     *
     * @return builder result
     */
    NDescriptorLicenseBuilder builder();
}

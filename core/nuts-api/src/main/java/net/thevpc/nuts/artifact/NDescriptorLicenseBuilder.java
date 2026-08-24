package net.thevpc.nuts.artifact;

import net.thevpc.nuts.util.NSetter;

import java.util.Map;

/**
 * NDescriptorLicenseBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NDescriptorLicenseBuilder extends NDescriptorLicense {
    /**
     * Name.
     *
     * @param name name
     * @return name result
     */
    @NSetter
    NDescriptorLicenseBuilder name(String name);

    /**
     * Url.
     *
     * @param url url
     * @return url result
     */
    @NSetter
    NDescriptorLicenseBuilder url(String url);

    /**
     * Distribution.
     *
     * @param distribution distribution
     * @return distribution result
     */
    @NSetter
    NDescriptorLicenseBuilder distribution(String distribution);

    /**
     * Comments.
     *
     * @param comments comments
     * @return comments result
     */
    @NSetter
    NDescriptorLicenseBuilder comments(String comments);

    /**
     * Id.
     *
     * @param id id
     * @return id result
     */
    @NSetter
    NDescriptorLicenseBuilder id(String id);

    /**
     * Properties.
     *
     * @param properties properties
     * @return properties result
     */
    @NSetter
    NDescriptorLicenseBuilder properties(Map<String, String> properties);

    /**
     * Copy.
     *
     * @return copy result
     */
    NDescriptorLicenseBuilder copy();

    /**
     * Build.
     *
     * @return build result
     */
    NDescriptorLicense build();
}

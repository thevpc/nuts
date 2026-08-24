package net.thevpc.nuts.artifact;

import net.thevpc.nuts.util.NSetter;

import java.util.Map;

/**
 * NDescriptorOrganizationBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NDescriptorOrganizationBuilder extends NDescriptorOrganization {
    /**
     * Name.
     *
     * @param name name
     * @return name result
     */
    @NSetter
    NDescriptorOrganizationBuilder name(String name);

    /**
     * Url.
     *
     * @param url url
     * @return url result
     */
    @NSetter
    NDescriptorOrganizationBuilder url(String url);

    /**
     * Comments.
     *
     * @param comments comments
     * @return comments result
     */
    @NSetter
    NDescriptorOrganizationBuilder comments(String comments);

    /**
     * Id.
     *
     * @param id id
     * @return id result
     */
    @NSetter
    NDescriptorOrganizationBuilder id(String id);

    /**
     * Properties.
     *
     * @param properties properties
     * @return properties result
     */
    @NSetter
    NDescriptorOrganizationBuilder properties(Map<String, String> properties);

    /**
     * Build.
     *
     * @return build result
     */
    NDescriptorOrganization build();

    /**
     * Copy.
     *
     * @return copy result
     */
    NDescriptorOrganizationBuilder copy();
}

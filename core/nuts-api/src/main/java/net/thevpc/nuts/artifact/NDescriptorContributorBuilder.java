package net.thevpc.nuts.artifact;


import net.thevpc.nuts.util.NSetter;

import java.util.List;
import java.util.Map;

/**
 * NDescriptorContributorBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NDescriptorContributorBuilder extends NDescriptorContributor {
    /**
     * Id.
     *
     * @param id id
     * @return id result
     */
    @NSetter
    NDescriptorContributorBuilder id(String id);

    /**
     * Name.
     *
     * @param name name
     * @return name result
     */
    @NSetter
    NDescriptorContributorBuilder name(String name);

    /**
     * Url.
     *
     * @param url url
     * @return url result
     */
    @NSetter
    NDescriptorContributorBuilder url(String url);

    /**
     * Email.
     *
     * @param email email
     * @return email result
     */
    @NSetter
    NDescriptorContributorBuilder email(String email);

    /**
     * Roles.
     *
     * @param roles roles
     * @return roles result
     */
    @NSetter
    NDescriptorContributorBuilder roles(List<String> roles);

    /**
     * Timezone.
     *
     * @param timezone timezone
     * @return timezone result
     */
    @NSetter
    NDescriptorContributorBuilder timezone(String timezone);

    /**
     * Icons.
     *
     * @param icons icons
     * @return icons result
     */
    @NSetter
    NDescriptorContributorBuilder icons(List<String> icons);

    /**
     * Properties.
     *
     * @param properties properties
     * @return properties result
     */
    @NSetter
    NDescriptorContributorBuilder properties(Map<String, String> properties);

    /**
     * Comments.
     *
     * @param comments comments
     * @return comments result
     */
    @NSetter
    NDescriptorContributorBuilder comments(String comments);

    /**
     * Organization.
     *
     * @param organization organization
     * @return organization result
     */
    @NSetter
    NDescriptorContributorBuilder organization(NDescriptorOrganization organization);

    /**
     * Build.
     *
     * @return build result
     */
    NDescriptorContributor build();

    /**
     * Copy.
     *
     * @return copy result
     */
    NDescriptorContributorBuilder copy();
}

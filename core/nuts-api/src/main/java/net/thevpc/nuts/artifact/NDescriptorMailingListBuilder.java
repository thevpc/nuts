package net.thevpc.nuts.artifact;

import net.thevpc.nuts.util.NSetter;

import java.util.List;
import java.util.Map;

/**
 * NDescriptorMailingListBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NDescriptorMailingListBuilder extends NDescriptorMailingList {
    /**
     * Id.
     *
     * @param id id
     * @return id result
     */
    @NSetter
    NDescriptorMailingListBuilder id(String id);

    /**
     * Name.
     *
     * @param name name
     * @return name result
     */
    @NSetter
    NDescriptorMailingListBuilder name(String name);

    /**
     * Subscribe.
     *
     * @param subscribe subscribe
     * @return subscribe result
     */
    @NSetter
    NDescriptorMailingListBuilder subscribe(String subscribe);

    /**
     * Unsubscribe.
     *
     * @param unsubscribe unsubscribe
     * @return unsubscribe result
     */
    @NSetter
    NDescriptorMailingListBuilder unsubscribe(String unsubscribe);

    /**
     * Post.
     *
     * @param post post
     * @return post result
     */
    @NSetter
    NDescriptorMailingListBuilder post(String post);

    /**
     * Archive.
     *
     * @param archive archive
     * @return archive result
     */
    @NSetter
    NDescriptorMailingListBuilder archive(String archive);

    /**
     * Other archives.
     *
     * @param otherArchives other archives
     * @return other archives result
     */
    @NSetter
    NDescriptorMailingListBuilder otherArchives(List<String> otherArchives);

    /**
     * Properties.
     *
     * @param properties properties
     * @return properties result
     */
    @NSetter
    NDescriptorMailingListBuilder properties(Map<String, String> properties);

    /**
     * Comments.
     *
     * @param comments comments
     * @return comments result
     */
    @NSetter
    NDescriptorMailingListBuilder comments(String comments);

    /**
     * Build.
     *
     * @return build result
     */
    NDescriptorMailingList build();

    /**
     * Copy.
     *
     * @return copy result
     */
    NDescriptorMailingListBuilder copy();

}

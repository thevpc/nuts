package net.thevpc.nuts.artifact;

import net.thevpc.nuts.util.NGetter;

import java.util.List;
import java.util.Map;

/**
 * NDescriptorMailingList interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NDescriptorMailingList {
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
     * Subscribe.
     *
     * @return subscribe result
     */
    @NGetter
    String subscribe();

    /**
     * Unsubscribe.
     *
     * @return unsubscribe result
     */
    @NGetter
    String unsubscribe();

    /**
     * Post.
     *
     * @return post result
     */
    @NGetter
    String post();

    /**
     * Archive.
     *
     * @return archive result
     */
    @NGetter
    String archive();

    /**
     * Other archives.
     *
     * @return other archives result
     */
    @NGetter
    List<String> otherArchives();

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
     * Read only.
     *
     * @return read only result
     */
    NDescriptorMailingList readOnly();

    /**
     * Builder.
     *
     * @return builder result
     */
    NDescriptorMailingListBuilder builder();
}

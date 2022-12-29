package net.thevpc.nuts;

import java.util.List;
import java.util.Map;

public interface NDescriptorMailingListBuilder extends NDescriptorMailingList {
    NDescriptorMailingListBuilder setId(String id);

    NDescriptorMailingListBuilder setName(String name);

    NDescriptorMailingListBuilder setSubscribe(String subscribe);

    NDescriptorMailingListBuilder setUnsubscribe(String unsubscribe);

    NDescriptorMailingListBuilder setPost(String post);

    NDescriptorMailingListBuilder setArchive(String archive);

    NDescriptorMailingListBuilder setOtherArchives(List<String> otherArchives);

    NDescriptorMailingListBuilder setProperties(Map<String, String> properties);

    NDescriptorMailingListBuilder setComments(String comments);

    NDescriptorMailingList build();

    NDescriptorMailingListBuilder copy();

}

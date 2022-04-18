package net.thevpc.nuts;

import java.util.List;
import java.util.Map;

public interface NutsDescriptorMailingListBuilder extends NutsDescriptorMailingList {
    NutsDescriptorMailingListBuilder setId(String id);

    NutsDescriptorMailingListBuilder setName(String name);

    NutsDescriptorMailingListBuilder setSubscribe(String subscribe);

    NutsDescriptorMailingListBuilder setUnsubscribe(String unsubscribe);

    NutsDescriptorMailingListBuilder setPost(String post);

    NutsDescriptorMailingListBuilder setArchive(String archive);

    NutsDescriptorMailingListBuilder setOtherArchives(List<String> otherArchives);

    NutsDescriptorMailingListBuilder setProperties(Map<String, String> properties);

    NutsDescriptorMailingListBuilder setComments(String comments);

    NutsDescriptorMailingList build();

    NutsDescriptorMailingListBuilder copy();

}

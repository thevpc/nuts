package net.thevpc.nuts.artifact;

import net.thevpc.nuts.util.NSetter;

import java.util.List;
import java.util.Map;

public interface NDescriptorMailingListBuilder extends NDescriptorMailingList {
    @NSetter
    NDescriptorMailingListBuilder id(String id);

    @NSetter
    NDescriptorMailingListBuilder name(String name);

    @NSetter
    NDescriptorMailingListBuilder subscribe(String subscribe);

    @NSetter
    NDescriptorMailingListBuilder unsubscribe(String unsubscribe);

    @NSetter
    NDescriptorMailingListBuilder post(String post);

    @NSetter
    NDescriptorMailingListBuilder archive(String archive);

    @NSetter
    NDescriptorMailingListBuilder otherArchives(List<String> otherArchives);

    @NSetter
    NDescriptorMailingListBuilder properties(Map<String, String> properties);

    @NSetter
    NDescriptorMailingListBuilder comments(String comments);

    NDescriptorMailingList build();

    NDescriptorMailingListBuilder copy();

}

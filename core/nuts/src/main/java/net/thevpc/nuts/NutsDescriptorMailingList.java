package net.thevpc.nuts;

import java.util.List;
import java.util.Map;

public interface NutsDescriptorMailingList {
    String getId();

    String getName();

    String getSubscribe();

    String getUnsubscribe();

    String getPost();

    String getArchive();

    List<String> getOtherArchives();

    Map<String, String> getProperties();

    String getComments();

    NutsDescriptorMailingList readOnly();

    NutsDescriptorMailingListBuilder builder();
}

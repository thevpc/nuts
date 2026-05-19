package net.thevpc.nuts.artifact;

import net.thevpc.nuts.util.NGetter;

import java.util.List;
import java.util.Map;

public interface NDescriptorMailingList {
    @NGetter
    String id();

    @NGetter
    String name();

    @NGetter
    String subscribe();

    @NGetter
    String unsubscribe();

    @NGetter
    String post();

    @NGetter
    String archive();

    @NGetter
    List<String> otherArchives();

    @NGetter
    Map<String, String> properties();

    @NGetter
    String comments();

    NDescriptorMailingList readOnly();

    NDescriptorMailingListBuilder builder();
}

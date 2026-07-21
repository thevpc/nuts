package net.thevpc.nuts.text;

import net.thevpc.nuts.spi.NComponent;

import java.util.List;

public interface NMsgCustomFormatter extends NComponent {
    String id();

    NText format(NMsg msg);

    List<String> extractParams(String message);
}

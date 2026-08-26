package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.text.NMsgType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NFormattedTextParts {
    private List<NFormattedTextPart> parts;
    private List<String> formats;
    private NMsgType type;

    public NFormattedTextParts(NMsgType type, List<NFormattedTextPart> parts) {
        this.type = type;
        this.parts = new ArrayList<>(parts);
    }

    public NFormattedTextPart[] getParts() {
        return parts.toArray(new NFormattedTextPart[0]);
    }

    public String getFormatAt(int index) {
        String[] formats1 = getFormats();
        if (index >= 0 && index < formats1.length) {
            return formats1[index];
        }
        return null;
    }

    public String[] getFormats() {
        if (formats == null) {
            formats = parts.stream().filter(NFormattedTextPart::isFormat).map(NFormattedTextPart::getValue).collect(Collectors.toList());
        }
        return formats.toArray(new String[0]);
    }


}

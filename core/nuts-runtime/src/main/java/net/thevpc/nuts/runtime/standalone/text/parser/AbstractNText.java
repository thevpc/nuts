package net.thevpc.nuts.runtime.standalone.text.parser;

import net.thevpc.nuts.runtime.standalone.text.NTextNodeWriterStringer;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NBlankable;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractNText implements NText {

    public AbstractNText() {
    }

    @Override
    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NTextNodeWriterStringer ss = new NTextNodeWriterStringer(out);
        ss.writeNode(this);
        return out.toString();
    }

    @Override
    public String filteredText() {
        return immutable().filteredText();
    }

    @Override
    public int length() {
        return immutable().length();
    }

    @Override
    public boolean isEmpty() {
        return immutable().isEmpty();
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(filteredText());
    }

    @Override
    public NTextBuilder builder() {
        return NTextBuilder.of().append(this);
    }

    @Override
    public boolean isPrimitive() {
        return this instanceof NPrimitiveText;
    }

    @Override
    public boolean isNormalized() {
        return this instanceof NNormalizedText;
    }

    @Override
    public List<NText> split(char c) {
        return split(String.valueOf(c), false);
    }

    @Override
    public List<NText> split(char c, boolean returnSeparator) {
        return split(String.valueOf(c), returnSeparator);
    }

    @Override
    public List<NText> split(String separator) {
        return split(separator, false);
    }

    @Override
    public NPrimitiveText[] toCharArray() {
        return toCharList().toArray(new NPrimitiveText[0]);
    }

    @Override
    public NText repeat(int times) {
        if (times <= 0) {
            return NText.ofBlank();
        }
        if (times == 1) {
            return this;
        }
        NTextBuilder b = NTextBuilder.of();
        for (int i = 0; i < times; i++) {
            b.append(this);
        }
        return b.build();
    }

    @Override
    public NText concat(NText other) {
        if (other == null) {
            return this;
        }
        return NText.ofList(this, other).simplify();
    }

    @Override
    public NText concat(NText... others) {
        List<NText> aa = new ArrayList<>();
        aa.add(this);
        if (others != null) {
            aa.addAll(Arrays.asList(others));
        }
        return NText.ofList(aa).simplify();
    }

    public NNormalizedText normalize() {
        return NTexts.of().normalize(this);
    }

    @Override
    public NNormalizedText normalize(NTextTransformConfig config) {
        return NTexts.of().normalize(this, config);
    }

    @Override
    public NNormalizedText normalize(NTextTransformer transformer, NTextTransformConfig config) {
        return NTexts.of().normalize(this, transformer, config);
    }
}

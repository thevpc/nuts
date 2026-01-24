package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNElementNewLine;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNElementSpace;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.text.NNewLineMode;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.List;

public class DefaultNElementFormatterBuilder implements NElementFormatterBuilder, NElementFormatOptions {
    private int indent=1;

    private int maxWidth=120;

    private int complexityThreshold=30;
    private int columnLimit =80;
    private NNewLineMode newLineMode = NNewLineMode.AUTO;
    private NContentType contentType;
    private NElementFormatterStyle style = NElementFormatterStyle.CUSTOM;
    private List<NElementFormatterAction> actions = new ArrayList<>();


    public int getColumnLimit() {
        return columnLimit;
    }

    @Override
    public NElementFormatterBuilder setColumnLimit(int columnLimit) {
        this.columnLimit = columnLimit;
        return this;
    }

    public NElementFormatterBuilder addAction(NElementFormatterAction action) {
        if(action!=null){
            actions.add(action);
        }
        return this;
    }

    @Override
    public NElementFormatterBuilder addSpace(NElementType elementType, NAffixAnchor anchor) {
        return addSpace(elementType, anchor, " ");
    }

    @Override
    public NNewLineMode getNewLineMode() {
        return newLineMode;
    }

    public DefaultNElementFormatterBuilder setNewLineMode(NNewLineMode newLineMode) {
        this.newLineMode = newLineMode == null ? NNewLineMode.AUTO : newLineMode;
        return this;
    }

    @Override
    public NElementFormatterBuilder clear(NElementType elementType, NAffixType affixType, NAffixAnchor anchor) {
        addAction(new NElementFormatterAction() {
            @Override
            public void apply(NElementFormatContext context) {
                NElementBuilder builder = context.builder();
                if (elementType != null && elementType != builder.type()) {
                    return;
                }
                List<NBoundAffix> all = builder.affixes();
                for (int i = all.size() - 1; i >= 0; i--) {
                    NBoundAffix a = all.get(i);
                    if (affixType != null && affixType != a.affix().type()) {
                        continue;
                    }
                    if (anchor != null && anchor != a.anchor()) {
                        continue;
                    }
                    builder.removeAffix(i);
                }
            }
        });
        return this;
    }

    @Override
    public NElementFormatterBuilder setSpaces(NElementType elementType, NAffixAnchor anchor, int count) {
        return setSpace(elementType, anchor, NStringUtils.repeat(" ", count));
    }

    @Override
    public NElementFormatterBuilder setSpace(NElementType elementType, NAffixAnchor anchor) {
        return setSpace(elementType, anchor, " ");
    }

    @Override
    public NElementFormatterBuilder setTabs(NElementType elementType, NAffixAnchor anchor, int count) {
        return setSpace(elementType, anchor, NStringUtils.repeat("\t", count));
    }

    @Override
    public NElementFormatterBuilder setTab(NElementType elementType, NAffixAnchor anchor) {
        return setSpace(elementType, anchor, "\t");
    }

    @Override
    public NElementFormatterBuilder setSpace(NElementType elementType, NAffixAnchor anchor, String space) {
        addAction(new NElementFormatterAction() {
            public void apply(NElementFormatContext context) {
                NElementBuilder builder = context.builder();
                if (elementType != null && elementType != builder.type()) {
                    return;
                }
                List<NBoundAffix> all = builder.affixes();
                boolean set = false;
                for (int i = all.size() - 1; i >= 0; i--) {
                    NBoundAffix a = all.get(i);
                    if (NAffixType.SPACE != a.affix().type()) {
                        continue;
                    }
                    if (anchor != null && anchor != a.anchor()) {
                        continue;
                    }
                    if (set) {
                        builder.removeAffix(i);
                    } else if (!NBlankable.isBlank(space)) {
                        builder.setAffix(i, NBoundAffix.of(DefaultNElementSpace.of(space), anchor));
                        set = true;
                    } else {
                        builder.removeAffix(i);
                        set = true;
                    }
                }
                if (!set) {
                    if (!NBlankable.isBlank(space)) {
                        builder.addAffix(NBoundAffix.of(DefaultNElementSpace.of(space), anchor));
                    }
                }
            }
        });
        return this;
    }

    @Override
    public NElementFormatterBuilder setNewline(NElementType elementType, NAffixAnchor anchor) {
        return setNewline(elementType, anchor, NNewLineMode.AUTO);
    }

    @Override
    public NElementFormatterBuilder addNewline(NElementType elementType, NAffixAnchor anchor) {
        return addNewline(elementType, anchor, NNewLineMode.AUTO);
    }

    @Override
    public NElementFormatterBuilder addNewline(NElementType elementType, NAffixAnchor anchor, NNewLineMode space) {
        addAction(new NElementFormatterAction() {
            public void apply(NElementFormatContext context) {
                NElementBuilder builder = context.builder();
                if (elementType != null && elementType != builder.type()) {
                    return;
                }
                if (space != null && !NBlankable.isBlank(space)) {
                    builder.addAffix(NBoundAffix.of(DefaultNElementNewLine.of(space), anchor));
                }
            }
        });
        return this;
    }

    @Override
    public NElementFormatterBuilder setNewline(NElementType elementType, NAffixAnchor anchor, NNewLineMode space) {
        addAction(new NElementFormatterAction() {
            public void apply(NElementFormatContext context) {
                NElementBuilder builder = context.builder();
                if (elementType != null && elementType != builder.type()) {
                    return;
                }
                List<NBoundAffix> all = builder.affixes();
                boolean set = false;
                for (int i = all.size() - 1; i >= 0; i--) {
                    NBoundAffix a = all.get(i);
                    if (NAffixType.NEWLINE != a.affix().type()) {
                        continue;
                    }
                    if (anchor != null && anchor != a.anchor()) {
                        continue;
                    }
                    if (set) {
                        builder.removeAffix(i);
                    } else if (space != null && !NBlankable.isBlank(space)) {
                        builder.setAffix(i, NBoundAffix.of(DefaultNElementNewLine.of(space), anchor));
                        set = true;
                    } else {
                        builder.removeAffix(i);
                        set = true;
                    }
                }
                if (!set) {
                    if (space != null && !NBlankable.isBlank(space)) {
                        builder.addAffix(NBoundAffix.of(DefaultNElementNewLine.of(space), anchor));
                    }
                }
            }
        });
        return this;
    }

    @Override
    public NElementFormatterBuilder removeSpace(NElementType elementType, NAffixAnchor anchor) {
        return setSpace(elementType, anchor, null);
    }

    @Override
    public NElementFormatterBuilder addSpace(NElementType elementType, NAffixAnchor anchor, String space) {
        addAction(new NElementFormatterAction() {
            public void apply(NElementFormatContext context) {
                NElementBuilder builder = context.builder();
                if (elementType != null && elementType != builder.type()) {
                    return;
                }
                if (!NBlankable.isBlank(space)) {
                    builder.addAffix(NBoundAffix.of(DefaultNElementSpace.of(space), anchor));
                }
            }
        });
        return this;
    }

    @Override
    public NContentType getContentType() {
        return contentType;
    }

    @Override
    public DefaultNElementFormatterBuilder setContentType(NContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    @Override
    public NElementFormatterStyle getStyle() {
        return style;
    }

    @Override
    public DefaultNElementFormatterBuilder setStyle(NElementFormatterStyle style) {
        if (style == null) {
            //do nothing
        } else if (style != this.style) {
            this.style = style;
        }
        return this;
    }

    @Override
    public int getIndent() {
        return indent;
    }

    @Override
    public DefaultNElementFormatterBuilder setIndent(int indent) {
        this.indent = indent;
        return this;
    }

    @Override
    public int getMaxWidth() {
        return maxWidth;
    }

    @Override
    public DefaultNElementFormatterBuilder setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    @Override
    public int getComplexityThreshold() {
        return complexityThreshold;
    }

    @Override
    public DefaultNElementFormatterBuilder setComplexityThreshold(int complexityThreshold) {
        this.complexityThreshold = complexityThreshold;
        return this;
    }

    @Override
    public NElementFormatter build() {
        switch (style) {
            case COMPACT: {
                clear(null, null, null);
                break;
            }
            case PRETTY: {
                clear(null, null, null);
                addAction(new TsonFormatPrettyAction());
                break;
            }
        }
        return new DefaultNElementFormatter(actions, this);
    }

}

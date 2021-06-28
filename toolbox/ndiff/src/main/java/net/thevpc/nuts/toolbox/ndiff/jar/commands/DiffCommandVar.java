package net.thevpc.nuts.toolbox.ndiff.jar.commands;

import net.thevpc.nuts.toolbox.ndiff.jar.*;
import net.thevpc.nuts.toolbox.ndiff.jar.util.DiffUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DiffCommandVar extends AbstractDiffCommand {
    public static final DiffCommand INSTANCE = new DiffCommandVar();

    protected DiffCommandVar() {
        super("var");
    }

    @Override
    public int acceptInput(Object input) {
        return -1;
    }

    @Override
    public boolean acceptDiffKey(DiffKey key) {
        switch (key.getKind()) {
            case DiffKey.KIND_VAR: {
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<DiffKey, String> map(Object item, DiffEvalContext diffEvalContext) {
        return new HashMap<>();
    }

    @Override
    public String hash(InputStream source) {
        return DiffUtils.hashStream(source);
    }

    @Override
    public DiffItem createContentDiffItem(DiffItemCreateContext context) {
        switch (context.getStatus()) {
            case ADDED: {
                return new DefaultDiffItem("var",context.getKey().getName(), DiffStatus.ADDED, null, null);
            }
            case REMOVED: {
                return new DefaultDiffItem("var",context.getKey().getName(), DiffStatus.REMOVED, null, null);
            }
            case CHANGED: {
                return new DefaultDiffItem("var",context.getKey().getName(), DiffStatus.CHANGED, context.getSourceValue() + " ~ " + context.getTargetValue(), null);
            }
        }
        throw new UnsupportedOperationException();
    }
}

package net.thevpc.nuts.toolbox.ndiff.jar.commands;

import net.thevpc.nuts.toolbox.ndiff.jar.*;
import net.thevpc.nuts.toolbox.ndiff.jar.util.DiffUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DiffCommandDefault extends AbstractDiffCommand {
    public static final DiffCommand INSTANCE =new DiffCommandDefault();

    protected DiffCommandDefault() {
        super("default");
    }

    @Override
    public int acceptInput(Object input) {
        return -1;
    }

    @Override
    public boolean acceptDiffKey(DiffKey key) {
        return true;
    }

    @Override
    public String hash(InputStream source) {
        return DiffUtils.hashStream(source);
    }

    @Override
    public Map<DiffKey, String> map(Object item, DiffEvalContext diffEvalContext) {
        return new HashMap<>();
    }

    @Override
    public DiffItem createContentDiffItem(DiffItemCreateContext context) {
        switch (context.getStatus()) {
            case ADDED: {
                return new DefaultDiffItem("file",context.getKey().getName(), DiffStatus.ADDED, null, null);
            }
            case REMOVED: {
                return new DefaultDiffItem("file",context.getKey().getName(), DiffStatus.REMOVED, null, null);
            }
            case CHANGED: {
                return new DefaultDiffItem("file",context.getKey().getName(), DiffStatus.CHANGED,
                        context.getSourceValue() + " / " + context.getTargetValue(), null);
            }
        }
        throw new UnsupportedOperationException();
    }

}

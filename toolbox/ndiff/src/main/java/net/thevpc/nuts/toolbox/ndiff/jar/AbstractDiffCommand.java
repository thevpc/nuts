package net.thevpc.nuts.toolbox.ndiff.jar;

import net.thevpc.nuts.toolbox.ndiff.jar.commands.DiffCommandDefault;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public abstract class AbstractDiffCommand implements DiffCommand {
    private String id;

    public AbstractDiffCommand(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public Object prepareSourceOrTarget(Object source) {
        return null;
    }



    public DiffResult eval(DiffEvalContext context) {
        try {
            DefaultDiffEvalContext context2 = new DefaultDiffEvalContext(context);
            Object source = context2.getSource();
            Object target = context2.getTarget();
            Object source2 = source==null?null:prepareSourceOrTarget(source);
            Object target2 = target==null?null:prepareSourceOrTarget(target);
            if (source2 != null && source2 != source) {
                context2.setSource(source2);
            }
            if (target2 != null && target2 != target) {
                context2.setTarget(target2);
            }
            context = context2;
            Map<DiffKey, String> m1 = map(context.getSource(), context);
            Map<DiffKey, String> m2 = map(context.getTarget(), context);
            TreeSet<DiffKey> keys = new TreeSet<>(m1.keySet());
            keys.addAll(m2.keySet());
            TreeMap<DiffKey, String> missing1 = new TreeMap<>();
            TreeMap<DiffKey, String> missing2 = new TreeMap<>();
            TreeMap<DiffKey, String[]> diffs = new TreeMap<>();
            for (DiffKey key : keys) {
                String v1 = m1.get(key);
                String v2 = m2.get(key);
                if (v1 == null) {
                    missing1.put(key, v2);
                } else if (v2 == null) {
                    missing2.put(key, v1);
                } else {
                    if (!v1.equals(v2)) {
                        diffs.put(key, new String[]{v1, v2});
                    }
                }
            }
            return createResult(missing1, missing2, diffs, context);
        }finally {
            if(context.getSource() instanceof Closeable){
                try {
                    ((Closeable) context.getSource()).close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            if(context.getTarget() instanceof Closeable){
                try {
                    ((Closeable) context.getTarget()).close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
    }

    public DiffItemCreateContext createContext(DiffKey name, DiffStatus kind, String sourceValue, String targetValue, DiffEvalContext diffEvalContext) {
        return new DefaultDiffItemCreateContext(name, kind, sourceValue, targetValue, this,diffEvalContext);
    }

    public DiffCommand resolveDiffItemFactory(DiffKey key, DiffEvalContext diffEvalContext) {
        for (DiffCommand diffItemFactory : diffEvalContext.getSupportedCommands()) {
            if (diffItemFactory.acceptDiffKey(key)) {
                return diffItemFactory;
            }
        }
        return DiffCommandDefault.INSTANCE;
    }

    @Override
    public DiffItem createChildItem(DiffKey name, DiffStatus status, String sourceValue, String targetValue, DiffEvalContext diffEvalContext) {
        DiffCommand f = resolveDiffItemFactory(name, diffEvalContext);
        if (f == null) {
            f = DiffCommandDefault.INSTANCE;
        }
        DiffItemCreateContext c = createContext(name, status, sourceValue, targetValue, diffEvalContext);
        DiffItem i = f.createContentDiffItem(c);
        if (i == null) {
            i = DiffCommandDefault.INSTANCE.createContentDiffItem(c);
        }
        return i;
    }

    protected DiffResult createResult(TreeMap<DiffKey, String> missing1, TreeMap<DiffKey, String> missing2, TreeMap<DiffKey, String[]> diffs, DiffEvalContext context) {
        return new DefaultDiffResult(this, context,missing1, missing2, diffs);
    }

}

package net.thevpc.nuts.runtime.standalone.time;

import net.thevpc.nuts.time.NProgressMonitor;
import net.thevpc.nuts.time.NProgressRunner;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class NDefaultProgressRunner implements NProgressRunner {
    private List<WeightedProcessor> all = new ArrayList<>();

    public void add(Consumer<Context> processor, double weight) {
        all.add(new WeightedProcessor(processor, weight));
    }

    public void add(Consumer<Context> processor) {
        all.add(new WeightedProcessor(processor, 1));
    }

    public void add(Runnable processor, double weight) {
        all.add(new WeightedProcessor(c -> processor.run(), weight));
    }

    public void add(Runnable processor) {
        all.add(new WeightedProcessor(c -> processor.run(), 1));
    }

    private class WeightedProcessor {
        private Consumer<Context> processor;
        private double weight;

        public WeightedProcessor(Consumer<Context> processor, double weight) {
            this.processor = processor;
            this.weight = weight;
        }
    }

    public void run() {
        ContextImpl cc = new ContextImpl();
        NProgressMonitor m = NProgressMonitor.of();
        m.start();
        NProgressMonitor[] mons = m.split(all.stream().mapToDouble(x -> x.weight).toArray());
        for (int i = 0; i < mons.length; i++) {
            NProgressMonitor mon = mons[i];
            mon.start();
            all.get(i).processor.accept(cc);
            mon.complete();
        }
        m.complete();
    }

    public static class ContextImpl implements NProgressRunner.Context {
        private Map<String, Object> context = new HashMap<>();

        public <T> NOptional<T> get(String name) {
            return get(name, null);
        }

        public <T> ContextImpl set(String name, Object value) {
            context.put(name, value);
            return this;
        }

        public <T> NOptional<T> get(String name, Class<T> expectedType) {
            NAssert.requireNonNull(name, "name");
            Object o = context.get(name);
            if (o == null) {
                if (context.containsKey(name)) {
                    return NOptional.ofNull();
                }
                return NOptional.ofNamedEmpty(name);
            }
            if (expectedType != null) {
                if (expectedType.isInstance(o)) {
                    return NOptional.of(expectedType.cast(o));
                }
                return NOptional.ofEmpty(NMsg.ofC("invalid type for %s", name));
            }
            return NOptional.ofNamed((T) o, name);
        }
    }
}

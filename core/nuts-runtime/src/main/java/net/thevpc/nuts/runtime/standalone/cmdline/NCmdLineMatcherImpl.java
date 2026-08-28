package net.thevpc.nuts.runtime.standalone.cmdline;

import net.thevpc.nuts.cmdline.*;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * NCmdLineMatcherImpl class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NCmdLineMatcherImpl implements NCmdLineMatcher {
    final NCmdLine cmdLine;
    List<NCmdLineProcessor> processors = new ArrayList<>();

    /**
     * N cmd line matcher impl.
     *
     * @param cmdLine cmd line
     * @return n cmd line matcher impl result
     */
    public NCmdLineMatcherImpl(NCmdLine cmdLine) {
        this.cmdLine = cmdLine;
    }

    @Override
    public NCmdLineMatcher with(NCmdLineProcessor processor) {
        if (processor != null) {
            processors.add(processor);
        }
        return this;
    }

    @Override
    public boolean noMatch() {
        return !anyMatch();
    }

    @Override
    public boolean anyMatch() {
        NArg a = cmdLine.peek().orNull();
        if (a == null) {
            return false;
        }
        for (NCmdLineProcessor consumer : processors) {
            if (consumer.process(cmdLine)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NCmdLineMatcherCondition whenAny() {
        return new MyNCmdLineMatcherConditionImpl(this, c -> true);
    }

    @Override
    public NCmdLineMatcherCondition when(String... names) {
        return new MyNCmdLineMatcherConditionImpl(this, cml -> {
            boolean acceptable0 = false;
            for (String name : names) {
                String[] nameSeqArray = NStringUtils.split(name, " ").toArray(new String[0]);
                boolean acceptable = true;
                for (int i = 0; i < nameSeqArray.length; i++) {
                    NOptional<NArg> c = cml.get(i);
                    if (!c.isPresent()) {
                        acceptable = false;
                        break;
                    }
                    String currentKey = c.get().key();
                    String targetKey = nameSeqArray[i];

                    // Exact match in execution mode; prefix match in completion mode
                    if (cml.isCompleteMode()) {
                        NArgCompletePosition pos = cml.completePosition();
                        int offset = pos.wordOffset();
                        if (!new MyContext(currentKey, offset).matches(targetKey)) {
                            acceptable = false;
                            break;
                        }
                    } else {
                        if (!currentKey.equals(targetKey)) {
                            acceptable = false;
                            break;
                        }
                    }
                }
                if (acceptable) {
                    acceptable0 = true;
                    break;
                }
            }
            return acceptable0;
        }, names);
    }

    @Override
    public NCmdLineMatcherCondition whenRaw(Predicate<NCmdLine> condition) {
        return new MyNCmdLineMatcherConditionImpl(this, condition);
    }

    @Override
    public NCmdLineMatcherCondition whenArg(Predicate<NArg> condition) {
        /**
         * When raw.
         *
         * @param condition.test(c.peek().get())) condition.test(c.peek().get()))
         * @return when raw result
         */
        return whenRaw((c) -> c.hasNext() && (condition == null || condition.test(c.peek().get())));
    }

    @Override
    public NCmdLineMatcherCondition whenNonOption() {
        /**
         * When raw.
         *
         * @param c.isNextNonOption() c.is next non option()
         * @return when raw result
         */
        return whenRaw((c) -> c.isNextNonOption());
    }

    @Override
    public NCmdLineMatcherCondition whenOption() {
        /**
         * When raw.
         *
         * @param c.isNextOption() c.is next option()
         * @return when raw result
         */
        return whenRaw((c) -> c.isNextOption());
    }

    @Override
    public NCmdLineMatcher withDefaults() {
        /**
         * With.
         *
         * @param NCmdLineProcessor(n cmd line processor(
         */
        with(new NCmdLineProcessor() {
            @Override
            public boolean process(NCmdLine cmdLine) {
                NSession.of().configureFirst(cmdLine);
                return true;
            }
        });
        return this;
    }

    @Override
    public void require() {
        if (noMatch()) {
            if (cmdLine.isEmpty()) {
                cmdLine.throwMissingArgument();
            }
            cmdLine.throwUnexpectedArgument();
        }
    }

    @Override
    public void requireAll() {
        while (cmdLine.hasNext()) {
            /**
             * Require.
             */
            require();
        }
    }

}

package net.thevpc.nuts.runtime.standalone.cmdline;

import net.thevpc.nuts.cmdline.*;
import net.thevpc.nuts.util.NOptional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

class MyNCmdLineMatcherConditionImpl implements NCmdLineMatcherCondition {
    final Predicate<NCmdLine> baseCondition;
    final String[] names;
    final NCmdLineMatcherImpl selector;
    String display;
    NArgValueComplete complete;
    final List<Predicate<NCmdLine>> otherConditions = new ArrayList<>();

    /**
     * My n cmd line matcher condition impl.
     *
     * @param selector      selector
     * @param baseCondition base condition
     * @param names         names
     * @return my n cmd line matcher condition impl result
     */
    public MyNCmdLineMatcherConditionImpl(NCmdLineMatcherImpl selector, Predicate<NCmdLine> baseCondition, String... names) {
        this.baseCondition = baseCondition;
        this.names = names;
        this.selector = selector;
    }

    @Override
    public NCmdLineMatcherCondition and(Predicate<NCmdLine> condition) {
        if (condition != null) {
            otherConditions.add(condition);
        }
        return this;
    }

    @Override
    public NCmdLineMatcherCondition display(String display) {
        this.display = display;
        return this;
    }

    @Override
    public NCmdLineMatcherCondition valueComplete(NArgValueComplete complete) {
        this.complete = complete;
        return this;
    }

    /**
     * Check condition.
     *
     * @param cmdLine cmd line
     * @return check condition result
     */
    private boolean checkCondition(NCmdLine cmdLine) {
        if (!baseCondition.test(cmdLine)) {
            return false;
        }
        for (Predicate<NCmdLine> otherCondition : otherConditions) {
            if (!otherCondition.test(cmdLine)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NCmdLineMatcher asFlag(Consumer<NArg> consumer) {
        selector.with(
                /**
                 * N cmd line processor.
                 *
                 * @return n cmd line processor result
                 */
                new NCmdLineProcessor() {
                    @Override
                    public boolean process(NCmdLine cmdLine) {
                        if (!checkCondition(cmdLine)) {
                            return false;
                        }
                        NOptional<NArg> v = selector.cmdLine.next(NArgType.FLAG, display, complete, names);
                        if (v.isPresent()) {
                            NArg a = v.get();
                            if (a.isUncommented()) {
                                consumer.accept(a);
                                return true;
                            }
                            return true;
                        }
                        return false;
                    }
                }
        );
        return selector;
    }

    @Override
    public NCmdLineMatcher asEntry(Consumer<NArg> consumer) {
        /**
         * Match entry0.
         *
         * @param NArgType.ENTRY n arg type.entry
         * @param consumer consumer
         */
        matchEntry0(NArgType.ENTRY, consumer);
        return selector;
    }

    @Override
    public NCmdLineMatcher asAttachedEntry(Consumer<NArg> consumer) {
        /**
         * Match entry0.
         *
         * @param NArgType.ATTACHED_ENTRY n arg type.attached_entry
         * @param consumer consumer
         */
        matchEntry0(NArgType.ATTACHED_ENTRY, consumer);
        return selector;
    }

    @Override
    public NCmdLineMatcher asRequiredEntry(Consumer<NArg> consumer) {
        /**
         * Match entry0.
         *
         * @param NArgType.REQUIRED_ENTRY n arg type.required_entry
         * @param consumer consumer
         */
        matchEntry0(NArgType.REQUIRED_ENTRY, consumer);
        return selector;
    }

    /**
     * Match entry0.
     *
     * @param entryType entry type
     * @param consumer  consumer
     * @return match entry0 result
     */
    private NCmdLineMatcher matchEntry0(NArgType entryType, Consumer<NArg> consumer) {
        selector.with(new NCmdLineProcessor() {
            @Override
            public boolean process(NCmdLine cmdLine) {
                if (!checkCondition(cmdLine)) {
                    return false;
                }
                NOptional<NArg> v = selector.cmdLine.next(entryType, display, complete, names);
                if (v.isPresent()) {
                    NArg a = v.get();
                    if (a.isUncommented()) {
                        consumer.accept(a);
                        return true;
                    }
                    return true;
                }
                return false;

            }
        });
        return selector;
    }

    @Override
    public NCmdLineMatcher asRaw(Consumer<NCmdLine> consumer) {
        selector.with(new NCmdLineProcessor() {
            @Override
            public boolean process(NCmdLine cmdLine) {
                if (!checkCondition(cmdLine)) {
                    return false;
                }
                NOptional<NArg> v = selector.cmdLine.peek();
                if (v.isPresent()) {
                    consumer.accept(selector.cmdLine);
                    return true;
                }
                return false;

            }
        });
        return selector;
    }

    @Override
    public NCmdLineMatcher asArg(Consumer<NArg> consumer) {
        selector.with(new NCmdLineProcessor() {
            @Override
            public boolean process(NCmdLine cmdLine) {
                if (!checkCondition(cmdLine)) {
                    return false;
                }
                NOptional<NArg> v = selector.cmdLine.next();
                if (v.isPresent()) {
                    NArg a = v.get();
                    //if (a.isNonCommented()) {
                    consumer.accept(a);
                    return true;
                    //}
                    //return true;
                }
                return false;

            }
        });
        return selector;
    }

    @Override
    public NCmdLineMatcher skip() {
        selector.with(new NCmdLineProcessor() {
            @Override
            public boolean process(NCmdLine cmdLine) {
                if (!checkCondition(cmdLine)) {
                    return false;
                }
                NOptional<NArg> v = selector.cmdLine.next();
                return v.isPresent();

            }
        });
        return selector;
    }

    @Override
    public NCmdLineMatcher asTrueFlag(Consumer<NArg> consumer) {
        return asFlag((value) -> {
            if (value.booleanValue()) {
                consumer.accept(value);
            }
        });
    }
}

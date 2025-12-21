package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.util.function.Supplier;

public class LazyNScoredValueImpl<T> implements NScoredValue<T> {
    Supplier<NScorable> scorerSupplier;
    Supplier<T> instanceSupplier;
    T _instance;
    Integer _score;
    NScorableContext supportCriteria;

    public LazyNScoredValueImpl(Supplier<NScorable> scorerSupplier, Supplier<T> instanceSupplier, NScorableContext supportCriteria) {
        this.scorerSupplier = scorerSupplier;
        this.instanceSupplier = instanceSupplier;
        this.supportCriteria = supportCriteria;
    }

    @Override
    public T value() {
        if (_instance == null) {
            synchronized (this) {
                if (_instance == null) {
                    if (_score == null) {
                        score();
                    }
                    if (_score == null) {
                        throw new NIllegalArgumentException(NMsg.ofC("unable to evaluate score"));
                    }
                    if (_score > 0) {
                        _instance = this.instanceSupplier.get();
                    } else {
                        throw new NIllegalArgumentException(NMsg.ofC("not a valid score"));
                    }
                }
            }
        }
        return _instance;
    }

    @Override
    public int score() {
        if (_score == null) {
            synchronized (this) {
                if (_score == null) {
                    NScorable scorer = scorerSupplier.get();
                    _score = scorer.getScore(supportCriteria);
                }
            }
        }
        return _score;
    }
}

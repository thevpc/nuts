package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.util.NCollectionDiff;
import net.thevpc.nuts.util.NCollectionDiffChange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NRateLimitedValueBuilderImpl implements NRateLimitedValueBuilder {
    String id;
    NRateLimitStrategyBuilderImpl lastConstraint;
    List<NRateLimitStrategyBuilderImpl> constraints = new ArrayList<>();
    private NRateLimitedValueFactoryImpl factory;

    public NRateLimitedValueBuilderImpl(String id, NRateLimitedValueFactoryImpl factory) {
        this.id = id;
        this.factory = factory;
    }

    @Override
    public NRateLimitedValueBuilder id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public NRateLimitedValue build() {
        NRateLimitedValueModel newModel = new NRateLimitedValueModel(
                id, null,
                Arrays.stream(constraints()).map(x -> new NRateLimitStrategyModel(x.getId(),
                        x.getStrategy(),
                        x.getMax(), x.getDuration(), 0, x.getStartDate(),new byte[0])).toArray(NRateLimitStrategyModel[]::new)
        );
        NRateLimitedValueModel old = factory.load(newModel.getId());
        if (old == null) {
            factory.save(newModel);
            return new NRateLimitedValueImpl(newModel, factory);
        } else {
            List<NRateLimitStrategyModel> okkay = new ArrayList<>();
            for (NCollectionDiffChange<NRateLimitStrategyModel> d : NCollectionDiff.diffList(Arrays.asList(old.getConstraints()), Arrays.asList(newModel.getConstraints()), m -> m.getId())) {
                switch (d.getMode()) {
                    case ADDED: {
                        okkay.add(d.getNewValue());
                        break;
                    }
                    case REMOVED: {
                        break;
                    }
                    case CHANGED: {
                        okkay.add(new NRateLimitStrategyModel(
                                d.getNewValue().getId(),
                                d.getNewValue().getStrategy(),
                                d.getNewValue().getMax(),
                                d.getNewValue().getDuration(),
                                Math.min(d.getOldValue().getAvailable(), d.getNewValue().getMax()),
                                d.getOldValue().getLastRefill(),
                                d.getOldValue().getConfig()
                        ));
                        break;
                    }
                    case UNCHANGED: {
                        okkay.add(d.getOldValue());
                    }
                }
            }
            newModel = new NRateLimitedValueModel(newModel.getId(), old.getLastAccess(), okkay.toArray(new NRateLimitStrategyModel[0]));
            factory.save(newModel);
            return new NRateLimitedValueImpl(newModel, factory);
        }
    }

    @Override
    public NRateLimitStrategyBuilder withLimit(String limitId) {
        return lastConstraint = new NRateLimitStrategyBuilderImpl(limitId, this);
    }


    private NRateLimitStrategyBuilderImpl[] constraints() {
        if (lastConstraint == null) {
            return constraints.toArray(new NRateLimitStrategyBuilderImpl[0]);
        }
        NRateLimitStrategyBuilderImpl[] arr = Arrays.copyOf(
                constraints.toArray(new NRateLimitStrategyBuilderImpl[0]),
                constraints.size() + 1
        );
        arr[arr.length - 1] = lastConstraint;
        return arr;
    }
}

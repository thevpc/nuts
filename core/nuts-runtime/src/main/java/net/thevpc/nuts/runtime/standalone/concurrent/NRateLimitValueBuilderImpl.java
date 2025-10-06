package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NCollectionDiff;
import net.thevpc.nuts.util.NCollectionDiffChange;
import net.thevpc.nuts.util.NMsg;

import java.util.*;

public class NRateLimitValueBuilderImpl implements NRateLimitValueBuilder {
    String id;
    NRateLimitRuleBuilderImpl lastRule;
    List<NRateLimitRuleBuilderImpl> rules = new ArrayList<>();
    private NRateLimitValueFactoryImpl factory;

    public NRateLimitValueBuilderImpl(String id, NRateLimitValueFactoryImpl factory) {
        this.id = id;
        this.factory = factory;
    }

    @Override
    public NRateLimitValueBuilder id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public NRateLimitValue build() {
        NRateLimitRuleBuilderImpl[] currRules = rules();
        if(currRules.length==0){
            throw new NIllegalArgumentException(NMsg.ofC("missing rules"));
        }
        {
            Set<String> visited=new HashSet<>();
            for (NRateLimitRuleBuilderImpl c : currRules) {
                String cid = c.getId();
                if(!NBlankable.isBlank(cid)){
                    if(!visited.add(cid)){
                        throw new  NIllegalArgumentException(NMsg.ofC("duplicate rule id %s for %s", cid, id));
                    }
                }
            }
        }
        {
            Set<String> visited=new HashSet<>();
            int index=1;
            for (NRateLimitRuleBuilderImpl c : currRules) {
                String cid = c.getId();
                if(NBlankable.isBlank(cid)){
                    while(true){
                        String cid2 = "rule-"+index;
                        if(visited.add(cid2)){
                           c.setId(cid2);
                           break;
                        }else{
                            index++;
                        }
                    }
                }else if(!visited.add(cid)){
                    throw new  NIllegalArgumentException(NMsg.ofC("duplicate rule id %s for %s", cid, id));
                }
            }
        }
        NRateLimitValueModel newModel = new NRateLimitValueModel(
                id, 0,
                Arrays.stream(currRules).map(x -> new NRateLimitRuleModel(x.getId(),
                        x.getStrategy(),
                        x.getMax(), x.getDuration()==null?0:x.getDuration().toMillis(), 0, x.getStartDate()==null?0:x.getStartDate().toEpochMilli(),
                        new byte[0])).toArray(NRateLimitRuleModel[]::new)
        );
        NRateLimitValueModel old = factory.load(newModel.getId());
        if (old == null) {
            factory.save(newModel);
            return new NRateLimitValueImpl(newModel, factory);
        } else {
            List<NRateLimitRuleModel> okkay = new ArrayList<>();
            for (NCollectionDiffChange<NRateLimitRuleModel> d : NCollectionDiff.diffList(Arrays.asList(old.getRules()), Arrays.asList(newModel.getRules()), m -> m.getId())) {
                switch (d.getMode()) {
                    case ADDED: {
                        okkay.add(d.getNewValue());
                        break;
                    }
                    case REMOVED: {
                        break;
                    }
                    case CHANGED: {
                        okkay.add(new NRateLimitRuleModel(
                                d.getNewValue().getId(),
                                d.getNewValue().getStrategy(),
                                d.getNewValue().getCapacity(),
                                d.getNewValue().getDuration(),
                                Math.min(d.getOldValue().getAvailable(), d.getNewValue().getCapacity()),
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
            newModel = new NRateLimitValueModel(newModel.getId(), old.getLastAccess(), okkay.toArray(new NRateLimitRuleModel[0]));
            factory.save(newModel);
            return new NRateLimitValueImpl(newModel, factory);
        }
    }

    @Override
    public NRateLimitRuleBuilder withLimit(String limitId) {
        return lastRule = new NRateLimitRuleBuilderImpl(limitId, this);
    }


    private NRateLimitRuleBuilderImpl[] rules() {
        if (lastRule == null) {
            return rules.toArray(new NRateLimitRuleBuilderImpl[0]);
        }
        NRateLimitRuleBuilderImpl[] arr = Arrays.copyOf(
                rules.toArray(new NRateLimitRuleBuilderImpl[0]),
                rules.size() + 1
        );
        arr[arr.length - 1] = lastRule;
        return arr;
    }
}

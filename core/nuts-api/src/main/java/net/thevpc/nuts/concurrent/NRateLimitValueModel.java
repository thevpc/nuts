package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDescribable;
import net.thevpc.nuts.elem.NUpletElementBuilder;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class NRateLimitValueModel implements Serializable, NElementDescribable {
    private String id;
    private long lastAccess;
    private NRateLimitRuleModel[] rules;

    public NRateLimitValueModel(String id, long lastAccess, NRateLimitRuleModel[] rules) {
        this.id = id == null ? "" : id;
        this.lastAccess = lastAccess;
        this.rules = rules;
    }

    public String getId() {
        return id;
    }

    public long getLastAccess() {
        return lastAccess;
    }

    public NRateLimitRuleModel[] getRules() {
        return rules;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NRateLimitValueModel that = (NRateLimitValueModel) o;
        return Objects.equals(id, that.id) && Objects.equals(lastAccess, that.lastAccess) && Objects.deepEquals(rules, that.rules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lastAccess, Arrays.hashCode(rules));
    }

    @Override
    public String toString() {
        return "NLimitedValueData{" +
                "id='" + id + '\'' +
                ", lastAccess=" + lastAccess +
                ", rules=" + Arrays.toString(rules) +
                '}';
    }

    @Override
    public NElement describe() {
        NUpletElementBuilder b = NElement.ofUpletBuilder("RateLimitValue")
                .add("id", id);
        if (lastAccess > 0) {
            b.add("lastAccess", lastAccess);
        }
        if (rules != null && rules.length > 0) {
            b.add("rules",
                    NElement.ofArray(Arrays.stream(rules)
                            .map(NRateLimitRuleModel::describe).toArray(NElement[]::new))
            );
        }
        return b.build();
    }
}

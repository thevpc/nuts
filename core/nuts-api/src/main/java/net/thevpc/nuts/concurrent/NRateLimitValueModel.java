package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NDescribable;
import net.thevpc.nuts.elem.NTupleElementBuilder;
import net.thevpc.nuts.io.NClosable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NCopiable;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NToStringBuilder;
import net.thevpc.nuts.util.NUnexpectedException;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Model class representing a rate-limited value.
 * <p>
 * This class contains the unique identifier of the value, the timestamp of the
 * last access, and an array of rate-limit rules ({@link NRateLimitRuleModel})
 * that govern its behavior. It is serializable and can be described as a
 * {@link NElement} for inspection or persistence.
 * <p>
 * Typically, instances of this class are created and managed by
 * {@link NRateLimitValueFactory} implementations.
 *
 * @since 0.8.7
 */
public class NRateLimitValueModel implements Serializable, NDescribable, NCopiable, Cloneable {
    /**
     * Unique identifier of this rate-limited value.
     */
    private String id;

    /**
     * Timestamp of the last access in milliseconds since epoch.
     */
    private long lastAccess;
    /**
     * Array of rate-limit rules associated with this value.
     */
    private NRateLimitRuleModel[] rules;


    /**
     * Constructs an empty {@code NRateLimitValueModel}.
     */
    public NRateLimitValueModel() {
        this.id = "";
    }

    /**
     * Constructs a new {@code NRateLimitValueModel}.
     *
     * @param id         unique identifier of the value, or empty string if null
     * @param lastAccess last access timestamp in milliseconds
     * @param rules      array of rate-limit rules governing this value
     */
    public NRateLimitValueModel(String id, long lastAccess, NRateLimitRuleModel[] rules) {
        this.id = id == null ? "" : id;
        this.lastAccess = lastAccess;
        this.rules = rules;
    }

    /**
     * Sets the unique identifier of this value.
     *
     * @param id identifier string
     * @return this instance
     */
    public NRateLimitValueModel id(String id) {
        this.id = id == null ? "" : id;
        return this;
    }

    /**
     * Sets the timestamp of the last access in milliseconds.
     *
     * @param lastAccess last access timestamp
     * @return this instance
     */
    public NRateLimitValueModel lastAccess(long lastAccess) {
        this.lastAccess = lastAccess;
        return this;
    }

    /**
     * Sets the array of rate-limit rules.
     *
     * @param rules array of rules
     * @return this instance
     */
    public NRateLimitValueModel rules(NRateLimitRuleModel[] rules) {
        this.rules = rules;
        return this;
    }

    /**
     * Returns the unique identifier of this value.
     *
     * @return identifier string
     */
    @NGetter
    public String id() {
        return id;
    }


    /**
     * Returns the timestamp of the last access.
     *
     * @return last access in milliseconds
     */
    @NGetter
    public long lastAccess() {
        return lastAccess;
    }

    /**
     * Returns the array of associated rate-limit rules.
     *
     * @return array of {@link NRateLimitRuleModel}
     */
    @NGetter
    public NRateLimitRuleModel[] rules() {
        return rules;
    }

    @Override
    public NRateLimitValueModel copy() {
        return clone();
    }

    @Override
    protected NRateLimitValueModel clone() {
        NRateLimitValueModel cloned = null;
        try {
            cloned = (NRateLimitValueModel) super.clone();
            if(cloned.rules!=null) {
                cloned.rules=Arrays.stream(cloned.rules)
                        .map(x->x.copy())
                        .toArray(NRateLimitRuleModel[]::new);
            }
        } catch (CloneNotSupportedException e) {
            throw new NUnexpectedException(NMsg.ofC("clone unsupported for %s",getClass()),e);
        }
        return cloned;
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
        return NToStringBuilder.of(this).omitBlanks(true)
                .add("id", id)
                .add("lastAccess", lastAccess)
                .add("rules", rules)
                .build();
    }

    /**
     * Returns a structured {@link NElement} representation of this value,
     * including its ID, last access timestamp, and all associated rules.
     *
     * @return an {@link NElement} describing this rate-limited value
     */
    @Override
    public NElement describe() {
        NTupleElementBuilder b = NElement.ofTupleBuilder("RateLimitValue")
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

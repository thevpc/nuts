package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * Default load balancing strategies provided by {@link NWorkBalancer}.
 * <p>
 * These strategies define how workers are selected for executing a job.
 * They can be used directly via {@link NWorkBalancerBuilder#setStrategy(String)}
 * or {@link NWorkBalancerBuilder#setStrategy(NWorkBalancerDefaultStrategy)}.
 *
 * <ul>
 *     <li>{@link #ROUND_ROBIN} – Jobs are distributed sequentially across workers in a circular manner.
 *     Each worker receives one job in turn. Simple and fair when workers are similar.</li>
 *
 *     <li>{@link #LEAST_LOAD} – Selects the worker with the lowest reported load based on
 *     {@link NWorkBalancerHostLoadMetricProvider} metrics. Useful when workers have varying capacities
 *     or workloads.</li>
 *
 *     <li>{@link #POWER_OF_TWO_CHOICES} – Picks two random workers and selects the one with the lower load.
 *     This strategy provides a good trade-off between randomness and load-awareness, and scales well with
 *     large numbers of workers.</li>
 * </ul>
 *
 * <p>
 * Each enum entry also has a lowercase identifier accessible via {@link #id()},
 * and can be parsed from a string using {@link #parse(String)}.
 * </p>
 *
 * <p>
 * <b>Extensibility:</b> You can define your own custom strategies using
 * {@link NWorkBalancerFactory#defineStrategy(String, NWorkBalancerStrategy)}.
 * This allows creating specialized worker selection logic beyond the default strategies.
 * </p>
 *
 * @since 0.8.7
 */

public enum NWorkBalancerDefaultStrategy implements NEnum {
    ROUND_ROBIN,
    LEAST_LOAD,
    POWER_OF_TWO_CHOICES;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NWorkBalancerDefaultStrategy() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    /**
     * Parses a string value to its corresponding {@link NWorkBalancerDefaultStrategy} if possible.
     *
     * @param value the string value to parse
     * @return an {@link NOptional} containing the strategy if parsing succeeded, or empty otherwise
     */
    public static NOptional<NWorkBalancerDefaultStrategy> parse(String value) {
        return NEnumUtils.parseEnum(value, NWorkBalancerDefaultStrategy.class);
    }

    @Override
    public String id() {
        return id;
    }

}

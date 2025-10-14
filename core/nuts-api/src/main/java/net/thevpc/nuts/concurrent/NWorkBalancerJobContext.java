package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NOptional;

import java.util.Map;

/**
 * Provides contextual information about a job being executed
 * within an {@link NWorkBalancer}.
 * <p>
 * This context is passed to each {@link NWorkBalancerJob} when executed,
 * giving access to job-specific metadata, the assigned worker, and options.
 * </p>
 * <p>
 * The context allows the job to be aware of:
 * <ul>
 *     <li>Its unique identifier and name</li>
 *     <li>The worker assigned to execute it</li>
 *     <li>Any global or worker-specific options</li>
 * </ul>
 * </p>
 *
 * @since 0.8.7
 */
public interface NWorkBalancerJobContext {

    /**
     * Returns the unique identifier of this job execution.
     * <p>
     * This ID is generated for each execution and can be used for
     * metrics, logging, or correlation in monitoring.
     *
     * @return unique job ID
     */
    String getJobId();

    /**
     * Returns the human-readable name of the job.
     *
     * @return job name
     */
    String getJobName();

    /**
     * Returns the name of the worker assigned to execute this job.
     *
     * @return worker name
     */
    String getWorkerName();

    int getWorkerIndex();

    int getWorkersCount();

    /**
     * Returns the {@link NWorkBalancerWorker} instance assigned
     * to execute this job.
     *
     * @return assigned worker
     */
    NWorkBalancerWorker getWorker();

    /**
     * Returns all options available for this job, including global
     * and worker-specific options.
     *
     * @return map of option names to values
     */
    Map<String, NElement> getOptions();

    /**
     * Returns the value of a specific option by name, if present.
     *
     * @param name the option key
     * @return optional value of the option
     */
    NOptional<NElement> getOption(String name);
}

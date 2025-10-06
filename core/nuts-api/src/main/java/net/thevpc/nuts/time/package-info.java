/**
 * Provides utilities for time measurement, duration formatting, and progress tracking.
 * <p>
 * This package includes:
 * <ul>
 *     <li><b>Clock and chronometers</b> for measuring elapsed time, timestamps, and scheduling.</li>
 *     <li><b>Duration and time formatting</b> for human-readable or structured output.</li>
 *     <li><b>Progress tracking</b> to represent the advancement of tasks or operations, including
 *         hierarchical or aggregated progress.</li>
 * </ul>
 * <p>
 * <b>Design rationale for progress:</b>
 * <ul>
 *     <li>Progress is independent of concurrency. It represents the logical completion state of a task,
 *         whether executed in a single thread or multiple threads.</li>
 *     <li>Progress can be subdivided into sub-progresses, aggregated, and reported through events.</li>
 *     <li>Progress can integrate with concurrent execution, but it is fundamentally about monitoring
 *         task advancement and duration estimation, not about managing threads.</li>
 * </ul>
 *
 * @since 0.8.5
 */
package net.thevpc.nuts.time;

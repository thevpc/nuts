/**
 * Concurrency and coordination utilities for Nuts.
 * <p>
 * This package provides abstractions and implementations for building
 * concurrent, resilient, and distributed applications. It includes mechanisms
 * for work distribution, rate limiting, retry policies, sagas, caching,
 * and circuit breakers.
 * </p>
 *
 * <p>
 * Key features provided by this package:
 * <ul>
 *     <li><b>Work Balancing:</b> Distribute tasks across multiple workers,
 *         track runtime metrics, and use customizable strategies to select
 *         the most suitable worker for each task.</li>
 *     <li><b>Rate Limiting:</b> Control the execution rate of operations,
 *         support throttling, and persist counters for long-lived limits.</li>
 *     <li><b>Locks and Synchronization:</b> Manage local or distributed
 *         locks, barriers, and coordination among threads or nodes.</li>
 *     <li><b>Circuit Breakers:</b> Stop failing operations to protect
 *         the system and recover gracefully when possible.</li>
 *     <li><b>Saga Orchestration:</b> Define distributed transactions with
 *         compensation and rollback strategies for complex workflows.</li>
 *     <li><b>Cached and Stable Values:</b> Memoize values, cache results,
 *         and stabilize state in concurrent environments.</li>
 *     <li><b>Retry Mechanisms:</b> Execute operations with configurable
 *         retry policies and failure handling strategies.</li>
 *     <li><b>Scoped Values:</b> Execute operations with a scoped context (value or stack).</li>
 * </ul>
 * </p>
 *
 * <p>
 * This package emphasizes:
 * <ul>
 *     <li>Decoupling runtime metrics from strategy logic.</li>
 *     <li>Extensible strategies for work distribution and resource usage.</li>
 *     <li>Persistence-friendly models for storing state and metrics.</li>
 *     <li>Support for both synchronous and asynchronous execution patterns.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Users can extend the framework by implementing custom strategies,
 * providing alternative storage backends, or integrating advanced
 * workflow steps.
 * </p>
 *
 */
package net.thevpc.nuts.concurrent;

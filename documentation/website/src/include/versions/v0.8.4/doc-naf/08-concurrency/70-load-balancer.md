---
title: Load Balancer
---

`NWorkBalancer` lets you distribute jobs across multiple workers using configurable strategies. You can define weights, choose strategies like **round-robin** or **least-load**, and track running jobs for observability and control.


Note: The NWorkBalancer is decoupled from execution. It does not run the job when you submit it. Instead, it returns a NCallable that selects a worker according to the strategy and tracks metrics. You must explicitly call the returned NCallable to execute the task.


### Basic Example: Weighted Distribution

```java
NWorkBalancerFactory factory = NWorkBalancerFactory.of();
NWorkBalancer<String> workBalancer = factory.<String>ofBuilder("example")
.addWorker("WorkerA").withWeight(1)
.addWorker("WorkerB").withWeight(2)
.build();

NCallable<String> callable = workBalancer.of("hello", context -> {
NOut.println(NMsg.ofC(
"call worker %s/%s:%s jobName:%s jobId:%s",
context.getWorkerIndex() + 1,
context.getWorkersCount(),
context.getWorkerName(),
context.getJobName(),
context.getJobId()
));
NConcurrent.of().sleep(50 + new Random().nextInt(50));
return "hello from " + context.getWorkerName();
});

NTaskSet tasks = NTaskSet.of();
for (int i = 0; i < 50; i++) {
tasks.call(callable);
}
tasks.join();
```

### Example: Using a Custom Strategy

```java
NWorkBalancerFactory factory = NWorkBalancerFactory.of();
NWorkBalancer<String> workBalancer = factory.<String>ofBuilder("example")
.addWorker("WorkerA").withWeight(1)
.addWorker("WorkerB").withWeight(2)
.then()
.setStrategy(NWorkBalancerDefaultStrategy.ROUND_ROBIN)
.build();

NCallable<String> callable = workBalancer.of("hello", context -> {
NOut.println(NMsg.ofC(
"call worker %s/%s:%s jobName:%s jobId:%s",
context.getWorkerIndex() + 1,
context.getWorkersCount(),
context.getWorkerName(),
context.getJobName(),
context.getJobId()
));
NConcurrent.of().sleep(50 + new Random().nextInt(50));
return "hello from " + context.getWorkerName();
});

NTaskSet tasks = NTaskSet.of();
for (int i = 0; i < 50; i++) {
tasks.call(callable);
}
tasks.join();

NOut.println("-------------------------------------------------------------");
NOut.println(NMsg.ofC("runningJobsCount %s", workBalancer.getRunningJobsCount()));
NOut.println(NMsg.ofC("workerLoads %s", workBalancer.getWorkerLoads()));
```

### Key Methods

#### `getRunningJobs()`

Returns a snapshot of all currently running jobs. Useful for monitoring, metrics aggregation, or custom cancellation logic.

#### `getRunningJobsCount()`

Returns the number of currently active jobs for this balancer.

#### `getWorkers()`

Returns the list of registered workers.

#### `getWorkerLoad(String workerName)`

Returns the load metrics for a specific worker.

#### `of(String name, NWorkBalancerJob<T> job)`

Wraps a job into an `NCallable` that will execute according to the balancer strategy. Each call tracks metrics independently.

#### `getOption(String name)` / `getOptions()`

Retrieve global options configured for this balancer. Options can customize worker behavior at runtime.

### Factory Usage: `NWorkBalancerFactory`

Create balancers using a factory:

```java
NWorkBalancerFactory factory = NWorkBalancerFactory.of();
NWorkBalancer<MyResult> balancer = factory.ofBuilder("my-balancer")
.addWorker("A").withWeight(1)
.addWorker("B").withWeight(2)
.setStrategy(NWorkBalancerDefaultStrategy.LEAST_LOAD)
.build();
```

You can also register custom strategies:

```java
factory.defineStrategy("myStrategy", new MyCustomStrategy());
NWorkBalancer<MyResult> balancer2 = factory.ofBuilder("callId")
.setStrategy("myStrategy")
.build();
```

### Notes

* Each job submitted through `of()` is automatically tracked.
* You can inspect running jobs, worker loads, and metrics at any time.
* Strategies can be swapped dynamically to optimize load distribution.

package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.toolbox.njob.model.*;
import net.thevpc.nuts.toolbox.njob.time.TimePeriod;
import net.thevpc.nuts.toolbox.njob.time.TimePeriods;
import net.thevpc.nuts.toolbox.njob.time.TimespanPattern;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NTasksSubService {

    private NutsApplicationContext context;
    private NJobConfigStore dal;
    private JobService service;

    public NTasksSubService(NutsApplicationContext context, NJobConfigStore dal, JobService service) {
        this.context = context;
        this.dal = dal;
        this.service = service;
    }

    public void updateTask(NTask task) {
        if (task.getName() == null) {
            task.setName("todo");
        }
        Instant now = Instant.now();
        if (task.getFlag() == null) {
            task.setFlag(NFlag.NONE);
        }
        if (task.getCreationTime() == null) {
            task.setCreationTime(now);
        }
        task.setModificationTime(now);
        if (task.getStatus() == null) {
            task.setStatus(NTaskStatus.TODO);
        }
        if (task.getPriority() == null) {
            task.setPriority(NPriority.NORMAL);
        }
        if (task.getObservations() == null) {
            task.setObservations("");
        }
        if (task.getStatus() == NTaskStatus.DONE) {
            if (task.getEndTime() == null) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.MILLISECOND, 0);
                c.set(Calendar.SECOND, 0);
                task.setEndTime(c.getTime().toInstant());
            }
            if (task.getStartTime() == null) {
                task.setStartTime(task.getEndTime());
            }
            if (task.getDuration() == null) {
                long between = ChronoUnit.MINUTES.between(task.getStartTime(), task.getEndTime());
                task.setDuration(new TimePeriod(between, ChronoUnit.MINUTES));
            }
        } else {
            if (task.getStartTime() == null) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.MILLISECOND, 0);
                c.set(Calendar.SECOND, 0);
                task.setStartTime(c.getTime().toInstant());
            }
        }
        if (task.getProject() == null) {
            task.setProject("misc");
        }

        String project = task.getProject();
        if (project != null) {
            NProject p = service.projects().getProject(project);
            if (p == null) {
                p = new NProject();
                p.setName(project);
                service.projects().updateProject(p);
            }
            task.setProject(p.getId());
        }
        dal.store(task);
    }

    public void addTask(NTask task) {
        if (task.getName() == null) {
            task.setName("todo");
        }
        if (task.getCreationTime() == null) {
            task.setCreationTime(Instant.now());
        }
        if (task.getStatus() == null) {
            task.setStatus(NTaskStatus.TODO);
        }
        if (task.getFlag() == null) {
            task.setFlag(NFlag.NONE);
        }
        if (task.getPriority() == null) {
            task.setPriority(NPriority.NORMAL);
        }
        if (task.getObservations() == null) {
            task.setObservations("");
        }
        if (task.getDueTime() == null) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.MILLISECOND, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.HOUR_OF_DAY, 23);
            task.setDueTime(c.toInstant());
        }

        if (task.getStatus() == NTaskStatus.DONE) {
            if (task.getEndTime() == null) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.MILLISECOND, 0);
                c.set(Calendar.SECOND, 0);
                task.setEndTime(c.getTime().toInstant());
            }
            if (task.getStartTime() == null) {
                task.setStartTime(task.getEndTime());
            }
            if (task.getDuration() == null) {
                long between = ChronoUnit.MINUTES.between(task.getStartTime(), task.getEndTime());
                task.setDuration(new TimePeriod(between, ChronoUnit.MINUTES));
            }
        } else {
            if (task.getStartTime() == null) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.MILLISECOND, 0);
                c.set(Calendar.SECOND, 0);
                task.setStartTime(c.getTime().toInstant());
            }
        }
        if (task.getProject() == null) {
            task.setProject("misc");
        }

        String project = task.getProject();
        if (project != null) {
            NProject p = service.projects().getProject(project);
            if (p == null) {
                p = new NProject();
                p.setName(project);
                service.projects().updateProject(p);
            }
        }
        task.setId(null);
        dal.store(task);
    }

    public Stream<NTask> findTasks(NTaskStatusFilter statusFilter, Instant endTime, int lastCount, ChronoUnit lastUnit, Predicate<NTask> whereFilter, NJobGroup groupBy, ChronoUnit groupTimeUnit, TimespanPattern groupPattern) {
        if (statusFilter == null) {
            statusFilter = NTaskStatusFilter.RECENT;
        }
        NTaskStatusFilter statusFilter0 = statusFilter;
        Instant endTime0 = endTime == null ? Instant.now() : endTime;
        Stream<NTask> s = findAllTasks().filter(
                x -> {

                    switch (statusFilter0) {
                        case ALL: {
                            return true;
                        }
                        case OPEN: {
                            return x.getStatus() == NTaskStatus.WIP || x.getStatus() == NTaskStatus.TODO;
                        }
                        case RECENT: {
                            if (x.getStatus() == NTaskStatus.WIP || x.getStatus() == NTaskStatus.TODO) {
                                return true;
                            }
                            Instant m = x.getModificationTime();
                            if (m == null) {
                                return true;
                            }
                            //last three days...
                            long days = (Instant.now().toEpochMilli() - m.toEpochMilli()) / 3600000 / 24;
                            return days > -3;
                        }
                        case CLOSED: {
                            return x.getStatus() == NTaskStatus.DONE || x.getStatus() == NTaskStatus.CANCELLED;
                        }
                        case WIP: {
                            return x.getStatus() == NTaskStatus.WIP;
                        }
                        case TODO: {
                            return x.getStatus() == NTaskStatus.TODO;
                        }
                        case DONE: {
                            return x.getStatus() == NTaskStatus.DONE;
                        }
                        case CANCELLED: {
                            return x.getStatus() == NTaskStatus.CANCELLED;
                        }
                        default: {
                            return true;
                        }
                    }
                }
        ).filter(x -> {
            if (!(endTime == null || x.getStartTime().compareTo(endTime) <= 0)) {
                return false;
            }
            if (lastCount >= 0 && lastUnit != null) {
                LocalDate endDate = endTime0.atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate startDate = x.getStartTime().atZone(ZoneId.systemDefault()).toLocalDate();
                return lastCount <= 0 || lastUnit.between(startDate, endDate) <= lastCount;
            }
            return true;
        }).sorted((o1, o2)
                -> {
            NTaskStatus s1 = o1.getStatus();
            NTaskStatus s2 = o2.getStatus();
            if (s1 == null) {
                s1 = NTaskStatus.TODO;
            }
            if (s2 == null) {
                s2 = NTaskStatus.TODO;
            }
            if (s1.isClosed() && !s2.isClosed()) {
                return 1;
            }
            if (!s1.isClosed() && s2.isClosed()) {
                return -1;
            }
            NPriority p1 = o1.getPriority();
            NPriority p2 = o2.getPriority();
            if (p1 == null) {
                p1 = NPriority.NORMAL;
            }
            if (p2 == null) {
                p2 = NPriority.NORMAL;
            }
            int r = p2.compareTo(p1);
            if (r != 0) {
                return r;
            }
            Instant st1 = o1.getStartTime();
            Instant st2 = o2.getStartTime();
            if (st1 == null && st2 == null) {
                return 0;
            }
            if (st1 == null) {
                return 1;
            }
            if (st2 == null) {
                return -1;
            }
            return -st1.compareTo(st2);
        }
        );
        if (whereFilter != null) {
            s = s.filter(whereFilter);
        }
        if (lastUnit == null && lastCount > 0) {
            s = s.limit(lastCount);
        }
        if (groupBy != null) {
            s = s.collect(Collectors.groupingBy(
                    (NJobGroup.PROJECT_NAME.equals(groupBy)) ? x -> x.getProject()
                    : (NJobGroup.NAME.equals(groupBy)) ? x -> x.getProject() + ":" + x.getName()
                    : (NJobGroup.SUMMARY.equals(groupBy)) ? x -> "summary"
                    : x -> x.getId()
            ))
                    .entrySet().stream().map(x -> groupTasks(x.getValue(), groupTimeUnit, groupPattern));
        }
        return s;
    }

    public NTask getTask(String taskId) {
        return dal.load(NTask.class, taskId);
    }

    public boolean removeTask(String taskId) {
        long count = findAllTasks().filter(x -> taskId.equals(x.getParentTaskId())).count();
        if (count > 1) {
            throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("Task is used in %d tasks. It cannot be removed.",count));
        } else if (count > 0) {
            throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("Task is used in one task. It cannot be removed."));
        }
        return dal.delete(NTask.class, taskId);
    }

    public NTask groupTasks(Collection<NTask> value, ChronoUnit timeUnit, TimespanPattern hoursPerDay) {
        return groupTasks(value.toArray(new NTask[0]), timeUnit, hoursPerDay);
    }

    public NTask groupTasks(NTask[] value, ChronoUnit timeUnit, TimespanPattern hoursPerDay) {
        NTask t = new NTask();
        TimePeriods tp = new TimePeriods();
        TreeSet<ChronoUnit> atu = new TreeSet<>();
        TreeSet<String> names = new TreeSet<String>();
        TreeSet<String> projects = new TreeSet<String>();
        TreeSet<NTaskStatus> statuses = new TreeSet<NTaskStatus>();
        for (NTask t2 : value) {
            if (t2.getStartTime() != null) {
                t.setStartTime(t2.getStartTime());
            }
            if (t2.getDuration() != null) {
                tp.add(t2.getDuration());
                atu.add(t2.getDuration().getUnit());
            }
            if (t2.getName() != null) {
                names.add(t2.getName());
            }
            if (t2.getProject() != null) {
                projects.add(t2.getProject());
            }
            if (t2.getStatus() != null) {
                statuses.add(t2.getStatus());
            }
        }
        t.setProject(projects.size() == 0 ? "" : projects.size() == 1 ? projects.toArray()[0].toString()
                : (projects.size() <= 3 || String.join(",", projects).length() < 20) ? String.join(",", projects)
                : (String.valueOf(projects.size()) + " projects")
        );
        if (statuses.size() >= 1) {
            t.setStatus(statuses.first());
        }
        ChronoUnit[] atu0 = atu.toArray(new ChronoUnit[0]);
        String tasks = " Task" + ((value.length == 1) ? "" : "s");
        String named = (names.size() == 0) ? "" : (names.size() == 1) ? (" named " + names.toArray()[0]) : (" with " + (names.size()) + " different names");
        t.setName(value.length + tasks + named);
        t.setDuration(tp.toUnit(timeUnit != null ? timeUnit : (atu0.length == 0 ? ChronoUnit.DAYS : atu0[0]), hoursPerDay));
        t.setId(UUID.randomUUID().toString());
        return t;
    }

    public Stream<NTask> findAllTasks() {
        return dal.search(NTask.class);
    }
}

package net.vpc.app.nuts.toolbox.njob;

import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.toolbox.njob.model.*;
import net.vpc.app.nuts.toolbox.njob.time.TimePeriod;
import net.vpc.app.nuts.toolbox.njob.time.TimePeriods;
import net.vpc.app.nuts.toolbox.njob.time.TimespanPattern;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Collection;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JobService {
    private NutsApplicationContext context;
    private NDal dal;

    public JobService(NutsApplicationContext context) {
        this.context = context;
        this.dal = new NDal(context);
    }

    public static String wildcardToRegex(String pattern) {
        if (pattern == null) {
            pattern = "*";
        }
        int i = 0;
        char[] cc = pattern.toCharArray();
        StringBuilder sb = new StringBuilder("^");
        while (i < cc.length) {
            char c = cc[i];
            switch (c) {
                case '.':
                case '$':
                case '{':
                case '}':
                case '+': {
                    sb.append('\\').append(c);
                    break;
                }
                case '?': {
                    sb.append(".");
                    break;
                }
                case '*': {
                    sb.append(".*");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
            i++;
        }
        sb.append('$');
        return sb.toString();
    }

    public void addProject(NProject p) {
        String name = p.getName();
        if (name == null) {
            throw new IllegalArgumentException("Invalid project");
        }
        NProject p0 = getProject(name);
        if (p0 != null) {
            throw new NutsIllegalArgumentException(context.getWorkspace(), "project already exists: " + name);
        }
        updateProject(p);
    }

    public void updateProject(NProject p) {
        String name = p.getName();
        if (name == null) {
            throw new IllegalArgumentException("Invalid project");
        }
        if (p.getBeneficiary() == null) {
            p.setBeneficiary("unspecified");
        }
        if (p.getCompany() == null) {
            p.setCompany(p.getBeneficiary());
        }
        if (p.getStartTime() == null) {
            p.setStartTime(Instant.now());
        }
        if (p.getStartWeekDay() == null) {
            p.setStartWeekDay(NDay.MONDAY);
        }
        dal.store(p);
    }

    public void addJob(NJob job) {
        if (job.getName() == null) {
            job.setName("work");
        }

        if (job.getStartTime() == null) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.MILLISECOND, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.HOUR, 8);
            job.setStartTime(c.getTime().toInstant());
        }
        if (job.getDuration() == null) {
            job.setDuration(new TimePeriod(1, TimeUnit.HOURS));
        }
        if (job.getProject() == null) {
            job.setProject("misc");
        }

        String project = job.getProject();
        if (project != null) {
            NProject p = dal.load(NProject.class, project);
            if (p == null) {
                p = new NProject();
                p.setName(project);
                updateProject(p);
            }
        }
        job.setId(null);
//        if (job.getId() != null) {
//            NJob r = dal.load(NJob.class, job.getId());
//            if (r == null) {
//                throw new IllegalArgumentException();
//            }
//        }
        dal.store(job);
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
        if (task.getStartTime() == null) {
            if (task.getStatus() == NTaskStatus.WIP || task.getStatus() == NTaskStatus.TODO) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.MILLISECOND, 0);
                c.set(Calendar.SECOND, 0);
                task.setStartTime(c.getTime().toInstant());
            }
        }
        if (task.getEndTime() == null && task.getStatus() == NTaskStatus.DONE) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.MILLISECOND, 0);
            c.set(Calendar.SECOND, 0);
            task.setEndTime(c.getTime().toInstant());
        }
        if (task.getDuration() == null && task.getEndTime() != null && task.getEndTime() != null && task.getStatus() == NTaskStatus.DONE) {
            long between = ChronoUnit.MINUTES.between(task.getStartTime(), task.getEndTime());
            task.setDuration(new TimePeriod(between, TimeUnit.MINUTES));
        }
        if (task.getProject() == null) {
            task.setProject("misc");
        }

        String project = task.getProject();
        if (project != null) {
            NProject p = dal.load(NProject.class, project);
            if (p == null) {
                p = new NProject();
                p.setName(project);
                updateProject(p);
            }
        }
//        task.setId(null);
//        if (task.getProject() != null) {
//            NProject r = dal.load(NProject.class, task.getId());
//            if (r == null) {
//                throw new IllegalArgumentException();
//            }
//        }
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
        if (task.getStartTime() == null) {
            if (task.getStatus() == NTaskStatus.WIP || task.getStatus() == NTaskStatus.TODO) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.MILLISECOND, 0);
                c.set(Calendar.SECOND, 0);
                task.setStartTime(c.getTime().toInstant());
            }
        }
        if (task.getEndTime() == null && task.getStatus() == NTaskStatus.DONE) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.MILLISECOND, 0);
            c.set(Calendar.SECOND, 0);
            task.setEndTime(c.getTime().toInstant());
        }
        if (task.getDuration() == null && task.getEndTime() != null && task.getEndTime() != null && task.getStatus() == NTaskStatus.DONE) {
            long between = ChronoUnit.MINUTES.between(task.getStartTime(), task.getEndTime());
            task.setDuration(new TimePeriod(between, TimeUnit.MINUTES));
        }
        if (task.getProject() == null) {
            task.setProject("misc");
        }

        String project = task.getProject();
        if (project != null) {
            NProject p = dal.load(NProject.class, project);
            if (p == null) {
                p = new NProject();
                p.setName(project);
                updateProject(p);
            }
        }
        task.setId(null);
//        if (task.getProject() != null) {
//            NProject r = dal.load(NProject.class, task.getId());
//            if (r == null) {
//                throw new IllegalArgumentException();
//            }
//        }
        dal.store(task);
    }

    public Instant getStartWeek(Instant date, NDay startWeekDay) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date.toEpochMilli());
        int d = c.get(Calendar.DAY_OF_WEEK);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        int d0 = startWeekDay.ordinal() - NDay.SUNDAY.ordinal() + 1;
        if (d != d0) {
            c.add(Calendar.DAY_OF_YEAR, d0 - d);
        }
        return Instant.ofEpochMilli(c.getTimeInMillis());
    }

    public Instant getStartMonth(Instant date) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date.toEpochMilli());
        int d = c.get(Calendar.DAY_OF_WEEK);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.DAY_OF_MONTH, 1);
        return Instant.ofEpochMilli(c.getTimeInMillis());
    }

    public Instant subWeek(int weekIndex) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(Instant.now().toEpochMilli());
        int d = c.get(Calendar.DAY_OF_WEEK);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.DAY_OF_WEEK, -weekIndex);
        return Instant.ofEpochMilli(c.getTimeInMillis());
    }

    public Stream<NJob> findWeekJobs(Instant date) {
        return dal.search(NJob.class).filter(x -> {
            NDay d = NDay.MONDAY;
            if (x.getProject() != null) {
                NDay d0 = dal.load(NProject.class, x.getProject()).getStartWeekDay();
                if (d0 != null) {
                    d = d0;
                }
            }
            return getStartWeek(date, d).equals(getStartWeek(x.getStartTime(), d));
        });
    }

    public Stream<NJob> findLastJobs(Instant endTime, int lastCount, ChronoUnit lastUnit, Predicate<NJob> whereFilter, NJobGroup groupBy, TimeUnit groupTimeUnit, TimespanPattern groupPattern) {
        Instant endTime0 = endTime == null ? Instant.now() : endTime;
        Stream<NJob> s = dal.search(NJob.class).filter(x -> {
            if (!(endTime == null || x.getStartTime().compareTo(endTime) <= 0)) {
                return false;
            }
            if (lastCount >= 0 && lastUnit != null) {
                LocalDate endDate = endTime0.atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate startDate = x.getStartTime().atZone(ZoneId.systemDefault()).toLocalDate();
                return lastCount <= 0 || lastUnit.between(startDate, endDate) <= lastCount;
            }
            return true;
        }).sorted((o1, o2) -> -o1.getStartTime().compareTo(o2.getStartTime()));
        if (whereFilter != null) {
            s = s.filter(whereFilter);
        }
        if (lastUnit == null && lastCount > 0) {
            s = s.limit(lastCount);
        }
        if (groupBy != null) {
            s = s.collect(Collectors.groupingBy(
                    (NJobGroup.PROJECT_NAME.equals(groupBy)) ? x -> x.getProject() :
                            (NJobGroup.JOB_NAME.equals(groupBy)) ? x -> x.getProject() + ":" + x.getName() :
                                    (NJobGroup.SUMMARY.equals(groupBy)) ? x -> "summary" :
                                            x -> x.getId()

            ))
                    .entrySet().stream().map(x -> groupJobs(x.getValue(), groupTimeUnit, groupPattern));
        }
        return s;
    }

    public Stream<NTask> findTasks(NTaskStatusFilter statusFilter, Instant endTime, int lastCount, ChronoUnit lastUnit, Predicate<NTask> whereFilter, NJobGroup groupBy, TimeUnit groupTimeUnit, TimespanPattern groupPattern) {
        if (statusFilter == null) {
            statusFilter = NTaskStatusFilter.OPEN;
        }
        NTaskStatusFilter statusFilter0 = statusFilter;
        Instant endTime0 = endTime == null ? Instant.now() : endTime;
        Stream<NTask> s = dal.search(NTask.class).filter(
                x -> {

                    switch (statusFilter0) {
                        case ALL: {
                            return true;
                        }
                        case OPEN: {
                            return x.getStatus() == NTaskStatus.WIP || x.getStatus() == NTaskStatus.TODO;
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
        }).sorted((o1, o2) ->
                {
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
                    return
                            -o1.getStartTime().compareTo(o2.getStartTime());
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
                    (NJobGroup.PROJECT_NAME.equals(groupBy)) ? x -> x.getProject() :
                            (NJobGroup.JOB_NAME.equals(groupBy)) ? x -> x.getProject() + ":" + x.getName() :
                                    (NJobGroup.SUMMARY.equals(groupBy)) ? x -> "summary" :
                                            x -> x.getId()

            ))
                    .entrySet().stream().map(x -> groupTasks(x.getValue(), groupTimeUnit, groupPattern));
        }
        return s;
    }

    public Stream<NJob> findMonthJobs(Instant date) {
        return dal.search(NJob.class).filter(x -> {
            return getStartMonth(date).equals(getStartMonth(x.getStartTime()));
        });
    }

    public Stream<NJob> tailWeekJobs(int count) {
        Instant w0 = getStartWeek(subWeek(count), NDay.SUNDAY);
        Instant w1 = getStartWeek(subWeek(count - 1), NDay.SUNDAY);
        return dal.search(NJob.class).filter(x -> {
            return x.getStartTime().compareTo(w0) >= 0 && x.getStartTime().compareTo(w1) < 0;
        });
    }

    public NJob getJob(String jobId) {
        return dal.load(NJob.class, jobId);
    }

    public NTask getTask(String taskId) {
        return dal.load(NTask.class, taskId);
    }

    public NProject getProject(String projectName) {
        return dal.load(NProject.class, projectName);
    }

    public boolean removeJob(String jobId) {
        return dal.delete(NJob.class, jobId);
    }

    public boolean removeTask(String taskId) {
        return dal.delete(NTask.class, taskId);
    }

    public boolean removeProject(String projectName) {
        if (dal.search(NJob.class).anyMatch(x -> projectName.equals(x.getProject()))) {
            throw new IllegalArgumentException("Project is used in on or multiple jobs. It cannot e removed.");
        }
        return dal.delete(NProject.class, projectName);
    }

    public Stream<NProject> findProjects() {
        return dal.search(NProject.class);
    }

    public NJob groupJobs(Collection<NJob> value, TimeUnit timeUnit, TimespanPattern hoursPerDay) {
        return groupJobs(value.toArray(new NJob[0]), timeUnit, hoursPerDay);
    }

    public NJob groupJobs(NJob[] value, TimeUnit timeUnit, TimespanPattern hoursPerDay) {
        NJob t = new NJob();
        TimePeriods tp = new TimePeriods();
        TreeSet<TimeUnit> atu = new TreeSet<TimeUnit>();
        TreeSet<String> names = new TreeSet<String>();
        TreeSet<String> projects = new TreeSet<String>();
        for (NJob nJob : value) {
            tp.add(nJob.getDuration());
            t.setStartTime(nJob.getStartTime());
            atu.add(nJob.getDuration().getUnit());
            names.add(nJob.getName());
            projects.add(nJob.getProject());
        }
        t.setProject(projects.size() == 0 ? "" : projects.size() == 1 ? projects.toArray()[0].toString() :
                (projects.size() <= 3 || String.join(",", projects).length() < 20) ? String.join(",", projects) :
                        (String.valueOf(projects.size()) + " projects")
        );
        TimeUnit[] atu0 = atu.toArray(new TimeUnit[0]);
        String jobs = " Job" + ((value.length == 1) ? "" : "s");
        String named = (names.size() == 0) ? "" : (names.size() == 1) ? (" named " + names.toArray()[0]) : (" with " + (names.size()) + " different names");
        t.setName(value.length + jobs + named);
        t.setDuration(tp.toUnit(timeUnit != null ? timeUnit : (atu0.length == 0 ? TimeUnit.DAYS : atu0[0]), hoursPerDay));
        t.setId(UUID.randomUUID().toString());
        return t;
    }

    public NTask groupTasks(Collection<NTask> value, TimeUnit timeUnit, TimespanPattern hoursPerDay) {
        return groupTasks(value.toArray(new NTask[0]), timeUnit, hoursPerDay);
    }

    public NTask groupTasks(NTask[] value, TimeUnit timeUnit, TimespanPattern hoursPerDay) {
        NTask t = new NTask();
        TimePeriods tp = new TimePeriods();
        TreeSet<TimeUnit> atu = new TreeSet<TimeUnit>();
        TreeSet<String> names = new TreeSet<String>();
        TreeSet<String> projects = new TreeSet<String>();
        TreeSet<NTaskStatus> statuses = new TreeSet<NTaskStatus>();
        for (NTask nJob : value) {
            tp.add(nJob.getDuration());
            t.setStartTime(nJob.getStartTime());
            atu.add(nJob.getDuration().getUnit());
            names.add(nJob.getName());
            projects.add(nJob.getProject());
            statuses.add(nJob.getStatus());
        }
        t.setProject(projects.size() == 0 ? "" : projects.size() == 1 ? projects.toArray()[0].toString() :
                (projects.size() <= 3 || String.join(",", projects).length() < 20) ? String.join(",", projects) :
                        (String.valueOf(projects.size()) + " projects")
        );
        if (statuses.size() == 1) {
            t.setStatus(statuses.first());
        }
        TimeUnit[] atu0 = atu.toArray(new TimeUnit[0]);
        String jobs = " Job" + ((value.length == 1) ? "" : "s");
        String named = (names.size() == 0) ? "" : (names.size() == 1) ? (" named " + names.toArray()[0]) : (" with " + (names.size()) + " different names");
        t.setName(value.length + jobs + named);
        t.setDuration(tp.toUnit(timeUnit != null ? timeUnit : (atu0.length == 0 ? TimeUnit.DAYS : atu0[0]), hoursPerDay));
        t.setId(UUID.randomUUID().toString());
        return t;
    }
}

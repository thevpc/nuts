package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.toolbox.njob.model.*;
import net.thevpc.nuts.toolbox.njob.time.TimePeriod;
import net.thevpc.nuts.toolbox.njob.time.TimePeriods;
import net.thevpc.nuts.toolbox.njob.time.TimespanPattern;
import net.thevpc.nuts.toolbox.njob.time.WeekDay;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JobService {
    private NutsApplicationContext context;
    private NJobConfigStore dal;

    public JobService(NutsApplicationContext context) {
        this.context = context;
        this.dal = new NJobConfigStore(context);
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
            throw new NutsIllegalArgumentException(context.getWorkspace(),"invalid project");
        }
        p.setId(null);
        NProject p0 = getProject(name);
        if (p0 != null) {
            throw new NutsIllegalArgumentException(context.getWorkspace(), "project already exists: " + name);
        }
        if (p.getBeneficiary() == null) {
            p.setBeneficiary("unspecified");
        }
        if (p.getCompany() == null) {
            String t = findAllProjects().filter(x -> x.getBeneficiary().equals(p.getBeneficiary()))
                    .sorted(Comparator.comparing(NProject::getCreationTime).reversed())
                    .map(x -> p.getBeneficiary())
                    .findFirst().orElse(null);
            if (t == null) {
                t = p.getBeneficiary();
            }
            p.setCompany(t);
        }
        p.setCreationTime(Instant.now());
        p.setModificationTime(p.getCreationTime());
        if (p.getStartTime() == null) {
            p.setStartTime(Instant.now());
        }
        if (p.getStartWeekDay() == null) {
            p.setStartWeekDay(WeekDay.MONDAY);
        }
//        dal.load(NProject.class,p.getId());
        dal.store(p);
    }

    private Stream<NProject> findAllProjects() {
        return dal.search(NProject.class);
    }

    public void updateProject(NProject p) {
        String name = p.getName();
        if (name == null) {
            throw new NutsIllegalArgumentException(context.getWorkspace(),"Invalid project");
        }
        String id = p.getId();
        if (id == null) {
            p.setId(dal.generateId(NProject.class));
        }
        if (p.getBeneficiary() == null) {
            p.setBeneficiary("unspecified");
        }
        if (p.getCompany() == null) {
            p.setCompany(p.getBeneficiary());
        }
        p.setModificationTime(Instant.now());
        if (p.getCreationTime() == null) {
            p.setCreationTime(p.getModificationTime());
        }
        if (p.getStartTime() == null) {
            p.setStartTime(Instant.now());
        }
        if (p.getStartWeekDay() == null) {
            p.setStartWeekDay(WeekDay.MONDAY);
        }
//        dal.load(NProject.class,p.getId());
        dal.store(p);
    }

    public boolean isIdFormat(String s) {
        return s != null && s.matches("[0-9a-fA-F-]{36}");
    }

    public void addJob(NJob job) {
        if (job.getName() == null) {
            job.setName("work");
        }
        job.setCreationTime(Instant.now());
        job.setModificationTime(job.getCreationTime());
        if (job.getStartTime() == null) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.MILLISECOND, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.HOUR, 8);
            job.setStartTime(c.getTime().toInstant());
        }
        if (job.getDuration() == null) {
            job.setDuration(new TimePeriod(1, ChronoUnit.HOURS));
        }
        if (job.getProject() == null) {
            job.setProject("misc");
        }

        String project = job.getProject();
        if (project != null) {
            NProject p = getProject(project);
            if (p == null) {
                if (isIdFormat(project)) {
                    throw new NoSuchElementException("Project not found: " + project);
                }
                p = new NProject();
                p.setName(project);
                addProject(p);
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
            task.setDuration(new TimePeriod(between, ChronoUnit.MINUTES));
        }
        if (task.getProject() == null) {
            task.setProject("misc");
        }

        String project = task.getProject();
        if (project != null) {
            NProject p = getProject(project);
            if (p == null) {
                p = new NProject();
                p.setName(project);
                updateProject(p);
            }
            task.setProject(p.getId());
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

    public void updateJob(NJob job) {
        if (job.getName() == null) {
            job.setName("work");
        }
        Instant now = Instant.now();
        if (job.getCreationTime() == null) {
            job.setCreationTime(now);
        }
        job.setModificationTime(now);
        if (job.getObservations() == null) {
            job.setObservations("");
        }
        if (job.getStartTime() == null) {

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
            job.setDuration(new TimePeriod(1, ChronoUnit.HOURS));
        }
        if (job.getProject() == null) {
            job.setProject("misc");
        }

        String project = job.getProject();
        if (project != null) {
            NProject p = getProject(project);
            if (p == null) {
                if (isIdFormat(project)) {
                    throw new NoSuchElementException("Project not found: " + project);
                }
                p = new NProject();
                p.setName(project);
                addProject(p);
            }
        }
        dal.store(job);
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
            task.setDuration(new TimePeriod(between, ChronoUnit.MINUTES));
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

    public Instant getStartWeek(Instant date, WeekDay startWeekDay) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date.toEpochMilli());
        int d = c.get(Calendar.DAY_OF_WEEK);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        int d0 = startWeekDay.ordinal() - WeekDay.SUNDAY.ordinal() + 1;
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
        return findAllJobs().filter(x -> {
            WeekDay d = WeekDay.MONDAY;
            if (x.getProject() != null) {
                WeekDay d0 = dal.load(NProject.class, x.getProject()).getStartWeekDay();
                if (d0 != null) {
                    d = d0;
                }
            }
            return getStartWeek(date, d).equals(getStartWeek(x.getStartTime(), d));
        });
    }

    public Stream<NJob> findLastJobs(Instant endTime, int lastCount, ChronoUnit lastUnit, Predicate<NJob> whereFilter, NJobGroup groupBy, ChronoUnit groupTimeUnit, TimespanPattern groupPattern) {
        Instant endTime0 = endTime == null ? Instant.now() : endTime;
        Stream<NJob> s = findAllJobs().filter(x -> {
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
                            (NJobGroup.NAME.equals(groupBy)) ? x -> x.getProject() + ":" + x.getName() :
                                    (NJobGroup.SUMMARY.equals(groupBy)) ? x -> "summary" :
                                            x -> x.getId()

            ))
                    .entrySet().stream().map(x -> groupJobs(x.getValue(), groupTimeUnit, groupPattern));
        }
        return s;
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
                    return
                            -st1.compareTo(st2);
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
                            (NJobGroup.NAME.equals(groupBy)) ? x -> x.getProject() + ":" + x.getName() :
                                    (NJobGroup.SUMMARY.equals(groupBy)) ? x -> "summary" :
                                            x -> x.getId()

            ))
                    .entrySet().stream().map(x -> groupTasks(x.getValue(), groupTimeUnit, groupPattern));
        }
        return s;
    }

    public Stream<NJob> findMonthJobs(Instant date) {
        if (date == null) {
            date = Instant.now();
        }
        Instant finalDate = date;
        return findAllJobs().filter(x -> {
            return getStartMonth(finalDate).equals(getStartMonth(x.getStartTime()));
        });
    }

    public Stream<NJob> tailWeekJobs(int count) {
        Instant w0 = getStartWeek(subWeek(count), WeekDay.SUNDAY);
        Instant w1 = getStartWeek(subWeek(count - 1), WeekDay.SUNDAY);
        return findAllJobs().filter(x -> {
            return x.getStartTime().compareTo(w0) >= 0 && x.getStartTime().compareTo(w1) < 0;
        });
    }

    public NJob getJob(String jobId) {
        return dal.load(NJob.class, jobId);
    }

    public NTask getTask(String taskId) {
        return dal.load(NTask.class, taskId);
    }

    public NProject getProjectOrError(String projectNameOrId) {
        NProject p = getProject(projectNameOrId);
        if (p == null) {
            throw new NutsIllegalArgumentException(context.getWorkspace(),"project not found " + projectNameOrId);
        }
        return p;
    }

    public NProject getProject(String projectNameOrId) {
        if (isIdFormat(projectNameOrId)) {
            return dal.load(NProject.class, projectNameOrId);
        } else {
            return findAllProjects().filter(x -> Objects.equals(x.getName(), projectNameOrId))
                    .findFirst().orElse(null);
        }
    }

    public boolean removeJob(String jobId) {
        long count = findAllTasks().filter(x -> jobId.equals(x.getJobId())).count();
        if (count > 1) {
            throw new NutsIllegalArgumentException(context.getWorkspace(),"Job is used in " + count + " tasks. It cannot be removed.");
        } else if (count > 0) {
            throw new NutsIllegalArgumentException(context.getWorkspace(),"Job is used in one task. It cannot be removed.");
        }
        return dal.delete(NJob.class, jobId);
    }

    public boolean removeTask(String taskId) {
        long count = findAllTasks().filter(x -> taskId.equals(x.getParentTaskId())).count();
        if (count > 1) {
            throw new NutsIllegalArgumentException(context.getWorkspace(),"Task is used in " + count + " tasks. It cannot be removed.");
        } else if (count > 0) {
            throw new NutsIllegalArgumentException(context.getWorkspace(),"Task is used in one task. It cannot be removed.");
        }
        return dal.delete(NTask.class, taskId);
    }

    public boolean removeProject(String projectName) {
        long countJobs = findAllJobs().filter(x -> projectName.equals(x.getProject())).count();
        long countTasks = findAllTasks().filter(x -> projectName.equals(x.getProject())).count();
        if (countJobs > 0 || countTasks > 0) {
            StringBuilder sb = new StringBuilder();
            if (countJobs > 0) {
                sb.append(countJobs > 1 ? "one job" : (countJobs + " jobs"));
            }
            if (countTasks > 0) {
                if (sb.length() > 0) {
                    sb.append(" and ");
                }
                sb.append(countTasks > 1 ? "one task" : (countTasks + " task"));
            }
            throw new NutsIllegalArgumentException(context.getWorkspace(),"Project is used in " + sb + ". It cannot be removed.");
        }
        return dal.delete(NProject.class, projectName);
    }

    private Stream<NJob> findAllJobs() {
        return dal.search(NJob.class);
    }

    public Stream<NProject> findProjects() {
        return findAllProjects();
    }

    public NJob groupJobs(Collection<NJob> value, ChronoUnit timeUnit, TimespanPattern hoursPerDay) {
        return groupJobs(value.toArray(new NJob[0]), timeUnit, hoursPerDay);
    }

    public NJob groupJobs(NJob[] value, ChronoUnit timeUnit, TimespanPattern hoursPerDay) {
        NJob t = new NJob();
        TimePeriods tp = new TimePeriods();
        TreeSet<ChronoUnit> atu = new TreeSet<ChronoUnit>();
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
        ChronoUnit[] atu0 = atu.toArray(new ChronoUnit[0]);
        String jobs = " Job" + ((value.length == 1) ? "" : "s");
        String named = (names.size() == 0) ? "" : (names.size() == 1) ? (" named " + names.toArray()[0]) : (" with " + (names.size()) + " different names");
        t.setName(value.length + jobs + named);
        t.setDuration(tp.toUnit(timeUnit != null ? timeUnit : (atu0.length == 0 ? ChronoUnit.DAYS : atu0[0]), hoursPerDay));
        t.setId(UUID.randomUUID().toString());
        return t;
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
        t.setProject(projects.size() == 0 ? "" : projects.size() == 1 ? projects.toArray()[0].toString() :
                (projects.size() <= 3 || String.join(",", projects).length() < 20) ? String.join(",", projects) :
                        (String.valueOf(projects.size()) + " projects")
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

    public boolean isUsedProject(String id) {
        NProject destinationJob = getProject(id);
        if (destinationJob == null) {
            return false;
        }
        if (findAllTasks().filter(x -> Objects.equals(x.getProject(), destinationJob.getId()) || Objects.equals(x.getProject(), destinationJob.getName()))
                .findFirst().orElse(null) != null) {
            return true;
        }
        if (findAllJobs().filter(x -> Objects.equals(x.getProject(), destinationJob.getId()) || Objects.equals(x.getProject(), destinationJob.getName()))
                .findFirst().orElse(null) != null) {
            return true;
        }
        return false;
    }

    public void mergeProjects(String destination, String... others) {
        NProject destinationJob = getProjectOrError(destination);
        List<NProject> src = Arrays.asList(others).stream().map(x -> getProjectOrError(x)).collect(Collectors.toList());
        for (NProject s : src) {
            findAllTasks().filter(x -> Objects.equals(x.getProject(), s.getId()) || Objects.equals(x.getProject(), s.getName()))
                    .forEach(
                            t -> {
                                t.setProject(destinationJob.getId());
                                updateTask(t);
                            }
                    );

            findAllJobs().filter(x -> Objects.equals(x.getProject(), s.getId()) || Objects.equals(x.getProject(), s.getName()))
                    .forEach(
                            t -> {
                                t.setProject(destinationJob.getId());
                                updateJob(t);
                            }
                    );
            removeProject(s.getId());
        }
    }

    private Stream<NTask> findAllTasks() {
        return dal.search(NTask.class);
    }
}

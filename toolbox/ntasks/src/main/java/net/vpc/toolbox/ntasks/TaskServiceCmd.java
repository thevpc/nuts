package net.vpc.toolbox.ntasks;

import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TaskServiceCmd {
    private TaskService service;
    private NutsApplicationContext context;

    public TaskServiceCmd(NutsApplicationContext context) {
        this.context = context;
        this.service = new TaskService(context);
    }


    public void runTaskAdd(NutsCommandLine cmd) {
        NTask t = new NTask();
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            if (t.getStartTime() == null && a.getStringKey().equals("on")) {
                t.setStartTime(new TimeParser().parseInstant(cmd.next().toString()));
            } else if (t.getStartTime() == null && a.getStringKey().equals("at")) {
                t.setStartTime(new TimeParser().setTimeOnly(true).parseInstant(cmd.next().toString()));
            } else if (t.getProject() == null && a.getStringKey().equals("for")) {
                t.setProject(cmd.next().toString());
            } else if (t.getObservations() == null && a.getStringKey().equals("desc")) {
                t.setObservations(cmd.next().toString());
            } else {
                NTimePeriod it = t.getDuration() != null ? null : NTimePeriod.parse(a.toString(), true);
                if (it != null) {
                    t.setDuration(it);
                } else {
                    if (t.getName() == null) {
                        t.setName(a.toString());
                    } else {
                        cmd.unexpectedArgument();
                    }
                }
            }
        }
        service.saveTask(t);
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("<<%s>> task added.\n",
                    t.getId()
            );
        }
    }

    public void runProjectAdd(NutsCommandLine cmd) {
        NProject t = new NProject();
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            if (t.getStartTime() == null && a.getStringKey().equals("on")) {
                t.setStartTime(new TimeParser().parseInstant(cmd.next().toString()));
            } else if (t.getStartTime() == null && a.getStringKey().equals("at")) {
                t.setStartTime(new TimeParser().setTimeOnly(true).parseInstant(cmd.next().toString()));
            } else if (t.getCustomer() == null && a.getStringKey().equals("for")) {
                t.setCustomer(cmd.next().toString());
            } else if (t.getCompany() == null && a.getStringKey().equals("via")) {
                t.setCompany(cmd.next().toString());
            } else if (t.getStartWeekDay() == null && a.getStringKey().equals("week")) {
                t.setStartWeekDay(NDay.valueOf(cmd.nextString().toString().toUpperCase()));
            } else if (t.getObservations() == null && a.getStringKey().equals("obs")) {
                t.setObservations(cmd.next().toString());
            } else {
                if (t.getName() == null) {
                    t.setName(a.toString());
                } else {
                    cmd.unexpectedArgument();
                }
            }
        }
        service.addProject(t);
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("<<%s>> project added.\n",
                    t.getName()
            );
        }
    }

    public void runProjectUpdate(NutsCommandLine cmd) {
        NProject t = null;
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            if (a.getStringKey().equals("on")) {
                if (t == null) {
                    cmd.throwError("project name expected");
                }
                t.setStartTime(new TimeParser().parseInstant(cmd.next().toString()));
            } else if (a.getStringKey().equals("at")) {
                if (t == null) {
                    cmd.throwError("project name expected");
                }
                t.setStartTime(new TimeParser().setTimeOnly(true).parseInstant(cmd.next().toString()));
            } else if (a.getStringKey().equals("for")) {
                if (t == null) {
                    cmd.throwError("project name expected");
                }
                t.setCustomer(cmd.next().toString());
            } else if (a.getStringKey().equals("via")) {
                if (t == null) {
                    cmd.throwError("project name expected");
                }
                t.setCompany(cmd.next().toString());
            } else if (a.getStringKey().equals("week")) {
                if (t == null) {
                    cmd.throwError("project name expected");
                }
                t.setStartWeekDay(NDay.valueOf(cmd.nextString().toString().toUpperCase()));
            } else if (a.getStringKey().equals("obs")) {
                if (t == null) {
                    cmd.throwError("project name expected");
                }
                t.setObservations(cmd.nextString().toString());
            } else {
                t = service.getProject(a.toString());
                if (t == null) {
                    cmd.throwError("project not found: " + a.toString());
                }
            }
        }
        if (t == null) {
            cmd.throwError("project name expected");
        }
        service.saveProject(t);
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("<<%s>> project updated.\n",
                    t.getName()
            );
        }
    }

    public boolean runCommands(NutsCommandLine cmd) {
        if (runProjectCommands(cmd)) {
            return true;
        }
        if (runTaskCommands(cmd)) {
            return true;
        }
        return false;
    }

    public boolean runTaskCommands(NutsCommandLine cmd) {
        if (cmd.next("a") != null || cmd.next("add task") != null || cmd.next("add") != null) {
            runTaskAdd(cmd);
            return true;
        } else if (cmd.next("l") != null || cmd.next("list tasks") != null || cmd.next("list") != null) {
            runTaskList(cmd);
            return true;
        } else if (cmd.next("r") != null || cmd.next("rm") != null || cmd.next("remove task") != null || cmd.next("remove tasks") != null || cmd.next("remove") != null) {
            runTaskRemove(cmd);
            return true;
        } else if (cmd.next("d") != null || cmd.next("show task") != null || cmd.next("show tasks") != null || cmd.next("show") != null) {
            runTaskShow(cmd);
            return true;
        } else if (cmd.next("t") != null || cmd.next("tasks")!=null) {
            runTaskList(cmd);
            return true;
        }else{
            runTaskList(cmd);
            return true;
        }
    }

    public boolean runProjectCommands(NutsCommandLine cmd) {
        if (cmd.next("ap") != null || cmd.next("add project") != null) {
            runProjectAdd(cmd);
            return true;
        } else if (cmd.next("up") != null || cmd.next("update project") != null) {
            runProjectUpdate(cmd);
            return true;
        } else if (cmd.next("lp") != null || cmd.next("list projects") != null) {
            runProjectList(cmd);
            return true;
        } else if (cmd.next("rp") != null || cmd.next("rmp") != null
                || cmd.next("remove project") != null
                || cmd.next("remove projects") != null
                || cmd.next("rm project") != null
                || cmd.next("rm projects") != null
        ) {
            runProjectRemove(cmd);
            return true;
        } else if (cmd.next("dp") != null || cmd.next("show project") != null || cmd.next("show projects") != null) {
            runProjectShow(cmd);
            return true;
        } else if (cmd.next("p") != null || cmd.next("projects")!=null) {
            runProjectList(cmd);
            return true;
        }
        return false;
    }

    private void runTaskRemove(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            if (service.removeTask(a.toString())) {
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("<<%s>> task removed.\n",
                            a.toString()
                    );
                }
            }
        }

    }

    private void runProjectRemove(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            if (service.removeProject(a.toString())) {
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("<<%s>> project removed.\n",
                            a.toString()
                    );
                }
            }
        }

    }

    private void runTaskShow(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            NTask task = service.getTask(a.toString());
            if (task == null) {
                context.getSession().out().printf("<<%s>>: @@not found@@.\n",
                        a.toString()
                );
            } else {
                context.getSession().out().printf("<<%s>>:\n",
                        a.toString()
                );
                context.getSession().out().printf("\t==task name==     : %s:\n", task.getName());
                context.getSession().out().printf("\t==project==       : %s:\n", task.getProject());
                context.getSession().out().printf("\t==duration==      : %s:\n", task.getDuration());
                context.getSession().out().printf("\t==start time==    : %s:\n", task.getStartTime());
                context.getSession().out().printf("\t==duration extra==: %s:\n", task.getInternalDuration());
                context.getSession().out().printf("\t==observations==  : %s:\n", task.getObservations());
            }
        }

    }

    private void runProjectShow(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            NProject task = service.getProject(a.toString());
            if (task == null) {
                context.getSession().out().printf("<<%s>>: @@not found@@.\n",
                        a.toString()
                );
            } else {
                context.getSession().out().printf("<<%s>>:\n",
                        a.toString()
                );
                context.getSession().out().printf("\t==project name==  : %s:\n", task.getName());
                context.getSession().out().printf("\t==customer==      : %s:\n", task.getCustomer());
                context.getSession().out().printf("\t==company==       : %s:\n", task.getCompany());
                context.getSession().out().printf("\t==start time==    : %s:\n", task.getStartTime());
                context.getSession().out().printf("\t==start week day==: %s:\n", task.getStartWeekDay());
                context.getSession().out().printf("\t==observations==  : %s:\n", task.getObservations());
            }
        }

    }

    private void runTaskList(NutsCommandLine cmd) {
        double hoursPerDay = 8;
        int count = 100;
        String groupBy = null;
        ChronoUnit countType = null;
        TimeUnit timeUnit = null;
        Predicate<NTask> whereFilter = null;
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            switch (a.getStringKey()) {
                case "-w": {
                    countType = ChronoUnit.WEEKS;
                    count = cmd.nextString().getArgumentValue().getInt();
                    break;
                }
                case "-m": {
                    countType = ChronoUnit.MONTHS;
                    count = cmd.nextString().getArgumentValue().getInt();
                    break;
                }
                case "-l": {
                    countType = null;
                    count = cmd.nextString().getArgumentValue().getInt();
                    break;
                }
                case "-u":
                case "--unit": {
                    timeUnit = NTimePeriod.parseUnit(cmd.nextString().getStringValue(), false);
                    break;
                }
                case "-g":
                case "--group":
                case "--groupBy":
                case "--groupby":
                case "--group-by": {
                    NutsArgument y = cmd.nextString();
                    switch (y.getStringValue()) {
                        case "p":
                        case "project": {
                            groupBy = "project";
                            break;
                        }
                        case "n":
                        case "name": {
                            groupBy = "name";
                            break;
                        }
                        case "s":
                        case "summary": {
                            groupBy = "summary";
                            break;
                        }
                        default: {
                            cmd.pushBack(y).unexpectedArgument("unvalid value");
                        }
                    }
                    break;
                }
                case "-p": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NTask> t = x -> sp.test(x.getProject());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-n": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NTask> t = x -> sp.test(x.getName());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-c": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NTask> t = x -> {
                        NProject project = service.getProject(x.getProject());
                        return sp.test(project == null ? "" : project.getCustomer());
                    };
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-C": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NTask> t = x -> {
                        NProject project = service.getProject(x.getProject());
                        return sp.test(project == null ? "" : project.getCompany());
                    };
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-d": {
                    String s = cmd.nextString().getStringValue();
                    if (s.startsWith(">=")) {
                        NTimePeriod p = NTimePeriod.parse(s.substring(2), false);
                        Predicate<NTask> t = x -> x.getDuration() != null && x.getDuration().compareTo(p) >= 0;
                        whereFilter = appendPredicate(whereFilter, t);
                    } else if (s.startsWith(">")) {
                        NTimePeriod p = NTimePeriod.parse(s.substring(1), false);
                        Predicate<NTask> t = x -> x.getDuration() != null && x.getDuration().compareTo(p) > 0;
                        whereFilter = appendPredicate(whereFilter, t);
                    } else if (s.startsWith("<=")) {
                        NTimePeriod p = NTimePeriod.parse(s.substring(2), false);
                        Predicate<NTask> t = x -> x.getDuration() != null && x.getDuration().compareTo(p) <= 0;
                        whereFilter = appendPredicate(whereFilter, t);
                    } else if (s.startsWith("<")) {
                        NTimePeriod p = NTimePeriod.parse(s.substring(1), false);
                        Predicate<NTask> t = x -> x.getDuration() != null && x.getDuration().compareTo(p) < 0;
                        whereFilter = appendPredicate(whereFilter, t);
                    } else {
                        NTimePeriod p = NTimePeriod.parse(s, false);
                        Predicate<NTask> t = x -> x.getDuration() != null && x.getDuration().compareTo(p) == 0;
                        whereFilter = appendPredicate(whereFilter, t);
                    }
                    break;
                }
                case "-t": {
                    String s = cmd.nextString().getStringValue();
                    if (s.startsWith(">=")) {
                        Instant p = new TimeParser().parseInstant(s.substring(2));
                        if (p != null) {
                            Predicate<NTask> t = x -> x.getStartTime() != null && x.getStartTime().compareTo(p) >= 0;
                            whereFilter = appendPredicate(whereFilter, t);
                        } else {
                            NTimePeriod p0 = NTimePeriod.parse(s.substring(1), false);
                            Calendar instance = Calendar.getInstance();
                            instance.add(Calendar.MINUTE, (int) p0.toUnit(TimeUnit.MINUTES, 24).getCount());
                            Instant p1 = instance.toInstant();
                            Predicate<NTask> t = x -> x.getStartTime() != null && x.getStartTime().compareTo(p1) >= 0;
                            whereFilter = appendPredicate(whereFilter, t);
                        }
                    } else if (s.startsWith(">")) {
                        Instant p = new TimeParser().parseInstant(s.substring(1));
                        if (p != null) {
                            Predicate<NTask> t = x -> x.getStartTime() != null && x.getStartTime().compareTo(p) > 0;
                            whereFilter = appendPredicate(whereFilter, t);
                        } else {
                            NTimePeriod p0 = NTimePeriod.parse(s.substring(1), false);
                            Calendar instance = Calendar.getInstance();
                            instance.add(Calendar.MINUTE, (int) p0.toUnit(TimeUnit.MINUTES, 24).getCount());
                            Instant p1 = instance.toInstant();
                            Predicate<NTask> t = x -> x.getStartTime() != null && x.getStartTime().compareTo(p1) > 0;
                            whereFilter = appendPredicate(whereFilter, t);
                        }
                    } else if (s.startsWith("<=")) {
                        Instant p = new TimeParser().parseInstant(s.substring(2));
                        if (p != null) {
                            Predicate<NTask> t = x -> x.getStartTime() != null && x.getStartTime().compareTo(p) <= 0;
                            whereFilter = appendPredicate(whereFilter, t);
                        } else {
                            NTimePeriod p0 = NTimePeriod.parse(s.substring(1), false);
                            Calendar instance = Calendar.getInstance();
                            instance.add(Calendar.MINUTE, (int) p0.toUnit(TimeUnit.MINUTES, 24).getCount());
                            Instant p1 = instance.toInstant();
                            Predicate<NTask> t = x -> x.getStartTime() != null && x.getStartTime().compareTo(p1) <= 0;
                            whereFilter = appendPredicate(whereFilter, t);
                        }
                    } else if (s.startsWith("<")) {
                        Instant p = new TimeParser().parseInstant(s.substring(1));
                        if (p != null) {
                            Predicate<NTask> t = x -> x.getStartTime() != null && x.getStartTime().compareTo(p) < 0;
                            whereFilter = appendPredicate(whereFilter, t);
                        } else {
                            NTimePeriod p0 = NTimePeriod.parse(s.substring(1), false);
                            Calendar instance = Calendar.getInstance();
                            instance.add(Calendar.MINUTE, (int) p0.toUnit(TimeUnit.MINUTES, 24).getCount());
                            Instant p1 = instance.toInstant();
                            Predicate<NTask> t = x -> x.getStartTime() != null && x.getStartTime().compareTo(p1) < 0;
                            whereFilter = appendPredicate(whereFilter, t);
                        }
                    } else {
                        Instant p = new TimeParser().parseInstant(s);
                        if (p != null) {
                            Predicate<NTask> t = x -> x.getStartTime() != null && x.getStartTime().compareTo(p) == 0;
                            whereFilter = appendPredicate(whereFilter, t);
                        } else {
                            NTimePeriod p0 = NTimePeriod.parse(s.substring(1), false);
                            Calendar instance = Calendar.getInstance();
                            instance.add(Calendar.MINUTE, (int) p0.toUnit(TimeUnit.MINUTES, 24).getCount());
                            Instant p1 = instance.toInstant();
                            Predicate<NTask> t = x -> x.getStartTime() != null && x.getStartTime().compareTo(p1) == 0;
                            whereFilter = appendPredicate(whereFilter, t);
                        }
                    }
                    break;
                }
                default: {
                    cmd.unexpectedArgument();
                }
            }
        }
        Stream<NTask> r =
                (countType != null) ? service.findLastTasks(null, count, countType).filter(whereFilter == null ? x -> true : whereFilter)
                        : service.findLastTasks(null, -1, null).filter(whereFilter == null ? x -> true : whereFilter).limit(count);
        TimeUnit timeUnit0 = timeUnit;
        double hoursPerDay0 = hoursPerDay;
        if (groupBy != null) {
            r = r.collect(Collectors.groupingBy(
                    ("project".equals(groupBy)) ? x -> x.getProject() :
                            ("name".equals(groupBy)) ? x -> x.getProject() + ":" + x.getName() :
                                    ("summary".equals(groupBy)) ? x -> "summary" :
                                            x -> x.getId()

            ))
                    .entrySet().stream().map(x -> {
                        List<NTask> value = x.getValue();
                        NTask t = new NTask();
                        NTimePeriods tp = new NTimePeriods();
                        TreeSet<TimeUnit> atu = new TreeSet<TimeUnit>();
                        TreeSet<String> names = new TreeSet<String>();
                        TreeSet<String> projects = new TreeSet<String>();
                        for (NTask nTask : value) {
                            tp.add(nTask.getDuration());
                            t.setStartTime(nTask.getStartTime());
                            atu.add(nTask.getDuration().getUnit());
                            names.add(nTask.getName());
                            projects.add(nTask.getProject());
                        }
                        t.setProject(projects.size() == 0 ? "" : projects.size() == 1 ? projects.toArray()[0].toString() :
                                (projects.size() <= 3 || String.join(",", projects).length() < 20) ? String.join(",", projects) :
                                        (String.valueOf(projects.size()) + " projects")
                        );
                        TimeUnit[] atu0 = atu.toArray(new TimeUnit[0]);
                        String tasks = " Task" + ((value.size() == 1) ? "" : "s");
                        String named = (names.size() == 0) ? "" : (names.size() == 1) ? (" named " + names.toArray()[0]) : (" with " + (names.size()) + " different names");
                        t.setName(value.size() + tasks + named);
                        t.setDuration(tp.toUnit(timeUnit0 != null ? timeUnit0 : (atu0.length == 0 ? TimeUnit.DAYS : atu0[0]), hoursPerDay0));
                        t.setId(UUID.randomUUID().toString());
                        return t;
                    });
            if (context.getSession().isPlainTrace()) {
                r.forEach(x -> {
                    context.getSession().out().printf("starting ##%s## during ##%s## [==%s==] %s\n",
                            x.getStartTime(),
                            timeUnit0 == null ? x.getDuration() : x.getDuration().toUnit(timeUnit0, hoursPerDay),
                            x.getProject() == null ? "*" : x.getProject(),
                            x.getName()
                    );
                });
            } else {
                context.getSession().formatObject(r.collect(Collectors.toList())).print(context.getSession().out());
            }
        } else {
            if (context.getSession().isPlainTrace()) {
                r.forEach(x -> {
                    context.getSession().out().printf("<<%s>> at ##%s## [[%s]] during ##%s## [==%s==] %s\n",
                            x.getId(),
                            x.getStartTime(),
                            x.getStartTime().atZone(ZoneId.systemDefault()).getDayOfWeek().toString().toLowerCase().substring(0,3),
                            timeUnit0 == null ? x.getDuration() : x.getDuration().toUnit(timeUnit0, hoursPerDay),
                            x.getProject() == null ? "*" : x.getProject(),
                            x.getName()
                    );
                });
            } else {
                context.getSession().formatObject(r.collect(Collectors.toList())).print(context.getSession().out());
            }
        }
    }

    private void runProjectList(NutsCommandLine cmd) {
        Predicate<NProject> whereFilter = null;
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            switch (a.getStringKey()) {
                case "-c": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NProject> t = x -> sp.test(x.getCustomer());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-C": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NProject> t = x -> sp.test(x.getCompany());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-n": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NProject> t = x -> sp.test(x.getName());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-t": {
                    String s = cmd.nextString().getStringValue();
                    if (s.startsWith(">=")) {
                        Instant p = new TimeParser().parseInstant(s.substring(2));
                        if (p != null) {
                            Predicate<NProject> t = x -> x.getStartTime() != null && x.getStartTime().compareTo(p) >= 0;
                            whereFilter = appendPredicate(whereFilter, t);
                        } else {
                            NTimePeriod p0 = NTimePeriod.parse(s.substring(1), false);
                            Calendar instance = Calendar.getInstance();
                            instance.add(Calendar.MINUTE, (int) p0.toUnit(TimeUnit.MINUTES, 24).getCount());
                            Instant p1 = instance.toInstant();
                            Predicate<NProject> t = x -> x.getStartTime() != null && x.getStartTime().compareTo(p1) >= 0;
                            whereFilter = appendPredicate(whereFilter, t);
                        }
                    } else if (s.startsWith(">")) {
                        Instant p = new TimeParser().parseInstant(s.substring(1));
                        if (p != null) {
                            Predicate<NProject> t = x -> x.getStartTime() != null && x.getStartTime().compareTo(p) > 0;
                            whereFilter = appendPredicate(whereFilter, t);
                        } else {
                            NTimePeriod p0 = NTimePeriod.parse(s.substring(1), false);
                            Calendar instance = Calendar.getInstance();
                            instance.add(Calendar.MINUTE, (int) p0.toUnit(TimeUnit.MINUTES, 24).getCount());
                            Instant p1 = instance.toInstant();
                            Predicate<NProject> t = x -> x.getStartTime() != null && x.getStartTime().compareTo(p1) > 0;
                            whereFilter = appendPredicate(whereFilter, t);
                        }
                    } else if (s.startsWith("<=")) {
                        Instant p = new TimeParser().parseInstant(s.substring(2));
                        if (p != null) {
                            Predicate<NProject> t = x -> x.getStartTime() != null && x.getStartTime().compareTo(p) <= 0;
                            whereFilter = appendPredicate(whereFilter, t);
                        } else {
                            NTimePeriod p0 = NTimePeriod.parse(s.substring(1), false);
                            Calendar instance = Calendar.getInstance();
                            instance.add(Calendar.MINUTE, (int) p0.toUnit(TimeUnit.MINUTES, 24).getCount());
                            Instant p1 = instance.toInstant();
                            Predicate<NProject> t = x -> x.getStartTime() != null && x.getStartTime().compareTo(p1) <= 0;
                            whereFilter = appendPredicate(whereFilter, t);
                        }
                    } else if (s.startsWith("<")) {
                        Instant p = new TimeParser().parseInstant(s.substring(1));
                        if (p != null) {
                            Predicate<NProject> t = x -> x.getStartTime() != null && x.getStartTime().compareTo(p) < 0;
                            whereFilter = appendPredicate(whereFilter, t);
                        } else {
                            NTimePeriod p0 = NTimePeriod.parse(s.substring(1), false);
                            Calendar instance = Calendar.getInstance();
                            instance.add(Calendar.MINUTE, (int) p0.toUnit(TimeUnit.MINUTES, 24).getCount());
                            Instant p1 = instance.toInstant();
                            Predicate<NProject> t = x -> x.getStartTime() != null && x.getStartTime().compareTo(p1) < 0;
                            whereFilter = appendPredicate(whereFilter, t);
                        }
                    } else {
                        Instant p = new TimeParser().parseInstant(s);
                        if (p != null) {
                            Predicate<NProject> t = x -> x.getStartTime() != null && x.getStartTime().compareTo(p) == 0;
                            whereFilter = appendPredicate(whereFilter, t);
                        } else {
                            NTimePeriod p0 = NTimePeriod.parse(s.substring(1), false);
                            Calendar instance = Calendar.getInstance();
                            instance.add(Calendar.MINUTE, (int) p0.toUnit(TimeUnit.MINUTES, 24).getCount());
                            Instant p1 = instance.toInstant();
                            Predicate<NProject> t = x -> x.getStartTime() != null && x.getStartTime().compareTo(p1) == 0;
                            whereFilter = appendPredicate(whereFilter, t);
                        }
                    }
                    break;
                }
                default: {
                    cmd.unexpectedArgument();
                }
            }
        }
        Stream<NProject> r =
                service.findProjects().filter(whereFilter == null ? x -> true : whereFilter)
                        .sorted(Comparator.comparing(NProject::getName));

        if (context.getSession().isPlainTrace()) {
            r.forEach(x -> {
                context.getSession().out().printf("##%s## [==%s==] [==%s==] %s\n",
                        x.getStartTime(),
                        x.getCompany(),
                        x.getCustomer(),
                        x.getName()
                );
            });
        } else {
            context.getSession().formatObject(r.collect(Collectors.toList())).print(context.getSession().out());
        }
    }

    private <T> Predicate<T> appendPredicate(Predicate<T> whereFilter, Predicate<T> t) {
        if (whereFilter == null) {
            whereFilter = t;
        } else {
            whereFilter = whereFilter.and(t);
        }
        return whereFilter;
    }

    private Predicate<String> createStringFilter(String s) {
        if (s.length() > 0 && s.startsWith("/") && s.endsWith("/")) {
            Pattern pattern = Pattern.compile(s);
            return x -> pattern.matcher(x == null ? "" : x).matches();
        }
        if (s.length() > 0 && s.contains("*")) {
            Pattern pattern = Pattern.compile(TaskService.wildcardToRegex(s));
            return x -> pattern.matcher(x == null ? "" : x).matches();
        }
        return x -> s.equals(x == null ? "" : x);
    }
}

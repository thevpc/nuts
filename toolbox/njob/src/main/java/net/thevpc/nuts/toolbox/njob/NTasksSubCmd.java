package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.njob.model.*;
import net.thevpc.nuts.toolbox.njob.time.TimeParser;
import net.thevpc.nuts.toolbox.njob.time.TimePeriod;
import net.thevpc.nuts.toolbox.njob.time.TimespanPattern;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NTasksSubCmd {
    private JobService service;
    private NutsApplicationContext context;
    private NutsWorkspace ws;
    private JobServiceCmd parent;

    public NTasksSubCmd(JobServiceCmd parent) {
        this.parent = parent;
        this.context = parent.context;
        this.service = parent.service;
        this.ws = parent.ws;
    }



    public void runTaskAdd(NutsCommandLine cmd) {
        boolean list = false;
        boolean show = false;
        NTask t = new NTask();
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            switch (a.getStringKey()) {
                case "--list":
                case "-l": {
                    list = cmd.nextBoolean().getBooleanValue();
                    break;
                }
                case "--show":
                case "-s": {
                    show = cmd.nextBoolean().getBooleanValue();
                    break;
                }
                case "--on":
                case "--due":
                case "-t": {
                    t.setDueTime(new TimeParser().parseInstant(cmd.nextString().getStringValue(), false));
                    break;
                }
                case "--at": {
                    t.setDueTime(new TimeParser().setTimeOnly(true).parseInstant(cmd.nextString().getStringValue(), false));
                    break;
                }
                case "--start": {
                    t.setStartTime(new TimeParser().parseInstant(cmd.nextString().getStringValue(), false));
                    break;
                }
                case "--end": {
                    t.setEndTime(new TimeParser().parseInstant(cmd.nextString().getStringValue(), false));
                    break;
                }
                case "--for": {
                    String v = cmd.nextString().getStringValue();
                    Instant u = new TimeParser().parseInstant(v, true);
                    if (u != null) {
                        t.setDueTime(u);
                    } else {
                        t.setProject(v);
                    }
                    break;
                }
                case "--project":
                case "-p": {
                    t.setProject(cmd.nextString().getStringValue());
                    break;
                }
                case "--name":
                case "-n": {
                    t.setName(cmd.nextString().getStringValue());
                    break;
                }
                case "--flag":
                case "-f": {
                    String v = cmd.nextString().getStringValue();
                    NFlag f = null;
                    if ("random".equalsIgnoreCase(v)) {
                        f = NFlag.values()[(int) (Math.random() * NFlag.values().length)];
                    } else {
                        f = NFlag.valueOf(v.toUpperCase());
                    }
                    t.setFlag(f);
                    break;
                }
                case "--job":
                case "-j": {
                    String jobId = cmd.nextString().getStringValue();
                    NJob job = service.jobs().getJob(jobId);
                    if (job == null) {
                        cmd.throwError("invalid job " + jobId);
                    }
                    t.setJobId(job.getId());
                    break;
                }
                case "--parent":
                case "-T": {
                    String taskId = cmd.nextString().getStringValue();
                    NTask parentTask = service.tasks().getTask(taskId);
                    if (parentTask == null) {
                        cmd.throwError("invalid parent task " + taskId);
                    }
                    t.setParentTaskId(parentTask.getId());
                    break;
                }
                case "--priority":
                case "-P": {
                    String v = cmd.nextString().getStringValue();
                    NPriority p = NPriority.NORMAL;
                    if (v.equalsIgnoreCase("higher")) {
                        p = p.higher();
                    } else if (v.equalsIgnoreCase("lower")) {
                        p = p.lower();
                    } else {
                        p = NPriority.valueOf(v.toLowerCase());
                    }
                    t.setPriority(p);
                    break;
                }
                case "--ob":
                case "-o": {
                    t.setObservations(cmd.nextString().getStringValue());
                    break;
                }
                case "--duration":
                case "-d": {
                    t.setDuration(TimePeriod.parse(cmd.nextString().getStringValue(), true));
                    break;
                }
                case "--wip": {
                    cmd.skip();
                    t.setStatus(NTaskStatus.WIP);
                    break;
                }
                case "--done": {
                    cmd.skip();
                    t.setStatus(NTaskStatus.DONE);
                    break;
                }
                case "--cancel": {
                    cmd.skip();
                    t.setStatus(NTaskStatus.CANCELLED);
                    break;
                }
                case "--todo": {
                    cmd.skip();
                    t.setStatus(NTaskStatus.TODO);
                    break;
                }
                case "--high": {
                    cmd.skip();
                    t.setPriority(NPriority.HIGH);
                    break;
                }
                case "--critical": {
                    cmd.skip();
                    t.setPriority(NPriority.CRITICAL);
                    break;
                }
                case "--normal": {
                    cmd.skip();
                    t.setPriority(NPriority.NORMAL);
                    break;
                }
                default: {
                    if (a.isNonOption()) {
                        if (t.getName() == null) {
                            t.setName(cmd.next().toString());
                        } else {
                            cmd.unexpectedArgument();
                        }
                    } else {
                        cmd.unexpectedArgument();
                    }
                }
            }
        }
        service.tasks().addTask(t);
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("task %s (%s) added.\n",
                    context.getWorkspace().formats().text().styled(t.getId(), NutsTextNodeStyle.primary(5)),
                    t.getName()
            );
        }
        if (show) {
            runTaskShow(ws.commandLine().create(t.getId()));
        }
        if (list) {
            runTaskList(ws.commandLine().create());
        }
    }


    public void runTaskUpdate(NutsCommandLine cmd) {
        List<NTask> tasks = new ArrayList<>();
        boolean list = false;
        boolean show = false;
        List<Consumer<NTask>> runLater = new ArrayList<>();
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            switch (a.getStringKey()) {
                case "--list":
                case "-l": {
                    list = cmd.nextBoolean().getBooleanValue();
                    break;
                }
                case "--show":
                case "-s": {
                    show = cmd.nextBoolean().getBooleanValue();
                    break;
                }
                case "--start": {
                    Instant v = new TimeParser().parseInstant(cmd.nextString().getStringValue(), false);
                    runLater.add(t -> t.setStartTime(v));
                    break;
                }
                case "-t":
                case "--on":
                case "--due": {
                    String v = cmd.nextString().getStringValue();
                    runLater.add(t -> t.setDueTime(TimePeriod.parseOpPeriodAsInstant(v, t.getDueTime(), true)));
                    break;
                }
                case "--at": {
                    Instant v = new TimeParser().setTimeOnly(true).parseInstant(cmd.nextString().getStringValue(), false);
                    runLater.add(t -> t.setDueTime(v));
                    break;
                }
                case "--end": {
                    Instant v = new TimeParser().parseInstant(cmd.nextString().getStringValue(), false);
                    runLater.add(t -> t.setEndTime(v));
                    break;
                }
                case "--wip": {
                    cmd.skip();
                    runLater.add(t -> t.setStatus(NTaskStatus.WIP));
                    break;
                }
                case "--done": {
                    cmd.skip();
                    runLater.add(t -> t.setStatus(NTaskStatus.DONE));
                    break;
                }
                case "--cancel": {
                    cmd.skip();
                    runLater.add(t -> t.setStatus(NTaskStatus.CANCELLED));
                    break;
                }
                case "--todo": {
                    cmd.skip();
                    runLater.add(t -> t.setStatus(NTaskStatus.TODO));
                    break;
                }
                case "--high": {
                    cmd.skip();
                    runLater.add(t -> t.setPriority(NPriority.HIGH));
                    break;
                }
                case "--critical": {
                    cmd.skip();
                    runLater.add(t -> t.setPriority(NPriority.CRITICAL));
                    break;
                }
                case "--normal": {
                    cmd.skip();
                    runLater.add(t -> t.setPriority(NPriority.NORMAL));
                    break;
                }
                case "++P":
                case "++prio":
                case "--prio++": {
                    cmd.skip();
                    runLater.add(t -> t.setPriority((t.getPriority() == null ? NPriority.NORMAL : t.getPriority()).higher()));
                    break;
                }
                case "--P":
                case "--prio":
                case "--prio--": {
                    a = cmd.next();
                    String v = a.getStringValue();
                    if (!a.getStringKey().equals("--prio")) {
                        v = null;
                    }
                    if (v == null) {
                        runLater.add(t -> t.setPriority((t.getPriority() == null ? NPriority.NORMAL : t.getPriority()).higher()));
                    } else {
                        NPriority pp = NPriority.parse(v);
                        runLater.add(t -> t.setPriority(pp));
                    }
                    break;
                }
                case "--status": {
                    NTaskStatus v = NTaskStatus.parse(cmd.nextString().getStringValue());
                    runLater.add(t -> t.setStatus(v));
                    break;
                }
                case "-d":
                case "--duration": {
                    TimePeriod v = TimePeriod.parse(cmd.nextString().getStringValue(), false);
                    runLater.add(t -> t.setDuration(v));
                    break;
                }
                case "-n":
                case "--name": {
                    String v = cmd.nextString().getStringValue();
                    runLater.add(t -> t.setName(v));
                    break;
                }
                case "-f":
                case "--flag": {
                    String v = cmd.nextString().getStringValue();
                    NFlag f = NFlag.parse(v);
                    runLater.add(t -> t.setFlag(f));
                    break;
                }
                case "-j":
                case "--job": {
                    String jobId = cmd.nextString().getStringValue();
                    NJob job = service.jobs().getJob(jobId);
                    if (job == null) {
                        cmd.throwError("invalid job " + jobId);
                    }
                    runLater.add(t -> t.setJobId(job.getId()));
                    break;
                }
                case "-T":
                case "--parent": {
                    String taskId = cmd.nextString().getStringValue();
                    NTask parentTask = service.tasks().getTask(taskId);
                    if (parentTask == null) {
                        cmd.throwError("invalid parent task " + taskId);
                    }
                    runLater.add(t -> t.setParentTaskId(parentTask.getId()));
                    break;
                }
                case "-P":
                case "--priority": {
                    String v = cmd.nextString().getStringValue();
                    runLater.add(t -> {
                        NPriority p = t.getPriority();
                        if (v.equalsIgnoreCase("higher")) {
                            p = p.higher();
                        } else if (v.equalsIgnoreCase("lower")) {
                            p = p.lower();
                        } else {
                            p = NPriority.parse(v);
                        }
                        t.setPriority(p);
                    });
                    break;
                }
                case "--for": {
                    String v = cmd.nextString().getStringValue();
                    runLater.add(t -> {
                        Instant u = TimePeriod.parseOpPeriodAsInstant(v, t.getDueTime(), true);
                        if (u != null) {
                            t.setDueTime(u);
                        } else {
                            t.setProject(v);
                        }
                    });
                    break;
                }
                case "-p":
                case "--project": {
                    String v = cmd.nextString().getStringValue();
                    runLater.add(t -> t.setProject(v));
                    break;
                }
                case "-o":
                case "--obs": {
                    String v = cmd.nextString().getStringValue();
                    runLater.add(t -> t.setObservations(v));
                    break;
                }
                case "-o+":
                case "--obs+":
                case "+obs": {
                    String v = cmd.nextString().getStringValue();
                    runLater.add(t -> {
                        String s = t.getObservations();
                        if (s == null) {
                            s = "";
                        }
                        s = s.trim();
                        if (!s.isEmpty()) {
                            s += "\n";
                        }
                        s += v;
                        s = s.trim();
                        t.setObservations(s);
                    });
                    break;
                }
                default: {
                    if (a.isNonOption()) {
                        String pid = cmd.next().toString();
                        NTask t = findTask(pid, cmd);
                        tasks.add(t);
                    } else {
                        cmd.unexpectedArgument();
                    }
                }
            }
        }
        if (tasks.isEmpty()) {
            cmd.throwError("task id expected");
        }
        for (NTask task : tasks) {
            for (Consumer<NTask> c : runLater) {
                c.accept(task);
            }
        }
        NutsFormatManager text = context.getWorkspace().formats();
        for (NTask task : new LinkedHashSet<>(tasks)) {
            service.tasks().updateTask(task);
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().printf("task %s (%s) updated.\n",
                        text.text().styled(task.getId(), NutsTextNodeStyle.primary(5)),
                        text.text().styled(task.getName(), NutsTextNodeStyle.primary(1))
                );
            }
        }
        if (show) {
            for (NTask t : new LinkedHashSet<>(tasks)) {
                runTaskList(ws.commandLine().create(t.getId()));
            }
        }
        if (list) {
            runTaskList(ws.commandLine().create());
        }
    }


    private void runTaskList(NutsCommandLine cmd) {
        TimespanPattern hoursPerDay = TimespanPattern.WORK;
        int count = 100;
        NJobGroup groupBy = null;
        ChronoUnit countType = null;
        ChronoUnit timeUnit = null;
        Predicate<NTask> whereFilter = null;
        NTaskStatusFilter status = null;
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            switch (a.getStringKey()) {
                case "-w":
                case "--weeks": {
                    countType = ChronoUnit.WEEKS;
                    count = cmd.nextString().getIntValue();
                    break;
                }
                case "-m":
                case "--months": {
                    countType = ChronoUnit.MONTHS;
                    count = cmd.nextString().getIntValue();
                    break;
                }
                case "-l": {
                    countType = null;
                    count = cmd.nextString().getIntValue();
                    break;
                }
                case "-u":
                case "--unit": {
                    timeUnit = TimePeriod.parseUnit(cmd.nextString().getStringValue(), false);
                    break;
                }
                case "--todo": {
                    cmd.nextString();
                    status = NTaskStatusFilter.TODO;
                    break;
                }
                case "-a":
                case "--all": {
                    cmd.nextString();
                    status = NTaskStatusFilter.ALL;
                    break;
                }
                case "-r":
                case "--recent": {
                    cmd.nextString();
                    status = NTaskStatusFilter.RECENT;
                    break;
                }
                case "--cancelled": {
                    cmd.nextString();
                    status = NTaskStatusFilter.CANCELLED;
                    break;
                }
                case "--closed": {
                    cmd.nextString();
                    status = NTaskStatusFilter.CLOSED;
                    break;
                }
                case "--wip": {
                    cmd.nextString();
                    status = NTaskStatusFilter.WIP;
                    break;
                }
                case "-o":
                case "--open": {
                    cmd.nextString();
                    status = NTaskStatusFilter.OPEN;
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
                            groupBy = NJobGroup.PROJECT_NAME;
                            break;
                        }
                        case "n":
                        case "name": {
                            groupBy = NJobGroup.NAME;
                            break;
                        }
                        case "s":
                        case "summary": {
                            groupBy = NJobGroup.SUMMARY;
                            break;
                        }
                        default: {
                            cmd.pushBack(y).unexpectedArgument("invalid value");
                        }
                    }
                    break;
                }
                case "--project":
                case "-p": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = parent.createProjectFilter(s);
                    Predicate<NTask> t = x -> sp.test(x.getProject());
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "-n":
                case "--name": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = parent.createStringFilter(s);
                    Predicate<NTask> t = x -> sp.test(x.getName());
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "-b":
                case "--beneficiary": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = parent.createStringFilter(s);
                    Predicate<NTask> t = x -> {
                        NProject project = service.projects().getProject(x.getProject());
                        return sp.test(project == null ? "" : project.getBeneficiary());
                    };
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "-c":
                case "--company": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = parent.createStringFilter(s);
                    Predicate<NTask> t = x -> {
                        NProject project = service.projects().getProject(x.getProject());
                        return sp.test(project == null ? "" : project.getCompany());
                    };
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "-d":
                case "--duration": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<TimePeriod> p = TimePeriod.parseFilter(s, false);
                    Predicate<NTask> t = x -> p.test(x.getDuration());
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "-t":
                case "--startTime":
                case "--start-time": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<Instant> t = new TimeParser().parseInstantFilter(s, false);
                    whereFilter = parent.appendPredicate(whereFilter, x -> t.test(x.getStartTime()));
                    break;
                }
                default: {
                    cmd.unexpectedArgument();
                }
            }
        }
        Stream<NTask> r = service.tasks().findTasks(status, null, count, countType, whereFilter, groupBy, timeUnit, hoursPerDay);

        if (context.getSession().isPlainTrace()) {
            NutsMutableTableModel m = ws.formats().table().createModel();
            List<NTask> lastResults = new ArrayList<>();
            int[] index = new int[1];
            r.forEach(x -> {
                index[0]++;
                m.newRow().addCells(toTaskRowArray(x,
                        parent.createHashId(index[0], -1)
                ));
                lastResults.add(x);
            });
            context.getSession().setProperty("LastResults", lastResults.toArray(new NTask[0]));
            ws.formats().table()
                    .setBorder("spaces")
                    .setModel(m).setSession(context.getSession()).println();
        } else {
            context.getSession().formatObject(r.collect(Collectors.toList())).print(context.getSession().out());
        }
    }

    private Object[] toTaskRowArray(NTask x, String index) {
        String project = x.getProject();
        NProject p = project == null ? null : service.projects().getProject(project);
        NTaskStatus s = x.getStatus();
        String dte0 = parent.getFormattedDate(x.getDueTime());
        NutsTextNodeBuilder dte = ws.formats().text().builder();
        if (s == NTaskStatus.CANCELLED || s == NTaskStatus.DONE) {
            dte.append(dte0, NutsTextNodeStyle.pale());
        } else if (x.getDueTime() != null && x.getDueTime().compareTo(Instant.now()) < 0) {
            dte.append(dte0, NutsTextNodeStyle.error());
        } else {
            dte.append(dte0, NutsTextNodeStyle.keyword(2));
        }
        String projectName = p != null ? p.getName() : project != null ? project : "*";
        return new Object[]{
                index,
                ws.formats().text().builder().append(x.getId(), NutsTextNodeStyle.pale()),
                parent.getFlagString(x.getFlag()),
                parent.getStatusString(x.getStatus()),
                parent.getPriorityString(x.getPriority()),
                dte.immutable(),
                parent.getFormattedProject(projectName),
                x.getName()
        };
    }


    private void runTaskRemove(NutsCommandLine cmd) {
        NutsFormatManager text = context.getWorkspace().formats();
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            NTask t = findTask(a.toString(), cmd);
            if (service.tasks().removeTask(t.getId())) {
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("task %s removed.\n",
                            text.text().styled(a.toString(), NutsTextNodeStyle.primary(5))
                    );
                }
            } else {
                context.getSession().out().printf("task %s %s.\n",
                        text.text().styled(a.toString(), NutsTextNodeStyle.primary(5)),
                        text.text().styled("not found", NutsTextNodeStyle.error())
                );
            }
        }

    }


    private void runTaskShow(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            NTask task = findTask(a.toString(), cmd);
            if (task == null) {
                context.getSession().out().printf("```kw %s```: ```error not found```.\n",
                        a.toString()
                );
            } else {
                context.getSession().out().printf("```kw %s```:\n",
                        task.getId()
                );
                String prefix = "\t                    ";
                context.getSession().out().printf("\t```kw2 task name```     : %s\n", parent.formatWithPrefix(task.getName(), prefix));
                context.getSession().out().printf("\t```kw2 status```        : %s\n", parent.formatWithPrefix(task.getStatus(), prefix));
                context.getSession().out().printf("\t```kw2 priority```      : %s\n", parent.formatWithPrefix(task.getPriority(), prefix));
                String project = task.getProject();
                NProject p = service.projects().getProject(project);
                if (project == null || project.length() == 0) {
                    context.getSession().out().printf("\t```kw2 project```       : %s\n", "");
                } else {
                    context.getSession().out().printf("\t```kw2 project```       : %s (%s)\n", project, parent.formatWithPrefix((p == null ? "?" : p.getName()), prefix));
                }
                context.getSession().out().printf("\t```kw2 flag```          : %s\n", parent.formatWithPrefix(task.getFlag(), prefix));
                context.getSession().out().printf("\t```kw2 parent id```     : %s\n", parent.formatWithPrefix(task.getParentTaskId(), prefix));
                context.getSession().out().printf("\t```kw2 job id```        : %s\n", parent.formatWithPrefix(task.getJobId(), prefix));
                context.getSession().out().printf("\t```kw2 due time```      : %s\n", parent.formatWithPrefix(task.getDueTime(), prefix));
                context.getSession().out().printf("\t```kw2 start time```    : %s\n", parent.formatWithPrefix(task.getStartTime(), prefix));
                context.getSession().out().printf("\t```kw2 end time```      : %s\n", parent.formatWithPrefix(task.getEndTime(), prefix));
                context.getSession().out().printf("\t```kw2 duration```      : %s\n", parent.formatWithPrefix(task.getDuration(), prefix));
                context.getSession().out().printf("\t```kw2 duration extra```: %s\n", parent.formatWithPrefix(task.getInternalDuration(), prefix));
                context.getSession().out().printf("\t```kw2 creation time``` : %s\n", parent.formatWithPrefix(task.getCreationTime(), prefix));
                context.getSession().out().printf("\t```kw2 modif. time```   : %s\n", parent.formatWithPrefix(task.getModificationTime(), prefix));
                context.getSession().out().printf("\t```kw2 observations```  : %s\n", parent.formatWithPrefix(task.getObservations(), prefix));
            }
        }
    }



    public boolean runTaskCommands(NutsCommandLine cmd) {
        if (cmd.next("a t", "t a", "ta", "at", "add task", "tasks add") != null) {
            runTaskAdd(cmd);
            return true;
        } else if (cmd.next("t u", "u t", "tu", "ut", "update task", "tasks update") != null) {
            runTaskUpdate(cmd);
            return true;
        } else if (cmd.next("l t", "t l", "lt", "tl", "list tasks", "tasks list") != null) {
            runTaskList(cmd);
            return true;
        } else if (cmd.next("tr", "rt", "trm", "rmt", "t r", "r t", "t rm", "rm t", "remove task", "remove tasks", "rm task", "rm tasks"
                , "tasks remove", "tasks rm") != null
        ) {
            runTaskRemove(cmd);
            return true;
        } else if (cmd.next("st", "ts", "s t", "t s", "show task", "show tasks", "tasks show") != null) {
            runTaskShow(cmd);
            return true;
        } else if (cmd.next("t", "tasks") != null) {
            if (cmd.next("--help") != null) {
                parent.showCustomHelp("njob-tasks");
            } else {
                runTaskList(cmd);
            }
            return true;
        }
        return false;
    }

    private NTask findTask(String pid, NutsCommandLine cmd) {
        NTask t = null;
        if (pid.startsWith("#")) {
            int x = parent.parseIntOrFF(pid.substring(1));
            if (x >= 1) {
                Object lastResults = context.getSession().getProperty("LastResults");
                if (lastResults instanceof NTask[] && x <= ((NTask[]) lastResults).length) {
                    t = ((NTask[]) lastResults)[x - 1];
                }
            }
        }
        if (t == null) {
            t = service.tasks().getTask(pid);
        }
        if (t == null) {
            cmd.throwError("task not found: " + pid);
        }
        return t;
    }

}

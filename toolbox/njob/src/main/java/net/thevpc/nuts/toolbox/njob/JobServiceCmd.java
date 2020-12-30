package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.njob.model.*;
import net.thevpc.nuts.toolbox.njob.time.*;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JobServiceCmd {
    private JobService service;
    private NutsApplicationContext context;
    private NutsWorkspace ws;

    public JobServiceCmd(NutsApplicationContext context) {
        this.context = context;
        this.service = new JobService(context);
        ws = context.getWorkspace();
    }

    private static String formatWithPrefix(Object value, String prefix) {
        if (prefix == null) {
            prefix = "";
        }
        if (value == null) {
            value = "";
        }
        if (value instanceof Instant) {
            value = LocalDateTime.ofInstant((Instant) value, ZoneId.systemDefault());
        }
        return Arrays.stream(value.toString().split("(\n|\r\n)")).collect(Collectors.joining("\n" + prefix));
    }

    public static int parseIntOrFF(String s) {
        if (s == null || s.isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return -1;
        }
    }

    public void runJobAdd(NutsCommandLine cmd) {
        NJob t = new NJob();
        boolean list = false;
        boolean show = false;
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
                case "--time":
                case "--on":
                case "--start":
                case "-t": {
                    t.setStartTime(new TimeParser().parseInstant(cmd.nextString().getStringValue(), false));
                    break;
                }
                case "--at": {
                    t.setStartTime(new TimeParser().setTimeOnly(true).parseInstant(cmd.nextString().getStringValue(), false));
                    break;
                }
                case "--for":
                case "--project":
                case "-p": {
                    t.setProject(cmd.nextString().getStringValue());
                    break;
                }
                case "--obs":
                case "-o": {
                    t.setObservations(cmd.nextString().getStringValue());
                    break;
                }
                case "--duration":
                case "-d": {
                    t.setDuration(TimePeriod.parse(cmd.nextString().getStringValue(), false));
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
        service.addJob(t);
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("job ######%s###### (%s) added.\n",
                    t.getId(),
                    t.getName()
            );
        }
        if (show) {
            runProjectShow(ws.commandLine().create(t.getId()));
        }
        if (list) {
            runProjectList(ws.commandLine().create());
        }
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
                    NJob job = service.getJob(jobId);
                    if (job == null) {
                        cmd.throwError("invalid job " + jobId);
                    }
                    t.setJobId(job.getId());
                    break;
                }
                case "--parent":
                case "-T": {
                    String taskId = cmd.nextString().getStringValue();
                    NTask parentTask = service.getTask(taskId);
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
        service.addTask(t);
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("task ######%s###### (%s) added.\n",
                    t.getId(),
                    t.getName()
            );
        }
        if (show) {
            runProjectShow(ws.commandLine().create(t.getId()));
        }
        if (list) {
            runProjectList(ws.commandLine().create());
        }
    }

    public void runProjectAdd(NutsCommandLine cmd) {
        NProject t = new NProject();
        boolean list = false;
        boolean show = false;
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            if (a.getStringKey().equals("--list") || a.getStringKey().equals("-l")) {
                list = cmd.nextBoolean().getBooleanValue();
            } else if (a.getStringKey().equals("--show") || a.getStringKey().equals("-s")) {
                show = cmd.nextBoolean().getBooleanValue();
            } else if (a.getStringKey().equals("-t") || a.getStringKey().equals("--start") || a.getStringKey().equals("--on")) {
                t.setStartTime(new TimeParser().parseInstant(cmd.nextString().getStringValue(), false));
            } else if (a.getStringKey().equals("--at")) {
                t.setStartTime(new TimeParser().setTimeOnly(true).parseInstant(cmd.nextString().getStringValue(), false));
            } else if (a.getStringKey().equals("-b") || a.getStringKey().equals("--beneficiary") || a.getStringKey().equals("--for")) {
                t.setBeneficiary(cmd.nextString().getStringValue());
            } else if (a.getStringKey().equals("-c") || a.getStringKey().equals("--company") || a.getStringKey().equals("--via")) {
                t.setCompany(cmd.nextString().getStringValue());
            } else if (a.getStringKey().equals("-1") || a.getStringKey().equals("--day1")) {
                t.setStartWeekDay(WeekDay.parse(cmd.nextString().getStringValue()));
            } else if (a.getStringKey().equals("-o") || a.getStringKey().equals("--obs")) {
                t.setObservations(cmd.nextString().getStringValue());
            } else if (a.isNonOption()) {
                if (t.getName() == null) {
                    t.setName(cmd.next().toString());
                } else {
                    cmd.unexpectedArgument();
                }
            } else {
                cmd.unexpectedArgument();
            }
        }
        service.addProject(t);
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("project ######%s###### (%s) added.\n",
                    t.getId(),
                    t.getName()
            );
        }
        if (show) {
            runProjectShow(ws.commandLine().create(t.getId()));
        }
        if (list) {
            runProjectList(ws.commandLine().create());
        }
    }

    public void runProjectUpdate(NutsCommandLine cmd) {
        List<NProject> projects = new ArrayList<>();
        boolean list = false;
        boolean show = false;
        String mergeTo=null;
        List<Consumer<NProject>> runLater = new ArrayList<>();
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            switch (a.getStringKey()) {
                case "-l":
                case "--list": {
                    list = cmd.nextBoolean().getBooleanValue();
                    break;
                }
                case "-s":
                case "--show": {
                    show = cmd.nextBoolean().getBooleanValue();
                    break;
                }
                case "--on":
                case "--start": {
                    Instant v = new TimeParser().parseInstant(cmd.nextString().getStringValue(), false);
                    runLater.add(t -> t.setStartTime(v));
                    break;
                }
                case "--at": {
                    Instant v = new TimeParser().setTimeOnly(true).parseInstant(cmd.nextString().getStringValue(), false);
                    runLater.add(t -> t.setStartTime(v));
                    break;
                }
                case "--for":
                case "--beneficiary":
                case "-b": {
                    String v = cmd.nextString().getStringValue();
                    runLater.add(t -> t.setBeneficiary(v));
                    break;
                }
                case "--company":
                case "--via":
                case "-c": {
                    String v = cmd.nextString().getStringValue();
                    runLater.add(t -> t.setCompany(v));
                    break;
                }
                case "--day1":
                case "-1": {
                    WeekDay v = WeekDay.parse(cmd.nextString().getStringValue());
                    runLater.add(t -> t.setStartWeekDay(v));
                    break;
                }
                case "--obs":
                case "-o": {
                    String v = cmd.nextString().getStringValue();
                    runLater.add(t -> t.setObservations(v));
                    break;
                }
                case "--merge-to": {
                    NutsArgument c = cmd.nextString();
                    if(c.isEnabled()){
                        if(mergeTo!=null){
                            cmd.pushBack(c);
                            cmd.unexpectedArgument();
                        }else{
                            mergeTo=c.getStringValue();
                        }
                    }
                    break;
                }
                case "++obs":
                case "+o": {
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
                        NProject t = findProject(pid, cmd);
                        projects.add(t);
                    } else {
                        cmd.unexpectedArgument();
                    }
                }
            }
        }
        if (projects.isEmpty()) {
            cmd.throwError("project name expected");
        }
        for (NProject project : projects) {
            for (Consumer<NProject> c : runLater) {
                c.accept(project);
            }
            service.updateProject(project);
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().printf("project ######%s###### (##%s##) updated.\n",
                        project.getId(),
                        project.getName()
                );
            }
        }
        if(mergeTo!=null){
            service.mergeProjects(mergeTo,projects.stream().map(x->x.getId()).toArray(String[]::new));
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().printf("projects mer to ######%s######.\n",mergeTo);
            }
        }
        if (show) {
            for (NProject t : new LinkedHashSet<>(projects)) {
                runProjectShow(ws.commandLine().create(t.getId()));
            }
        }
        if (list) {
            runProjectList(ws.commandLine().create());
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
                    NJob job = service.getJob(jobId);
                    if (job == null) {
                        cmd.throwError("invalid job " + jobId);
                    }
                    runLater.add(t -> t.setJobId(job.getId()));
                    break;
                }
                case "-T":
                case "--parent": {
                    String taskId = cmd.nextString().getStringValue();
                    NTask parentTask = service.getTask(taskId);
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
        for (NTask task : new LinkedHashSet<>(tasks)) {
            service.updateTask(task);
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().printf("task ######%s###### (##%s##) updated.\n",
                        task.getId(),
                        task.getName()
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

    public void runJobUpdate(NutsCommandLine cmd) {
        List<NJob> jobs = new ArrayList<>();
        boolean list = false;
        boolean show = false;
        List<Consumer<NJob>> runLater = new ArrayList<>();
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
                case "--on": {
                    String v = cmd.nextString().getStringValue();
                    runLater.add(t -> t.setStartTime(TimePeriod.parseOpPeriodAsInstant(v, t.getStartTime(), true)));
                    break;
                }
                case "--at": {
                    Instant v = new TimeParser().setTimeOnly(true).parseInstant(cmd.nextString().getStringValue(), false);
                    runLater.add(t -> t.setStartTime(v));
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
                        NJob t = findJob(cmd.next().toString(), cmd);
                        jobs.add(t);
                    } else {
                        cmd.unexpectedArgument();
                    }
                }
            }
        }
        if (jobs.isEmpty()) {
            cmd.throwError("job id expected");
        }
        for (NJob job : jobs) {
            for (Consumer<NJob> c : runLater) {
                c.accept(job);
            }
        }
        for (NJob job : new LinkedHashSet<>(jobs)) {
            service.updateJob(job);
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().printf("job ######%s###### (##%s##) updated.\n",
                        job.getId(),
                        job.getName()
                );
            }
        }
        if (show) {
            for (NJob t : new LinkedHashSet<>(jobs)) {
                runTaskList(ws.commandLine().create(t.getId()));
            }
        }
        if (list) {
            runJobList(ws.commandLine().create());
        }
    }

    public boolean runCommands(NutsCommandLine cmd) {
        if (runProjectCommands(cmd)) {
            return true;
        }
        if (runJobCommands(cmd)) {
            return true;
        }
        if (runTaskCommands(cmd)) {
            return true;
        }
        if (cmd.next("summary") != null) {
            runTaskSummary(cmd);
            return true;
        } else if (cmd.next("help") != null) {
            for (String s : new String[]{"jobs", "projects", "tasks"}) {
                showCustomHelp("njob-" + s);
                return true;
            }
            showCustomHelp("njob");
            return true;
        }
        return false;
    }

    private void runTaskSummary(NutsCommandLine cmd) {
        long projectsCount = service.findProjects().count();
        long tasksCount = service.findTasks(NTaskStatusFilter.OPEN, null, -1, null, null, null, null, null).count();
        long jobsCount = service.findMonthJobs(null).count();
        long allJobsCount = service.findLastJobs(null, -1, null, null, null, null, null).count();
        context.getSession().out().printf("##%s## project%s\n", projectsCount, projectsCount == 1 ? "" : "s");
        context.getSession().out().printf("##%s## open task%s\n", tasksCount, tasksCount == 1 ? "" : "s");
        context.getSession().out().printf("##%s## job%s %s\n", allJobsCount, allJobsCount == 1 ? "" : "s",
                allJobsCount == 0 ? "" : NutsString.of(
                        "(##" + jobsCount + "## this month)"
                )
        );
    }

    public boolean runJobCommands(NutsCommandLine cmd) {
        if (cmd.next("aj", "ja", "a j", "j a", "add job", "jobs add") != null) {
            runJobAdd(cmd);
            return true;
        } else if (cmd.next("lj", "jl", "l j", "j l", "list jobs", "jobs list") != null) {
            runJobList(cmd);
            return true;
        } else if (cmd.next("rj", "jr", "jrm", "rmj", "j rm", "rm j", "j r", "r j", "remove job", "remove jobs", "jobs remove") != null) {
            runJobRemove(cmd);
            return true;
        } else if (cmd.next("uj", "ju", "j u", "u j", "update job", "update jobs", "jobs update", "jobs update") != null) {
            runJobUpdate(cmd);
            return true;
        } else if (cmd.next("js", "sj", "j s", "s j", "show job", "show jobs", "jobs show") != null) {
            runJobShow(cmd);
            return true;
        } else if (cmd.next("j", "jobs") != null) {
            if (cmd.next("--help") != null) {
                showCustomHelp("njob-jobs");
            } else {
                runJobList(cmd);
            }
            return true;
        } else {
            return false;
        }
    }

    private void showCustomHelp(String name) {
        context.getSession().out().println(context.getWorkspace().formats().text().loadFormattedString("/net/thevpc/nuts/toolbox/" + name + ".ntf",
                getClass().getClassLoader(), null));
    }

    public boolean runProjectCommands(NutsCommandLine cmd) {
        if (cmd.next("ap", "a p", "pa", "p a", "add project", "projects add") != null) {
            runProjectAdd(cmd);
            return true;
        } else if (cmd.next("pu", "up", "p u", "u p", "update project", "projects update") != null) {
            runProjectUpdate(cmd);
            return true;
        } else if (cmd.next("lp", "pl", "l p", "p l", "list projects", "projects list") != null) {
            runProjectList(cmd);
            return true;
        } else if (cmd.next("rp", "rmp", "pr", "prm", "r p", "rm p", "p r", "p rm", "remove project", "remove projects", "rm project", "rm projects", "projects remove") != null
        ) {
            runProjectRemove(cmd);
            return true;
        } else if (cmd.next("ps", "sp", "s p", "p s", "show project", "show projects", "projects show") != null) {
            runProjectShow(cmd);
            return true;
        } else if (cmd.next("p", "projects") != null) {
            if (cmd.next("--help") != null) {
                showCustomHelp("njob-projects");
            } else {
                runProjectList(cmd);
            }
            return true;
        }
        return false;
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
                showCustomHelp("njob-tasks");
            } else {
                runTaskList(cmd);
            }
            return true;
        }
        return false;
    }

    private void runJobRemove(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            NJob t = findJob(a.toString(), cmd);
            if (service.removeJob(t.getId())) {
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("job ######%s###### removed.\n",
                            a.toString()
                    );
                }
            } else {
                context.getSession().out().printf("job ######%s###### ```error not found```.\n",
                        a.toString()
                );
            }
        }

    }

    private void runTaskRemove(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            NTask t = findTask(a.toString(), cmd);
            if (service.removeTask(t.getId())) {
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("task ######%s###### removed.\n",
                            a.toString()
                    );
                }
            } else {
                context.getSession().out().printf("task ######%s###### ```error not found```.\n",
                        a.toString()
                );
            }
        }

    }

    private void runProjectRemove(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            NProject t = findProject(a.toString(), cmd);
            if (service.removeProject(t.getId())) {
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("project ######%s###### removed.\n",
                            a.toString()
                    );
                }
            } else {
                context.getSession().out().printf("project ######%s###### ```error not found```.\n",
                        a.toString()
                );
            }
        }

    }

    private void runJobShow(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            NJob job = findJob(a.toString(), cmd);
            if (job == null) {
                context.getSession().out().printf("```kw %s```: ```error not found```.\n",
                        a.toString()
                );
            } else {
                context.getSession().out().printf("```kw %s```:\n",
                        job.getId()
                );
                String prefix = "\t                    ";
                context.getSession().out().printf("\t```kw2 job name```      : %s:\n", formatWithPrefix(job.getName(), prefix));
                String project = job.getProject();
                NProject p = service.getProject(project);
                if (project == null || project.length() == 0) {
                    context.getSession().out().printf("\t```kw2 project```       : %s\n", "");
                } else {
                    context.getSession().out().printf("\t```kw2 project```       : %s (%s)\n", project, formatWithPrefix(p == null ? "?" : p.getName(), prefix));
                }
                context.getSession().out().printf("\t```kw2 duration```      : %s\n", formatWithPrefix(job.getDuration(), prefix));
                context.getSession().out().printf("\t```kw2 start time```    : %s\n", formatWithPrefix(job.getStartTime(), prefix));
                context.getSession().out().printf("\t```kw2 duration extra```: %s\n", formatWithPrefix(job.getInternalDuration(), prefix));
                context.getSession().out().printf("\t```kw2 observations```  : %s\n", formatWithPrefix(job.getObservations(), prefix));
            }
        }

    }

    private void runProjectShow(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            NProject project = findProject(a.toString(), cmd);
            if (project == null) {
                context.getSession().out().printf("```kw %s```: ```error not found```.\n",
                        a.toString()
                );
            } else {
                context.getSession().out().printf("```kw %s```:\n",
                        project.getId()
                );
                String prefix = "\t                    ";
                context.getSession().out().printf("\t```kw2 project name```  : %s\n", formatWithPrefix(project.getName(), prefix));
                context.getSession().out().printf("\t```kw2 beneficiary```   : %s\n", formatWithPrefix(project.getBeneficiary(), prefix));
                context.getSession().out().printf("\t```kw2 company```       : %s\n", formatWithPrefix(project.getCompany(), prefix));
                context.getSession().out().printf("\t```kw2 start time```    : %s\n", formatWithPrefix(project.getStartTime(), prefix));
                context.getSession().out().printf("\t```kw2 start week day```: %s\n", formatWithPrefix(project.getStartWeekDay(), prefix));
                context.getSession().out().printf("\t```kw2 observations```  : %s\n", formatWithPrefix(project.getObservations(), prefix));
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
                context.getSession().out().printf("\t```kw2 task name```     : %s\n", formatWithPrefix(task.getName(), prefix));
                context.getSession().out().printf("\t```kw2 status```        : %s\n", formatWithPrefix(task.getStatus(), prefix));
                context.getSession().out().printf("\t```kw2 priority```      : %s\n", formatWithPrefix(task.getPriority(), prefix));
                String project = task.getProject();
                NProject p = service.getProject(project);
                if (project == null || project.length() == 0) {
                    context.getSession().out().printf("\t```kw2 project```       : %s\n", "");
                } else {
                    context.getSession().out().printf("\t```kw2 project```       : %s (%s)\n", project, formatWithPrefix((p == null ? "?" : p.getName()), prefix));
                }
                context.getSession().out().printf("\t```kw2 flag```          : %s\n", formatWithPrefix(task.getFlag(), prefix));
                context.getSession().out().printf("\t```kw2 parent id```     : %s\n", formatWithPrefix(task.getParentTaskId(), prefix));
                context.getSession().out().printf("\t```kw2 job id```        : %s\n", formatWithPrefix(task.getJobId(), prefix));
                context.getSession().out().printf("\t```kw2 due time```      : %s\n", formatWithPrefix(task.getDueTime(), prefix));
                context.getSession().out().printf("\t```kw2 start time```    : %s\n", formatWithPrefix(task.getStartTime(), prefix));
                context.getSession().out().printf("\t```kw2 end time```      : %s\n", formatWithPrefix(task.getEndTime(), prefix));
                context.getSession().out().printf("\t```kw2 duration```      : %s\n", formatWithPrefix(task.getDuration(), prefix));
                context.getSession().out().printf("\t```kw2 duration extra```: %s\n", formatWithPrefix(task.getInternalDuration(), prefix));
                context.getSession().out().printf("\t```kw2 creation time``` : %s\n", formatWithPrefix(task.getCreationTime(), prefix));
                context.getSession().out().printf("\t```kw2 modif. time```   : %s\n", formatWithPrefix(task.getModificationTime(), prefix));
                context.getSession().out().printf("\t```kw2 observations```  : %s\n", formatWithPrefix(task.getObservations(), prefix));
            }
        }

    }

    private void runJobList(NutsCommandLine cmd) {
        TimespanPattern hoursPerDay = TimespanPattern.WORK;
        int count = 100;
        NJobGroup groupBy = null;
        ChronoUnit countType = null;
        ChronoUnit timeUnit = null;
        Predicate<NJob> whereFilter = null;
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            switch (a.getStringKey()) {
                case "-w":
                case "--weeks":
                    {
                    countType = ChronoUnit.WEEKS;
                    count = cmd.nextString().getIntValue();
                    break;
                }
                case "-m":
                case "--months":
                    {
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
                case "-p":
                case "--project":
                    {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createProjectFilter(s);
                    Predicate<NJob> t = x -> sp.test(x.getProject());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "--name":
                    {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NJob> t = x -> sp.test(x.getName());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-b":
                case "--beneficiary":
                    {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NJob> t = x -> {
                        NProject project = service.getProject(x.getProject());
                        return sp.test(project == null ? "" : project.getBeneficiary());
                    };
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-c":
                case "--company":
                    {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NJob> t = x -> {
                        NProject project = service.getProject(x.getProject());
                        return sp.test(project == null ? "" : project.getCompany());
                    };
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-d":
                case "--duration":
                    {
                    String s = cmd.nextString().getStringValue();
                    Predicate<TimePeriod> p = TimePeriod.parseFilter(s, false);
                    Predicate<NJob> t = x -> p.test(x.getDuration());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-t":
                case "--startTime":
                case "--start-time":
                    {
                    String s = cmd.nextString().getStringValue();
                    Predicate<Instant> t = new TimeParser().parseInstantFilter(s, false);
                    whereFilter = appendPredicate(whereFilter, x -> t.test(x.getStartTime()));
                    break;
                }
                default: {
                    cmd.unexpectedArgument();
                }
            }
        }
        Stream<NJob> r = service.findLastJobs(null, count, countType, whereFilter, groupBy, timeUnit, hoursPerDay);
        ChronoUnit timeUnit0 = timeUnit;
        if (context.getSession().isPlainTrace()) {
            NutsMutableTableModel m = ws.formats().table().createModel();
            NJobGroup finalGroupBy = groupBy;
            List<NJob> lastResults = new ArrayList<>();
            int[] index = new int[1];
            r.forEach(x -> {
                NutsString durationString = ws.formats().text().builder()
                        .appendStyled(String.valueOf(timeUnit0 == null ? x.getDuration() : x.getDuration().toUnit(timeUnit0, hoursPerDay)),NutsTextNodeStyle.KEYWORD1)
                        .immutable();
                index[0]++;
                lastResults.add(x);
                m.newRow().addCells(
                        (finalGroupBy != null) ?
                                new Object[]{
                                        createHashId(index[0], -1),
                                        getFormattedDate(x.getStartTime()),
                                        durationString,
                                        getFormattedProject(x.getProject() == null ? "*" : x.getProject()),
                                        x.getName()

                                } : new Object[]{
                                createHashId(index[0], -1),
                                ws.formats().text().builder().appendStyled(x.getId(),NutsTextNodeStyle.PALE1).immutable(),
                                getFormattedDate(x.getStartTime()),
                                durationString,
                                getFormattedProject(x.getProject() == null ? "*" : x.getProject()),
                                x.getName()

                        }
                );
            });
            context.getSession().setProperty("LastResults", lastResults.toArray(new NJob[0]));
            ws.formats().table()
                    .setBorder("spaces")
                    .setModel(m).println(context.getSession().out());
        } else {
            context.getSession().formatObject(r.collect(Collectors.toList())).print(context.getSession().out());
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
                case "--weeks":
                    {
                    countType = ChronoUnit.WEEKS;
                    count = cmd.nextString().getIntValue();
                    break;
                }
                case "-m":
                case "--months":
                    {
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
                    Predicate<String> sp = createProjectFilter(s);
                    Predicate<NTask> t = x -> sp.test(x.getProject());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-n":
                case "--name":
                    {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NTask> t = x -> sp.test(x.getName());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-b":
                case "--beneficiary":
                    {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NTask> t = x -> {
                        NProject project = service.getProject(x.getProject());
                        return sp.test(project == null ? "" : project.getBeneficiary());
                    };
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-c":
                case "--company":
                    {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NTask> t = x -> {
                        NProject project = service.getProject(x.getProject());
                        return sp.test(project == null ? "" : project.getCompany());
                    };
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-d":
                case "--duration":
                    {
                    String s = cmd.nextString().getStringValue();
                    Predicate<TimePeriod> p = TimePeriod.parseFilter(s, false);
                    Predicate<NTask> t = x -> p.test(x.getDuration());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-t":
                case "--startTime":
                case "--start-time":
                    {
                    String s = cmd.nextString().getStringValue();
                    Predicate<Instant> t = new TimeParser().parseInstantFilter(s, false);
                    whereFilter = appendPredicate(whereFilter, x -> t.test(x.getStartTime()));
                    break;
                }
                default: {
                    cmd.unexpectedArgument();
                }
            }
        }
        Stream<NTask> r = service.findTasks(status, null, count, countType, whereFilter, groupBy, timeUnit, hoursPerDay);

        if (context.getSession().isPlainTrace()) {
            NutsMutableTableModel m = ws.formats().table().createModel();
            List<NTask> lastResults = new ArrayList<>();
            int[] index = new int[1];
            r.forEach(x -> {
                index[0]++;
                m.newRow().addCells(toTaskRowArray(x,
                        createHashId(index[0], -1)
                ));
                lastResults.add(x);
            });
            context.getSession().setProperty("LastResults", lastResults.toArray(new NTask[0]));
            ws.formats().table()
                    .setBorder("spaces")
                    .setModel(m).println(context.getSession().out());
        } else {
            context.getSession().formatObject(r.collect(Collectors.toList())).print(context.getSession().out());
        }
    }

    private Object[] toTaskRowArray(NTask x, String index) {
        String project = x.getProject();
        NProject p = project == null ? null : service.getProject(project);
        NTaskStatus s = x.getStatus();
        String dte0 = getFormattedDate(x.getDueTime());
        NutsTextNodeBuilder dte = ws.formats().text().builder();
        if (s == NTaskStatus.CANCELLED || s == NTaskStatus.DONE) {
            dte.appendStyled(dte0, NutsTextNodeStyle.PALE1);
        } else if (x.getDueTime() != null && x.getDueTime().compareTo(Instant.now()) < 0) {
            dte.appendStyled(dte0, NutsTextNodeStyle.ERROR1);
        } else {
            dte.appendStyled(dte0, NutsTextNodeStyle.KEYWORD2);
        }
        String projectName = p != null ? p.getName() : project != null ? project : "*";
        return new Object[]{
                index,
                ws.formats().text().builder().appendStyled(x.getId(),NutsTextNodeStyle.PALE1),
                getFlagString(x.getFlag()),
                getStatusString(x.getStatus()),
                getPriorityString(x.getPriority()),
                dte.immutable(),
                getFormattedProject(projectName),
                x.getName()
        };
    }

    private NutsString getFormattedProject(String projectName) {
        return ws.formats().text().builder().appendHashedStyle(projectName).immutable();
    }

    private String getFormattedDate(Instant x) {
        if (x == null) {
            return "?";
        }
        return new TimeFormatter().format(x.atZone(ZoneId.systemDefault()).toLocalDateTime());
//        String s = x.atZone(ZoneId.systemDefault()).toString() + " " +
//                x.atZone(ZoneId.systemDefault()).getDayOfWeek().toString().toLowerCase().substring(0, 3);
//        return s;
    }

    private NutsString getCheckedString(Boolean x) {
        if (x == null) {
            return NutsString.of("");
        }
        if (x) {
            return NutsString.of("\u2611");
        } else {
            return NutsString.of("\u25A1");
        }
    }

    private NutsString getPriorityString(NPriority x) {
        if (x == null) {
            return NutsString.of("N");
        }
        switch (x) {
            case NONE:
                return NutsString.of("```pale 0```ø");
            case LOW:
                return NutsString.of("```pale L```ø");
            case NORMAL:
                return NutsString.of("N");
            case MEDIUM:
                return NutsString.of("##M##ø");
            case URGENT:
                return NutsString.of("###U###ø");
            case HIGH:
                return NutsString.of("####H####ø");
            case CRITICAL:
                return NutsString.of("```error C```ø");
        }
        return NutsString.of("?");
    }

    private NutsString getStatusString(NTaskStatus x) {
        if (x == null) {
            return NutsString.of("*");
        }
        switch (x) {
            case TODO:
//                return new NutsImmutableString("\u2B58");
                return NutsString.of("\u24c9");
//            case TODO:return new NutsImmutableString("\u25A1");
            case DONE:
                return NutsString.of("ø```success \u2611```ø");
//            case WIP:return new NutsImmutableString("##\u25B6##");
            case WIP:
                return NutsString.of("##\u24CC##ø");
//                return new NutsImmutableString("##\u25F6##");
            case CANCELLED:
//                return new NutsImmutableString("@@\u2613@@");
//                return new NutsImmutableString("@@\u2421@@");
//                return new NutsImmutableString("@@\u26C3@@");
                return NutsString.of("```error \u2718```ø");
        }
        return NutsString.of("?");
    }

    private NutsString getFlagString(String x, int index) {
        switch (index) {
            case 1:
                return NutsString.of("##"+x+"##ø");
            case 2:
                return NutsString.of("###"+x+"###ø");
            case 3:
                return NutsString.of("####"+x+"####ø");
            case 4:
                return NutsString.of("#####"+x+"#####ø");
            case 5:
                return NutsString.of("######"+x+"######ø");
        }
        throw new IllegalArgumentException("Invalid index "+index);
    }

    private NutsString getFlagString(NFlag x) {
        if (x == null) {
            x = NFlag.NONE;
        }
        switch (x) {
            case NONE:
                return NutsString.of("\u2690");

            case STAR1:
                return getFlagString("\u2605",1);
            case STAR2:
                return getFlagString("\u2605",2);
            case STAR3:
                return getFlagString("\u2605",3);
            case STAR4:
                return getFlagString("\u2605",4);
            case STAR5:
                return getFlagString("\u2605",5);

            case FLAG1:
                return getFlagString("\u2691",1);
            case FLAG2:
                return getFlagString("\u2691",2);
            case FLAG3:
                return getFlagString("\u2691",3);
            case FLAG4:
                return getFlagString("\u2691",4);
            case FLAG5:
                return getFlagString("\u2691",5);

            case KING1:
                return getFlagString("\u265A",1);
            case KING2:
                return getFlagString("\u265A",2);
            case KING3:
                return getFlagString("\u265A",3);
            case KING4:
                return getFlagString("\u265A",4);
            case KING5:
                return getFlagString("\u265A",5);

            case HEART1:
                return getFlagString("\u2665",1);
            case HEART2:
                return getFlagString("\u2665",2);
            case HEART3:
                return getFlagString("\u2665",3);
            case HEART4:
                return getFlagString("\u2665",4);
            case HEART5:
                return getFlagString("\u2665",5);

            case PHONE1:
                return getFlagString("\u260E",1);
            case PHONE2:
                return getFlagString("\u260E",2);
            case PHONE3:
                return getFlagString("\u260E",3);
            case PHONE4:
                return getFlagString("\u260E",4);
            case PHONE5:
                return getFlagString("\u260E",5);
        }
        return NutsString.of("[" + x.toString().toLowerCase() + "]");
    }

    private void runProjectList(NutsCommandLine cmd) {
        Predicate<NProject> whereFilter = null;
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            switch (a.getStringKey()) {
                case "-b":
                case "-beneficiary":
                    {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NProject> t = x -> sp.test(x.getBeneficiary());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-c":
                case "-company":
                    {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NProject> t = x -> sp.test(x.getCompany());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-n":
                case "--name":
                    {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NProject> t = x -> sp.test(x.getName());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "--unused":
                    {
                    boolean unused = cmd.nextBoolean().getBooleanValue();
                    Predicate<NProject> t = x -> service.isUsedProject(x.getId())!=unused;
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-t":
                case "--startTime":
                case "--start-time":
                    {
                    String s = cmd.nextString().getStringValue();
                    Predicate<Instant> t = new TimeParser().parseInstantFilter(s, false);
                    whereFilter = appendPredicate(whereFilter, x -> t.test(x.getStartTime()));
                    break;
                }
                default: {
                    cmd.unexpectedArgument();
                }
            }
        }
        Stream<NProject> r =
                service.findProjects().filter(whereFilter == null ? x -> true : whereFilter)
                        .sorted(
                                (x, y) -> {
                                    Instant s1 = x.getStartTime();
                                    Instant s2 = y.getStartTime();
                                    int v = s2.compareTo(s1);
                                    if (v != 0) {
                                        return v;
                                    }
                                    return x.getName().compareTo(y.getName());
                                }
                        );

        if (context.getSession().isPlainTrace()) {
            NutsMutableTableModel m = ws.formats().table().createModel();
            List<NProject> lastResults = new ArrayList<>();
            int[] index = new int[1];
            r.forEach(x -> {
                Instant st = x.getStartTime();
                String sts = "";
                if (st != null) {
                    LocalDateTime d = LocalDateTime.ofInstant(st, ZoneId.systemDefault());
                    sts = d.getYear() + " " + d.getMonth().toString().toLowerCase().substring(0, 3);
                }
                lastResults.add(x);
                index[0]++;
                m.newRow().addCells(
                        createHashId(index[0], -1),
                        x.getId(),
                        sts,
                        x.getCompany(),
                        x.getBeneficiary(),
                        getFormattedProject(x.getName() == null ? "*" : x.getName())
                );
            });
            context.getSession().setProperty("LastResults", lastResults.toArray(new NProject[0]));
            ws.formats().table()
                    .setBorder("spaces")
                    .setModel(m).println(context.getSession().out());
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
            Pattern pattern = Pattern.compile(JobService.wildcardToRegex(s));
            return x -> pattern.matcher(x == null ? "" : x).matches();
        }
        return x -> s.equals(x == null ? "" : x);
    }

    public void runInteractive(NutsCommandLine cmdLine) {
        NutsSession session = context.getSession();
        context.getWorkspace().io().term().enableRichTerm(context.getSession());

        session.out().println("##" + context.getAppId().getArtifactId() + " " + context.getAppId().getVersion() + "## interactive mode. type ```error q``` to quit.");
        InputStream in = session.getTerminal().in();
        Exception lastError = null;
        while (true) {
            String line = null;
            try {
                line = session.getTerminal().readLine("> ");
            } catch (NoSuchElementException e) {
            }
            if (line == null) {
                break;
            }
            //line=line.trim();
            if (line.isEmpty()) {
                //
            } else if (line.trim().equals("q") || line.trim().equals("quit") || line.trim().equals("exit")) {
                break;
            } else if (line.trim().equals("err") || line.trim().equals("show-error") || line.trim().equals("show error")) {
                if (lastError != null) {
                    lastError.printStackTrace(session.out());
                }
            } else {
                NutsCommandLine cmd = ws.commandLine().parse(line);
                cmd.setCommandName(context.getAppId().getArtifactId());
                try {
                    lastError = null;
                    boolean b = runCommands(cmd);
                    if (!b) {
                        session.out().println("```error command not found```");
                    }
                } catch (Exception ex) {
                    lastError = ex;
                    String m = ex.getMessage();
                    if (m == null) {
                        m = ex.toString();
                    }
                    session.err().printf("```error ERROR: %s```\n", m);
                }
            }
        }
    }

    public Predicate<String> createProjectFilter(String s) {
        if (service.isIdFormat(s)) {
            return createStringFilter(s);
        } else {
            Predicate<String> sp = createStringFilter(s);
            return x -> {
                NProject y = service.getProject(x);
                return y != null && sp.test(y.getName());
            };
        }
    }

    private NProject findProject(String pid, NutsCommandLine cmd) {
        NProject t = null;
        if (pid.startsWith("#")) {
            int x = parseIntOrFF(pid.substring(1));
            if (x >= 1) {
                Object lastResults = context.getSession().getProperty("LastResults");
                if (lastResults instanceof NProject[] && x <= ((NProject[]) lastResults).length) {
                    t = ((NProject[]) lastResults)[x - 1];
                }
            }
        }
        if (t == null) {
            t = service.getProject(pid);
        }
        if (t == null) {
            cmd.throwError("project not found: " + pid);
        }
        return t;
    }

    private NTask findTask(String pid, NutsCommandLine cmd) {
        NTask t = null;
        if (pid.startsWith("#")) {
            int x = parseIntOrFF(pid.substring(1));
            if (x >= 1) {
                Object lastResults = context.getSession().getProperty("LastResults");
                if (lastResults instanceof NTask[] && x <= ((NTask[]) lastResults).length) {
                    t = ((NTask[]) lastResults)[x - 1];
                }
            }
        }
        if (t == null) {
            t = service.getTask(pid);
        }
        if (t == null) {
            cmd.throwError("task not found: " + pid);
        }
        return t;
    }

    private NJob findJob(String pid, NutsCommandLine cmd) {
        NJob t = null;
        if (pid.startsWith("#")) {
            int x = parseIntOrFF(pid.substring(1));
            if (x >= 1) {
                Object lastResults = context.getSession().getProperty("LastResults");
                if (lastResults instanceof NJob[] && x <= ((NJob[]) lastResults).length) {
                    t = ((NJob[]) lastResults)[x - 1];
                }
            }
        }
        if (t == null) {
            t = service.getJob(pid);
        }
        if (t == null) {
            cmd.throwError("job not found: " + pid);
        }
        return t;
    }

    private String createHashId(int value, int maxValues) {
//        DecimalFormat decimalFormat = new DecimalFormat("00");
//        return "#"+decimalFormat.format(value);
        return "#" + value;
    }
}

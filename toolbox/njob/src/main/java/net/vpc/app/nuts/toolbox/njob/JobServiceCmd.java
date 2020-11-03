package net.vpc.app.nuts.toolbox.njob;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.njob.model.*;
import net.vpc.app.nuts.toolbox.njob.time.*;

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


    public void runJobAdd(NutsCommandLine cmd) {
        NJob t = new NJob();
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
            } else if (a.getStringKey().equals("-p") || a.getStringKey().equals("--project") || a.getStringKey().equals("--for")) {
                t.setProject(cmd.nextString().getStringValue());
            } else if (a.getStringKey().equals("-o") || a.getStringKey().equals("--obs")) {
                t.setObservations(cmd.nextString().getStringValue());
            } else if (a.getStringKey().equals("-d") || a.getStringKey().equals("--duration")) {
                t.setDuration(TimePeriod.parse(cmd.nextString().getStringValue(), false));
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
        service.addJob(t);
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("job {{%s}} (%s) added.\n",
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
            if (a.getStringKey().equals("--list") || a.getStringKey().equals("-l")) {
                list = cmd.nextBoolean().getBooleanValue();
            } else if (a.getStringKey().equals("--show") || a.getStringKey().equals("-s")) {
                show = cmd.nextBoolean().getBooleanValue();
            } else if (a.getStringKey().equals("-t") || a.getStringKey().equals("--on") || a.getStringKey().equals("--due")) {
                t.setDueTime(new TimeParser().parseInstant(cmd.nextString().getStringValue(), false));
            } else if (a.getStringKey().equals("--at")) {
                t.setDueTime(new TimeParser().setTimeOnly(true).parseInstant(cmd.nextString().getStringValue(), false));
            } else if (a.getStringKey().equals("--start")) {
                t.setStartTime(new TimeParser().parseInstant(cmd.nextString().getStringValue(), false));
            } else if (a.getStringKey().equals("--end")) {
                t.setEndTime(new TimeParser().parseInstant(cmd.nextString().getStringValue(), false));
            } else if (a.getStringKey().equals("--for")) {
                String v = cmd.nextString().getStringValue();
                Instant u = new TimeParser().parseInstant(v, true);
                if (u != null) {
                    t.setDueTime(u);
                } else {
                    t.setProject(v);
                }
            } else if (a.getStringKey().equals("-p") || a.getStringKey().equals("--project")) {
                t.setProject(cmd.nextString().getStringValue());
            } else if (a.getStringKey().equals("-n") || a.getStringKey().equals("--name")) {
                t.setName(cmd.nextString().getStringValue());
            } else if (a.getStringKey().equals("-f") || a.getStringKey().equals("--flag")) {
                String v = cmd.nextString().getStringValue();
                NFlag f = null;
                if ("random".equalsIgnoreCase(v)) {
                    f = NFlag.values()[(int) (Math.random() * NFlag.values().length)];
                } else {
                    f = NFlag.valueOf(v.toUpperCase());
                }
                t.setFlag(f);
            } else if (a.getStringKey().equals("-j") || a.getStringKey().equals("--job")) {
                String jobId = cmd.nextString().getStringValue();
                NJob job = service.getJob(jobId);
                if (job == null) {
                    cmd.throwError("invalid job " + jobId);
                }
                t.setJobId(job.getId());
            } else if (a.getStringKey().equals("-T") || a.getStringKey().equals("--parent")) {
                String taskId = cmd.nextString().getStringValue();
                NTask parentTask = service.getTask(taskId);
                if (parentTask == null) {
                    cmd.throwError("invalid parent task " + taskId);
                }
                t.setParentTaskId(parentTask.getId());
            } else if (a.getStringKey().equals("-P") || a.getStringKey().equals("--priority")) {
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
            } else if (a.getStringKey().equals("-o") || a.getStringKey().equals("--obs")) {
                t.setObservations(cmd.nextString().getStringValue());
            } else if (a.getStringKey().equals("-d") || a.getStringKey().equals("--duration")) {
                t.setDuration(TimePeriod.parse(cmd.nextString().getStringValue(), true));
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
        service.addTask(t);
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("task {{%s}} (%s) added.\n",
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
            context.getSession().out().printf("project {{%s}} (%s) added.\n",
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
        List<Consumer<NProject>> runLater = new ArrayList<>();
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            if (a.getStringKey().equals("--list") || a.getStringKey().equals("-l")) {
                list = cmd.nextBoolean().getBooleanValue();
            } else if (a.getStringKey().equals("--show") || a.getStringKey().equals("-s")) {
                show = cmd.nextBoolean().getBooleanValue();
            } else if (a.getStringKey().equals("-o") || a.getStringKey().equals("--on") || a.getStringKey().equals("--start")) {
                Instant v = new TimeParser().parseInstant(cmd.nextString().getStringValue(), false);
                runLater.add(t -> t.setStartTime(v));
            } else if (a.getStringKey().equals("--at")) {
                Instant v = new TimeParser().setTimeOnly(true).parseInstant(cmd.nextString().getStringValue(), false);
                runLater.add(t -> t.setStartTime(v));
            } else if (a.getStringKey().equals("-b") || a.getStringKey().equals("--for") || a.getStringKey().equals("--beneficiary")) {
                String v = cmd.nextString().getStringValue();
                runLater.add(t -> t.setBeneficiary(v));
            } else if (a.getStringKey().equals("-c") || a.getStringKey().equals("--via") || a.getStringKey().equals("--company")) {
                String v = cmd.nextString().getStringValue();
                runLater.add(t -> t.setCompany(v));
            } else if (a.getStringKey().equals("-1") || a.getStringKey().equals("--day1")) {
                WeekDay v = WeekDay.parse(cmd.nextString().getStringValue());
                runLater.add(t -> t.setStartWeekDay(v));
            } else if (a.getStringKey().equals("-o") || a.getStringKey().equals("--obs")) {
                String v = cmd.nextString().getStringValue();
                runLater.add(t -> t.setObservations(v));
            } else if (a.getStringKey().equals("--obs+") || a.getStringKey().equals("-o+")) {
                String v = cmd.nextString().getStringValue();
                runLater.add(t -> {
                    String s=t.getObservations();
                    if(s==null){
                        s="";
                    }
                    s=s.trim();
                    if(!s.isEmpty()){
                        s+="\n";
                    }
                    s+=v;
                    s=s.trim();
                    t.setObservations(s);
                });
            } else {
                NProject t = service.getProject(cmd.next().toString());
                if (t == null) {
                    cmd.throwError("project not found: " + a.toString());
                }
                projects.add(t);
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
                context.getSession().out().printf("project {{%s}} (##%s##) updated.\n",
                        project.getId(),
                        project.getName()
                );
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
            if (a.getStringKey().equals("--list") || a.getStringKey().equals("-l")) {
                list = cmd.nextBoolean().getBooleanValue();
            } else if (a.getStringKey().equals("--show") || a.getStringKey().equals("-s")) {
                show = cmd.nextBoolean().getBooleanValue();
            } else if (a.getStringKey().equals("--start")) {
                Instant v = new TimeParser().parseInstant(cmd.nextString().getStringValue(), false);
                runLater.add(t -> t.setStartTime(v));
            } else if (a.getStringKey().equals("-t") || a.getStringKey().equals("--due") || a.getStringKey().equals("--on")) {
                String v = cmd.nextString().getStringValue();
                runLater.add(t -> t.setDueTime(TimePeriod.parseOpPeriodAsInstant(v, t.getDueTime(), true)));
            } else if (a.getStringKey().equals("--at")) {
                Instant v = new TimeParser().setTimeOnly(true).parseInstant(cmd.nextString().getStringValue(), false);
                runLater.add(t -> t.setDueTime(v));
            } else if (a.getStringKey().equals("--end")) {
                Instant v = new TimeParser().parseInstant(cmd.nextString().getStringValue(), false);
                runLater.add(t -> t.setEndTime(v));
            } else if (a.getStringKey().equals("--wip")) {
                cmd.skip();
                runLater.add(t -> t.setStatus(NTaskStatus.WIP));
            } else if (a.getStringKey().equals("--done")) {
                cmd.skip();
                runLater.add(t -> t.setStatus(NTaskStatus.DONE));
            } else if (a.getStringKey().equals("--cancel") || a.getStringKey().equals("--cancelled")) {
                cmd.skip();
                runLater.add(t -> t.setStatus(NTaskStatus.CANCELLED));
            } else if (a.getStringKey().equals("--todo")) {
                cmd.skip();
                runLater.add(t -> t.setStatus(NTaskStatus.TODO));
            } else if (a.getStringKey().equals("--high")) {
                cmd.skip();
                runLater.add(t -> t.setPriority(NPriority.HIGH));
            } else if (a.getStringKey().equals("--critical")) {
                cmd.skip();
                runLater.add(t -> t.setPriority(NPriority.CRITICAL));
            } else if (a.getStringKey().equals("--normal")) {
                cmd.skip();
                runLater.add(t -> t.setPriority(NPriority.NORMAL));
            } else if (a.getStringKey().equals("--prio++") || a.getStringKey().equals("-p++")) {
                cmd.skip();
                runLater.add(t -> t.setPriority((t.getPriority() == null ? NPriority.NORMAL : t.getPriority()).higher()));
            } else if (a.getStringKey().equals("--prio--") || a.getStringKey().equals("-p--")) {
                cmd.skip();
                runLater.add(t -> t.setPriority((t.getPriority() == null ? NPriority.NORMAL : t.getPriority()).lower()));
            } else if (a.getStringKey().equals("--status")) {
                NTaskStatus v = NTaskStatus.valueOf(cmd.nextString().getStringValue().toLowerCase());
                runLater.add(t -> t.setStatus(v));
            } else if (a.getStringKey().equals("-d") || a.getStringKey().equals("--duration")) {
                TimePeriod v = TimePeriod.parse(cmd.nextString().getStringValue(), false);
                runLater.add(t -> t.setDuration(v));
            } else if (a.getStringKey().equals("-n") || a.getStringKey().equals("--name") || a.getStringKey().equals("-n")) {
                String v = cmd.nextString().getStringValue();
                runLater.add(t -> t.setName(v));
            } else if (a.getStringKey().equals("-f") || a.getStringKey().equals("--flag")) {
                String v = cmd.nextString().getStringValue();
                NFlag f = ("random".equalsIgnoreCase(v)) ?
                        NFlag.values()[(int) (Math.random() * NFlag.values().length)]
                        : NFlag.valueOf(v.toUpperCase());
                runLater.add(t -> t.setFlag(f));
            } else if (a.getStringKey().equals("-j") || a.getStringKey().equals("--job")) {
                String jobId = cmd.nextString().getStringValue();
                NJob job = service.getJob(jobId);
                if (job == null) {
                    cmd.throwError("invalid job " + jobId);
                }
                runLater.add(t -> t.setJobId(job.getId()));
            } else if (a.getStringKey().equals("-T") || a.getStringKey().equals("--parent")) {
                String taskId = cmd.nextString().getStringValue();
                NTask parentTask = service.getTask(taskId);
                if (parentTask == null) {
                    cmd.throwError("invalid parent task " + taskId);
                }
                runLater.add(t -> t.setParentTaskId(parentTask.getId()));
            } else if (a.getStringKey().equals("-P") || a.getStringKey().equals("--priority")) {
                String v = cmd.nextString().getStringValue();
                runLater.add(t -> {
                    NPriority p = t.getPriority();
                    if (v.equalsIgnoreCase("higher")) {
                        p = p.higher();
                    } else if (v.equalsIgnoreCase("lower")) {
                        p = p.lower();
                    } else {
                        p = NPriority.valueOf(v.toLowerCase());
                    }
                    t.setPriority(p);
                });
            } else if (a.getStringKey().equals("--for")) {
                String v = cmd.nextString().getStringValue();
                runLater.add(t -> {
                    Instant u = TimePeriod.parseOpPeriodAsInstant(v, t.getDueTime(), true);
                    if (u != null) {
                        t.setDueTime(u);
                    } else {
                        t.setProject(v);
                    }
                });
            } else if (a.getStringKey().equals("-p") || a.getStringKey().equals("--project") || a.getStringKey().equals("-p")) {
                String v = cmd.nextString().getStringValue();
                runLater.add(t -> t.setProject(v));
            } else if (a.getStringKey().equals("--obs") || a.getStringKey().equals("-o")) {
                String v = cmd.nextString().getStringValue();
                runLater.add(t -> t.setObservations(v));
            } else if (a.getStringKey().equals("--obs+") || a.getStringKey().equals("-o+")) {
                String v = cmd.nextString().getStringValue();
                runLater.add(t -> {
                    String s=t.getObservations();
                    if(s==null){
                        s="";
                    }
                    s=s.trim();
                    if(!s.isEmpty()){
                       s+="\n";
                    }
                    s+=v;
                    s=s.trim();
                    t.setObservations(s);
                });
            } else if (a.isNonOption()) {
                NTask t = service.getTask(cmd.next().toString());
                if (t == null) {
                    cmd.throwError("task not found: " + a.toString());
                }
                tasks.add(t);
            } else {
                cmd.unexpectedArgument();
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
                context.getSession().out().printf("task {{%s}} (##%s##) updated.\n",
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
        return false;
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
        } else if (cmd.next("js", "sj", "j s", "s j", "show job", "show jobs", "jobs show") != null) {
            runJobShow(cmd);
            return true;
        } else if (cmd.next("j", "jobs") != null) {
            runJobList(cmd);
            return true;
        } else {
            return false;
        }
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
            runProjectList(cmd);
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
            runTaskList(cmd);
            return true;
        }
        return false;
    }

    private void runJobRemove(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            if (service.removeJob(a.toString())) {
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("job {{%s}} removed.\n",
                            a.toString()
                    );
                }
            } else {
                context.getSession().out().printf("job {{%s}} @@not found@@.\n",
                        a.toString()
                );
            }
        }

    }

    private void runTaskRemove(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            if (service.removeTask(a.toString())) {
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("task {{%s}} removed.\n",
                            a.toString()
                    );
                }
            } else {
                context.getSession().out().printf("task {{%s}} @@not found@@.\n",
                        a.toString()
                );
            }
        }

    }

    private void runProjectRemove(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            if (service.removeProject(a.toString())) {
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("project {{%s}} removed.\n",
                            a.toString()
                    );
                }
            } else {
                context.getSession().out().printf("project {{%s}} @@not found@@.\n",
                        a.toString()
                );
            }
        }

    }

    private void runJobShow(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            NJob job = service.getJob(a.toString());
            if (job == null) {
                context.getSession().out().printf("<<%s>>: @@not found@@.\n",
                        a.toString()
                );
            } else {
                context.getSession().out().printf("<<%s>>:\n",
                        a.toString()
                );
                context.getSession().out().printf("\t==job name==      : %s:\n", job.getName() == null ? "" : job.getName().toString());
                String project = job.getProject();
                NProject p = service.getProject(project);
                if (project == null || project.length() == 0) {
                    context.getSession().out().printf("\t==project==       : %s\n", "");
                } else {
                    context.getSession().out().printf("\t==project==       : %s (%s)\n", project, (p == null ? "?" : p.getName()));
                }
                context.getSession().out().printf("\t==duration==      : %s:\n", job.getDuration() == null ? "" : job.getDuration().toString());
                context.getSession().out().printf("\t==start time==    : %s:\n", job.getStartTime() == null ? "" : LocalDateTime.ofInstant(job.getStartTime(), ZoneId.systemDefault()));
                context.getSession().out().printf("\t==duration extra==: %s:\n", job.getInternalDuration() == null ? "" : job.getInternalDuration().toString());
                context.getSession().out().printf("\t==observations==  : %s:\n", job.getObservations() == null ? "" : job.getObservations().toString());
            }
        }

    }

    private void runProjectShow(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            NProject project = service.getProject(a.toString());
            if (project == null) {
                context.getSession().out().printf("<<%s>>: @@not found@@.\n",
                        a.toString()
                );
            } else {
                context.getSession().out().printf("<<%s>>:\n",
                        a.toString()
                );
                context.getSession().out().printf("\t==project name==  : %s:\n", project.getName() == null ? "" : project.getName().toString());
                context.getSession().out().printf("\t==beneficiary==   : %s:\n", project.getBeneficiary() == null ? "" : project.getBeneficiary().toString());
                context.getSession().out().printf("\t==company==       : %s:\n", project.getCompany() == null ? "" : project.getCompany().toString());
                context.getSession().out().printf("\t==start time==    : %s:\n", project.getStartTime() == null ? "" : LocalDateTime.ofInstant(project.getStartTime(), ZoneId.systemDefault()));
                context.getSession().out().printf("\t==start week day==: %s:\n", project.getStartWeekDay() == null ? "" : project.getStartWeekDay().toString());
                context.getSession().out().printf("\t==observations==  : %s:\n", project.getObservations() == null ? "" : project.getObservations().toString());
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
                context.getSession().out().printf("\t==task name==     : %s\n", task.getName() == null ? "" : task.getName().toString());
                context.getSession().out().printf("\t==status==        : %s\n", task.getStatus() == null ? "" : task.getStatus().toString());
                context.getSession().out().printf("\t==priority==      : %s\n", task.getPriority() == null ? "" : task.getPriority().toString());
                String project = task.getProject();
                NProject p = service.getProject(project);
                if (project == null || project.length() == 0) {
                    context.getSession().out().printf("\t==project==       : %s\n", "");
                } else {
                    context.getSession().out().printf("\t==project==       : %s (%s)\n", project, (p == null ? "?" : p.getName()));
                }
                context.getSession().out().printf("\t==flag==          : %s\n", task.getFlag() == null ? "" : task.getFlag().toString());
                context.getSession().out().printf("\t==parent id==     : %s\n", task.getParentTaskId() == null ? "" : task.getParentTaskId().toString());
                context.getSession().out().printf("\t==job id==        : %s\n", task.getJobId() == null ? "" : task.getJobId().toString());
                context.getSession().out().printf("\t==due time==      : %s\n", task.getDueTime() == null ? "" : LocalDateTime.ofInstant(task.getDueTime(), ZoneId.systemDefault()));
                context.getSession().out().printf("\t==start time==    : %s\n", task.getStartTime() == null ? "" : LocalDateTime.ofInstant(task.getStartTime(), ZoneId.systemDefault()));
                context.getSession().out().printf("\t==end time==      : %s\n", task.getEndTime() == null ? "" : LocalDateTime.ofInstant(task.getEndTime(), ZoneId.systemDefault()));
                context.getSession().out().printf("\t==duration==      : %s\n", task.getDuration() == null ? "" : task.getDuration().toString());
                context.getSession().out().printf("\t==duration extra==: %s\n", task.getInternalDuration() == null ? "" : task.getInternalDuration().toString());
                context.getSession().out().printf("\t==creation time== : %s\n", task.getCreationTime() == null ? "" : LocalDateTime.ofInstant(task.getCreationTime(), ZoneId.systemDefault()));
                context.getSession().out().printf("\t==modif. time==   : %s\n", task.getModificationTime() == null ? "" : LocalDateTime.ofInstant(task.getModificationTime(), ZoneId.systemDefault()));
                context.getSession().out().printf("\t==observations==  : %s\n", task.getObservations() == null ? "" : task.getObservations());
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
                case "-w": {
                    countType = ChronoUnit.WEEKS;
                    count = cmd.nextString().getIntValue();
                    break;
                }
                case "-m": {
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
                            groupBy = NJobGroup.JOB_NAME;
                            break;
                        }
                        case "s":
                        case "summary": {
                            groupBy = NJobGroup.SUMMARY;
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
                    Predicate<String> sp = createProjectFilter(s);
                    Predicate<NJob> t = x -> sp.test(x.getProject());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-n": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NJob> t = x -> sp.test(x.getName());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-b": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NJob> t = x -> {
                        NProject project = service.getProject(x.getProject());
                        return sp.test(project == null ? "" : project.getBeneficiary());
                    };
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-c": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NJob> t = x -> {
                        NProject project = service.getProject(x.getProject());
                        return sp.test(project == null ? "" : project.getCompany());
                    };
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-d": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<TimePeriod> p = TimePeriod.parseFilter(s, false);
                    Predicate<NJob> t = x -> p.test(x.getDuration());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-t": {
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
            r.forEach(x -> {
                NutsString durationString =ws.str().appendRaw("##",String.valueOf(timeUnit0 == null ? x.getDuration() : x.getDuration().toUnit(timeUnit0, hoursPerDay)))
                .toNutsString();
                m.newRow().addCells(
                        (finalGroupBy != null) ?
                                new Object[]{
                                        getFormattedDate(x.getStartTime()),
                                        durationString,
                                        getFormattedProject(x.getProject() == null ? "*" : x.getProject()),
                                        x.getName()

                                } : new Object[]{
                                ws.str().appendRaw("<<",x.getId()).toNutsString(),
                                getFormattedDate(x.getStartTime()),
                                durationString,
                                getFormattedProject(x.getProject() == null ? "*" : x.getProject()),
                                x.getName()

                        }
                );
            });
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
                case "-w": {
                    countType = ChronoUnit.WEEKS;
                    count = cmd.nextString().getIntValue();
                    break;
                }
                case "-m": {
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
                            groupBy = NJobGroup.JOB_NAME;
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
                case "-n": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NTask> t = x -> sp.test(x.getName());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-b": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NTask> t = x -> {
                        NProject project = service.getProject(x.getProject());
                        return sp.test(project == null ? "" : project.getBeneficiary());
                    };
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-c": {
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
                    Predicate<TimePeriod> p = TimePeriod.parseFilter(s, false);
                    Predicate<NTask> t = x -> p.test(x.getDuration());
                    whereFilter = appendPredicate(whereFilter, t);
                    break;
                }
                case "-t": {
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
            r.forEach(x -> {
                m.newRow().addCells(toTaskRowArray(x));
            });
            ws.formats().table()
                    .setBorder("spaces")
                    .setModel(m).println(context.getSession().out());
        } else {
            context.getSession().formatObject(r.collect(Collectors.toList())).print(context.getSession().out());
        }
    }

    private Object[] toTaskRowArray(NTask x) {
        String project = x.getProject();
        NProject p = project == null ? null : service.getProject(project);
        NTaskStatus s = x.getStatus();
        String dte0 = getFormattedDate(x.getDueTime());
        NutsStringBuilder dte = ws.str();
        if (s == NTaskStatus.CANCELLED || s == NTaskStatus.DONE) {
            dte.appendRaw("<<" ,dte0);
        } else if (x.getDueTime() != null && x.getDueTime().compareTo(Instant.now()) < 0) {
            dte.appendRaw("@@" ,dte0);
        } else {
            dte.appendRaw("##" ,dte0);
        }
        String projectName = p != null ? p.getName() : project != null ? project : "*";
        return new Object[]{
                ws.str().appendRaw("<<",x.getId()),
                getFlagString(x.getFlag()),
                getStatusString(x.getStatus()),
                getPriorityString(x.getPriority()),
                dte.toNutsString(),
                getFormattedProject(projectName),
                x.getName()
        };
    }

    private NutsString getFormattedProject(String projectName) {
        return ws.str().appendHashed(projectName).toNutsString();
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
            return new NutsString("");
        }
        if (x) {
            return new NutsString("\u2611");
        } else {
            return new NutsString("\u25A1");
        }
    }

    private NutsString getPriorityString(NPriority x) {
        if (x == null) {
            return new NutsString("N");
        }
        switch (x) {
            case NONE:
                return new NutsString("<<0>>");
            case LOW:
                return new NutsString("<<L>>");
            case NORMAL:
                return new NutsString("N");
            case MEDIUM:
                return new NutsString("##M##");
            case URGENT:
                return new NutsString("{{U}}");
            case HIGH:
                return new NutsString("**H**");
            case CRITICAL:
                return new NutsString("@@C@@");
        }
        return new NutsString("?");
    }

    private NutsString getStatusString(NTaskStatus x) {
        if (x == null) {
            return new NutsString("*");
        }
        switch (x) {
            case TODO:
//                return new NutsString("\u2B58");
                return new NutsString("\u24c9");
//            case TODO:return new NutsString("\u25A1");
            case DONE:
                return new NutsString("((\u2611))");
//            case WIP:return new NutsString("##\u25B6##");
            case WIP:
                return new NutsString("##\u24CC##");
//                return new NutsString("##\u25F6##");
            case CANCELLED:
//                return new NutsString("@@\u2613@@");
//                return new NutsString("@@\u2421@@");
//                return new NutsString("@@\u26C3@@");
                return new NutsString("@@\u2718@@");
        }
        return new NutsString("?");
    }

    private NutsString getFlagString(NFlag x) {
        if (x == null) {
            x = NFlag.NONE;
        }
        switch (x) {
            case NONE:
                return new NutsString("\u2690");
            case STAR1:
                return new NutsString("==\u2605==");
            case STAR2:
                return new NutsString("{{\u2605}}");
            case STAR3:
                return new NutsString("##\u2605##");
            case STAR4:
                return new NutsString("[[\u2605]]");
            case STAR5:
                return new NutsString("**\u2605**");

            case FLAG1:
                return new NutsString("==\u2691==");
            case FLAG2:
                return new NutsString("{{\u2691}}");
            case FLAG3:
                return new NutsString("##\u2691##");
            case FLAG4:
                return new NutsString("[[\u2691]]");
            case FLAG5:
                return new NutsString("**\u2691**");

            case KING1:
                return new NutsString("==\u265A==");
            case KING2:
                return new NutsString("{{\u265A}}");
            case KING3:
                return new NutsString("##\u265A##");
            case KING4:
                return new NutsString("[[\u265A]]");
            case KING5:
                return new NutsString("**\u265A**");

            case HEART1:
                return new NutsString("==\u2665==");
            case HEART2:
                return new NutsString("{{\u2665}}");
            case HEART3:
                return new NutsString("##\u2665##");
            case HEART4:
                return new NutsString("[[\u2665]]");
            case HEART5:
                return new NutsString("**\u2665**");
            case PHONE1:
                return new NutsString("==\u260E==");
            case PHONE2:
                return new NutsString("{{\u260E}}");
            case PHONE3:
                return new NutsString("##\u260E##");
            case PHONE4:
                return new NutsString("[[\u260E]]");
            case PHONE5:
                return new NutsString("**\u260E**");
        }
        return new NutsString("[" + x.toString().toLowerCase() + "]");
    }

    private void runProjectList(NutsCommandLine cmd) {
        Predicate<NProject> whereFilter = null;
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            switch (a.getStringKey()) {
                case "-c": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = createStringFilter(s);
                    Predicate<NProject> t = x -> sp.test(x.getBeneficiary());
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
            r.forEach(x -> {
                Instant st = x.getStartTime();
                String sts = "";
                if (st != null) {
                    LocalDateTime d = LocalDateTime.ofInstant(st, ZoneId.systemDefault());
                    sts = d.getYear() + " " + d.getMonth().toString().toLowerCase().substring(0, 3);
                }
                m.newRow().addCells(
                        x.getId(),
                        sts,
                        x.getCompany(),
                        x.getBeneficiary(),
                        x.getName());
            });
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
        session.out().println("{{" + context.getAppId().getArtifactId() + " " + context.getAppId().getVersion() + "}} interactive mode. type **q** to quit.");
        InputStream in = session.getTerminal().in();
        Scanner sc = new Scanner(in);
        Exception lastError = null;
        while (true) {
            session.out().print("> ");
            session.out().flush();
            String line = null;
            try {
                line = sc.nextLine();
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
            } else if (line.trim().equals("show-error") || line.trim().equals("show error")) {
                if (lastError != null) {
                    lastError.printStackTrace(session.out());
                }
                break;
            } else {
                NutsCommandLine cmd = ws.commandLine().parse(line);
                cmd.setCommandName(context.getAppId().getArtifactId());
                try {
                    lastError = null;
                    boolean b = runCommands(cmd);
                    if (!b) {
                        session.out().println("@@command not found@@");
                    }
                } catch (Exception ex) {
                    lastError = ex;
                    String m = ex.getMessage();
                    if (m == null) {
                        m = ex.toString();
                    }
                    session.err().printf("@@@error:@@@ @@%s@@\n", m);
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
}

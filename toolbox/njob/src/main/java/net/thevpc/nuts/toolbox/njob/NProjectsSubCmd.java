package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.njob.model.NProject;
import net.thevpc.nuts.toolbox.njob.time.TimeParser;
import net.thevpc.nuts.toolbox.njob.time.WeekDay;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NProjectsSubCmd {

    private JobService service;
    private NutsApplicationContext context;
    private NutsWorkspace ws;
    private JobServiceCmd parent;

    public NProjectsSubCmd(JobServiceCmd parent) {
        this.parent = parent;
        this.context = parent.context;
        this.service = parent.service;
        this.ws = parent.ws;
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
        if (cmd.isExecMode()) {
            service.projects().addProject(t);
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().printf("project %s (%s) added.\n",
                        context.getWorkspace().text().forStyled(t.getId(), NutsTextStyle.primary5()),
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
    }

    public void runProjectUpdate(NutsCommandLine cmd) {
        List<NProject> projects = new ArrayList<>();
        boolean list = false;
        boolean show = false;
        String mergeTo = null;
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
                    if (c.isEnabled()) {
                        if (mergeTo != null) {
                            cmd.pushBack(c);
                            cmd.unexpectedArgument();
                        } else {
                            mergeTo = c.getStringValue();
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
            cmd.throwError(NutsMessage.formatted("project name expected"));
        }
        if (cmd.isExecMode()) {
            NutsTextManager text = context.getWorkspace().text();
            for (NProject project : projects) {
                for (Consumer<NProject> c : runLater) {
                    c.accept(project);
                }
                service.projects().updateProject(project);
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("project %s (%s) updated.\n",
                            text.forStyled(project.getId(), NutsTextStyle.primary5()),
                            text.forStyled(project.getName(), NutsTextStyle.primary1())
                    );
                }
            }
            if (mergeTo != null) {
                service.projects().mergeProjects(mergeTo, projects.stream().map(x -> x.getId()).toArray(String[]::new));
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("projects merged to %s.\n",
                            context.getWorkspace()
                            .text().forStyled(mergeTo, NutsTextStyle.primary5())
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
    }

    private void runProjectList(NutsCommandLine cmd) {
        Predicate<NProject> whereFilter = null;
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            switch (a.getStringKey()) {
                case "-b":
                case "-beneficiary": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = parent.createStringFilter(s);
                    Predicate<NProject> t = x -> sp.test(x.getBeneficiary());
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "-c":
                case "-company": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = parent.createStringFilter(s);
                    Predicate<NProject> t = x -> sp.test(x.getCompany());
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "-n":
                case "--name": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = parent.createStringFilter(s);
                    Predicate<NProject> t = x -> sp.test(x.getName());
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "--unused": {
                    boolean unused = cmd.nextBoolean().getBooleanValue();
                    Predicate<NProject> t = x -> service.projects().isUsedProject(x.getId()) != unused;
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
        if (cmd.isExecMode()) {

            Stream<NProject> r
                    = service.projects().findProjects().filter(whereFilter == null ? x -> true : whereFilter)
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
                            parent.createHashId(index[0], -1),
                            x.getId(),
                            sts,
                            x.getCompany(),
                            x.getBeneficiary(),
                            parent.getFormattedProject(x.getName() == null ? "*" : x.getName())
                    );
                });
                context.getSession().setProperty("LastResults", lastResults.toArray(new NProject[0]));
                ws.formats().table()
                        .setBorder("spaces")
                        .setValue(m).println(context.getSession().out());
            } else {
                context.getSession().getWorkspace().formats().object(r.collect(Collectors.toList())).print(context.getSession().out());
            }
        }
    }

    private void runProjectRemove(NutsCommandLine cmd) {
        NutsTextManager text = context.getWorkspace().text();
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            if (cmd.isExecMode()) {
                NProject t = findProject(a.toString(), cmd);
                if (service.projects().removeProject(t.getId())) {
                    if (context.getSession().isPlainTrace()) {
                        context.getSession().out().printf("project %s removed.\n",
                                text.forStyled(a.toString(), NutsTextStyle.primary5())
                        );
                    }
                } else {
                    context.getSession().out().printf("project %s %s.\n",
                            text.forStyled(a.toString(), NutsTextStyle.primary5()),
                            text.forStyled("not found", NutsTextStyle.error())
                    );
                }
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
                context.getSession().out().printf("\t```kw2 project name```  : %s\n", parent.formatWithPrefix(project.getName(), prefix));
                context.getSession().out().printf("\t```kw2 beneficiary```   : %s\n", parent.formatWithPrefix(project.getBeneficiary(), prefix));
                context.getSession().out().printf("\t```kw2 company```       : %s\n", parent.formatWithPrefix(project.getCompany(), prefix));
                context.getSession().out().printf("\t```kw2 start time```    : %s\n", parent.formatWithPrefix(project.getStartTime(), prefix));
                context.getSession().out().printf("\t```kw2 start week day```: %s\n", parent.formatWithPrefix(project.getStartWeekDay(), prefix));
                context.getSession().out().printf("\t```kw2 observations```  : %s\n", parent.formatWithPrefix(project.getObservations(), prefix));
            }
        }

    }

    private NProject findProject(String pid, NutsCommandLine cmd) {
        NProject t = null;
        if (pid.startsWith("#")) {
            int x = parent.parseIntOrFF(pid.substring(1));
            if (x >= 1) {
                Object lastResults = context.getSession().getProperty("LastResults");
                if (lastResults instanceof NProject[] && x <= ((NProject[]) lastResults).length) {
                    t = ((NProject[]) lastResults)[x - 1];
                }
            }
        }
        if (t == null) {
            t = service.projects().getProject(pid);
        }
        if (t == null) {
            cmd.throwError(NutsMessage.cstyle("project not found: %s", pid));
        }
        return t;
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
        } else if (cmd.next("rp", "rmp", "pr", "prm", "r p", "rm p", "p r", "p rm", "remove project", "remove projects", "rm project", "rm projects", "projects remove") != null) {
            runProjectRemove(cmd);
            return true;
        } else if (cmd.next("ps", "sp", "s p", "p s", "show project", "show projects", "projects show") != null) {
            runProjectShow(cmd);
            return true;
        } else if (cmd.next("p", "projects") != null) {
            if (cmd.next("--help") != null) {
                parent.showCustomHelp("njob-projects");
            } else {
                runProjectList(cmd);
            }
            return true;
        }
        return false;
    }

}

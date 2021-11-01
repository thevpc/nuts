package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.njob.model.*;
import net.thevpc.nuts.toolbox.njob.time.TimeFormatter;

import java.io.InputStream;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JobServiceCmd {

    protected JobService service;
    protected NutsApplicationContext context;
    protected NutsSession session;
    private NJobsSubCmd jobs;
    private NTasksSubCmd tasks;
    private NProjectsSubCmd projects;

    public JobServiceCmd(NutsApplicationContext context) {
        this.context = context;
        this.service = new JobService(context);
        session = context.getSession();
        jobs = new NJobsSubCmd(this);
        tasks = new NTasksSubCmd(this);
        projects = new NProjectsSubCmd(this);
    }

    protected static String formatWithPrefix(Object value, String prefix) {
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

    public boolean runCommands(NutsCommandLine cmd) {
        if (projects.runProjectCommands(cmd)) {
            return true;
        }
        if (jobs.runJobCommands(cmd)) {
            return true;
        }
        if (tasks.runTaskCommands(cmd)) {
            return true;
        }
        if (cmd.next("summary") != null) {
            runSummary(cmd);
            return true;
        } else if (cmd.next("help") != null) {
            for (String s : new String[]{"jobs", "projects", "tasks"}) {
                if (cmd.isExecMode()) {
                    showCustomHelp("njob-" + s);
                }
                return true;
            }
            if (cmd.isExecMode()) {
                showCustomHelp("njob");
            }
            return true;
        }
        return false;
    }

    private void runSummary(NutsCommandLine cmd) {
        if (cmd.isExecMode()) {
            long projectsCount = service.projects().findProjects().count();
            long tasksCount = service.tasks().findTasks(NTaskStatusFilter.OPEN, null, -1, null, null, null, null, null).count();
            long jobsCount = service.jobs().findMonthJobs(null).count();
            long allJobsCount = service.jobs().findLastJobs(null, -1, null, null, null, null, null).count();
            NutsTexts text = NutsTexts.of(context.getSession());
            context.getSession().out().printf("%s open task%s\n", text.ofStyled("" + tasksCount, NutsTextStyle.primary1()), tasksCount == 1 ? "" : "s");
            context.getSession().out().printf("%s job%s %s\n", text.ofStyled("" + allJobsCount, NutsTextStyle.primary1()), allJobsCount == 1 ? "" : "s",
                    allJobsCount == 0 ? ""
                            : text.builder()
                            .append("(")
                            .append("" + jobsCount, NutsTextStyle.primary1())
                            .append(" this month)")
            );
            context.getSession().out().printf("%s project%s\n", text.ofStyled("" + projectsCount, NutsTextStyle.primary1()), projectsCount == 1 ? "" : "s");
        }
    }

    protected void showCustomHelp(String name) {
        NutsTexts text = NutsTexts.of(context.getSession());
        context.getSession().out().println(text.parser().parseResource("/net/thevpc/nuts/toolbox/" + name + ".ntf",
                text.parser().createLoader(getClass().getClassLoader())
        ));
    }

    protected NutsString getFormattedProject(String projectName) {
        NutsTextBuilder builder = NutsTexts.of(session).builder();
        builder.getStyleGenerator()
                .setIncludeForeground(true)
                .setUsePaletteColors();
        return builder.appendHash(projectName).immutable();
    }

    protected String getFormattedDate(Instant x) {
        if (x == null) {
            return "?";
        }
        return new TimeFormatter().format(x.atZone(ZoneId.systemDefault()).toLocalDateTime());
//        String s = x.atZone(ZoneId.systemDefault()).toString() + " " +
//                x.atZone(ZoneId.systemDefault()).getDayOfWeek().toString().toLowerCase().substring(0, 3);
//        return s;
    }

    protected NutsString getCheckedString(Boolean x) {
        if (x == null) {
            return NutsTexts.of(context.getSession()).ofPlain("");
        }
        if (x) {
            return NutsTexts.of(context.getSession()).ofPlain("\u2611");
        } else {
            return NutsTexts.of(context.getSession()).ofPlain("\u25A1");
        }
    }

    protected NutsString getPriorityString(NPriority x) {
        if (x == null) {
            return NutsTexts.of(context.getSession()).ofPlain("N");
        }
        switch (x) {
            case NONE:
                return NutsTexts.of(session).ofStyled("0", NutsTextStyle.pale());
            case LOW:
                return NutsTexts.of(session).ofStyled("L", NutsTextStyle.pale());
            case NORMAL:
                return NutsTexts.of(session).ofPlain("N");
            case MEDIUM:
                return NutsTexts.of(session).ofStyled("M", NutsTextStyle.primary1());
            case URGENT:
                return NutsTexts.of(session).ofStyled("U", NutsTextStyle.primary2());
            case HIGH:
                return NutsTexts.of(session).ofStyled("H", NutsTextStyle.primary3());
            case CRITICAL:
                return NutsTexts.of(session).ofStyled("C", NutsTextStyle.fail());
        }
        return NutsTexts.of(context.getSession()).ofPlain("?");
    }

    protected NutsString getStatusString(NTaskStatus x) {
        NutsTexts text = NutsTexts.of(context.getSession());
        if (x == null) {
            return text.ofPlain("*");
        }
        switch (x) {
            case TODO:
                return text.ofPlain("\u24c9");
            case DONE:
                return text.ofStyled("\u2611", NutsTextStyle.success());
            case WIP:
                return text.ofStyled("\u24CC", NutsTextStyle.primary1());
            case CANCELLED:
                return text.ofStyled("\u2718", NutsTextStyle.fail());
        }
        return text.ofPlain("?");
    }

    private NutsString getFlagString(String x, int index) {
        switch (index) {
            case 1:
                return NutsTexts.of(session).ofStyled(x, NutsTextStyle.primary1());
            case 2:
                return NutsTexts.of(session).ofStyled(x, NutsTextStyle.primary2());
            case 3:
                return NutsTexts.of(session).ofStyled(x, NutsTextStyle.primary3());
            case 4:
                return NutsTexts.of(session).ofStyled(x, NutsTextStyle.primary4());
            case 5:
                return NutsTexts.of(session).ofStyled(x, NutsTextStyle.primary5());
        }
        throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("invalid index %s", index));
    }

    protected NutsString getFlagString(NFlag x) {
        if (x == null) {
            x = NFlag.NONE;
        }
        switch (x) {
            case NONE:
                return NutsTexts.of(context.getSession()).ofPlain("\u2690");

            case STAR1:
                return getFlagString("\u2605", 1);
            case STAR2:
                return getFlagString("\u2605", 2);
            case STAR3:
                return getFlagString("\u2605", 3);
            case STAR4:
                return getFlagString("\u2605", 4);
            case STAR5:
                return getFlagString("\u2605", 5);

            case FLAG1:
                return getFlagString("\u2691", 1);
            case FLAG2:
                return getFlagString("\u2691", 2);
            case FLAG3:
                return getFlagString("\u2691", 3);
            case FLAG4:
                return getFlagString("\u2691", 4);
            case FLAG5:
                return getFlagString("\u2691", 5);

            case KING1:
                return getFlagString("\u265A", 1);
            case KING2:
                return getFlagString("\u265A", 2);
            case KING3:
                return getFlagString("\u265A", 3);
            case KING4:
                return getFlagString("\u265A", 4);
            case KING5:
                return getFlagString("\u265A", 5);

            case HEART1:
                return getFlagString("\u2665", 1);
            case HEART2:
                return getFlagString("\u2665", 2);
            case HEART3:
                return getFlagString("\u2665", 3);
            case HEART4:
                return getFlagString("\u2665", 4);
            case HEART5:
                return getFlagString("\u2665", 5);

            case PHONE1:
                return getFlagString("\u260E", 1);
            case PHONE2:
                return getFlagString("\u260E", 2);
            case PHONE3:
                return getFlagString("\u260E", 3);
            case PHONE4:
                return getFlagString("\u260E", 4);
            case PHONE5:
                return getFlagString("\u260E", 5);
        }
        return NutsTexts.of(context.getSession()).ofPlain("[" + x.toString().toLowerCase() + "]");
    }

    protected <T> Predicate<T> appendPredicate(Predicate<T> whereFilter, Predicate<T> t) {
        if (whereFilter == null) {
            whereFilter = t;
        } else {
            whereFilter = whereFilter.and(t);
        }
        return whereFilter;
    }

    protected Predicate<String> createStringFilter(String s) {
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
        NutsSystemTerminal.enableRichTerm(session);
        session.config().getSystemTerminal()
                .setCommandAutoCompleteResolver(new JobAutoCompleter())
                .setCommandHistory(
                        NutsCommandHistory.of(session)
                                .setPath(Paths.get(context.getVarFolder()).resolve("njob-history.hist"))
                )
                .setCommandReadHighlighter(new NutsCommandReadHighlighter() {
                    @Override
                    public NutsText highlight(String buffer, NutsSession session) {
                        return NutsTexts.of(session).ofCode("sh", buffer).highlight(session);
                    }
                });
        session.env().setProperty(JobServiceCmd.class.getName(), this);

//        session.setTerminal(
//                session.io().term().createTerminal(
//                session.io().term().getSystemTerminal(), 
//                        session
//        ));
        NutsTexts text = NutsTexts.of(session);

        session.out().printf(
                "%s interactive mode. type %s to quit.%n",
                text.ofStyled(context.getAppId().getArtifactId() + " " + context.getAppId().getVersion(), NutsTextStyle.primary1()),
                text.ofStyled("q", NutsTextStyle.error())
        );
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
                    lastError.printStackTrace(session.out().asPrintStream());
                }
            } else {
                NutsCommandLine cmd = NutsCommandLine.parse(line,this.session);
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
                NProject y = service.projects().getProject(x);
                return y != null && sp.test(y.getName());
            };
        }
    }

    protected String createHashId(int value, int maxValues) {
//        DecimalFormat decimalFormat = new DecimalFormat("00");
//        return "#"+decimalFormat.format(value);
        return "#" + value;
    }
}

package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.toolbox.njob.time.WeekDay;

import java.time.Instant;
import java.util.*;

public class JobService {
    private NApplicationContext context;
    private NJobConfigStore dal;
    private NJobsSubService jobs;
    private NTasksSubService tasks;
    private NProjectsSubService projects;

    public JobService(NApplicationContext context) {
        this.context = context;
        this.dal = new NJobConfigStore(context);
        this.jobs = new NJobsSubService(context, dal, this);
        this.tasks = new NTasksSubService(context, dal, this);
        this.projects = new NProjectsSubService(context, dal, this);
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

    public NJobsSubService jobs() {
        return jobs;
    }

    public NTasksSubService tasks() {
        return tasks;
    }

    public NProjectsSubService projects() {
        return projects;
    }

    public boolean isIdFormat(String s) {
        return s != null && s.matches("[0-9a-fA-F-]{36}");
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

}

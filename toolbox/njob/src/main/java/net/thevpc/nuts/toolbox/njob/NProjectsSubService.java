package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.toolbox.njob.model.NProject;
import net.thevpc.nuts.toolbox.njob.time.WeekDay;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NProjectsSubService {
    private NutsApplicationContext context;
    private NJobConfigStore dal;
    private JobService service;

    public NProjectsSubService(NutsApplicationContext context, NJobConfigStore dal, JobService service) {
        this.context = context;
        this.dal = dal;
        this.service = service;
    }

    public void addProject(NProject p) {
        String name = p.getName();
        if (name == null) {
            throw new NutsIllegalArgumentException(context.getSession(),"invalid project");
        }
        p.setId(null);
        NProject p0 = getProject(name);
        if (p0 != null) {
            throw new NutsIllegalArgumentException(context.getSession(), "project already exists: " + name);
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
        dal.store(p);
    }

    public Stream<NProject> findAllProjects() {
        return dal.search(NProject.class);
    }

    public void updateProject(NProject p) {
        String name = p.getName();
        if (name == null) {
            throw new NutsIllegalArgumentException(context.getSession(),"Invalid project");
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
        dal.store(p);
    }

    public NProject getProjectOrError(String projectNameOrId) {
        NProject p = getProject(projectNameOrId);
        if (p == null) {
            throw new NutsIllegalArgumentException(context.getSession(),"project not found " + projectNameOrId);
        }
        return p;
    }

    public NProject getProject(String projectNameOrId) {
        if (service.isIdFormat(projectNameOrId)) {
            return dal.load(NProject.class, projectNameOrId);
        } else {
            return findAllProjects().filter(x -> Objects.equals(x.getName(), projectNameOrId))
                    .findFirst().orElse(null);
        }
    }


    public boolean removeProject(String projectName) {
        long countJobs = service.jobs().findAllJobs().filter(x -> projectName.equals(x.getProject())).count();
        long countTasks = service.tasks().findAllTasks().filter(x -> projectName.equals(x.getProject())).count();
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
            throw new NutsIllegalArgumentException(context.getSession(),"Project is used in " + sb + ". It cannot be removed.");
        }
        return dal.delete(NProject.class, projectName);
    }
    public boolean isUsedProject(String id) {
        NProject destinationJob = getProject(id);
        if (destinationJob == null) {
            return false;
        }
        if (service.tasks().findAllTasks().filter(x -> Objects.equals(x.getProject(), destinationJob.getId()) || Objects.equals(x.getProject(), destinationJob.getName()))
                .findFirst().orElse(null) != null) {
            return true;
        }
        if (service.jobs().findAllJobs().filter(x -> Objects.equals(x.getProject(), destinationJob.getId()) || Objects.equals(x.getProject(), destinationJob.getName()))
                .findFirst().orElse(null) != null) {
            return true;
        }
        return false;
    }

    public void mergeProjects(String destination, String... others) {
        NProject destinationJob = getProjectOrError(destination);
        List<NProject> src = Arrays.asList(others).stream().map(x -> getProjectOrError(x)).collect(Collectors.toList());
        for (NProject s : src) {
            service.tasks().findAllTasks().filter(x -> Objects.equals(x.getProject(), s.getId()) || Objects.equals(x.getProject(), s.getName()))
                    .forEach(
                            t -> {
                                t.setProject(destinationJob.getId());
                                service.tasks().updateTask(t);
                            }
                    );

            service.jobs().findAllJobs().filter(x -> Objects.equals(x.getProject(), s.getId()) || Objects.equals(x.getProject(), s.getName()))
                    .forEach(
                            t -> {
                                t.setProject(destinationJob.getId());
                                service.jobs().updateJob(t);
                            }
                    );
            removeProject(s.getId());
        }
    }

    public Stream<NProject> findProjects() {
        return findAllProjects();
    }

}

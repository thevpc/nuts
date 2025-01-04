package net.thevpc.nuts.runtime.standalone.workspace.cmd;


import java.util.ArrayList;
import java.util.List;

public class NRepositoryAndFetchModeTracker {
    private List<NRepositoryAndFetchMode> possibilities = new ArrayList<>();
    private List<NRepositoryAndFetchMode> failed = new ArrayList<>();

//    public NutsRepositoryAndFetchModeTracker(NSession session, NutsFetchStrategy nutsFetchModes, InstalledVsNonInstalledSearch installedVsNonInstalledSearch) {
//        NutsRepositoryAndFetchModeTracker descTracker=new NutsRepositoryAndFetchModeTracker();
//        for (NutsFetchMode mode : nutsFetchModes) {
//            for (NutsRepository repo : NutsWorkspaceUtils.of(session.getWorkspace())
//                    .filterRepositories(NutsRepositorySupportedAction.SEARCH,
//                            id, repositoryFilter,
//                    mode, session, installedVsNonInstalledSearch)) {
//                descTracker.addPossibility(new NutsRepositoryAndFetchMode(repo,mode));
//            }
//        }
//    }

    public NRepositoryAndFetchModeTracker() {

    }

    public NRepositoryAndFetchModeTracker(List<NRepositoryAndFetchMode> possibilities) {
        this.possibilities.addAll(possibilities);
    }

    public void addPossibility(NRepositoryAndFetchMode a) {
        this.possibilities.add(a);
    }

    public void addFailure(NRepositoryAndFetchMode a) {
        this.failed.add(a);
    }

    public boolean accept(NRepositoryAndFetchMode a) {
        return possibilities.contains(a) && !failed.contains(a);
    }

    public List<NRepositoryAndFetchMode> all() {
        return possibilities;
    }

    public List<NRepositoryAndFetchMode> available() {
        List<NRepositoryAndFetchMode> possibilities2 = new ArrayList<>(possibilities);
        possibilities2.removeAll(failed);
        return possibilities2;
    }
}

package net.thevpc.nuts.runtime.standalone.workspace.cmd;


import java.util.ArrayList;
import java.util.List;

public class NutsRepositoryAndFetchModeTracker {
    private List<NutsRepositoryAndFetchMode> possibilities = new ArrayList<>();
    private List<NutsRepositoryAndFetchMode> failed = new ArrayList<>();

//    public NutsRepositoryAndFetchModeTracker(NutsSession session, NutsFetchStrategy nutsFetchModes, InstalledVsNonInstalledSearch installedVsNonInstalledSearch) {
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

    public NutsRepositoryAndFetchModeTracker() {

    }

    public NutsRepositoryAndFetchModeTracker(List<NutsRepositoryAndFetchMode> possibilities) {
        this.possibilities.addAll(possibilities);
    }

    public void addPossibility(NutsRepositoryAndFetchMode a) {
        this.possibilities.add(a);
    }

    public void addFailure(NutsRepositoryAndFetchMode a) {
        this.failed.add(a);
    }

    public boolean accept(NutsRepositoryAndFetchMode a) {
        return possibilities.contains(a) && !failed.contains(a);
    }

    public List<NutsRepositoryAndFetchMode> all() {
        return possibilities;
    }

    public List<NutsRepositoryAndFetchMode> available() {
        List<NutsRepositoryAndFetchMode> possibilities2 = new ArrayList<>(possibilities);
        possibilities2.removeAll(failed);
        return possibilities2;
    }
}

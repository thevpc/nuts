package net.vpc.app.nuts;

/**
 * Created by vpc on 6/23/17.
 */
public class NutsUpdate {
    NutsId baseId;
    NutsId localId;
    NutsId availableId;

    public NutsUpdate(NutsId baseId, NutsId localId, NutsId availableId) {
        this.baseId = baseId;
        this.localId = localId;
        this.availableId = availableId;
    }

    public NutsId getBaseId() {
        return baseId;
    }

    public NutsId getLocalId() {
        return localId;
    }

    public NutsId getAvailableId() {
        return availableId;
    }
}

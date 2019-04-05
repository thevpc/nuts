package net.vpc.app.nuts.core.util;

import net.vpc.app.nuts.NutsRepository;

import java.util.Comparator;

public class SpeedRepositoryComparator implements Comparator<NutsRepository> {

    public static final SpeedRepositoryComparator INSTANCE = new SpeedRepositoryComparator();

    private SpeedRepositoryComparator() {
    }

    @Override
    public int compare(NutsRepository o1, NutsRepository o2) {
        return Integer.compare(o1.config().getSpeed(true), o2.config().getSpeed(true));
    }
}

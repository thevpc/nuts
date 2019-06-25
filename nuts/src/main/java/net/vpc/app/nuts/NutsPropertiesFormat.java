package net.vpc.app.nuts;

import java.util.Map;

public interface NutsPropertiesFormat extends NutsFormat {

    NutsPropertiesFormat model(Map map);

    NutsPropertiesFormat setModel(Map map);

    Map getModel();

    boolean isSort();

    String getSeparator();

    NutsPropertiesFormat separator(String separator);

    NutsPropertiesFormat setSeparator(String separator);

    NutsPropertiesFormat sort();

    NutsPropertiesFormat sort(boolean sort);

    NutsPropertiesFormat setSort(boolean sort);

    @Override
    NutsPropertiesFormat session(NutsSession session);

    @Override
    NutsPropertiesFormat setSession(NutsSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsPropertiesFormat configure(boolean skipUnsupported, String... args);
}

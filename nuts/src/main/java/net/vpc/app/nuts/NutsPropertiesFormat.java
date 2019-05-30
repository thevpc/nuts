package net.vpc.app.nuts;

import java.util.Map;

public interface NutsPropertiesFormat extends NutsFormat {

    @Override
    NutsPropertiesFormat terminalFormat(NutsTerminalFormat metric);

    @Override
    NutsPropertiesFormat setTerminalFormat(NutsTerminalFormat metric);

    @Override
    NutsPropertiesFormat session(NutsSession session);

    @Override
    NutsPropertiesFormat setSession(NutsSession session);

    @Override
    NutsPropertiesFormat configure(String ... args);

    NutsPropertiesFormat model(Map map);
    NutsPropertiesFormat setModel(Map map);

    Map getModel();

    boolean isSort();

//    boolean isTable();
//
//    NutsPropertiesFormat table();
//    NutsPropertiesFormat table(boolean table);
//    NutsPropertiesFormat setTable(boolean table);

    String getSeparator();

    NutsPropertiesFormat separator(String separator);
    NutsPropertiesFormat setSeparator(String separator);

    NutsPropertiesFormat sort();
    NutsPropertiesFormat sort(boolean sort);
    NutsPropertiesFormat setSort(boolean sort);

}

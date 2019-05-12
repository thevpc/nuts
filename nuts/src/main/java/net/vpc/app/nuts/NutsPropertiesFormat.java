package net.vpc.app.nuts;

import java.io.PrintStream;
import java.util.Map;

public interface NutsPropertiesFormat {
    
    boolean isSort() ;

    boolean isTable() ;

    public NutsPropertiesFormat setTable(boolean table);

    public String getSeparator() ;

    public NutsPropertiesFormat setSeparator(String separator);

    NutsPropertiesFormat setSort(boolean sort);

    void format(Map map, PrintStream out);

    
}

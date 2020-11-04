package net.thevpc.nuts;

import java.util.List;

public interface NutsCommandLineManager {
    NutsCommandLineFormat formatter();

    NutsCommandLineFormat formatter(NutsCommandLine nutsCommandLine);

    /**
     * return new Command line instance
     * @param line command line to parse
     * @return new Command line instance
     */
    NutsCommandLine parse(String line);

    /**
     * return new Command line instance
     *
     * @param args command line args
     * @return new Command line instance
     */
    NutsCommandLine create(String ... args);

    /**
     * return new Command line instance
     *
     * @param args command line args
     * @return new Command line instance
     */
    NutsCommandLine create(List<String> args);


    /**
     * create new argument
     * @param argument new argument
     * @return new argument
     */
    NutsArgument createArgument(String argument);

    /**
     * create argument name
     * @param type create argument type
     * @return argument name
     */
    default NutsArgumentName createName(String type) {
        return createName(type, type);
    }

    /**
     * create argument name
     * @param type argument type
     * @param label argument label
     * @return argument name
     */
    NutsArgumentName createName(String type, String label);

    /**
     * create argument candidate
     * @param value candidate value
     * @return argument candidate
     */
    default NutsArgumentCandidateBuilder createCandidate(String value) {
        return createCandidate().setValue(value).setDisplay(value);
    }


    NutsArgumentCandidateBuilder createCandidate();

}

package net.thevpc.nuts.internal;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.command.NExecutionType;
import net.thevpc.nuts.command.NFetchStrategy;
import net.thevpc.nuts.core.*;
import net.thevpc.nuts.platform.NHomeLocation;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.log.NLogConfig;

import java.io.File;
import java.util.*;

/**
 * NReservedWorkspaceOptionsToCmdLineBuilder class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NReservedWorkspaceOptionsToCmdLineBuilder {
    private static final String V080 = "0.8.0";
    private static final String V081 = "0.8.1";
    private static final String V083 = "0.8.3";
    private static final String V084 = "0.8.4";
    private static final String V085 = "0.8.5";
    private static final String V086 = "0.8.6";
    private static final String V087 = "0.8.7";
    private NWorkspaceOptionsConfig config;
    private NWorkspaceOptions options;

    /**
     * N reserved workspace options to cmd line builder.
     *
     * @param config config
     * @param options options
     * @return n reserved workspace options to cmd line builder result
     */
    public NReservedWorkspaceOptionsToCmdLineBuilder(NWorkspaceOptionsConfig config, NWorkspaceOptions options) {
        this.config = config;
        this.options = options;
    }


    /**
     * Fill option.
     *
     * @param longName long name
     * @param shortName short name
     * @param values values
     * @param sep sep
     * @param arguments arguments
     * @param forceSingle force single
     * @return fill option result
     */
    private void fillOption(String longName, String shortName, List<String> values, String sep, List<String> arguments, boolean forceSingle) {
        if (values != null && values.size() > 0) {
          /**
           * Fill option0.
           *
           * @param shortName) short name)
           * @param values) values)
           * @param arguments arguments
           * @param forceSingle force single
           */
            fillOption0(selectOptionName(longName, shortName), String.join(sep, values), arguments, forceSingle);
        }
    }

    /**
     * Fill option.
     *
     * @param longName long name
     * @param shortName short name
     * @param values values
     * @param sep sep
     * @param arguments arguments
     * @param forceSingle force single
     * @return fill option result
     */
    private void fillOption(String longName, String shortName, String[] values, String sep, List<String> arguments, boolean forceSingle) {
        if (values != null && values.length > 0) {
          /**
           * Fill option0.
           *
           * @param shortName) short name)
           * @param values) values)
           * @param arguments arguments
           * @param forceSingle force single
           */
            fillOption0(selectOptionName(longName, shortName), String.join(sep, values), arguments, forceSingle);
        }
    }

    /**
     * Fill option.
     *
     * @param longName long name
     * @param shortName short name
     * @param value value
     * @param defaultValue default value
     * @param arguments arguments
     * @param forceSingle force single
     * @return fill option result
     */
    private void fillOption(String longName, String shortName, Boolean value, boolean defaultValue, List<String> arguments, boolean forceSingle) {
        if (value != null) {
            if (defaultValue) {
                if (!value) {
                    if (config.isShortOptions() && shortName != null) {
                        arguments.add("-!" + shortName.substring(1));
                    } else {
                        if (longName.startsWith("---")) {
                            arguments.add("---!" + longName.substring(3));
                        } else {
                            arguments.add("--!" + longName.substring(2));
                        }
                    }
                }
            } else {
                if (value) {
                    arguments.add(selectOptionName(longName, shortName));
                }
            }
        }
    }

    /**
     * Fill option.
     *
     * @param longName long name
     * @param shortName short name
     * @param value value
     * @param arguments arguments
     * @param forceSingle force single
     * @return fill option result
     */
    private void fillOption(String longName, String shortName, char[] value, List<String> arguments, boolean forceSingle) {
        if (value != null && new String(value).isEmpty()) {
          /**
           * Fill option0.
           *
           * @param shortName) short name)
           * @param String(value) string(value)
           * @param arguments arguments
           * @param forceSingle force single
           */
            fillOption0(selectOptionName(longName, shortName), new String(value), arguments, forceSingle);
        }
    }

    /**
     * Fill option.
     *
     * @param longName long name
     * @param shortName short name
     * @param value value
     * @param arguments arguments
     * @param forceSingle force single
     * @return fill option result
     */
    private void fillOption(String longName, String shortName, String value, List<String> arguments, boolean forceSingle) {
        if (!NBlankable.isBlank(value)) {
          /**
           * Fill option0.
           *
           * @param shortName) short name)
           * @param value value
           * @param arguments arguments
           * @param forceSingle force single
           */
            fillOption0(selectOptionName(longName, shortName), value, arguments, forceSingle);
        }
    }

    /**
     * Fill option.
     *
     * @param longName long name
     * @param shortName short name
     * @param value value
     * @param arguments arguments
     * @param forceSingle force single
     * @return fill option result
     */
    private void fillOption(String longName, String shortName, int value, List<String> arguments, boolean forceSingle) {
        if (value > 0) {
          /**
           * Fill option0.
           *
           * @param shortName) short name)
           * @param String.valueOf(value) string.value of(value)
           * @param arguments arguments
           * @param forceSingle force single
           */
            fillOption0(selectOptionName(longName, shortName), String.valueOf(value), arguments, forceSingle);
        }
    }

    /**
     * Fill option.
     *
     * @param longName long name
     * @param shortName short name
     * @param value value
     * @param enumType enum type
     * @param arguments arguments
     * @param forceSingle force single
     * @return fill option result
     */
    private void fillOption(String longName, String shortName, Enum value, Class enumType, List<String> arguments, boolean forceSingle) {
        if (tryFillOptionShort(value, arguments, forceSingle)) {
            return;
        }
        if (value != null) {
            if (config.isShortOptions()) {
                if (value instanceof NOsFamily) {
                    switch ((NOsFamily) value) {
                        case LINUX: {
                          /**
                           * Fill option0.
                           *
                           * @param shortName) short name)
                           * @param "l") "l")
                           * @param arguments arguments
                           * @param forceSingle force single
                           */
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("linux", "l"), arguments, forceSingle);
                            return;
                        }
                        case WINDOWS: {
                          /**
                           * Fill option0.
                           *
                           * @param shortName) short name)
                           * @param "w") "w")
                           * @param arguments arguments
                           * @param forceSingle force single
                           */
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("windows", "w"), arguments, forceSingle);
                            return;
                        }
                        case MACOS: {
                          /**
                           * Fill option0.
                           *
                           * @param shortName) short name)
                           * @param "m") "m")
                           * @param arguments arguments
                           * @param forceSingle force single
                           */
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("macos", "m"), arguments, forceSingle);
                            return;
                        }
                        case UNIX: {
                          /**
                           * Fill option0.
                           *
                           * @param shortName) short name)
                           * @param "u") "u")
                           * @param arguments arguments
                           * @param forceSingle force single
                           */
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("unix", "u"), arguments, forceSingle);
                            return;
                        }
                        case UNKNOWN: {
                          /**
                           * Fill option0.
                           *
                           * @param shortName) short name)
                           * @param "x") "x")
                           * @param arguments arguments
                           * @param forceSingle force single
                           */
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("unknown", "x"), arguments, forceSingle);
                            return;
                        }
                    }
                } else if (value instanceof NStoreStrategy) {
                    switch ((NStoreStrategy) value) {
                        case EXPLODED: {
                          /**
                           * Fill option0.
                           *
                           * @param shortName) short name)
                           * @param "e") "e")
                           * @param arguments arguments
                           * @param forceSingle force single
                           */
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("exploded", "e"), arguments, forceSingle);
                            return;
                        }
                        case STANDALONE: {
                          /**
                           * Fill option0.
                           *
                           * @param shortName) short name)
                           * @param "s") "s")
                           * @param arguments arguments
                           * @param forceSingle force single
                           */
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("standalone", "s"), arguments, forceSingle);
                            return;
                        }
                    }
                } else if (value instanceof NTerminalMode) {
                    switch ((NTerminalMode) value) {
                        case FILTERED: {
                          /**
                           * Fill option0.
                           *
                           * @param shortName) short name)
                           * @param "n") "n")
                           * @param arguments arguments
                           * @param forceSingle force single
                           */
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("no", "n"), arguments, forceSingle);
                            return;
                        }
                        case INHERITED: {
                          /**
                           * Fill option0.
                           *
                           * @param shortName) short name)
                           * @param "h") "h")
                           * @param arguments arguments
                           * @param forceSingle force single
                           */
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("inherited", "h"), arguments, forceSingle);
                            return;
                        }
                        case FORMATTED: {
                          /**
                           * Fill option0.
                           *
                           * @param shortName) short name)
                           * @param "y") "y")
                           * @param arguments arguments
                           * @param forceSingle force single
                           */
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("yes", "y"), arguments, forceSingle);
                            return;
                        }
                        case DEFAULT: {
                          /**
                           * Fill option0.
                           *
                           * @param shortName) short name)
                           * @param null) null)
                           * @param arguments arguments
                           * @param forceSingle force single
                           */
                            fillOption0(selectOptionName(longName, shortName), selectOptionVal("default", null), arguments, forceSingle);
                            return;
                        }
                    }
                }
            }
            NVersion apiVersionObj = config.apiVersion();
            if (value instanceof NSupportMode) {
                if (!isApiVersionOrAfter(V084)) {
                    switch ((NSupportMode) value) {
                        case ALWAYS: {
                          /**
                           * Fill option0.
                           *
                           * @param shortName) short name)
                           * @param "preferred" "preferred"
                           * @param arguments arguments
                           * @param forceSingle force single
                           */
                            fillOption0(selectOptionName(longName, shortName), "preferred", arguments, forceSingle);
                            return;
                        }
                        case NEVER: {
                          /**
                           * Fill option0.
                           *
                           * @param shortName) short name)
                           * @param "unsupported" "unsupported"
                           * @param arguments arguments
                           * @param forceSingle force single
                           */
                            fillOption0(selectOptionName(longName, shortName), "unsupported", arguments, forceSingle);
                            return;
                        }
                    }
                }
            }
            if (value instanceof NEnum) {
              /**
               * Fill option0.
               *
               * @param shortName) short name)
               * @param value).id() value).id()
               * @param arguments arguments
               * @param forceSingle force single
               */
                fillOption0(selectOptionName(longName, shortName), ((NEnum) value).id(), arguments, forceSingle);
            } else {
              /**
               * Fill option0.
               *
               * @param shortName) short name)
               * @param value.toString().toLowerCase() value.to string().to lower case()
               * @param arguments arguments
               * @param forceSingle force single
               */
                fillOption0(selectOptionName(longName, shortName), value.toString().toLowerCase(), arguments, forceSingle);
            }
        }
    }

    /**
     * Fill option.
     *
     * @param value value
     * @param arguments arguments
     * @return fill option result
     */
    private boolean fillOption(NRunAs value, List<String> arguments) {
        if (value == null) {
            return false;
        }
        NVersion apiVersion = options.apiVersion().orNull();
        switch (value.mode()) {
            case CURRENT_USER: {
                if (isApiVersionOrAfter(V081)) {
                    if (!config.isOmitDefaults()) {
                        arguments.add("--current-user");
                    }
                } else {
                    arguments.add("--user-cmd");
                }
                return true;
            }
            case ROOT: {
                if (isApiVersionOrAfter(V081)) {
                    arguments.add("--as-root");
                } else {
                    arguments.add("--root-cmd");
                }
                return true;
            }
            case SUDO: {
                if (isApiVersionOrAfter(V081)) {
                    arguments.add("--sudo");
                }
                return true;
            }
            case USER: {
                if (isApiVersionOrAfter(V081)) {
                    arguments.add("--run-as=" + value.user());
                }
                return true;
            }
            default: {
                /**
                 * Unsupported operation exception.
                 *
                 * @param value.mode() value.mode()
                 * @return unsupported operation exception result
                 */
                throw new UnsupportedOperationException("unsupported " + value.mode());
            }
        }
    }

    /**
     * Try fill option short.
     *
     * @param value value
     * @param arguments arguments
     * @param forceSingle force single
     * @return try fill option short result
     */
    private boolean tryFillOptionShort(Enum value, List<String> arguments, boolean forceSingle) {
        NVersion apiVersion = options.apiVersion().orNull();
        if (value != null) {
            if (config.isShortOptions()) {
                if (value instanceof NOpenMode) {
                    switch ((NOpenMode) value) {
                        case OPEN_OR_ERROR: {
                          /**
                           * Fill option0.
                           *
                           * @param "-o") "-o")
                           * @param "r") "r")
                           * @param arguments arguments
                           * @param forceSingle force single
                           */
                            fillOption0(selectOptionName("--open-mode", "-o"), selectOptionVal("open-or-error", "r"), arguments, forceSingle);
                            return true;
                        }
                        case CREATE_OR_ERROR: {
                          /**
                           * Fill option0.
                           *
                           * @param "-o") "-o")
                           * @param "w") "w")
                           * @param arguments arguments
                           * @param forceSingle force single
                           */
                            fillOption0(selectOptionName("--open-mode", "-o"), selectOptionVal("create-or-error", "w"), arguments, forceSingle);
                            return true;
                        }
                        case OPEN_OR_CREATE: {
                            if (!config.isOmitDefaults()) {
                              /**
                               * Fill option0.
                               *
                               * @param "-o") "-o")
                               * @param "rw") "rw")
                               * @param arguments arguments
                               * @param forceSingle force single
                               */
                                fillOption0(selectOptionName("--open-mode", "-o"), selectOptionVal("open-or-create", "rw"), arguments, forceSingle);
                            }
                            return true;
                        }
                        case OPEN_OR_NULL: {
                          /**
                           * Fill option0.
                           *
                           * @param "-o") "-o")
                           * @param "on") "on")
                           * @param arguments arguments
                           * @param forceSingle force single
                           */
                            fillOption0(selectOptionName("--open-mode", "-o"), selectOptionVal("open-or-null", "on"), arguments, forceSingle);
                            return true;
                        }
                    }
                }
                if (value instanceof NExecutionType) {
                    switch ((NExecutionType) value) {
                        case SYSTEM: {
                            if (isApiVersionOrAfter(V081)) {
                                arguments.add("--system");
                            } else {
                                arguments.add("--user-cmd");
                            }
                            return true;
                        }
                        case EMBEDDED: {
                            arguments.add(selectOptionName("--embedded", "-b"));
                            return true;
                        }
                        case SPAWN: {
                            if (!config.isOmitDefaults()) {
                                arguments.add(selectOptionName("--spawn", "-x"));
                            }
                            return true;
                        }
                        case OPEN: {
                            arguments.add(selectOptionName("--open-file", "--open-file"));
                            return true;
                        }
                    }
                }
                if (value instanceof NConfirmationMode) {
                    switch ((NConfirmationMode) value) {
                        case YES: {
                            arguments.add(selectOptionName("--yes", "-y"));
                            return true;
                        }
                        case NO: {
                            arguments.add(selectOptionName("-no", "-n"));
                            return true;
                        }
                        case ASK: {
                            if (!config.isOmitDefaults()) {
                                arguments.add("--ask");
                                return true;
                            }
                            break;
                        }
                        case ERROR: {
                            arguments.add("--error");
                            return true;
                        }
                    }
                }
                if (value instanceof NTerminalMode) {
                    NVersion apiVersionObj = config.apiVersion();
                    switch ((NTerminalMode) value) {
                        case FILTERED: {
                            if (isApiVersionOrAfter(V084)) {
                                arguments.add("--color=filtered");
                            } else {
                                arguments.add(selectOptionName("--!color", "-!c"));
                            }
                            return true;
                        }
                        case FORMATTED: {
                            arguments.add(selectOptionName("--color", "-c"));
                            return true;
                        }
                        case INHERITED: {
                            arguments.add(selectOptionName("--color=inherited", "-c=h"));
                            return true;
                        }
                        case ANSI: {
                            arguments.add(selectOptionName("--color=ansi", "-c=a"));
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Fill option.
     *
     * @param value value
     * @param arguments arguments
     * @param forceSingle force single
     * @return fill option result
     */
    private void fillOption(Enum value, List<String> arguments, boolean forceSingle) {
        if (value != null) {
            if (tryFillOptionShort(value, arguments, forceSingle)) {
                return;
            }
            if (value instanceof NEnum) {
                arguments.add("--" + ((NEnum) value).id());
            } else {
                arguments.add("--" +
                        NNameFormat.CONST_NAME.format(value.name())
                );
            }
        }
    }

    /**
     * Select option val.
     *
     * @param longName long name
     * @param shortName short name
     * @return select option val result
     */
    private String selectOptionVal(String longName, String shortName) {
        if (config.isShortOptions()) {
            return shortName;
        }
        return longName;
    }

    /**
     * Select option name.
     *
     * @param longName long name
     * @param shortName short name
     * @return select option name result
     */
    private String selectOptionName(String longName, String shortName) {
        if (config.isShortOptions() && shortName != null) {
            return shortName;
        }
        return longName;
    }

    /**
     * Fill option0.
     *
     * @param name name
     * @param value value
     * @param arguments arguments
     * @param forceSingle force single
     * @return fill option0 result
     */
    private void fillOption0(String name, String value, List<String> arguments, boolean forceSingle) {
        if (config.isSingleArgOptions() || forceSingle) {
            arguments.add(name + "=" + value);
        } else {
            arguments.add(name);
            arguments.add(value);
        }
    }


    /**
     * Converts to cmd line.
     *
     * @return to cmd line result
     */
    public NCmdLine toCmdLine() {
        NVersion apiVersionObj = config.apiVersion();
        List<String> arguments = new ArrayList<>();

      /**
       * Fill option.
       *
       * @param "--java" "--java"
       * @param "-j" "-j"
       * @param options.javaCommand().orNull() options.java command().or null()
       * @param arguments arguments
       * @param false false
       */
        fillOption("--java", "-j", options.javaCommand().orNull(), arguments, false);
      /**
       * Fill option.
       *
       * @param "--java-options" "--java-options"
       * @param "-O" "-o"
       * @param options.javaOptions().orNull() options.java options().or null()
       * @param arguments arguments
       * @param false false
       */
        fillOption("--java-options", "-O", options.javaOptions().orNull(), arguments, false);
        String wsString = options.workspace().orNull();
        if (NBlankable.isBlank(wsString)) {
            //default workspace name
            wsString = "";
        } else if (wsString.contains("/") || wsString.contains("\\")) {
            //workspace path
            wsString = new File(wsString).toPath().toAbsolutePath().normalize().toString();
        } else {
            //workspace name
        }
      /**
       * Fill option.
       *
       * @param "--workspace" "--workspace"
       * @param "-w" "-w"
       * @param wsString ws string
       * @param arguments arguments
       * @param false false
       */
        fillOption("--workspace", "-w", wsString, arguments, false);
      /**
       * Fill option.
       *
       * @param "--user" "--user"
       * @param "-u" "-u"
       * @param options.userName().orNull() options.user name().or null()
       * @param arguments arguments
       * @param false false
       */
        fillOption("--user", "-u", options.userName().orNull(), arguments, false);
      /**
       * Fill option.
       *
       * @param "--password" "--password"
       * @param "-p" "-p"
       * @param options.credential().orNull() options.credential().or null()
       * @param arguments arguments
       * @param false false
       */
        fillOption("--password", "-p", options.credential().orNull(), arguments, false);
      /**
       * Fill option.
       *
       * @param "--boot-version" "--boot-version"
       * @param "-V" "-v"
       * @param options.apiVersion().map(Object::toString).orNull() options.api version().map( object::to string).or null()
       * @param arguments arguments
       * @param false false
       */
        fillOption("--boot-version", "-V", options.apiVersion().map(Object::toString).orNull(), arguments, false);
      /**
       * Fill option.
       *
       * @param "--boot-runtime" "--boot-runtime"
       * @param null null
       * @param options.runtimeId().map(Object::toString).orNull() options.runtime id().map( object::to string).or null()
       * @param arguments arguments
       * @param false false
       */
        fillOption("--boot-runtime", null, options.runtimeId().map(Object::toString).orNull(), arguments, false);

        {
            NTerminalMode nTerminalMode = options.terminalMode().orNull();
            if (!isApiVersionOrAfter(V084)) {
                if (options.bot().orElse(false)) {
                    //force filtered for older nuts
                    nTerminalMode = NTerminalMode.FILTERED;
                }
            }
            if (!(config.isOmitDefaults() && nTerminalMode == NTerminalMode.FORMATTED)) {
              /**
               * Fill option.
               *
               * @param "--color" "--color"
               * @param "-c" "-c"
               * @param nTerminalMode n terminal mode
               * @param NTerminalMode.class n terminal mode.class
               * @param arguments arguments
               * @param true true
               */
                fillOption("--color", "-c", nTerminalMode, NTerminalMode.class, arguments, true);
            }
        }
        NLogConfig logConfig = options.logConfig().orNull();
        if (logConfig != null) {
            if (logConfig.logTermLevel() != null && logConfig.logTermLevel() == logConfig.logFileLevel()) {
              /**
               * Fill option.
               *
               * @param logConfig.logFileLevel().toString().toLowerCase() log config.log file level().to string().to lower case()
               * @param null null
               * @param true true
               * @param false false
               * @param arguments arguments
               * @param false false
               */
                fillOption("--log-" + logConfig.logFileLevel().toString().toLowerCase(), null, true, false, arguments, false);
            } else {
                if (logConfig.logTermLevel() != null) {
                  /**
                   * Fill option.
                   *
                   * @param logConfig.logTermLevel().toString().toLowerCase() log config.log term level().to string().to lower case()
                   * @param null null
                   * @param true true
                   * @param false false
                   * @param arguments arguments
                   * @param false false
                   */
                    fillOption("--log-term-" + logConfig.logTermLevel().toString().toLowerCase(), null, true, false, arguments, false);
                }
                if (logConfig.logFileLevel() != null) {
                  /**
                   * Fill option.
                   *
                   * @param logConfig.logFileLevel().toString().toLowerCase() log config.log file level().to string().to lower case()
                   * @param null null
                   * @param true true
                   * @param false false
                   * @param arguments arguments
                   * @param false false
                   */
                    fillOption("--log-file-" + logConfig.logFileLevel().toString().toLowerCase(), null, true, false, arguments, false);
                }
            }
            if (logConfig.logFileCount() > 0) {
              /**
               * Fill option.
               *
               * @param "--log-file-count" "--log-file-count"
               * @param null null
               * @param String.valueOf(logConfig.logFileCount()) string.value of(log config.log file count())
               * @param arguments arguments
               * @param false false
               */
                fillOption("--log-file-count", null, String.valueOf(logConfig.logFileCount()), arguments, false);
            }
          /**
           * Fill option.
           *
           * @param "--log-file-size" "--log-file-size"
           * @param null null
           * @param logConfig.logFileSize() log config.log file size()
           * @param arguments arguments
           * @param false false
           */
            fillOption("--log-file-size", null, logConfig.logFileSize(), arguments, false);
          /**
           * Fill option.
           *
           * @param "--log-file-base" "--log-file-base"
           * @param null null
           * @param logConfig.logFileBase() log config.log file base()
           * @param arguments arguments
           * @param false false
           */
            fillOption("--log-file-base", null, logConfig.logFileBase(), arguments, false);
          /**
           * Fill option.
           *
           * @param "--log-file-name" "--log-file-name"
           * @param null null
           * @param logConfig.logFileName() log config.log file name()
           * @param arguments arguments
           * @param false false
           */
            fillOption("--log-file-name", null, logConfig.logFileName(), arguments, false);
        }
      /**
       * Fill option.
       *
       * @param "--exclude-extension" "--exclude-extension"
       * @param "-X" "-x"
       * @param options.excludedExtensions().orElseGet(Collections::emptyList) options.excluded extensions().or else get( collections::empty list)
       * @param ";" ";"
       * @param arguments arguments
       * @param false false
       */
        fillOption("--exclude-extension", "-X", options.excludedExtensions().orElseGet(Collections::emptyList), ";", arguments, false);

        if (isApiVersionOrAfter(V081)) {
          /**
           * Fill option.
           *
           * @param "--repositories" "--repositories"
           * @param "-r" "-r"
           * @param options.repositories().orElseGet(Collections::emptyList) options.repositories().or else get( collections::empty list)
           * @param ";" ";"
           * @param arguments arguments
           * @param false false
           */
            fillOption("--repositories", "-r", options.repositories().orElseGet(Collections::emptyList), ";", arguments, false);
        } else {
          /**
           * Fill option.
           *
           * @param "--repository" "--repository"
           * @param "-r" "-r"
           * @param options.repositories().orElseGet(Collections::emptyList) options.repositories().or else get( collections::empty list)
           * @param ";" ";"
           * @param arguments arguments
           * @param false false
           */
            fillOption("--repository", "-r", options.repositories().orElseGet(Collections::emptyList), ";", arguments, false);
        }

      /**
       * Fill option.
       *
       * @param "--global" "--global"
       * @param "-g" "-g"
       * @param options.system().orNull() options.system().or null()
       * @param false false
       * @param arguments arguments
       * @param false false
       */
        fillOption("--global", "-g", options.system().orNull(), false, arguments, false);
      /**
       * Fill option.
       *
       * @param "--gui" "--gui"
       * @param null null
       * @param options.gui().orNull() options.gui().or null()
       * @param false false
       * @param arguments arguments
       * @param false false
       */
        fillOption("--gui", null, options.gui().orNull(), false, arguments, false);
      /**
       * Fill option.
       *
       * @param "--read-only" "--read-only"
       * @param "-R" "-r"
       * @param options.readOnly().orNull() options.read only().or null()
       * @param false false
       * @param arguments arguments
       * @param false false
       */
        fillOption("--read-only", "-R", options.readOnly().orNull(), false, arguments, false);
      /**
       * Fill option.
       *
       * @param "--trace" "--trace"
       * @param "-t" "-t"
       * @param options.trace().orNull() options.trace().or null()
       * @param true true
       * @param arguments arguments
       * @param false false
       */
        fillOption("--trace", "-t", options.trace().orNull(), true, arguments, false);
      /**
       * Fill option.
       *
       * @param "--progress" "--progress"
       * @param "-P" "-p"
       * @param options.progressOptions().orNull() options.progress options().or null()
       * @param arguments arguments
       * @param true true
       */
        fillOption("--progress", "-P", options.progressOptions().orNull(), arguments, true);
      /**
       * Fill option.
       *
       * @param "--solver" "--solver"
       * @param null null
       * @param options.dependencySolver().orNull() options.dependency solver().or null()
       * @param arguments arguments
       * @param false false
       */
        fillOption("--solver", null, options.dependencySolver().orNull(), arguments, false);
        if (isApiVersionOrAfter(V083)) {
          /**
           * Fill option.
           *
           * @param "--debug" "--debug"
           * @param null null
           * @param options.debug().orNull() options.debug().or null()
           * @param arguments arguments
           * @param true true
           */
            fillOption("--debug", null, options.debug().orNull(), arguments, true);
        } else {
          /**
           * Fill option.
           *
           * @param "--debug" "--debug"
           * @param null null
           * @param options.debug().isPresent() options.debug().is present()
           * @param false false
           * @param arguments arguments
           * @param true true
           */
            fillOption("--debug", null, options.debug().isPresent(), false, arguments, true);
        }
      /**
       * Fill option.
       *
       * @param "--install-companions" "--install-companions"
       * @param "-k" "-k"
       * @param options.installCompanions().orNull() options.install companions().or null()
       * @param false false
       * @param arguments arguments
       * @param false false
       */
        fillOption("--install-companions", "-k", options.installCompanions().orNull(), false, arguments, false);
      /**
       * Fill option.
       *
       * @param "--skip-welcome" "--skip-welcome"
       * @param "-K" "-k"
       * @param options.skipWelcome().orElse(false) options.skip welcome().or else(false)
       * @param false false
       * @param arguments arguments
       * @param false false
       */
        fillOption("--skip-welcome", "-K", options.skipWelcome().orElse(false), false, arguments, false);
      /**
       * Fill option.
       *
       * @param "--out-line-prefix" "--out-line-prefix"
       * @param null null
       * @param options.outLinePrefix().orNull() options.out line prefix().or null()
       * @param arguments arguments
       * @param false false
       */
        fillOption("--out-line-prefix", null, options.outLinePrefix().orNull(), arguments, false);
      /**
       * Fill option.
       *
       * @param "--skip-boot" "--skip-boot"
       * @param "-Q" "-q"
       * @param options.skipBoot().orNull() options.skip boot().or null()
       * @param false false
       * @param arguments arguments
       * @param false false
       */
        fillOption("--skip-boot", "-Q", options.skipBoot().orNull(), false, arguments, false);
      /**
       * Fill option.
       *
       * @param "--cached" "--cached"
       * @param null null
       * @param options.cached().orNull() options.cached().or null()
       * @param true true
       * @param arguments arguments
       * @param false false
       */
        fillOption("--cached", null, options.cached().orNull(), true, arguments, false);
      /**
       * Fill option.
       *
       * @param "--indexed" "--indexed"
       * @param null null
       * @param options.indexed().orNull() options.indexed().or null()
       * @param true true
       * @param arguments arguments
       * @param false false
       */
        fillOption("--indexed", null, options.indexed().orNull(), true, arguments, false);
      /**
       * Fill option.
       *
       * @param "--transitive" "--transitive"
       * @param null null
       * @param options.transitive().orNull() options.transitive().or null()
       * @param true true
       * @param arguments arguments
       * @param false false
       */
        fillOption("--transitive", null, options.transitive().orNull(), true, arguments, false);
        if (isApiVersionOrAfter(V081)) {
          /**
           * Fill option.
           *
           * @param "--bot" "--bot"
           * @param "-B" "-b"
           * @param options.bot().orNull() options.bot().or null()
           * @param false false
           * @param arguments arguments
           * @param false false
           */
            fillOption("--bot", "-B", options.bot().orNull(), false, arguments, false);
        }
        if (isApiVersionOrAfter(V085)) {
          /**
           * Fill option.
           *
           * @param "--preview-repo" "--preview-repo"
           * @param "-U" "-u"
           * @param options.previewRepo().orNull() options.preview repo().or null()
           * @param false false
           * @param arguments arguments
           * @param false false
           */
            fillOption("--preview-repo", "-U", options.previewRepo().orNull(), false, arguments, false);
          /**
           * Fill option.
           *
           * @param "--shared-instance" "--shared-instance"
           * @param null null
           * @param options.sharedInstance().orNull() options.shared instance().or null()
           * @param false false
           * @param arguments arguments
           * @param false false
           */
            fillOption("--shared-instance", null, options.sharedInstance().orNull(), false, arguments, false);
        }
        if (options.fetchStrategy().isPresent() && options.fetchStrategy().orNull() != NFetchStrategy.ONLINE) {
          /**
           * Fill option.
           *
           * @param "--fetch" "--fetch"
           * @param "-f" "-f"
           * @param options.fetchStrategy().orNull() options.fetch strategy().or null()
           * @param NFetchStrategy.class n fetch strategy.class
           * @param arguments arguments
           * @param false false
           */
            fillOption("--fetch", "-f", options.fetchStrategy().orNull(), NFetchStrategy.class, arguments, false);
        }
      /**
       * Fill option.
       *
       * @param options.confirm().orNull() options.confirm().or null()
       * @param arguments arguments
       * @param false false
       */
        fillOption(options.confirm().orNull(), arguments, false);
      /**
       * Fill option.
       *
       * @param options.outputFormat().orNull() options.output format().or null()
       * @param arguments arguments
       * @param false false
       */
        fillOption(options.outputFormat().orNull(), arguments, false);
        for (String outputFormatOption : options.outputFormatOptions().orElseGet(Collections::emptyList)) {
          /**
           * Fill option.
           *
           * @param "--output-format-option" "--output-format-option"
           * @param "-T" "-t"
           * @param outputFormatOption output format option
           * @param arguments arguments
           * @param false false
           */
            fillOption("--output-format-option", "-T", outputFormatOption, arguments, false);
        }
        if (isApiVersionOrAfter(V080)) {
            fillOption("--expire", "-N",
                    options.expireTime().map(Object::toString).orNull(),
                    arguments, false);
            if (options.outLinePrefix().isPresent()
                    && Objects.equals(options.outLinePrefix(), options.errLinePrefix())
                    && options.outLinePrefix().get().length() > 0) {
              /**
               * Fill option.
               *
               * @param "--line-prefix" "--line-prefix"
               * @param null null
               * @param options.outLinePrefix().orNull() options.out line prefix().or null()
               * @param arguments arguments
               * @param false false
               */
                fillOption("--line-prefix", null, options.outLinePrefix().orNull(), arguments, false);
            } else {
                if (options.outLinePrefix().isPresent() && options.outLinePrefix().get().length() > 0) {
                  /**
                   * Fill option.
                   *
                   * @param "--out-line-prefix" "--out-line-prefix"
                   * @param null null
                   * @param options.outLinePrefix().orNull() options.out line prefix().or null()
                   * @param arguments arguments
                   * @param false false
                   */
                    fillOption("--out-line-prefix", null, options.outLinePrefix().orNull(), arguments, false);
                }
                if (options.errLinePrefix().isPresent() && options.errLinePrefix().get().length() > 0) {
                  /**
                   * Fill option.
                   *
                   * @param "--err-line-prefix" "--err-line-prefix"
                   * @param null null
                   * @param options.errLinePrefix().orNull() options.err line prefix().or null()
                   * @param arguments arguments
                   * @param false false
                   */
                    fillOption("--err-line-prefix", null, options.errLinePrefix().orNull(), arguments, false);
                }
            }
        }
        if (isApiVersionOrAfter(V081)) {
          /**
           * Fill option.
           *
           * @param "--theme" "--theme"
           * @param null null
           * @param options.theme().orNull() options.theme().or null()
           * @param arguments arguments
           * @param false false
           */
            fillOption("--theme", null, options.theme().orNull(), arguments, false);
        }
        if (isApiVersionOrAfter(V081)) {
          /**
           * Fill option.
           *
           * @param "--locale" "--locale"
           * @param "-L" "-l"
           * @param options.locale().orNull() options.locale().or null()
           * @param arguments arguments
           * @param false false
           */
            fillOption("--locale", "-L", options.locale().orNull(), arguments, false);
        }
        if (isApiVersionOrAfter(V084)) {
          /**
           * Fill option.
           *
           * @param "--init-launchers" "--init-launchers"
           * @param null null
           * @param options.initLaunchers().orNull() options.init launchers().or null()
           * @param true true
           * @param arguments arguments
           * @param false false
           */
            fillOption("--init-launchers", null, options.initLaunchers().orNull(), true, arguments, false);
          /**
           * Fill option.
           *
           * @param "--init-platforms" "--init-platforms"
           * @param null null
           * @param options.initLaunchers().orNull() options.init launchers().or null()
           * @param true true
           * @param arguments arguments
           * @param false false
           */
            fillOption("--init-platforms", null, options.initLaunchers().orNull(), true, arguments, false);
          /**
           * Fill option.
           *
           * @param "--init-java" "--init-java"
           * @param null null
           * @param options.initLaunchers().orNull() options.init launchers().or null()
           * @param true true
           * @param arguments arguments
           * @param false false
           */
            fillOption("--init-java", null, options.initLaunchers().orNull(), true, arguments, false);
          /**
           * Fill option.
           *
           * @param "--init-scripts" "--init-scripts"
           * @param null null
           * @param options.initLaunchers().orNull() options.init launchers().or null()
           * @param true true
           * @param arguments arguments
           * @param false false
           */
            fillOption("--init-scripts", null, options.initLaunchers().orNull(), true, arguments, false);
          /**
           * Fill option.
           *
           * @param "--desktop-launcher" "--desktop-launcher"
           * @param null null
           * @param options.desktopLauncher().orNull() options.desktop launcher().or null()
           * @param NSupportMode.class n support mode.class
           * @param arguments arguments
           * @param false false
           */
            fillOption("--desktop-launcher", null, options.desktopLauncher().orNull(), NSupportMode.class, arguments, false);
          /**
           * Fill option.
           *
           * @param "--menu-launcher" "--menu-launcher"
           * @param null null
           * @param options.desktopLauncher().orNull() options.desktop launcher().or null()
           * @param NSupportMode.class n support mode.class
           * @param arguments arguments
           * @param false false
           */
            fillOption("--menu-launcher", null, options.desktopLauncher().orNull(), NSupportMode.class, arguments, false);
          /**
           * Fill option.
           *
           * @param "--user-launcher" "--user-launcher"
           * @param null null
           * @param options.desktopLauncher().orNull() options.desktop launcher().or null()
           * @param NSupportMode.class n support mode.class
           * @param arguments arguments
           * @param false false
           */
            fillOption("--user-launcher", null, options.desktopLauncher().orNull(), NSupportMode.class, arguments, false);
          /**
           * Fill option.
           *
           * @param "--isolation-level" "--isolation-level"
           * @param null null
           * @param options.isolationLevel().orNull() options.isolation level().or null()
           * @param NIsolationLevel.class n isolation level.class
           * @param arguments arguments
           * @param false false
           */
            fillOption("--isolation-level", null, options.isolationLevel().orNull(), NIsolationLevel.class, arguments, false);
        } else if (isApiVersionOrAfter(V081)) {
          /**
           * Fill option.
           *
           * @param "---init-launchers" "---init-launchers"
           * @param null null
           * @param options.initLaunchers().orNull() options.init launchers().or null()
           * @param true true
           * @param arguments arguments
           * @param false false
           */
            fillOption("---init-launchers", null, options.initLaunchers().orNull(), true, arguments, false);
          /**
           * Fill option.
           *
           * @param "---init-platforms" "---init-platforms"
           * @param null null
           * @param options.initLaunchers().orNull() options.init launchers().or null()
           * @param true true
           * @param arguments arguments
           * @param false false
           */
            fillOption("---init-platforms", null, options.initLaunchers().orNull(), true, arguments, false);
          /**
           * Fill option.
           *
           * @param "---init-java" "---init-java"
           * @param null null
           * @param options.initLaunchers().orNull() options.init launchers().or null()
           * @param true true
           * @param arguments arguments
           * @param false false
           */
            fillOption("---init-java", null, options.initLaunchers().orNull(), true, arguments, false);
          /**
           * Fill option.
           *
           * @param "---init-scripts" "---init-scripts"
           * @param null null
           * @param options.initLaunchers().orNull() options.init launchers().or null()
           * @param true true
           * @param arguments arguments
           * @param false false
           */
            fillOption("---init-scripts", null, options.initLaunchers().orNull(), true, arguments, false);
          /**
           * Fill option.
           *
           * @param "---system-desktop-launcher" "---system-desktop-launcher"
           * @param null null
           * @param options.desktopLauncher().orNull() options.desktop launcher().or null()
           * @param NSupportMode.class n support mode.class
           * @param arguments arguments
           * @param false false
           */
            fillOption("---system-desktop-launcher", null, options.desktopLauncher().orNull(), NSupportMode.class, arguments, false);
          /**
           * Fill option.
           *
           * @param "---system-menu-launcher" "---system-menu-launcher"
           * @param null null
           * @param options.desktopLauncher().orNull() options.desktop launcher().or null()
           * @param NSupportMode.class n support mode.class
           * @param arguments arguments
           * @param false false
           */
            fillOption("---system-menu-launcher", null, options.desktopLauncher().orNull(), NSupportMode.class, arguments, false);
          /**
           * Fill option.
           *
           * @param "---system-custom-launcher" "---system-custom-launcher"
           * @param null null
           * @param options.desktopLauncher().orNull() options.desktop launcher().or null()
           * @param NSupportMode.class n support mode.class
           * @param arguments arguments
           * @param false false
           */
            fillOption("---system-custom-launcher", null, options.desktopLauncher().orNull(), NSupportMode.class, arguments, false);
        }

      /**
       * Fill option.
       *
       * @param "--name" "--name"
       * @param null null
       * @param NStringUtils.strip(options.name().orNull()) n string utils.strip(options.name().or null())
       * @param arguments arguments
       * @param false false
       */
        fillOption("--name", null, NStringUtils.strip(options.name().orNull()), arguments, false);
      /**
       * Fill option.
       *
       * @param "--archetype" "--archetype"
       * @param "-A" "-a"
       * @param options.archetype().orNull() options.archetype().or null()
       * @param arguments arguments
       * @param false false
       */
        fillOption("--archetype", "-A", options.archetype().orNull(), arguments, false);
      /**
       * Fill option.
       *
       * @param "--store-layout" "--store-layout"
       * @param null null
       * @param options.storeLayout().orNull() options.store layout().or null()
       * @param NOsFamily.class n os family.class
       * @param arguments arguments
       * @param false false
       */
        fillOption("--store-layout", null, options.storeLayout().orNull(), NOsFamily.class, arguments, false);
      /**
       * Fill option.
       *
       * @param "--store-strategy" "--store-strategy"
       * @param null null
       * @param options.storeStrategy().orNull() options.store strategy().or null()
       * @param NStoreStrategy.class n store strategy.class
       * @param arguments arguments
       * @param false false
       */
        fillOption("--store-strategy", null, options.storeStrategy().orNull(), NStoreStrategy.class, arguments, false);
      /**
       * Fill option.
       *
       * @param "--repo-store-strategy" "--repo-store-strategy"
       * @param null null
       * @param options.repositoryStoreStrategy().orNull() options.repository store strategy().or null()
       * @param NStoreStrategy.class n store strategy.class
       * @param arguments arguments
       * @param false false
       */
        fillOption("--repo-store-strategy", null, options.repositoryStoreStrategy().orNull(), NStoreStrategy.class, arguments, false);
        Map<NStoreType, String> storeLocations = options.storeLocations().orElseGet(Collections::emptyMap);
        for (NStoreType location : NStoreType.values()) {
            String s = storeLocations.get(location);
            if (!NBlankable.isBlank(s)) {
              /**
               * Fill option.
               *
               * @param "-location" "-location"
               * @param null null
               * @param s s
               * @param arguments arguments
               * @param false false
               */
                fillOption("--" + location.id() + "-location", null, s, arguments, false);
            }
        }

        Map<NHomeLocation, String> homeLocations = options.homeLocations().orElseGet(Collections::emptyMap);
        if (homeLocations != null) {
            for (NStoreType location : NStoreType.values()) {
                String s = homeLocations.get(NHomeLocation.of(null, location));
                if (!NBlankable.isBlank(s)) {
                  /**
                   * Fill option.
                   *
                   * @param "-home" "-home"
                   * @param null null
                   * @param s s
                   * @param arguments arguments
                   * @param false false
                   */
                    fillOption("--system-" + location.id() + "-home", null, s, arguments, false);
                }
            }
            for (NOsFamily osFamily : NOsFamily.values()) {
                for (NStoreType location : NStoreType.values()) {
                    String s = homeLocations.get(NHomeLocation.of(osFamily, location));
                    if (!NBlankable.isBlank(s)) {
                      /**
                       * Fill option.
                       *
                       * @param "-home" "-home"
                       * @param null null
                       * @param s s
                       * @param arguments arguments
                       * @param false false
                       */
                        fillOption("--" + osFamily.id() + "-" + location.id() + "-home", null, s, arguments, false);
                    }
                }
            }
        }
        if (isApiVersionOrAfter(V080)) {
            if (options.switchWorkspace().isPresent()) {
              /**
               * Fill option.
               *
               * @param "--switch" "--switch"
               * @param null null
               * @param options.switchWorkspace().orNull() options.switch workspace().or null()
               * @param false false
               * @param arguments arguments
               * @param false false
               */
                fillOption("--switch", null, options.switchWorkspace().orNull(), false, arguments, false);
            }
        }

      /**
       * Fill option.
       *
       * @param "--help" "--help"
       * @param "-h" "-h"
       * @param options.commandHelp().orElse(false) options.command help().or else(false)
       * @param false false
       * @param arguments arguments
       * @param false false
       */
        fillOption("--help", "-h", options.commandHelp().orElse(false), false, arguments, false);
      /**
       * Fill option.
       *
       * @param "--version" "--version"
       * @param "-v" "-v"
       * @param options.commandVersion().orElse(false) options.command version().or else(false)
       * @param false false
       * @param arguments arguments
       * @param false false
       */
        fillOption("--version", "-v", options.commandVersion().orElse(false), false, arguments, false);

        if (!(config.isOmitDefaults() && (options.openMode().isNotPresent() || options.openMode().orNull() == NOpenMode.OPEN_OR_CREATE))) {
          /**
           * Fill option.
           *
           * @param options.openMode().orNull() options.open mode().or null()
           * @param arguments arguments
           * @param false false
           */
            fillOption(options.openMode().orNull(), arguments, false);
        }
      /**
       * Fill option.
       *
       * @param options.executionType().orNull() options.execution type().or null()
       * @param arguments arguments
       * @param false false
       */
        fillOption(options.executionType().orNull(), arguments, false);
      /**
       * Fill option.
       *
       * @param options.runAs().orNull() options.run as().or null()
       * @param arguments arguments
       */
        fillOption(options.runAs().orNull(), arguments);
      /**
       * Fill option.
       *
       * @param "--reset" "--reset"
       * @param "-Z" "-z"
       * @param options.reset().orNull() options.reset().or null()
       * @param false false
       * @param arguments arguments
       * @param false false
       */
        fillOption("--reset", "-Z", options.reset().orNull(), false, arguments, false);
      /**
       * Fill option.
       *
       * @param "--recover" "--recover"
       * @param "-z" "-z"
       * @param options.recover().orNull() options.recover().or null()
       * @param false false
       * @param arguments arguments
       * @param false false
       */
        fillOption("--recover", "-z", options.recover().orNull(), false, arguments, false);
      /**
       * Fill option.
       *
       * @param "--dry" "--dry"
       * @param "-D" "-d"
       * @param options.dry().orNull() options.dry().or null()
       * @param false false
       * @param arguments arguments
       * @param false false
       */
        fillOption("--dry", "-D", options.dry().orNull(), false, arguments, false);
        if (isApiVersionOrAfter(V085)) {
          /**
           * Fill option.
           *
           * @param "--reset-hard" "--reset-hard"
           * @param null null
           * @param options.reset().orNull() options.reset().or null()
           * @param false false
           * @param arguments arguments
           * @param false false
           */
            fillOption("--reset-hard", null, options.reset().orNull(), false, arguments, false);
        }
        if (isApiVersionOrAfter(V084)) {
          /**
           * Fill option.
           *
           * @param "--stacktrace" "--stacktrace"
           * @param "-d" "-d"
           * @param options.showStacktrace().orNull() options.show stacktrace().or null()
           * @param false false
           * @param arguments arguments
           * @param false false
           */
            fillOption("--stacktrace", "-d", options.showStacktrace().orNull(), false, arguments, false);
        }
        if (isApiVersionOrAfter(V081)) {
            if (options.customOptions() != null) {
                arguments.addAll(options.customOptions().orElseGet(Collections::emptyList));
            }
        }
        //final options for execution
        if ((!config.isOmitDefaults() && options.applicationArguments().isPresent() && !options.applicationArguments().get().isEmpty())
                || !options.executorOptions().orElseGet(Collections::emptyList).isEmpty()) {
            arguments.add(selectOptionName("--exec", "-e"));
        }
        arguments.addAll(options.executorOptions().orElseGet(Collections::emptyList));
        arguments.addAll(options.applicationArguments().orElseGet(Collections::emptyList));
        return NCmdLine.of(arguments);
    }

    /**
     * Checks if is api version or after.
     *
     * @param version version
     * @return is api version or after result
     */
    private boolean isApiVersionOrAfter(String version) {
        NVersion apiVersionObj = config.apiVersion();
        return apiVersionObj == null || apiVersionObj.compareTo(version) >= 0;
    }

}

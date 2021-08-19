package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * https://specifications.freedesktop.org/desktop-entry-spec/desktop-entry-spec-latest.html#recognized-keys
 */
public class FreeDesktopEntry {
    public static final String GROUP_DESKTOP_ENTRY = "Desktop Entry";
    //#!/usr/bin/env xdg-open
    private Map<String, Group> groups = new LinkedHashMap<>();

    public void add(Group g) {
        if (g.getGroupName() == null) {
            throw new IllegalArgumentException("illegal free desktop group Name");
        }
        groups.put(g.getGroupName(), g);
    }

    public Group getOrCreateDesktopEntry() {
        Group q = groups.get(GROUP_DESKTOP_ENTRY);
        if (q == null) {
            q = Group.desktopEntry(null, null, System.getProperty("user.home"));
            groups.put(GROUP_DESKTOP_ENTRY, q);
        }
        if (q.getType() == null) {
            //should never happen
            q.setType(Type.APPLICATION);
        }
        return q;
    }

    public Group getOrCreateGroup(String name) {
        return groups.computeIfAbsent(name, k -> new Group(name));
    }

    public List<Group> getGroups() {
        return new ArrayList<>(groups.values());
    }

    public enum Type {
        APPLICATION,
        LINK,
        DIRECTORY,
    }

    public static class Group {
        private final String groupName;
        /**
         * menu path/category, will not be persisted
         */

        /**
         * Version of the Desktop Group Specification that the desktop entry conforms with. Entries that confirm with this version of the specification should use 1.5. Note that the version field is not required to be present.
         */
        private String version;
        /**
         * This specification defines 3 types of desktop entries: Application (type 1), Link (type 2) and Directory (type 3). To allow the addition of new types in the future, implementations should ignore desktop entries with an unknown type.
         */
        private Type type;
        /**
         * Specific name of the application, for example "Mozilla".
         */
        private String name;

        /**
         * Generic name of the application, for example "Web Browser".
         */
        private String genericName;

        /**
         * Tooltip for the entry, for example "View sites on the Internet". The value should not be redundant with the values of Name and GenericName.
         */
        private String comment;

        /**
         * Path to an executable file on disk used to determine if the program is actually installed. If the path is not an absolute path, the file is looked up in the $PATH environment variable. If the file is not present or if it is not executable, the entry may be ignored (not be used in menus, for example).
         */
        private String tryExec;

        /**
         * Program to execute, possibly with arguments. See the Exec key for details on how this key works. The Exec key is required if DBusActivatable is not set to true. Even if DBusActivatable is true, Exec should be specified for compatibility with implementations that do not understand DBusActivatable.
         */
        private String exec;

        /**
         * Icon to display in file manager, menus, etc. If the name is an absolute path, the given file will be used. If the name is not an absolute path, the algorithm described in the Icon Theme Specification will be used to locate the icon.
         */
        private String icon;

        /**
         * If entry is of type Application, the working directory to run the program in.
         */
        private String path;

        /**
         * NoDisplay means "this application exists, but don't display it in the menus". This can be useful to e.g. associate this application with MIME types, so that it gets launched from a file manager (or other apps), without having a menu entry for it (there are tons of good reasons for this, including e.g. the netscape -remote, or kfmclient openURL kind of stuff).
         * <p>
         * https://freedesktop.org/wiki/Standards/icon-theme-spec/
         */
        private boolean noDisplay;

        /**
         * Hidden should have been called Deleted. It means the user deleted (at his level) something that was present (at an upper level, e.g. in the system dirs). It's strictly equivalent to the .desktop file not existing at all, as far as that user is concerned. This can also be used to "uninstall" existing files (e.g. due to a renaming) - by letting make install install a file with Hidden=true in it.
         */
        private boolean hidden;

        /**
         * Whether the program runs in a terminal window.
         */
        private boolean terminal;

        /**
         * A boolean value specifying if D-Bus activation is supported for this application. If this key is missing, the default value is false. If the value is true then implementations should ignore the Exec key and send a D-Bus message to launch the application. See D-Bus Activation for more information on how this works. Applications should still include Exec= lines in their desktop files for compatibility with implementations that do not understand the DBusActivatable key.
         */
        private boolean dbusActivatable;

        /**
         * Identifiers for application actions. This can be used to tell the application to make a specific action, different from the default behavior. The Application actions section describes how actions work.
         */
        private List<String> actions = new ArrayList<>();
        /**
         * The MIME type(s) supported by this application.
         */
        private List<String> mimeType = new ArrayList<>();

        /**
         * Categories in which the entry should be shown in a menu (for possible values see the Desktop Menu Specification).
         * https://www.freedesktop.org/wiki/Specifications/menu-spec/
         */
        private List<String> categories = new ArrayList<>();

        /**
         * A list of interfaces that this application implements. By default, a desktop file implements no interfaces. See Interfaces for more information on how this works.
         */
        private List<String> implementsList = new ArrayList<>();

        /**
         * A list of strings which may be used in addition to other metadata to describe this entry. This can be useful e.g. to facilitate searching through entries. The values are not meant for display, and should not be redundant with the values of Name or GenericName.
         */
        private List<String> keywords = new ArrayList<>();

        /**
         * A list of strings identifying the desktop environments that should display/not display a given desktop entry.
         * <p>
         * By default, a desktop file should be shown, unless an OnlyShowIn key is present, in which case, the default is for the file not to be shown.
         * <p>
         * If $XDG_CURRENT_DESKTOP is set then it contains a colon-separated list of strings. In order, each string is considered. If a matching entry is found in OnlyShowIn then the desktop file is shown. If an entry is found in NotShowIn then the desktop file is not shown. If none of the strings match then the default action is taken (as above).
         * <p>
         * $XDG_CURRENT_DESKTOP should have been set by the login manager, according to the value of the DesktopNames found in the session file. The entry in the session file has multiple values separated in the usual way: with a semicolon.
         * <p>
         * The same desktop name may not appear in both OnlyShowIn and NotShowIn of a group.
         */
        private List<String> onlyShowIn = new ArrayList<>();

        /**
         * A list of strings identifying the desktop environments that should display/not display a given desktop entry.
         * <p>
         * By default, a desktop file should be shown, unless an OnlyShowIn key is present, in which case, the default is for the file not to be shown.
         * <p>
         * If $XDG_CURRENT_DESKTOP is set then it contains a colon-separated list of strings. In order, each string is considered. If a matching entry is found in OnlyShowIn then the desktop file is shown. If an entry is found in NotShowIn then the desktop file is not shown. If none of the strings match then the default action is taken (as above).
         * <p>
         * $XDG_CURRENT_DESKTOP should have been set by the login manager, according to the value of the DesktopNames found in the session file. The entry in the session file has multiple values separated in the usual way: with a semicolon.
         * <p>
         * The same desktop name may not appear in both OnlyShowIn and NotShowIn of a group.
         */
        private List<String> notShowIn = new ArrayList<>();

        /**
         * If true, it is KNOWN that the application will send a "remove" message when started with the DESKTOP_STARTUP_ID environment variable set. If false, it is KNOWN that the application does not work with startup notification at all (does not shown any window, breaks even when using StartupWMClass, etc.). If absent, a reasonable handling is up to implementations (assuming false, using StartupWMClass, etc.). (See the Startup Notification Protocol Specification for more details).
         */
        private boolean startNotify;

        /**
         * If specified, it is known that the application will map at least one window with the given string as its WM class or WM name hint (see the Startup Notification Protocol Specification for more details).
         */
        private String startupWMClass;

        /**
         * If entry is Link type, the URL to access.
         */
        private String url;

        /**
         * PrefersNonDefaultGPU
         */
        private boolean prefersNonDefaultGPU;

        public Group(String groupName) {
            this.groupName = groupName;
        }


        public static Group desktopEntry(String name, String exec, String path) {
            return application(GROUP_DESKTOP_ENTRY, name, exec, path);
        }

        public static Group application(String entryId, String name, String exec, String path) {
            return new Group(entryId).setType(Type.APPLICATION)
                    .setName(name)
                    .setExec(exec)
                    .setPath(path)
                    ;
        }

        public static Group link(String entryId, String name, String url) {
            return new Group(entryId).setType(Type.LINK)
                    .setName(name)
                    .setUrl(url)
                    ;
        }

        public static Group directory(String entryId, String name, String path) {
            return new Group(entryId).setType(Type.DIRECTORY)
                    .setName(name)
                    .setPath(path)
                    ;
        }

        private boolean addString(String k, String v, Map<String, Object> m) {
            if (v != null && v.length() > 0) {
                m.put(k, v);
                return true;
            }
            return false;
        }

        private boolean addBoolean(String k, boolean v, Map<String, Object> m) {
            if (v) {
                m.put(k, v);
                return true;
            }
            return false;
        }

        private boolean addStrings(String k, List<String> v, Map<String, Object> m) {
            if (v != null && v.size() > 0) {
                m.put(k, new ArrayList<>(v));
                return true;
            }
            return false;
        }

        public Map<String, Object> toMap() {
            LinkedHashMap<String, Object> m = new LinkedHashMap<>();
            if (type != null) {
                m.put("Type", type);
            }
            addString("Version", version, m);
            addString("Name", name, m);
            addString("GenericName", genericName, m);
            addString("Comment", comment, m);
            addString("TryExec", tryExec, m);
            addString("Exec", exec, m);
            addString("Icon", icon, m);
            addString("Path", path, m);
            addString("URL", url, m);
            addString("StartupWMClass", startupWMClass, m);
            addBoolean("NoDisplay", noDisplay, m);
            addBoolean("Hidden", hidden, m);
            addBoolean("Terminal", terminal, m);
            addBoolean("DBusActivatable", dbusActivatable, m);
            addBoolean("StartNotify", startNotify, m);
            addBoolean("PrefersNonDefaultGPU", prefersNonDefaultGPU, m);
            addStrings("Actions", actions, m);
            addStrings("MimeType", mimeType, m);
            addStrings("Categories", categories, m);
            addStrings("ImplementsList", implementsList, m);
            addStrings("Keywords", keywords, m);
            addStrings("OnlyShowIn", onlyShowIn, m);
            addStrings("NotShowIn", notShowIn, m);
            addStrings("NotShowIn", notShowIn, m);
            return m;
        }

        public String getGroupName() {
            return groupName;
        }

//        public Group setEntryName(String entryName) {
//            this.entryName = entryName;
//            return this;
//        }

        public String getVersion() {
            return version;
        }

        public Group setVersion(String version) {
            this.version = version;
            return this;
        }

        public Type getType() {
            return type;
        }

        public Group setType(Type type) {
            this.type = type;
            return this;
        }

        public String getName() {
            return name;
        }

        public Group setName(String name) {
            this.name = name;
            return this;
        }

        public String getGenericName() {
            return genericName;
        }

        public Group setGenericName(String genericName) {
            this.genericName = genericName;
            return this;
        }

        public String getComment() {
            return comment;
        }

        public Group setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public String getTryExec() {
            return tryExec;
        }

        public Group setTryExec(String tryExec) {
            this.tryExec = tryExec;
            return this;
        }

        public String getExec() {
            return exec;
        }

        public Group setExec(String exec) {
            this.exec = exec;
            return this;
        }

        public String getIcon() {
            return icon;
        }

        public Group setIcon(String icon) {
            this.icon = icon;
            return this;
        }

        public String getPath() {
            return path;
        }

        public Group setPath(String path) {
            this.path = path;
            return this;
        }

        public boolean isNoDisplay() {
            return noDisplay;
        }

        public Group setNoDisplay(boolean noDisplay) {
            this.noDisplay = noDisplay;
            return this;
        }

        public boolean isHidden() {
            return hidden;
        }

        public Group setHidden(boolean hidden) {
            this.hidden = hidden;
            return this;
        }

        public boolean isTerminal() {
            return terminal;
        }

        public Group setTerminal(boolean terminal) {
            this.terminal = terminal;
            return this;
        }

        public boolean isDbusActivatable() {
            return dbusActivatable;
        }

        public Group setDbusActivatable(boolean dbusActivatable) {
            this.dbusActivatable = dbusActivatable;
            return this;
        }

        public List<String> getActions() {
            return actions;
        }

        public Group setActions(List<String> actions) {
            this.actions = actions;
            return this;
        }

        public List<String> getMimeType() {
            return mimeType;
        }

        public Group setMimeType(List<String> mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public List<String> getCategories() {
            return categories;
        }

        public Group addCategory(String category) {
            if(category!=null && !categories.contains(category)){
                categories.add(category);
            }
            return this;
        }

        public Group setCategories(List<String> categories) {
            this.categories = categories;
            return this;
        }

        public List<String> getImplementsList() {
            return implementsList;
        }

        public Group setImplementsList(List<String> implementsList) {
            this.implementsList = implementsList;
            return this;
        }

        public List<String> getKeywords() {
            return keywords;
        }

        public Group setKeywords(List<String> keywords) {
            this.keywords = keywords;
            return this;
        }

        public List<String> getOnlyShowIn() {
            return onlyShowIn;
        }

        public Group setOnlyShowIn(List<String> onlyShowIn) {
            this.onlyShowIn = onlyShowIn;
            return this;
        }

        public List<String> getNotShowIn() {
            return notShowIn;
        }

        public Group setNotShowIn(List<String> notShowIn) {
            this.notShowIn = notShowIn;
            return this;
        }

        public boolean isStartNotify() {
            return startNotify;
        }

        public Group setStartNotify(boolean startNotify) {
            this.startNotify = startNotify;
            return this;
        }

        public String getStartupWMClass() {
            return startupWMClass;
        }

        public Group setStartupWMClass(String startupWMClass) {
            this.startupWMClass = startupWMClass;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public Group setUrl(String url) {
            this.url = url;
            return this;
        }

        public boolean isPrefersNonDefaultGPU() {
            return prefersNonDefaultGPU;
        }

        public Group setPrefersNonDefaultGPU(boolean prefersNonDefaultGPU) {
            this.prefersNonDefaultGPU = prefersNonDefaultGPU;
            return this;
        }
    }
}

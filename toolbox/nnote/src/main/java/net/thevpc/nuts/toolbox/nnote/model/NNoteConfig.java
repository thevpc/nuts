/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import net.thevpc.echo.AppWindowDisplayMode;

/**
 *
 * @author vpc
 */
public class NNoteConfig {

    private String locale;
    private String plaf;
    private String lastOpenPath;
    private Boolean displayToolBar;
    private Boolean displayStatusBar;
    private AppWindowDisplayMode displayMode;
    private String iconSet;
    private List<String> recentFiles = new ArrayList<>();
    private List<String> recentContentTypes = new ArrayList<>();

    public String getLastOpenPath() {
        return lastOpenPath;
    }

    public void setLastOpenPath(String lastOpenPath) {
        this.lastOpenPath = lastOpenPath;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getIconSet() {
        return iconSet;
    }

    public void setIconSet(String iconSet) {
        this.iconSet = iconSet;
    }

    public List<String> getRecentFiles() {
        return recentFiles;
    }

    public void setRecentFiles(List<String> recentFiles) {
        this.recentFiles = recentFiles;
    }

    public String getPlaf() {
        return plaf;
    }

    public void setPlaf(String plaf) {
        this.plaf = plaf;
    }

    public void addRecentFile(String recentFile) {
        if (recentFile != null && recentFile.trim().length() > 0) {
            LinkedHashSet<String> all = new LinkedHashSet<>();
            if (recentFiles != null) {
                for (String r : recentFiles) {
                    if (r != null && r.trim().length() > 0) {
                        all.add(r);
                    }
                }
            }
            List<String> ok = new ArrayList<>(all);
            if (!all.contains(recentFile)) {
                ok.add(0, recentFile);
            }
            this.recentFiles = ok;
        }
    }

    public AppWindowDisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(AppWindowDisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    public Boolean getDisplayToolBar() {
        return displayToolBar;
    }

    public void setDisplayToolBar(Boolean displayToolBar) {
        this.displayToolBar = displayToolBar;
    }

    public Boolean getDisplayStatusBar() {
        return displayStatusBar;
    }

    public void setDisplayStatusBar(Boolean displayStatusBar) {
        this.displayStatusBar = displayStatusBar;
    }

    public List<String> getRecentContentTypes() {
        return recentContentTypes;
    }

    public void setRecentContentTypes(List<String> recentContentTypes) {
        this.recentContentTypes = recentContentTypes;
    }
    
}

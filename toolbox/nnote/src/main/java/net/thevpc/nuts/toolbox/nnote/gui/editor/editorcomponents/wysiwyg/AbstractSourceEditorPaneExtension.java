/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.wysiwyg;

import net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.wysiwyg.SourceEditorPaneExtension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.text.EditorKit;
import net.thevpc.echo.Application;
import net.thevpc.echo.swing.core.swing.SwingApplicationsHelper;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteTypes;

/**
 *
 * @author vpc
 */
public abstract class AbstractSourceEditorPaneExtension implements SourceEditorPaneExtension {

    public void addAction(String id, Action a, JToolBar bar, JPopupMenu popup, Context context) {
        Action pa = prepareAction(id, a, context);
        if (bar != null) {
            bar.add(pa);
        }
        if (popup != null) {
            popup.add(pa);
        }
    }

    public void addSeparator(JToolBar bar, JPopupMenu popup, Context context) {
        if (bar != null) {
            bar.addSeparator();
        }
        if (popup != null) {
            popup.addSeparator();
        }
    }

    public Action uninstallAction(Action a, Context context) {
        SwingApplicationsHelper.unregisterAction(a, context.getApp());
        return a;
    }
    
    public Action prepareAction(String id, Action a, Context context) {
        SwingApplicationsHelper.registerAction(a, "Action." + id, "$Action." + id + ".icon", context.getApp());
        context.add(a);
        return a;
    }

    public void addContentTypeChangeListener(Context context, ContentTypeChangeListener listener) {
        context.getPane().addPropertyChangeListener("editorKit", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                EditorKit ek = (EditorKit) evt.getNewValue();
                String ct = ek == null ? "" : ek.getContentType();
                if (ct == null) {
                    ct = "";
                }
                listener.onContentTypeChanged(NNoteTypes.normalizeContentType(ct), context);
            }
        }
        );

    }

    public static interface ContentTypeChangeListener {

        void onContentTypeChanged(String contentType, Context context);
    }

    public static class Context {

        private List<Action> actions = new ArrayList<>();
        private Application app;
        private JEditorPane pane;

        public Context(Application app, JEditorPane pane) {
            this.app = app;
            this.pane = pane;
        }

        public JEditorPane getPane() {
            return pane;
        }

        public Application getApp() {
            return app;
        }

        public void setApp(Application app) {
            this.app = app;
        }

        public void add(Action a) {
            actions.add(a);
        }

        public void setActionVisible(Action a, boolean b) {
            a.putValue("visible", b);
        }

        public void setActionEnabled(Action a, boolean b) {
            a.putValue("enabled", b);
        }

        public void setAllActionsVisible(boolean b) {
            for (Action action : actions) {
                setActionVisible(action, b);
            }
        }

        public void setAllActionsEnabled(boolean b) {
            for (Action action : actions) {
                setActionEnabled(action, b);
            }
        }

        public List<Action> getActions() {
            return actions;
        }

    }
}

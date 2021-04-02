/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.wysiwyg;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTML;
import net.thevpc.common.swing.JDropDownButton;
import net.thevpc.echo.Application;
import net.thevpc.echo.swing.core.swing.SwingApplicationsHelper;
import net.thevpc.jeep.editor.JEditorPaneBuilder;
import net.thevpc.nuts.toolbox.nnote.gui.util.CutomHTMLAction;
import net.thevpc.common.swing.RectColorIcon;

/**
 *
 * @author vpc
 */
public class SourceEditorPanePanelHtmlExtension extends AbstractSourceEditorPaneExtension {

    AbstractSourceEditorPaneExtension.Context context;

    @Override
    public void uninstall(JEditorPaneBuilder editorBuilder, Application app) {
        if (context != null) {
            for (Action action : context.getActions()) {
                uninstallAction(action, context);
            }
        }
    }

    @Override
    public void prepareEditor(JEditorPaneBuilder editorBuilder, boolean compactMode, Application app) {
        JEditorPane pane = editorBuilder.editor();
        JPopupMenu popup = pane.getComponentPopupMenu();
        JToolBar bar = compactMode ? null : new JToolBar();
        context = new AbstractSourceEditorPaneExtension.Context(app, pane);
        addAction("font-bold", new StyledEditorKit.BoldAction(), bar, popup, context);
        addAction("font-italic", new StyledEditorKit.ItalicAction(), bar, popup, context);
        addAction("font-underline", new StyledEditorKit.UnderlineAction(), bar, popup, context);
        addSeparator(bar, popup, context);
        addAction("align-left", new StyledEditorKit.AlignmentAction("align-left", StyleConstants.ALIGN_LEFT), bar, popup, context);
        addAction("align-center", new StyledEditorKit.AlignmentAction("align-center", StyleConstants.ALIGN_CENTER), bar, popup, context);
        addAction("align-right", new StyledEditorKit.AlignmentAction("align-right", StyleConstants.ALIGN_RIGHT), bar, popup, context);
        addAction("align-justify", new StyledEditorKit.AlignmentAction("align-justify", StyleConstants.ALIGN_JUSTIFIED), bar, popup, context);

        addSeparator(bar, null, context);
        addAction("insert-ul", new CutomHTMLAction("insert-ul", "<ul><li>item</li><li>item</li><li>item</li></ul>", HTML.Tag.P, HTML.Tag.UL), bar, null, context);
        addAction("insert-ol", new CutomHTMLAction("insert-ol", "<ol><li>item</li><li>item</li><li>item</li></ol>", HTML.Tag.P, HTML.Tag.OL), bar, null, context);

        JDropDownButton insertMenu = new JDropDownButton("");
        SwingApplicationsHelper.registerButton(insertMenu, null, "insert-tag", app);
        insertMenu.setQuickActionDelay(0);
        insertMenu.add(prepareAction("insert-hr", new CutomHTMLAction("insert-hr", "<hr>", HTML.Tag.P, HTML.Tag.HR), context));
        insertMenu.add(prepareAction("insert-break", new StyledEditorKit.InsertBreakAction(), context));
        insertMenu.add(prepareAction("insert-tab", new StyledEditorKit.InsertTabAction(), context));
        insertMenu.add(prepareAction("insert-h1", new CutomHTMLAction("insert-h1", "<h1></h1>", HTML.Tag.P, HTML.Tag.H1), context));
        insertMenu.add(prepareAction("insert-h2", new CutomHTMLAction("insert-h2", "<h2></h2>", HTML.Tag.P, HTML.Tag.H2), context));
        insertMenu.add(prepareAction("insert-h3", new CutomHTMLAction("insert-h3", "<h3></h3>", HTML.Tag.P, HTML.Tag.H3), context));
        insertMenu.add(prepareAction("insert-h4", new CutomHTMLAction("insert-h4", "<h4></h4>", HTML.Tag.P, HTML.Tag.H4), context));
        if (bar != null) {
            bar.add(insertMenu);
        }

        JDropDownButton fontTypeMenu = new JDropDownButton();
        SwingApplicationsHelper.registerButton(fontTypeMenu, null, "font-type", app);
        fontTypeMenu.setQuickActionDelay(0);
        if (bar != null) {
            bar.add(fontTypeMenu);
        }
        String[] fontTypes = {"SansSerif", "Serif", "Monospaced", "Dialog", "DialogInput"};
        for (int i = 0; i < fontTypes.length; i++) {
            JMenuItem nextTypeItem = new JMenuItem(fontTypes[i]);
            nextTypeItem.setAction(new StyledEditorKit.FontFamilyAction(fontTypes[i], fontTypes[i]));
            fontTypeMenu.add(nextTypeItem);
        }

        JDropDownButton fontSizeMenu = new JDropDownButton("");
        SwingApplicationsHelper.registerButton(fontSizeMenu, null, "font-size", app);
        fontSizeMenu.setQuickActionDelay(0);
        if (bar != null) {
            bar.add(fontSizeMenu);
        }
        int[] fontSizes = {6, 8, 10, 12, 14, 16, 20, 24, 32, 36, 48, 72};
        for (int i = 0; i < fontSizes.length; i++) {
            JMenuItem nextSizeItem = new JMenuItem(String.valueOf(fontSizes[i]));
            nextSizeItem.setAction(new StyledEditorKit.FontSizeAction(String.valueOf(fontSizes[i]), fontSizes[i]));
            fontSizeMenu.add(nextSizeItem);
        }

        Map<String, Color> colMap = new LinkedHashMap<>();
        colMap.put("Color.blue", Color.blue);
        colMap.put("Color.red", Color.red);
        colMap.put("Color.yellow", Color.yellow);
        colMap.put("Color.gray", Color.gray);
        colMap.put("Color.green", Color.green);
        colMap.put("Color.orange", Color.orange);
        colMap.put("Color.cyan", Color.cyan);
        colMap.put("Color.black", Color.black);
        colMap.put("Color.white", Color.white);
        JDropDownButton colorsMenu = new JDropDownButton("");
        SwingApplicationsHelper.registerButton(colorsMenu, null, "colors", app);
        colorsMenu.setQuickActionDelay(0);
        if (bar != null) {
            bar.add(colorsMenu);
        }

        for (Map.Entry<String, Color> entry : colMap.entrySet()) {
            JMenuItem nextSizeItem = new JMenuItem(entry.getKey());
            Color color = entry.getValue();
            StyledEditorKit.ForegroundAction foregroundAction = new StyledEditorKit.ForegroundAction(entry.getKey(), entry.getValue());
            SwingApplicationsHelper.registerAction(foregroundAction, entry.getKey(), null, app);
            foregroundAction.putValue(AbstractAction.SMALL_ICON, new RectColorIcon(color));
            nextSizeItem.setAction(foregroundAction);
            colorsMenu.add(nextSizeItem);
        }

        addContentTypeChangeListener(context, new ContentTypeChangeListener() {
            @Override
            public void onContentTypeChanged(String contentType, Context context) {
                boolean isHtml = "text/html".equals(contentType);
                if (bar != null) {
                    bar.setVisible(isHtml);
                }
                context.setAllActionsVisible(isHtml);
                context.setAllActionsEnabled(isHtml);
            }
        });
        if (bar != null) {
            editorBuilder.header().add(bar);
        }
        boolean isHtml = false;//"text/html".equals(contentType);
        if (bar != null) {
            bar.setVisible(isHtml);
        }
        context.setAllActionsVisible(isHtml);
        context.setAllActionsEnabled(isHtml);
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.richeditor;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.text.StyledEditorKit;
import net.thevpc.common.swing.JDropDownButton;
import net.thevpc.echo.swing.core.swing.SwingApplicationsHelper;
import net.thevpc.jeep.editor.JEditorPaneBuilder;
import net.thevpc.common.swing.RectColorIcon;
import net.thevpc.echo.Application;
import net.thevpc.more.shef.AlignEnum;
import net.thevpc.more.shef.BlocEnum;
import net.thevpc.more.shef.InlineStyleEnum;
import net.thevpc.more.shef.ShefHelper;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.source.AbstractSourceEditorPaneExtension;

/**
 *
 * @author vpc
 */
public class SourceEditorPanePanelHtmlExtension extends AbstractSourceEditorPaneExtension {

    AbstractSourceEditorPaneExtension.Context context;

    @Override
    public void uninstall(JEditorPaneBuilder editorBuilder, NNoteGuiApp sapp) {
        if (context != null) {
            for (Action action : context.getActions()) {
                uninstallAction(action, context);
            }
        }
    }

    @Override
    public void prepareEditor(JEditorPaneBuilder editorBuilder, boolean compactMode, NNoteGuiApp sapp) {
        JEditorPane editor = editorBuilder.editor();
        JPopupMenu popup = editor.getComponentPopupMenu();
        JToolBar bar = compactMode ? null : new JToolBar();
        Application app = sapp.app();
        context = new AbstractSourceEditorPaneExtension.Context(sapp, editor);

        JDropDownButton blocMenu = new JDropDownButton("");
        SwingApplicationsHelper.registerButton(blocMenu, null, "insert-bloc", sapp.app());
        blocMenu.setQuickActionDelay(0);
        blocMenu.add(prepareAction("insert-h1", al((e) -> ShefHelper.runInsertBloc(editor, BlocEnum.H1)), context));
        blocMenu.add(prepareAction("insert-h2", al((e) -> ShefHelper.runInsertBloc(editor, BlocEnum.H2)), context));
        blocMenu.add(prepareAction("insert-h3", al((e) -> ShefHelper.runInsertBloc(editor, BlocEnum.H3)), context));
        blocMenu.add(prepareAction("insert-h4", al((e) -> ShefHelper.runInsertBloc(editor, BlocEnum.H4)), context));
        blocMenu.add(prepareAction("insert-h5", al((e) -> ShefHelper.runInsertBloc(editor, BlocEnum.H5)), context));
        blocMenu.add(prepareAction("insert-h6", al((e) -> ShefHelper.runInsertBloc(editor, BlocEnum.H6)), context));
        blocMenu.add(prepareAction("insert-pre", al((e) -> ShefHelper.runInsertBloc(editor, BlocEnum.PRE)), context));
        blocMenu.add(prepareAction("insert-div", al((e) -> ShefHelper.runInsertBloc(editor, BlocEnum.DIV)), context));
        blocMenu.add(prepareAction("insert-p", al((e) -> ShefHelper.runInsertBloc(editor, BlocEnum.P)), context));
        blocMenu.add(prepareAction("insert-blockquote", al((e) -> ShefHelper.runInsertBloc(editor, BlocEnum.BLOCKQUOTE)), context));
        blocMenu.add(prepareAction("insert-ol", al((e) -> ShefHelper.runInsertBloc(editor, BlocEnum.OL)), context));
        blocMenu.add(prepareAction("insert-ul", al((e) -> ShefHelper.runInsertBloc(editor, BlocEnum.UL)), context));
        if (bar != null) {
            bar.add(blocMenu);
        }

        addActionListener("font-bold", (e) -> ShefHelper.runToggleInlineStyle(editor, InlineStyleEnum.BOLD), bar, popup, context);
        addActionListener("font-italic", (e) -> ShefHelper.runToggleInlineStyle(editor, InlineStyleEnum.ITALIC), bar, popup, context);
        addActionListener("font-underline", (e) -> ShefHelper.runToggleInlineStyle(editor, InlineStyleEnum.UNDERLINE), bar, popup, context);
        addActionListener("font-strike", (e) -> ShefHelper.runToggleInlineStyle(editor, InlineStyleEnum.STRIKE), bar, popup, context);
        addActionListener("font-sup", (e) -> ShefHelper.runToggleInlineStyle(editor, InlineStyleEnum.SUP), bar, popup, context);
        addActionListener("font-sub", (e) -> ShefHelper.runToggleInlineStyle(editor, InlineStyleEnum.SUB), bar, popup, context);
        addActionListener("font-strong", (e) -> ShefHelper.runToggleInlineStyle(editor, InlineStyleEnum.STRONG), bar, popup, context);
        addActionListener("font-em", (e) -> ShefHelper.runToggleInlineStyle(editor, InlineStyleEnum.EM), bar, popup, context);
        addActionListener("font-cite", (e) -> ShefHelper.runToggleInlineStyle(editor, InlineStyleEnum.CITE), bar, popup, context);
        addSeparator(bar, popup, context);
        addActionListener("align-left", (e) -> ShefHelper.runTextAlign(editor, AlignEnum.LEFT), bar, popup, context);
        addActionListener("align-center", (e) -> ShefHelper.runTextAlign(editor, AlignEnum.CENTER), bar, popup, context);
        addActionListener("align-right", (e) -> ShefHelper.runTextAlign(editor, AlignEnum.RIGHT), bar, popup, context);
        addActionListener("align-justify", (e) -> ShefHelper.runTextAlign(editor, AlignEnum.JUSTIFY), bar, popup, context);

        addSeparator(bar, null, context);
        addActionListener("insert-ul", (e) -> ShefHelper.runInsertBloc(editor, BlocEnum.UL), bar, null, context);
        addActionListener("insert-ol", (e) -> ShefHelper.runInsertBloc(editor, BlocEnum.OL), bar, null, context);

        JDropDownButton insertMenu = new JDropDownButton("");
        SwingApplicationsHelper.registerButton(insertMenu, null, "insert-tag", sapp.app());
        insertMenu.setQuickActionDelay(0);
        insertMenu.add(prepareAction("insert-hr", al((e) -> ShefHelper.runInsertHorizontalRule(editor)), context));
        insertMenu.add(prepareAction("insert-break", new StyledEditorKit.InsertBreakAction(), context));
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
        boolean isHtml = "text/html".equals(editor.getContentType());
        if (bar != null) {
            bar.setVisible(isHtml);
        }
        context.setAllActionsVisible(isHtml);
        context.setAllActionsEnabled(isHtml);
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.search;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import net.thevpc.common.swing.SwingUtilities3;
import net.thevpc.echo.AppToolWindow;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;
import net.thevpc.nuts.toolbox.nnote.service.search.strsearch.StringSearchResult;

/**
 *
 * @author vpc
 */
public class SearchResultPanel extends JPanel {

    private NNoteGuiApp sapp;
    private AppToolWindow resultPanelTool;
    private SearchResultPanelItem item;

    public SearchResultPanel(NNoteGuiApp sapp) {
        super(new BorderLayout());
        this.sapp = sapp;
        item = new SearchResultPanelItemImpl();
        add((JComponent) item);
    }

    public SearchResultPanelItem createNewPanel() {
        return item;
    }

    public AppToolWindow getResultPanelTool() {
        return resultPanelTool;
    }

    public void setResultPanelTool(AppToolWindow resultPanelTool) {
        this.resultPanelTool = resultPanelTool;
    }

    public void showResults() {
        resultPanelTool.active().set(true);
    }

    public static interface SearchResultPanelItem {

        void appendResult(StringSearchResult<VNNote> x);

        boolean isSearching();

        void resetResults();

        void setSearching(boolean b);

    }

    public static class SearchResultPanelItemImpl extends JPanel implements SearchResultPanelItem {

        private boolean searching;
        private JTable table = new JTable();
        private DefaultTableModel model = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        public SearchResultPanelItemImpl() {
            super(new BorderLayout());
            model.setColumnIdentifiers(new Object[]{
                "Position",
                "Name",
                "Line"
            });
            table.setModel(model);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(50, 50));
            add(scroll);
        }

        public void resetResults() {
            SwingUtilities3.invokeLater(() -> {
                while (model.getRowCount() > 0) {
                    model.removeRow(0);
                }
            });
        }

        public void appendResult(StringSearchResult<VNNote> x) {
            SwingUtilities3.invokeLater(() -> {
                model.addRow(new Object[]{
                    String.valueOf(x.getRow()) + ":" + String.valueOf(x.getColumn()),
                    x.getObject().getName(),
                    x
                });
            });
        }

        public void setSearching(boolean b) {
            this.searching = b;
        }

        public boolean isSearching() {
            return searching;
        }
    }
}

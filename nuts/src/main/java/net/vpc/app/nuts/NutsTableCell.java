/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

/**
 *
 * @author vpc
 */
public interface NutsTableCell {

    //        public RenderedCell getRendered() {
    //            return rendered;
    //        }
    //        public void setRendered(RenderedCell rendered) {
    //            this.rendered = rendered;
    //        }
    int getColspan();

    NutsTableCell setColspan(int colspan);

    int getRowspan();

    NutsTableCell setRowspan(int rowspan);

    int getX();

    int getY();

    Object getValue();

    NutsTableCell setValue(Object value);

    NutsTableCellFormat getFormatter();

    NutsTableCell setFormatter(NutsTableCellFormat formatter);
    
}

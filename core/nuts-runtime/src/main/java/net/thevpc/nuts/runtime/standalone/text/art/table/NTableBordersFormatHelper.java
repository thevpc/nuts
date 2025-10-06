package net.thevpc.nuts.runtime.standalone.text.art.table;

import net.thevpc.nuts.text.NTableBordersFormat;
import net.thevpc.nuts.text.NTableSeparator;
import net.thevpc.nuts.runtime.standalone.text.art.region.NTextRegion;
import net.thevpc.nuts.runtime.standalone.text.art.region.NTextRegionImpl;

public class NTableBordersFormatHelper {
    private NTableBordersFormat base;
    private NTextRegion[] all;
    private int startHorizontalBorderSize;
    private int startVerticalBorderSize;

    public NTableBordersFormatHelper(NTableBordersFormat base) {
        this.base = base;
        NTableSeparator[] values = NTableSeparator.values();
        this.all = new NTextRegion[values.length];
        for (int i = 0; i < values.length; i++) {
            NTableSeparator value = values[i];
            NTextRegionImpl r = new NTextRegionImpl(base.format(value));
            this.all[i] = r;
            switch (value) {
                case FIRST_ROW_LINE:
                case MIDDLE_ROW_LINE:
                case LAST_ROW_LINE: {
                    startVerticalBorderSize = Math.max(1, r.rows());
                    break;
                }
                case ROW_START:
                case ROW_SEP:
                case ROW_END: {
                    startHorizontalBorderSize = Math.max(1, r.columns());
                    break;
                }
                default: {
                    startHorizontalBorderSize = Math.max(1, r.columns());
                    startVerticalBorderSize = Math.max(1, r.rows());
                    break;
                }
            }
        }
        for (int i = 0; i < values.length; i++) {
            NTableSeparator value = values[i];
            NTextRegionImpl r = new NTextRegionImpl(base.format(value));
            this.all[i] = r;
            switch (value) {
                case FIRST_ROW_LINE:
                case MIDDLE_ROW_LINE:
                case LAST_ROW_LINE: {
                    startVerticalBorderSize = Math.max(1, r.rows());
                    break;
                }
                case ROW_START:
                case ROW_SEP:
                case ROW_END: {
                    startHorizontalBorderSize = Math.max(1, r.columns());
                    break;
                }
                default: {
                    startHorizontalBorderSize = Math.max(1, r.columns());
                    startVerticalBorderSize = Math.max(1, r.rows());
                    break;
                }
            }
        }
    }

    public NTextRegion get(NTableSeparator separator) {
        return all[separator.ordinal()];
    }

    public NTextRegion line(NTableSeparator separator,int count) {
        NTextRegion r = all[separator.ordinal()];
        if(r.columns()>count){
            return r.subRegion(0,0,count,r.rows());
        }
        for (int i = 1; i < count; i++) {
            r=r.concatHorizontally(r);
            if(r.columns()>count){
                return r.subRegion(0,0,count,r.rows());
            }
        }
        return r;
    }

    public NTextRegion column(NTableSeparator separator,int count) {
        NTextRegion r = all[separator.ordinal()];
        if(r.rows()>count){
            return r.subRegion(0,0,r.columns(),count);
        }
        for (int i = 1; i < count; i++) {
            r=r.concatVertically(r);
            if(r.rows()>count){
                return r.subRegion(0,0,r.columns(),count);
            }
        }
        return r;
    }

    public int getStartHorizontalBorderSize() {
        return startHorizontalBorderSize;
    }

    public int getStartVerticalBorderSize() {
        return startVerticalBorderSize;
    }



}

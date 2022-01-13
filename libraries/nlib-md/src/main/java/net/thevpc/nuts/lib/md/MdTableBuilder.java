package net.thevpc.nuts.lib.md;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MdTableBuilder implements MdElementBuilder{
    private List<MdColumnBuilder> columns = new ArrayList<>();
    private List<MdRowBuilder> rows = new ArrayList<>();


    public MdTable build() {
        return new MdTable(
                columns.stream().map(x->x.build()).toArray(MdColumn[]::new),
                rows.stream().map(x->x.build()).toArray(MdRow[]::new)
        );
    }

    public MdTableBuilder addColumns(MdColumnBuilder ... c){
        columns.addAll(Arrays.asList(c));
        return this;
    }

    public MdTableBuilder addRows(MdRowBuilder ... c){
        rows.addAll(Arrays.asList(c));
        return this;
    }

    public MdTableBuilder addColumns(MdColumn ... c){
        columns.addAll(Arrays.asList(c).stream().map(x -> new MdColumnBuilder(x)).collect(Collectors.toList()));
        return this;
    }

    public MdTableBuilder addRows(MdRow ... c){
        rows.addAll(Arrays.asList(c).stream().map(x -> new MdRowBuilder(x)).collect(Collectors.toList()));
        return this;
    }

    public static class MdColumnBuilder {
        private MdElementBuilder name;
        private MdHorizontalAlign horizontalAlign;

        public MdColumnBuilder() {
        }

        public MdColumnBuilder(MdColumn c) {
            this.name = MdFactory.element(c.getName());
            this.horizontalAlign = c.getHorizontalAlign();
        }

        public MdElementBuilder getName() {
            return name;
        }

        public MdColumnBuilder setName(String name) {
            return setName(MdFactory.text(name));
        }

        public MdColumnBuilder setName(MdElement name) {
            return setName(MdFactory.element(name));
        }

        public MdColumnBuilder setName(MdElementBuilder name) {
            this.name = name;
            return this;
        }

        public MdHorizontalAlign getHorizontalAlign() {
            return horizontalAlign;
        }

        public MdColumnBuilder setHorizontalAlign(MdHorizontalAlign horizontalAlign) {
            this.horizontalAlign = horizontalAlign;
            return this;
        }

        public MdColumn build() {
            return new MdColumn(
                    name == null ? MdText.empty() : name.build(),
                    horizontalAlign == null ? MdHorizontalAlign.LEFT : horizontalAlign
            );
        }
    }

    public static class MdRowBuilder {
        private List<MdElementBuilder> cells = new ArrayList<>();
        private boolean header;

        public MdRowBuilder() {
        }

        public MdRowBuilder(MdRow c) {
            this.header = c.isHeader();
            this.cells.addAll(Arrays.stream(c.getCells()).map(x->MdFactory.element(x)).collect(Collectors.toList()));
        }

        public List<MdElementBuilder> getCells() {
            return cells;
        }

        public MdRowBuilder setCells(List<MdElementBuilder> cells) {
            this.cells = cells;
            return this;
        }

        public MdRowBuilder addCells(MdElementBuilder... cells) {
            this.cells.addAll(Arrays.asList(cells));
            return this;
        }

        public MdRowBuilder addCells(MdElement... cells) {
            this.cells.addAll(Arrays.asList(cells).stream().map(x->
                    MdFactory.element(x)
            ).collect(Collectors.toList()));
            return this;
        }

        public boolean isHeader() {
            return header;
        }

        public MdRowBuilder setHeader(boolean header) {
            this.header = header;
            return this;
        }

        public MdRow build() {
            return new MdRow(
                    cells.stream().map(x->x.build()).toArray(MdElement[]::new),
                    header
            );
        }
    }


}

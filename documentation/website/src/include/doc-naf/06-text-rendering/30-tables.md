---
title: Rendering Tables
---


One of the most powerful text rendering features in `NAF` is its ability to render structured tables directly in the terminal. This is made possible through the NTextArt API, which can render tabular data with automatic alignment, wrapping, spanning, and per-cell styling — all while remaining fully compatible with Nuts’ messaging and text system (NText, NMsg, NOut, etc.).

Unlike ad-hoc printf-based formatting, NTextArt tables are aware of structure, style, and layout. They handle complex cases such as multiline cells, column and row spanning, per-cell styling, and even semantic rendering (e.g., italic, success, error) without losing readability.

## Basic Usage

To render a table, create a NMutableTableModel, populate it with rows and optional headers, and pass it to a NTextArtTableRenderer.

```java
NMutableTableModel table = NTableModel.of()
.addHeaderRow(NTableCell.of(NText.of("Name")), NTableCell.of(NText.of("Status")))
.addRow(NTableCell.of(NText.of("adam")), NTableCell.of(NText.ofStyled("active", NTextStyle.italic())))
.addRow(NTableCell.of(NText.of("eve")),  NTableCell.of(NText.ofStyled("inactive", NTextStyle.success())));

NOut.println(NTextArt.of().tableRenderer().get().render(table));
```

Output:
```
+------+----------+
| Name | Status   |
+------+----------+
| adam | active   |
| eve  | inactive |
+------+----------+

```

You can choose from multiple built-in renderers (e.g. "table:ascii", "table:spaces") or register your own custom renderer.


## Advanced Features

Cells can contain multiple lines of text, and the renderer will automatically adjust row heights:


```java
NMutableTableModel table = NTableModel.of()
    .addRow(NTableCell.of(NText.of("adam\nwas\nhere"), NTableCell.of(NText.of("active")))
    .addRow(NTableCell.of(NText.of("eve"), NTableCell.of(NText.of("inactive")));
```

Output:
```
+------------------------+
| adam                  |
| was                   |
| here                  |
+------------+----------+
| adam       | adam     |
| here       | is here  |
+------------+----------+
```

### Column Spanning (colspan)

Cells can span across multiple columns, just like in HTML tables:

```java
NMutableTableModel table = NTableModel.of()
    .addRow(NTableCell.of(NText.of("adam\nwas\nhere"),2,1)) //colspan=2, rowspan=1
    .addRow(NText.of("adam\nhere"), NText.of("adam\nis\nhere"))
        ; // first cell spans 2 columns

NMutableTableModel sameTable = NTableModel.of()
    .addRow(NTableCellBuilder.of(NText.of("adam\nwas\nhere")).colspan(2).horizontalAlign(NPositionType.FIRST)) //colspan=2, align=left
    .addRow(NText.of("adam\nhere"), NText.of("adam\nis\nhere"))
        ; // first cell spans 2 columns

```

Result:
```
+------------------------+
| adam                  |
| was                   |
| here                  |
+------------+----------+
| adam       | adam     |
| here       | is here  |
+------------+----------+
```

### Row Spanning (rowspan)

Cells can also span vertically across rows:


```java
NMutableTableModel table = NTableModel.of()
    .addRow(NTableCell.of(NText.of("tall\ncell\nvery\ntall"),1,2), NText.of("short"))
    .addRow(NText.of("another"))
    .setCellRowSpan(0, 0, 2); // span horizontally over 2 rows

NMutableTableModel sameTable = NTableModel.of()
        .addRow(NTableCellBuilder.of(NText.of("adam\nwas\nhere")).rowpan(2).horizontalAlign(NPositionType.FIRST)) //colspan=2, align=left
        .addRow(NText.of("adam\nhere"), NText.of("adam\nis\nhere"))
        ;

```

### Mixed Column Counts

Rows can have different numbers of columns. The renderer handles layout automatically:

```java
NMutableTableModel table = NTableModel.of()
    .addRow(NTableCell.of(NText.of("adam\nwas\nhere"),3,1))
    .addRow(NText.of("adam\nhere"), NText.of("adam\nis\nhere"), NText.of(3))
    .setCellColSpan(0, 0, 3);
```

### Per-Cell Styling

Cells can carry formatting information using NTextStyle. This enables bold, italic, color, semantic meaning (e.g., success, error), and more.

```java
.addRow(NTableCell.of(NText.of("adam"), NTableCell.of(NText.ofStyled("active", NTextStyle.italic())))
.addRow(NTableCell.of(NText.of("eve"),  NTableCell.of(NText.ofStyled("inactive", NTextStyle.success())));
```

## Multiple Renderers

Different renderers can be used for different table aesthetics or output contexts. For example:

```java
NOut.println(art.getTableRenderer("table:spaces").get().render(table));
```

This renderer uses space padding instead of ASCII borders — useful for compact, plain-text output.

You can also iterate over all registered renderers:


```java
for (NTextArtTableRenderer renderer : art.tableRenderers()) {
    NOut.println(renderer.getName() + "::");
    NOut.println(renderer.render(table));
}
```

## Performance Considerations

Rendering tables is efficient, but when dealing with thousands of rows, consider paginating or streaming rows instead of rendering all at once.

Cell layout calculations (especially with spanning) are cached internally to minimize overhead.

## Why NTextArt Tables Matter

Because NTextArt tables are semantic and structure-aware:
- They understand cell spanning, multiline content, and style.
- They integrate seamlessly with NText, NMsg, and NOut.
- They’re renderer-agnostic — the same model can be rendered as ASCII, space-aligned text, or even graphical pixel art in the future.
- They form a foundation for higher-level features like search results, dependency trees, and diagnostics in Nuts CLI.

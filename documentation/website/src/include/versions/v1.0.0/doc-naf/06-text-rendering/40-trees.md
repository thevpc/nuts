---
title: Rendering Trees
---

For hierarchical data like dependency graphs, process hierarchies, or nested objects, `NAF` provides tree rendering through the `NTextArt` API.

## Basic Usage

To render a tree, construct a hierarchy of `NTreeNode` instances and render them using `NTextArtTreeRenderer`:

```java
NTreeNode root = NTreeNode.of(NText.of("Root"),
        NTreeNode.of(NText.of("Child 1")),
        NTreeNode.of(NText.of("Child 2"),
                NTreeNode.of(NText.of("Grandchild A")),
                NTreeNode.of(NText.of("Grandchild B"))
        )
);

NOut.println(NTextArt.of().treeRenderer().get().render(root));
```

Output:

```
Root
├─ Child 1
└─ Child 2
   ├─ Grandchild A
   └─ Grandchild B
```

## Configuring the Renderer

You can customize rendering behavior directly on the `NTextArtTreeRenderer` instance, such as hiding the root element or selecting specific renderers:

```java
NTextArt art = NTextArt.of();

// Hide the root node
NText result = art.treeRenderer().get()
        .omitRoot(true)
        .render(root);

// Or load a named renderer
NTextArtTreeRenderer customRenderer = NTextArtTreeRenderer.of("tree:compact");
NOut.println(customRenderer.render(root));
```

## Custom Node Models & Nested Components

`NTreeNode` is an interface with two core methods: `content()` and `children()`. 
Because `content()` returns an `NText`, you can embed complex rich text or even rendered components (like `NTableModel`) inside tree nodes:

```java
static class TableNode implements NTreeNode {
    private final int value;
    private final NTextArt art;

    public TableNode(int value, NTextArt art) {
        this.value = value;
        this.art = art;
    }

    @Override
    public NText content() {
        return art.tableRenderer().get().render(
            NTableModel.of().addRow(NTableCell.of(NText.of(value)))
        );
    }

    @Override
    public List<NTreeNode> children() {
        return (value < 3) 
            ? Arrays.asList(value + 1, value + 2).stream()
                    .map(v -> new TableNode(v, art))
                    .collect(Collectors.toList()) 
            : Collections.emptyList();
    }
}

NTextArt art = NTextArt.of();
NTreeNode tree = new TableNode(1, art);
NOut.println(art.treeRenderer().get().render(tree));
```

Output:

```

╭─╮
│1│
╰─╯
├── ╭─╮
│   │2│
│   ╰─╯
│   ├── ╭─╮
│   │   │3│
│   │   ╰─╯
│   └── ╭─╮
│       │4│
│       ╰─╯
└── ╭─╮
    │3│
    ╰─╯

```

## Anonymous Nodes
Nodes can carry blank content (via `NText.ofBlank()`), allowing you to structure groupings without introducing extra label text:

```java
NTreeNode tree = NTreeNode.of(NText.ofBlank(),
        NTreeNode.of(NText.of("siblings"),
                NTreeNode.of(NText.ofBlank(),
                        NTreeNode.of(NText.of("id=1")),
                        NTreeNode.of(NText.of("label=first"))
                ),
                NTreeNode.of(NText.ofBlank(),
                        NTreeNode.of(NText.of("id=2")),
                        NTreeNode.of(NText.of("label=second"))
                )
        )
);
```

## Rendering Hierarchical Objects

You can also serialize structured objects directly to tree format using `NObjectObjectWriter` and `NContentType.TREE`:

```java
Map<String, Object> map = NMaps.of(
        "a", 2,
        "b", NMaps.of("c", new Object[]{ NMaps.of("e", 3), NMaps.of("e", 3), 3 }, "d", 3),
        "d", NMaps.of("e", 3)
);

NObjectObjectWriter.of()
        .outputFormat(NContentType.TREE)
        .println(map, NOut.get());
```



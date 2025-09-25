---
title: NTextArt — Turn Text into Art
subTitle:  |
  Beyond ASCII banners and pixel-style visuals, <code>NTextArt</code>
  also supports structured data rendering. You can display tables with
  aligned rows and columns, and hierarchical trees where each node can
  contain multi-line or styled text — even full tables. Branches,
  indentation, and node formatting are handled automatically, making
  it easy to visualize complex hierarchical or tabular data directly
  in the terminal with a clean, readable layout.
contentType: java
---

    class MyNode implements NTreeNode {
        int value;
        public MyNode(int value) { this.value = value;}
        @Override
        public NText value() {
            return art.getTableRenderer().get().render(NTableModel.of().addRow(NText.of(value)));
        }
        @Override
        public List&lt;NTreeNode> children() {
            return (value < 3) ? Arrays.&lt;Integer>asList(value + 1, value + 2).stream().map(MyNode::new).collect(Collectors.toList())
                    : Collections.emptyList();
        }
    }
    NTreeNode tree = new MyNode(1);
    NOut.println(art.getTreeRenderer().get().render(tree));
    //   ╭─╮
    //   │1│
    //   ╰─╯
    //   ├── ╭─╮
    //   │   │2│
    //   │   ╰─╯
    //   │   ├── ╭─╮
    //   │   │   │3│
    //   │   │   ╰─╯
    //   │   └── ╭─╮
    //   │       │4│
    //   │       ╰─╯
    //   └── ╭─╮
    //       │3│
    //       ╰─╯

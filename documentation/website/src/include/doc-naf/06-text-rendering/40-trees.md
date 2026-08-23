---
title: Rendering Trees
---



For hierarchical data like dependency graphs or process hierarchies:

```java
NTreeNode root = 
        NTreeNode.of(NText.of("Root"),
                NTreeNode.of(NText.of("Child 1")),
                NTreeNode.of(NText.of("Child 2"),
                        NTreeNode.of(NText.of("Grandchild A")),
                        NTreeNode.of(NText.of("Grandchild B"))
                )
        )
;

NOut.println(NTextArt.of().treeRenderer().get().render(root));
```

Result :

```
Root
├─ Child 1
└─ Child 2
   ├─ Grandchild A
   └─ Grandchild B
```

NTextArt integrates seamlessly with tree rendering as well. You can render a tree whose nodes themselves contain rendered tables:

```java

class MyNode implements NTreeNode {
    int value;
    public MyNode(int value) { this.value = value; }
    @Override
    public NText content() {
        return art.tableRenderer().get().render(
            NTableModel.of().addRow(NText.of(value))
        );
    }
    @Override
    public List<NTreeNode> children() {
        return value < 3 ? Arrays.asList(value+1, value+2).stream().map(MyNode::new).collect(Collectors.toList()) : List.of();
    }
}
NTreeNode tree = new MyNode(1);
NOut.println(art.treeRenderer().get().render(tree));

```


Result: 

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

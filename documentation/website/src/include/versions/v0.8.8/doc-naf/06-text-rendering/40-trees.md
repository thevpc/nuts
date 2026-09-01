---
title: Rendering Trees
---



For hierarchical data like dependency graphs or process hierarchies:

```java
NTreeNode root = new MyNode("Root", List.of(
    new MyNode("Child 1"),
    new MyNode("Child 2", List.of(
        new MyNode("Grandchild A"),
        new MyNode("Grandchild B")
    ))
));

NOut.println(NTextArt.of().getTreeRenderer().get().render(root));
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
    public NText value() {
        return art.getTableRenderer().get().render(
            NTableModel.of().addRow(NText.of(value))
        );
    }
    @Override
    public List<NTreeNode> children() {
        return value < 3 ? Arrays.asList(value+1, value+2).stream().map(MyNode::new).collect(Collectors.toList()) : List.of();
    }
}
NTreeNode tree = new MyNode(1);
NOut.println(art.getTreeRenderer().get().render(tree));

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

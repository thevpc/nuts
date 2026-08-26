---
title: Structured Console Output with NTF
subTitle:  |
  <code>NAF</code> supports <code>NTF</code> (<strong>Nuts Text
  Format</strong>), enabling rich, structured, and styled console
  output. You can embed variables, apply colors or styles, and produce
  messages that are both human-readable and machine-friendly. This
  makes your CLI output more expressive, interactive, and easier to
  interpret.
contentType: java
---

NOut.println(
    NMsg.ofV("Hello ${user}, your task status is ${status}",
             NMaps.of(
                "user", "Alice",
                "status", NText.ofStyled("OK", NTextStyle.primary1())
    )
);
NOut.println(
    NMsg.ofC("Hello %s, your task status is %s",
            "Bob"
            NText.ofStyled("KO", NTextStyle.danger())
    )
);
NOut.println(
    NMsg.ofC("Hello %s, your task status is ##:danger KO##",
            "Meriam"
            "KO"
    )
);

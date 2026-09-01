---
title: Commandline
subTitle:  |
  NCmdLine lets you parse and handle command-line arguments with full
  flexibility, while keeping your code clean and readable. Define
  flags, options, and non-option arguments, all with automatic type
  handling and default values. With NAF, building sophisticated CLI
  tools becomes straightforward, letting you focus on functionality
  instead of parsing logic.
contentType: java
---

NCmdLine cmdLine = NApp.of().getCmdLine(); // or from somewhere else
NRef&lt;Boolean> boolOption = NRef.of(false);
NRef&lt;String> stringOption = NRef.ofNull();
List&lt;String> others = new ArrayList&lt;>();
while (cmdLine.hasNext()) {
    cmdLine.matcher()
            .with("-o", "--option").matchFlag((v) -> boolOption.set(v.booleanValue()))
            .with("-n", "--name").matchEntry((v) -> stringOption.set(v.stringValue()))
            .withNonOption().matchAny((v) -> stringOption.set(v.image()))
            .requireDefaults()
    ;
}
NOut.println(NMsg.ofC("boolOption=%s stringOption=%s others=%s", boolOption, stringOption, others));

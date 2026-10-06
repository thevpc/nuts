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

NCmdLine cmdLine = NApplication.of().cmdLine(); // or from somewhere else
NRef<Boolean> boolOption = NRef.of(false);
NRef<String> stringOption = NRef.ofNull();
List<String> others = new ArrayList<>();
while (cmdLine.hasNext()) {
    cmdLine.matcher()
            .when("-o", "--option").asFlag((v) -> boolOption.set(v.booleanValue()))
            .when("-n", "--name").asEntry((v) -> stringOption.set(v.stringValue()))
            .whenNonOption().asArg((v) -> stringOption.set(v.image()))
            .withDefaults()
            .require()
    ;
}
NOut.println(NMsg.ofC("boolOption=%s stringOption=%s others=%s", boolOption, stringOption, others));

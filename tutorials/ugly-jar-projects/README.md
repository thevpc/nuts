This folder contains some examples of projects implemented using the ugly flat-jar concept and hence without using `nuts`

* `base-project` gives an example of developing an application that does not use any maven plugin. It wont be executable unless you configure correctly your classpath and main class in the a long long java commandline. Still it is fully supported by `nuts` (without any `nuts` dependency of course).
* `dependency-project` rewrites the `base-project` using `maven-jar-plugin`, `maven-dependency-plugin` and `maven-antrun-plugin` to create a zip file that contains the app and its dependencies.
* `assembly-project` rewrites the `base-project` using `maven-assembly-plugin`  to create a jar file that contains a flattened view the app and its dependencies (uber jar).
* `shade-project` rewrites the `base-project` using `maven-shade-plugin` to create a jar file that contains a flattened view the app and its dependencies (uber jar).
* `onejar-project` rewrites the `base-project` using `onejar-maven-plugin` to create a jar file that contains a both the app and its dependencies as embedded jars (jar jar).  
* `spring-boot-maven-plugin` rewrites the `base-project` using `spring-boot-maven-plugin` to create a jar file that contains a both the app and its dependencies as embedded jars (jar jar).  

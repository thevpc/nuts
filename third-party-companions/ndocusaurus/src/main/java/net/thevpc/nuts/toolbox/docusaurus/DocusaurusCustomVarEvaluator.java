package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NObjectElement;

import java.util.Arrays;
import java.util.function.Function;

class DocusaurusCustomVarEvaluator implements Function<String, Object> {
    NElement config;
    String projectName;
    String projectTitle;
    DocusaurusProject project;

    public DocusaurusCustomVarEvaluator(DocusaurusProject project) {
        this.project = project;
        config = project.getConfig();
        projectName = project.getProjectName();
        projectTitle = project.getTitle();
    }

    @Override
    public Object apply(String varName) {
        switch (varName) {
            case "projectName": {
                return projectName;
            }
            case "projectTitle": {
                return projectTitle;
            }
            default: {
                String[] a = Arrays.stream(varName.split("[./]")).map(String::trim).filter(x -> !x.isEmpty())
                        .toArray(String[]::new);
                NElement config = this.config;
                if (config != null) {
                    for (String s : a) {
                        config = config.asObject().orElse(NObjectElement.ofEmpty()).get(s).orNull();
                        if (config == null) {
                            return null;
                        }
                    }
                }
                if (config == null) {
                    return null;
                }
                if (config.isNull()) {
                    return null;
                }
                if (config.isString()) {
                    return config.asString().get();
                }
                if (config.isArray()) {
                    return config.asArray().get().stream().map(Object::toString).toArray(String[]::new);
                }
                return config.asString().get();
            }
        }
    }
}

package net.thevpc.nuts.toolbox.nutsserver.http.commands;

import net.thevpc.nuts.toolbox.nutsserver.util.NutsServerUtils;
import net.thevpc.nuts.toolbox.nutsserver.util.XmlHelper;
import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommandContext;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class GetMavenFacadeCommand extends AbstractFacadeCommand {
    public GetMavenFacadeCommand() {
        super("get-mvn");
    }

    public static boolean acceptUri(String uri) {
        return uri.endsWith(".pom") || uri.endsWith(".jar") || uri.endsWith("/maven-metadata.xml");
    }

    @Override
    public void executeImpl(FacadeCommandContext context) throws IOException {
        URI uri = context.getRequestURI();
//        System.out.println("get-mvn " + uri.toString());
        List<String> split = NutsServerUtils.split(uri.toString(), "/");
        String n = split.get(split.size() - 1);
        if (n.endsWith(".pom")) {
            if (split.size() >= 4) {
                NutsId id = context.getWorkspace().id().builder().setArtifactId(split.get(split.size() - 3))
                        .setGroupId(String.join(".", split.subList(0, split.size() - 3)))
                        .setVersion(split.get(split.size() - 2)).build();
                NutsDefinition fetch = context.getWorkspace().fetch().setId(id).setSession(context.getSession())
                        .getResultDefinition();
                NutsDescriptor d = fetch.getDescriptor();
                if(context.isHeadMethod()){
                    context.sendResponseHeaders(200,-1);
                    return;
                }
                try {
                    XmlHelper xml = new XmlHelper();
                    xml.push("project")
                            .append("modelVersion", "4.0.0")
                            .append("groupId", d.getId().getGroupId())
                            .append("artifactId", d.getId().getArtifactId())
                            .append("version", d.getId().getVersion().toString())
                            .append("name", d.getName())
                            .append("description", d.getDescription())
                            .append("packaging", d.getPackaging())
                    ;
                    if (d.getParents().length > 0) {
                        xml.push("parent")
                                .append("groupId", d.getParents()[0].getGroupId())
                                .append("artifactId", d.getParents()[0].getArtifactId())
                                .append("version", d.getParents()[0].getVersion().toString())
                                .pop();
                    }

                    xml.push("properties");
                    for (NutsDescriptorProperty e : d.getProperties()) {
                        xml.append(e.getName(), e.getValue());
                    }
                    xml.pop();

                    xml.push("dependencies");
                    for (NutsDependency dependency : d.getDependencies()) {
                        xml.push("dependency")
                                .append("groupId", dependency.getGroupId())
                                .append("artifactId", dependency.getArtifactId())
                                .append("version", dependency.getVersion().toString())
                                .append("scope", NutsServerUtils.toMvnScope(dependency.getScope()))
                                .pop();

                    }
                    xml.pop();
                    if (d.getParents().length > 0) {
                        xml.push("parent")
                                .append("groupId", d.getParents()[0].getGroupId())
                                .append("artifactId", d.getParents()[0].getArtifactId())
                                .append("version", d.getParents()[0].getVersion().toString())
                                .pop();
                    }
                    if (d.getStandardDependencies().length > 0) {
                        //dependencyManagement
                        xml.push("dependencyManagement");
                        xml.push("dependencies");
                        for (NutsDependency dependency : d.getStandardDependencies()) {
                            xml.push("dependency")
                                    .append("groupId", dependency.getGroupId())
                                    .append("artifactId", dependency.getArtifactId())
                                    .append("version", dependency.getVersion().toString())
                                    .append("scope", NutsServerUtils.toMvnScope(dependency.getScope()))
                                    .pop();

                        }
                        xml.pop();
                        xml.pop();
                    }
                    context.sendResponseBytes(200, xml.toXmlBytes());
                } catch (Exception ex) {
                    context.sendError(500, ex.toString());
                }
            } else {
                context.sendError(404, "File Note Found");
            }
        } else if (n.endsWith(".jar")) {
            if (split.size() >= 4) {
                NutsId id = context.getWorkspace().id().builder().setArtifactId(split.get(split.size() - 3))
                        .setGroupId(String.join(".", split.subList(0, split.size() - 3)))
                        .setVersion(split.get(split.size() - 2)).build();
                NutsDefinition fetch = context.getWorkspace().fetch().setId(id).setSession(context.getSession())
                        .getResultDefinition();
                if(context.isHeadMethod()){
                    context.sendResponseHeaders(200,-1);
                    return;
                }
                context.sendResponseFile(200, fetch.getPath());
            } else {
                context.sendError(404, "File Note Found");
            }
        } else if (n.equals("maven-metadata.xml")) {
            if (split.size() >= 3) {
                NutsId id = context.getWorkspace().id().builder().setArtifactId(split.get(split.size() - 2))
                        .setGroupId(String.join(".", split.subList(0, split.size() - 2))).build();
                NutsStream<NutsId> resultIds = context.getWorkspace().search().addId(id).setDistinct(true).setSorted(true).getResultIds();
                if(context.isHeadMethod()){
                    context.sendResponseHeaders(200,-1);
                    return;
                }
                try {
                    XmlHelper xml = new XmlHelper();
                    xml.push("metadata")
                            .append("groupId", id.getGroupId())
                            .append("artifactId", id.getArtifactId())
                            .push("versioning")
                    ;
                    List<NutsId> versions = resultIds.toList();
                    if (versions.size() > 0) {
                        xml.append("release", versions.get(0).getVersion().toString());
                        xml.push("versions");
                        for (NutsId resultId : versions) {
                            xml.append("version", resultId.getVersion().toString());
                        }
                        xml.pop();
                    }
                    xml.pop();
                    context.sendResponseBytes(200, xml.toXmlBytes());
                } catch (Exception ex) {
                    context.sendError(500, ex.toString());
                }
            } else {
                context.sendError(404, "File Note Found");
            }
        } else {
            context.sendError(404, "File Note Found");
        }

    }
}

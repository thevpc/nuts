package net.vpc.app.nuts.core.util.bundledlibs.mvn;

import java.util.Objects;

/**
 *  <repository>
 *             <url>http://repository.primefaces.org/</url>
 *             <id>PrimeFaces-maven-lib</id>
 *             <layout>default</layout>
 *             <name>Repository for library PrimeFaces-maven-lib</name>
 *         </repository>
 *         <repository>
 *             <id>vpc-public-maven</id>
 *             <url>https://raw.github.com/thevpc/vpc-public-maven/master</url>
 *             <snapshots>
 *                 <enabled>true</enabled>
 *                 <updatePolicy>always</updatePolicy>
 *             </snapshots>
 *         </repository>
 */
public class PomRepository {
    private String id;
    private String layout;
    private String url;
    private String name;
    private PomRepositoryPolicy releases;
    private PomRepositoryPolicy snapshots;

    public PomRepository() {
    }

    public PomRepository(String id, String layout, String url, String name, PomRepositoryPolicy releases, PomRepositoryPolicy snapshots) {
        this.id = id;
        this.layout = layout;
        this.url = url;
        this.name = name;
        this.releases = releases;
        this.snapshots = snapshots;
    }

    public String getId() {
        return id;
    }

    public PomRepository setId(String id) {
        this.id = id;
        return this;
    }

    public String getLayout() {
        return layout;
    }

    public PomRepository setLayout(String layout) {
        this.layout = layout;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public PomRepository setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getName() {
        return name;
    }

    public PomRepository setName(String name) {
        this.name = name;
        return this;
    }

    public PomRepositoryPolicy getReleases() {
        return releases;
    }

    public PomRepository setReleases(PomRepositoryPolicy releases) {
        this.releases = releases;
        return this;
    }

    public PomRepositoryPolicy getSnapshots() {
        return snapshots;
    }

    public PomRepository setSnapshots(PomRepositoryPolicy snapshots) {
        this.snapshots = snapshots;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PomRepository that = (PomRepository) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(layout, that.layout) &&
                Objects.equals(url, that.url) &&
                Objects.equals(name, that.name) &&
                Objects.equals(releases, that.releases) &&
                Objects.equals(snapshots, that.snapshots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, layout, url, name, releases, snapshots);
    }
}

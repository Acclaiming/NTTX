package io.kurumi.ntt.maven;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class MvnArtifact {

    public String repository;

    public String groupId;

    public String artifactId;

    public String version;

    public String packaging;

    public List<MvnArtifact> dependencies;

    public HashSet<MvnArtifact> marge() {

        HashSet<MvnArtifact> all = new HashSet<>();

        all.add(this);

        for (MvnArtifact art : dependencies) {

            all.addAll(art.marge());

        }

        return all;

    }

    public String name() {

        return groupId + ":" + artifactId + ":" + version;

    }

    public String path() {

        return repository + groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + "." + packaging;

    }

    public String fileName() {

        return artifactId + "-" + version + "." + packaging;

    }

    public String fileNameZip() {

        return artifactId + "-" + version + ".zip";

    }

    @Override
    public int hashCode() {

        return name().hashCode();

    }

    @Override
    public boolean equals(Object obj) {

        if (super.equals(obj)) return true;

        if (obj instanceof MvnArtifact) {

            MvnArtifact art = (MvnArtifact) obj;

            return art.groupId.equals(groupId) && art.artifactId.equals(artifactId) && art.version.equals(version);

        }

        return false;

    }

}


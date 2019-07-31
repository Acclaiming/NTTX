package io.kurumi.maven;

import java.util.List;

public class MvnArtifact {

	public String repository;

	public String groupId;

	public String artifactId;

	public String version;

	public String packaging;

	public List<MvnArtifact> dependencies;

}


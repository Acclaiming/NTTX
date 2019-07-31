package io.kurumi.maven;

import java.util.List;
import javax.naming.spi.ResolveResult;
import java.util.LinkedList;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.HttpException;
import org.jdom2.input.SAXBuilder;
import java.io.IOException;
import org.jdom2.JDOMException;
import org.jdom2.Document;

public class MvnDownload {

	public static String central = "https://repo1.maven.org/maven2/";
	public static String jCenter = "https://jcenter.bintray.com/";
	public static String sonatype = "https://oss.sonatype.org/content/repositories/releases/";
	public static String springPlugins = "https://repo.spring.io/plugins-release/";
	public static String springLibM = "https://repo.spring.io/libs-milestone/";
	public static String hortonworks = "https://repo.hortonworks.com/content/repositories/releases/";
	public static String atlassian = "https://maven.atlassian.com/content/repositories/atlassian-public/";
	public static String jBossReleases = "https://repository.jboss.org/nexus/content/repositories/releases/";
	public static String jBossEA = "https://repository.jboss.org/nexus/content/repositories/ea/";
	public static String springLibRelease = "https://repo.spring.io/libs-release/";
	public static String iBiblio = "https://maven.ibiblio.org/maven2/";
	public static String xWikiReleases = "https://maven.xwiki.org/releases/";
	public static String wSO2Releases = "https://maven.wso2.org/nexus/content/repositories/releases/";
	public static String nuxeo = "https://maven-eu.nuxeo.org/nexus/content/repositories/public-releases/";
	public static String wSO2Public = "https://maven.wso2.org/nexus/content/repositories/public/";
	public static String clojars = "https://clojars.org/repo/";
	public static String geomajas = "http://maven.geomajas.org/";
	public static String apacheReleases = "https://repository.apache.org/content/repositories/releases/";
	public static String beDataDriven = "https://nexus.bedatadriven.com/content/repositories/public/";
	public static String atlassianPkgs = "https://packages.atlassian.com/maven-public/";

	public static class Artifact {

		public String repository;

		public String groupId;

		public String artifactId;

		public String version;

		public String packaging;

		public List<Artifact> dependencies;

	}

	private List<String> repositories = new LinkedList<>();

	public MvnDownload() {

		repositories.add(central);
		repositories.add(jCenter);
		repositories.add(sonatype);
		repositories.add(springPlugins);
		repositories.add(springLibM);
		repositories.add(hortonworks);
		repositories.add(atlassian);
		repositories.add(jBossReleases);
		repositories.add(jBossEA);
		repositories.add(springLibRelease);
		repositories.add(iBiblio);
		repositories.add(xWikiReleases);
		repositories.add(wSO2Releases);
		repositories.add(nuxeo);
		repositories.add(wSO2Public);
		repositories.add(clojars);
		repositories.add(geomajas);
		repositories.add(apacheReleases);
		repositories.add(beDataDriven);
		repositories.add(atlassianPkgs);

	}

	public MvnDownload(String... repositories) {

		this();

		for (String repository : repositories) addRepository(repository);

	}

	public MvnDownload addRepository(String repositoryUrl) {

		if (!repositoryUrl.startsWith("http")) {

			repositoryUrl = "http://" + repositoryUrl;

		}

		if (!repositoryUrl.endsWith("/")) {

			repositoryUrl = repositoryUrl + "/";

		}

		this.repositories.add(repositoryUrl);

		return this;

	}

	public Artifact resolve(String groupId,String artifactId,String version,String targetRepository) {

		if (version == null) {

			String mavenMeta = null;

			for (String repository : repositories) {

				try {

					mavenMeta = HttpUtil.get(repository + groupId.replace(".","/") + "/" + artifactId + "/maven-metadata.xml");

					targetRepository = repository;
					
					break;

				} catch (HttpException ignored) {}

			}
			
			if (mavenMeta == null) return null;

			Document document;
			
			try {
				
				document = new SAXBuilder().build(mavenMeta);
				
			} catch (Exception e) {
				
				return null;
				
			}
			
			version = document.getRootElement().getChild("versioning").getChild("latest").getValue();

		}
		
		String pomXml = null;
		
		if (targetRepository == null) {
			
			for (String repository : repositories) {

				try {

					pomXml = HttpUtil.get(repository + groupId.replace(".","/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom");

					targetRepository = repository;

					break;

				} catch (HttpException ignored) {}

			}
		
		} else {
			
			try {

				pomXml = HttpUtil.get(targetRepository + groupId.replace(".","/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom");

			} catch (HttpException ignored) {}
			
		}
		
		if (pomXml == null){} return null;

	}

}

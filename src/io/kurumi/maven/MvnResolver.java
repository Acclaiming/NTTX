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
import org.jdom2.Element;
import java.util.LinkedHashMap;
import java.util.Map;

public class MvnResolver {

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

	private List<String> repositories = new LinkedList<>();

	public MvnResolver() {

		repositories.add(central);
		repositories.add(jCenter);
		repositories.add(sonatype);
		
		/*
		
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
		
		*/

	}

	public MvnResolver(String... repositories) {

		this();

		for (String repository : repositories) addRepository(repository);

	}

	public MvnResolver addRepository(String repositoryUrl) {

		if (!repositoryUrl.startsWith("http")) {

			repositoryUrl = "http://" + repositoryUrl;

		}

		if (!repositoryUrl.endsWith("/")) {

			repositoryUrl = repositoryUrl + "/";

		}

		this.repositories.add(repositoryUrl);

		return this;

	}

	public MvnArtifact resolve(String groupId,String artifactId,String version,String defaultRepository) throws MvnException {

		MvnArtifact art = new MvnArtifact();

		art.groupId = groupId;
		art.artifactId = artifactId;

		if (version.matches("(\\+|latest)")) version = null;
		
		String targetRepository = null;

		if (version == null) {

			String mavenMeta = null;

			if (defaultRepository != null) {

				try {

					mavenMeta = HttpUtil.get(defaultRepository + groupId.replace(".","/") + "/" + artifactId + "/maven-metadata.xml");

					targetRepository = defaultRepository;

				} catch (HttpException ignored) {}

			}

			if (mavenMeta == null) {

				for (String repository : repositories) {

					try {

						mavenMeta = HttpUtil.get(repository + groupId.replace(".","/") + "/" + artifactId + "/maven-metadata.xml");

						targetRepository = repository;

						break;

					} catch (HttpException ignored) {}

				}

			}

			if (mavenMeta == null) throw new MvnException("找不到这个库");

			Document document;

			try {

				document = new SAXBuilder().build(mavenMeta);

			} catch (Exception e) {

				throw new MvnException("metadata 解析失败",e);

			}

			version = document.getRootElement().getChild("versioning").getChild("latest").getValue();

		}

		art.version = version;

		String pomXml = null;

		if (targetRepository != null) {

			try {

				pomXml = HttpUtil.get(targetRepository + groupId.replace(".","/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom");

				targetRepository = defaultRepository;

			} catch (HttpException ignored) {}

		} else if (defaultRepository != null) {

			try {

				pomXml = HttpUtil.get(defaultRepository + groupId.replace(".","/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom");

			} catch (HttpException ignored) {}

		}

		if (pomXml == null) {

			for (String repository : repositories) {

				try {

					pomXml = HttpUtil.get(repository + groupId.replace(".","/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom");

					targetRepository = repository;

					break;

				} catch (HttpException ignored) {}

			}

		}

		if (pomXml == null) throw new MvnException("找不到 pom.xml");

		Document document;

		try {

			document = new SAXBuilder().build(pomXml);

		} catch (Exception e) {

			throw new MvnException("pom.xml 解析失败");

		}

		Element packaging = document.getRootElement().getChild("packaging");

		if (packaging == null) {

			art.packaging = "jar";

		} else {

			art.packaging = packaging.getValue();

		}
		
		LinkedHashMap<String,String> props = new LinkedHashMap<>();
		
		Element properties = document.getRootElement().getChild("properties");

		if (properties != null) {
			
			for (Element prop : properties.getChildren()) {
				
				props.put(prop.getName(),prop.getValue());
				
			}
			
		}
		
		Element parent = document.getRootElement().getChild("parent");
		
		if (parent != null) {
			
			Element parentVersion = parent.getChild("version");
			
			if (parentVersion != null) {
				
				props.put("project.parent.version",parentVersion.getValue());
				
			}
			
		}
		
		Element dependencies = document.getRootElement().getChild("dependencies");

		art.dependencies = new LinkedList<>();
		
		if (dependencies == null) return art;
		
		for (Element dependency : dependencies.getChildren()) {

			Element optional = dependency.getChild("optional");

			if (optional != null && "true".equals(optional.getValue())) continue;
			
			Element scope = dependency.getChild("scope");

			if (scope != null && !"compile".equals(scope.getValue())) continue;
			
			String group = dependency.getChild("groupId").getValue();
			String artifact = dependency.getChild("artifactId").getValue();

			String depVer = null;
			
			Element versionObj = dependency.getChild("version");

			if (versionObj != null) {
				
				depVer = versionObj.getValue();
				
				for (Map.Entry<String,String> prop : props.entrySet()) {
					
					depVer.replace("${" + prop.getKey() + "}",prop.getValue());
					
				}
				
			}
			
			MvnArtifact dep = resolve(group,artifact,depVer,targetRepository);

			if (dep != null) art.dependencies.add(dep);
			
		}
		
		return art;

	}

}

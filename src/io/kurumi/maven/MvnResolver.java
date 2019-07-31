package io.kurumi.maven;

import cn.hutool.http.HttpException;
import cn.hutool.http.HttpUtil;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

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

	public MvnArtifact resolve(String groupId,String artifactId,String version,String defaultRepository,StringBuilder log) throws MvnException {

		MvnArtifact art = new MvnArtifact();

		art.groupId = groupId;
		art.artifactId = artifactId;

		String targetRepository = null;

		log.append("\n\n正在解析 : ").append(groupId).append(":").append(artifactId).append(":").append(version);
		
		if (version.matches("(\\+|latest)")) {
			
			version = null;
			
		}
		
		if (version == null) {

			log.append("\n没有指定版本，正在获取");
			
			String mavenMeta = null;

			if (defaultRepository != null) {

				try {

					mavenMeta = HttpUtil.get(defaultRepository + groupId.replace(".","/") + "/" + artifactId + "/maven-metadata.xml");

					targetRepository = defaultRepository;

					log.append("\n从上级获取最新版本 成功");
					
				} catch (HttpException ignored) {
					
					log.append("\n从上级获取最新版本 失败");
					
					
				}

			}

			if (mavenMeta == null) {

				for (String repository : repositories) {

					try {

						mavenMeta = HttpUtil.get(repository + groupId.replace(".","/") + "/" + artifactId + "/maven-metadata.xml");

						targetRepository = repository;

						log.append("\n从 "+ repository + " 获取最新版本 成功");
						
						break;

					} catch (HttpException ignored) {
						
						log.append("\n从 "+ repository + " 获取最新版本 失败");
						
						
					}

				}

			}

			if (mavenMeta == null) throw new MvnException(log.toString());

			Document document;

			try {

				document = new SAXBuilder().build(new StringReader(mavenMeta));

			} catch (Exception e) {

				log.append("\n解析元数据失败 : " + e.toString());
				
				throw new MvnException(log.toString());

			}

			version = document.getRootElement().getChild("versioning").getChild("latest").getValue();

		}

		art.version = version;

		String pomXml = null;

		if (targetRepository != null && !targetRepository.equals(defaultRepository)) {

			try {

				pomXml = HttpUtil.get(targetRepository + groupId.replace(".","/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom");

				log.append("\n从 " + targetRepository + " 获取 Pom 成功");
				
			} catch (HttpException ignored) {
				
				log.append("\n从 " + targetRepository + " 获取 Pom 失败");
				
			}

		} else if (defaultRepository != null) {

			try {

				pomXml = HttpUtil.get(defaultRepository + groupId.replace(".","/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom");

				log.append("\n从上级源获取 Pom 成功");
				
			} catch (HttpException ignored) {
				
				log.append("\n从上级源获取 Pom 失败");
				
			}

		}

		if (pomXml == null) {

			for (String repository : repositories) {

				try {

					pomXml = HttpUtil.get(repository + groupId.replace(".","/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom");

					targetRepository = repository;

					log.append("\n从 " + repository + " 获取 Pom 成功");
					
					break;

				} catch (HttpException ignored) {
					
					log.append("\n从 " + repository + " 获取 Pom 失败");
					
				}

			}

		}

		if (pomXml == null) {
			
			log.append("\n获取 Pom 失败");
			
			throw new MvnException(log.toString());

		}
			
		Document document;

		try {

			document = new SAXBuilder().build(new StringReader(pomXml));

		} catch (Exception e) {

			log.append("\n解析 Pom 失败 : " + e.toString());
			
			throw new MvnException(log.toString());

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
		
		if (dependencies == null) {
			
			log.append("\n没有依赖项");
			
			return art;
			
		}
		
		for (Element dependency : dependencies.getChildren()) {
					
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
			
			log.append("\n发现依赖 : " + group + ":" + artifact + ":" + depVer);
			
			Element optional = dependency.getChild("optional");

			if (optional != null && "true".equals(optional.getValue())) {
				
				log.append("\n 是可选依赖 跳过");
				
				continue;
				
			}

			Element scope = dependency.getChild("scope");

			if (scope != null && !"compile".equals(scope.getValue())) {
				
				log.append("\n 是可选依赖 跳过");
				
				continue;
				
			}
			
			
			MvnArtifact dep = resolve(group,artifact,depVer,targetRepository,log);

			if (dep != null) art.dependencies.add(dep);
			
		}
		
		return art;

	}

}

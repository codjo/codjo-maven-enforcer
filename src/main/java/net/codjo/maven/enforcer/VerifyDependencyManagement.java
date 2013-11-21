package net.codjo.maven.enforcer;
import net.codjo.maven.enforcer.StringProcessor.Finder;
import net.codjo.util.file.FileUtil;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

public class VerifyDependencyManagement implements EnforcerRule {

    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
        try {
            File projectFile = (File)helper.evaluate("${project.file}");

            MavenEmbedder mavenEmbedder = new MavenEmbedder();
            mavenEmbedder.setClassLoader(getClass().getClassLoader());
            mavenEmbedder.start();
            MavenProject project = mavenEmbedder.readProject(projectFile);

            Map fileCache = new HashMap();
            for (Iterator it = project.getDependencies().iterator(); it.hasNext();) {
                Dependency dependency = (Dependency)it.next();
                if (dependency.getGroupId().equals(project.getGroupId())
                    && hasNoVersion(dependency, getPomContent(project, fileCache))
                    && !isFoundInDependencyManagement(project, dependency, fileCache)) {
                    throw new EnforcerRuleException(buildErrorMessage(dependency));
                }
            }
        }
        catch (EnforcerRuleException e) {
            throw e;
        }
        catch (ExpressionEvaluationException e) {
            throw new EnforcerRuleException("Unable to lookup an expression " + e.getLocalizedMessage(), e);
        }
        catch (Exception e) {
            throw new EnforcerRuleException("Unable to execute rule " + e.getLocalizedMessage(), e);
        }
    }


    public String getCacheId() {
        return "";
    }


    public boolean isCacheable() {
        return false;
    }


    public boolean isResultValid(EnforcerRule enforcerRule) {
        return false;
    }


    private String buildErrorMessage(Dependency dependency) {
        String groupId = dependency.getGroupId();
        String artifactId = dependency.getArtifactId();
        StringBuilder errorMessage = new StringBuilder()
              .append("'dependencyManagement.dependencies.dependency' is missing for ")
              .append(dependency.getGroupId()).append(":").append(dependency.getArtifactId()).append("\n")
              .append("\n")
              .append("<dependency>\n")
              .append("   <groupId>").append(groupId).append("</groupId>\n")
              .append("   <artifactId>").append(artifactId).append("</artifactId>\n")
              .append("   <version>${project.version}</version>\n");
        if (hasClassifier(dependency)) {
            String classifier = dependency.getClassifier();
            errorMessage.append("   <classifier>").append(classifier).append("</classifier>\n");
        }
        if (hasType(dependency)) {
            String type = dependency.getType();
            errorMessage.append("   <type>").append(type).append("</type>\n");
        }
        errorMessage
              .append("</dependency>\n");
        return errorMessage.toString();
    }


    private boolean hasNoVersion(Dependency dependency, String pomContent) throws IOException {
        return findDependency("dependencies", dependency, pomContent)
              .notContaining(nodeWithContent("version", ".*?")).hasFound();
    }


    /**
     * @noinspection SimplifiableIfStatement
     */
    private boolean isFoundInDependencyManagement(MavenProject project,
                                                  Dependency toFind,
                                                  Map fileContentCache) throws IOException {
        if (!project.getGroupId().equals(toFind.getGroupId())) {
            return false;
        }

        if (project.getFile() == null) {
            return false;
        }

        String pomContent = getPomContent(project, fileContentCache);
        if (findDependency("dependencyManagement", toFind, pomContent).hasFound()) {
            return true;
        }

        if (!project.hasParent()) {
            return false;
        }

        return isFoundInDependencyManagement(project.getParent(), toFind, fileContentCache);
    }


    private String getPomContent(MavenProject project, Map fileContentCache) throws IOException {
        String pomContent = (String)fileContentCache.get(project);
        if (pomContent == null) {
            pomContent = FileUtil.loadContent(project.getFile());
            Pattern pattern = Pattern.compile("<!--.*?-->");
            Matcher matcher = pattern.matcher(pomContent);
            pomContent = matcher.replaceAll("");
            fileContentCache.put(project, pomContent);
        }
        return pomContent;
    }


    private Finder findDependency(String rootNode, Dependency dependency, String pomContent) {
        Finder finder = new StringProcessor(pomContent)
              .find(nodeWithContent(rootNode, ".*?"))
              .find(nodeWithContent("dependency", ".*?"))
              .containing(nodeWithContent("groupId", dependency.getGroupId()))
              .containing(nodeWithContent("artifactId", dependency.getArtifactId()));
        if (hasClassifier(dependency)) {
            finder = finder.containing(nodeWithContent("classifier", dependency.getClassifier()));
        }
        else {
            finder = finder.notContaining(nodeWithContent("classifier", ".*?"));
        }
        if (hasType(dependency)) {
            finder = finder.containing(nodeWithContent("type", dependency.getType()));
        }
        else {
            finder = finder.notContaining(nodeWithContent("type", ".*?"));
        }
        return finder;
    }


    private String nodeWithContent(String nodeName, String content) {
        return "<" + nodeName + ">\\s*" + content + "\\s*</" + nodeName + ">";
    }


    private boolean hasClassifier(Dependency dependency) {
        String classifier = dependency.getClassifier();
        return classifier != null && !"compile".equals(classifier);
    }


    private boolean hasType(Dependency dependency) {
        String type = dependency.getType();
        return type != null && !"jar".equals(type);
    }
}

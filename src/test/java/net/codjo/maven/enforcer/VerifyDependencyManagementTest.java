package net.codjo.maven.enforcer;
import net.codjo.maven.common.test.PathUtil;
import junit.framework.TestCase;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.plugins.enforcer.EnforcerTestUtils;
import org.apache.maven.project.MavenProject;

public class VerifyDependencyManagementTest extends TestCase {
    private MavenProject module;


    public void test_inDependencyManagement() throws Exception {
        module = createModule("/super-pom/codjo-library/codjo-library-inDependencyManagement/pom.xml");

        new VerifyDependencyManagement().execute(EnforcerTestUtils.getHelper(module));
    }


    public void test_notInDependencyManagement_mustFail() throws Exception {
        module = createModule("/super-pom/codjo-library/codjo-library-notInDependencyManagement/pom.xml");

        try {
            new VerifyDependencyManagement().execute(EnforcerTestUtils.getHelper(module));
            fail();
        }
        catch (EnforcerRuleException e) {
            assertEquals(
                  "'dependencyManagement.dependencies.dependency' is missing for net.codjo.library:codjo-library-notInDependencyManagement\n"
                  + "\n"
                  + "<dependency>\n"
                  + "   <groupId>net.codjo.library</groupId>\n"
                  + "   <artifactId>codjo-library-notInDependencyManagement</artifactId>\n"
                  + "   <version>${project.version}</version>\n"
                  + "</dependency>\n",
                  e.getLocalizedMessage());
        }
    }


    public void test_withClassifier_mustFail() throws Exception {
        module = createModule("/super-pom/codjo-library/codjo-library-withClassifier/pom.xml");

        try {
            new VerifyDependencyManagement().execute(EnforcerTestUtils.getHelper(module));
            fail();
        }
        catch (EnforcerRuleException e) {
            assertEquals(
                  "'dependencyManagement.dependencies.dependency' is missing for net.codjo.library:codjo-library-notInDependencyManagement\n"
                  + "\n"
                  + "<dependency>\n"
                  + "   <groupId>net.codjo.library</groupId>\n"
                  + "   <artifactId>codjo-library-notInDependencyManagement</artifactId>\n"
                  + "   <version>${project.version}</version>\n"
                  + "   <classifier>server</classifier>\n"
                  + "</dependency>\n",
                  e.getLocalizedMessage());
        }
    }


    public void test_withComments_mustFail() throws Exception {
        module = createModule("/super-pom/codjo-library/codjo-library-withComments/pom.xml");

        try {
            new VerifyDependencyManagement().execute(EnforcerTestUtils.getHelper(module));
            fail();
        }
        catch (EnforcerRuleException e) {
            assertEquals(
                  "'dependencyManagement.dependencies.dependency' is missing for net.codjo.library:codjo-library-withComments\n"
                  + "\n"
                  + "<dependency>\n"
                  + "   <groupId>net.codjo.library</groupId>\n"
                  + "   <artifactId>codjo-library-withComments</artifactId>\n"
                  + "   <version>${project.version}</version>\n"
                  + "</dependency>\n",
                  e.getLocalizedMessage());
        }
    }


    public void test_withVersion() throws Exception {
        module = createModule("/super-pom/codjo-library/codjo-library-withVersion/pom.xml");

        new VerifyDependencyManagement().execute(EnforcerTestUtils.getHelper(module));
    }


    private MavenProject createModule(String pomfile) {
        MavenProject project = new MavenProject();
        project.setFile(PathUtil.find(getClass(), pomfile));
        project.setGroupId("net.codjo.library");
        return project;
    }
}

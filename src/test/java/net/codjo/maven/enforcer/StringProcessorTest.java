package net.codjo.maven.enforcer;
import net.codjo.maven.common.test.PathUtil;
import net.codjo.util.file.FileUtil;
import junit.framework.TestCase;
/**
 *
 */
public class StringProcessorTest extends TestCase {

    public void test_containing() throws Exception {
        String text = FileUtil.loadContent(PathUtil.find(getClass(), "/super-pom/pom.xml"));

        assertTrue(new StringProcessor(text)
              .find("<dependencyManagement>.*?</dependencyManagement>")
              .find("<dependency>.*?</dependency>")
              .containing("<groupId>net.codjo.library</groupId>")
              .containing("<artifactId>codjo-library-notInDependencyManagement</artifactId>")
              .containing("<version>1.0</version>").hasFound());
    }


    public void test_containing_mustFail() throws Exception {
        String text = FileUtil.loadContent(PathUtil.find(getClass(), "/super-pom/pom.xml"));

        assertFalse(new StringProcessor(text)
              .find("<dependencyManagement>.*?</dependencyManagement>")
              .find("<dependency>.*?</dependency>")
              .containing("<groupId>unknown</groupId>")
              .containing("<artifactId>codjo-library-inDependencyManagement</artifactId>")
              .containing("<version>.*?project.version.*?</version>").hasFound());
    }


    public void test_notContaining() throws Exception {
        String text = FileUtil.loadContent(PathUtil.find(getClass(), "/super-pom/pom.xml"));

        assertFalse(new StringProcessor(text)
              .find("<dependencyManagement>.*?</dependencyManagement>")
              .find("<dependency>.*?</dependency>")
              .containing("<groupId>unknown</groupId>")
              .containing("<artifactId>codjo-library-inDependencyManagement</artifactId>")
              .notContaining("<version>.*?project.version.*?</version>").hasFound());
    }
}

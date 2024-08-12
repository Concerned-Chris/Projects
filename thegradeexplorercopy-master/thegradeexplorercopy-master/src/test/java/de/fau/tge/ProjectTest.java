package de.fau.tge;

import junit.framework.TestCase;
import org.junit.Test;

import javax.xml.crypto.Data;

public class ProjectTest extends TestCase {

    @Test
    /**
     * Unit test to verify that the method createNewProject() works properly
     */
    public void testCreateNewProjectWithoutSpaceInName() {
        Project project = new Project("test", "junit", "");
        project.persist();

        DatasetClass dsc = new DatasetClass();
        assertEquals(dsc.getProjectInfo("test").getName(), project.getName());
    }

    @Test
    /**
     * Unit test to verify that the method createNewProject() works properly
     */
    public void testCreateNewProjectWithSpaceInName() {
        Project project = new Project("test project");

        //Project will not be created.
        assertEquals(project.persist(), false);
    }
}
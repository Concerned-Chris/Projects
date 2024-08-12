package de.fau.tge;

/**
 * Project
 * Author: Monique Mück (monique.mueck@fau.de)
 *
 * This class creates an Object that contains necessary data saved to the Database.
 */

public class Project implements Persistable {

    private String name;
    private String creator;
    private String creationDate;

    private boolean create = false;
    DatasetClass datasetClass = new DatasetClass();

    /**
     * Default-Constructor.
     */
    public Project() {
        // default constructor
    }
    /**
     * simplicstic Constructor.
     * @param name the Name of the Project.
     */
    public Project(String name) {
        this.name = name;
    }
    /**
     * Constructor that defines all properties.
     * @param name the Name of the Project.
     * @param creator the Name of the Project's creator.
     * @param creationDate the Date of Creation, set by system.
     */
    public Project(String name, String creator, String creationDate) {
        this.name = name;
        this.creator = creator;
        this.creationDate = creationDate;
    }

    @Override
    public boolean persist() {
        this.create = datasetClass.createNewProject(name, creator);
        if (!create) {
            // update
        } else {
            // create
            System.out.println("Create project now ;)");
            return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
}

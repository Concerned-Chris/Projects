package de.fau.tge;

public interface Persistable {
    /**
     * saves everything to the database -> creates or updates regarding if the creation throws an error.
     * @return boolean, if it was successfull.
     */
    boolean persist();
    // void delete();
}

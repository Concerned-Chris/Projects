package de.fau.qLStore.Line;

public enum LineFormats {

    SWDF("SWDF"),
    DBPEDIA("DBPedia"),
    LGD("LGD"),
    RKB("RKB"),
    WIKIDATA_200("Wikidata_200"),
    WIKIDATA_500("Wikidata_500"),
    MY_CSV("My_CSV"),
    MY_TSV("My_TSV");

    private final String name;

    LineFormats(String s) {
        this.name = s;
    }

    public String toString() {
        return this.name;
    }
}

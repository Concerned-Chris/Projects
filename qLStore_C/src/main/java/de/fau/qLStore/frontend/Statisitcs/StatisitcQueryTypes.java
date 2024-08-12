package de.fau.qLStore.frontend.Statisitcs;

public class StatisitcQueryTypes {

    //TODO add Construct, Describe and Update
    private int all;
    private int uniqueAll;
    private int select;
    private int uniqueSelect;
    private int ask;
    private int uniqueAsk;

    public StatisitcQueryTypes(){

    }

    public int getAll() {
        return all;
    }

    public void setAll(int all) {
        this.all = all;
    }

    public int getUniqueAll() {
        return uniqueAll;
    }

    public void setUniqueAll(int uniqueAll) {
        this.uniqueAll = uniqueAll;
    }

    public int getSelect() {
        return select;
    }

    public void setSelect(int select) {
        this.select = select;
    }

    public int getUniqueSelect() {
        return uniqueSelect;
    }

    public void setUniqueSelect(int uniqueSelect) {
        this.uniqueSelect = uniqueSelect;
    }

    public int getAsk() {
        return ask;
    }

    public void setAsk(int ask) {
        this.ask = ask;
    }

    public int getUniqueAsk() {
        return uniqueAsk;
    }

    public void setUniqueAsk(int uniqueAsk) {
        this.uniqueAsk = uniqueAsk;
    }
}

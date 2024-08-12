package de.fau.qLStore.frontend.Statisitcs;

public class StatisticHelper {

    public String a;
    public int b;
    public double c;
    public int d;
    public double e;

    StatisticHelper(String opSet, int absV, String relV, int absU, String relU){
        this.a = opSet;
        this.b = absV;
        this.c = Double.parseDouble(relV.replace(',','.'));
        this.d = absU;
        this.e = Double.parseDouble(relU.replace(',','.'));
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public double getC() {
        return c;
    }

    public void setC(double c) {
        this.c = c;
    }

    public int getD() {
        return d;
    }

    public void setD(int d) {
        this.d = d;
    }

    public double getE() {
        return e;
    }

    public void setE(double e) {
        this.e = e;
    }
}

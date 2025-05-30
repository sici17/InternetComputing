package it.unical.dimes.reti.usermade.Appello04Aprile2023;

import java.io.Serializable;

public class Offerta implements Serializable {

    private String CF;
    private int importo;
    private int ID;

    public String getCF() {
        return CF;
    }

    public void setCF(String CF) {
        this.CF = CF;
    }

    public int getImporto() {
        return importo;
    }

    public void setImporto(int importo) {
        this.importo = importo;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }


    public Offerta(String CF, int importo, int ID) {
        this.CF = CF;
        this.importo = importo;
        this.ID = ID;
    }
}


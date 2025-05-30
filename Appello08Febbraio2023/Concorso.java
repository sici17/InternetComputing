package it.unical.dimes.reti.usermade.Appello08Febbraio2023;

import java.io.Serializable;
import java.util.Date;

public class Concorso implements Serializable {
    private int ID;
    private int posti;
    private Date scadenza;

    public Concorso(int ID, int posti, Date scadenza) {
        this.ID = ID;
        this.posti = posti;
        this.scadenza = scadenza;
    }

    public int getID() {
        return ID;
    }

    public int getPosti() {
        return posti;
    }

    public Date getScadenza() {
        return scadenza;
    }


}

package it.unical.dimes.reti.usermade.Appello04Aprile2023;

import java.io.Serializable;

public class Asta implements Serializable {
    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    private int ID;
    private String nome;

    public Asta(int ID, String nome) {
        this.ID = ID;
        this.nome = nome;
    }
}

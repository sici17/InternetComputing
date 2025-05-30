package it.unical.dimes.reti.usermade.Appello08Febbraio2023;

import java.io.Serializable;

public class Partecipazione implements Serializable {
    private int IDconcorso;
    private String nome, cognome, CF, curriculum;

    public Partecipazione(int IDconcorso, String nome, String cognome, String CF, String curriculum) {
        this.IDconcorso = IDconcorso;
        this.nome = nome;
        this.cognome = cognome;
        this.CF=CF;
        this.curriculum=curriculum;
    }

    public int getIDconcorso() {
        return IDconcorso;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getCF() {
        return CF;
    }

    public String getCurriculum() {
        return curriculum;
    }

    public String toString(){
        return "ID concorso: "+IDconcorso+", nome: "+nome+", cognome: "+cognome+", CF: "+CF+", curriculum: "+curriculum;
    }



}

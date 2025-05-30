package it.unical.dimes.reti.usermade.Appello14Marzo2024;

import java.io.Serializable;
import java.util.Random;

public class StatoSensore implements Serializable {

    private int stateNumber;
    private int sensoreID;
    private double umidità, temperatura;

    public StatoSensore(int idS){
        this.sensoreID = idS;
        Random rand = new Random();
        this.umidità = rand.nextDouble();
        this.temperatura = rand.nextDouble();

    }

    public double getTemperatura() {
        return temperatura;
    }

    public double getUmidità(){
        return this.umidità;
    }

    public int getStateNumber() {
        return stateNumber;
    }

    public void setStateNumber(int stateNumber) {
        this.stateNumber = stateNumber;
    }

    public int getSensoreID() {
        return sensoreID;
    }

    public String toString(){
        return "Stato del sensore: "+sensoreID+", temperatura: "+temperatura+ " e umidità: "+umidità;
    }

}


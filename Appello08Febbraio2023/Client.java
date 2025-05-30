package it.unical.dimes.reti.usermade.Appello08Febbraio2023;

import java.io.*;
import java.net.*;

public class Client {
    private final static int portTCP=3000;
    private final static int portUDP=4000;
    private final static int portMulticast=5000;
    private final static String addressMulticast="230.0.0.1";
    private String nome, cognome, CF, curriculum;
    private boolean inviata=false;
    private int IDprot;
    private InetAddress serverAddress;

    private static MulticastSocket multicastSocket;
    private Socket client;
    private DatagramSocket ds;


    public Client(String nome, String cognome, String CF, String curriculum, InetAddress server){
        this.nome = nome;
        this.cognome = cognome;
        this.CF = CF;
        this.curriculum = curriculum;
        this.serverAddress = server;
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


    public void inviaRichiesta(Partecipazione p){

        try{
            client = new Socket(serverAddress, portTCP);
            System.out.println("Sto per inviare la partecipazione per il concorso con ID "+p.getIDconcorso());
            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
            out.writeObject(p);
            System.out.println("Ho inviato la partecipazione");

            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String res = in.readLine();
            System.out.println("Il server ha risposto con "+res);
            if(!res.equals("NOT_ACCEPTED")){
                String[] splitted = res.split("-");
                IDprot = Integer.parseInt(splitted[0]);
                System.out.println(IDprot);
                inviata = true;

            }else inviata = false;


        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void cancellaRichiesta(){
        try{
            if(!inviata) System.out.println("Impossibile cancellare la richiesta: inesistente");
            else{
                ds = new DatagramSocket();
                byte[] buf = new byte[256];
                StringBuilder sb = new StringBuilder();
                sb.append(IDprot);
                String msg = sb.toString();
                buf = msg.getBytes();

                DatagramPacket dp = new DatagramPacket(buf, buf.length, serverAddress, portUDP);
                ds.send(dp);
                System.out.println("Ho inviato la richiesta di cancellazione per la partecipazione "+IDprot);

                byte[] res = new byte[256];
                DatagramPacket result = new DatagramPacket(res, res.length);

                ds.receive(result);
                String ans = new String(result.getData());

                System.out.println("Il server ha risposto con "+ans);

                ds.close();

            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void attendiRisultati(){
        System.out.println("Sto attendendo i risultati...");
        try{
            InetAddress group = InetAddress.getByName(addressMulticast);
            multicastSocket = new MulticastSocket();
            multicastSocket.joinGroup(group);

            byte[] buf = new byte[256];
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            multicastSocket.receive(dp);

            String res = new String(dp.getData());
            System.out.println("I risultati sono: "+res);

            multicastSocket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws UnknownHostException{
        Client c1 = new Client("voglio", "morire", "percheHoSceltoQuestaFacoltà", "nonLoSo", InetAddress.getByName("127.0.4.1"));
        Partecipazione p1 = new Partecipazione(1, c1.getNome(), c1.getCognome(), c1.getCF(), c1.getCurriculum());
        Client c2 = new Client("voglio", "morire", "percheHoSceltoQuestaFacoltà", "nonLoSo", InetAddress.getByName("127.0.4.1"));
        Partecipazione p2 = new Partecipazione(2, c2.getNome(), c2.getCognome(), c2.getCF(), c2.getCurriculum());
        c1.inviaRichiesta(p1);
        c2.inviaRichiesta(p2);
        c2.cancellaRichiesta();
        c1.attendiRisultati();


    }


}

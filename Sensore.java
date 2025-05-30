package it.unical.dimes.reti.usermade.Appello14Marzo2024;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class Sensore {
    private final static int portTCP=3000, portUDP=4000, portSUB=4000;
    private static int ID;
    private static String hostname;
    private InetAddress serverAdd;
    private StatoSensore state;

    public Sensore(String hostname, int id){
        this.hostname = hostname;
        ID = id;
        try{
            serverAdd=InetAddress.getByName(hostname);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public int getID(){
        return ID;
    }

    public void sendState(){
        state = new StatoSensore(ID);
        System.out.println("Ho rilevato lo stato "+state.toString());
        try{
            Socket client = new Socket(serverAdd, portTCP);
            System.out.println("Mi sono connesso al server.");
            ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
            oos.writeObject(state);
            System.out.println("Ho inviato lo stato.");

            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String res = br.readLine();
            if(res.equals("403: BAD REQUEST")){
                System.out.println("Il server ha chiuso le richieste. res: "+res);
                client.close();
            }else{
                if(res.equals("invalid state.")){
                    System.out.println("Stato invalido.");
                    client.close();
                }else {
                    int stateID = Integer.parseInt(res);
                    state.setStateNumber(stateID);
                    System.out.println("La richiesta è andata a buon fine. Id assegnato allo stato: " + stateID);
                    client.close();
                }
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void submiss(){
        System.out.println("Voglio iscrivermi al servizio di notifica.");
        try{
            Socket client = new Socket(serverAdd, portSUB);
            PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
            //String msg = "ID_"+ID;
            pw.println(ID);
            System.out.println("Ho inviato al server la domanda di iscrizione.");
            client.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void waitNotification(){
        System.out.println("Resto in attesa di notifiche.");
        try{
            DatagramSocket ds = new DatagramSocket(portUDP);
            while(true) {
                byte[] buf = new byte[256];

                DatagramPacket dp = new DatagramPacket(buf, buf.length);
                ds.receive(dp);

                String res = new String(dp.getData());

                System.out.println("Notifica: " + res.toString());
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException{
        Sensore s1 = new Sensore("127.0.3.1", 1);
        s1.sendState();
        Thread.sleep(4000);
        s1.submiss();
        s1.waitNotification();
    }
}

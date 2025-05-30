package it.unical.dimes.reti.usermade.Appello04Aprile2023;

import java.io.*;
import java.net.*;

public class Client {
    protected final static int multicastPort = 5000;
    protected final static String multicastIP = "230.0.0.1";
    protected final static int UDPresults = 4000;
    private String CF;
    private int importo;

    public Client(String CF, int importo){
        this.CF = CF;
        this.importo = importo;
    }

    public String getCF(){
        return CF;
    }

    public void run(){
        String not = notificaAsta();
        String[] splitted = not.split("-");
        int IDasta = Integer.parseInt(splitted[0]);
        int port = Integer.parseInt(splitted[1]);
        String nomeAsta = splitted[2];

        Offerta off = new Offerta(CF, importo, IDasta);

        inviaOfferta(port, off);

        riceviRisultato();
    }

    private String notificaAsta(){
        InetAddress group;
        MulticastSocket multi;
        String msg = null;
        try{
            multi = new MulticastSocket(multicastPort);
            group = InetAddress.getByName(multicastIP);
            multi.joinGroup(group);

            byte[] buf = new byte[256];
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            multi.receive(dp);

            msg = new String(dp.getData());


        }catch(IOException e){
            e.printStackTrace();
        }finally {
            return msg;
        }
    }

    private void inviaOfferta(int port, Offerta off){
        Socket client;
        try{
            client = new Socket();
            ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
            oos.writeObject(off);

            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String st = br.readLine();
            while(st != null){
                st = br.readLine();
            }

            client.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void riceviRisultato(){
        DatagramSocket ds;
        try{
            ds = new DatagramSocket(UDPresults);
            byte[] buf = new byte[256];
            DatagramPacket dp = new DatagramPacket(buf, buf.length);

            ds.receive(dp);

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client("ABC", 200);
        Client client1 = new Client("ABCD", 201);
        Client client2 = new Client("ABCDE", 202);
        client.run();
        client1.run();
        client2.run();

    }
}

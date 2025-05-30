package it.unical.dimes.reti.usermade.Appello04Aprile2023;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class Server {
    protected static int P;
    protected static Random rand;
    protected final static int TIMEOUT = 60000;
    protected final static int multicastPort = 5000;
    protected final static String multicastIP = "230.0.0.1";
    protected final static int UDPfinal = 4000;
    protected static HashMap<String, Integer> clienti = new HashMap<>(); //<CF, importo>
    protected static HashMap<String, InetAddress> clientiAdd = new HashMap<>(); //<CF, address>

    private Asta asta;

    public Server(Asta asta) {
        this.asta = asta;
        this.rand = new Random();
        this.P = rand.nextInt((4000 - 3000) + 1) + 30000;
    }

    public int getP() {
        return P;
    }

    public void getRequests() {
        ServerSocket serverSocket = null;
        Socket socket = null;

            try {
                serverSocket = new ServerSocket(P);
                while (true) {
                    socket = serverSocket.accept();
                    serverSocket.setSoTimeout(TIMEOUT);
                    ClientHandler cl = new ClientHandler(asta, socket);
                    cl.start();
                }
            } catch (IOException e) {
                inviaRisultati();
            }
    }

    public void inviaRisultati(){
        int max=0;
        String id=null;
        for(String cl : clienti.keySet()){
            if(clienti.get(cl) > max){
                max = clienti.get(cl);
                id = cl;
            }
        }
        InetAddress address = clientiAdd.get(id);
        DatagramSocket ds;
        try{
            ds = new DatagramSocket(UDPfinal);
            byte[] buf = new byte[256];
            String msg = "Winner!";
            buf = msg.getBytes();

            DatagramPacket dp = new DatagramPacket(buf, buf.length, address, UDPfinal);
            ds.send(dp);

            ds.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    class ClientHandler extends Thread {

        private Asta asta;
        private Socket socket;

        public ClientHandler(Asta asta, Socket socket) {
            this.asta = asta;
            this.socket = socket;
        }

        public void run() {
            String msg = asta.getID() + "-" + socket.getLocalPort() + "-" + asta.getNome();
            InetAddress add;
            MulticastSocket multicastSocket = null;
            try {
                add = InetAddress.getByName(multicastIP);
                multicastSocket = new MulticastSocket(multicastPort);
                byte[] buf = new byte[256];
                buf = msg.getBytes();

                DatagramPacket dp = new DatagramPacket(buf, buf.length, add, multicastPort);
                multicastSocket.send(dp);

                riceviRisposta();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                multicastSocket.close();
            }
        }

        private static void riceviRisposta(){
            ServerSocket server;
            Socket s;
            try{
                server = new ServerSocket(P);
                while(true){
                    s = server.accept();
                    ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                    Offerta off = (Offerta) ois.readObject();
                    String ID = off.getCF();
                    int importo = off.getImporto();


                    boolean response = rand.nextBoolean();
                    String msg = null;
                    if(response){
                        msg="true";
                        clienti.put(ID, importo);
                        clientiAdd.put(ID,s.getLocalAddress());
                    }

                    else msg = "false";

                    PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
                    pw.write(msg);
                }

            }catch(IOException e){
                e.printStackTrace();
            }catch(ClassNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        LinkedList<Asta> aste = new LinkedList<>();
        Asta a1 = new Asta(1, "telefono");
        Asta a2 = new Asta(2, "cello");
        aste.add(a1);
        aste.add(a2);
        for(Asta a : aste){
            Server server = new Server(a);
            server.getRequests();
        }
    }

}
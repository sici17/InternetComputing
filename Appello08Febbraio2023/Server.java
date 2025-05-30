package it.unical.dimes.reti.usermade.Appello08Febbraio2023;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class Server {
    private final static int portTCP=3000;
    private final static int portUDP=4000;
    private final static int portMulticast=5000;
    private final static String addressMulticast="230.0.0.1";
    private final static String addressServer="127.0.4.1";

    private static int ID=0;
    private Semaphore mutex = new Semaphore(1);


    private static MulticastSocket multicastSocket;

    private static Map<Integer, InetAddress> partecipazioni;
    private static List<Concorso> concorsi;

    private static ServerSocket serverSocket;
    private static DatagramSocket datagramSocket;


    public Server(List<Concorso> concorsi){
        this.concorsi=new LinkedList<>(concorsi);
        this.partecipazioni = Collections.synchronizedMap(new HashMap<Integer, InetAddress>());
        init();
    }

    public void init() {
        try {
            serverSocket = new ServerSocket(portTCP);
            datagramSocket = new DatagramSocket(portUDP);

            for (Concorso concorso : concorsi) {
                new Thread(() -> handleRequests(concorso)).start();
            }

            new CancelHandler(datagramSocket).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequests(Concorso concorso) {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                new RequestHandler(socket, concorso).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class RequestHandler extends Thread{
        private Socket socket;
        private static Concorso concorso;
        private boolean attivo;

        public RequestHandler(Socket socket, Concorso c){
            concorso = c;
            this.attivo=true;
            this.socket=socket;
        }

        public void run(){
            while(attivo) {
                Date now = Calendar.getInstance().getTime();
                if(now.after(concorso.getScadenza())){
                    attivo=false;
                    break;
                }
                try {
                    System.out.println("RequestHandler avviato.");
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    Partecipazione p = (Partecipazione) ois.readObject();

                    System.out.println("Partecipazione ricevuta: " + p.toString());
                    String msg;
                    Date instant = Calendar.getInstance().getTime();
                    if (isValid(p, instant)) {
                        System.out.println("Partecipazione valida.");
                        mutex.acquire();
                        ID++;
                        partecipazioni.put(ID, socket.getInetAddress());
                        mutex.release();
                        msg = ID + "-" + instant;

                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println(msg);

                    } else {
                        System.out.println("Partecipazione non valida.");
                        msg = "NOT_ACCEPTED";
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println(msg);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try{
                System.out.println("Timeout. Calcolo vincitori.");
                InetAddress multiAdd = InetAddress.getByName(addressMulticast);
                multicastSocket = new MulticastSocket(portMulticast);
                byte[] buf = new byte[256];
                String res = scegliVincitori(concorso);
                buf = res.getBytes();
                DatagramPacket dp = new DatagramPacket(buf, buf.length, multiAdd, portMulticast);
                multicastSocket.send(dp);
                System.out.println("Ho inviato i risultati.");

            }catch(IOException e){
                e.printStackTrace();
            }
        }

        private static String scegliVincitori(Concorso c){
            StringBuilder sb = new StringBuilder();
            sb.append(c.getID());
            Random rand = new Random();
            int i = rand.nextInt(partecipazioni.keySet().size());
            for(int j=0; j<c.getPosti(); j++){
                sb.append("posto "+j+": "+partecipazioni.get(i)+"\n");
            }
            return sb.toString();
        }

        private static boolean isValid(Partecipazione p, Date instant){
                if(concorso.getID() == p.getIDconcorso()){
                    if(instant.after(concorso.getScadenza())) return false;
                    if( p.getNome()==null || p.getCognome() == null || p.getCF() == null || p.getCurriculum()==null) return false;
                }
            return true;
        }
    }

    class CancelHandler extends Thread{
        private DatagramSocket datagramSocket;

        public CancelHandler(DatagramSocket ds){
            this.datagramSocket=ds;
        }

        public void run(){
            try{
                System.out.println("Cancelhandler avviato.");
                byte[] buf = new byte[256];
                DatagramPacket dp = new DatagramPacket(buf, buf.length);
                datagramSocket.receive(dp);

                String res = new String(dp.getData());
                int id = Integer.parseInt(res);
                System.out.println("Cancelhandler ricevuta: " + id);
                String msg;

                mutex.acquire();
                if(partecipazioni.containsKey(id)){
                    partecipazioni.remove(id);
                    System.out.println("Partecipazione rimossa correttamente.");
                    msg = "true";
                }else{
                    msg = "false";
                    System.out.println("Partecipazione non rimossa correttamente.");
                }
                mutex.release();
                byte[] respo= new byte[256];
                respo=msg.getBytes();
                DatagramPacket response = new DatagramPacket(respo, respo.length, dp.getAddress(), dp.getPort());
                dp.setData(respo);

            }catch(IOException e){
                e.printStackTrace();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args){
        Concorso c1 = new Concorso(1, 2, new Date(2024, 11, 12));
        Concorso c2 = new Concorso(2, 3, new Date(2024, 0, 12));
        Concorso c3 = new Concorso(3, 4, new Date(2024, 11, 12));
        LinkedList<Concorso> concorsi = new LinkedList<>();
        concorsi.add(c1);
        concorsi.add(c2);
        concorsi.add(c3);
        Server server = new Server(concorsi);

    }


}

package it.unical.dimes.reti.usermade.Appello14Marzo2024;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class Server {
    private final static int portTCP=3000, portUDP=4000, portSUB=4000;
    private static int STATE_ID=0;
    private Semaphore mutex = new Semaphore(1);
    private final static String bad = "403: BAD REQUEST";

    private Map<Integer, List<StatoSensore>> rilevazioni; //<ID_SENSORE, list Stati>
    private Map<Integer, InetAddress> iscrizioni; //<ID_SENSORE, inetadd>

    public Server(){

        rilevazioni = Collections.synchronizedMap(new HashMap<>());
        iscrizioni = Collections.synchronizedMap(new HashMap<>());

        init();
    }

    private void init(){
        try{
            ServerSocket sensor = new ServerSocket(portTCP);
            ServerSocket sub = new ServerSocket(portSUB);
            while(true){
                Socket s = sensor.accept();
                new SensorHandler(s).start();
                Socket s2 = sub.accept();
                new SubmissionHandler(s2).start();
            }
        }catch(IOException e) {
            e.printStackTrace();
        }

    }

    class SensorHandler extends Thread{
        private Socket socket;

        public SensorHandler(
                Socket s
        ){
            socket = s;
        }

        public void run(){
            System.out.println("Sensor Handler avviato.");
            try{
                    Calendar now = Calendar.getInstance();
                    PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    StatoSensore state = (StatoSensore) ois.readObject();

                    if(isActive(now)){
                        System.out.println("Richiesta ricevuta in orario.");
                        System.out.println("Ho ricevuto lo stato: "+state.toString());
                        mutex.acquire();
                        int idSens = state.getSensoreID();

                        if(! rilevazioni.containsKey(idSens) || rilevazioni.get(idSens).isEmpty()){
                            STATE_ID++;
                            state.setStateNumber(STATE_ID);
                            List<StatoSensore> stati = new LinkedList<>();
                            stati.add(state);
                            rilevazioni.put(idSens, stati);
                            System.out.println("Nessuna rilevazione precedente per il sensore "+idSens+". Creato da zero.");
                            pw.println(STATE_ID);

                            String notifica = idSens+"#"+STATE_ID+"#"+state.getUmidità()+"#"+state.getTemperatura();
                            inviaNotifica(notifica, idSens);

                            }else{
                                if(isValid(state, idSens)){
                                    STATE_ID++;
                                    state.setStateNumber(STATE_ID);
                                    System.out.println("Stato valido.");
                                    List<StatoSensore> stati = rilevazioni.get(idSens);
                                    stati.add(state);
                                    rilevazioni.put(idSens, stati);
                                    pw.println(STATE_ID);
                                    System.out.println("Erano presenti altre rilevazioni. Aggiorno.");
                                    String notifica = idSens+"#"+STATE_ID+"#"+state.getUmidità()+"#"+state.getTemperatura();
                                    inviaNotifica(notifica, idSens);
                                }else{
                                    System.out.println("Stato non valido.");
                                    pw.println("invalid state.");
                                }
                            }
                            mutex.release();
                    }else {
                        System.out.println("Richiesta ricevuta fuori orario.");
                        pw.println(bad);
                    }

                    socket.close();

            }catch(IOException e){
                e.printStackTrace();
            }catch(ClassNotFoundException e){
                e.printStackTrace();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }

        private boolean isValid(StatoSensore stato, int id){
            if(mediaUm(rilevazioni.get(id), stato.getUmidità()) && mediaTem(rilevazioni.get(id), stato.getTemperatura())) return false;
            return true;
        }

        private boolean mediaUm(List<StatoSensore> stati, double um){
            double val=0;
            for(StatoSensore s : stati){
                val+=s.getUmidità();
            }
            double percentage = val*5 / 100;

            if(val+percentage==um || val-percentage==um) return true;
            return false;
        }

        private boolean mediaTem(List<StatoSensore> stati, double temp){
            double val=0;
            for(StatoSensore s : stati){
                val+=s.getTemperatura();
            }
            double percentage = val*5 / 100;

            if(val+percentage==temp || val-percentage==temp) return true;
            return false;
        }

        private boolean isActive(Calendar now){
            int hour = now.get(Calendar.HOUR_OF_DAY);
            if(hour>= 8 && hour<=13) return true;
            return false;
        }

        private void inviaNotifica(String notifica, int id){
            try{
                for(Integer i : iscrizioni.keySet()){
                    if(i!=id){
                        DatagramSocket ds = new DatagramSocket();
                        byte[] buf = new byte[256];
                        buf = notifica.getBytes();
                        DatagramPacket dp = new DatagramPacket(buf, buf.length, iscrizioni.get(i), portUDP);
                        ds.send(dp);
                        System.out.println("Ho inviato la notifica al sensore con id "+i);
                        ds.close();
                    }
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    class SubmissionHandler extends Thread{
    private Socket  socket;
        public SubmissionHandler(Socket cli){
            System.out.println("SubmissionHandler avviato.");
            socket=cli;
        }

        public void run(){
            try{

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line = in.readLine();
                    int idSens = Integer.parseInt(line);
                    System.out.println("Ho ricevuto una richiesta di sottoscrizione dal sensore con id "+idSens);
                    if(! iscrizioni.containsKey(idSens)){
                        System.out.println("Utente mai registrato.");
                        iscrizioni.put(idSens, socket.getInetAddress());
                    }
                    socket.close();

            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args){
        Server server = new Server();
    }



}

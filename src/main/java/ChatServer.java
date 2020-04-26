import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ChatServer {

    private static final Map<String,PrintWriter> clients = new HashMap<>();

    public static void main(String[] args){
        System.out.println("The chat server is running...");
        Runnable startServer = () -> {
            ExecutorService pool = Executors.newFixedThreadPool(20);
            try (ServerSocket listener = new ServerSocket(59001)) {
                while (true) {
                    pool.execute(new Handler(listener.accept()));
                }
            } catch (Exception e) {
                System.out.println("The chat server throw exception");
            }
        };
        new Thread(startServer).start();
    }

    private static class Handler implements Runnable {
        private String name;
        private final Socket socket;
        private Scanner in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                // Keep requesting a name until we get a unique one.
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.nextLine();
                    synchronized (clients) {
                        if (!name.isEmpty() && !clients.containsKey(name)) {
                            break;
                        }
                    }
                }

                out.println("NAMEACCEPTED " + name);
                clients.forEach((clientName,writer)->{
                    writer.println("MESSAGE " + name + " has joined");
                });

                clients.put(name,out);

                while (true) {

                    String input = in.nextLine();
                    System.out.println("server receive input:"+input);
                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    }
                    clients.forEach((clientName,writer)->{
                        if(input.startsWith("EXCHANGE")||input.startsWith("ENDEXCHANGE")){
                            if(clientName.equals(name))return;
                            writer.println(input);
                        }else{
                            writer.println("ENCRYPTMESSAGE " + name + ": " + input);
                        }
                    });

                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (out != null) {
                    clients.remove(name);
                    clients.forEach((clientName,writer)->{
                        writer.println("MESSAGE " + name + " has left");
                    });
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("throw exception");
                }
            }
        }
    }
}
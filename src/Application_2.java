import java.io.*;
import java.net.*;
import java.util.ArrayList;

class CONFIG {
    public static String HOST = "localhost";
    public static int PORT = 6060; 
    public static int BUFFER_SIZE = 1024;
}

public class Application_2 {
    public static void main(String[] args) {
        Server server = new Server();
        server.start();

        try { Thread.sleep(500); } 
        catch (InterruptedException e) { e.printStackTrace(); }

        Client client_1 = new Client();
            System.out.println("client 1 got response: ");
            client_1.connect_to_server();
            
            Client client_2 = new Client();
            System.out.println("client 2 got response: ");
            client_2.connect_to_server();

        server.terminate_server();
    }
}

class Server extends Thread {
    private boolean thread_alive;
    private DatagramSocket server_socket;

    private ArrayList<RegisteredClient> registered_clients;

    public Server() { 
        this.thread_alive = true;
        this.registered_clients = new ArrayList<RegisteredClient>();
    }

    @Override
    public void run() {
        while (thread_alive) {
            try {
                this.server_socket = new DatagramSocket(CONFIG.PORT);
                System.out.println("server started successfully on port: " + CONFIG.PORT);
                System.out.println();
     
                while (true && thread_alive) {
                    byte[] buffer = new byte[CONFIG.BUFFER_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    
                    this.server_socket.receive(packet);

                    InetAddress client_addr = packet.getAddress();
                    int client_port = packet.getPort();
               
                    if (this.registered_clients.size() == 0) { this.registered_clients.add( new RegisteredClient(client_addr.getHostAddress(), client_port) ); } 
                    else {
                        for (RegisteredClient registered_client : this.registered_clients) {    
                            if (registered_client.port != client_port) {
                                this.registered_clients.add( new RegisteredClient(client_addr.getHostAddress(), client_port) );
                                break;
                            }
                        }
                    }
 
                    StringBuilder sb = new StringBuilder();
                    for (RegisteredClient registered_client : this.registered_clients) {
                        sb.append(registered_client.host);
                        sb.append(":");
                        sb.append(registered_client.port);
                        sb.append(" | ");
                    }
                    sb.delete(sb.length() - 3, sb.length());
                    
                    byte[] response_data = sb.toString().getBytes();
                    DatagramPacket response_packet = new DatagramPacket(response_data, response_data.length, client_addr, client_port);

                    this.server_socket.send(response_packet);

                    try { Thread.sleep(1); } 
                    catch (InterruptedException e) { e.printStackTrace(); }
                }  
            } 
            catch (Exception err) { System.out.println("[error while server launching]"); }
        }
    }

    public void terminate_server() {
        try {
            this.thread_alive = false;
            this.server_socket.close();
        } catch (Exception err) { System.out.println("[error while server termination]"); }
    }
}

class Client {
    public void connect_to_server() {
        try { 
            DatagramSocket socket = new DatagramSocket();
            InetAddress server_addr = InetAddress.getByName(CONFIG.HOST);
            
            byte[] buffer = "nothing".getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, server_addr, CONFIG.PORT);
            socket.send(packet); 

            buffer = new byte[CONFIG.BUFFER_SIZE];
            packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            String received_message = new String(packet.getData(), 0, packet.getLength());
            System.out.println(received_message);
            System.out.println("");
        } catch (IOException e) { System.out.println("[error while connecting to the server]"); }
    }
}

class RegisteredClient {
    protected String host;
    protected int port;

    public RegisteredClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
}
import java.io.*;
import java.net.*;

class CONFIG {
    public static String HOST = "localhost";
    public static int PORT = 5050; 
}

public class Application_1 {
    public static void main(String[] args) {
        Server server = new Server();
        server.start();

        try { Thread.sleep(100); } 
        catch (InterruptedException e) { e.printStackTrace(); }

        Client client = new Client();
            client.request_factorial_calculation(10);
            client.request_sum_calculation(5, 5);
            client.request_difference_calculation(10, 5);

        server.terminate_server();
    }
}

class Server extends Thread {
    private boolean thread_alive;
    private ServerSocket server_socket;

    public Server() { this.thread_alive = true; }

    @Override
    public void run() {
        while (thread_alive) {
            try {
                this.server_socket = new ServerSocket(CONFIG.PORT);
                System.out.println("server started successfully on port: " + CONFIG.PORT);
                System.out.println();
     
                while (true && thread_alive) {
                    Socket client_socket = this.server_socket.accept();
        
                    ObjectOutputStream socket_out = new ObjectOutputStream(client_socket.getOutputStream());
                    ObjectInputStream socket_in = new ObjectInputStream(client_socket.getInputStream());
                    
                    Request request = (Request) socket_in.readObject();
                    handle_request(socket_out, request);
                    
                    client_socket.close();

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

    public void handle_request(ObjectOutputStream socket_out, Request request) {
        try {
            long start_time, end_time;
            int result;

            switch_label:
            switch (request.task) {
                case "calculate_factorial":
                    if (request.data[0] < 0) { 
                        socket_out.writeObject( new Response("fail", "factorial is not defined for negative numbers", 0, 0) );
                        break switch_label;
                    }


                    start_time = System.nanoTime();
                    result = 1;
                    for (int k = 1; k <= request.data[0]; k++) { result *= k; }
                    end_time = System.nanoTime();

                    socket_out.writeObject( new Response("ok", "", result, end_time - start_time) );
                    break;

                case "calculate_sum":
                    start_time = System.nanoTime();
                    result = request.data[0] + request.data[1];
                    end_time = System.nanoTime(); 

                    socket_out.writeObject( new Response("ok", "", result, end_time - start_time) );
                    break;

                case "calculate_difference":
                    start_time = System.nanoTime();
                    result = request.data[0] - request.data[1];
                    end_time = System.nanoTime();
                    
                    socket_out.writeObject( new Response("ok", "", result, end_time - start_time) );
                    break;

                default:
                    break;
            }
        } catch (Exception err) { System.out.println("[error while handling a request]"); }
    }
}


class Client {
    public void request_factorial_calculation(int number) { 
        try {
            Socket socket = new Socket(CONFIG.HOST, CONFIG.PORT);

            ObjectOutputStream socket_out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream socket_in = new ObjectInputStream(socket.getInputStream());

            socket_out.writeObject( new Request("calculate_factorial", new int[]{number}) ); 

            Response response = (Response) socket_in.readObject();
            if (response.status.equals("fail")) { System.out.println("client [request error: " + response.message + "]"); }
            else { System.out.println("[server response]: factorial result: " + response.result + " | time - " + response.time + "ns"); }

            socket.close(); 
        } catch (Exception err) { System.out.println("client [error while a request to the server]"); }
    }

    public void request_sum_calculation(int number_1, int number_2) { 
        try {
            Socket socket = new Socket(CONFIG.HOST, CONFIG.PORT);

            ObjectOutputStream socket_out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream socket_in = new ObjectInputStream(socket.getInputStream());

            socket_out.writeObject( new Request("calculate_sum", new int[]{number_1, number_2}) ); 

            Response response = (Response) socket_in.readObject();
            if (response.status.equals("fail")) { System.out.println("client [request error: " + response.message + "]"); }
            else { System.out.println("[server response]: sum result: " + response.result + " | time - " + response.time + "ns"); }

            socket.close(); 
        } catch (Exception err) { System.out.println("client [error while a request to the server]"); }
    }

    public void request_difference_calculation(int number_1, int number_2) { 
        try {
            Socket socket = new Socket(CONFIG.HOST, CONFIG.PORT);

            ObjectOutputStream socket_out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream socket_in = new ObjectInputStream(socket.getInputStream());

            socket_out.writeObject( new Request("calculate_difference", new int[]{number_1, number_2}) ); 

            Response response = (Response) socket_in.readObject();
            if (response.status.equals("fail")) { System.out.println("client [request error: " + response.message + "]"); }
            else { System.out.println("[server response]: difference result: " + response.result + " | time - " + response.time + "ns"); }

            socket.close(); 
        } catch (Exception err) { System.out.println("client [error while a request to the server]"); }
    }
}

class Request implements Serializable {
    protected String task;
    protected int[] data;

    public Request(String task, int[] data) {
        this.task = task;
        this.data = data;
    }
}

class Response implements Serializable {
    protected String status;
    protected String message;
    protected int result;
    protected long time;

    public Response(String status, String message, int result, long time) {
        this.status = status;
        this.message = message;
        this.result = result;
        this.time = time;
    }
}
package KnockKnock;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{
    protected static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket client;
    private BufferedReader input;
    private PrintWriter output;
    private String userName;

    public ClientHandler(Socket client) throws IOException {
        this.client = client;
        this.input = new BufferedReader(new InputStreamReader(client.getInputStream()));
        this.output = new PrintWriter(client.getOutputStream(), true);
        this.userName = input.readLine();
        clientHandlers.add(this);
        sendMessage(userName + " has entered the chat!");
    }
    @Override
    public void run() {
        String message;

        while (true){
            try {
                message = input.readLine();
                if(message.endsWith("exit"))
                {
                    close();
                    break;
                }
                sendMessage(message);

            } catch (IOException e){
                System.err.println("ClientHandler error!");
                break;
            }
        }
    }
    public void sendMessage(String message){
        for(ClientHandler clientHandler:clientHandlers){
            try {
                if(!clientHandler.userName.equals(userName)){
                    clientHandler.output.println(message);
                }
            }catch (Exception e){
                System.err.println("Error!");
            }
        }
    }
    protected void close(){
        try{
            clientHandlers.remove(this);
            sendMessage(userName + " has left the chat!");
            client.shutdownInput();
            client.shutdownOutput();
            client.close();
        } catch (IOException e) {
            System.err.println("Could not close socket");
            System.exit(-1);
        }
    }
}

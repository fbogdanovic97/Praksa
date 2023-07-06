package KnockKnock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Scanner;

public class UDPClient {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String userName;

    private static final byte[] BUFFER = new byte[256];
    public UDPClient(Socket socket, String userName) throws IOException {
        this.socket = socket;
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new PrintWriter(socket.getOutputStream(), true);
        this.userName = userName;
    }

    public void sendMessageToClientHandler(){
        try{
            output.println(userName);
            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()){
                String message = scanner.nextLine();
                output.println(userName + ": " + message);
            }
        }catch (Error e){
            System.err.println("SendMsgtoCH error!");
        }
    }

    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String message;
                try {
                    while ((message = input.readLine())!= null){
                        System.out.println(message);
                    }
                }catch (IOException e){
                    close();
                }

            }
        }).start();
    }

    protected void close(){
        try{
            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
        } catch (IOException e) {
            System.out.println("Could not close socket");
            System.exit(-1);
        }
    }


    public static void main(String[] args) throws IOException {
        InetAddress address;
        String receivedData;
        InetAddress group = InetAddress.getByName("239.255.255.255");
        try (MulticastSocket socket = new MulticastSocket(6666)) {
            socket.joinGroup(group);
            DatagramPacket packet = new DatagramPacket(BUFFER, BUFFER.length);
            socket.receive(packet);

            address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(BUFFER, BUFFER.length, address, port);
            receivedData = new String(packet.getData(), 0, packet.getLength());
        }

        System.out.println(receivedData);
        String[] receivedDataParts = receivedData.split("__");
        String serverName = receivedDataParts[0];
        int portNumber = Integer.parseInt(receivedDataParts[1].trim());

        Scanner scanner = new Scanner(System.in);
        System.out.println("What is your user name?");
        String userName = scanner.nextLine();

        Socket socket = new Socket(address, portNumber);
        UDPClient client = new UDPClient(socket, userName);
        client.listenForMessage();
        client.sendMessageToClientHandler();
    }
}

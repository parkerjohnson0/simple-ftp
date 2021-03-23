/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 *
 * @author peppe
 */
public class clientFTP {

    public static void main(String[] args) {
        new clientFTP();
    }

    public clientFTP() {
        //Command socket
        Socket clientSock;
        BufferedReader in;
        PrintWriter out;
        int commandPort = 1040;
        int dataPort = 0;
        Scanner input = new Scanner(System.in);
        while (true) {
            try {
                clientSock = new Socket("localhost", commandPort);
                System.out.println("Connected to FTP server");
                in = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
                out = new PrintWriter(clientSock.getOutputStream(), true);
                System.out.println("What type of connection do you want? Valid inputs are: \n"
                        + "PORT portnumber\nPASV");

                String connectionType = input.nextLine();
                //getting port number if active connection requested.
                if (!(connectionType.equals("PASV"))) {
                    String[] aConnectionType = connectionType.split(" ");
                    dataPort = Integer.parseInt(aConnectionType[1]);
                }

                out.println(connectionType);
                //print 230 OK code from server
                System.out.println(in.readLine());

                System.out.println("Commands: GET/PUT fName.fExt");
                String clientRequest = input.nextLine();
                out.println(clientRequest);
                String[] requestToArray = clientRequest.split(" ");
                String requestType = requestToArray[0];
                String fileName = requestToArray[1];
                //print  OK message code from server
                System.out.println(in.readLine());
                if (requestType.equals("GET")) {

                    File file = new File("src/client/" + fileName);
                    if (connectionType.equals("PASV")) {
                        dataPort = Integer.parseInt(in.readLine());
                        Socket clientDataSocket = new Socket("localhost", dataPort);
                        System.out.println("DEBUG: Passive connection established. ");
                        receiveData(clientDataSocket, file);
                        clientDataSocket.close();
                    } else {
                        ServerSocket dataSock = new ServerSocket(dataPort);
                        out.println("READY");
                        Socket serverDataSocket = dataSock.accept();

                        //debug. delete when finished. 
                        System.out.println("DEBUG: Active connection established");
                        System.out.println("DEBUG: Data port is " + dataPort);
                        receiveData(serverDataSocket, file);
                        dataSock.close();
                    }

                } else if (requestType.equals("PUT")) {
                   
                    File file = new File("src/client/" + fileName);
                    if (connectionType.equals("PASV")) {
                        dataPort = Integer.parseInt(in.readLine());
                        Socket clientDataSocket = new Socket("localhost", dataPort);
                        System.out.println("DEBUG: Passive connection established. ");
                        sendData(clientDataSocket, file);
                        clientDataSocket.close();
                    } else {
                        ServerSocket dataSock = new ServerSocket(dataPort);
                        out.println("READY");
                        Socket serverDataSocket = dataSock.accept();

                        //debug. delete when finished. 
                        System.out.println("DEBUG: Active connection established");
                        System.out.println("DEBUG: Data port is " + dataPort);
                        sendData(serverDataSocket, file);
                        dataSock.close();
                        serverDataSocket.close();
                    }

                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }

    }

    private void receiveData(Socket dataSocket, File fName) {
        BufferedReader in = null;
        PrintWriter printWriter = null;
        try {
            in = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
            printWriter = new PrintWriter(fName);
        } catch (Exception e) {
            System.out.println("Failed setting up I/O on data socket.");
            e.printStackTrace();
        }
        try {

            String line = in.readLine();
            while (!(line.equals("File Ended"))) {
                printWriter.println(line);
                line = in.readLine();

            }

            printWriter.close();
            
            System.out.println("DEBUG: DATA RECEIVED");
        } catch (Exception e) {
            System.out.println("Failed writing file.");
            e.printStackTrace();
        }

    }

    private void sendData(Socket dataSocket, File file) {
        BufferedReader in;
        PrintWriter out = null;
        Scanner fileRead = null;
        try {
            fileRead = new Scanner(file);
            in = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
            out = new PrintWriter(dataSocket.getOutputStream(), true);
        } catch (Exception e) {
            System.out.println("Failed setting up I/O on data socket.");
            e.printStackTrace();
        }
        while (fileRead.hasNextLine()) {
            out.println(fileRead.nextLine());
        }
        out.println("File Ended");
        System.out.println("DEBUG: DATA SENT");

    }
}

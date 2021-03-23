/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author peppe
 */
public class serverFTP {

    public static void main(String[] args) {
        new serverFTP();
    }

    public serverFTP() {

       
        //Command socket
        ServerSocket servSock;
        Socket clientSock;
        Scanner input = new Scanner(System.in);
        BufferedReader in;
        PrintWriter out;
        int commandPort = 1040, dataPort = 0;

        try {
             ;
            //waits on client to connect
            servSock = new ServerSocket(commandPort);
            System.out.println("Waiting on client to connect.");
            while (true) {
                boolean isPasv = false, isActive = false;
                clientSock = servSock.accept();
                System.out.println("Client connected.");
                //sets up I/O
                in = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
                out = new PrintWriter(clientSock.getOutputStream(), true);
                //receives connection type and parses
                String connectionType = in.readLine();
                //just for debugging purposes. dont need
                System.out.println("DEBUG: Connection type: " + connectionType);

                if (connectionType.equals("PASV")) {
                    isPasv = true;
                    dataPort = (int) (Math.random() * 3975 + 1025);

                } else {
                    isActive = true;
                    //extract port from string
                    dataPort = Integer.parseInt((connectionType.split(" "))[1]);
                }
                //send message code to client TODO: handle error at this stage 
                out.println("230 OK");
                //getting PUT/GET file request string
                String clientRequest = in.readLine();
                //just for debugging purposes. dont need
                System.out.println("Client requested: " + clientRequest);
                //split request into usable chunks and assign to their own variables
                String[] requestToArray = parseRequest(clientRequest);
                String requestType = requestToArray[0];
                String fileName = requestToArray[1];

                if (requestType.equals("GET")) {
                    ServerSocket pasvDataSocket;
                    Socket actvDataSocket;
                    Socket clientDataSocket;
                    File file = new File("src/server/" + fileName);
                    boolean fileExists = file.exists();
                    System.out.println("server/" + fileName + " exists: " + fileExists);
                    if (fileExists) {
                        out.println("220 OK");
                        //data socket

                        if (isActive) {
                            //server receives ready message from client
                            System.out.println(in.readLine());
                            actvDataSocket = new Socket("localhost", dataPort);
                            System.out.println("DEBUG: Active connection established");
                            sendData(actvDataSocket, file);
                            actvDataSocket.close();

                        } else if (isPasv) {
                            out.println(dataPort);
                            pasvDataSocket = new ServerSocket(dataPort);

                            clientDataSocket = pasvDataSocket.accept();
                            System.out.println("DEBUG: Passive connection established. ");
                            sendData(clientDataSocket, file);
                            pasvDataSocket.close();
                            clientDataSocket.close();

                        }

                    } else {
                        out.println("404 Bad Request");
                    }
                } else if (requestType.equals("PUT")) {
                    ServerSocket pasvDataSocket;
                    Socket actvDataSocket;
                    Socket clientDataSocket;
                    File file = new File("src/server/" + fileName);
                    boolean serverAccept = true;
                    if (serverAccept) {
                        out.println("210 OK");
                        //data socket

                        if (isActive) {
                            //server receives ready message from client
                            System.out.println(in.readLine());
                            actvDataSocket = new Socket("localhost", dataPort);
                            System.out.println("DEBUG: Active connection established");
                            receiveData(actvDataSocket, file);
                            actvDataSocket.close();

                        } else if (isPasv) {
                            out.println(dataPort);
                            pasvDataSocket = new ServerSocket(dataPort);

                            clientDataSocket = pasvDataSocket.accept();
                            System.out.println("DEBUG: Passive connection established. ");
                            receiveData(clientDataSocket, file);
                            pasvDataSocket.close();
                            clientDataSocket.close();

                        }

                    } else {
                        out.println("407 Bad Request");
                    }

                }
                
                clientSock.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


private String[] parseRequest(String string) {
        String[] temp = string.split(" ");

        return temp;
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
}

package com.chatapp.chatapp.server;

import com.chatapp.chatapp.client.ClientController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class ServerController {

    private static final int PORT = 5000;
    private static HashSet<ObjectOutputStream> clientsInputStreamCollection = new HashSet<>();
    private boolean isRunning = false;
    private ServerSocket serverSocket;

    @FXML
    private TextArea displayServer;

    @FXML
    public void initialize(){
        appendStatus("Server Running ....");
    }

    public void appendStatus(String message) {
        Platform.runLater(() -> displayServer.appendText(message + "\n"));
    }

    @FXML
    void addClientOnAction(ActionEvent event) {
        if (!isRunning){
            start();
        }

        openClient();
    }

    private void start() {
        isRunning = true;

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                appendStatus("Server running on " + PORT);
                while(isRunning){
                    Socket clientSocket = serverSocket.accept();
                    appendStatus("client connected");

                    Thread clientThread = new Thread(new Handler(clientSocket));
                    clientThread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (isRunning){
                    appendStatus("Error starting server: " + e.getMessage());
                }
            }
        }).start();

    }

    private void openClient() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/client.fxml"));
                Scene scene = new Scene(loader.load(), 600, 600);
                Stage stage = new Stage();
                stage.setTitle("Client");
                stage.setScene(scene);
                ;               stage.setOnCloseRequest(event -> {
                    ClientController controller = loader.getController();
                    controller.Disconnect();
                });
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private class Handler implements Runnable{
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private String clientName;

        Handler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                while(true){
                    out.writeObject("SUBMITNAME");
                    clientName = (String) in.readObject();
                    if (!clientName.trim().isEmpty() && clientName != null){
                        break;
                    }
                    appendStatus("Invalid name, requesting again");
                }

                out.writeObject("NAMEACCEPTED");
                appendStatus("Client " + clientName + " connected");
                broadcast("TEXT " + clientName + " joined the chat");

                synchronized (clientsInputStreamCollection){
                    clientsInputStreamCollection.add(out);
                }

                while(true){
                    Object message = in.readObject();
                    if (message == null) break;
                    if (message instanceof String){
                        String text = (String) message;

                        if(text.equals("TIME")){
                            broadcast("TIME " + clientName + ": " + "10.27 AM");
                            break;
                        }

                        else if(text.equals("DATE")){
                            broadcast("DATE " + clientName + ": " + "05/23/2025");
                            break;
                        }

                        else if(text.equals("UPTIME")){
                            broadcast("UPTIME " + clientName + ": " + "3 min");
                            break;
                        }

                        else if(text.equals("BYE")){
                            broadcast("BYE " + clientName + ": " + "disConnected");
                            break;
                        }

                        broadcast("TEXT " + clientName + ": " +text);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                appendStatus("Error connecting to client");
            }finally {
                if (clientName != null){
                    appendStatus("Client " + clientName + " disconnected");
                    broadcast("TEXT " + clientName + " left the chat");
                }

                synchronized (clientsInputStreamCollection){
                    clientsInputStreamCollection.remove(out);
                }

                try {
                    socket.close();
                } catch (IOException e) {
                    appendStatus("Error closing client socket");
                    e.printStackTrace();
                }
            }
        }

        private void broadcast ( String message){
            synchronized (clientsInputStreamCollection){
                for (ObjectOutputStream writer : clientsInputStreamCollection){
                    try {
                        writer.writeObject(message);
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        appendStatus("Error broadcasting the message..");
                    }
                }
            }
        }

        private void broadcast(String header,byte [] imageData) {
            synchronized (clientsInputStreamCollection){
                for (ObjectOutputStream writer : clientsInputStreamCollection){
                    try {
                        writer.writeObject(header);
                        writer.writeObject(imageData);
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        appendStatus("Error broadcasting the image..");
                    }
                }
            }
        }
    }



}

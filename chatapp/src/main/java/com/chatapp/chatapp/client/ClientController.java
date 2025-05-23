package com.chatapp.chatapp.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;

public class ClientController {

    @FXML
    private ListView<Object> display;

    @FXML
    private TextField input;

    @FXML
    private Button sendbtn;

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String UserNames;
    private boolean IsnameAccepted = false;

    @FXML
    public void initialize(){
        display.setCellFactory(listView -> new ListCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                }

                if (item instanceof String) {
                    setText((String) item);
                }
            }
        });


        try {
            Socket socket = new Socket("localhost", 5000);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            Thread thread = new Thread(() -> Lisner());
            thread.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void promptForName(){
        Platform.runLater(()->{
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Enter your name");
            dialog.setHeaderText("Please enter your name");

            dialog.showAndWait().ifPresent(name ->{
                UserNames = name.trim();
                if (UserNames.isEmpty()){
                    display.getItems().add("Name cannot be empty, Please try again");
                    promptForName();
                }else {
                    try {
                        out.writeObject(UserNames);
                        out.flush();
                        dialog.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        display.getItems().add("Error sending name, Please try again");
                    }
                }
            });

            if (dialog.getResult().isEmpty()){
                Platform.exit();
            }
        });
    }

    private void Lisner() {
        try {
            while(true){
                Object message = in.readObject();
                if (message == null) break;
                if (message instanceof String){
                    String text = (String) message;

                    if (text.startsWith("SUBMITNAME")){
                        if (!IsnameAccepted){
                            promptForName();
                        }
                    }

                    else if (text.startsWith("NAMEACCEPTED")){
                        IsnameAccepted = true;
                        Platform.runLater(() -> display.getItems().add("Connected as " + UserNames));
                    }

                    else if (text.startsWith("TIME")){
                        Platform.runLater(() ->{
                            if (text.startsWith("TIME " + UserNames + ": ")){
                                display.getItems().add("server: " + text.substring(UserNames.length()+2+5));
                            }else {
                                display.getItems().add(text.substring(5));
                            }
                        });
                    }

                    else if (text.startsWith("DATE")){
                        Platform.runLater(() ->{
                            if (text.startsWith("DATE " + UserNames + ": ")){
                                display.getItems().add("server: " + text.substring(UserNames.length()+2+5));
                            }else {
                                display.getItems().add(text.substring(5));
                            }
                        });
                    }

                    else if (text.startsWith("UPTIME")){
                        Platform.runLater(() ->{
                            if (text.startsWith("UPTIME " + UserNames + ": ")){
                                display.getItems().add("server: " + text.substring(UserNames.length()+2+5));
                            }else {
                                display.getItems().add(text.substring(5));
                            }
                        });
                    }

                    else if (text.startsWith("BYE")){
                        Platform.runLater(() ->{
                            if (text.startsWith("BYE " + UserNames + ": ")){
                                display.getItems().add("server: " + text.substring(UserNames.length()+2+5));
                            }else {
                                display.getItems().add(text.substring(5));
                            }
                        });
                        Disconnect();
                    }

                    else if (text.startsWith("TEXT")){
                        Platform.runLater(() ->{
                            if (text.startsWith("TEXT " + UserNames + ": ")){
                                display.getItems().add("You: " + text.substring(UserNames.length()+2+5));
                            }else {
                                display.getItems().add(text.substring(5));
                            }
                        });
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            Platform.runLater(() -> display.getItems().add("Disconnected"));
        }finally {
            Disconnect();
        }
    }

    public void Disconnect() {
        try {
            if (out != null) {
                out.close();
            }

            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnSendOnAction(ActionEvent event) {
        String message = input.getText().trim();
        if (message.isEmpty()) return;
        try {
            out.writeObject(message);
            out.flush();
            input.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
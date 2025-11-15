package com.example;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();

    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        startRecieving();
    }

    public ObservableList<NtfyMessageDto> getMessages(){
        return messages;
    }

    public void sendMessage(String user, String text){
        connection.sendMessage(user, text);
    }

    private void startRecieving() {
        connection.receiveMessage(dto -> messages.add(dto));
    }
}

package com.example;

import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;


/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private static final String USERNAME = "Tatjana";

    private HelloModel model;

    @FXML
    private Button sendButton;

    @FXML
    private ProgressIndicator spinner;

    @FXML
    private ListView<NtfyMessageDto> messageList;

    @FXML
    private TextField messageField;

    @FXML
    private void initialize() {
        try {
            NtfyConnection connection = new NtfyHttpConnection("chatroom");

            model = new HelloModel(connection);

            messageList.setItems(model.getMessages());

            messageList.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(NtfyMessageDto item, boolean empty){
                    super.updateItem(item, empty);
                    if (empty || item == null){
                        setText(null);
                    } else{
                        setText(item.user() + ": " + item.message());
                    }
                }
            });

        } catch (IllegalStateException e){
            showError("Environment error", "Missing or invalid NTFY_SERVER_URL in .env");
            sendButton.setDisable(true);
            messageField.setDisable(true);
        }
    }

    public void onSendMessage(ActionEvent actionEvent) {
        if (model == null) {
            showError("Send failed", "Messaging is not available due to configuration error.");
            return;
        }
        String message = messageField.getText().trim();
        if (message.isEmpty()) return;

        messageField.clear();

        Task<Void> sendTask = new Task<>(){
            @Override
            protected Void call() throws Exception{
                Thread.sleep(1000); // 1 seconds
                model.sendMessage(USERNAME, message);
                return null;
            }
        };

        spinner.setVisible(true);
        sendButton.setDisable(true);

        sendTask.setOnSucceeded(e -> {
            spinner.setVisible(false);
            sendButton.setDisable(false);
        });

        sendTask.setOnFailed(e -> {
            spinner.setVisible(false);
            sendButton.setDisable(false);
            showError("Send failed", sendTask.getException());
        });

        new Thread(sendTask).start();
    }

    private void showError(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (text == null || text.isBlank()) {
            text = "An unknown error occurred.";
        }
        alert.setContentText(text);
        alert.showAndWait();
    }

    private void showError(String title, Throwable e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()){
            message = e.getClass().getSimpleName();
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);

        alert.setContentText(message);
        alert.showAndWait();
    }
}

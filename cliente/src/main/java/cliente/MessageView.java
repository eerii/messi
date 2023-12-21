package cliente;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class MessageView {
    @FXML
    private TextField messageInputField;

    @FXML
    private Button sendButton;

    @FXML
    private ListView<String> userListView;

    @FXML
    private Label errorLabel;

    @FXML
    public void handleSendButtonAction() {
        String message = messageInputField.getText();
        // TODO: Send the message
        messageInputField.clear();
    }

    @FXML
    public void initialize() {
        userListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // TODO: Load the messages with the selected user
            }
        });
    }
}
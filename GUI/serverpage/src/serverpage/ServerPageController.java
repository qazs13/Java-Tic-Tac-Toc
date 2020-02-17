/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverpage;

import database.Database;
import interfaces.Player;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Vector;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import server.Server;
/**
 *
 * @author E.S
 */

class ServerThread extends Thread
{
    Server server;
    public void run ()
    {
        server = new Server();
        server.runServer();
    }

    public void stopThread()
    {
        server.stopServer();
        System.out.println(server);
        this.stop();
    }
}

public class ServerPageController implements Initializable {
    
    private Label label;
    ServerThread serverThread;
    
    Database db = new Database();
    Vector<Player> allPlayers;
    
    @FXML
    private TableView<Player> playersTable;
    @FXML
    private TableColumn<Player, String> userNameCol;
    @FXML
    private TableColumn<Player, Integer> scoreCol;
    @FXML
    private TableColumn<Player, String> statusCol;
    void fetchPlayers(){
        allPlayers = db.retriveAllPlayers().Players;
        ObservableList<Player> _allPlayers = FXCollections.observableList(allPlayers);
        playersTable.setItems(_allPlayers);
    }
    
    @FXML
    private void serverOn(ActionEvent event) {
        serverThread = new ServerThread();
        serverThread.start();
        fetchPlayers();
    }
    
    @FXML
    private void serverOff(ActionEvent event) {
        serverThread.stopThread();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userNameCol.setCellValueFactory(new PropertyValueFactory<Player, String>("userName"));
        scoreCol.setCellValueFactory(new PropertyValueFactory<Player, Integer>("score"));
        statusCol.setCellValueFactory(new PropertyValueFactory<Player, String>("status"));
        fetchPlayers();
    }
    
}
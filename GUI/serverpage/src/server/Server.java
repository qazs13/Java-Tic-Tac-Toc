package server;

import com.google.gson.Gson;
import interfaces.*;
import database.Database;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    ServerSocket server_socket;
    Database db = new Database();
    String message = null;
    static HashMap<ServerHandler,String> map=new HashMap<>();
    Gson incomeObjectFromPlayer = null;
    public void runServer(){
        try {
            System.err.println("Server is Opened Succesfully");
            server_socket = new ServerSocket(5000);
            while(true){
                Socket internal_socket=server_socket.accept();
                System.err.println("New player is here");
                new ServerHandler(internal_socket);
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    class ServerHandler extends Thread
    {
        private DataInputStream input;
        private PrintStream output;
        private Socket playerSocket;

        public ServerHandler(Socket socket){
           
            try {
                this.playerSocket=socket;
                input = new DataInputStream(playerSocket.getInputStream());
                output = new PrintStream(playerSocket.getOutputStream());
                start();
                }
            catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
        @Override
        public void run(){
            incomeObjectFromPlayer = new Gson();
            while(true){
                try {
                    message = input.readLine();
                    XOInterface xoPlayer = incomeObjectFromPlayer.fromJson(message, XOInterface.class);
                    System.err.println(xoPlayer);
                    System.out.println(xoPlayer.getPlayer().getUserName());
                    if(xoPlayer.getTypeOfOpearation().equals(Messages.LOGIN))
                    {
                        PlayerLoginCheck(xoPlayer);                        
                    }
                    else if(xoPlayer.getTypeOfOpearation().equals(Messages.REGISTER))
                    {
                        PlayerRegister(xoPlayer);
                    }
                    else if(xoPlayer.getTypeOfOpearation().equals(Messages.PLAYING_SINGLE_MODE))
                        
                    {
                        db.createGame(xoPlayer);        
                    }
                    else if(xoPlayer.getTypeOfOpearation().equals(Messages.SINGLE_MODE_FINISHED))
                    {
                        db.endGame(xoPlayer);
                        db.updateScoreOffline(xoPlayer);
                        sendMsgToAllInternalSocket(xoPlayer);
                    }
                    else if(xoPlayer.getTypeOfOpearation().equals(Messages.GET_PLAYERS))
                    {
                        db.retriveAllPlayers();
                    }
                    else if(xoPlayer.getTypeOfOpearation().equals(Messages.INVITE))
                    {
                        sendMsgToDesiredInternalSocket(xoPlayer);
                        //need to be continued
                    }
                    
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
 
        void PlayerLoginCheck(XOInterface xoPlayer){
            if (db.checkLogIn(xoPlayer))
            {
                Hashmapper(xoPlayer); 
                xoPlayer = db.makePlayerOnline(xoPlayer);
                incomeObjectFromPlayer = new Gson();
                message = incomeObjectFromPlayer.toJson(xoPlayer);
                this.output.println(message);
                xoPlayer.setTypeOfOpearation(Messages.NEW_PLAYER_LOGGED_IN);
                sendMsgToAllInternalSocket(xoPlayer);
            }
            else
            {
                xoPlayer.setOpearationResult(false);
                incomeObjectFromPlayer = new Gson();
                message = incomeObjectFromPlayer.toJson(xoPlayer);
                this.output.println(message);
               // this.stop();
            }
        } 
        
        void PlayerRegister(XOInterface xoPlayer){
            if (db.checkSignUp(xoPlayer))
            {
                XOInterface xoPlayerRecived = new XOInterface();
                xoPlayerRecived.setTypeOfOpearation(Messages.SIGN_UP_ACCEPTED);
                boolean flag = db.createplayer(xoPlayer);
                xoPlayerRecived.setOpearationResult(flag);
                incomeObjectFromPlayer = new Gson();
                message = incomeObjectFromPlayer.toJson(xoPlayerRecived);
                this.output.println(message);                
            }
            else
            {   
                System.out.println("error");
                XOInterface xoPlayerRecived = new XOInterface();
                xoPlayerRecived.setTypeOfOpearation(Messages.SIGN_UP_REJECTED);
                xoPlayerRecived.setOpearationResult(false);
                incomeObjectFromPlayer = new Gson();
                message = incomeObjectFromPlayer.toJson(xoPlayerRecived);
                this.output.println(message);                
            }
        }
        
       void Hashmapper(XOInterface xoPlayer){
            map.put(this,xoPlayer.getPlayer().getUserName());
            System.out.println(map);
        }         
        
        void sendMsgToDesiredInternalSocket(XOInterface xoPlayer){
            ServerHandler key = null;
            incomeObjectFromPlayer = new Gson();
            for(Map.Entry kv: map.entrySet()){
                    if (kv.getValue().equals(xoPlayer.getGameLog().getOpponentPlayer())) {
                        key = (ServerHandler) kv.getKey();
                        System.out.println(key.toString());                             
                    }
            }
            message = incomeObjectFromPlayer.toJson(xoPlayer);
            key.output.println(message);
        }
        
       
        void sendMsgToAllInternalSocket(XOInterface xoPlayer){
            ServerHandler key = null;
            incomeObjectFromPlayer = new Gson();
            message = incomeObjectFromPlayer.toJson(xoPlayer);
            for(Map.Entry kv: map.entrySet()){
                  
                System.out.println(kv);
                key = (ServerHandler) kv.getKey();
                key.output.println(message);
            }
         }
    }
         public void stopServer()
         {
            try {
                System.out.println("Stopped Server");
                server_socket.close();
            } catch (IOException ex) {
                System.out.println("error");
            }
         }    
}
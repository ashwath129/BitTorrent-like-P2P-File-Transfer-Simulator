package com.ufl.server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class PeerConnectionHandler extends Thread {
    private Socket connection;
    private ObjectInputStream in;	//stream read from the socket
    private ObjectOutputStream out;    //stream write to the socket
    private int peerIndex;		//The index number of the client
    private int chunksAllowed;
    private ServerSocket globalConnection;
    private File[] chunks;

    PeerConnectionHandler (ServerSocket connection, int peerIndex, File[] chunks) throws IOException {
        this.globalConnection = connection;
        this.connection = connection.accept();
        this.peerIndex = peerIndex;
        this.chunks = chunks;
        this.chunksAllowed = chunks.length / 5;
    }

    @Override
    public void run () {
        super.run();
        try {
            int rangeStart = (peerIndex - 1) * chunksAllowed;
            int rangeEnd = rangeStart + this.chunksAllowed;
            File[] chunksForCurrentPeer = Arrays.copyOfRange(this.chunks, rangeStart, rangeEnd);
            //initialize Input and Output streams
            out = new ObjectOutputStream(connection.getOutputStream());
            out.writeObject(this.chunks.length);
            out.writeObject(chunksForCurrentPeer.length);

            int i = rangeStart + 1;
            for (File chunk:chunksForCurrentPeer) {
                System.out.println("sending chunk [" + i + "] to client at port " + peerIndex);
                sendMessage(chunk);
                i++;
            }

            out.close();
            System.out.println("[SERVER] closing connection for client " + peerIndex);
            connection.close();
        } catch (IOException ioException) {
            System.out.println("Disconnected with Client " + peerIndex);
        } finally {
            //Close connections
            try {
                out.close();
                connection.close();
            } catch(IOException ioException){
                System.out.println("Disconnected with Client " + peerIndex);
            }
        }
    }

    //send a message to the output stream
    private void sendMessage(Object msg) {
        try {
            out.writeObject(msg);
            out.flush();
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public String toString () {
        return "Connection handler for client " + this.peerIndex;
    }
}

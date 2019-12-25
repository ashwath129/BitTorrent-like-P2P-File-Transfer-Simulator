package com.ufl.util;


import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PeerThread extends Thread {

	private Socket threadSocket;
	
	private ObjectOutputStream outputDataStream;
	private ObjectInputStream inputDataStream;
	private String chunkLoc;
	//Initialize the peer server thread with the socket connector and the chunks location
	public PeerThread(Socket s, String chunkLoc){
		this.threadSocket = s;		
		this.chunkLoc=chunkLoc;		
	}

	public void run() {
		try {
			//create the new output stream of the thread
			outputDataStream = new ObjectOutputStream(threadSocket.getOutputStream());
			//create the new input stream for the server thread
			inputDataStream = new ObjectInputStream(threadSocket.getInputStream());
			while(true)
			{
			//For getting the chunk number that has to be sent to the neighbor
			int chunkId = (int) inputDataStream.readObject();
			if(chunkId<0)
				break;
			//Get the file chunks		
			File[] chunkFiles = new File(chunkLoc).listFiles();
			//String which we get , to check if it has to be sent or not
			String[] cid;
			File currentChunk=null;
			//This is a flag to check if it has the current chunk
			boolean chunkFlag = false;
				assert chunkFiles != null;
				for (File chunkFile : chunkFiles) {
					currentChunk = chunkFile;
					if (chunkId == Integer.parseInt(chunkFile.getName())) {
						//We get the chunk ids and check to modify the flag if the chunk is available
						chunkFlag = true;
						break;
					}
				}
			File send_chunk;
			if(chunkFlag)
			{	//send the chunk object after constructing it			
				send_chunk = currentChunk;
			}
			else
			{				
				send_chunk = null;
			}
				send_Chunk(send_chunk);											
			}		
			closeConn();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	private void send_Chunk(Object send_chunk) {
		try {	
            //Send the chunks to the neighbor		
			outputDataStream.writeObject(send_chunk);
			//FLush remaining bytes, basically does nothing
			outputDataStream.flush();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private synchronized void closeConn() {
		try {
			//close streams and threads
			outputDataStream.close();			
			threadSocket.close();
			System.out.println("[PEER SERVER DISCONNECTED]");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

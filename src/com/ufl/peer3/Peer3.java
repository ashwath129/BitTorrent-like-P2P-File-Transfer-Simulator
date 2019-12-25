package com.ufl.peer3;

import com.ufl.util.PeerThread;

import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Peer3 {

	//server socket for the peer which acts as a server to its neighbor peer
	ServerSocket socket_peerServer;
	//General socket connector
	Socket socket_connector;
	//peer socket for the peer that acts as client
	Socket socket_peerClient;
	//Data input stream for getting input file bytes through socket
	ObjectInputStream dataInputStream;
	//Data input stream for sending input file bytes through socket
	ObjectOutputStream dataOutputStream;
	//Set of chunklists
	Set<Integer> chunks_Set;
	static String root = Peer3.class.getResource(".").getPath();
	static String baseLocation = root + "/PeerFiles";
	static String chunksLocation = baseLocation + "/chunks";
	//original server port
	public static int server_ConnectPort;
	//Peer port for this peer, that is , peer which acts as a server
	public static int peer_ServerPort;
	//Peer port for its respective neighbor that is given as input
	public static int peer_NeighborPort;

	public static void main(String[] args) {
		//Accept arguments
		server_ConnectPort = Integer.parseInt(args[0]);
		peer_ServerPort = Integer.parseInt(args[1]);
		peer_NeighborPort = Integer.parseInt(args[2]);

		Peer3 c = new Peer3();
		new File(baseLocation + "/chunks/").mkdirs();
		String peerC = null;
		//Total peer file chunks to be got
		int chunkFiles_total;
		try {

			//We connect the peer to server
			c.peer_NeighborConnect(c.server_ConnectPort);
//			peerC = (String) c.dataInputStream.readObject();
			//Get all chunks from the server
			chunkFiles_total = (int) c.dataInputStream.readObject();

			c.chunks_Set = Collections.synchronizedSet(new LinkedHashSet<Integer>());
			for (int i = 1; i <= chunkFiles_total; i++)
				//Add the chunkfiles to the chunk set
				c.chunks_Set.add(i);

			//Now we get all the chunks from the server for adding it to the peer
			int receiveChunks = (int) c.dataInputStream.readObject();
			while (receiveChunks > 0) {
				//receive chunk and create a chunk file object
				File peerFile_chunk = c.getChunks();
				if (peerFile_chunk != null)
					// to add to the peer chunk folder
					c.generateChunks(chunksLocation, peerFile_chunk);
				else
					System.out.println("No partitions received");
				receiveChunks--;
			}
			c.disconnect_Peer();


			//Create thread to keep accepting connections as a server until all the chunks have been received
			Thread thread =new Thread(new Runnable(){
				public void run(){
					//Connection for peer that acts as server
					c.peer_ServerConn(c.peer_ServerPort);

				}});
			thread.start();

			//Connect to the neighbor peer
			c.peer_NeighborConnect(c.peer_NeighborPort);


			System.out.println("Peer 3 , [CHUNKS_TOTAL] - " + chunkFiles_total);
			while(true)
			{
				System.out.println("Peer 3 , [CHUNKS TO BE RECEIVED] - "+c.chunks_Set);
				if(!c.chunks_Set.isEmpty())
				{
					//ChunkList Size
					int chunkList_size = c.chunks_Set.size();
					Integer[] chunkArray = c.chunks_Set.toArray(new Integer[chunkList_size]);
					for (int i=0; i<chunkArray.length; i++)
					{
						//Send the chunklist of objects to be received from the server or neighbor
						c.dataOutputStream.writeObject(chunkArray[i]);
						c.dataOutputStream.flush();
						//Keep receiving chunk files and write to the chunk folder of the peer
						File peerFile_chunk = c.getChunks();
						if (peerFile_chunk != null)
							c.generateChunks(chunksLocation, peerFile_chunk);
					}
				}
				else
				{	//all list of chunks have been received so send back messages
					c.dataOutputStream.writeObject(-1);
					c.dataOutputStream.flush();
					break;
				}
				Thread.sleep(2000);
			}
			c.disconnect_Peer();
			//Function call to create and accumulate full file
			c.generateCompleteFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	//Method to accept connections from the peer as server,this peer acts as a server for its neighbor
	public void peer_ServerConn(int port) {
		try {

			socket_peerServer = new ServerSocket(port);
			System.out.println(this.getClass().getName() + " Peer 3 [ACCEPT]");
			int nc = 1; //WE keep the neighbor count to 1 for now
			while (true) {
				if (nc>0) {
					nc--;
					socket_connector = socket_peerServer.accept();
					//Accept a socket connection on listening
					System.out.println("[ACCEPTED] " + peer_NeighborPort);
					//Create a server thread to keep listening and handling multiple clients to receive and send file chunks
					PeerThread pt = new PeerThread(socket_connector, chunksLocation);
					pt.start();
				} else {
					System.out.println("[FINISH]");
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Method to connect to peer neighbor
	public void peer_NeighborConnect(int port) throws InterruptedException {
		boolean flag=true;
		//connect via localhost
		String localhostPort = "127.0.0.1";
		while(flag)
		{
			try {
				flag=false;
				socket_peerClient = new Socket(localhostPort, port);
				System.out.println("[CONNECTED] to" + server_ConnectPort);
				//Repsective input and output streams for the peers to send and receive chunks
				dataInputStream = new ObjectInputStream(socket_peerClient.getInputStream());
				dataOutputStream = new ObjectOutputStream(socket_peerClient.getOutputStream());
			}
			catch(ConnectException e)
			{
				System.out.println("Listening for connection at "+port);
				Thread.sleep(5000);
				flag=true;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void disconnect_Peer() {
		try {
			//Close peer connections and input streams
			dataInputStream.close();
			socket_peerClient.close();
			System.out.println("[DISCONNECT]" + peer_ServerPort);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Get chunkfile objectof chunkfileobject class type
	public File getChunks() {
		File chunk = null;
		try {
			//Get chunk from server
			chunk = (File) dataInputStream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return chunk;
	}


	public synchronized void generateChunks(String chunksLocation, File peerFile_chunk) {
		try {
			System.out.println("[RECEIVED] Chunk [" + peerFile_chunk.getName() + "] from " + socket_peerClient.getPort());
			//Get the chunks , add it to the chunk location of the peer as a file
			byte[] chunk = new byte[102400]; // should be 100kb, see demo
			FileInputStream fileInStream = new FileInputStream(peerFile_chunk);
			BufferedInputStream bufferInStream = new BufferedInputStream(fileInStream);
			int bytesRead = bufferInStream.read(chunk);

			FileOutputStream filedataOutputStream = new FileOutputStream(new File(chunksLocation, peerFile_chunk.getName()));
			//In a buffered stream write the chunk file data and chunk size for info to be passed to other peers
			BufferedOutputStream bufferdataOutputStream = new BufferedOutputStream(filedataOutputStream);
			bufferdataOutputStream.write(chunk, 0, bytesRead);
			//We keep modifying chunks as it keeps getting downloaded for each peer
			chunks_Set.remove(Integer.valueOf(peerFile_chunk.getName()));
			//Flush out remaining bytes,basically does nothing
			bufferdataOutputStream.flush();
			bufferdataOutputStream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void generateCompleteFile() {

		//List the files
		String chunksLocation = baseLocation + "/chunks";
		File[] files = new File(chunksLocation).listFiles();
		//Each chunk size is 100KB
		byte[] chunk = new byte[102400];
		//Final file is kept here
		new File(baseLocation + "/final/").mkdirs();
		try {
			//create output stream to write file
			FileOutputStream filedataOutputStream = new FileOutputStream(
					new File(baseLocation + "/final/" + "test.pdf"));
			//create buffered output data stream to read the input stream of the file in bytes
			BufferedOutputStream bufferdataOutputStream = new BufferedOutputStream(filedataOutputStream);

			Arrays.sort(files, Comparator.comparing(File::getName));
			for (File f : files) {
//                System.out.println(f.getName());
				FileInputStream fis = new FileInputStream(f);
				byte[] fileBytes = new byte[(int) f.length()];
				int bytesRead = fis.read(fileBytes, 0,(int)  f.length());
				assert(bytesRead == fileBytes.length);
				assert(bytesRead == (int) f.length());
				filedataOutputStream.write(fileBytes);
				filedataOutputStream.flush();
				fis.close();
			}
			filedataOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

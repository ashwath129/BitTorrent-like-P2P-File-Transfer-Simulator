# BitTorrent-like-P2P-File-Transfer-Simulator
This is a peer to peer network implementation for file transfer among peers and a file owner/server.

Instructions on how to run 
----------------------------
1) Extract contents of the zip folder in your desired location

2) Make sure there are 7 folders in src/com/ufl/ - one for each Peer , one for Server/File Owner , Utility folder for the Peer Thread

3) Open cmd/terminal in the src folder, run "javac com/ufl/server/Server.java","javac com/ufl/util/PeerThread.java"

4) Open 5 cmds/terminals in the src folder, run "javac com/ufl/peer1/Peer1.java","javac com/ufl/peer2/Peer2.java","javac com/ufl/peer3/Peer3.java","javac com/ufl/peer4/Peer4.java","javac com/ufl/peer5/Peer5.java"
   respectively
   
5) Start the fileOwner/Server by going to the cmd in step 3 and running "java com/ufl/server/Server 8000

6) Now in the terminals/cmds opened in step 4, run the commands "java com/ufl/peer1/Peer1 8000 8001 8002", "java com/ufl/peer2/Peer2 8000 8002 8003", "java com/ufl/peer3/Peer3 8000 8003 8004", "java com/ufl/peer4/Peer4 8000 8004 8005",
   "java com/ufl/peer5/Peer5 8000 8005 8001" respectively in each of the folders. Here the first argument is the File Owner's port , the second argument is the port of the Peer(that will act as the server to it's neighbor), and the
   third argument is port of the neighbor peer
   
7) The program in each of the peer terminals get disconnected after every peer has every chunk. The chunks can be found in each of the peer's folder and inside PeerFiles/chunks.
   The complete file after the chunks have been compiled can be found in PeerFiles/chunks/final/. The server also contains the chunks in the chunks folders and has the main file as well.

NOTE: The name for the test file is "test.pdf" and for now is harcoded in the server as well as peer files. 
   
Basic Implementation Specifics
--------------------------------
This is a peer to peer network implementation for file transfer among peers and a file owner/server. In our project we have 5 peers and each peer is connected to its next peer(in numerical
order) and the last peer is connected to the first peer , forming a circular type topology (Peer 1's neighbor is 2, 2's neighbor is 3....,5's neighbor is 1)

The main server contains the file to be distributed among the peers(test.pdf). The server is started and breaks the files into chunks (each chunks of size 100KB), and distributes the chunks
initially to the peers (every peer gets 8 chunks for say, a total of 40 chunks, in a sequential manner). The server accepts connections and runs multiple threads to run multiple clients concurrently.

Each peer is started and connects to the main file owner first and gets the initial sets of chunks. It runs two threads of control , one where it acts as a server to accept connections
from its neighbors and one in which gets chunks from the file owner/neighbor.

The chunk files are written in each of the Peer folders, the server folder, and the compiled full file as well that can be checked for consistency. (See instructions to see where they are)

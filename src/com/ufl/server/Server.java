package com.ufl.server;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

public class Server {
    private static final int CHUNK_SIZE = 102400;
    private static final int NUM_PEERS = 5;
    private static final String FILE_PATH = "com/ufl/server/";
    private static final String FILE_NAME = "test.pdf";

    private File[] getChunksForFile () throws IOException {
        String rootDir = Paths.get(".").toAbsolutePath().normalize().toString() + "/";
        File file = new File(rootDir + FILE_PATH + FILE_NAME);
        String chunksDirPath = rootDir + FILE_PATH + "chunks/";
        File chunksDir = new File(chunksDirPath);
        byte[] chunk = new byte[CHUNK_SIZE];

        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        assert chunksDir.mkdir();
        int read;
        int chunkId = 0;
        while ((read = bis.read(chunk)) > 0) {
            String fileName = ++chunkId < 10 ? "0" + chunkId : String.valueOf(chunkId);
            BufferedOutputStream bufferOutStream = new BufferedOutputStream(new FileOutputStream(
                    new File(chunksDirPath, fileName)
            ));
            bufferOutStream.write(chunk, 0, read);
            bufferOutStream.close();
        }
        bis.close();

        return new File(chunksDirPath).listFiles();
    }

    public static void main (String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        int peerNum = 0;
        Server server = new Server();

        try (ServerSocket listener = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            File[] chunks = server.getChunksForFile();
            Arrays.sort(chunks, Comparator.comparing(File::getName));

            //noinspection InfiniteLoopStatement
            while (true) {
                peerNum++;
                new PeerConnectionHandler(listener, peerNum, chunks).start();
                System.out.println("Client " + peerNum + " is connected!");
            }
        }


    }


}

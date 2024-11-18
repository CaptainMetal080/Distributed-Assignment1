/**
 * @author Qusay H. Mahmoud
 */

import javax.sound.midi.Track;
import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.List;

public class MusicServer {
    static int ClientCount=0;
    static int MaxConnection=2;

    public static void main(String[] args) throws Exception {

            ServerSocket serverSocket = new ServerSocket(3500);
            System.out.println("Server Listening on port 3500 :) ....");
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Client connected: " + client.getPort());
                new Thread(() -> {
                    ClientCount++;
                    MusicPLayer musicPLayer=new MusicPLayer();
                    try {
                        DataInputStream in = new DataInputStream(client.getInputStream());
                        DataOutputStream out = new DataOutputStream(client.getOutputStream());
                        PrintWriter output = new PrintWriter(client.getOutputStream(), true);

                        while(isAllowed()) {
                            String command=in.readUTF();
                            if(command.equals("play")) {
                                out.writeUTF(Tracklist(System.getProperty("user.dir") + "\\Tracks\\"));
                                String track = in.readUTF();
                                playSong(track, out, output);
                            }
                            if(command.equals("download")){
                                out.writeUTF(Tracklist(System.getProperty("user.dir") + "\\Tracks\\"));
                                String filename = in.readUTF();
                                String trackpath = System.getProperty("user.dir") + "\\Tracks\\" + filename + ".Wav";
                                downloadSong(trackpath, out);
                            }

                        }
                        output.println("Disconnected server is full\n");

                    } catch (IOException e) {
                        System.err.println("Error handling client: " + e.getMessage());
                    } finally {
                        ClientCount--;
                        try {
                            client.close();
                            System.out.println("Client disconnected: " + client.getPort());
                        } catch (IOException e) {
                            System.err.println("Error closing client socket: " + e.getMessage());
                        }
                    }
                }).start();
            }
    }

    public static boolean isAllowed(){
        return ClientCount <= MaxConnection;
    }
    public static void downloadSong(String filePath, DataOutputStream out) {
        File file = new File(filePath);
        try {
            if (file.exists() && file.isFile()) {
                out.writeUTF(file.getName());
                out.writeLong(file.length());

                try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file))) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                System.out.println("File sent: " + file.getName());
            } else {
                out.writeUTF("File not found");
                out.writeLong(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void playSong(String track, DataOutputStream out, PrintWriter output) {
        try {
            String trackpath=System.getProperty("user.dir")+"\\Tracks\\"+track+".Wav";
            // The Tracks directory
            System.out.println(trackpath);
            File songTrack = new File(trackpath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(songTrack);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            // Start playing the clip
            output.println("Playing track: "+track);
            clip.start();
            long totalMicroseconds = clip.getMicrosecondLength();
            int totalSeconds = (int) (totalMicroseconds / 1_000_000);
            out.writeUTF("START");

            // Displays progress for the music track
            while (clip.isActive()) {
                long elapsedMicroseconds = clip.getMicrosecondPosition();
                int elapsedSeconds = (int) (elapsedMicroseconds / 1_000_000);
                String elapsedTime = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60);
                String totalTime = String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);
                String progress = String.format("Progress: " + elapsedTime + " / " + totalTime);
                output.println(progress);
                Thread.sleep(100); // Update every 1/10th second
            }
            clip.drain();
            System.out.println("\nPlayback finished.");
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
            e.printStackTrace();
        }
    }
        public static String Tracklist(String directoryPath) {
            File directory = new File(directoryPath);

            // to avoid errors checking if the directory exists and is a directory
            if (directory.exists() && directory.isDirectory()) {
                // List all .wav files
                File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));

                //now look through the directory and append the file names in a list
                if (files != null && files.length > 0) {
                    StringBuilder Tracklist= new StringBuilder("----------\n");
                    for (File file : files) {
                        Tracklist.append(file.getName()).append("\n");
                    }
                    Tracklist.append("-----------");
                    return Tracklist.toString();
                } else {
                    return ("No WAV files found in " + directoryPath);
                }
            }
            return "Directory failed";
        }

}





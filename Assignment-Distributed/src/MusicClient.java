/**
 * @author Qusay H. Mahmoud
 */

import java.io.*;
import java.net.*;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

public class MusicClient {

  public static void main(String argv[]) throws Exception {
    Socket echo;
    BufferedReader In;
    DataOutputStream dos;
    Scanner input=new Scanner(System.in);

    echo = new Socket("localhost", 3500);
    In = new BufferedReader(new InputStreamReader(echo.getInputStream()));
    DataInputStream dis = new DataInputStream(echo.getInputStream());
    dos = new DataOutputStream(echo.getOutputStream());
while(true) {
  System.out.println("What do you wish to do:\n Download, Play");
  String req = input.next();
  String action = req.toLowerCase();

  if (action.equals("play")) {
    System.out.println("Choose a track from the tracklist to play:");
    dos.writeUTF("play");
    System.out.println(dis.readUTF());
    String track = input.next();
    dos.writeUTF(track);
    dos.flush();
    System.out.println("Awaiting server response...");
    System.out.println(In.readLine());
    String response = dis.readUTF();
    if ("START".equals(response)) {
      String progress;
      // Continuously read progress updates
      while ((progress = In.readLine()) != null) {
            System.out.print("\r" + progress); // Overwrite the current line
            System.out.flush(); // Ensure the output is flushed
          }
          System.out.println(); // Move to the next line after completion
        }
      }
  else if (action.equals("download")) {
    System.out.println("Choose a track from the tracklist to download:");
    dos.writeUTF("download");
    System.out.println(dis.readUTF());
    String file = input.next();
    dos.writeUTF(file);
    dos.flush();

    String filename = dis.readUTF();
    long fileSize = dis.readLong();

    if (fileSize > 0) {
      try (FileOutputStream fos = new FileOutputStream("download_" + filename)) {
        byte[] buffer = new byte[4096];
        long totalRead = 0;
        int read;
        while (totalRead < fileSize && (read = dis.read(buffer)) != -1) {
          fos.write(buffer, 0, read);
          totalRead += read;
        }
        System.out.println("File downloaded successfully as " + "downloaded_" + filename);
      } catch (IOException e) {
        e.printStackTrace();
      }

    } else {
      System.out.println("File not found on the server.");
    }
  }
  else{
    System.out.println("Wrong Command please retry\n");
  }
    }
  }
}

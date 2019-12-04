import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

/*
 * need to change the IP and port of edge server in line 64
 * */

public class Client {//Request for file, send filename and receive file from edge server

    private Socket s;

    public Client(String host, int port, String fileName) {
        try {
            s = new Socket(host, port);//set connection based on host add. and port
            requestFile(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void requestFile(String fileName) throws IOException {
    	//send the filename and receive the file
        DataInputStream in = new DataInputStream(s.getInputStream());
        DataOutputStream out = new DataOutputStream(s.getOutputStream());
        
        //calculate time
        long startTime = System.currentTimeMillis();
        
        //send file name
        System.out.println("======== sending the file name: " + fileName + " ========");
        out.writeUTF(fileName);
        
    	//receive file
        FileOutputStream fos = new FileOutputStream(fileName);
        System.out.println("======== waiting to receive the file ========");
        byte[] buffer = new byte[409600];
        long filesize = in.readLong(); // receive file size 
        int read = 0;
        int totalRead = 0;
        int remaining = (int) filesize;
        while((read = in.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
            totalRead += read;
            remaining -= read;
            System.out.println("read " + totalRead + " bytes.");
            fos.write(buffer, 0, read);//write the file
        }
        System.out.println("======== file received ========");

        //calculate time
        long endTime = System.currentTimeMillis();
        System.out.println("======== Time used: " + (endTime-startTime) + " ms ========");
        fos.close();
        out.close();
        in.close();
        s.close();
    }
    
    public static void main(String[] args) {
    	//request for file to edge server with the IP, port and file name
        //Client f1 = new Client("172.24.50.174", 1988, "file1.png");
        //Client f2 = new Client("172.24.50.174", 1988, "file2.png");
        Client f3 = new Client("172.24.50.174", 1988, "file3.png");
        Client f4 = new Client("172.24.50.174", 1988, "file4.png");
        //Client f5 = new Client("172.24.50.174", 1988, "file5.png");
    }

}
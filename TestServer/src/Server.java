import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class Server extends Thread {

    private ServerSocket ss;

    public Server(int port) {//create the server socket
        try {
            ss = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void run() {
    	while(true) {
			try {
				Socket ssSock = ss.accept();//receive a new request
	            sendFile(ssSock);
			} catch (IOException e) {
				e.printStackTrace();
			}
    		
    	}
    }
    
    public void sendFile(Socket ssSock) throws IOException {
    	//receive filename and send file to requester
        DataInputStream in = new DataInputStream(ssSock.getInputStream());
        DataOutputStream out = new DataOutputStream(ssSock.getOutputStream());
        
        //get the name of the file that the requester want
        String fileName = in.readUTF();
        
        //wait for 1 seconds to emulate remote delay
        try {
			TimeUnit.MILLISECONDS.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
        //assuming the file exist in the server, send the file to requester
        //send the length of the file
        File file = new File(fileName);
        out.writeLong(file.length());
        //send the content of the file
        FileInputStream fis = new FileInputStream(fileName);
        System.out.println("======== start sending "+ fileName + " to the requestor ========");
        byte[] buffer = new byte[40000];
        while (fis.read(buffer) > 0) {
            out.write(buffer);
        }
        System.out.println("======== "+ fileName + " sended ========");
        
        fis.close();
        in.close();
        out.close();
    }
    
    public static void main(String[] args) {
        Server fs = new Server(1989);
        fs.start();
    }

}
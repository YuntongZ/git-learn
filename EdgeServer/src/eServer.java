import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/* need to change the remote server's IP address and port in line 57
 * need to change the local port in line 169
 * */
public class eServer extends Thread {
	
    private ServerSocket ss;
    private int total = 3;//the total file number that we can cache
    private String clock[][] = new String[2][total];//first row is name of file, second is the flag to indicate whether this file used or not
    
    public eServer(int localPort) {//create the server socket, receive request from client
        try {
            ss = new ServerSocket(localPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                Socket ssSock = ss.accept();//receive a client request
                sendFile(ssSock);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void sendFile(Socket ssSock) throws IOException {
    	//receive file name and send send file to client. if there is no such file in local, download it from remote server
        DataInputStream inss = new DataInputStream(ssSock.getInputStream());
        DataOutputStream outss = new DataOutputStream(ssSock.getOutputStream());
        
        //receive file name
        String fileName = inss.readUTF();
        System.out.println("======== file name received: " + fileName + " ========");
        
        //decide whether request the file to remote server
        File file = new File(fileName);
        long filesize = 0;
        int flagFileExist = 1;
        
        if(file.exists() == false) {//need to download it from remote server
        	flagFileExist = 0;
            Socket s = null;
            /*need to change the IP address and port here*/
        	try {
    			s = new Socket("10.26.6.73", 1989);//using the address and port of remote server to connect with it
        		
        	}catch (IOException e) {
                e.printStackTrace();
            }

        	//need to request this file to remote server
            DataInputStream ins = new DataInputStream(s.getInputStream());
            DataOutputStream outs = new DataOutputStream(s.getOutputStream());
            
        	//first send filename, then receive file 
            //send file name to the remote server
            System.out.println("======== sending the file name to the remote server: " + fileName + " ========");
            outs.writeUTF(fileName);
            
        	//receive file from the remote server
            FileOutputStream fos = new FileOutputStream(fileName);
            System.out.println("======== waiting to receive the file from the remote server ========");
            byte[] buffer = new byte[409600];
            filesize = ins.readLong(); // receive file size 
            int read = 0;
            int totalRead = 0;
            int remaining = (int) filesize;
            while((read = ins.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                totalRead += read;
                remaining -= read;
                System.out.println("read " + totalRead + " bytes.");
                fos.write(buffer, 0, read);
            }
            System.out.println("======== file received ========");
            fos.close();
            outs.close();
            ins.close();
            s.close();
        }
        //using clock algorithm to cache files
        UpdateCache(fileName, flagFileExist);
        
        //now it is sure that the file exists in the server, send the file to client
        //send the size of the file
        if (filesize == 0){//there exist the file and parameter filesize is not changed
            filesize = file.length();
        }
        
        //send the file size to client
        outss.writeLong(filesize);
        
        //send the content of the file to client
        FileInputStream fis = new FileInputStream(fileName);
        System.out.println("======== start sending the file to the client ========");
        byte[] buffer = new byte[40000];
        while (fis.read(buffer) > 0) {
            outss.write(buffer);
        }
        System.out.println("======== file sended ========");
        fis.close();
        inss.close();
        outss.close();
    }
    

	private void UpdateCache(String file, int flagFileExist) {
    	//using clock algorithm to cache
    	//if the file exists in the cache, mark it to indicate this file is read
    	if (flagFileExist == 1) {
    		//the file is in local, make the clock to 1
    		for (int i = 0; i < total; i++) {
    			if (clock[0][i].contentEquals(file)) {
    				clock[1][i]="1";
    			}
    		}
    	}
    	else {//the file is download from remote server
    		//need to put the file in clock array
    		int j = 0;
    		for (; j < total; j++) {
    			if (clock[0][j] == null) {//there is enough room for the file, put the file into data structure
    				clock[0][j]=file;
    				clock[1][j]="1";
    				break;
    			}
    		}
    		if (j==total) {
    			//there is no room, need to replace one file in clock
    			int r = 0;
        		for (; r < total; r++) {
        			if (clock[1][r] == "0") {//replace with this 
        				clock[0][r]=file;
        				clock[1][r]="1";
        				break;
        			}
        			else {//change it to 0
        				clock[1][r]="0";
        			}
        		}
        		if (r==total) {
        			//the first iteration cannot find a file to replace, then it must replace with the first file because it is marked 0
    				clock[0][0]=file;
    				clock[1][0]="1";
        		}
    		}
    	}
    	//print the content of clock array
    	for (int s=0; s<total; s++) {
    		if (clock[0][s] != null) {
                System.out.println("== file: " + clock[0][s] + " == clock: " + clock[1][s] + " ==");
    		}
    	}
	}
	
	public static void main(String[] args) {
		/*need to change the local port here*/
        eServer fs = new eServer(1988);
        fs.start();
    }
}
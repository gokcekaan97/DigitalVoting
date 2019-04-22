package project_VOTE;

import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
public class VotingServerThread implements Runnable{
    private Socket socket;
    private final static Lock lock = new ReentrantLock(); 
    public VotingServerThread(Socket socket) {
        this.socket=socket;
    }
    public void run() {
        lock.lock();
            try {
            	System.out.println("tatata");
            }
         finally {
                     lock.unlock();
                 }
    }
}
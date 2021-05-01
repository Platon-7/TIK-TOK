package myAPP;

import java.io.IOException;
import java.util.Scanner;

public class ConsumerThread extends Thread {
    AppNode app;
    public ConsumerThread(AppNode app){
        this.app=app;

    }
    public void run() {
        app.connect();
        Message answer;
        Scanner in = new Scanner(System.in);
        do {
            System.out.println("Please enter video search: ");
            String key = in.nextLine();
            System.out.println(key);
            Message request=new Message(app.channelName.channelName,key,0,null);
            try {
                app.out.writeObject(request);
                app.out.flush();
                System.out.println("SENT key");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                answer = (Message) app.in.readObject();
                if(answer.getChunks()==0){
                    app.push(answer.getKey(),null);
                }
                if(answer.getChunks()==1){
                    app.playData(answer.getKey(), null);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }while(!in.nextLine().equals("end"));
    }
}

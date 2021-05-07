package myAPP;

import java.io.IOException;
import java.net.Socket;

public class PublisherThread extends Thread {
    AppNode app;
    Socket requestSocket = null;
    public PublisherThread(AppNode app){
        this.app=app;
        app.init();
        app.connect();
    }

    public void run() {
        try {
            Message key;
            for(int i=0;i<app.channelName.hashtagsPublished.size();i++){
                key=new Message(app.channelName.channelName,app.channelName.hashtagsPublished.get(i),1,null);
                if(i==(app.channelName.hashtagsPublished.size()-1)){
                    key.setChunks(0);
                }
                app.sem.acquire();
                app.out.writeObject(key);
                app.out.flush();
                System.out.println("SENT hashtag");
                app.sem.release();
            }
            app.sem.acquire();
            key =(Message) app.in.readObject();
            app.sem.release();
            System.out.println("RECEIVED KEY" + key.getKey());
            app.push(key.getKey(),null);
            app.playData(null,null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

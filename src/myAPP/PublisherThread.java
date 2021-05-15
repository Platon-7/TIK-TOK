package myAPP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PublisherThread extends Thread {
    AppNode app;
    Socket client;
    ObjectInputStream in;
    ObjectOutputStream out;
    public PublisherThread(AppNode app,  Socket client){
        this.app=app;
        this.client=client;
        try {
            out = new ObjectOutputStream(client.getOutputStream());
            in = new ObjectInputStream(client.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            Message msg;
            msg = (Message) in.readObject();
            if (msg.channelName.equals("Server")) {
                int counter = app.channelName.hashtagsPublished.size();
                for (int i = 0; i < app.channelName.hashtagsPublished.size(); i++) {
                    if (counter == 0) {
                        msg = new Message(app.channelName.channelName, null, "-1", null);
                        out.writeObject(msg);
                        out.flush();
                    }
                    counter--;
                    msg = new Message(app.channelName.channelName, app.channelName.hashtagsPublished.get(i), String.valueOf(counter), null);
                    out.writeObject(msg);
                    out.flush();
                }
            } else {
                msg = new Message(app.channelName.channelName, null, null, null);
                out.writeObject(msg);
                out.flush();
            }
            while (true) {
                msg = (Message) in.readObject();
                System.out.println("RECEIVED KEY" + msg.getKey());
                app.push(msg.getKey(), null);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

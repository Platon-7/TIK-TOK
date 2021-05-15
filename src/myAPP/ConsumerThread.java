package myAPP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ConsumerThread extends Thread {
    AppNode app;
    Socket client;
    ObjectOutputStream out;
    ObjectInputStream in;
    public ConsumerThread(AppNode app,Socket client){
        this.app=app;
        this.client=client;
        try {
            out = new ObjectOutputStream(client.getOutputStream());
            in = new ObjectInputStream(client.getInputStream());
            Message info=new Message(app.channelName.channelName, String.valueOf(app.PORT),"PublisherInfo",null);
            out.writeObject(info);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void run() {
        int ans;

        Scanner input = new Scanner(System.in);
        do {
            System.out.println("To take an action type in the associated number");
            System.out.println("What would you like to do: ");
            System.out.println("1.Search for videos...");
            System.out.println("2.Add new videos...");
            ans = input.nextInt();
            Message request;
            if (ans == 1){
                System.out.println("Please enter your desired search item: ");
            String key = input.nextLine();
            System.out.println(key);
            request = new Message(app.channelName.channelName, key, "Consumer", null);
            try {
                out.writeObject(request);
                out.flush();
                System.out.println("SENT key");
                request = (Message) in.readObject();
                if (request.getFlag().equals("Redirect")) {
                    System.out.println("Got redirected to " + request.channelName + "in port" + request.getKey());
                    in.close();
                    out.close();
                    client = new Socket(request.channelName, Integer.parseInt(request.getKey()));
                    out = new ObjectOutputStream(client.getOutputStream());
                    in = new ObjectInputStream(client.getInputStream());
                    request = new Message(app.channelName.channelName, key, "Redirect", null);
                    out.writeObject(request);
                    out.flush();
                    System.out.println("SENT REDIRECTED REQUEST");
                    request = (Message) in.readObject();
                }
                if (request.getFlag().equals("No results")) {
                    System.out.println("No video results for search item: " + request.getKey());
                } else {
                    app.playData(request.getFlag(), null);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }else if(ans==2)
            {
                ArrayList<String> update=app.updateNodes();
                try {
                in.close();
                out.close();
                client = new Socket("127.0.0.1",4321);
                out = new ObjectOutputStream(client.getOutputStream());
                in = new ObjectInputStream(client.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
                if(update.isEmpty()){
                    System.out.println("No new videos found...");
                }else{
                    try {
                        request = new Message(app.channelName.channelName,null , "new Hashtags", null);
                        out.writeObject(request);
                        out.flush();
                        int counter=update.size()-1;
                        for(int i=0;i<update.size();i++){
                            request = new Message(app.channelName.channelName,update.get(i) , String.valueOf(counter), null);
                            out.writeObject(request);
                            out.flush();
                            counter--;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
            System.out.println("Do you want to end your navigation? yes/no");
        }while(!input.nextLine().equals("yes"));
        try {
            in.close();
            out.close();
            client.close();
            for (int i = 0; i < app.brokers.size(); i++) {
                app.brokers.get(i).in.close();
                app.brokers.get(i).out.close();
                app.brokers.get(i).client.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

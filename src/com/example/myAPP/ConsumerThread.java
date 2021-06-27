package com.example.myAPP;

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
    String serverIp="";
    String serverPort="";
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
            System.out.println("3.Add hashtags to existing video...");
            System.out.println("4.Delete existing video from the app...");
            System.out.println("5.Delete a hashtag from an existing video in the app...");
            ans = Integer.parseInt(input.nextLine());
            Message request;
            if (ans == 1) {
                System.out.println("Please enter your desired search item: ");
                String key = input.nextLine();
                System.out.println(key);
                request = new Message(app.channelName.channelName, key, "Consumer", null);
                try {
                    out.writeObject(request);
                    out.flush();
                    request = (Message) in.readObject();
                    if (request.getFlag().equals("Redirect")) {
                        serverIp=request.channelName;
                        serverPort=request.getKey();
                        in.close();
                        out.close();
                        client = new Socket(request.channelName, Integer.parseInt(request.getKey()));
                        out = new ObjectOutputStream(client.getOutputStream());
                        in = new ObjectInputStream(client.getInputStream());
                        request = new Message(app.channelName.channelName, key, "Consumer", null);
                        out.writeObject(request);
                        out.flush();
                        request = (Message) in.readObject();
                    }
                    if (request.getFlag().equals("No results")) {
                        System.out.println("No video results for search item: " + request.getKey());
                    } else {
                        app.playData(request.getFlag(), null);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    try {
                        in.close();
                        out.close();
                        client.close();
                        return;
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            } else
                if (ans == 2) {
                    ArrayList<String> update = app.updateNodes();

                    if (update.isEmpty()) {
                        System.out.println("New videos do not exist or they have no hashtags...");
                    } else {
                        try {
                            try {
                                if (!serverPort.isEmpty()) {
                                    if(!serverPort.equals("4321")) {
                                        in.close();
                                        out.close();
                                        client = new Socket("127.0.0.1", 4321);
                                        out = new ObjectOutputStream(client.getOutputStream());
                                        in = new ObjectInputStream(client.getInputStream());
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            request = new Message(app.channelName.channelName, null, "new Videos", null);
                            out.writeObject(request);
                            out.flush();
                            int counter = update.size() - 1;
                            for (int i = 0; i < update.size(); i++) {
                                request = new Message(app.channelName.channelName, update.get(i), String.valueOf(counter), null);
                                out.writeObject(request);
                                out.flush();
                                counter--;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                } else if (ans == 3) {
                    do {
                        System.out.println("Enter the name of the video you want to add hashtags to: ");
                        String name = input.nextLine();
                        if (!app.namesVideos.contains(name)) {
                            System.out.println("The video with the name " + name + " does not exist in the app...");
                            System.out.println("Make sure you have uploaded the video and try again...");
                            break;
                        }
                        do {
                            System.out.println("Enter the hashtag you with to add: ");
                            String hashtag = input.nextLine();
                            if (app.addHashTag(name, hashtag)) {
                                if (!serverPort.isEmpty()) {
                                    if (!serverPort.equals("4321")) {
                                        try {
                                            in.close();
                                            out.close();
                                            client = new Socket("127.0.0.1", 4321);
                                            out = new ObjectOutputStream(client.getOutputStream());
                                            in = new ObjectInputStream(client.getInputStream());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                request = new Message(app.channelName.channelName, hashtag, "new hashtag", null);
                                try {
                                    out.writeObject(request);
                                    out.flush();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                System.out.println("Hashtag: " + hashtag + " already exists for video: " + name);
                            }
                            System.out.println("Do you wish to add another hashtag to this video? yes/no");
                        } while (input.nextLine().equals("yes"));
                        System.out.println("Do you wish to add another hashtag to another video? yes/no");
                    } while (input.nextLine().equals("yes"));
                } else if (ans == 4) {
                    if (!app.published.isEmpty()) {
                        System.out.println("Enter the name of the video you want to delete from the app: ");
                        String name = input.nextLine();
                        app.deleteVideo(name);
                    } else {
                        System.out.println("You have no videos to delete from the app...");
                    }

                } else if (ans == 5) {
                    if (!app.published.isEmpty()) {
                        if (!(app.channelName.userVideoFilesMap.size() == 1)) {
                            System.out.println("Do you wish to remove a hashtag from all your videos? yes/no");
                            if (input.nextLine().equals("yes")) {
                                System.out.println("Enter the the hashtag you want removed: ");
                                String hashtag = input.nextLine();
                                app.removeHashTag(hashtag);
                            } else {
                                System.out.println("Enter the name of the video you want to delete a hashtag from: ");
                                String name = input.nextLine();
                                System.out.println("Enter the hashtag you want to delete hashtag from: " + name);
                                String hashtag = input.nextLine();
                                app.removeHashTagFromVideo(name, hashtag);
                            }

                        } else {
                            System.out.println("You have no videos with hashtags...");
                        }

                    } else {
                        System.out.println("You have no videos to delete hashtags from...");
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

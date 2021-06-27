package com.example.myAPP;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.IOUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AppNode implements Publisher,Consumer {
    Socket requestSocket = null;
    ServerSocket providerSocket;
    Socket connection = null;
    ChannelName channelName;
    ArrayList<Value> published = new ArrayList<>();
    Message answer;
    ConsumerThread cons;
    PublisherThread pub;
    List<PublisherThread> brokers=new ArrayList<>();
    int PORT;
    String videoLocation;
    ArrayList<String> namesVideos=new ArrayList<>();

    int maxBufferSize = 512 * 1024; //512kb
    public AppNode(String PORT,String videoLocation) {
        this.PORT = Integer.parseInt(PORT);
        this.videoLocation = videoLocation;
        Scanner in = new Scanner(System.in);
        System.out.println("Enter your unique channelName please: ");
        channelName = new ChannelName(in.nextLine());
        init();
        connect();
    }

    @Override
    public void register(Broker broker, String channelName) {

    }

    @Override
    public void disconnect(Broker broker, String channelName) {

    }

    @Override
    public void playData(String data, Value value) {
        int counter=Integer.parseInt(data);
        System.out.println("Got "+counter+ " video(s)");
        do {
            int length = 0;
            List<byte[]> chunks = new ArrayList<>();
                    do {
                        try {
                            answer = (Message) cons.in.readObject();
                            chunks.add(answer.getData().videoFile.getVideoFileChunk());
                            length += answer.getData().videoFile.getVideoFileChunk().length;

                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    } while (Integer.parseInt(answer.getFlag()) > 0);

            VideoFile temp = answer.getData().videoFile;
            length += chunks.get(chunks.size() - 1).length;
            File mp4 = new File("prodVideo/" + answer.getData().getVideoFile().videoName + ".mp4");
            OutputStream os = null;
            try {
                os = new FileOutputStream(mp4);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                byte[] after = new byte[length];
                for (int j = 0; j < chunks.size(); j++) {
                    if (j == 0) {
                        System.arraycopy(chunks.get(j), 0, after, 0, chunks.get(j).length);
                    } else {
                        System.arraycopy(chunks.get(j), 0, after, j * chunks.get(j - 1).length, chunks.get(j).length);
                    }
                }

                os.write(after);
                os.close();
                //Parser method parameters
           /* BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            FileInputStream inputstream = new FileInputStream("prodVideo/"+temp.videoName+".mp4");
            ParseContext pcontext = new ParseContext();
            MP4Parser MP4Parser = new MP4Parser();
            //parsing the document
            MP4Parser.parse(inputstream, handler, metadata);
            metadata.set("meta:creation-date",temp.dateCreated);
            metadata.set("xmpDM:duration",temp.length);
            metadata.set("tiff:ImageWidth",temp.frameWidth);
            metadata.set("tiff:ImageLength",temp.frameHeight);*/
                System.out.println("File downloaded");
            } catch (IOException e) {
                e.printStackTrace();
            }
            counter--;
        }while (counter>0);
    }

    @Override
    public void init() {
        try {
            //detecting all files already published in directory videos
            ArrayList<String> filenames = (ArrayList) Files.list(Paths.get(videoLocation + "/")).filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString()).collect(Collectors.toList());
            Value value=new Value();
            VideoFile video;
            for (int i = 0; i < filenames.size(); i++) {
                ArrayList<String> mylist = new ArrayList<>();
                BodyContentHandler handler = new BodyContentHandler();
                Metadata metadata = new Metadata();

                //detecting Mp4 files already published
                if (filenames.get(i).contains(".mp4")) {
                    FileInputStream inputstream = new FileInputStream(videoLocation + "/" + filenames.get(i));
                    ParseContext pcontext = new ParseContext();
                    MP4Parser MP4Parser = new MP4Parser();
                    MP4Parser.parse(inputstream, handler, metadata, pcontext);
                    video = new VideoFile(filenames.get(i).substring(0, filenames.get(i).length() - 4), channelName.channelName,
                            metadata.get("meta:creation-date"),
                            metadata.get("xmpDM:duration"), null, metadata.get("tiff:ImageWidth"),
                            metadata.get("tiff:ImageLength"), new ArrayList<>(), null);
                    value = new Value(video);
                    published.add(value);
                    namesVideos.add(video.videoName);
                }
                if (filenames.get(i).contains(".txt")) {
                    //detecting txt files already published
                    try {
                        FileReader reader = new FileReader(videoLocation + "/" + filenames.get(i));
                        BufferedReader breader = new BufferedReader(reader);
                        String a = breader.readLine();//diabazoyme to .txt to kanoyme split me ta # kai meta epeidh ta kobei ta prosthetoyme
                        String[] tags = a.split("#");
                        for (int j = 0; j < tags.length; j++) {
                            if (!tags[j].isEmpty()) {
                                mylist.add("#" + tags[j]);
                            }
                        }
                        breader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //adding hashtags to video
                        value.getVideoFile().setAssociatedHashtags(mylist);
                    //adding hashtags to channel name
                    for (String hashtag : mylist) {
                        if (!channelName.hashtagsPublished.contains(hashtag)) {
                            channelName.hashtagsPublished.add(hashtag);
                        }
                    }
                }
            }
            ArrayList<Value> temp=new ArrayList<>();
            for(int i=0;i<channelName.hashtagsPublished.size();i++) {
                temp=new ArrayList<>();
                for (int k = 0; k < published.size(); k++) {
                    if (published.get(k).getVideoFile().associatedHashtags.contains(channelName.hashtagsPublished.get(i))) {
                        temp.add(published.get(k));
                    }
                }
                channelName.userVideoFilesMap.put(channelName.hashtagsPublished.get(i), temp);
            }
            channelName.userVideoFilesMap.put(channelName.channelName, published);
        } catch (TikaException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Broker> getBrokers() {
        return null;
    }

    @Override
    public void connect() {
        try {
            requestSocket = new Socket("127.0.0.1", 4321);
            cons=new ConsumerThread(this,requestSocket);
            providerSocket = new ServerSocket(PORT);
            boolean flag=true;
                while (true) {
                    connection = providerSocket.accept();
                    pub=new PublisherThread(this,connection);
                    brokers.add(pub);
                    pub.start();
                    TimeUnit.SECONDS.sleep(1);
                    if(flag){
                        cons.start();
                        flag=false;
                    }
                }
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException | InterruptedException ioException) {
            try {
                requestSocket.close();
                providerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void disconnect() {
    return;

    }

    @Override
    public ArrayList<String> updateNodes() {
        ArrayList<String> newHashtags=new ArrayList<>();

        try {
            //detecting all files already published in directory
            ArrayList<String> filenames = (ArrayList) Files.list(Paths.get(videoLocation + "/")).filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString()).collect(Collectors.toList());
            Value value = new Value();
            VideoFile video;
                for (int i = 0; i < filenames.size(); i++) {
                    ArrayList<String> mylist = new ArrayList<>();
                    BodyContentHandler handler = new BodyContentHandler();
                    Metadata metadata = new Metadata();
                    if (!namesVideos.contains(filenames.get(i).substring(0, filenames.get(i).length() - 4))) {
                        //detecting Mp4 files already published
                        if (filenames.get(i).contains(".mp4")) {
                            FileInputStream inputstream = new FileInputStream(videoLocation + "/" + filenames.get(i));
                            ParseContext pcontext = new ParseContext();
                            MP4Parser MP4Parser = new MP4Parser();
                            MP4Parser.parse(inputstream, handler, metadata, pcontext);
                            video = new VideoFile(filenames.get(i).substring(0, filenames.get(i).length() - 4), channelName.channelName,
                                    metadata.get("meta:creation-date"),
                                    metadata.get("xmpDM:duration"), null, metadata.get("tiff:ImageWidth"),
                                    metadata.get("tiff:ImageLength"), new ArrayList<>(), null);
                            value = new Value(video);
                            published.add(value);
                            System.out.println("Video with name " + filenames.get(i).substring(0, filenames.get(i).length() - 4) + " added...");
                            channelName.userVideoFilesMap.put(channelName.channelName, published);
                            namesVideos.add(video.videoName);
                        }
                        if (filenames.get(i).contains(".txt")) {
                            //detecting txt files already published
                            try {
                                FileReader reader = new FileReader(videoLocation + "/" + filenames.get(i));
                                BufferedReader breader = new BufferedReader(reader);
                                String a = breader.readLine();//diabazoyme to .txt to kanoyme split me ta # kai meta epeidh ta kobei ta prosthetoyme
                                String[] tags = a.split("#");
                                for (int k = 0; k < tags.length; k++) {
                                    if (!tags[k].isEmpty()) {
                                        mylist.add("#" + tags[k]);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //adding hashtags to video
                            value.getVideoFile().setAssociatedHashtags(mylist);
                            //adding hashtags to channel name
                            for (String hashtag : mylist) {
                                if (!channelName.hashtagsPublished.contains(hashtag)) {
                                    channelName.hashtagsPublished.add(hashtag);
                                    newHashtags.add(hashtag);
                                }
                            }
                        }
                    }
                }
            ArrayList<Value> temp=new ArrayList<>();
            for(int i=0;i<newHashtags.size();i++) {
                temp=new ArrayList<>();
                for (int k = 0; k < published.size(); k++) {
                    if (published.get(k).getVideoFile().associatedHashtags.contains(newHashtags.get(i))) {
                        temp.add(published.get(k));
                    }
                }
                channelName.userVideoFilesMap.put(newHashtags.get(i), temp);
            }
        } catch (TikaException | IOException | SAXException e) {
            e.printStackTrace();
        }

        return  newHashtags;
    }

    @Override
    public void addHashTag(String hashTag) {

    }

    @Override
    public Boolean addHashTag(String videoName, String hashtag) {
        for (int j = 0; j < published.size(); j++) {
            if (published.get(j).getVideoFile().videoName.equals(videoName)) {
                if (published.get(j).getVideoFile().associatedHashtags.contains(hashtag)) {
                    return false;
                }
                published.get(j).getVideoFile().associatedHashtags.add(hashtag);
                if (channelName.hashtagsPublished.contains(hashtag)) {
                    channelName.userVideoFilesMap.get(hashtag).add(published.get(j));
                } else {
                    channelName.hashtagsPublished.add(hashtag);
                    ArrayList<Value> temp = new ArrayList<>();
                    temp.add(published.get(j));
                    channelName.userVideoFilesMap.put(hashtag, temp);
                }
            }
        }
        try {
            FileWriter writer = new FileWriter(videoLocation + "/" +videoName + ".txt", true);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(hashtag);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            try {
                File myObj = new File(videoLocation + "/" +videoName+".txt");
                FileWriter writer = new FileWriter(myObj);
                BufferedWriter bufferedWriter = new BufferedWriter(writer);
                bufferedWriter.write(hashtag);
                bufferedWriter.flush();
                bufferedWriter.close();
            } catch (IOException ee) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
        return true;
    }
    public void removeHashTagFromVideo(String videoName,String hashtag) {
        for (int j = 0; j < published.size(); j++) {
            if (published.get(j).getVideoFile().videoName.equals(videoName)) {
                if (!published.get(j).getVideoFile().associatedHashtags.contains(hashtag)) {
                    System.out.println("The video named "+ videoName+" does not contain "+hashtag+"...");
                    return;
                }

                published.get(j).getVideoFile().associatedHashtags.remove(hashtag);

                if (channelName.userVideoFilesMap.get(hashtag).size()==1) {
                    channelName.userVideoFilesMap.remove(hashtag);
                    channelName.hashtagsPublished.remove(hashtag);
                }
                try {
                    FileReader reader = new FileReader(videoLocation + "/" +videoName + ".txt");
                    BufferedReader breader = new BufferedReader(reader);
                    String a = breader.readLine();
                    FileWriter writer = new FileWriter(videoLocation + "/" +videoName + ".txt");
                    a=a.replace(hashtag,"");
                    writer.write(a);
                    writer.flush();
                    writer.close();
                    System.out.println("Hashtag "+hashtag+" successfully removed from "+videoName+"...");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void removeHashTag(String hashtag) {
        if(!channelName.userVideoFilesMap.containsKey(hashtag)){
            System.out.println("Hashtag: "+hashtag+" could not be found in any video...");
            return;
        }
        for(int i=0;i<channelName.userVideoFilesMap.get(hashtag).size();i++){
            channelName.userVideoFilesMap.get(hashtag).get(i).getVideoFile().associatedHashtags.remove(hashtag);
            try {
                FileReader reader = new FileReader(videoLocation + "/" +channelName.userVideoFilesMap.get(hashtag).get(i).getVideoFile().videoName + ".txt");
                BufferedReader breader = new BufferedReader(reader);
                String a = breader.readLine();
                FileWriter writer = new FileWriter(videoLocation + "/" +channelName.userVideoFilesMap.get(hashtag).get(i).getVideoFile().videoName + ".txt");
                a=a.replaceAll(hashtag,"");
                writer.append(a);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Hashtag "+hashtag+" successfully removed from all videos...");
    }

    @Override
    public void getBrokerList() {

    }

    @Override
    public Broker hashTopic(String hashTag) {
        return null;
    }

    public void deleteVideo(String videoName) {
        if(!namesVideos.contains(videoName)){
            System.out.println("Video named " + videoName + " could not be found...");
            return;
        }
        namesVideos.remove(videoName);
        Value temp=new Value();
        for(int i=0;i<published.size();i++){
            if(published.get(i).getVideoFile().videoName.equals(videoName)){
                for(int j=0;j<published.get(i).getVideoFile().associatedHashtags.size();j++)
                {
                    channelName.userVideoFilesMap.get(published.get(i).getVideoFile().associatedHashtags.get(j)).remove(published.get(i));
                    if(channelName.userVideoFilesMap.get(published.get(i).getVideoFile().associatedHashtags.get(j)).isEmpty()){
                        channelName.userVideoFilesMap.remove(published.get(i).getVideoFile().associatedHashtags.get(j));
                    }
                }
                temp=published.get(i);
                break;
            }
        }

        published.remove(temp);
        System.out.println("Video successfully deleted from app");
    }

    @Override
    public void push(String key, Value value) {
        Message videoChunks;
        if(!channelName.userVideoFilesMap.containsKey(key))
        {
            videoChunks=new Message(channelName.channelName,key,"No results",null);
            for(int j=0;j<brokers.size();j++) {
                if (brokers.get(j).equals(Thread.currentThread())) {
                    try {
                        brokers.get(j).out.writeObject(videoChunks);
                        brokers.get(j).out.flush();
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        videoChunks=new Message(channelName.channelName,key,String.valueOf(channelName.userVideoFilesMap.get(key).size()),null);
        ArrayList<Value> temp;
        for(int j=0;j<brokers.size();j++) {
            if (brokers.get(j).equals(Thread.currentThread())) {
                try {
                    brokers.get(j).out.writeObject(videoChunks);
                    brokers.get(j).out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < channelName.userVideoFilesMap.get(key).size(); i++) {
                    int counter;
                    temp = generateChunks(channelName.userVideoFilesMap.get(key).get(i).videoFile.videoName);
                    counter = temp.size() - 1;
                    for (int k = 0; k < temp.size(); k++) {
                        videoChunks = new Message(channelName.channelName, published.get(i).getVideoFile().videoName, String.valueOf(counter), temp.get(k));
                        try {
                            brokers.get(j).out.writeObject(videoChunks);
                            brokers.get(j).out.flush();
                            counter--;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
    }

    @Override
    public void notifyFailure(Broker broker) {

    }
    @Override
    public void notifyBrokersForHashTags(String hashTag) {

    }

    @Override
    public ArrayList<Value> generateChunks(String key) {
        VideoFile temp = new VideoFile();
        for (int i = 0; i < published.size(); i++) {
            //if key==Channel name
            if (key.equalsIgnoreCase(published.get(i).getVideoFile().videoName)) {
                temp = published.get(i).getVideoFile();
            }
        }
        byte[] byteArr = new byte[0];
        try {
            FileInputStream unchunkedmp4 = new FileInputStream(videoLocation+"/" + temp.videoName + ".mp4");
            byteArr = IOUtils.toByteArray(unchunkedmp4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int CHUNK_SIZE = maxBufferSize;
        byte[] temporary;
        int bytesRead = 0;
        ByteBuffer before = ByteBuffer.wrap(byteArr);
        int FILE_SIZE = byteArr.length;
        int NUMBER_OF_CHUNKS = FILE_SIZE / CHUNK_SIZE + 1;
        int bytesRemaining = FILE_SIZE;
        VideoFile copyInfo;
        ArrayList<Value> chunked = new ArrayList<>();
        for (int j = 0; j < NUMBER_OF_CHUNKS; j++) {
            if (j == NUMBER_OF_CHUNKS - 1) {
                CHUNK_SIZE = bytesRemaining;
            }
            temporary = new byte[CHUNK_SIZE]; //Temporary Byte Array
            before.get(temporary, 0, CHUNK_SIZE);
            bytesRead += CHUNK_SIZE;
            if (bytesRead > 0) // If bytes read is not empty
            {
                bytesRemaining -= CHUNK_SIZE;
            }
            copyInfo= new VideoFile(temp.videoName,temp.channelName,
                    temp.dateCreated,temp.length,temp.framerate,temp.frameWidth,
                    temp.frameHeight,temp.associatedHashtags,temporary);

            chunked.add(new Value(copyInfo));
        }
        return chunked;
    }

    public static void main(String[] args) {
        new AppNode(args[0],args[1]);
    }
}
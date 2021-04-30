package myAPP;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.IOUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class AppNode implements Publisher,Consumer {
    Socket requestSocket = null;
    ObjectOutputStream out;
    //ροή για να παίρνεις δεδομένα από τον διακομιστή
    ObjectInputStream in;
    ChannelName channelName = new ChannelName("tiktoker");
    ArrayList<Value> published = new ArrayList<>();
    Message answer;

    int maxBufferSize = 512 * 1024; //512kb
    public AppNode() {

    }

    @Override
    public void register(Broker broker, String channelName) {

    }

    @Override
    public void disconnect(Broker broker, String channelName) {

    }

    @Override
    public void playData(String data, Value value) {

        ByteBuffer after=null;
        do{
            try {
                answer = (Message) in.readObject();
                after.put(answer.getData());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }while(answer.getChunks()==1);
        File mp4 = new File("prodVideo/attempt_4.mp4");
        OutputStream os = null;
        try {
            os = new FileOutputStream(mp4);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            os.write(after.array());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void init() {
        try {
            //detecting all files already published in directory videos
            ArrayList<String> filenames = (ArrayList) Files.list(Paths.get("Videos/")).filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString()).collect(Collectors.toList());
            for (int i = 0; i < filenames.size(); i++) {

                ArrayList<String> mylist = new ArrayList<>();
                BodyContentHandler handler = new BodyContentHandler();
                Metadata metadata = new Metadata();
                //detecting Mp4 files already published
                if (filenames.get(i).contains(".mp4")) {
                    FileInputStream inputstream = new FileInputStream("Videos/" + filenames.get(i));
                    ParseContext pcontext = new ParseContext();
                    MP4Parser MP4Parser = new MP4Parser();
                    MP4Parser.parse(inputstream, handler, metadata, pcontext);

                }
                if (filenames.get(i).contains(".txt")) {

                    //detecting txt files already published
                    try {
                        FileReader reader = new FileReader("Videos/" + filenames.get(i));
                        BufferedReader breader = new BufferedReader(reader);

                        String a = breader.readLine();//diabazoyme to .txt to kanoyme split me ta # kai meta epeidh ta kobei ta prosthetoyme

                        String[] tags = a.split("#");
                        for (int j = 0; j < tags.length; j++) {
                            mylist.add("#" + tags[j]);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                    //adding hashtags to channel name
                    if (channelName.hashtagsPublished.isEmpty()) {
                        channelName.hashtagsPublished.addAll(mylist);
                    } else {
                        for (String hashtag : mylist) {
                            if (!channelName.hashtagsPublished.contains(hashtag)) {
                                channelName.hashtagsPublished.add(hashtag);
                            }
                        }
                    }
                }
                //assuming every MP4 file is followed by TXT with hashtags to create video file
                if (!((i + 1) == filenames.size())) {
                    if (filenames.get(i + 1).contains(".mp4")) {

                        VideoFile video = new VideoFile(filenames.get(i).substring(0, filenames.get(i).length() - 4), channelName.channelName, metadata.get("meta:creation-date"),
                                metadata.get("xmpDM:duration"), null, metadata.get("tiff:ImageWidth"),
                                metadata.get("tiff:ImageLength"), mylist, null);
                        Value value = new Value(video);
                        published.add(value);
                    }
                } else {

                    VideoFile video = new VideoFile(filenames.get(i).substring(0, filenames.get(i).length() - 4), channelName.channelName, metadata.get("meta:creation-date"),
                            metadata.get("xmpDM:duration"), null, metadata.get("tiff:ImageWidth"),
                            metadata.get("tiff:ImageLength"), mylist, null);
                    Value value = new Value(video);
                    published.add(value);

                }


            }
        } catch (TikaException | IOException | SAXException e) {
            e.printStackTrace();
        }
        generateChunks("tiktoker");

    }


    @Override
    public List<Broker> getBrokers() {
        return null;
    }

    @Override
    public void connect() {
        try {

            //Δημιουργία υποδοχής που θα συνδεθεί με τη θύρα 4321 στο διακομιστή
            //ο οποίος βρίσκεται στην διεύθυνση με IP 127.0.0.1
            requestSocket = new Socket("127.0.0.1", 4321);

            //Obtain Socket’s OutputStream and use it to initialize ObjectOutputStream
            out = new ObjectOutputStream(requestSocket.getOutputStream());

            //Obtain Socket’s InputStream and use it to initialize ObjectInputStream
            in = new ObjectInputStream(requestSocket.getInputStream());

        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void updateNodes() {

    }

    @Override
    public void addHashTag(String hashTag) {

    }

    @Override
    public void removeHashTag(String hashTag) {

    }

    @Override
    public void getBrokerList() {

    }

    @Override
    public Broker hashTopic(String hashTag) {
        return null;
    }

    @Override
    public void push(String key, Value value) {
        System.out.println("Entered push");
        generateChunks(key);
        for (int i = 0; i < published.size(); i++) {
            if (published.get(i).getVideoFile().associatedHashtags.contains(key) || channelName.channelName.equals(key)) {

                int CHUNK_SIZE = maxBufferSize;
                byte[] temporary = null;
                int bytesRead = 0;
                ByteBuffer before = ByteBuffer.wrap(published.get(i).getVideoFile().videoFileChunk);
                int FILE_SIZE = published.get(i).getVideoFile().videoFileChunk.length;
                int NUMBER_OF_CHUNKS = FILE_SIZE/CHUNK_SIZE +1;
                int bytesRemaining=FILE_SIZE;
                for (int j=0;j<NUMBER_OF_CHUNKS;j++){
                    if (j == NUMBER_OF_CHUNKS-1)
                    {
                        CHUNK_SIZE = bytesRemaining;
                        System.out.println("CHUNK_SIZE: " + CHUNK_SIZE);
                    }
                    temporary = new byte[CHUNK_SIZE]; //Temporary Byte Array
                    System.out.println("remaining "+bytesRemaining+" CHUNK "+CHUNK_SIZE+" BYTES READ "
                            + bytesRead +" temp " + temporary.length);
                    before.get(temporary, 0, CHUNK_SIZE);
                    System.out.println("temp"+temporary[0]);
                    bytesRead += CHUNK_SIZE;
                    if (bytesRead > 0) // If bytes read is not empty
                    {
                        bytesRemaining -= CHUNK_SIZE;
                    }
                    Message videoChunks = new Message(channelName.channelName, key, 1, temporary);
                    try {
                        out.writeObject(videoChunks);
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
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
        for (int i = 0; i < published.size(); i++) {
            //if key==Channel name
            if (key.equalsIgnoreCase(published.get(i).getVideoFile().channelName)) {
                try {
                    FileInputStream unchunkedmp4 = new FileInputStream("Videos/" + published.get(i).getVideoFile().videoName + ".mp4");
                    byte[] byteArr = IOUtils.toByteArray(unchunkedmp4);
                    published.get(i).getVideoFile().setVideoFileChunk(byteArr);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            for (int j = 0; j < published.get(i).getVideoFile().getAssociatedHashtags().size(); j++) {
                //if key==one of the hashtags
                if (key.equalsIgnoreCase(published.get(i).getVideoFile().getAssociatedHashtags().get(j))) {
                    try {
                        FileInputStream unchunkedmp4 = new FileInputStream("Videos/" + published.get(i).getVideoFile().videoName + ".mp4");
                        byte[] byteArr = IOUtils.toByteArray(unchunkedmp4);
                        published.get(i).getVideoFile().setVideoFileChunk(byteArr);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return published;
    }

    public static void main(String[] args) {
        AppNode app = new AppNode();
        Message answer;
        app.connect();
        app.init();
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
                answer= (Message) app.in.readObject();
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
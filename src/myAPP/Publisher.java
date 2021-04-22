package myAPP;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.IOUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class Publisher implements PublisherInterface {

    ChannelName channelName;

    public Publisher() {

    }

    @Override
    public void init() {

    }

    @Override
    public List<BrokerInterface> getBrokers() {
        return null;
    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void updateNodes() {

    }

    @Override
    public void addHashTag(String hashTag) {
        List<String> mylist = null;
        try {
            FileReader reader = new FileReader("Videos/idryma_athinas.txt");
            BufferedReader breader = new BufferedReader(reader);
            while(breader.readLine() !=null) {
                String a = breader.readLine();//diabazoyme to .txt to kanoyme split me ta # kai meta epeidh ta kobei ta prosthetoyme
                String tags[] = a.split("#");
                for (int i = 0; i < tags.length; i++) {
                    mylist.add("#" + tags[i]);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        @Override
    public void removeHashTag(String hashTag) {

    }

    @Override
    public void getBrokerList() {

    }

    @Override
    public BrokerInterface hashTopic(String hashTag) {
        return null;
    }

    @Override
    public void push(String hashTag, Value value) {

    }

    @Override
    public void notifyFailure(BrokerInterface broker) {

    }

    @Override
    public void notifyBrokersForHashTags(String hashTag) {

    }

    @Override
    public ArrayList<Value> generateChunks(String hashTag){


        try {
            //detecting the file type
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            FileInputStream inputstream = new FileInputStream(new File("Videos/attempt_1.mp4"));
            ParseContext pcontext = new ParseContext();
            //Html parser
            MP4Parser MP4Parser = new MP4Parser();
            MP4Parser.parse(inputstream, handler, metadata,pcontext);
            System.out.println("Contents of the document:  :" + handler.toString());
            System.out.println("Metadata of the document:");
            String[] metadataNames = metadata.names();
            String value = metadata.toString();

            for(String name : metadataNames) {
                System.out.println(name + ": " + metadata.get(name));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (TikaException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        FileInputStream unchunkedmp4 = null;
        try
        {
            unchunkedmp4 = new FileInputStream("Videos/attempt_1.mp4");

            byte [] byteArr = IOUtils.toByteArray(unchunkedmp4);
        }
        catch (IOException ioe)
        {}


        return null;
    }
}

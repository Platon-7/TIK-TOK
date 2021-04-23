package myAPP;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.IOUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Publisher implements PublisherInterface {

    ChannelName channelName;
    ArrayList<Value> published =new ArrayList<>();

    public Publisher() {
    channelName.channelName="tiktokers";
    }

    @Override
    public void init() {
        try {
            //detecting all files already published in directory videos
            ArrayList<String> filenames = (ArrayList)Files.list(Paths.get("Videos/")).filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString()).collect(Collectors.toList());
            for (int i=0;i< filenames.size();i++) {
                ArrayList<String> mylist = null;
                BodyContentHandler handler = new BodyContentHandler();
                Metadata metadata = new Metadata();
                //detecting Mp4 files already published
                if(filenames.get(i).contains(".mp4")) {
                    FileInputStream inputstream = new FileInputStream(new File("Videos/" + filenames.get(i)));
                    ParseContext pcontext = new ParseContext();
                    MP4Parser MP4Parser = new MP4Parser();
                    MP4Parser.parse(inputstream, handler, metadata, pcontext);

                }else{
                    //detecting txt files already published
                    try {
                        FileReader reader = new FileReader("Videos/" + filenames.get(i));
                        BufferedReader breader = new BufferedReader(reader);
                        while(breader.readLine() !=null) {
                            String a = breader.readLine();//diabazoyme to .txt to kanoyme split me ta # kai meta epeidh ta kobei ta prosthetoyme
                            String tags[] = a.split("#");
                            for (int j = 0; j < tags.length; j++) {
                                mylist.add("#" + tags[j]);
                            }
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //adding hashtags to channel name
                    if(channelName.hashtagsPublished.isEmpty()){
                        channelName.hashtagsPublished.addAll(mylist);
                    }else
                    {
                        for(String hashtag:mylist){
                            if(!channelName.hashtagsPublished.contains(hashtag)){
                                channelName.hashtagsPublished.add(hashtag);
                            }
                        }
                    }
                }
                //assuming every MP4 file is followed by TXT with hashtags to create video file
                if(!(i+1> filenames.size())) {
                    if (filenames.get(i + 1).contains(".mp4")) {

                        VideoFile video = new VideoFile(filenames.get(i).substring(0, filenames.get(i).length() - 4), channelName.channelName, metadata.get("meta:creation-date"),
                                metadata.get("xmpDM:duration"), null, metadata.get("tiff:ImageWidth"),
                                metadata.get("tiff:ImageLength"), mylist, null);
                        Value value = new Value(video);
                        published.add(value);
                    }
                }else{

                        VideoFile video = new VideoFile(filenames.get(i).substring(0, filenames.get(i).length() - 4), channelName.channelName, metadata.get("meta:creation-date"),
                                metadata.get("xmpDM:duration"), null, metadata.get("tiff:ImageWidth"),
                                metadata.get("tiff:ImageLength"), mylist, null);
                        Value value = new Value(video);
                        published.add(value);

                }



            }
            } catch(FileNotFoundException e){
                e.printStackTrace();
            } catch(TikaException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            } catch(SAXException e){
                e.printStackTrace();
            }

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
    public ArrayList<Value> generateChunks(String key){




            for(int i=0;i<published.size();i++){
                //if key==Channel name or key== video name
                if(key.equalsIgnoreCase(published.get(i).getVideoFile().channelName)||key.equalsIgnoreCase(published.get(i).getVideoFile().videoName)){
                    try {
                        FileInputStream unchunkedmp4 = new FileInputStream("Videos/"+published.get(i).getVideoFile().videoName+".mp4" );
                        byte [] byteArr = IOUtils.toByteArray(unchunkedmp4);
                        published.get(i).getVideoFile().setVideoFileChunk(byteArr);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                for(int j=0;j<published.get(i).getVideoFile().getAssociatedHashtags().size();j++){
                    //if key==one of the hashtags
                    if(key.equalsIgnoreCase(published.get(i).getVideoFile().getAssociatedHashtags().get(j))){
                            try {
                                FileInputStream unchunkedmp4 = new FileInputStream("Videos/"+published.get(i).getVideoFile().videoName+".mp4" );
                                byte [] byteArr = IOUtils.toByteArray(unchunkedmp4);
                                published.get(i).getVideoFile().setVideoFileChunk(byteArr);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }
                }
            }




        return published;
    }
}

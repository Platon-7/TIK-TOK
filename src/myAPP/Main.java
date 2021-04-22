package myAPP;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    public static void main(String args[]){
        try {
            //detecting the file type
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            FileInputStream inputstream = new FileInputStream("Videos/attempt_1.mp4");
            ParseContext pcontext = new ParseContext();
            //Html parser
            MP4Parser MP4Parser = new MP4Parser();
            MP4Parser.parse(inputstream, handler, metadata,pcontext);
            System.out.println("Contents of the document:  :" + handler.toString());
            System.out.println("Metadata of the document:");
            String[] metadataNames = metadata.names();


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

    }
}

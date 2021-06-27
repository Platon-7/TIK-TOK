package com.example.myAPP;

import java.io.Serializable;
import java.util.ArrayList;

public class VideoFile implements Serializable {
    private static final long serialVersionUID = -2723363051271966962L;
    String videoName;
    String channelName;
    String dateCreated;
    String length;
    String framerate;
    String frameWidth;
    String frameHeight;
    ArrayList<String> associatedHashtags;
    byte[] videoFileChunk;

    public void setVideoFileChunk(byte[] videoFileChunk) {
        this.videoFileChunk = videoFileChunk;
    }

    public byte[] getVideoFileChunk() {
        return videoFileChunk;
    }

    public VideoFile() {
    }

    public ArrayList<String> getAssociatedHashtags() {
        return associatedHashtags;
    }

    public void setAssociatedHashtags(ArrayList<String> associatedHashtags) {
        this.associatedHashtags = associatedHashtags;
    }

    public VideoFile(String videoName, String channelName, String dateCreated,
                     String length, String framerate, String frameWidth,
                     String frameHeight, ArrayList<String> associatedHashtags, byte[] videoFileChunk) {
        this.videoName = videoName;
        this.channelName = channelName;
        this.dateCreated = dateCreated;
        this.length = length;
        this.framerate = framerate;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.associatedHashtags = associatedHashtags;
        this.videoFileChunk = videoFileChunk;

    }
}

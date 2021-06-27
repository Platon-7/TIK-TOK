package com.example.myAPP;

import java.io.Serializable;

public class Value implements Serializable {
    private static final long serialVersionUID = -2723363051271966963L;
    VideoFile videoFile;

    public Value(VideoFile videoFile) {
        this.videoFile = videoFile;
    }

    public Value() {
    }

    public VideoFile getVideoFile() {
        return videoFile;
    }

    public void setVideoFile(VideoFile videoFile) {
        this.videoFile = videoFile;
    }
}

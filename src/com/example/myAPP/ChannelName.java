package com.example.myAPP;

import java.util.ArrayList;
import java.util.HashMap;

public class ChannelName {
    String channelName;
    ArrayList<String> hashtagsPublished;
    HashMap<String, ArrayList<Value>> userVideoFilesMap;

    public ChannelName(String channelName) {
        this.channelName = channelName;
        hashtagsPublished=new ArrayList<>();
        userVideoFilesMap=new HashMap<>();
    }
}

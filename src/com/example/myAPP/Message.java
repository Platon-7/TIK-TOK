package com.example.myAPP;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = -2723363051271966964L;
    Value data;
    String key;
    String channelName;
    String flag;

    public Message(String channelName, String key, String flag, Value data) {
        this.channelName=channelName;
        this.key=key;
        this.data=data;
        this.flag=flag;
    }
    public Message(){};
    public String getChannelName() {
        return channelName;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public Value getData() {
        return data;
    }

    public String getKey() {
        return key;
    }
}

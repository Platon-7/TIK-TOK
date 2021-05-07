package myAPP;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = -2723363051271966964L;
    byte[] data;
    String key;
    String channelName;
    int chunks;

    public Message(String channelName,String key,int chunks,byte[] data) {
        this.channelName=channelName;
        this.key=key;
        this.data=data;
        this.chunks=chunks;
    }

    public String getChannelName() {
        return channelName;
    }

    public int getChunks() {
        return chunks;
    }

    public void setChunks(int chunks) {
        this.chunks = chunks;
    }

    public byte[] getData() {
        return data;
    }

    public String getKey() {
        return key;
    }
}

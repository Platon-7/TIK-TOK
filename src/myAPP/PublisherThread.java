package myAPP;

public class PublisherThread extends Thread {
    AppNode app;
    public PublisherThread(AppNode app){
        this.app=app;
        app.init();
    }

    public void run() {
        app.init();
        app.connect();

    }
}

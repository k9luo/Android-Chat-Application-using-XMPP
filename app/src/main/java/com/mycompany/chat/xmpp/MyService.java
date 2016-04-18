package com.mycompany.chat.xmpp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import org.jivesoftware.smack.chat.Chat;

public class MyService extends Service {

    private static final String DOMAIN = "192.168.0.108";
    private static String USERNAME = "admin";
    private static String PASSWORD = "admin";
    public static ConnectivityManager cm;
    public static MyXMPP xmpp;
    public static boolean ServerchatCreated = false;
    String text = "";
    private Messenger messageHandler;

    @Override
    public IBinder onBind(final Intent intent) {
        return new LocalBinder<MyService>(this);

    }

    public Chat chat;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences pref = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
        String user=pref.getString("user", null);
        String pass=pref.getString("pass",null );
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        xmpp = MyXMPP.getInstance(MyService.this, DOMAIN, user, pass);
        xmpp.connect("onCreate");
    }



    @Override
    public int onStartCommand(final Intent intent, final int flags,
                              final int startId) {


        return Service.START_NOT_STICKY;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        xmpp.connection.disconnect();
    }

    public static boolean isNetworkConnected() {
        return cm.getActiveNetworkInfo() != null;
    }


    public void sendMessage(int i) {
        Message message = Message.obtain();
        switch (i) {
            case 1:
                message.arg1 = 1;
                break;
        }
        try {
            messageHandler.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
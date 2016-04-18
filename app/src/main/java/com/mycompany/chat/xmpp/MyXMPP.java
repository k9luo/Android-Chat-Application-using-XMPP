package com.mycompany.chat.xmpp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mycompany.chat.adapter.MessageModel;
import com.mycompany.chat.ui.ChatActivity;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager.AutoReceiptMode;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyXMPP implements PingFailedListener {
    private static byte[] dataReceived;
    public static boolean connected = false;
    public boolean loggedin = false;
    public static boolean isconnecting = false;
    public static boolean isToasted = true;
    private boolean chat_created = false;
    private String serverAddress;
    public static XMPPTCPConnection connection;
    public static String loginUser;
    public static String passwordUser;
    Gson gson;
    MyService context;
    public static MyXMPP instance = null;
    public static boolean instanceCreated = false;
    private FileTransferManager manager;

    public MyXMPP(MyService context, String serverAdress, String logiUser,
                  String passwordser) {
        this.serverAddress = serverAdress;
        this.loginUser = logiUser;
        this.passwordUser = passwordser;
        this.context = context;
        init();

    }

    public MyXMPP() {

    }

    public XMPPTCPConnection getConnection() {
        return connection;
    }

    public static MyXMPP getInstance(MyService context, String server,
                                     String user, String pass) {

        if (instance == null) {
            instance = new MyXMPP(context, server, user, pass);
            instanceCreated = true;
        }
        return instance;

    }

    public org.jivesoftware.smack.chat.Chat Mychat;

    ChatManagerListenerImpl mChatManagerListener;
    MMessageListener mMessageListener;

    String text = "";
    String mMessage = "", mReceiver = "";

    static {
        try {
            Class.forName("org.jivesoftware.smack.ReconnectionManager");
        } catch (ClassNotFoundException ex) {
            // problem loading reconnection manager
        }
    }

    public void init() {
        gson = new Gson();
        mMessageListener = new MMessageListener(context);
        mChatManagerListener = new ChatManagerListenerImpl();
        initialiseConnection();

    }

    private void initialiseConnection() {

        XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration
                .builder();
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        config.setServiceName(serverAddress);
        config.setHost(serverAddress);
        config.setPort(5222);
        config.setDebuggerEnabled(true);
        XMPPTCPConnection.setUseStreamManagementResumptiodDefault(true);
        XMPPTCPConnection.setUseStreamManagementDefault(true);
        connection = new XMPPTCPConnection(config.build());
        XMPPConnectionListener connectionListener = new XMPPConnectionListener();
        connection.addConnectionListener(connectionListener);
        PingManager pingManager = PingManager.getInstanceFor(connection);
        pingManager.registerPingFailedListener(this);
        manager = FileTransferManager.getInstanceFor(connection);
        manager.addFileTransferListener(new FileTransferIMPL());
        FileTransferNegotiator.getInstanceFor(connection);
    }

    public void disconnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                connection.disconnect();
            }
        }).start();
    }

    public void connect(final String caller) {

        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected synchronized Boolean doInBackground(Void... arg0) {
                if (connection.isConnected())
                    return false;
                isconnecting = true;
                if (isToasted)
                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {

                            Toast.makeText(context,
                                    caller + "=>connecting....",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                Log.d("Connect() Function", caller + "=>connecting....");

                try {
                    connection.connect();
                    DeliveryReceiptManager dm = DeliveryReceiptManager
                            .getInstanceFor(connection);
                    dm.setAutoReceiptMode(AutoReceiptMode.always);
                    dm.addReceiptReceivedListener(new ReceiptReceivedListener() {

                        @Override
                        public void onReceiptReceived(final String fromid,
                                                      final String toid, final String msgid,
                                                      final Stanza packet) {

                        }
                    });
                    connected = true;

                } catch (IOException e) {
                    if (isToasted)
                        new Handler(Looper.getMainLooper())
                                .post(new Runnable() {

                                    @Override
                                    public void run() {

                                        Toast.makeText(
                                                context,
                                                "(" + caller + ")"
                                                        + "IOException: ",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                    Log.e("(" + caller + ")", "IOException: " + e.getMessage());
                } catch (SmackException e) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(context,
                                    "(" + caller + ")" + "SMACKException: ",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.e("(" + caller + ")",
                            "SMACKException: " + e.getMessage());
                } catch (XMPPException e) {
                    if (isToasted)

                        new Handler(Looper.getMainLooper())
                                .post(new Runnable() {

                                    @Override
                                    public void run() {

                                        Toast.makeText(
                                                context,
                                                "(" + caller + ")"
                                                        + "XMPPException: ",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    Log.e("connect(" + caller + ")",
                            "XMPPException: " + e.getMessage());

                }
                return isconnecting = false;
            }
        };
        connectionThread.execute();
    }

    public void login() {

        try {
            connection.login(loginUser, passwordUser);
            Log.i("LOGIN", "Yey! We're connected to the Xmpp server!");
            loggedin = true;

        } catch (XMPPException | SmackException | IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }

    }

    @Override
    public void pingFailed() {

    }


    private class ChatManagerListenerImpl implements ChatManagerListener {
        @Override
        public void chatCreated(final org.jivesoftware.smack.chat.Chat chat,
                                final boolean createdLocally) {
            if (!createdLocally)
                chat.addMessageListener(mMessageListener);

        }

    }

    public void sendMessage(MessageModel chatMessage) {
        String body = gson.toJson(chatMessage);

        if (!chat_created) {
            Mychat = ChatManager.getInstanceFor(connection).createChat(
                    chatMessage.getReceiver() + "@algonation",
                    mMessageListener);
            chat_created = true;
        }
        final Message message = new Message();
        message.setBody(body);
        message.setStanzaId(String.valueOf(chatMessage.getMsgIdl()));
        message.setType(Message.Type.chat);

        try {
            if (connection.isAuthenticated()) {

                Mychat.sendMessage(message);

            } else {

                login();
            }
        } catch (NotConnectedException e) {
            Log.e("xmpp.SendMessage()", "msg Not sent!-Not Connected!");

        } catch (Exception e) {
            Log.e("xmpp.SendMessage()-Exception",
                    "msg Not sent!" + e.getMessage());
        }

    }

    public class XMPPConnectionListener implements ConnectionListener {
        @Override
        public void connected(final XMPPConnection connection) {

            Log.d("xmpp", "Connected!");
            connected = true;
            if (!connection.isAuthenticated()) {
                login();
            }
        }

        @Override
        public void connectionClosed() {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                        Toast.makeText(context, "ConnectionCLosed!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            Log.d("xmpp", "ConnectionCLosed!");
            connected = false;
            chat_created = false;
            loggedin = false;
        }

        @Override
        public void connectionClosedOnError(Exception arg0) {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(context, "ConnectionClosedOn Error!!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            Log.d("xmpp", "ConnectionClosedOn Error!");
            connected = false;

            chat_created = false;
            loggedin = false;
        }

        @Override
        public void reconnectingIn(int arg0) {

            Log.d("xmpp", "Reconnectingin " + arg0);

            loggedin = false;
        }

        @Override
        public void reconnectionFailed(Exception arg0) {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {

                        Toast.makeText(context, "ReconnectionFailed!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            Log.d("xmpp", "ReconnectionFailed!");
            connected = false;

            chat_created = false;
            loggedin = false;
        }

        @Override
        public void reconnectionSuccessful() {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                        Toast.makeText(context, "REConnected!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            Log.d("xmpp", "ReconnectionSuccessful");
            connected = true;

            chat_created = false;
            loggedin = false;
        }

        @Override
        public void authenticated(XMPPConnection arg0, boolean arg1) {
            Log.d("xmpp", "Authenticated!");
            loggedin = true;

            ChatManager.getInstanceFor(connection).addChatListener(
                    mChatManagerListener);

            chat_created = false;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }).start();
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                        Toast.makeText(context, "Connected!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
        }
    }

    private class MMessageListener implements ChatMessageListener {

        public MMessageListener(Context contxt) {
        }

        @Override
        public void processMessage(final org.jivesoftware.smack.chat.Chat chat,
                                   final Message message) {
            Log.i("MyXMPP_MESSAGE_LISTENER", "Xmpp message received: '"
                    + message);

            if (message.getType() == Message.Type.chat
                    && message.getBody() != null) {
                final MessageModel chatMessage = gson.fromJson(
                        message.getBody(), MessageModel.class);

                processMessage(message);
            }
        }

        private void processMessage(final Message message) {

            String sender1 = message.getFrom();
            String receiver = message.getTo();
            final Random random = new Random();
            final String delimiter = "\\@";
            String[] temp = sender1.split(delimiter);
            String[] temp1 = receiver.split(delimiter);
            final String sender = temp[0];
            Log.d("USERS" + sender, temp1[0]);

            final MessageModel messageModel = gson.fromJson(
                    message.getBody(), MessageModel.class);
            messageModel.setIsMine(false);
            messageModel.setMsgIdl(random.nextInt(1000));
            messageModel.setType("TEXT");
            messageModel.setReceiver(temp1[0]);
            messageModel.setSender(sender);
            ChatActivity.chatlist.add(messageModel);

            CommonMethods commonMethods = new CommonMethods(context);
            commonMethods.createTable(temp1[0]);
            //  String tablename, String s, String r, String m, String w,String datatype
            //commonMethods.insertIntoTable(temp1[0], sender, temp1[0], message.getBody(), "r", "TEXT");
            commonMethods.insertIntoTable(messageModel.getSender(), messageModel.getSender(), messageModel.getReceiver(), messageModel.getMsg(), "r", "TEXT");

            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    ChatActivity.chatAdapter.notifyDataSetChanged();

                }
            });
        }
    }

    public class FileTransferIMPL implements FileTransferListener {

        @Override
        public void fileTransferRequest(final FileTransferRequest request) {
            final IncomingFileTransfer transfer = request.accept();
            try {
                InputStream is = transfer.recieveFile();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                int nRead;
                byte[] buf = new byte[1024];
                try {
                    while ((nRead = is.read(buf, 0, buf.length)) != -1) {
                        os.write(buf, 0, nRead);
                    }
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dataReceived = os.toByteArray();
                createDirectoryAndSaveFile(dataReceived, request.getFileName());
                Log.i("File Received", transfer.getFileName());
                processMessage(request);
            } catch (XMPPException ex) {
                Logger.getLogger(MyXMPP.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SmackException e) {
                e.printStackTrace();
            }
        }
    }

    private void createDirectoryAndSaveFile(byte[] imageToSave, String fileName) {
        File direct = new File(Environment.getExternalStorageDirectory() + "/LocShopie/Received/");
        if (!direct.exists()) {
            File wallpaperDirectory = new File("/sdcard/LocShopie/Received/");
            wallpaperDirectory.mkdirs();
        }
        File file = new File(new File("/sdcard/LocShopie/Received/"), fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(imageToSave);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMessage(final FileTransferRequest request) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                Log.i("MSG RECE", "LOOPER");
                Random random = new Random();
                CommonMethods commonMethods = new CommonMethods(context);
                int iend = request.getRequestor().lastIndexOf("@");
                String requester = request.getRequestor().substring(0, 10);
                commonMethods.createTable(requester);
                Log.i("MSG RECE", requester);

                SharedPreferences pref = context.getSharedPreferences("Login", context.MODE_PRIVATE);
                String rec = pref.getString("user", null);

                // String tablename, String s, String r, String m, String w,String datatype
                commonMethods.insertIntoTable(requester, requester, rec, request.getFileName(), "r", "IMAGE");
                final MessageModel chatMessage = new MessageModel();
                chatMessage.setSender(requester);
                chatMessage.setType("IMAGE");
                chatMessage.setReceiver(rec);
                chatMessage.setMsgIdl(random.nextInt(1000));
                chatMessage.setIsMine(false);
                chatMessage.setMsg(request.getFileName());
                ChatActivity.chatlist.add(chatMessage);
                ChatActivity.chatAdapter.notifyDataSetChanged();
                Log.i("MSG RECE", request.getRequestor());

            }
        });
    }

    public void fileTransfer(String user, Bitmap bitmap, String filename) throws XMPPException {
        Roster roster = Roster.getInstanceFor(connection);
        String destination = roster.getPresence(user).getFrom();
        // Create the file transfer manager
        FileTransferManager manager = FileTransferManager.getInstanceFor(connection);
        // Create the outgoing file transfer
        final OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(destination);
        // Send the file
        //transfer.sendFile(new File("abc.txt"), "You won't believe this!");
        transfer.sendStream(new ByteArrayInputStream(convertFileToByte(bitmap)), filename, convertFileToByte(bitmap).length, "A greeting");

        System.out.println("Status :: " + transfer.getStatus() + " Error :: " + transfer.getError() + " Exception :: " + transfer.getException());
        System.out.println("Is it done? " + transfer.isDone());
        if (transfer.getStatus().equals(FileTransfer.Status.refused))
            System.out.println("refused  " + transfer.getError());
        else if (transfer.getStatus().equals(FileTransfer.Status.error))
            System.out.println(" error " + transfer.getError());
        else if (transfer.getStatus().equals(FileTransfer.Status.cancelled))
            System.out.println(" cancelled  " + transfer.getError());
        else
            System.out.println("Success");
    }


    public byte[] convertFileToByte(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public boolean createNewAccount(String username, String newpassword) {
        boolean status = false;
        if (connection == null) {
            try {
                connection.connect();
            } catch (SmackException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }

        try {
            String newusername = username + connection.getServiceName();
            Log.i("service", connection.getServiceName());
            AccountManager accountManager = AccountManager.getInstance(connection);
            accountManager.createAccount(username, newpassword);
            status = true;
        } catch (SmackException.NoResponseException e) {
            status = false;
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
            status = false;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            status = false;
        }
        connection.disconnect();
        return status;

    }

}
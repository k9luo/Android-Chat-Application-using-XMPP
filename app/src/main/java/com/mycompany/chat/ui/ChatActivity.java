package com.mycompany.chat.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.mycompany.chat.R;
import com.mycompany.chat.adapter.ChatAdapter;
import com.mycompany.chat.adapter.MessageModel;
import com.mycompany.chat.xmpp.CommonMethods;
import com.mycompany.chat.xmpp.MyService;

import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Random;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText msg_edittext;
    private Random random;
    public static ArrayList<MessageModel> chatlist;
    public static ChatAdapter chatAdapter;
    ListView msgListView;
    private String receiverUser;
    private String senderUser;
    private SQLiteDatabase mydb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        random = new Random();

        Intent intent = getIntent();
        receiverUser = intent.getStringExtra("USER");
        SharedPreferences pref = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
        senderUser = pref.getString("user", null);


        msg_edittext = (EditText) findViewById(R.id.messageEditText);
        msgListView = (ListView) findViewById(R.id.msgListView);
        ImageView imageView = (ImageView) findViewById(R.id.imageSendButton);
        imageView.setOnClickListener(this);
        ImageButton sendButton = (ImageButton) findViewById(R.id.sendMessageButton);
        sendButton.setOnClickListener(this);

        // ----Set autoscroll of listview when a new message arrives----//
        msgListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        msgListView.setStackFromBottom(true);

        chatlist = new ArrayList<MessageModel>();
        chatAdapter = new ChatAdapter(ChatActivity.this, chatlist);

        if (isTableExists(receiverUser)) {
            loadDataFromLocal(receiverUser);
        }

        msgListView.setAdapter(chatAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sendMessageButton:
                sendTextMessage(view);
                break;
            case R.id.imageSendButton:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 123);
                break;
        }
    }

    public void sendTextMessage(View v) {
        String message = msg_edittext.getEditableText().toString();
        if (!message.equalsIgnoreCase("")) {
            //chatlist.add(new MessageModel(senderUser, receiverUser, message, "1", true,random.nextInt(1000)));
            msg_edittext.setText("");
            //
            //
            chatAdapter.add(new MessageModel(senderUser, receiverUser, message, "TEXT", true, random.nextInt(1000)));
            //chatlist.add(new MessageModel(senderUser, receiverUser, message, "TEXT", true, random.nextInt(1000)));
            chatAdapter.notifyDataSetChanged();
            MyService.xmpp.sendMessage(new MessageModel(senderUser, receiverUser, message, "TEXT", true, random.nextInt(1000)));
            CommonMethods commonMethods = new CommonMethods(ChatActivity.this);
            commonMethods.createTable(receiverUser);
            //  String tablename, String s, String r, String m, String w,String datatype
            commonMethods.insertIntoTable(receiverUser, senderUser, receiverUser, message, "m", "TEXT");
            chatAdapter.notifyDataSetChanged();


        }
    }


    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase("chat", null,
                    SQLiteDatabase.OPEN_READONLY);
            checkDB.close();
        } catch (SQLiteException e) {
            // database doesn't exist yet.
        }
        return checkDB != null;
    }

    public boolean isTableExists(String tableName) {
        mydb = openOrCreateDatabase(CommonMethods.DBNAME, Context.MODE_PRIVATE, null);
        Cursor cursor = mydb.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public void loadDataFromLocal(String tablename) {
        String tblname = "'" + tablename + "'";
        boolean w = false;
        mydb = openOrCreateDatabase(CommonMethods.DBNAME, Context.MODE_PRIVATE, null);
        Cursor allrows = mydb.rawQuery("SELECT * FROM " + tblname, null);
        System.out.println("COUNT : " + allrows.getCount());
        Integer cindex = allrows.getColumnIndex("sender");
        Integer cindex1 = allrows.getColumnIndex("receiver");
        Integer cindext2 = allrows.getColumnIndex("msg");
        Integer cindex3 = allrows.getColumnIndex("who");
        Integer cindex4 = allrows.getColumnIndex("type");
        System.out.print(cindex + "\n" + cindex1 + "\n" + cindext2 + "\n" + cindex3);
        if (allrows.moveToFirst()) {
            do {
                String sender = allrows.getString(allrows.getColumnIndex("sender"));
                String receiver = allrows.getString(allrows.getColumnIndex("receiver"));
                String msg = allrows.getString(allrows.getColumnIndex("msg"));
                String who = allrows.getString(allrows.getColumnIndex("who"));
                String type = allrows.getString(allrows.getColumnIndex("type"));
                if (who.equals("m")) {
                    w = true;
                } else if (who.equals("r")) {
                    w = false;
                }
                MessageModel messageModel = new MessageModel(sender, receiver, msg, type, w, random.nextInt(1000));
                chatAdapter.add(messageModel);

            }
            while (allrows.moveToNext());
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == 123 && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data
                final Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                final Bitmap bitmap = BitmapFactory.decodeFile(imgDecodableString);
                new Handler(Looper.getMainLooper())
                        .post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String message = getFileName(selectedImage);
                                    CommonMethods commonMethods = new CommonMethods(ChatActivity.this);
                                    commonMethods.createTable(receiverUser);
                                    final MessageModel chatMessage = new MessageModel();
                                    chatMessage.setMsg(message);
                                    chatMessage.setSender(senderUser);
                                    chatMessage.setIsMine(true);
                                    chatMessage.setType("IMAGE");
                                    chatMessage.setReceiver(receiverUser);
                                    chatMessage.setMsgIdl(random.nextInt(1000));
                                    msg_edittext.setText("");
                                    chatlist.add(chatMessage);
                                    new SendPicture(bitmap, selectedImage).execute();
                                    chatAdapter.notifyDataSetChanged();
                                    //  String tablename, String s, String r, String m, String w,String datatype
                                    commonMethods.insertIntoTable(receiverUser, receiverUser, senderUser, message, "m", "IMAGE");
                                    MyService.xmpp.fileTransfer(receiverUser + "@algonation/Smack", bitmap, getFileName(selectedImage));
                                } catch (XMPPException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public String getFileName(Uri uri) {
        String path = uri.getPath();
        String filename = null;
        if (path.length() > 0) {
            String filepath = path;
            String fname = filepath.substring(filepath.lastIndexOf("/") + 1, filepath.length());
            String filetype = ".jpg";
            filename = fname + filetype;
        } else {
        }
        return filename;
    }

    private void createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {
        File direct = new File(Environment.getExternalStorageDirectory() + "/LocShopie/sent/");
        if (!direct.exists()) {
            File wallpaperDirectory = new File("/sdcard/LocShopie/sent/");
            wallpaperDirectory.mkdirs();
        }
        File file = new File(new File("/sdcard/LocShopie/sent/"), fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class SendPicture extends AsyncTask<Void, Void, Void> {
        Uri uri;
        Bitmap bitmap;

        public SendPicture(Bitmap bitmap, Uri uri) {
            this.bitmap = bitmap;
            this.uri = uri;
        }

        @Override
        protected Void doInBackground(Void... params) {
            createDirectoryAndSaveFile(bitmap, getFileName(uri));


            return null;
        }
    }

}

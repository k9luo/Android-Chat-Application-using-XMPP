package com.mycompany.chat.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mycompany.chat.R;

import java.util.ArrayList;

public class ChatAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    ArrayList<MessageModel> chatMessageList;

    public ChatAdapter(AppCompatActivity activity, ArrayList<MessageModel> list) {
        chatMessageList = list;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return chatMessageList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageModel message = (MessageModel) chatMessageList.get(position);
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(R.layout.chatbubble, null);
        TextView msg = (TextView) vi.findViewById(R.id.message_text);
        ImageView imageView = (ImageView) vi.findViewById(R.id.imageMsg);

        if (message.getType().contains("TEXT")) {
            msg.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            msg.setText(message.getMsg());
        } else if (message.getType().contains("IMAGE")) {
            msg.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            if (message.isMine()) {
                String zipFile = Environment.getExternalStorageDirectory() + "/LocShopie/sent/" + message.getMsg();
                Bitmap bitmap = BitmapFactory.decodeFile(zipFile);
                imageView.setImageBitmap(bitmap);

            } else {
                String zipFile = Environment.getExternalStorageDirectory() + "/LocShopie/Received/" + message.getMsg();
                Bitmap bitmap = BitmapFactory.decodeFile(zipFile);
                imageView.setImageBitmap(bitmap);
            }

        }

        LinearLayout layout = (LinearLayout) vi
                .findViewById(R.id.bubble_layout);
        LinearLayout parent_layout = (LinearLayout) vi
                .findViewById(R.id.bubble_layout_parent);
        // if message is mine then align to right
        if (message.isMine) {
            layout.setBackgroundResource(R.drawable.bubble2);
            parent_layout.setGravity(Gravity.RIGHT);
        }
        // If not mine then align to left
        else {
            layout.setBackgroundResource(R.drawable.bubble1);
            parent_layout.setGravity(Gravity.LEFT);
        }
        msg.setTextColor(Color.BLACK);
        return vi;
    }

    public void add(MessageModel object) {
        chatMessageList.add(object);
    }
}
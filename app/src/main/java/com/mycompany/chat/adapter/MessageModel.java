package com.mycompany.chat.adapter;

public class MessageModel {
    String sender;
    String receiver;
    String msg;
    String type;
    boolean isMine;

    public int getMsgIdl() {
        return msgIdl;
    }

    public void setMsgIdl(int msgIdl) {
        this.msgIdl = msgIdl;
    }

    int msgIdl;
    public MessageModel(){

    }
    public
    MessageModel(String sen,String rec,String msgBody,String msgtype,boolean whoIsSender,int Id){
        this.msgIdl=Id;
        this.sender=sen;
        this.receiver=rec;
        this.msg=msgBody;
        this.type=msgtype;
        this.isMine=whoIsSender;

    }


    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setIsMine(boolean isMine) {
        this.isMine = isMine;
    }
}

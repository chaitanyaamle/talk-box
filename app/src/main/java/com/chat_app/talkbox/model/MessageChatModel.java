package com.chat_app.talkbox.model;

public class MessageChatModel {
    private String id;
    private String text;
    private String fromId;
    private String toId;
    private Long timestamp;

    public MessageChatModel(){

    }

    public MessageChatModel(String id,String text, String fromId,String toId,Long timestamp) {
        this.text = text;
        this.id = id;
        this.fromId = fromId;
        this.toId = toId;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getFromId() {
        return fromId;
    }

    public String getToId() {
        return toId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}

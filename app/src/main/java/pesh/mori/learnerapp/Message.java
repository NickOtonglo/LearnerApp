package pesh.mori.learnerapp;

/**
 * Created by Nick Otto on 16/09/2018.
 */

public class Message {

    private String title,message,timestamp,category,recipient_id,sender_id,reference;

    public Message() {

    }

    public Message(String title, String message, String timestamp, String category, String recipient_id, String sender_id, String reference) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.category = category;
        this.recipient_id = recipient_id;
        this.sender_id = sender_id;
        this.reference = reference;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRecipient_id() {
        return recipient_id;
    }

    public void setRecipient_id(String recipient_id) {
        this.recipient_id = recipient_id;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}

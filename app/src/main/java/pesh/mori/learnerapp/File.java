package pesh.mori.learnerapp;

/**
 * Created by Nick Otto on 15/08/2018.
 */

public class File {
    private String title,description,url,timestamp,file_type,file_path;

    public File() {
    }

    public File(String title, String description, String url, String timestamp, String file_type, String file_path) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.timestamp = timestamp;
        this.file_type = file_type;
        this.file_path = file_path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public String getFile() {
        return file_path;
    }

    public void setFile(String file_path) {
        this.file_path = file_path;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getFile_type() {
        return file_type;
    }

    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }
}

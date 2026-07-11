package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static gitlet.Utils.sha1;

/** Represents a Gitlet commit object.
 *  @author minuet
 */
public class Commit implements Serializable {
    private String timestamp;
    private String parentId;
    private String secondParentId;
    private HashMap<String, String> trackedFiles;
    private String message;

    public Commit() {
        this.message = "initial commit";
        this.timestamp = "Thu Jan 1 00:00:00 1970 +0000";
        this.parentId = null;
        this.secondParentId = null;
        this.trackedFiles = new HashMap<>();
    }

    public Commit(String message, String parentId,
                  HashMap<String, String> trackedFiles) {
        this(message, parentId, null, trackedFiles);
    }

    public Commit(String message, String parentId, String secondParentId,
                  HashMap<String, String> trackedFiles) {
        this.message = message;
        Date date = new Date();
        SimpleDateFormat formatter =
                new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        this.timestamp = formatter.format(date);
        this.parentId = parentId;
        this.secondParentId = secondParentId;
        this.trackedFiles = new HashMap<>(trackedFiles);
    }

    public String getID() {
        String parentForHash = parentId == null ? "" : parentId;
        String secondParentForHash =
                secondParentId == null ? "" : secondParentId;
        return sha1(message, timestamp, parentForHash,
                secondParentForHash, trackedFiles.toString());
    }

    public HashMap<String, String> getTrackedFiles() {
        return trackedFiles;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getParentId() {
        return parentId;
    }

    public String getSecondParentId() {
        return secondParentId;
    }
}

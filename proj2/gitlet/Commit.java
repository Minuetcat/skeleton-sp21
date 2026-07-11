package gitlet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import static gitlet.Utils.*;
import java.io.Serializable;
import java.util.Locale;

/** Represents a gitlet commit object.
 *  A class about commit for gitlet.
 *  does at a high level.
 *
 *  @author minuet
 */
public class Commit implements Serializable {
    /**
     * Add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    private String timestamp;
    private String parentId;
    private HashMap<String, String> trackedFiles;

    /** The message of this Commit. */
    private String message;

    public Commit() {
        this.message = "initial commit";
        this.timestamp = "Thu Jan 1 00:00:00 1970 +0000";
        this.parentId = null;
        this.trackedFiles = new HashMap<>();
    }

    public Commit(String message, String parentId, HashMap<String, String> trackedFiles) {
        this.message = message;
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        this.timestamp = formatter.format(date);
        this.parentId = parentId;
        this.trackedFiles = new HashMap<>(trackedFiles);
    }

    public String getID() {
        String parentForHash = parentId == null ? "" : parentId;
        return sha1(message, timestamp, parentForHash, trackedFiles.toString());
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
}
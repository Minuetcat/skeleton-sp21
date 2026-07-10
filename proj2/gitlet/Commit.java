package gitlet;

// TODO: any imports you need here

import java.util.Date;
import java.util.HashMap;
import static gitlet.Utils.*;
import java.io.Serializable;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
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

    /* TODO: fill in the rest of this class. */
    public Commit() {
        this.message = "initial commit";
        this.timestamp = "Thu Jan 1 00:00:00 1970 +0000";
        this.parentId = null;
        this.trackedFiles = new HashMap<>();
    }

    public Commit(String message, String parentId, HashMap<String, String> trackedFiles) {
        this.message = message;
        Date date = new Date();
        this.timestamp = date.toString();
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
}

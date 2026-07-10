package gitlet;

import java.io.File;
import java.util.HashMap;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    public static final File STAGE_FILE = join(GITLET_DIR, "stage");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");


    /* TODO: fill in the rest of this class. */
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        GITLET_DIR.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        HashMap<String, String> stage = new HashMap<>();
        writeObject(STAGE_FILE, stage);
        Commit initialCommit = new Commit();
        String initialCommitId = initialCommit.getID();
        File initialCommitFile = join(COMMITS_DIR, initialCommitId);
        writeObject(initialCommitFile, initialCommit);
        writeContents(HEAD_FILE, initialCommitId);
    }

    public static void add(String fileName) {
        File fileToAdd = join(CWD, fileName);
        if (!fileToAdd.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        byte[] fileContent = readContents(fileToAdd);
        String blobId = sha1(fileContent);
        File blobFile = join(BLOBS_DIR, blobId);
        if (!blobFile.exists()) {
            writeContents(blobFile, fileContent);
        }
        HashMap<String, String> stage = readObject(STAGE_FILE, HashMap.class);
        stage.put(fileName, blobId);
        writeObject(STAGE_FILE, stage);
    }

    public static void commit(String message) {
        return;
    }

    public static void log() {
        return;
    }
}

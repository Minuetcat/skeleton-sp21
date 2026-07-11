package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static gitlet.Utils.*;

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
    public static final File STAGE_ADD_FILE = join(GITLET_DIR, "stageAdd");
    public static final File STAGE_REMOVE_FILE = join(GITLET_DIR, "stageRemove");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    public static final File BRANCHES_DIR = join(GITLET_DIR, "branches");

    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        GITLET_DIR.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        BRANCHES_DIR.mkdir();
        HashMap<String, String> stageAdd = new HashMap<>();
        HashSet<String> stageRemove = new HashSet<>();
        writeObject(STAGE_ADD_FILE, stageAdd);
        writeObject(STAGE_REMOVE_FILE, stageRemove);
        Commit initialCommit = new Commit();
        String initialCommitId = initialCommit.getID();
        File initialCommitFile = join(COMMITS_DIR, initialCommitId);
        writeObject(initialCommitFile, initialCommit);
        writeContents(HEAD_FILE, "master");
        File masterBranchFile = join(BRANCHES_DIR, "master");
        writeContents(masterBranchFile, initialCommitId);
    }

    public static void rm(String fileName) {
        HashMap<String, String> stageAdd = readObject(STAGE_ADD_FILE, HashMap.class);
        Commit headCommit = getHeadCommit();
        HashMap<String, String> trackedFiles = headCommit.getTrackedFiles();

        boolean stagedForAddition = stageAdd.containsKey(fileName);
        boolean trackedByHead = trackedFiles.containsKey(fileName);

        if (!stagedForAddition && !trackedByHead) {
            System.out.println("No reason to remove the file.");
            return;
        }
        if (stagedForAddition) {
            stageAdd.remove(fileName);
            writeObject(STAGE_ADD_FILE, stageAdd);
        }
        if (trackedByHead) {
            HashSet<String> stageRemove = readObject(STAGE_REMOVE_FILE, HashSet.class);
            stageRemove.add(fileName);
            writeObject(STAGE_REMOVE_FILE, stageRemove);
            File workingFile = join(CWD, fileName);
            restrictedDelete(workingFile);
        }
    }

    public static void add(String fileName) {
        File fileToAdd = join(CWD, fileName);
        if (!fileToAdd.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        byte[] fileContent = readContents(fileToAdd);
        String blobId = sha1(fileContent);
        HashMap<String, String> stageAdd = readObject(STAGE_ADD_FILE, HashMap.class);
        HashSet<String> stageRemove = readObject(STAGE_REMOVE_FILE, HashSet.class);
        if (stageRemove.contains(fileName)) {
            stageRemove.remove(fileName);
            writeObject(STAGE_REMOVE_FILE, stageRemove);
        }
        Commit headCommit = getHeadCommit();
        HashMap<String, String> trackedFiles = headCommit.getTrackedFiles();
        if (trackedFiles.containsKey(fileName)) {
            String trackedBlobId = trackedFiles.get(fileName);
            if (trackedBlobId.equals(blobId)) {
                stageAdd.remove(fileName);
                writeObject(STAGE_ADD_FILE, stageAdd);
                return;
            }
        }
        File blobFile = join(BLOBS_DIR, blobId);
        if (!blobFile.exists()) {
            writeContents(blobFile, fileContent);
        }
        stageAdd.put(fileName, blobId);
        writeObject(STAGE_ADD_FILE, stageAdd);
    }

    public static void commit(String message) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        HashMap<String, String> stageAdd = readObject(STAGE_ADD_FILE, HashMap.class);
        HashSet<String> stageRemove = readObject(STAGE_REMOVE_FILE, HashSet.class);
        if (stageAdd.isEmpty() && stageRemove.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        Commit headCommit = getHeadCommit();
        HashMap<String, String> trackedFiles = headCommit.getTrackedFiles();
        HashMap<String, String> newTrackedFiles = new HashMap<>(trackedFiles);
        newTrackedFiles.putAll(stageAdd);
        for (String fileName : stageRemove) {
            newTrackedFiles.remove(fileName);
        }
        Commit newCommit = new Commit(message, readContentsAsString(HEAD_FILE), newTrackedFiles);
        String newCommitId = newCommit.getID();
        File newCommitFile = join(COMMITS_DIR, newCommitId);
        writeObject(newCommitFile, newCommit);
        writeContents(HEAD_FILE, newCommitId);
        HashMap<String, String> emptyStageAdd = new HashMap<>();
        writeObject(STAGE_ADD_FILE, emptyStageAdd);
        HashSet<String> emptyStageRemove = new HashSet<>();
        writeObject(STAGE_REMOVE_FILE, emptyStageRemove);
    }

    public static void log() {
        String currentCommitId = readContentsAsString(HEAD_FILE);
        while (currentCommitId != null) {
            File currentCommitFile = join(COMMITS_DIR, currentCommitId);
            Commit currentCommit = readObject(currentCommitFile, Commit.class);

            System.out.println("===");
            System.out.println("commit " + currentCommitId);
            System.out.println("Date: " + currentCommit.getTimestamp());
            System.out.println(currentCommit.getMessage());
            System.out.println();

            currentCommitId = currentCommit.getParentId();
        }
    }

    public static void globalLog() {
        List<String> commitFileNames = plainFilenamesIn(COMMITS_DIR);
        for (String commitId : commitFileNames) {
            File commitfile = join(COMMITS_DIR, commitId);
            Commit commit = readObject(commitfile, Commit.class);

            System.out.println("===");
            System.out.println("commit " + commitId);
            System.out.println("Date: " + commit.getTimestamp());
            System.out.println(commit.getMessage());
            System.out.println();
        }
    }

    public static void find(String message) {
        List<String> commitFileNames = plainFilenamesIn(COMMITS_DIR);
        boolean found = false;
        for (String commitId : commitFileNames) {
            File commitfile = join(COMMITS_DIR, commitId);
            Commit commit = readObject(commitfile, Commit.class);
            if (commit.getMessage().equals(message)) {
                System.out.println(commitId);
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void checkoutFile(String fileName) {
        Commit headCommit = getHeadCommit();
        restoreFileFromCommit(headCommit, fileName);
        return;
    }

    public static void checkoutFileFromCommit(String commitId, String fileName) {
        File commitFile = join(COMMITS_DIR, commitId);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commit = readObject(commitFile, Commit.class);
        restoreFileFromCommit(commit, fileName);
        return;
    }

    private static void restoreFileFromCommit(Commit commit, String fileName) {
        HashMap<String, String> trackedFiles = commit.getTrackedFiles();
        if (!trackedFiles.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
        } else {
            String blobId = trackedFiles.get(fileName);
            File blobFile = join(BLOBS_DIR, blobId);
            byte[] fileContent = readContents(blobFile);
            File outputFile = join(CWD, fileName);
            writeContents(outputFile, fileContent);
        }
    }

    private static Commit getHeadCommit() {
        String currentBranchName = readContentsAsString(HEAD_FILE);
        File currentBranchFile = join(BRANCHES_DIR, currentBranchName);
        String headCommitId = readContentsAsString(currentBranchFile);
        File headCommitFile = join(COMMITS_DIR, headCommitId);
        return readObject(headCommitFile, Commit.class);
    }
}

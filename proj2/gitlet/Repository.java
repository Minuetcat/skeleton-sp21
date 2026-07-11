package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  a class about repository for gitlet.
 *  does at a high level.
 *
 *  @author minuet
 */
public class Repository {
    /**
     * Add instance variables here.
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

    public static void rm(String fileName) {
        HashMap<String, String> stageAdd = readObject(STAGE_ADD_FILE, HashMap.class);
        Commit headCommit = getHeadCommit();
        HashMap<String, String> trackedFiles = headCommit.getTrackedFiles();

        boolean stageForAddition = stageAdd.containsKey(fileName);
        boolean trackedByHead = trackedFiles.containsKey(fileName);

        if (!stageForAddition && !trackedByHead) {
            System.out.println("No reason to remove the file.");
            return;
        }
        if (stageForAddition) {
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
        Commit newCommit = new Commit(message, readContentsAsString(getCurrentBranchFile()), newTrackedFiles);
        String newCommitId = newCommit.getID();
        File newCommitFile = join(COMMITS_DIR, newCommitId);
        writeObject(newCommitFile, newCommit);
        writeContents(getCurrentBranchFile(), newCommitId);
        HashMap<String, String> emptyStageAdd = new HashMap<>();
        writeObject(STAGE_ADD_FILE, emptyStageAdd);
        HashSet<String> emptyStageRemove = new HashSet<>();
        writeObject(STAGE_REMOVE_FILE, emptyStageRemove);
    }

    public static void log() {
        String currentCommitId = getCurrentCommitId();
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
        for (String commitFileName : commitFileNames) {
            File commitFile = join(COMMITS_DIR, commitFileName);
            Commit commit = readObject(commitFile, Commit.class);

            System.out.println("===");
            System.out.println("commit " + commitFileName);
            System.out.println("Date: " + commit.getTimestamp());
            System.out.println(commit.getMessage());
            System.out.println();
        }
    }

    public static void find(String message) {
        List<String> commitFileNames = plainFilenamesIn(COMMITS_DIR);
        boolean found = false;
        for (String commitFileName : commitFileNames) {
            File commitFile = join(COMMITS_DIR, commitFileName);
            Commit commit = readObject(commitFile, Commit.class);
            if (commit.getMessage().equals(message)) {
                System.out.println(commitFileName);
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void status() {
        System.out.println("=== Branches ===");
        List<String> branchNames = plainFilenamesIn(BRANCHES_DIR);
        for (String branchName : branchNames) {
            if (branchName.equals(getCurrentBranchName())) {
                System.out.print("*");
            }
            System.out.println(branchName);
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        HashMap<String, String> stageAdd = readObject(STAGE_ADD_FILE, HashMap.class);
        List<String> addFileNames =  new ArrayList<>(stageAdd.keySet());
        Collections.sort(addFileNames);
        for (String addFileName : addFileNames) {
            System.out.println(addFileName);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        HashSet<String> stageRemove = readObject(STAGE_REMOVE_FILE, HashSet.class);
        List<String> removeFileNames = new ArrayList<>(stageRemove);
        Collections.sort(removeFileNames);
        for (String removeFileName : removeFileNames) {
            System.out.println(removeFileName);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public static void checkoutFile(String fileName) {
        Commit headCommit = getHeadCommit();
        restoreFileFromCommit(headCommit, fileName);
        return;
    }

    public static void checkoutFileFromCommit(String commitId, String fileName) {
        String fullCommitId = findFullCommitId(commitId);
        if (fullCommitId == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        File commitFile = join(COMMITS_DIR, fullCommitId);
        Commit commit = readObject(commitFile, Commit.class);
        restoreFileFromCommit(commit, fileName);
        return;
    }

    public static void checkoutBranch(String branchName) {
        File branchFile = join(BRANCHES_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            return;
        }
        if (branchName.equals(getCurrentBranchName())) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        String targetCommitId = readContentsAsString(branchFile);
        File targetCommitFile = join(COMMITS_DIR, targetCommitId);
        Commit targetCommit = readObject(targetCommitFile, Commit.class);
        Commit currentCommit = getHeadCommit();
        HashMap<String, String> targetTrackedFiles = targetCommit.getTrackedFiles();
        HashMap<String, String> currentTrackedFiles = currentCommit.getTrackedFiles();

        List<String> workingFileNames = plainFilenamesIn(CWD);
        for (String fileName : workingFileNames) {
            boolean trackedByCurrent = currentTrackedFiles.containsKey(fileName);
            boolean trackedByTarget = targetTrackedFiles.containsKey(fileName);

            if (!trackedByCurrent && trackedByTarget) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }

        for (String fileName : targetTrackedFiles.keySet()) {
            restoreFileFromCommit(targetCommit, fileName);
        }

        for (String fileName : currentTrackedFiles.keySet()) {
            if (!targetTrackedFiles.containsKey(fileName)) {
                File workingFile = join(CWD, fileName);
                restrictedDelete(workingFile);
            }
        }

        writeContents(HEAD_FILE, branchName);
        HashMap<String, String> emptyStageAdd = new HashMap<>();
        writeObject(STAGE_ADD_FILE, emptyStageAdd);
        HashSet<String> emptyStageRemove = new HashSet<>();
        writeObject(STAGE_REMOVE_FILE, emptyStageRemove);
    }

    public static void branch(String branchName) {
        File branchFile = join(BRANCHES_DIR, branchName);
        if (branchFile.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        writeContents(branchFile, getCurrentCommitId());
    }

    public static void rmBranch(String branchName) {
        File branchFile = join(BRANCHES_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (branchName.equals(getCurrentBranchName())) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        branchFile.delete();
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
        String headCommitId = readContentsAsString(getCurrentBranchFile());
        File headCommitFile = join(COMMITS_DIR, headCommitId);
        return readObject(headCommitFile, Commit.class);
    }

    private static String getCurrentBranchName() {
        return readContentsAsString(HEAD_FILE);
    }

    private static File getCurrentBranchFile() {
        return join(BRANCHES_DIR, getCurrentBranchName());
    }

    private static String getCurrentCommitId() {
        return readContentsAsString(getCurrentBranchFile());
    }

    private static String findFullCommitId(String commitId) {
        File exactCommitFile = join(COMMITS_DIR, commitId);
        if (exactCommitFile.exists()) {
            return commitId;
        }
        List<String> commitFileNames = plainFilenamesIn(COMMITS_DIR);
        for (String fullCommitId : commitFileNames) {
            if (fullCommitId.startsWith(commitId)) {
                return fullCommitId;
            }
        }
        return null;
    }

    public static void reset(String commitId) {
        String fullCommitId = findFullCommitId(commitId);
        if (fullCommitId == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        File targetCommitFile = join(COMMITS_DIR, fullCommitId);
        Commit targetCommit = readObject(targetCommitFile, Commit.class);
        Commit currentCommit = getHeadCommit();
        HashMap<String, String> targetTrackedFiles = targetCommit.getTrackedFiles();
        HashMap<String, String> currentTrackedFiles = currentCommit.getTrackedFiles();

        List<String> workingFileNames = plainFilenamesIn(CWD);
        for (String fileName : workingFileNames) {
            boolean trackedByCurrent = currentTrackedFiles.containsKey(fileName);
            boolean trackedByTarget = targetTrackedFiles.containsKey(fileName);

            if (!trackedByCurrent && trackedByTarget) {
                System.out.println(
                        "There is an untracked file in the way; "
                                + "delete it, or add and commit it first.");
                return;
            }
        }

        for (String fileName : targetTrackedFiles.keySet()) {
            restoreFileFromCommit(targetCommit, fileName);
        }

        for (String fileName : currentTrackedFiles.keySet()) {
            if (!targetTrackedFiles.containsKey(fileName)) {
                File workingFile = join(CWD, fileName);
                restrictedDelete(workingFile);
            }
        }

        File currentBranchFile = getCurrentBranchFile();
        writeContents(currentBranchFile, fullCommitId);
        HashMap<String, String> emptyStageAdd = new HashMap<>();
        writeObject(STAGE_ADD_FILE, emptyStageAdd);
        HashSet<String> emptyStageRemove = new HashSet<>();
        writeObject(STAGE_REMOVE_FILE, emptyStageRemove);
    }

    public static void merge(String branchName) {
        HashMap<String, String> stageAdd = readObject(STAGE_ADD_FILE, HashMap.class);
        HashSet<String> stageRemove = readObject(STAGE_REMOVE_FILE, HashSet.class);
        if (!stageAdd.isEmpty() || !stageRemove.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        File givenBranchFile = join(BRANCHES_DIR, branchName);
        if (!givenBranchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        String currentBranchName = getCurrentBranchName();
        if (currentBranchName.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        String givenCommitId = readContentsAsString(givenBranchFile);
        File givenCommitFile = join(COMMITS_DIR, givenCommitId);
        Commit givenCommit = readObject(givenCommitFile, Commit.class);
        Commit currentCommit = getHeadCommit();
        HashMap<String, String> targetTrackedFiles = givenCommit.getTrackedFiles();
        HashMap<String, String> currentTrackedFiles = currentCommit.getTrackedFiles();
        List<String> workingFileNames = plainFilenamesIn(CWD);
        for (String fileName : workingFileNames) {
            boolean trackedByCurrent = currentTrackedFiles.containsKey(fileName);
            boolean trackedByTarget = targetTrackedFiles.containsKey(fileName);

            if (!trackedByCurrent && trackedByTarget) {
                System.out.println(
                        "There is an untracked file in the way; "
                                + "delete it, or add and commit it first.");
                return;
            }

        }
        String currentCommitId = getCurrentCommitId();
        String splitPointId = findSplitPoint(currentCommitId, givenCommitId);

        if (splitPointId.equals(givenCommitId)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }

        if (splitPointId.equals(currentCommitId)) {
            for (String fileName : targetTrackedFiles.keySet()) {
                restoreFileFromCommit(givenCommit, fileName);
            }
            for (String fileName : currentTrackedFiles.keySet()) {
                if (!targetTrackedFiles.containsKey(fileName)) {
                    File workingFile = join(CWD, fileName);
                    restrictedDelete(workingFile);
                }
            }
            File currentBranchFile = getCurrentBranchFile();
            writeContents(currentBranchFile, givenCommitId);
            HashMap<String, String> emptyStageAdd = new HashMap<>();
            writeObject(STAGE_ADD_FILE, emptyStageAdd);
            HashSet<String> emptyStageRemove = new HashSet<>();
            writeObject(STAGE_REMOVE_FILE, emptyStageRemove);
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        File splitPointFile = join(COMMITS_DIR, splitPointId);
        Commit splitPointCommit = readObject(splitPointFile, Commit.class);
        HashMap<String, String> splitTrackedFiles = splitPointCommit.getTrackedFiles();
        HashSet<String> allFileNames = new HashSet<>();
        allFileNames.addAll(splitTrackedFiles.keySet());
        allFileNames.addAll(currentTrackedFiles.keySet());
        allFileNames.addAll(targetTrackedFiles.keySet());

        boolean conflictOccurred = false;

        for (String fileName : allFileNames) {
            String splitBlobId = splitTrackedFiles.get(fileName);
            String currentBlobId = currentTrackedFiles.get(fileName);
            String givenBlobId = targetTrackedFiles.get(fileName);

            boolean currentChanged =
                    !Objects.equals(splitBlobId, currentBlobId);
            boolean givenChanged =
                    !Objects.equals(splitBlobId, givenBlobId);
            boolean currentGivenDifferent =
                    !Objects.equals(currentBlobId, givenBlobId);

            if (!currentChanged && givenChanged) {
                if (givenBlobId == null) {
                    rm(fileName);
                } else {
                    restoreFileFromCommit(givenCommit, fileName);
                    add(fileName);
                }
            } else if (currentChanged
                    && givenChanged
                    && currentGivenDifferent) {
                conflictOccurred = true;

                String currentContent = "";
                if (currentBlobId != null) {
                    File currentBlobFile = join(BLOBS_DIR, currentBlobId);
                    currentContent = readContentsAsString(currentBlobFile);
                }

                String givenContent = "";
                if (givenBlobId != null) {
                    File givenBlobFile = join(BLOBS_DIR, givenBlobId);
                    givenContent = readContentsAsString(givenBlobFile);
                }

                String conflictContent =
                        "<<<<<<< HEAD\n"
                                + currentContent
                                + "=======\n"
                                + givenContent
                                + ">>>>>>>\n";

                File workingFile = join(CWD, fileName);
                writeContents(workingFile, conflictContent);
                add(fileName);
            }
        }
        String mergeMessage =
                "Merged " + branchName + " into "
                        + currentBranchName + ".";

        commit(mergeMessage);

        if (conflictOccurred) {
            System.out.println("Encountered a merge conflict.");
        }

    }

    private static String findSplitPoint(String currentCommitId, String givenCommitId) {
        HashSet<String> currentAncestors = new HashSet<>();
        while (currentCommitId != null) {
            currentAncestors.add(currentCommitId);
            File currentCommitFile = join(COMMITS_DIR, currentCommitId);
            Commit currentCommit = readObject(currentCommitFile, Commit.class);
            currentCommitId = currentCommit.getParentId();
        }

        String searchCommitId = givenCommitId;
        while (searchCommitId != null) {
            if (currentAncestors.contains(searchCommitId)) {
                return searchCommitId;
            }
            File searchCommitFile = join(COMMITS_DIR, searchCommitId);
            Commit searchCommit = readObject(searchCommitFile, Commit.class);
            searchCommitId = searchCommit.getParentId();
        }

        return null;
    }
}
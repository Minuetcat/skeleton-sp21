package gitlet;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static gitlet.Utils.join;
import static gitlet.Utils.plainFilenamesIn;
import static gitlet.Utils.readContents;
import static gitlet.Utils.readContentsAsString;
import static gitlet.Utils.readObject;
import static gitlet.Utils.restrictedDelete;
import static gitlet.Utils.sha1;
import static gitlet.Utils.writeContents;
import static gitlet.Utils.writeObject;

/** Represents a Gitlet repository.
 *
 * @author minuet
 */
public class Repository {
    /** Current working directory. */
    public static final File CWD =
            new File(System.getProperty("user.dir"));
    /** Main Gitlet metadata directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** Directory containing serialized commits. */
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    /** Directory containing file-content blobs. */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    /** File containing the addition staging area. */
    public static final File STAGE_ADD_FILE =
            join(GITLET_DIR, "stageAdd");
    /** File containing the removal staging area. */
    public static final File STAGE_REMOVE_FILE =
            join(GITLET_DIR, "stageRemove");
    /** File containing the current branch name. */
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    /** Directory containing branch pointers. */
    public static final File BRANCHES_DIR =
            join(GITLET_DIR, "branches");

    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println(
                    "A Gitlet version-control system already exists "
                            + "in the current directory.");
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
        Commit newCommit = new Commit(
                message,
                readContentsAsString(getCurrentBranchFile()),
                newTrackedFiles);
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

            printCommit(currentCommitId, currentCommit);
            currentCommitId = currentCommit.getParentId();
        }
    }

    public static void globalLog() {
        List<String> commitFileNames = plainFilenamesIn(COMMITS_DIR);
        for (String commitFileName : commitFileNames) {
            File commitFile = join(COMMITS_DIR, commitFileName);
            Commit commit = readObject(commitFile, Commit.class);
            printCommit(commitFileName, commit);
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
        HashMap<String, String> stageAdd =
                readObject(STAGE_ADD_FILE, HashMap.class);
        HashSet<String> stageRemove =
                readObject(STAGE_REMOVE_FILE, HashSet.class);
        if (!stageAdd.isEmpty() || !stageRemove.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }

        File givenBranchFile = join(BRANCHES_DIR, branchName);
        if (!givenBranchFile.exists()) {
            System.out.println(
                    "A branch with that name does not exist.");
            return;
        }

        String currentBranchName = getCurrentBranchName();
        if (currentBranchName.equals(branchName)) {
            System.out.println(
                    "Cannot merge a branch with itself.");
            return;
        }

        String currentCommitId = getCurrentCommitId();
        String givenCommitId =
                readContentsAsString(givenBranchFile);
        Commit currentCommit = getHeadCommit();
        Commit givenCommit = readObject(
                join(COMMITS_DIR, givenCommitId), Commit.class);

        HashMap<String, String> currentTrackedFiles =
                currentCommit.getTrackedFiles();
        HashMap<String, String> givenTrackedFiles =
                givenCommit.getTrackedFiles();

        if (hasUntrackedConflict(
                currentTrackedFiles, givenTrackedFiles)) {
            System.out.println(
                    "There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
            return;
        }

        String splitPointId =
                findSplitPoint(currentCommitId, givenCommitId);
        if (splitPointId.equals(givenCommitId)) {
            System.out.println(
                    "Given branch is an ancestor of the current branch.");
            return;
        }
        if (splitPointId.equals(currentCommitId)) {
            updateWorkingDirectory(currentCommit, givenCommit);
            writeContents(getCurrentBranchFile(), givenCommitId);
            clearStage();
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        Commit splitPointCommit = readObject(
                join(COMMITS_DIR, splitPointId), Commit.class);
        boolean conflictOccurred = mergeFiles(
                splitPointCommit, currentCommit, givenCommit);

        String mergeMessage =
                "Merged " + branchName + " into "
                        + currentBranchName + ".";
        createMergeCommit(
                mergeMessage, currentCommitId, givenCommitId);

        if (conflictOccurred) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    private static boolean hasUntrackedConflict(
            HashMap<String, String> currentTrackedFiles,
            HashMap<String, String> targetTrackedFiles) {
        List<String> workingFileNames = plainFilenamesIn(CWD);
        for (String fileName : workingFileNames) {
            boolean trackedByCurrent =
                    currentTrackedFiles.containsKey(fileName);
            boolean trackedByTarget =
                    targetTrackedFiles.containsKey(fileName);
            if (!trackedByCurrent && trackedByTarget) {
                return true;
            }
        }
        return false;
    }

    private static boolean mergeFiles(
            Commit splitPointCommit,
            Commit currentCommit,
            Commit givenCommit) {
        HashMap<String, String> splitTrackedFiles =
                splitPointCommit.getTrackedFiles();
        HashMap<String, String> currentTrackedFiles =
                currentCommit.getTrackedFiles();
        HashMap<String, String> givenTrackedFiles =
                givenCommit.getTrackedFiles();

        HashSet<String> allFileNames = new HashSet<>();
        allFileNames.addAll(splitTrackedFiles.keySet());
        allFileNames.addAll(currentTrackedFiles.keySet());
        allFileNames.addAll(givenTrackedFiles.keySet());

        boolean conflictOccurred = false;
        for (String fileName : allFileNames) {
            String splitBlobId = splitTrackedFiles.get(fileName);
            String currentBlobId =
                    currentTrackedFiles.get(fileName);
            String givenBlobId = givenTrackedFiles.get(fileName);

            boolean currentChanged =
                    !Objects.equals(splitBlobId, currentBlobId);
            boolean givenChanged =
                    !Objects.equals(splitBlobId, givenBlobId);
            boolean versionsDifferent =
                    !Objects.equals(currentBlobId, givenBlobId);

            if (!currentChanged && givenChanged) {
                applyGivenVersion(
                        fileName, givenBlobId, givenCommit);
            } else if (currentChanged
                    && givenChanged && versionsDifferent) {
                writeConflictFile(
                        fileName, currentBlobId, givenBlobId);
                add(fileName);
                conflictOccurred = true;
            }
        }
        return conflictOccurred;
    }

    private static void applyGivenVersion(
            String fileName,
            String givenBlobId,
            Commit givenCommit) {
        if (givenBlobId == null) {
            rm(fileName);
        } else {
            restoreFileFromCommit(givenCommit, fileName);
            add(fileName);
        }
    }

    private static void updateWorkingDirectory(Commit currentCommit,
                                               Commit targetCommit) {
        HashMap<String, String> currentTrackedFiles =
                currentCommit.getTrackedFiles();
        HashMap<String, String> targetTrackedFiles =
                targetCommit.getTrackedFiles();

        for (String fileName : targetTrackedFiles.keySet()) {
            restoreFileFromCommit(targetCommit, fileName);
        }
        for (String fileName : currentTrackedFiles.keySet()) {
            if (!targetTrackedFiles.containsKey(fileName)) {
                restrictedDelete(join(CWD, fileName));
            }
        }
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

    private static void writeConflictFile(String fileName,
                                          String currentBlobId,
                                          String givenBlobId) {
        String currentContent = readBlobAsString(currentBlobId);
        String givenContent = readBlobAsString(givenBlobId);
        String conflictContent =
                "<<<<<<< HEAD\n"
                        + currentContent
                        + "=======\n"
                        + givenContent
                        + ">>>>>>>\n";
        writeContents(join(CWD, fileName), conflictContent);
    }

    private static String readBlobAsString(String blobId) {
        if (blobId == null) {
            return "";
        }
        return readContentsAsString(join(BLOBS_DIR, blobId));
    }

    private static void createMergeCommit(String message,
                                          String firstParentId,
                                          String secondParentId) {
        HashMap<String, String> stageAdd =
                readObject(STAGE_ADD_FILE, HashMap.class);
        HashSet<String> stageRemove =
                readObject(STAGE_REMOVE_FILE, HashSet.class);

        HashMap<String, String> newTrackedFiles =
                new HashMap<>(getHeadCommit().getTrackedFiles());
        newTrackedFiles.putAll(stageAdd);
        for (String fileName : stageRemove) {
            newTrackedFiles.remove(fileName);
        }

        Commit mergeCommit = new Commit(
                message, firstParentId, secondParentId, newTrackedFiles);
        String mergeCommitId = mergeCommit.getID();
        writeObject(join(COMMITS_DIR, mergeCommitId), mergeCommit);
        writeContents(getCurrentBranchFile(), mergeCommitId);
        clearStage();
    }

    private static String findSplitPoint(String currentCommitId,
                                         String givenCommitId) {
        HashMap<String, Integer> currentDistances =
                getAncestorDistances(currentCommitId);
        HashMap<String, Integer> givenDistances =
                getAncestorDistances(givenCommitId);

        String bestSplitPointId = null;
        int bestDistance = Integer.MAX_VALUE;

        for (String commitId : currentDistances.keySet()) {
            if (givenDistances.containsKey(commitId)) {
                int totalDistance = currentDistances.get(commitId)
                        + givenDistances.get(commitId);
                if (totalDistance < bestDistance) {
                    bestDistance = totalDistance;
                    bestSplitPointId = commitId;
                }
            }
        }
        return bestSplitPointId;
    }

    private static HashMap<String, Integer> getAncestorDistances(
            String startCommitId) {
        HashMap<String, Integer> distances = new HashMap<>();
        ArrayDeque<String> commitQueue = new ArrayDeque<>();
        ArrayDeque<Integer> distanceQueue = new ArrayDeque<>();
        commitQueue.add(startCommitId);
        distanceQueue.add(0);

        while (!commitQueue.isEmpty()) {
            String commitId = commitQueue.remove();
            int distance = distanceQueue.remove();
            if (distances.containsKey(commitId)) {
                continue;
            }
            distances.put(commitId, distance);

            Commit commit = readObject(
                    join(COMMITS_DIR, commitId), Commit.class);
            String firstParentId = commit.getParentId();
            String secondParentId = commit.getSecondParentId();

            if (firstParentId != null) {
                commitQueue.add(firstParentId);
                distanceQueue.add(distance + 1);
            }
            if (secondParentId != null) {
                commitQueue.add(secondParentId);
                distanceQueue.add(distance + 1);
            }
        }
        return distances;
    }

    private static void printCommit(String commitId, Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commitId);
        if (commit.getSecondParentId() != null) {
            String firstParent = commit.getParentId().substring(0, 7);
            String secondParent =
                    commit.getSecondParentId().substring(0, 7);
            System.out.println(
                    "Merge: " + firstParent + " " + secondParent);
        }
        System.out.println("Date: " + commit.getTimestamp());
        System.out.println(commit.getMessage());
        System.out.println();
    }

    private static void clearStage() {
        writeObject(STAGE_ADD_FILE, new HashMap<String, String>());
        writeObject(STAGE_REMOVE_FILE, new HashSet<String>());
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

    private static Commit getHeadCommit() {
        String headCommitId = readContentsAsString(getCurrentBranchFile());
        File headCommitFile = join(COMMITS_DIR, headCommitId);
        return readObject(headCommitFile, Commit.class);
    }

    private static String getCurrentCommitId() {
        return readContentsAsString(getCurrentBranchFile());
    }

    private static String getCurrentBranchName() {
        return readContentsAsString(HEAD_FILE);
    }

    private static File getCurrentBranchFile() {
        return join(BRANCHES_DIR, getCurrentBranchName());
    }

}
package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author minuet
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        if (!firstArg.equals("init") && !Repository.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        switch(firstArg) {
            case "init":
                if(args.length != 1) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.init();
                break;

            case "log":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.log();
                break;

            case "global-log":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.globalLog();
                break;

            case "status":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.status();
                break;

            case "rm":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.rm(args[1]);
                break;

            case "add":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.add(args[1]);
                break;

            case "find":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.find(args[1]);
                break;

            case "commit":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.commit(args[1]);
                break;

            case "branch":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.branch(args[1]);
                break;

            case "rm-branch":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.rmBranch(args[1]);
                break;

            case "reset":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.reset(args[1]);
                break;

            case "checkout":
                if (args.length == 3 && args[1].equals("--")) {
                    Repository.checkoutFile(args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    Repository.checkoutFileFromCommit(args[1], args[3]);
                } else if (args.length == 2) {
                    Repository.checkoutBranch(args[1]);
                } else {
                    System.out.println("Incorrect operands.");
                }
                break;

            default:
                System.out.println("No command with that name exists.");
                return;
        }
    }
}
package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
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
                // TODO: handle the `init` command
                if(args.length != 1) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                if(args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.add(args[1]);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                if(args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.commit(args[1]);
                break;
            case "log":
                if(args.length != 1) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.log();
                break;
//            case "checkout":
//                if(args.length != 2) {
//                    System.out.println("Incorrect operands.");
//                    return;
//                }
//                Repository.checkout(args[1]);
//                break;
            default:
                System.out.println("No command with that name exists.");
                return;
        }
    }
}

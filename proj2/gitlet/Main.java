package gitlet;

import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Li
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void checkDir()
    {
        if (Repository.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                Repository.init();
                break;
            case "add":
                Repository.add(args[1]);
                break;
            case "commit":
                Repository.commit(args[1]);
                break;
            case "checkout":
                switch (args.length) {
                    case 2:
                        Repository.checkout(args[1]);
                        break;
                    case 3:
                        Repository.checkout(args[1], args[2]);
                        break;
                    case 4:
                        Repository.checkout(args[1], args[2], args[3]);
                        break;
                }
                break;
            case "log":
                Repository.log();
                break;
            case "rm":
                Repository.rm(args[1]);
                break;
            case "global-log":
                Repository.global_log();
                break;
            case "find":
                Repository.find(args[1]);
                break;
            case "status":
                Repository.status();
                break;
            case "branch":
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                Repository.rm_branch(args[1]);
                break;
            case "reset":
                Repository.reset(args[1]);
                break;
            case "merge":
                Repository.merge(args[1]);
                break;
            case "debug":
                Repository.debug();
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
        }
    }
}

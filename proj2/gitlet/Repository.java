package gitlet;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Li
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    public static final File TREES_DIR = join(GITLET_DIR, "trees");
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    public static final File BRANCHES_DIR = join(GITLET_DIR, "branches");
    public static final File STAGES_DIR = join(GITLET_DIR, "stages");
    public static final File HEAD_FILE = join(GITLET_DIR, "head");


    public static boolean exists()
    {
        return GITLET_DIR.exists() ||
                BLOBS_DIR.exists() ||
                TREES_DIR.exists() ||
                COMMITS_DIR.exists() ||
                BRANCHES_DIR.exists() ||
                STAGES_DIR.exists() ||
                HEAD_FILE.exists();
    }

    private static void initBranch(String commit_name) throws IOException {
        final File master = Utils.join(BRANCHES_DIR, "master");
        master.createNewFile();
        Utils.writeObject(master, commit_name);
    }

    private static void store(Commit commit, List<String> files) throws IOException {
//        Map<String, String> blobs = new HashMap<>();
        if (files != null)
            for (final String file : files) {
                File f = Utils.join(STAGES_DIR, file);
                String fhash = Utils.sha1(Utils.readContentsAsString(f));
                File blob = Utils.join(BLOBS_DIR, fhash);
                Utils.writeContents(blob, Utils.readContentsAsString(f));
//                Utils.restrictedDelete(f);
                String relpath = getRelPath(STAGES_DIR, f);
                commit.putFile(relpath, fhash);
                f.delete();
            }

        final String commit_name = commit.hash();
        final File commit_file = new File(COMMITS_DIR, commit_name);
        Utils.writeObject(commit_file, commit);
        Utils.writeObject(HEAD_FILE, commit_name);
    }

    public static void init() throws IOException {
        final String init_message = "initial commit";
        if(exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        GITLET_DIR.mkdirs();
        BLOBS_DIR.mkdirs();
        TREES_DIR.mkdirs();
        COMMITS_DIR.mkdirs();
        BRANCHES_DIR.mkdirs();
        STAGES_DIR.mkdirs();
        HEAD_FILE.createNewFile();
        final Commit c = new Commit(init_message, null, null);
        initBranch(c.hash());
        store(c, null);
    }

    public static Commit current() throws IOException {
        final String commit_name = Utils.readObject(HEAD_FILE, String.class);
        final File commit_file = Utils.join(COMMITS_DIR, commit_name);
        final Commit result = Utils.readObject(commit_file, Commit.class);
        return result;
    }

    public static String currentBranch() throws IOException {
        return null;
    }

    public static Commit getCommitById(String id) {
        String commit_name = null;
        if (id.length() == 40) {
            commit_name = id;
        } else if (id.length() == 6) {
            List<String> files = Utils.plainFilenamesIn(COMMITS_DIR);
            var fs = files.stream().filter(s -> s.startsWith(id)).collect(Collectors.toList());
            if(fs.size() == 1) {
                commit_name = String.valueOf(fs.get(0));
            }
        }
        final File commit_file = Utils.join(COMMITS_DIR, commit_name);
        final Commit result = Utils.readObject(commit_file, Commit.class);
        return result;
    }

    private static boolean filecompare(File a, File b) {
        String f1 = Utils.readContentsAsString(a);
        String f2 = Utils.readContentsAsString(b);
        return f1.equals(f2);
    }

    private static boolean filecompare(String a, File b) {
        return filecompare(new File(a), b);
    }

    private static boolean filecompare(File a, String b) {
        return filecompare(a, new File(b));
    }

    private static boolean filecompare(String a, String b) {
        return filecompare(new File(a), new File(b));
    }

    private static String getRelPath(File a, File b) {
        String path1 = a.getAbsolutePath();
        String path2 = b.getAbsolutePath();
        if (!path2.startsWith(path1)) {
            return null;
        }
        return path2.substring(path1.length() + 1);
    }

    private static String getRelPath(String a, String b) {
        return getRelPath(new File(a), new File(b));
    }

    private static String getRelPath(File f) {
        String a = CWD.getAbsolutePath();
        return getRelPath(new File(a), f);
    }

    private static String getRelPath(String f) {
        return getRelPath(new File(f));
    }

    public static void add(String file_path) throws IOException {
        File f = new File(file_path);
        if(!f.exists()) {
            System.out.println("File does not exist: ");
            return;
        }
        String rel_path = getRelPath(f);
        final Commit cur = current();
        Map<String, String> files = cur.getFiles();
        File staged = Utils.join(STAGES_DIR, rel_path);
        if (staged.exists()) {
            if (filecompare(staged, f)) {
                return;
            }
        } else {
            if (files != null && files.containsKey(rel_path)) {
                File blob = Utils.join(BLOBS_DIR, files.get(rel_path));
                if (filecompare(blob, f)) {
                    return;
                }
            }
            staged.createNewFile();
        }

        Utils.writeContents(staged, Utils.readContents(f));
    }

    public static void commit(String message) throws IOException {
        Commit cur = current();
        List<String> files = Utils.plainFilenamesIn(STAGES_DIR);
        Map<String, String> blobs = new HashMap<String, String>();
        Commit nc = new Commit(message, Arrays.asList(cur.hash()), blobs);
        store(nc, files);
    }

    public static void do_checkout(String commit_id, String filename) {

    }

    public static void checkout(String branch) {

    }

    public static void checkout(String unused, String filename) throws IOException {
        if (!unused.equals("--")) {
            return;
        }
        Commit cur = current();
        var files = cur.getFiles();
        if(files != null && files.containsKey(filename)) {
            String blob_path = files.get(filename);
            File blob = Utils.join(BLOBS_DIR, blob_path);
            File f = Utils.join(CWD, filename);
            if (!f.exists())
                f.createNewFile();
            Utils.writeContents(f, Utils.readContentsAsString(blob));
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    public static void checkout(String commit_id, String unused, String filename) throws IOException {
        if (!unused.equals("--")) {
            return;
        }
        Commit cur = getCommitById(commit_id);
        var files = cur.getFiles();
        if(files != null && files.containsKey(filename)) {
            String blob_path = files.get(filename);
            File blob = Utils.join(BLOBS_DIR, blob_path);
            File f = Utils.join(CWD, filename);
            if (!f.exists())
                f.createNewFile();
            Utils.writeContents(f, Utils.readContentsAsString(blob));
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    public static void log() throws IOException {
        Commit cur = current();

        do {
            System.out.println("===");
            System.out.println("commit " + cur.hash());
            String outs = new Formatter(Locale.US).format("%1$ta %1$tb %1$te %1$tT %1$tY %1$tz", cur.getDate()).toString();
            System.out.println("Date: " + outs);
            System.out.println(cur.getMessage());
            System.out.println();
            if (cur.getPrevious() != null) {
                String prev = cur.getPrevious().get(0);
                File prev_file = Utils.join(COMMITS_DIR, prev);
                cur = Utils.readObject(prev_file, Commit.class);
            } else {
                cur = null;
            }
        } while (cur != null);
    }
}

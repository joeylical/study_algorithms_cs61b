package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
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
    public static final File STAGERM_FILE = join(GITLET_DIR, "stagerm");

    public static boolean exists()
    {
        return GITLET_DIR.exists()
                || BLOBS_DIR.exists()
                || TREES_DIR.exists()
                || COMMITS_DIR.exists()
                || BRANCHES_DIR.exists()
                || STAGES_DIR.exists()
                || HEAD_FILE.exists()
                || STAGERM_FILE.exists();
    }

    private static void initBranch(String CommitName) {
        final File master = Utils.join(BRANCHES_DIR, "master");
        try {
            master.createNewFile();
        } catch (IOException e) {
            Utils.error(e.getMessage());
        }
        Utils.writeObject(master, CommitName);
    }

    private static void store(Commit commit, List<String> files) {
        if (files != null) {
            for (final String file : files) {
                File f = Utils.join(STAGES_DIR, file);
                String fhash = Utils.sha1(Utils.readContentsAsString(f));
                File blob = Utils.join(BLOBS_DIR, fhash);
                Utils.writeContents(blob, Utils.readContentsAsString(f));
                String relpath = getRelPath(STAGES_DIR, f);
                commit.putFile(relpath, fhash);
                f.delete();
            }
        }
        // stage rm

        final String commitName = commit.hash();
        final File commitFile = new File(COMMITS_DIR, commitName);
        Utils.writeObject(commitFile, commit);
        HeadInfo hi = new HeadInfo(commitName, currentBranch());
        Utils.writeObject(HEAD_FILE, hi);
        var currentBranch = currentBranch();
        File branchFile = Utils.join(BRANCHES_DIR, currentBranch);
        Utils.writeContents(branchFile, commit.hash());
    }

    public static void init() {
        final String init_message = "initial commit";
        if(exists()) {
            System.out.println(
                "A Gitlet version-control system already exists in the current directory.");
            return;
        }
        GITLET_DIR.mkdirs();
        BLOBS_DIR.mkdirs();
        TREES_DIR.mkdirs();
        COMMITS_DIR.mkdirs();
        BRANCHES_DIR.mkdirs();
        STAGES_DIR.mkdirs();
        try {
            STAGERM_FILE.createNewFile();
            HEAD_FILE.createNewFile();
            final Commit c = new Commit(init_message, null, new TreeMap<String, String>());
            initBranch(c.hash());
            store(c, new ArrayList<>());
        } catch(IOException e) {
            System.out.println(
               "A Gitlet version-control system already exists in the current directory.");
        }

    }

    public static Commit current() {
        final var hi = Utils.readObject(HEAD_FILE, HeadInfo.class);
        final String commitName = hi.CommitName;
        final File commitFile = Utils.join(COMMITS_DIR, commitName);
        final Commit result = Utils.readObject(commitFile, Commit.class);
        return result;
    }

    public static String currentBranch() {
        if (HEAD_FILE.exists()) {
            if (HEAD_FILE.length() > 0) {
                final HeadInfo hi = Utils.readObject(HEAD_FILE, HeadInfo.class);
                return hi.BranchName;
            } else {
                return "master";
            }
        }
        return null;
    }

    public static Commit getCommitById(String id) {
        String commitName = null;
        if (id.length() == 40) {
            commitName = id;
        } else if (id.length() == 6) {
            List<String> files = Utils.plainFilenamesIn(COMMITS_DIR);
            var fs = files.stream().filter(s -> s.startsWith(id)).collect(Collectors.toList());
            if(fs.size() == 1) {
                commitName = String.valueOf(fs.get(0));
            } else {
                return null;
            }
        } else {
            return null;
        }
        final File commitFile = Utils.join(COMMITS_DIR, commitName);
        if (!commitFile.exists()) {
            return null;
        }
        final Commit result = Utils.readObject(commitFile, Commit.class);
        return result;
    }

    private static boolean fileCompare(File a, File b) {
        if (a.length() != b.length()) {
            return false;
        }
        String f1 = Utils.readContentsAsString(a);
        String f2 = Utils.readContentsAsString(b);
        return f1.equals(f2);
    }

    private static boolean fileCompare(String a, File b) {
        return fileCompare(new File(a), b);
    }

    private static boolean fileCompare(File a, String b) {
        return fileCompare(a, new File(b));
    }

    private static boolean fileCompare(String a, String b) {
        return fileCompare(new File(a), new File(b));
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

    public static void add(String filePath) {
        File f = new File(filePath);
        if(!f.exists()) {
            System.out.println("File does not exist: ");
            return;
        }
        String relPath = getRelPath(f);

        if (STAGERM_FILE.length() > 0) {
            TreeSet<String> filesToRemove = Utils.readObject(STAGERM_FILE, TreeSet.class);
            if (filesToRemove.contains(relPath)) {
                filesToRemove.remove(relPath);
            }
            Utils.writeObject(STAGERM_FILE, filesToRemove);
        }

        final Commit cur = current();
        Map<String, String> files = cur.getFiles();
        File staged = Utils.join(STAGES_DIR, relPath);
        if (staged.exists()) {
            if (fileCompare(staged, f)) {
                return;
            }
        } else {
            if (files != null && files.containsKey(relPath)) {
                File blob = Utils.join(BLOBS_DIR, files.get(relPath));
                if (fileCompare(blob, f)) {
                    return;
                }
            }
            try {
                staged.createNewFile();
            } catch(IOException e) {
                Utils.error(e.getMessage());
            }
        }

        Utils.writeContents(staged, Utils.readContents(f));
    }

    public static void commit(String message) {
        if (message.length() == 0) {
            System.out.println("Please enter a commit message.");
            return;
        }
        Commit cur = current();
        List<String> files = Utils.plainFilenamesIn(STAGES_DIR);
        var originalFiles = cur.getFiles();
        Map<String, String> blobs;
        if (originalFiles != null)
            blobs = new TreeMap<String, String>(originalFiles);
        else
            blobs = new TreeMap<String, String>();
        Commit nc = new Commit(message, Arrays.asList(cur.hash()), blobs);
        boolean rm = false;
        if (STAGERM_FILE.length() > 0) {
            TreeSet<String> filesToRemove = Utils.readObject(STAGERM_FILE, TreeSet.class);
            for (String file : filesToRemove) {
                files.remove(file);
                rm = true;
            }
            Utils.writeObject(STAGERM_FILE, new TreeSet<String>());
        }

        store(nc, files);
        if (files.size() == 0 && !rm)
            System.out.println("No changes added to the commit.");
    }

    public static void checkout(String branchName) {
        if (branchName.equals(currentBranch())) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        File branchFile = Utils.join(BRANCHES_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            return;
        }
        var branchCommit = Utils.readContentsAsString(branchFile);
        var branch = getCommitById(branchCommit);

        branch.getFiles().forEach((fn, blob) -> {
            var f = Utils.join(CWD, fn);
            var blob_f = Utils.join(BLOBS_DIR, blob);
            String content;
            content = Utils.readContentsAsString(blob_f);
            Utils.writeContents(f, content);
        });
    }

    public static void checkout(String unused, String filename) {
        if (!unused.equals("--")) {
            return;
        }
        Commit cur = current();
        var files = cur.getFiles();
        if (files != null && files.containsKey(filename)) {
            String blob_path = files.get(filename);
            File blob = Utils.join(BLOBS_DIR, blob_path);
            File f = Utils.join(CWD, filename);
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    Utils.error(e.getMessage());
                }
            }
            Utils.writeContents(f, Utils.readContentsAsString(blob));
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    public static void checkout(String commitID, String unused, String filename) {
        if (!unused.equals("--")) {
            System.out.println("Incorrect operands.");
            return;
        }
        Commit cur = getCommitById(commitID);
        if (cur == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        var files = cur.getFiles();
        if (files != null && files.containsKey(filename)) {
            String blob_path = files.get(filename);
            File blob = Utils.join(BLOBS_DIR, blob_path);
            File f = Utils.join(CWD, filename);
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            Utils.writeContents(f, Utils.readContentsAsString(blob));
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    public static void log() {
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
                File prevFile = Utils.join(COMMITS_DIR, prev);
                cur = Utils.readObject(prevFile, Commit.class);
            } else {
                cur = null;
            }
        } while (cur != null);
    }

    public static void rm(String filename) {
        File f = new File(filename);
        String relPath = getRelPath(f);
        Commit cur = current();
        TreeSet<String> filesToRemove;

        if (STAGERM_FILE.length() > 0) {
            filesToRemove = Utils.readObject(STAGERM_FILE, TreeSet.class);
        } else {
            filesToRemove = new TreeSet<String>();
        }

        boolean noReason = true;

        if (!filesToRemove.contains(relPath)) {
            File staged = Utils.join(STAGES_DIR, relPath);
            if (staged.exists()) {
                noReason = false;
                Utils.restrictedDelete(staged);
            }
            if (cur.getFiles().get(relPath) != null) {
                noReason = false;
                filesToRemove.add(relPath);
                Utils.writeObject(STAGERM_FILE, filesToRemove);
            }
        }
        if (noReason) {
            System.out.println("No reason to remove the file.");
        } else {
            Utils.restrictedDelete(filename);
        }
    }

    public static void global_log() {
        for (var file: Utils.plainFilenamesIn(COMMITS_DIR)) {
            File f = Utils.join(COMMITS_DIR, file);
            Commit c = Utils.readObject(f, Commit.class);
            System.out.println("===");
            System.out.println("commit " + c.hash());
            String outs = new Formatter(Locale.US).format("%1$ta %1$tb %1$te %1$tT %1$tY %1$tz", c.getDate()).toString();
            System.out.println("Date: " + outs);
            System.out.println(c.getMessage());
            System.out.println();
        }
    }

    public static void find(String str) {
        boolean found = false;
        for (var file: Utils.plainFilenamesIn(COMMITS_DIR)) {
            File f = Utils.join(COMMITS_DIR, file);
            Commit c = Utils.readObject(f, Commit.class);
            if (c.getMessage().indexOf(str) == -1) {
                continue;
            }
            found = true;
            System.out.println(c.hash());
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    private static boolean isStaged(File f) {
        var fo = Utils.join(STAGES_DIR, getRelPath(f));
        if (!fo.exists()) {
            return false;
        }
        if (fo.length() != f.length()) {
            return false;
        }
        return fileCompare(fo, f);
    }

    private static boolean isStaged(String filename) {
        return isStaged(Utils.join(CWD, filename));
    }

    public static void status() {
        final String curBranch = currentBranch();
        final Commit cur = current();
        System.out.println("=== Branches ===");
        for (var file: Utils.plainFilenamesIn(BRANCHES_DIR)) {
            if (curBranch.equals(file))
                System.out.print("*");
//            else
//                System.out.print(" ");
            System.out.println(file);
        }

        System.out.println();
        System.out.println("=== Staged Files ===");
        for (var file: Utils.plainFilenamesIn(STAGES_DIR)) {
            System.out.println(file);
        }

        System.out.println();
        System.out.println("=== Removed Files ===");
        var srm = Utils.join(GITLET_DIR, "stagerm");
        if (srm.length() > 0) {
            Set<String> filesToRemove = Utils.readObject(STAGERM_FILE, TreeSet.class);
            filesToRemove.forEach(f -> Utils.message(f));
        }

        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (var file: Utils.plainFilenamesIn(CWD)) {
            if(cur.getFiles().containsKey(file)) {
                File blobFile = Utils.join(BLOBS_DIR, cur.getFiles().get(file));
                if(!fileCompare(file, blobFile) && !isStaged(file)) {
                    System.out.println(file);
                }
            }
        }

        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (var file: Utils.plainFilenamesIn(CWD)) {
            var staged = Utils.join(STAGES_DIR, file);
            if (!cur.getFiles().containsKey(file)
                && !isStaged(file)) {
                System.out.println(file);
            }
        }
    }

    public static void branch(String branchName) {
        final HeadInfo hi = Utils.readObject(HEAD_FILE, HeadInfo.class);
        File branchFile = Utils.join(BRANCHES_DIR, branchName);
        if (branchFile.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        try {
            branchFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Utils.writeContents(branchFile, hi.CommitName);
//        hi.BranchName = branchName;
//        Utils.writeObject(HEAD_FILE, hi);
    }

    public static void rm_branch(String branchName) {
        final HeadInfo hi = Utils.readObject(HEAD_FILE, HeadInfo.class);
        File branchFile = Utils.join(BRANCHES_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (branchName.equals(currentBranch())) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        Utils.restrictedDelete(branchFile);
    }

    public static void reset(String id) {
        final Commit c = getCommitById(id);
        if (c == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Map<String, String> m = c.getFiles();
        for (var fname: m.keySet()) {
            File blob = Utils.join(BLOBS_DIR, m.get(fname));
            Utils.writeContents(Utils.join(CWD, fname), Utils.readContents(blob));
        }
    }

    private static Commit getPrev(Commit c, int index) {
        var prevName = c.getPrevious().get(index);
        File commitFile = Utils.join(COMMITS_DIR, prevName);
        if (!commitFile.exists()) {
            return null;
        }
        return Utils.readObject(commitFile, Commit.class);
    }

    private static class lineOperation {
        int op;
        String line;
        public lineOperation(int op, String line) {
            this.op = op;
            this.line = line;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof lineOperation) {
                lineOperation other = (lineOperation) obj;
                return op == other.op && line.equals(other.line);
            }
            return super.equals(obj);
        }
    }

    private static List<lineOperation> diff(String from, String to) {
        var lines1 = to.split("\n");
        var lines2 = from.split("\n");
        int[][] m = new int[lines1.length + 1][lines2.length + 1];

        for (int i = 1; i < m.length; i++) {
            for (int j = 1; j < m[i].length; j++) {
                if (lines1[i-1].equals(lines2[j-1])) {
                    m[i][j] = m[i - 1][j - 1] + 1;
                } else {
                    m[i][j] = Math.max(m[i - 1][j], m[i][j - 1]);
                }
            }
        }

        List<lineOperation> ops = getLineOperations(lines1, lines2, m);

        return ops;
    }

    private static List<lineOperation> getLineOperations(String[] lines1, String[] lines2, int[][] m) {
        int i = lines1.length;
        int j = lines2.length;
        List<lineOperation> ops = new ArrayList<>();
        while (i > 0 || j > 0) {
            lineOperation op;
            if (i > 0 && m[i][j] == m[i - 1][j]) {
                op = new lineOperation(1, lines1[i-1]);
                i--;
            } else if (j > 0 && m[i][j] == m[i][j-1]) {
                op = new lineOperation(-1, lines2[j-1]);
                j--;
            } else {
                op = new lineOperation(0, lines1[i-1]);
                i--;
                j--;
            }
//            ops.add(op);
//            ops.addLast(op);
            ops.add(ops.size()-1, op);
        }
        return ops;
    }

    private static List<lineOperation> diff(File from, File to) {
        return diff(Utils.readContentsAsString(from), Utils.readContentsAsString(to));
    }

    static class OpPath{
        int x;
        List<lineOperation> history;
        OpPath(int x, List<lineOperation> history) {
            this.x = x;
            this.history = history;
        }

    }

    private static List<lineOperation> myers_diff(File from, File to) {
        String a;
        String b;
        if (from != null) {
            a = Utils.readContentsAsString(from);
        } else {
            a = "";
        }
        if (to != null) {
            b = Utils.readContentsAsString(to);
        } else {
            b = "";
        }
        return myers_diff(a, b);
    }

    private static List<lineOperation> myers_diff(String from, String to) {
        Map<Integer, OpPath> paths = new HashMap<>();
        paths.put(1, new OpPath(0, new ArrayList<>()));
        var lines1 = from.split("\n");
        var lines2 = to.split("\n");

        final int max1 = lines1.length;
        final int max2 = lines2.length;
        int x;
        List<lineOperation> history;
        for (int d=0;d < max1+max2+1;d++) {
            for (int k=-d; k <= d+1;k += 2) {
                final boolean godown = (k == -d) ||
                        ((k != d) && (paths.get(k-1).x < paths.get(k+1).x));

                if (godown) {
                    x = paths.get(k+1).x;
                    history = paths.get(k+1).history;
                } else {
                    x = paths.get(k-1).x+1;
                    history = paths.get(k-1).history;
                }
//                history = List.copyOf(history);
                history = new ArrayList<>(history);
                int y = x - k;

                if (1 <= y && y <= max2 && godown) {
                    history.add(new lineOperation(1, lines2[y-1]));
                } else if (1 <= x && x <= max1 && y <= max2) {
                    history.add(new lineOperation(-1, lines1[x-1]));
                }

                while (x < max1 && y < max2 && lines1[x].equals(lines2[y])) {
                    x++;
                    y++;
                    history.add(new lineOperation(0, lines1[x-1]));
                }

                if (x >= max1 && y >= max2) {
                    return history;
                } else {
                    paths.put(k, new OpPath(x, history));
                }
            }
        }

        return null;
    }

    private static void print_diff(List<lineOperation> diff) {
        String[] ops = {"- ", "  ", "+ "};
        for (lineOperation op : diff) {
            System.out.print(ops[op.op+1]);
            System.out.println(op.line);
        }
    }

    public static String merge_diff(List<lineOperation> diff1, List<lineOperation> diff2) {

        List<String> result = new ArrayList<>();
        int lineNo1 = 0;
        int lineNo2 = 0;

        while (lineNo1 < diff1.size() && lineNo2 < diff2.size()) {
            if (diff1.get(lineNo1).equals(diff2.get(lineNo2))) {
                if (diff1.get(lineNo1).op != -1)
                    result.add(diff1.get(lineNo1).line);
                lineNo1++;
                lineNo2++;
            } else {
                List<String> conflicts1 = new ArrayList<>();
                List<String> conflicts2 = new ArrayList<>();
                while (!diff1.get(lineNo1).equals(diff2.get(lineNo2))) {
                    if (diff1.get(lineNo1).op != -1)
                        conflicts1.add(diff1.get(lineNo1).line);
                    if (diff2.get(lineNo2).op != -1)
                        conflicts2.add(diff2.get(lineNo2).line);
                    if (diff1.get(lineNo2).op != 0)
                        lineNo1++;
                    if (diff2.get(lineNo2).op != 0)
                        lineNo2++;
                }
                var sb = new StringBuilder();
                sb.append("<<<<<<< HEAD\n");
                conflicts1.forEach(s -> sb.append(s+'\n'));
                sb.append("=======\n");
                conflicts2.forEach(s -> sb.append(s+'\n'));
                sb.append(">>>>>>>");
                result.add(sb.toString());
            }
        }

        while (lineNo1 < diff1.size()) {
            if (diff1.get(lineNo1).op != -1)
                result.add(diff1.get(lineNo1).line);
            lineNo1++;
        }

        while (lineNo2 < diff1.size()) {
            if (diff1.get(lineNo2).op != -1)
                result.add(diff1.get(lineNo2).line);
            lineNo2++;
        }

        return String.join("\n", result);
    }

    public static void merge(String branchName) {
        var cur = current();
        File branchFile = Utils.join(BRANCHES_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
        }
        var branch = Utils.readObject(branchFile, Commit.class);
        // find LUA
        Set<Commit> history = new HashSet<>();

        Queue<Commit> layer = new ArrayDeque<>();
        layer.add(cur);
        layer.add(branch);

        Commit lua;
        while (true) {
            Commit pointer = layer.poll();
            if (history.contains(pointer)) {
                lua = pointer;
                break;
            }
            var prev = pointer.getPrevious();
            for (String p: prev) {
                Commit c = getCommitById(p);
                history.add(c);
            }
        }

        // fast merge
        if (lua.equals(cur) || lua.equals(branch)) {
            // cur is ancestor of target
            // or target is ancestor of cur
            Utils.writeObject(HEAD_FILE, branch.hash());
            cur = current();
            cur.getFiles().forEach((fn, blob) -> {
                var blobFile = Utils.join(BLOBS_DIR, blob);
                var cur_f = Utils.join(CWD, fn);
                String content;
                content = Utils.readContentsAsString(blobFile);
                Utils.writeContents(cur_f, content);
            });
            return;
        }

        // make it simple
        Set<String> filesToBeDeleted = new HashSet<>();

        // get a set of files in (lua, head, target)
        Set<String> files = new HashSet<>();
        files.addAll(lua.getFiles().keySet());
        files.addAll(branch.getFiles().keySet());
        files.addAll(cur.getFiles().keySet());

        // process each file in the set
        for (String file : files) {
            var luaBlob = lua.getFiles().get(file);
            var branchBlob = branch.getFiles().get(file);
            var curBlob = cur.getFiles().get(file);
            // keep the file
            if (luaBlob != null && curBlob != null && branchBlob != null
                    && lua.equals(curBlob) && branchBlob.equals(curBlob)) {
                // same file, do nothing
            }
            // remove the file
            else if (luaBlob != null && branchBlob == null && curBlob != null) {
                filesToBeDeleted.add(file); // or just delete here
            }
            else {
                // merge the file
                var luaF = luaBlob != null ? Utils.join(CWD, luaBlob) : null;
                var branchF = branchBlob != null ? Utils.join(CWD, branchBlob) : null;
                var curF = curBlob != null ? Utils.join(CWD, curBlob) : null;
                // get diffs
                var diff1 = diff(luaF, curF);
                var diff2 = diff(luaF, branchF);
                // merge diffs and apply
                var out = merge_diff(diff1, diff2);
                var f = Utils.join(CWD, file);
                // write to file
                Utils.writeContents(f, out);
            }
        }

        // delete files
        for(String file : filesToBeDeleted) {
            Utils.restrictedDelete(Utils.join(CWD, file));
        }
    }
}

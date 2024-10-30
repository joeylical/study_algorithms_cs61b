package gitlet;

import java.io.File;
import java.io.IOException;
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
    public static final File STAGERM_FILE = join(GITLET_DIR, "stagerm");

    public static boolean exists()
    {
        return GITLET_DIR.exists() ||
                BLOBS_DIR.exists() ||
                TREES_DIR.exists() ||
                COMMITS_DIR.exists() ||
                BRANCHES_DIR.exists() ||
                STAGES_DIR.exists() ||
                HEAD_FILE.exists() ||
                STAGERM_FILE.exists();
    }

    private static void initBranch(String commit_name) {
        final File master = Utils.join(BRANCHES_DIR, "master");
        try{
            master.createNewFile();
        } catch (IOException e){}
        Utils.writeObject(master, commit_name);
    }

    private static void store(Commit commit, List<String> files) {
        if (files != null)
            for (final String file : files) {
                File f = Utils.join(STAGES_DIR, file);
                String fhash = Utils.sha1(Utils.readContentsAsString(f));
                File blob = Utils.join(BLOBS_DIR, fhash);
                Utils.writeContents(blob, Utils.readContentsAsString(f));
                String relpath = getRelPath(STAGES_DIR, f);
                commit.putFile(relpath, fhash);
                f.delete();
            }
        // stage rm

        final String commit_name = commit.hash();
        final File commit_file = new File(COMMITS_DIR, commit_name);
        Utils.writeObject(commit_file, commit);
        HeadInfo hi = new HeadInfo(commit_name, currentBranch());
        Utils.writeObject(HEAD_FILE, hi);
    }

    public static void init() {
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
        try{
            STAGERM_FILE.createNewFile();
            HEAD_FILE.createNewFile();
            final Commit c = new Commit(init_message, null, null);
            initBranch(c.hash());
            store(c, null);
        } catch(IOException e){
            System.out.println("A Gitlet version-control system already exists in the current directory.");
        }

    }

    public static Commit current() {
        final var hi = Utils.readObject(HEAD_FILE, HeadInfo.class);
        final String commit_name = hi.commit_name;
        final File commit_file = Utils.join(COMMITS_DIR, commit_name);
        final Commit result = Utils.readObject(commit_file, Commit.class);
        return result;
    }

    public static String currentBranch() {
        if(HEAD_FILE.exists()) {
            if (HEAD_FILE.length() > 0) {
                final HeadInfo hi = Utils.readObject(HEAD_FILE, HeadInfo.class);
                return hi.branch_name;
            } else {
                return "master";
            }
        }
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
            } else {
                return null;
            }
        } else {
            return null;
        }
        final File commit_file = Utils.join(COMMITS_DIR, commit_name);
        if(!commit_file.exists()) {
            return null;
        }
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

    public static void add(String file_path) {
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
            try{
                staged.createNewFile();
            } catch(IOException e){}
        }

        Utils.writeContents(staged, Utils.readContents(f));
    }

    public static void commit(String message) {
        Commit cur = current();
        List<String> files = Utils.plainFilenamesIn(STAGES_DIR);
        var original_files = cur.getFiles();
        Map<String, String> blobs;
        if (original_files != null)
            blobs = new TreeMap<String, String>(original_files);
        else
            blobs = new TreeMap<String, String>();
        Commit nc = new Commit(message, Arrays.asList(cur.hash()), blobs);
        store(nc, files);
    }

    public static void do_checkout(String commit_id, String filename) {

    }

    public static void checkout(String branch_name) {
        File branch_file = Utils.join(BRANCHES_DIR, branch_name);
        if(!branch_file.exists()) {
            System.out.println("A branch with that name does not exist.");
        }
        var branch = Utils.readObject(branch_file, Commit.class);

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
        if(files != null && files.containsKey(filename)) {
            String blob_path = files.get(filename);
            File blob = Utils.join(BLOBS_DIR, blob_path);
            File f = Utils.join(CWD, filename);
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                }
            }
            Utils.writeContents(f, Utils.readContentsAsString(blob));
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    public static void checkout(String commit_id, String unused, String filename) {
        if (!unused.equals("--")) {
            return;
        }
        Commit cur = getCommitById(commit_id);
        var files = cur.getFiles();
        if(files != null && files.containsKey(filename)) {
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
                File prev_file = Utils.join(COMMITS_DIR, prev);
                cur = Utils.readObject(prev_file, Commit.class);
            } else {
                cur = null;
            }
        } while (cur != null);
    }

    public static void rm(String filename) {
        File f = new File(filename);
        String rel_path = getRelPath(f);
        Commit cur = current();
        TreeSet<String> rmfiles = Utils.readObject(STAGERM_FILE, TreeSet.class);
        if (!rmfiles.contains(rel_path)) {
            File staged = Utils.join(STAGES_DIR, rel_path);
            if (staged.exists()) {
                Utils.restrictedDelete(staged);
            }
            if (cur.getFiles().get(rel_path) != null) {
                rmfiles.add(rel_path);
                Utils.writeObject(STAGERM_FILE, rmfiles);
            }

        }
    }

    public static void global_log() {
        for(var file: Utils.plainFilenamesIn(COMMITS_DIR)) {
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
        for(var file: Utils.plainFilenamesIn(COMMITS_DIR)) {
            File f = Utils.join(COMMITS_DIR, file);
            Commit c = Utils.readObject(f, Commit.class);
            if (c.getMessage().indexOf(str) == -1) {
                found = true;
                continue;
            }
            System.out.println("===");
            System.out.println("commit " + c.hash());
            String outs = new Formatter(Locale.US).format("%1$ta %1$tb %1$te %1$tT %1$tY %1$tz", c.getDate()).toString();
            System.out.println("Date: " + outs);
            System.out.println(c.getMessage());
            System.out.println();
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void status() {
        final String cur_branch = currentBranch();
        System.out.println("=== Branches ===");
        for(var file: Utils.plainFilenamesIn(BRANCHES_DIR)) {
            if (cur_branch.equals(file))
                System.out.print("*");
            else
                System.out.print(" ");
            System.out.println(file);
        }
    }

    public static void branch(String branch_name) {
        final HeadInfo hi = Utils.readObject(HEAD_FILE, HeadInfo.class);
        File branch_file = Utils.join(BRANCHES_DIR, branch_name);
        if (branch_file.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        try{
            branch_file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Utils.writeContents(branch_file, hi.commit_name);
        hi.branch_name = branch_name;
        Utils.writeObject(HEAD_FILE, hi);
    }

    public static void rm_branch(String branch_name) {
        final HeadInfo hi = Utils.readObject(HEAD_FILE, HeadInfo.class);
        File branch_file = Utils.join(BRANCHES_DIR, branch_name);
        if (!branch_file.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if(branch_name.equals(currentBranch())) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        Utils.restrictedDelete(branch_file);
    }

    public static void reset(String id) {
        final Commit c = getCommitById(id);
        if (c == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Map<String, String> m = c.getFiles();
        for(var fname: m.keySet()) {
            File blob = Utils.join(BLOBS_DIR, fname);
            Utils.writeContents(Utils.join(CWD, fname), Utils.readContents(blob));
        }
    }

    private static Commit getPrev(Commit c, int index) {
        var prev_name = c.getPrevious().get(index);
        File commit_file = Utils.join(COMMITS_DIR, prev_name);
        if(!commit_file.exists()) {
            return null;
        }
        return Utils.readObject(commit_file, Commit.class);
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

        for(int i = 1; i < m.length; i++) {
            for(int j = 1; j < m[i].length; j++) {
                if(lines1[i-1].equals(lines2[j-1])) {
                    m[i][j] = m[i - 1][j - 1] + 1;
                } else {
                    m[i][j] = Math.max(m[i - 1][j], m[i][j - 1]);
                }
            }
        }

        int i = lines1.length;
        int j = lines2.length;
        List<lineOperation> ops = new ArrayList<>();
        while(i > 0 || j > 0) {
            lineOperation op;
            if(i > 0 && m[i][j] == m[i - 1][j]) {
                op = new lineOperation(1, lines1[i-1]);
                i--;
            }else if(j > 0 && m[i][j] == m[i][j-1]) {
                op = new lineOperation(-1, lines2[j-1]);
                j--;
            }else{
                op = new lineOperation(0, lines1[i-1]);
                i--;
                j--;
            }
//            ops.add(op);
            ops.addLast(op);
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
        if(from != null) {
            a = Utils.readContentsAsString(from);
        } else {
            a = "";
        }
        if(to != null) {
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
        for(int d=0;d < max1+max2+1;d++) {
            for(int k=-d; k <= d+1;k += 2) {
                final boolean godown = (k == -d) ||
                        ((k != d) && (paths.get(k-1).x < paths.get(k+1).x));

                if(godown) {
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

                if(x >= max1 && y >= max2) {
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
        for(lineOperation op : diff) {
            System.out.print(ops[op.op+1]);
            System.out.println(op.line);
        }
    }

    public static String merge_diff(List<lineOperation> diff1, List<lineOperation> diff2) {

        List<String> result = new ArrayList<>();
        int lineNo1 = 0;
        int lineNo2 = 0;

        while(lineNo1 < diff1.size() && lineNo2 < diff2.size()) {
            if(diff1.get(lineNo1).equals(diff2.get(lineNo2))) {
                if(diff1.get(lineNo1).op != -1)
                    result.add(diff1.get(lineNo1).line);
                lineNo1++;
                lineNo2++;
            } else {
                List<String> conflits1 = new ArrayList<>();
                List<String> conflits2 = new ArrayList<>();
                while(!diff1.get(lineNo1).equals(diff2.get(lineNo2))) {
                    if(diff1.get(lineNo1).op != -1)
                        conflits1.add(diff1.get(lineNo1).line);
                    if(diff2.get(lineNo2).op != -1)
                        conflits2.add(diff2.get(lineNo2).line);
                    if(diff1.get(lineNo2).op != 0)
                        lineNo1++;
                    if(diff2.get(lineNo2).op != 0)
                        lineNo2++;
                }
                var sb = new StringBuilder();
                sb.append("<<<<<<< HEAD\n");
                conflits1.forEach(s -> sb.append(s+'\n'));
                sb.append("=======\n");
                conflits2.forEach(s -> sb.append(s+'\n'));
                sb.append(">>>>>>>");
                result.add(sb.toString());
            }
        }

        while(lineNo1 < diff1.size()) {
            if(diff1.get(lineNo1).op != -1)
                result.add(diff1.get(lineNo1).line);
            lineNo1++;
        }

        while(lineNo2 < diff1.size()) {
            if(diff1.get(lineNo2).op != -1)
                result.add(diff1.get(lineNo2).line);
            lineNo2++;
        }

        return String.join("\n", result);
    }

    public static void merge(String branch_name) {
        var cur = current();
        File branch_file = Utils.join(BRANCHES_DIR, branch_name);
        if(!branch_file.exists()) {
            System.out.println("A branch with that name does not exist.");
        }
        var branch = Utils.readObject(branch_file, Commit.class);
        // find LUA
        Set<Commit> history = new HashSet<>();

        Queue<Commit> layer = new ArrayDeque<>();
        layer.add(cur);
        layer.add(branch);

        Commit lua;
        while(true) {
            Commit pointer = layer.poll();
            if(history.contains(pointer)) {
                lua = pointer;
                break;
            }
            var prev = pointer.getPrevious();
            for(String p: prev) {
                Commit c = getCommitById(p);
                history.add(c);
            }
        }

        // fast merge
        if(lua.equals(cur) || lua.equals(branch)) {
            // cur is ancestor of target
            // or target is ancestor of cur
            Utils.writeObject(HEAD_FILE, branch.hash());
            cur = current();
            cur.getFiles().forEach((fn, blob) -> {
                var blob_file = Utils.join(BLOBS_DIR, blob);
                var cur_f = Utils.join(CWD, fn);
                String content;
                content = Utils.readContentsAsString(blob_file);
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
        for(String file : files) {
            var lua_blob = lua.getFiles().get(file);
            var branch_blob = branch.getFiles().get(file);
            var cur_blob = cur.getFiles().get(file);
            // keep the file
            if (lua_blob != null && cur_blob != null && branch_blob != null
                    && lua.equals(cur_blob) && branch_blob.equals(cur_blob)) {
                // same file, do nothing
            }
            // remove the file
            else if (lua_blob != null && branch_blob == null && cur_blob != null) {
                filesToBeDeleted.add(file); // or just delete here
            }
            else {
                // merge the file
                var lua_f = lua_blob != null?Utils.join(CWD, lua_blob):null;
                var branch_f = branch_blob != null?Utils.join(CWD, branch_blob):null;
                var cur_f = cur_blob != null?Utils.join(CWD, cur_blob):null;
                // get diffs
                var diff1 = diff(lua_f, cur_f);
                var diff2 = diff(lua_f, branch_f);
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

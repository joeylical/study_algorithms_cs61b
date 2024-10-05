package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Li
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private long timestamp;
    private List<String> previous;
    private Map<String, String> files;

    public Commit(String message, List<String> previous, Map<String, String> files) {
        this.message = message;
        this.timestamp = new Date().getTime();
        this.previous = previous;
        this.files = files;
    }

    public Commit(String message, long timestamp, List<String> previous, Map<String, String> files) {
        this.message = message;
        this.timestamp = timestamp;
        this.previous = previous;
        this.files = files;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Date getDate() {
        return new Date(timestamp);
    }

    public List<String> getPrevious() {
        return previous;
    }

    public Map<String, String> getFiles() {
        return files;
    }

    public void putFile(String fileName, String blob) {
        this.files.put(fileName, blob);
    }

    public byte[] toBytes() {
        return Utils.serialize(this);
    }

    public String hash() {
        return Utils.sha1(toBytes());
    }
}

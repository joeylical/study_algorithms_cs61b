package gitlet;

import java.io.Serializable;

public class HeadInfo implements Serializable {
    public String commit_name;
    public String branch_name;
    public HeadInfo(String commit_name, String branch_name) {
        this.commit_name = commit_name;
        this.branch_name = branch_name;
    }
}
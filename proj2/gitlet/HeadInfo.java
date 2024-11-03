package gitlet;

import java.io.Serializable;

public class HeadInfo implements Serializable {
    public String CommitName;
    public String BranchName;
    public HeadInfo(String CommitName, String BranchName) {
        this.CommitName = CommitName;
        this.BranchName = BranchName;
    }
}

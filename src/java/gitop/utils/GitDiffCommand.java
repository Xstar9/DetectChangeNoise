package gitop.utils;

public class GitDiffCommand {
    // 显示差异的摘要统计信息(changed/added/deleted)
    public static String hash1 = "<hash1>";
    public static String hash2 = "<hash2>";
    public static String gitDiff4count = "git diff " + hash1 + " " + hash2 + " --stat";

    public static String classFilename = "filename.java";
    public static String gitDiff4patch = "git diff " + hash1 + " " + hash2 + " " + classFilename + " > patch_name";

    //   显示两个【分支】之间的差异
    public static String branch1 = "<branch1>";
    public static String branch2 = "<branch2>";
    public static String gitDiff4branch = "git diff " + branch1 + " " + branch2;

    //  显示指定提交（commit）和当前工作区之间的差异
    public static String commit = "<commit>";
    public static String gitDiff4workspace = "git diff " + commit;

    //  显示指定文件的差异
    public static String filePath = "<file>";
    public static String gitDiff4file = "git diff " + filePath;

    //  修改的内容及上下文行，具有指定数量的上下文行
    public static String context_line_num = "0";
    public static String gitDiff4 = "git diff -U" + context_line_num;

    // 构建 git diff 命令
    //  ProcessBuilder builder = new ProcessBuilder("git", "diff", "--name-only", baseCommit, targetCommit);
    // -U<num> ===  --unified=num

    //--diff-filter=[(A|C|D|M|R|T|U|X|B)…[*]]
    //Added (A), Copied (C), Deleted (D), Modified (M), Renamed (R), have their type (i.e. regular file, symlink, submodule, …) changed (T)

    public static String baseCommit = "3324c04b4b0ed43ff010769f2ff3d8b2ef5adb8f"; // 基础commit id
    public static String targetCommit = "5df47ec9a0fe7f7aefd3cf2eb8d33f18e5ae078d"; // 目标commit id
}

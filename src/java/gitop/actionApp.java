package gitop;


import clean.RuleSelector;
import gitop.ast.MethodBlockVisitor;
import gitop.utils.AccurateUtils;
import gitop.utils.DiffRelatedMethodUtils;
import gitop.utils.GitDiffUtils;
import gitop.utils.GitUtils;

import java.io.IOException;
import java.util.*;

import static clean.RuleSelector.processRefactorRenameType;
import static gitop.utils.AccurateUtils.*;
import static gitop.utils.GitDiffUtils.checkout2CommitId;
import static gitop.utils.GitDiffUtils.getTime;



public class actionApp {
    public static String startCommit = "badc19efbf9140b7dcff6de1613d936ac796cc25";//"5c98bbc738989bc1b12a7cce009e9fc4f707a495"; // Before commit5c98bbc738989bc1b12a7cce009e9fc4f707a495
    public static String endCommit = "c7c5f9b9582410ce39e5c9fb6b78173d01b3746c";//"1fbc115d1e94ae9d08dd933ddbe841cfb56f48e8"; // Newer Commitf25d488f40117f7caaedfb04f25086ae2857aa82
    public static String project_dir = "E:\\project-DataSets20240927\\scribejava";//"D:\\JavaProject\\ForMyIdea\\homework\\tmp\\refactoring-toy-example";

    public static void main(String[] args) throws InterruptedException, IOException {
        actionApp action = new actionApp();
        String[] list  = getCommitList();
        String latestSHA = list[0];
//        for(int id=0; id<183; id++) {
//            System.out.println(id);
            RuleSelector.entry("2d658e8123ecf362c20f9e2a7e0b1500ac49cc59","e99a5af0312bbeda6d42471d0fdbbeb84e83a365", project_dir,new HashSet<>()); // ("049001d1c0d5af5dd50676f7e2450375533d97fc","f34896a734e893759f1ca89930b0e3e30d75dca5",project_dir);
            Set<String> changeSet = new TreeSet<>();
            String time = getTime();
//            System.out.println("---------------------------------------"+(id+1)+"--------------------------------------------------------------------------------------------------------------------------------------");
            // Step 1: get Diff
            GitDiffUtils util = new GitDiffUtils();
            util.getDiffByJGit(project_dir,  "2d658e8123ecf362c20f9e2a7e0b1500ac49cc59","e99a5af0312bbeda6d42471d0fdbbeb84e83a365",time);
//            checkout2CommitId(project_dir,"9ee10efecf52ade6af3784105a159ec646edaf5b");
            System.out.println("------------------------------------Step 1: get Diff-------------------------------------------------------------------");
            // Step 2: get Change File And Lines
            String file_dir = "E:\\project-DataSets20240927\\";
            String project_name = "scribejava\\";
            String prefix_path = file_dir + project_name;
            String diff_file = "diff_content/diff-" + time + ".diff"; //  diff-LocalTime.diff(or other,eg:.txt、.patch ...)
            Map<String, List<List<Integer>>> resultMap = action.getChangeFileAndLines(prefix_path,diff_file);
            System.out.println("------------------------------------Step 2: get Change File And Lines--------------------------------------------------");
            action.getChangeSet(changeSet, prefix_path, resultMap);
            System.out.println("------------------------------------Step 3: Basic Change Set Result----------------------------------------------------");
            System.out.println("Change Impact Set:");
            System.out.println(changeSet);
            Thread.sleep(1000);
//        }
//        StringBuffer res = new StringBuffer();
//        RuleSelector.result.forEach(s -> res.append(s).append("\n"));
//        writeFile("D:\\JavaProject\\SPAT\\trans-commits\\result11.txt",res.toString());
//        checkout2CommitId(project_dir,latestSHA);
    }

    public void getChangeSet(Set<String> changeSet, String prefix_path, Map<String, List<List<Integer>>> resultMap) {
        MethodBlockVisitor visitor = new MethodBlockVisitor();
        for (String path : resultMap.keySet()) {
            // Step: 3.1 Read Change File
            Map<String, List<Integer>> file = visitor.showClassMethod(
                    readFile(prefix_path + path)
            );
            // Step: 3.2 Scan Each Method for Change Range linked to Method Range
            for (String method : file.keySet()) {
                for (int i = 0; i < resultMap.get(path).size(); i++) {
                    //3.3  Match Basic Change Method
                    if (isChangeInMethodRange(resultMap.get(path).get(i).get(0),
                            resultMap.get(path).get(i).get(1),
                            file.get(method).get(0),
                            file.get(method).get(1))) {
                        changeSet.add(method.split(" ")[1]);
                        System.out.println("Change Range " + resultMap.get(path).get(i) + " Belong to " + method);
                    }
                }
            }
        }
    }

    public void gitDiff(String startCommit,String endCommit){
        String time = getTime();
        GitDiffUtils util = new GitDiffUtils();
        util.getDiffByJGit(project_dir, startCommit, endCommit, time);
    }

    public  Map<String, List<List<Integer>>> getChangeFileAndLines(String prefix_path,String diff_file){
        DiffRelatedMethodUtils utils = new DiffRelatedMethodUtils();
        String s = readFile(prefix_path + diff_file);
        Map<String, List<List<Integer>>> resultMap = utils.getChangeFileAndLines(s);
        System.out.println("Change File And Lines ResultMap: " + resultMap);
        return resultMap;
    }

    public static  String[] getCommitList(){
        GitUtils.getGitAllCommit(project_dir);
//        System.out.println(Arrays.asList(MethodBlockVisitor.readFile("D:\\JavaProject\\ForMyIdea\\homework\\tmp\\refactoring-toy-example\\commitList.txt").split("\n")));
        return readFile(
                project_dir + "/commitList.csv"
//                "D:\\JavaProject\\ForMyIdea\\homework\\tmp\\refactoring-toy-example\\commitList.txt"
        ).split("\n");
    }
}

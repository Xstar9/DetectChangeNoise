package gitop;

import clean.RuleSelector;
import com.opencsv.CSVWriter;
import gitop.ast.MethodBlockVisitor;
import gitop.utils.DiffRelatedMethodUtils;
import gitop.utils.GitDiffUtils;
import gitop.utils.GitUtils;

import java.io.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import static gitop.utils.AccurateUtils.*;
import static gitop.utils.GitDiffUtils.checkout2CommitId;
import static gitop.utils.GitDiffUtils.getTime;
import static spat.ProcessMain.findGitDirectoryUpwards;


// 随机组合策略：将1-3次提交变更分析结果组合为1次变更；
public class actionApp2 {
    public static String startCommit = "badc19efbf9140b7dcff6de1613d936ac796cc25";//"5c98bbc738989bc1b12a7cce009e9fc4f707a495"; // Before commit5c98bbc738989bc1b12a7cce009e9fc4f707a495
    public static String endCommit = "c7c5f9b9582410ce39e5c9fb6b78173d01b3746c";//"1fbc115d1e94ae9d08dd933ddbe841cfb56f48e8"; // Newer Commitf25d488f40117f7caaedfb04f25086ae2857aa82
    public static String project_dir; // = "E:\\project-DataSet1\\WebGoat";//"D:\\JavaProject\\ForMyIdea\\homework\\tmp\\refactoring-toy-example";

    public static void main(String[] args) throws InterruptedException, IOException {
        actionApp2 action = new actionApp2();
        List<String> projectsDir = new ArrayList<>();
        System.out.println("扫描被测数据集项目路径.....");
        String[] interrupt_conn_project = {"netty","symja-parser"};//{"netty","spring-data-jpa","RuoYi","WebGoat","helios","spring-data-neo4j","spring-hateoas","scribejava","symja-parser"};
        for (File file:new File("E:\\project-DataSets20240920").listFiles()){
            projectsDir.add(Objects.requireNonNull(findGitDirectoryUpwards(file)).getAbsolutePath());
        }
        Map<String, Integer> valueCountMap = countColumnValues1("D:\\JavaProject\\SPAT\\trans-commits\\trans-commits-datasets20240920.csv", 2);

        SecureRandom random = new SecureRandom();
        for (String currentProject:projectsDir) {
//            if(Arrays.stream(interrupt_conn_project).collect(Collectors.toList()).contains(currentProject.substring(currentProject.lastIndexOf("\\")+1)))
//            {
//                continue;
//            }
            if(Arrays.stream(interrupt_conn_project).collect(Collectors.toList()).contains(currentProject.substring(currentProject.lastIndexOf("\\")+1)))
            {
                continue;
            }
            project_dir = currentProject;
            String[] list = getCommitList();
            String latestSHA = list[0];
            Set<String> resultNoiseList = new TreeSet<>();
            Set<String> tripleChangeSetList = new TreeSet<>();
            List<String>  tripleCommits = new ArrayList<>();
            int newCommitSum = valueCountMap.get(currentProject.substring(currentProject.lastIndexOf("\\")+1));
            int count = newCommitSum;
            int loop = 1;
            int group = random.nextInt(3)+1;
            count-=group;
            for (int id = 0; id < newCommitSum; id++) {
                System.out.println(id);
                String time = getTime();
//                String file_dir = "E:\\project-DataSet1\\";
//                String project_name = "WebGoat";
//                String prefix_path = file_dir + project_name;
                RuleSelector.entry(list[id + 1].trim(), list[id].trim(), project_dir, resultNoiseList); // ("049001d1c0d5af5dd50676f7e2450375533d97fc","f34896a734e893759f1ca89930b0e3e30d75dca5",project_dir);
                Set<String> changeSet = new TreeSet<>();

//            System.out.println("---------------------------------------"+(id+1)+"--------------------------------------------------------------------------------------------------------------------------------------");
                // Step 1: get Diff
                GitDiffUtils util = new GitDiffUtils();
                util.getDiffByJGit(project_dir, list[id + 1].trim(), list[id].trim(), time);//(project_dir,  "573a662e30680e54b9dbb02503a0e3440f8070c4","ad1b2b52c1785e66f9e492a2d57354f8350231b4", time);
//            checkout2CommitId(project_dir,"9ee10efecf52ade6af3784105a159ec646edaf5b");
                System.out.println("------------------------------------Step 1: get Diff-------------------------------------------------------------------");
                // Step 2: get Change File And Lines
                String diff_file = "diff_content/diff-" + time + ".diff"; //  diff-LocalTime.diff(or other,eg:.txt、.patch ...)
                Map<String, List<List<Integer>>> resultMap = action.getChangeFileAndLines(project_dir, diff_file);
                System.out.println("------------------------------------Step 2: get Change File And Lines--------------------------------------------------");
                action.getChangeSet(changeSet, project_dir, resultMap);
                System.out.println("------------------------------------Step 3: Basic Change Set Result----------------------------------------------------");
                tripleChangeSetList.addAll(changeSet);
                if (loop == group) {
                    // 重置为loop
                    loop = 1;
                    group = random.nextInt(3)+1;
                    if(count-group<=0){
                        group = 1;
                    }else {
                        count -= group;
                    }
                    System.out.println("噪声：" + resultNoiseList);
                    System.out.println("Change Impact Set:");
                    System.out.println(tripleChangeSetList);
                    tripleCommits.add(list[id+1]);
                    tripleCommits.add(list[id]);
                    writeNoiseMatchingResult2CSV("D:\\JavaProject\\SPAT\\trans-commits\\result-20240920-3.csv",
                            new String[]{currentProject,tripleCommits.toString(), RuleSelector.result1.toString(),tripleChangeSetList.toString()});
                    resultNoiseList.clear();
                    tripleChangeSetList.clear();
                    tripleCommits.clear();
                    RuleSelector.result1.clear();
                } else {
                    // 1-3次变更为一组，模拟多方法变更
                    tripleCommits.add(list[id+1]);
                    tripleCommits.add(list[id]);
                    loop++;
                }
                Thread.sleep(666);
            }
            StringBuffer res = new StringBuffer();
            RuleSelector.result.forEach(s -> res.append(s).append("\n"));
            writeFile("D:\\JavaProject\\SPAT\\trans-commits\\result920-3.txt", res.toString());
            checkout2CommitId(project_dir, latestSHA);
        }
    }

    public static Map<String, Integer> countColumnValues1(String filePath, int columnIndex) {
        Map<String, Integer> valueCountMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                // Skip header line
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String values = line.replace("\"","").split(",")[columnIndex-1];
                if(!valueCountMap.containsKey(values.split("/")[0])){
                    valueCountMap.put(values.split("/")[0],1);
                }else {
                    valueCountMap.put(values.split("/")[0], valueCountMap.get(values.split("/")[0]) + 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return valueCountMap;
    }

    public void getChangeSet(Set<String> changeSet, String prefix_path, Map<String, List<List<Integer>>> resultMap) {
        MethodBlockVisitor visitor = new MethodBlockVisitor();
        for (String path : resultMap.keySet()) {
            // Step: 3.1 Read Change File
            Map<String, List<Integer>> file = visitor.showClassMethod(
                    readFile(prefix_path+((prefix_path.endsWith("\\")||prefix_path.endsWith("/"))?"":"\\") + path)
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
        String s = readFile(prefix_path + ((prefix_path.endsWith("\\")||prefix_path.endsWith("/"))?"":"\\") + diff_file);
        Map<String, List<List<Integer>>> resultMap = utils.getChangeFileAndLines(s);
        System.out.println("Change File And Lines ResultMap: " + resultMap);
        return resultMap;
    }

    public static  String[] getCommitList(){
        if(!new File(project_dir+"/_commitList.csv").exists()){
            GitUtils.getGitAllCommit(project_dir);
        }
//        System.out.println(Arrays.asList(MethodBlockVisitor.readFile("D:\\JavaProject\\ForMyIdea\\homework\\tmp\\refactoring-toy-example\\commitList.txt").split("\n")));
        return readFile(
                project_dir + "_commitList.csv"
//                "D:\\JavaProject\\ForMyIdea\\homework\\tmp\\refactoring-toy-example\\commitList.txt"
        ).split("\n");
    }

    public static void writeNoiseMatchingResult2CSV(String filePath, String[] data) {
        File file = new File(filePath);
        boolean fileExists = file.exists();
        CSVWriter writer = null;

        try {
            FileWriter outputFile = new FileWriter(filePath, true); // true 表示追加写入
            writer = new CSVWriter(outputFile);

            // 如果文件不存在，写入表头
            if (!fileExists) {
                // ToDO:  除了混合和变量重命名，默认RelatedMethodsNum是1；还需补充真实相关的方法（有哪些），即预期的变更影响方法集+数量
                String[] header = {"project_name", "start_end_SHA", "noiseType","changeSet-triple"};
                writer.writeNext(header);
            }

            // 写入数据
            writer.writeNext(data);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

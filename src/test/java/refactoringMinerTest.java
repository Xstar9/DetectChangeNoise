import gr.uom.java.xmi.diff.CodeRange;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

import static spat.ProcessMain.findJavaSourceDirectories;

public class refactoringMinerTest {

    public static Map<String, Integer> countColumnValues(String filePath, int columnIndex,String project) {
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

                String[] values = line.replace("\"","").split(",");
                if(values[1].split("/")[0].equals(project)){
                    if (values.length > columnIndex) {
                        String value = values[columnIndex].trim();
                        valueCountMap.put(value, valueCountMap.getOrDefault(value, 0) + 1);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return valueCountMap;
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

    public static File findGitDirectoryUpwards(File directory) {
        File currentDir = directory;
        while (currentDir != null) {
            File gitDir = new File(currentDir, ".git");
            if (gitDir.exists() && gitDir.isDirectory()) {
                return gitDir;
            }
            currentDir = currentDir.getParentFile();
        }
        return null; // 如果没找到 .git 目录，返回 null
    }
    public static void main(String[] args) {
//        String rootDirectory = "E:\\projectDataSet\\RuoYi"; // 替换为实际的根目录路径
//        List<String> srcLocatedList = findJavaSourceDirectories(rootDirectory);
//        int random = 0;
//        if(srcLocatedList.size()>2){
//            SecureRandom rand = new SecureRandom();// 18种转换规则
//            random = rand.nextInt(srcLocatedList.size());
//        }
//        String localPath = srcLocatedList.get(random);
//        System.out.println(localPath.substring(0, localPath.length() - 14));
        String filePath = "D:\\JavaProject\\SPAT\\trans-commits\\trans-commits-datasets20240920.csv";//"D:\\JavaProject\\SPAT\\trans-commits\\trans-commits-datasets1.csv"; // 替换为实际CSV文件路径
        int columnIndex = 4; // 替换为你要统计的列索引 (从0开始)
        String project = "spring-data-neo4j";
        Map<String, Integer> valueCountMap = countColumnValues1(filePath, 2);

        // 打印每个值及其出现次数
        for (Map.Entry<String, Integer> entry : valueCountMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        for (String entry : valueCountMap.keySet()){
            System.out.println(entry + ": ");
            countColumnValues(filePath, columnIndex, entry).forEach((k, v) -> System.out.println("  " + k + ": " + v));
        }
//        String startingPath = "E:\\projectDataSet\\spring-data-jpa\\spring-data-jpa\\src"; // 替换为实际的目录路径
//        File gitDir = findGitDirectoryUpwards(new File(startingPath));
//        if (gitDir != null) {
//            System.out.println("Found .git directory at: " + gitDir.getAbsolutePath());
//        } else {
//            System.out.println("No .git directory found in any upper level directories.");
//        }
//
//        String jarTarget1 = "D:\\JavaProject\\jta2\\accurate-analysis\\tmp-8d16ea14c478d30f\\benchmark-old.jar.decompiled";
//        String jarTarget2 = "D:\\JavaProject\\jta2\\accurate-analysis\\tmp-9b4392d08c8c3122\\benchmark-new.jar.decompiled";
//
//
//        Map<String, List<Integer>> refactorMap = new TreeMap<>();
//
//        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
//// You must provide absolute paths to the directories. Relative paths will cause exceptions.
//        File file1 = new File(jarTarget1);
//        File file2 = new File(jarTarget2);
//        miner.detectAtDirectories(file1, file2, new RefactoringHandler() {
//            @Override
//            public void handle(String commitId, List<Refactoring> refactorings) {
//                System.out.println("Refactorings at " + commitId);
//                for (Refactoring ref : refactorings) {
//                    if(!ref.getRefactoringType().toString().startsWith("RENAME")){
//                        // 参数类型变了、新增/删除参数，都可能会影响到功能，因此不该重构视为噪声
//                        continue;
//                    }
//                    System.out.println(ref.toString());
//                    System.out.println(ref.getRefactoringType());
//                    System.out.println(ref.toJSON());
//                    System.out.println(ref.getRefactoringType().toString()+"： "+ref.rightSide().toString());
//                    System.out.println(ref.toString());
//                    List<Integer> set = new ArrayList<>();
//                    for (CodeRange codeRange : ref.rightSide()) {
//                        for (int i = codeRange.getStartLine(); i <= codeRange.getEndLine(); i++) {
//                            set.add(i);
//                        }
//                        refactorMap.put(codeRange.getFilePath() + "|" + ref.getRefactoringType(), set);
////                        log.info(codeRange.getStartLine()+","+codeRange.getEndLine()+" "+codeRange.getCodeElement());
//                    }
//                }
//            }
//        });
//        System.out.println(refactorMap);
    }
}

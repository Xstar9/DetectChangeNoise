package clean;


import clean.model.ChangeNoise;
import gitop.utils.AccurateUtils;
import gitop.utils.GitUtils;
import gr.uom.java.xmi.diff.CodeRange;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.*;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.io.File;
import java.util.*;

import static spat.git.GitUtils.readFile;


/**
 * 目前主要检查  xx重命名
 *
 * @author Z.X
 *
 */
public class CleanRefactor {
    public static String PROJECT_DIR = "D:\\下载\\static-dynamic-diff";

    /**
     * 检测代码重命名类型的变更（需要新commit在旧commit后面）
     *
     * @param Commit1 old
     * @param Commit2 new
     * @param gitUrl  源码本地路径（cloneIfNotExists不存在则需要clone）
     *
     * @return refactorMap = {filePath | RefactorType : {lineNumSet}}
     */
    public Map<String, List<Integer>> checkRefactoringFromCommits(String Commit1, String Commit2, String gitUrl , String projectDir){
        ChangeNoise changeNoise = new ChangeNoise();
        GitService gitService = new GitServiceImpl();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
        Map<String, List<Integer>> refactorMap = new TreeMap<>();
        try{
            // 下载到本地比较
            Repository repo = gitService.cloneIfNotExists(projectDir, gitUrl); // 指定本地路径不存在则需要clone gitUrl
            System.out.println("Download project complete, next Step...");
            miner.detectBetweenCommits(repo, Commit1, Commit2, new RefactoringHandler() {
                @Override
                public void handle(String commitId, List<Refactoring> refactorings) {
                    System.out.println("Refactorings at "+ commitId);
                    for (Refactoring ref : refactorings) {
                         System.out.println(ref.getRefactoringType().toString()+": " +ref.rightSide().toString());
                         System.out.println(ref.toString());
                        List<Integer> set = new ArrayList<>();
                        for (CodeRange codeRange : ref.rightSide()) {
                            for (int i = codeRange.getStartLine(); i <= codeRange.getEndLine(); i++) {
                                set.add(i);
                            }
                            refactorMap.put(codeRange.getFilePath() + "|" + ref.getRefactoringType(), set);
//                         System.out.println(codeRange.getStartLine()+","+codeRange.getEndLine()+" "+codeRange.getCodeElement());
                        }

                    }
                }
            });
             System.out.println(refactorMap.toString());
//        changeNoise.setNewRelatedLines();
//        changeNoise.setModifiedContent();
        }catch (Exception e){
            e.printStackTrace();
        }
        return refactorMap;
    }


    /**
     * 检测代码重命名类型的变更（需要新commit在旧commit后面）
     *
     * @param dir1 旧版project根目录
     * @param dir2 新版project根目录
     *
     * @return refactorMap = {filePath | RefactorType : {lineNumSet}}
     */
    public Map<String, List<Integer>> checkRefactoringFromDirectories(String dir1, String dir2){
        ChangeNoise changeNoise = new ChangeNoise();
        Map<String, List<Integer>> refactorMap = new TreeMap<>();

        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
// You must provide absolute paths to the directories. Relative paths will cause exceptions.
        File file1 = new File(dir1);
        File file2 = new File(dir2);
        miner.detectAtDirectories(file1, file2, new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                System.out.println("Refactorings at " + commitId);
                for (Refactoring ref : refactorings) {
                    if(!ref.getRefactoringType().toString().startsWith("RENAME")){
                        // 参数类型变了、新增/删除参数，都可能会影响到功能，因此不该重构视为噪声
                        continue;
                    }
                    System.out.println(ref.toString());
                    System.out.println(ref.getRefactoringType());
                    System.out.println(ref.toJSON());
                     System.out.println("\n"+ref.getRefactoringType().toString()+ref.rightSide().toString());
                     System.out.println(ref.toString());
                    List<Integer> set = new ArrayList<>();
                    for (CodeRange codeRange : ref.rightSide()) {
                        for (int i = codeRange.getStartLine(); i <= codeRange.getEndLine(); i++) {
                            set.add(i);
                        }
                        refactorMap.put(codeRange.getFilePath() + "|" + ref.getRefactoringType(), set);
//                         System.out.println(codeRange.getStartLine()+","+codeRange.getEndLine()+" "+codeRange.getCodeElement());
                    }
                }
            }
        });
        System.out.println(refactorMap);
        return refactorMap;
    }

    /**
     * 检测代码重命名类型的变更（需要新commit在旧commit后面）
     *
     * @param dir1 旧版project根目录
     * @param dir2 新版project根目录
     *
     * @return refactorMap = {filePath | RefactorType : {lineNumSet}}
     */
    public Map<String, List<Integer>> checkRefactoringFromFile(String dir1, String dir2, String... clazz){
        ChangeNoise changeNoise = new ChangeNoise();
        Map<String, List<Integer>> refactorMap = new TreeMap<>();

        Map<String, String> fileContentsBefore = new HashMap<>();
        Map<String, String> fileContentsAfter = new HashMap<>();
        fileContentsBefore.put(clazz[0],readFile(dir1 + clazz[0]));
        fileContentsAfter.put(clazz[0],readFile(dir2 + clazz[0]));
// populate the maps
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
// Each key should correspond to a file path starting from the root of the repository
// populate the maps
        miner.detectAtFileContents(fileContentsBefore, fileContentsAfter, new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                System.out.println("Refactorings at " + commitId);
                for (Refactoring ref : refactorings) {
                    System.out.println("\n"+ref.getRefactoringType().toString()+ref.rightSide().toString());
                     System.out.println(ref.toString());
                    List<Integer> set = new ArrayList<>();
                    for (CodeRange codeRange : ref.rightSide()) { // leftSide()为旧版相关对应的更改
                        for (int i = codeRange.getStartLine(); i <= codeRange.getEndLine(); i++) {
                            set.add(i);
                        }
                        refactorMap.put(codeRange.getFilePath() + "|" + ref.getRefactoringType(), set);
                    }
                }
            }
        });
        System.out.println(refactorMap);
        return refactorMap;
    }

    /**
     * 检测【反编译代码文件】中，重命名类型的变更（需要新commit在旧commit后面）
     *
     * @param fileContentsBefore 旧版project 文件路径：文件内容 映射
     * @param fileContentsAfter 新版project 文件路径：文件内容 映射
     *
     * @return refactorMap = {filePath | RefactorType : {lineNumSet}}
     */
    public Map<String, List<Integer>> checkRefactoringFromFileMap(Map<String, String> fileContentsBefore,
                                                                  Map<String, String> fileContentsAfter){
//        ChangeNoise changeNoise = new ChangeNoise();
        Map<String, List<Integer>> refactorMap = new TreeMap<>();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
        miner.detectAtFileContents(fileContentsBefore, fileContentsAfter, new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                System.out.println("Refactorings at " + commitId);
                for (Refactoring ref : refactorings) {
                    System.out.println("\n"+ref.getRefactoringType().toString()+ref.rightSide().toString());
                     System.out.println(ref.toString());
                    List<Integer> set = new ArrayList<>();
                    for (CodeRange codeRange : ref.rightSide()) { // leftSide()为旧版相关对应的更改
                        for (int i = codeRange.getStartLine(); i <= codeRange.getEndLine(); i++) {
                            set.add(i);
                        }
                        // 因为比较的是【两个存放再不同目录下】的反编译代码，所以暂时把移动源文件类型的重构去掉
                        if(ref.getRefactoringType() != RefactoringType.MOVE_SOURCE_FOLDER){
                            refactorMap.put(codeRange.getFilePath() + "|" + ref.getRefactoringType(), set);
                        }
                    }
                }
            }
        });
//        System.out.println(refactorMap);
        return refactorMap;
    }

    public static void detectRefactoring(String projectRoot, String Commit1, String Commit2, Map<String, Map<Integer, String>> diffMap, String gitUrl) {
        projectRoot = projectRoot.isEmpty() ? "D:\\下载\\static-dynamic-diff" : projectRoot;
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
        // 下载到本地比较
        Repository repo = GitUtils.fetchGit(gitUrl, projectRoot, "master");
        List<String> commitList;
        if (Commit1.isEmpty() || Commit2.isEmpty()) {
            commitList = GitUtils.getGitAllCommit(projectRoot);
            Commit1 = commitList.get(1);
            Commit2 = commitList.get(0);
        }
        final Map<RefactoringType, List<CodeRange>> codeRangeMap = new HashMap<>();
        try {
            miner.detectBetweenCommits(repo, Commit1, Commit2, new RefactoringHandler() {
                @Override
                public void handle(String commitId, List<Refactoring> refactorings) {
                     System.out.println("Refactorings at "+commitId);
                    for (Refactoring ref : refactorings) {
                        codeRangeMap.put(ref.getRefactoringType(), ref.rightSide());
                    }
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (Map.Entry<RefactoringType, List<CodeRange>> entry : codeRangeMap.entrySet()) {
            RefactoringType refactoringType = entry.getKey();
            String refactoringTypeName = refactoringType.toString();
            if (refactoringTypeName.startsWith("RENAME") && !refactoringTypeName.endsWith("CLASS")) {
                 System.out.println(refactoringType+":   ");
                entry.getValue().stream()
                        .filter(codeRange -> diffMap.containsKey(codeRange.getFilePath()))
                        .forEach(codeRange -> {
                            List<Integer> lines = new ArrayList<>();
                            diffMap.get(codeRange.getFilePath()).keySet().stream()
                                    .filter(line -> AccurateUtils.isChangeInMethodRange(line, line, codeRange.getStartLine(), codeRange.getEndLine()))
                                    .forEach(lines::add);
                            if (!lines.isEmpty()) {
                                 System.out.println("变更行：" + lines + " belong to ["
                                        + codeRange.getStartLine() + "," + codeRange.getEndLine() + "] = {"
                                        + codeRange.getCodeElement() + "}");
                            }
                        });
            }
            if (refactoringTypeName.startsWith("RENAME_CLASS")) {
                 System.out.println("This refactor action is: " + refactoringTypeName);
            }
        }
    }


}

package gitop.utils;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.*;
import java.util.*;

import static gitop.utils.GitDiffUtils.filter_not_java;


/**
 * GitUtils 代码变更内容 映射 行号
 *
 * @author xin
 * @Date   2024/4/19
 *
 */
@Slf4j
public class GitUtils {

    public static String getCurrentGitBranch(String localGitPath) throws IOException {
        Repository repository = Git.open(new File(localGitPath)).getRepository();
        return repository.getBranch();
    }

    public static List<String> listAllGitBranch(String localGitPath) {
        List<String> branchList = new ArrayList<>();
        try {
            // 打开本地仓库
            try (Git git = Git.open(new File(localGitPath))) {
                Collection<Ref> branches = git.branchList().call();
                // 输出所有分支的名称
                for (Ref branch : branches) {
//                    log.info("分支：" + branch.getName());
                    branchList.add(branch.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return branchList;
    }

    public static Repository fetchGit(String remoteUrl, String localPath, String branchName) {
        // 设置远程仓库的URL、本地路径、分支名称以及用户名密码等信息
        remoteUrl = remoteUrl.isEmpty() ? "https://gitee.com/star-zheng/refactoring-toy-example.git" : remoteUrl;
        localPath = localPath.isEmpty() ? "D:\\JavaProject\\jta\\tmp\\refactoring-toy-example" : localPath;
        branchName = branchName.isEmpty() ? "master" : branchName;
        Repository repository = null;
        try {
            // Check if the local repository exists
            File localRepoDir = new File(localPath + "/.git");
            if (localRepoDir.exists()) {
                log.info("Local repository exists. Updating...");
                // Open the existing local repository
                repository = new FileRepositoryBuilder().setGitDir(localRepoDir).build();
                // Fetch from the remote repository
                try (Git git = new Git(repository)) {
                    git.fetch().call();
                    // Pull changes from the remote branch
                    PullResult pullResult = git.pull()
//                            .setRemote("origin")
                            .setRemoteBranchName(branchName)
                            .call();
                    // Check if pull was successful
                    if (pullResult.isSuccessful()) {
                        log.info("Pull successful!");
                    } else {
                        log.info("Pull failed: " + pullResult.toString());
                    }
                }
            } else {
                log.info("Local repository does not exist. Cloning...");
                // Clone the remote repository to the local path
                CloneCommand cloneCommand = Git.cloneRepository()
                        .setURI(remoteUrl)
                        .setDirectory(new File(localPath))
                        .setBranch(branchName);
                // .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
                Git git = cloneCommand.call();
                log.info("Repository cloned successfully!");
                git.close();
            }
        } catch (IOException | GitAPIException e) {
            log.info("Error: " + e.getMessage());
        }
        return repository;
    }

    public static Map<String, Map<Integer, String>> mapDiffWithLinesByFile(String diffFile) {
        Map<String, Map<Integer, String>> fileMap = new HashMap<>();
        String currentFile = null;
//        Set<String> modifiedClass = new HashSet<>();// 获取被修改的类文件名
        try (BufferedReader br = new BufferedReader(new FileReader(diffFile))) {
            String line;
            List<Integer> lineId = new ArrayList<>();
            while ((line = br.readLine()) != null) {
//                log.info(line);
                boolean isJava = false;
                if (line.startsWith("+++")) {
                    // 提取文件路径
                    String filePath = line.substring(line.indexOf('/') + 1).trim();
                    if (!filePath.isEmpty()) {
                        if (!filter_not_java(filePath)) {
                            isJava = true;
                            currentFile = filePath;
                            Map<Integer, String> line4code = new TreeMap<>();
                            fileMap.put(currentFile, line4code);
//                        modifiedClass.add(currentFile);
                        }
                    }
                }
                if (isJava) {
                    int baseLine, delta;
                    if (line.startsWith("@@")) {
                        // 提取新增代码行，格式为 “+66,5” or "-13 +13"
                        String change = line.split("\\+")[1].split(" ")[0];
                        // 消除“+”
                        change = change.substring(0, change.length());
                        // 获取变更行号范围
                        if (change.contains(",")) {
                            baseLine = Integer.parseInt(change.split(",")[0]);
                            delta = Integer.parseInt(change.split(",")[1]);
                            for (int i = 0; i < delta; i++) {
                                if (currentFile != null) {
                                    lineId.add(baseLine + i);
                                }
                            }
                        } else {
                            if (currentFile != null) {
                                lineId.add(Integer.parseInt(change));
                            }
                        }
                    } else if (line.startsWith("+") && !line.startsWith("+++")) { // 新增行号与新增内容对应
                        StringBuilder stringBuilder = new StringBuilder(line);
                        String s = stringBuilder.substring(1, line.length()).replace("\t", "");
                        fileMap.get(currentFile).put(lineId.get(0), s);
                        lineId.remove(0);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileMap;
    }

    public static Map<String, Map<Integer, String>> mapDiff2ContentWithLines(String projectPath,String diffContent) {
        Map<String, Map<Integer, String>> fileMap = new HashMap<>();
        String currentFile = null;
        boolean isJava = false;
        List<Integer> lineId = new ArrayList<>();
        for (String line : diffContent.split("\n")) {
//                log.info(line);
            if (line.startsWith("+++ ")) {
                // 提取文件路径
                String filePath = line.substring(line.indexOf('/') + 1).trim();
                if (!filePath.isEmpty()) {
                    if (!filter_not_java(filePath)) {
                        isJava = true;
                        currentFile = projectPath + "/" + filePath;
                        Map<Integer, String> line4code = new TreeMap<>();
                        fileMap.put(currentFile, line4code);
//                        modifiedClass.add(currentFile);
                    } else {
                        isJava = false;
                    }
                }
                continue;
            }
            if (isJava) {
                int baseLine, delta;
                if (line.startsWith("@@")) {
                    // 提取新增代码行，格式为 “+66,5” or "-13 +13"
                    String change = line.split("\\+")[1].split(" ")[0];
                    // 消除“+”
                    change = change.substring(0, change.length());
                    // 获取变更行号范围
                    if (change.contains(",")) {
                        baseLine = Integer.parseInt(change.split(",")[0]);
                        delta = Integer.parseInt(change.split(",")[1]);
                        for (int i = 0; i < delta; i++) {
                            if (currentFile != null) {
                                lineId.add(baseLine + i);
                            }
                        }
                    } else {
                        if (currentFile != null) {
                            lineId.add(Integer.parseInt(change));
                        }
                    }
                } else if (line.startsWith("+") && !line.startsWith("+++")) { // 新增行号与新增内容对应
                    StringBuilder stringBuilder = new StringBuilder(line);
                    String s = stringBuilder.substring(1, line.length()).replace("\t", "");
                    fileMap.get(currentFile).put(lineId.get(0), s);
                    lineId.remove(0);
//                        log.info(fileMap.get(currentFile));
                }
            }
        }
        return fileMap;
    }

    public static Map<String, List<Integer>> codeDiff2lineNum(String diffFile) {
        Map<String, List<Integer>> fileMap = new HashMap<>();
        String currentFile = null;
        try (BufferedReader br = new BufferedReader(new FileReader(diffFile))) {
            String line;
            while ((line = br.readLine()) != null) {
//                log.info(line);
                if (line.startsWith("+++")) {
                    // 提取文件路径
                    String filePath = line.substring(line.indexOf('/') + 1).trim();
                    if (!filePath.isEmpty()) {
                        currentFile = filePath;
                        fileMap.put(currentFile, new ArrayList<>());
                    }
                }
                int baseLine, delta;
                if (line.startsWith("@@")) {
                    // 提取新增代码行，格式为 “+66,5”
                    String change = line.split("\\+")[1].split(" ")[0];
                    // 消除“+”
                    change = change.substring(0, change.length());
                    if (change.contains(",")) {
                        baseLine = Integer.parseInt(change.split(",")[0]);
                        delta = Integer.parseInt(change.split(",")[1]);
                        for (int i = 0; i < delta; i++) {
                            if (currentFile != null) {
                                fileMap.get(currentFile).add(baseLine + i);
                            }
                        }
                    } else {
                        if (currentFile != null) {
                            fileMap.get(currentFile).add(Integer.parseInt(change));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileMap;
    }

    //Line Struct: commitSHA|commitLog
    public static List<String> getGitAllCommit(String gitRepositoryPath) {
        List<String> commitList = new ArrayList<>();
        try {
            // 指定 Git 仓库路径
//            String gitRepositoryPath = "D:\\JavaProject\\ForMyIdea\\homework\\tmp\\refactoring-toy-example\\";
            // 打开 Git 仓库
            try (Git git = Git.open(new File(gitRepositoryPath))) {
                // 执行 git log 命令
                LogCommand logger = git.log();
                Iterable<RevCommit> commits = logger.call();
                // 创建输出文件
                File outputFile = new File(gitRepositoryPath + "_commitList.csv");
//                if (!outputFile.exists()) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                    // 遍历提交并写入文件
                    Iterator<RevCommit> iterator = commits.iterator();
                    while (iterator.hasNext()) {
                        RevCommit commit = iterator.next();
                        writer.write(commit.getName() + "\n");//+","+commit.getFullMessage());
//                            log.info(commit.getName());
                        commitList.add(commit.getName());
                    }
                    writer.close();
                    log.info("Finish~ Git log written to: " + outputFile.getAbsolutePath());
                }
            }
//                else {
//                    // 文件已存在，则读取文件内容
//                    String content = new String(Files.readAllBytes(Paths.get(outputFile.getPath())), StandardCharsets.UTF_8);
//                    log.info("The content of the file is:\n" + content);
//                    commitList = Arrays.asList(content.split("\n"));
//                }
//            }
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
        }
        return commitList;
    }

//    public static void main(String[] args) {
//        getGitAllCommit("D:\\JavaProject\\jta\\tmp\\static-dynamic-diff");
//    }
}



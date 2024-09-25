package spat.git;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
public class GitUtils {
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
                File outputFile = new File(gitRepositoryPath + "/commitList.csv");
//                if (!outputFile.exists()) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                    // 遍历提交并写入文件
                    Iterator<RevCommit> iterator = commits.iterator();
                    while (iterator.hasNext()) {
                        RevCommit commit = iterator.next();
                        writer.write(commit.getName() + "\n");//+","+commit.getFullMessage());
//                            System.out.println(commit.getName());
                        commitList.add(commit.getName());
                    }
                    writer.close();
                    System.out.println("Finish~ Git log written to: " + outputFile.getAbsolutePath());
                }
            }
//                else {
//                    // 文件已存在，则读取文件内容
//                    String content = new String(Files.readAllBytes(Paths.get(outputFile.getPath())), StandardCharsets.UTF_8);
//                    System.out.println("The content of the file is:\n" + content);
//                    commitList = Arrays.asList(content.split("\n"));
//                }
//            }
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
        }
        return commitList;
    }

    public static boolean getGitClone(String remoteUrl, String localPath){
        try {
        if (!new File(localPath).exists()) {

                Git.cloneRepository()
                        .setURI(remoteUrl)
                        .setDirectory(new File(localPath))
                        .call();
                System.out.println("Repository cloned to " + localPath);
                return true;

        } else {
            System.out.println("Directory already exists. Skipping clone.");
            return true; // 如果跳过已经生成过的项目改为false
        }
        } catch (GitAPIException e) {
        e.printStackTrace();
        return false;
    }
    }

    public static Repository fetchGit(String remoteUrl, String localPath, String branchName) {
        // 设置远程仓库的URL、本地路径、分支名称以及用户名密码等信息
        remoteUrl = remoteUrl.isEmpty() ? "https://gitee.com/star-zheng/refactoring-toy-example.git" : remoteUrl;
        localPath = localPath.isEmpty() ? "D:\\JavaProject\\jta\\tmp\\refactoring-toy-example" : localPath;
//        branchName = branchName.isEmpty() ? "master" : branchName;
        Repository repository = null;
        try {
            // Check if the local repository exists
            File localRepoDir = new File(localPath + "/.git");
            if (localRepoDir.exists()) {
                System.out.println("Local repository exists. Updating...");
                // Open the existing local repository
                repository = new FileRepositoryBuilder().setGitDir(localRepoDir).build();
                // Fetch from the remote repository
                try (Git git = new Git(repository)) {
                    git.fetch().call();
                    // Pull changes from the remote branch
                    PullResult pullResult = git.pull()
//                            .setRemote("origin")
//                            .setRemoteBranchName(branchName)
                            .call();
                    // Check if pull was successful
                    if (pullResult.isSuccessful()) {
                        System.out.println("Pull successful!");
                    } else {
                        System.out.println("Pull failed: " + pullResult.toString());
                    }
                }
            } else {
                System.out.println("Local repository does not exist. Cloning...");
                // Clone the remote repository to the local path
                CloneCommand cloneCommand = Git.cloneRepository()
                        .setURI(remoteUrl)
                        .setDirectory(new File(localPath))
                        .setBranch(branchName);
                // .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
                Git git = cloneCommand.call();
                System.out.println("Repository cloned successfully!");
                git.close();
            }
        } catch (IOException | GitAPIException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return repository;
    }


    /**
     * readFile
     * 封装FileReader读取文件方法
     *
     */
    public static String readFile(String path){
        StringBuilder content = new StringBuilder();
        try (FileReader fileReader = new FileReader(path);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace(); // 可以根据实际情况处理异常
        }
        return content.toString();
    }

    public static List<String> findJavaDirectories(Path root) throws IOException {
        List<String> javaDirs = new ArrayList<>();

        // 使用目录流遍历目录和子目录
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            for (Path entry : stream) {
                // 检查是否是java目录
                if (Files.isDirectory(entry) && entry.getFileName().toString().equals("java")) {
                    javaDirs.add(entry.toAbsolutePath().toString());
                }
                // 递归查找子目录
                if (Files.isDirectory(entry)) {
                    javaDirs.addAll(findJavaDirectories(entry));
                }
            }
        }

        // 过滤掉位于test目录下的java目录
        return javaDirs;
    }

    // 检查一个路径是否位于test目录下
    public static boolean isUnderTestDirectory(String path) {
//        Path parent = path.getParent();
//        while (parent != null && parent.getParent() != null) {
        return path.contains("src") &&
                path.contains("main");
//            parent = parent.getParent();
//        }
    }
    public static void writeToFile(String filePath, String content) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        } catch (IOException e) {
            System.out.println("Failed to write file: " + filePath);
            e.printStackTrace();
        }
    }

    public static String getCurrentCommitId(Git git) throws GitAPIException {
        Iterable<RevCommit> commits = git.log().setMaxCount(1).call();
        return commits.iterator().next().getName();
    }

    public static void main(String[] args) {
        getGitAllCommit("E:\\projectDataSet\\spring-hateoas\\spring-hateoas");
    }

}

package gitop.utils;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

import static com.github.gumtreediff.matchers.Matcher.LOGGER;

/**
 * GitDiffUtils  获取git diff内容等操作，命令行方式、JGit库方式
 *
 * @author Z.X
 * @Date   2024/4/19
 *
 */
@Slf4j
public class GitDiffUtils {
    public static String startCommit1 = "1fbc115d1e94ae9d08dd933ddbe841cfb56f48e8";
    public static String endCommit1 = "f25d488f40117f7caaedfb04f25086ae2857aa82";
    public static String startCommit = "478ef821038e53790fe49ff8bbdec6e9ec2652cc";
    public static String endCommit = "8afd60bde8876065963e5e4b7e5ff1e2d45efa4d";
    public String project_name = "DiffDemo";
    public static String project_dir = "D:\\JavaProject\\ForMyIdea\\homework\\tmp\\refactoring-toy-example"; //"D:\\JavaProject\\ForMyIdea\\homework\\git_dir\\" + project_name;
    public static Map<String, Map<Integer, String>> diffMap;

    public static final String[] UN_VISITOR_TYPE = {".java"};

    public static final String[] SYSTEM_PACKAGES =
            {"java/*", "javax/*", "retrofit2/*", "com/airbnb/*", "org/apache/*"};

    public static void main(String[] args) {
        String time = getTime();  //临时。后续在改进结构
//        getDiff(project_dir,startCommit1,endCommit1,time);
        GitUtils.getGitAllCommit(project_dir);
    }

    public Map<String, Map<Integer, String>> getDiffByCommand(String project_dir, String startCommit, String endCommit, String time) {
        try {
            List<String> command = new ArrayList<>();
            command.add("git");
            command.add("diff");
            command.add("-U" + 0); // 等价于 command.add("--unified=0"); 0 = 变更行的上下文行数，默认是3 会影响diff展示的行号（偏移）
            command.add(startCommit);
            command.add(endCommit);

            ProcessBuilder builder = new ProcessBuilder(command);
//             ProcessBuilder builder= new ProcessBuilder("git", "diff", "-U" + context_line_num, startCommit,endCommit, "--unified=0");
            // 设置工作目录
            builder.directory(new File(project_dir));
            // 指定文件夹路径
            String folderPath = project_dir + "\\diff_content";
            // 创建Path对象
            Path path = Paths.get(folderPath);
            try {
                // 使用Files.createDirectories方法创建文件夹（包括父文件夹）
                Files.createDirectories(path);
                log.info("Folder {} created successfully", path);
            } catch (IOException e) {
                log.info("Failed to create folder: " + e.getMessage());
            }
            String diffPath = folderPath + "/diff-" + time + ".diff";//getTime()
            // 启动进程
            Process process = builder.start();
            // 处理输出流
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            // 保存diff文件
            FileWriter fileWriter = new FileWriter(diffPath);
            String line;
            while ((line = reader.readLine()) != null) {
                fileWriter.write(line + "\n");
            }
            fileWriter.flush();
            fileWriter.close();

//            diffMap = GitUtil.mapDiffWithLines(diffPath);
//            log.info(diffMap);
            // 等待进程完成
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("get Diff Success");
            } else {
                log.info("Error executing git diff command!");
            }
            process.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return diffMap;
    }

    public static void checkout2CommitId(String projectPath, String commitId){
        try {
            // Open the Git repository from the specified project directory
            Repository repository = new FileRepositoryBuilder()
                    .setGitDir(new File(projectPath + "/.git"))
                    .build();
            // Create a Git instance to interact with the repository
            try (Git git = new Git(repository)) {
                git.fetch().call();
                if (!git.getRepository().getBranch().equals(commitId)) {
                    // 并行/异常中段可能会产生 Unable to create '/.git/index.lock'
                    git.checkout().setName(commitId).call();
                }
            } catch (GitAPIException e) {
                LOGGER.log(Level.SEVERE, "An GitAPIException occurred in checkout2CommitId", e);
            }
            repository.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An GitAPIException occurred in checkout2CommitId", e);
        }
    }
    public Map<String, Map<Integer, String>> getDiffByJGit(String projectDir, String startCommit, String endCommit, String time) {
        Map<String, Map<Integer, String>> diffMap = new HashMap<>();
        String folderPath = projectDir + "\\diff_content";
        try {
            // Open the Git repository from the specified project directory
            Repository repository = new FileRepositoryBuilder()
                    .setGitDir(new File(projectDir + "/.git"))
                    .build();
            // Create a Git instance to interact with the repository
            try (Git git = new Git(repository)) {
//                git.fetch().call();
                // Get the currently checked out branch
                // String currentBranch = repository.getBranch();
                // Pull changes from the remote branch
//                PullResult pullResult = git.pull().setRemoteBranchName("master").call();

                // Get the object IDs for the start and end commits
                ObjectId startCommitId = repository.resolve(startCommit);
                ObjectId endCommitId = repository.resolve(endCommit);
                // Get the diff between the start and end commits
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                DiffFormatter diffFormatter = new DiffFormatter(outputStream);
                diffFormatter.setRepository(repository);
                diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
                diffFormatter.setContext(0);  // Set the context lines, no setting and default = 3
                // Generate the diff content
                diffFormatter.format(startCommitId, endCommitId);
                diffFormatter.flush();
                diffFormatter.close();
//                if (!git.getRepository().getBranch().equals(endCommit)) {
//                    // 并行/异常中段可能会产生 Unable to create '/.git/index.lock'
//                    git.checkout().setName("master").call();
//                }

                Path path = Paths.get(folderPath);
                try {
                    // 使用Files.createDirectories方法创建文件夹（包括父文件夹）
                    Files.createDirectories(path);
                    log.info("Folder " + path + " created successfully");
                } catch (IOException e) {
                    log.info("Failed to create folder: " + e.getMessage());
                }
                String diffPath = folderPath + "/diff-" + time + ".diff";//getTime()

                // 保存diff文件
                FileWriter fileWriter = new FileWriter(diffPath);
                fileWriter.write(outputStream.toString());
                fileWriter.flush();
                fileWriter.close();
//                log.info(outputStream.toString());
                // Convert the diff content to a map
                diffMap = GitUtils.mapDiff2ContentWithLines(projectDir, outputStream.toString());

            }

            repository.close();
            log.info("get Diff Success");
        } catch (IOException e) {
            e.printStackTrace();
            log.info("Error getting diff: " + e.getMessage());
        }
        return diffMap;
    }


    public static void showAddLineAndDeleteLine(String result) {
        String[] segment;
        List<String> deleteList = new ArrayList<>();
        List<String> addList = new ArrayList<>();
        for (String s : result.split("\n")) {
            if (s.startsWith("+++")) {
                String ff = s.substring(s.indexOf('/') + 1).trim();
            }
        }
//        Pattern pattern = Pattern.compile("^@@ -[0-9]+(,[0-9]+)? \+([0-9]+)(,[0-9]+)? @@");
        segment = result.split("(diff --git.*\n.*\n.*\n.*\n.*\n?)|(@@.*@@)");
        List<String> change_segment = new ArrayList<>();
        for (String a : segment) {
            if (!a.equals("")) {
                change_segment.add(a);
            }
        }
        for (String ss : change_segment) {
            for (String s : ss.split("\n")) {
                if (s.startsWith("+")) {
                    addList.add(s);
                } else if (s.startsWith("-")) {
                    deleteList.add(s);
                }
            }
        }
        log.info("新增行：");
        for (String i : addList) {
            log.info(i);
        }
        log.info("删除行：");
        for (String i : deleteList) {
            log.info(i);
        }
    }

    public static void generateDiff(String project_dir) {
        String folderPath = project_dir + "\\diff_content";
        Path path = Paths.get(folderPath);
        try {
            // 使用Files.createDirectories方法创建文件夹（包括父文件夹）
            Files.createDirectories(path);
            log.info("Folder created successfully");
        } catch (IOException e) {
            log.info("Failed to create folder: " + e.getMessage());
        }

        String diffPath = folderPath + "/diff-" + getTime() + ".diff";
    }

    public static String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        return sdf.format(date);
    }

    /**
     * filter_not_java
     * 目前变更差异只考虑.java，暂不考虑配置文件等外部上下文
     */
    public static boolean filter_not_java(String file_path) {
        return !file_path.endsWith(".java");
    }

}

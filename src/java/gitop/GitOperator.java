package gitop;


import gitop.utils.AccurateUtils;
import gitop.utils.DiffRelatedMethodUtils;
import gitop.utils.GitUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * GitOperator
 *
 * @author Z.X
 */
@Slf4j
public class GitOperator {

    /**
     * checkout到指定commitId
     *
     * @param projectPath
     * @param commitId
     */
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
                throw new RuntimeException(e);
            }
            repository.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成diff文件
     *
     * @param projectPath
     * @param startCommit
     * @param endCommit
     */
    public static Map<String, Map<Integer, String>> generateDiff(String projectPath, String startCommit, String endCommit){
        // 判断CommitId是否为40位、是否存在
        if(startCommit.length() != 40 || endCommit.length() != 40){
            throw new RuntimeException("CommitId is not 40 characters");
        }
        Map<String, Map<Integer, String>> diffMap = new HashMap<>();
        String folderPath = projectPath + "\\diff_content";
        try {
            // Open the Git repository from the specified project directory
            Repository repository = new FileRepositoryBuilder()
                    .setGitDir(new File(projectPath + "/.git"))
                    .build();
            // Create a Git instance to interact with the repository
            try (Git git = new Git(repository)) {
                git.fetch().call();
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
                Path path = Paths.get(folderPath);
                try {
                    // 使用Files.createDirectories方法创建文件夹（包括父文件夹）
                    Files.createDirectories(path);
                    log.info("Folder {} created successfully", path);
                } catch (IOException e) {
                    log.info("Failed to create folder: " + e.getMessage());
                }
                String diffPath = folderPath + "/" + startCommit + "_" + endCommit + ".diff";
                // 保存diff文件
                FileWriter fileWriter = new FileWriter(diffPath);
                fileWriter.write(outputStream.toString());
                fileWriter.flush();
                fileWriter.close();
                // Convert the diff content to a map
                diffMap = GitUtils.mapDiff2ContentWithLines(projectPath, outputStream.toString());
            } catch (GitAPIException e) {
                throw new RuntimeException(e);
            }
            repository.close();
            log.info("get Diff Success");
        } catch (IOException e) {
            e.printStackTrace();
            log.info("Error getting diff: " + e.getMessage());
        }
        return diffMap;
    }

    public  Map<String, List<List<Integer>>> getChangeFileAndLines(DiffRelatedMethodUtils utils, Set<String> changeSet, String diffPath){
        String s = AccurateUtils.readFile(diffPath);
        Map<String, List<List<Integer>>> resultMap = utils.getChangeFileAndLines(s);
        System.out.println("Change File And Lines ResultMap: " + resultMap);
        return resultMap;
    }

}

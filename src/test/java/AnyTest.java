
import data.ClassInfo;
import data.DTO;
import data.MethodInfo;
import data.Project;
import jdt.ClassMethodsMapVisitor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.Test;
import util.BranchCounter;
import util.CodesStatistics;
import util.TestMethodCounter;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.io.MoreFiles.listFiles;
import static spat.ProcessMain.findGitDirectoryUpwards;
import static spat.git.GitUtils.readFile;

public class AnyTest {


    @Test
    public void test1() throws IOException {
        for (File file:new File("E:\\project-DataSets20240914").listFiles()){
            System.out.println(Objects.requireNonNull(findGitDirectoryUpwards(file)).getAbsolutePath());
        }

    }
    @Test
    public void test() {
       String[] items = readFile("D:\\JavaProject\\demo6\\src\\main\\resources\\1.txt").split("\n");

        Map<String, Integer> itemCount = new HashMap<>();

        for (String item : items) {
            // Increment the count if the item already exists, otherwise add it with a count of 1
            itemCount.put(item, itemCount.getOrDefault(item, 0) + 1);
        }

        // Print the counts
        for (Map.Entry<String, Integer> entry : itemCount.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    /**
     *  统计类下函数的名称、数量
     */
    @Test
    public void test2() {
        String projectPath = "E:\\project-DataSets1\\RuoYi";
        List<File> a = new ArrayList<>();
        for(String s : TestMethodCounter.findJavaSourceDirectories(projectPath)){
            TestMethodCounter.findTestFiles(new File(s),a);
        }
        Project project = new Project();
        project.setProjectName(projectPath.substring(projectPath.lastIndexOf("\\")+1));
        Map<String,String> classPathMap = new HashMap<>();
        for (File file : a){
            String classname = file.getAbsolutePath().
                    substring(file.getAbsolutePath().lastIndexOf("\\")+1,file.getAbsolutePath().length()-5);
            if(!classPathMap.containsKey(classname)){
                classPathMap.put(classname,file.getAbsolutePath());
            }else {
                classPathMap.put(classname+"1",file.getAbsolutePath());
            }

        }
        ClassMethodsMapVisitor classMethodsMapVisitor = new ClassMethodsMapVisitor();
//        classMethodsMapVisitor.showClassMethod(readFile(a.get(1).getAbsolutePath()));
        List<Map<String, List<String>>> resultList = new ArrayList<>();
        CodesStatistics cs = new CodesStatistics(new File(projectPath));
        for (File key :a){
            ClassInfo classInfo = new ClassInfo();
            String classPath =key.getAbsolutePath();
            String classname = classPath.
                                    substring(classPath.lastIndexOf("\\")+1,classPath.length()-5);
            classInfo.setClassName(classname);
            classInfo.setClassPath(classPath);
            // 遍历类中的所有方法
            Map<String, List<String>> map = classMethodsMapVisitor.showClassMethod(readFile(classPath));
            List<String> tmpList = map.get(classname);
            List<MethodInfo> methodInfoList = new ArrayList<>();
            if(tmpList==null){
                tmpList = new ArrayList<>();
            }else {
                tmpList.forEach(s -> {
                    MethodInfo methodInfo = new MethodInfo();
                    methodInfo.setMethodName(s);
                    methodInfoList.add(methodInfo);
                });
            }
            map.remove(classname);
            map.put(classPathMap.get(classname),tmpList);
            String type = "class";
            Map<String,Integer> methodBranchPaths = BranchCounter.countMethodBranchPaths(key.getAbsolutePath(),type);
            System.out.println(methodBranchPaths);
//            System.out.println(classname + " 类方法数量：" + tmpList.size());
            if(methodBranchPaths.isEmpty()){
                classInfo.setType("interface");
                classInfo.setMethodList(methodInfoList);
                project.getSrcClassList().add(classInfo);
//                System.out.println("接口类，只是声明，被实现，因此一般不包含分支路径，往往作为外部调用入口");
                continue;
            }
            classInfo.setType("class");
            methodInfoList.forEach(m->{
                if(methodBranchPaths.containsKey(m.getMethodName())){
                    m.setTestCaseCount(methodBranchPaths.get(m.getMethodName()));
                }
            });
//            System.out.println(map);
//            System.out.println("方法路径数（模拟-方法关联用例数）：" + methodBranchPaths);
            classInfo.setMethodList(methodInfoList);
            System.out.println(classInfo.getMethodList().stream().map(MethodInfo::getTestCaseCount).collect(Collectors.toList()));
//            classInfo.getMethodList().forEach(m-> {
//                if(m.getTestCaseCount()>=65536){
//                    System.out.println(m.getTestCaseCount());
//                    System.out.println(classInfo.getClassPath()+": "+m.getMethodName());
//                }
//            });
            System.out.println(classInfo.getMethodList().stream().mapToInt(MethodInfo::getTestCaseCount).sum());
            classInfo.setTestCaseCount(classInfo.getMethodList().stream().mapToInt(MethodInfo::getTestCaseCount).sum());
            project.getSrcClassList().add(classInfo);
        }
        project.setTestCaseSum(project.getSrcClassList().stream().mapToInt(ClassInfo::getTestCaseCount).sum());
        project.setTotalLines(cs.countLines());
        project.setCommentLines(cs.countCommentLines());
        project.setValidLines(cs.countValidLines());
        project.setWhiteSpaceLines(cs.countWhiteSpaceLines());
//        System.out.println(project);
        List<ClassInfo> result = DTO.querySomeTypeClasses(project, "interface");
    }

    @Test
    public void testMergeCommit() throws IOException, GitAPIException {
        String projectDir = "D:\\JavaProject\\coming\\refactoring-toy-example";
        // 打开本地仓库
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder
                .setGitDir(new File(projectDir + File.separator +".git"))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build();

        Git git = new Git(repository);

        // 获取最近的9个提交
        Iterable<RevCommit> commits = git.log().all().setMaxCount(6).call();

        List<RevCommit> commitList = new ArrayList<>();
        for (RevCommit commit : commits) {
            commitList.add(commit);
        }

        // 按3个一组进行合并提交
        for (int i = 0; i < commitList.size(); i +=3) {
            mergeCommits(git, commitList.subList(i, i + 3));
        }

        // 关闭资源
        git.close();
        repository.close();
    }

    // 合并提交的方法
    private static void mergeCommits(Git git, List<RevCommit> commits) throws GitAPIException, IOException {
        if (commits.size() != 3) {
            return;
        }

        // 将 HEAD 重置为更早的提交（保留工作区变更）
        String commitToReset = commits.get(2).getName();
        git.reset().setMode(org.eclipse.jgit.api.ResetCommand.ResetType.SOFT).setRef(commitToReset).call();

        // 添加所有更改
        git.add().addFilepattern(".").call();

        // 合并提交信息
        StringBuilder commitMessage = new StringBuilder();
        for (RevCommit commit : commits) {
            commitMessage.append(commit.getFullMessage()).append("；");
        }

        // 提交新的合并后的提交
        git.commit().setMessage(commitMessage.toString()).call();

        System.out.println("合并了提交：" + commits.stream().map(RevCommit::getFullMessage).collect(Collectors.toList()) + " 到 " + commits.get(2).getFullMessage());
    }
}

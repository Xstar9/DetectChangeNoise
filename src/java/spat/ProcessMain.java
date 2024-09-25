package spat;

import com.opencsv.CSVWriter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import spat.git.GitUtils;
import spat.rules.TransformationType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static spat.ManyMain.trans_commits_report_dir;
import static spat.ManyMain.writeCloneErrorToCSV;
import static spat.git.GitUtils.*;

public class ProcessMain {
    public static String projectList = "E:\\projectDataSet\\spring-hateoas\\list1.csv";

    public static String projectLocalPath = "E:\\projectDataSet\\spring-hateoas\\";

    public static String currentDir;

    public static String commitListPath = projectLocalPath + currentDir + "commitList.csv";

    public static int count = 0;

    public static List<Integer> successTransformCommit = new ArrayList<>();

    public static List<Integer> failedTransformCommit = new ArrayList<>();
    private static final String error_trans_file = "D:\\JavaProject\\SPAT\\trans-error.csv";;
    private static final String success_trans_file = "D:\\JavaProject\\SPAT\\commits-dataset0.csv";

    // TODO：后续可以实现两次以上（最多5次）的转变，然后打乱数据集；因为一次转换影响的仅仅一个方法；剩下的就人工手动构造（10%-20%/或结合GPT）


    public static void main(String[] args) throws IOException {
        // 选择基数据集（项目存储库-clone本地-包含一定量的关于源代码更改相关的提交）
//        GitUtils.readFile(projectList);
//        String gitRepositoryPath = readFile(projectList).split("\n")[0];
//        currentDir = gitRepositoryPath.substring(gitRepositoryPath.lastIndexOf("/") + 1, gitRepositoryPath.length() - 4);
//        String localPath = projectLocalPath + currentDir;
        currentDir = "copycat";//"spring-hateoas";
        String localPath = "E:\\projectDataSet\\" + currentDir;
        List<String> srcLocatedList = findJavaSourceDirectories(localPath);
        int random0 = 0;
        if(srcLocatedList.size()>2){
            SecureRandom rand = new SecureRandom();// 18种转换规则
            random0 = rand.nextInt(srcLocatedList.size());
        }else if(srcLocatedList.isEmpty()){
            writeCloneErrorToCSV(trans_commits_report_dir+"clone_fail_projects.csv",new String[]{"https://github.com/kuujo/copycat.git"});
            return;
        }
        localPath = srcLocatedList.get(random0);
        localPath = localPath.substring(0, localPath.length() - 14);
        currentDir = localPath.substring(localPath.lastIndexOf("\\") + 1);
        // 项目的历史提交可以作为挖掘变更模式的基础 TODO: 数据集式修改改成循环即可
//        GitUtils.fetchGit(gitRepositoryPath, localPath, "");
//        List<String> allCommit = GitUtils.getGitAllCommit(localPath);
        //  获取源项目路径---用来转换所需的输入
        Path path = Paths.get(localPath).toAbsolutePath();
        List<String> javaDirectory = GitUtils.findJavaDirectories(path);
        javaDirectory = javaDirectory.stream()
                .filter(GitUtils::isUnderTestDirectory)
                .collect(Collectors.toList());
//        int random = new Random(17).nextInt(); // 17种转换规则
        args = new String[4];
        int current = 1;

        boolean forAndWhile = false;


        // 循环次数在使用random时，有效；不使用random选择规则进行调试时，则是一直重复这个规则。所以设置为1即可；
        // TODO：先制作100-500条看效果和局部实验
        for (int j = 0; j < 50; j++) {
            count = j;
            SecureRandom rand = new SecureRandom();// 18种转换规则
            int random1 = rand.nextInt(18);
//            args[0] = "2";
            args[0] = String.valueOf(random1); //		args[0] = String.valueOf(random1);
            args[1] = javaDirectory.get(0); // "D:\\JavaProject\\jta\\tmp\\static-dynamic-diff\\src\\main\\java";
            args[2] = "D:\\JavaProject\\SPAT\\transform-output\\" + currentDir + "\\";
            args[3] = "C:\\Program Files\\Java\\jdk1.8.0_271\\jre\\lib\\rt.jar";

//            if(args[0].equals("1") && !forAndWhile){ // 如果上一次转换是for（初始状态false），则下一次转换是while；如果第一次随机为1，false，不会执行
//                forAndWhile = true;
//                args[0] = "2";
//            }else if (args[0].equals("2") && forAndWhile){
//                // 如果上一次转换是for（初始状态false），则下一次转换是while; 如果第一次随机为2，false，则会直接执行规则2
//                forAndWhile = false;
//                args[0] = "1";
//            }
            // 规则转换---引入等价替换噪声（扰动）
            // 1. 17种随机变换规则  单（原子）扰动添加--（0-n处）
            // 2. （2-3）/17 种扰动复合转换  复合变更
            // 转换输出：被转换程序的路径    转换后的路径
            Utils.maxTrans = 2;
            String dirOfTheFiles = args[1];
            String outputDir = args[2];
            ArrayList<String> jre_rtPath = new ArrayList<String>();
            for (int i = 3; i < args.length; i++) {
                jre_rtPath.add(args[i]);
            }
            ParseFilesInDir(dirOfTheFiles, outputDir, Utils.ArryStr2priStrList(jre_rtPath), args[0]);
            System.out.println("第"+j+"次");
        }
        System.out.println("successTransformCommit： "+successTransformCommit);
        System.out.println("failTransformCommit： "+failedTransformCommit);
//        args[0] = "0"; //		args[0] = String.valueOf(random);
//        args[1] = javaDirectory.get(0); // "D:\\JavaProject\\jta\\tmp\\static-dynamic-diff\\src\\main\\java";
//        args[2] = "D:\\JavaProject\\SPAT\\transform-output\\" + currentDir + "\\";
//        args[3] = "C:\\Program Files\\Java\\jdk1.8.0_271\\jre\\lib\\rt.jar";
//        // 规则转换---引入等价替换噪声（扰动）
//        // 1. 17种随机变换规则  单（原子）扰动添加--（0-n处）
//        // 2. （2-3）/17 种扰动复合转换  复合变更
//        // 转换输出：被转换程序的路径    转换后的路径
//        Utils.maxTrans = 3;
//        String dirOfTheFiles = args[1];
//        String outputDir = args[2];
//        ArrayList<String> jre_rtPath = new ArrayList<String>();
//        for (int i = 3; i < args.length; i++) {
//            jre_rtPath.add(args[i]);
//        }
//        ParseFilesInDir(dirOfTheFiles, outputDir, Utils.ArryStr2priStrList(jre_rtPath), args[0]);

        // 记录被转换状态的提交id
        // 将转换后的程序 临时覆盖掉本地的源代码 执行git 提交命令; 记录提交id
        // 切回原始状态的提交id （设置为master） 或 记录前后的提交id对

        //
    }

    public void transform2Commit() throws IOException {

    }

    public static String getOutputPath(String filePath, String dirPath, String outputDir) {
        String relativePath = filePath.substring(dirPath.length() + 1);
//        System.out.println("Utils1: "+filePath);
//        System.out.println("Utils1: "+dirPath);
//        System.out.println("Utils2: "+relativePath);
//        System.out.println("Utils3: "+outputDir);
        return outputDir + relativePath;
    }

    // loop directory to get file list
    public static void ParseFilesInDir(String dirPath, String outputDir, String[] arrString, String idOfRule) throws IOException {
        boolean isOnce = true;
        boolean isHasChange = false;
        File root = new File(dirPath);
        File[] files = Utils.folderMethod(root.getAbsolutePath(), outputDir);
        List<File> fileList = Arrays.stream(files)
                .filter(File::isFile)
                .collect(Collectors.toList());

        // 打乱，随机，避免每次都是转换同一个文件
        Collections.shuffle(fileList);
//        List<File> selectedFiles = fileList.stream().limit(2).collect(Collectors.toList());
        Set<File> fileSet = new HashSet<File>(fileList);
        ForkJoinPool myPool = new ForkJoinPool(32);

//            myPool.submit(() ->
        int k = 0;
        for (File f : fileSet) {
            k++;
            String filePath = f.getAbsolutePath();
//                System.out.println(filePath);
//            try {
            // 如果未成功转换文件的规则，则继续
            parse(Utils.readFileToString(filePath), dirPath, outputDir, arrString, idOfRule, filePath);
            // 如果成功转换了文件，则跳出循环；即结束对每个文件符合规则都进行转换，达到只转换一次的目的---产生变更
            if (new File(getOutputPath(filePath, dirPath, outputDir)).exists() && !readFile(getOutputPath(filePath, dirPath, outputDir)).isEmpty()) {
                isHasChange = true;
                break;
            }

            //                    System.out.println("Output Dir:  "+outputDir);
//            } catch (Exception e) {
//                System.out.println("trans failed: " + filePath);
//                e.printStackTrace();
//            } catch (Error s) {
//                System.out.println("trans failed: " + s.toString());
//                s.printStackTrace();
//            }
        }
//        fileSet.parallelStream().forEach(f -> {
//                String filePath = f.getAbsolutePath();
////                System.out.println(filePath);
//                try {
//                    parse(Utils.readFileToString(filePath), dirPath, outputDir, arrString, idOfRule, filePath);
////                    System.out.println("Output Dir:  "+outputDir);
//                } catch (Exception e) {
//                    System.out.println("trans failed: " + filePath);
//                    e.printStackTrace();
//                } catch (Error s) {
//                    System.out.println("trans failed: " + s.toString());
//                    s.printStackTrace();
//                }
//            });
//            ).get();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
        List<String> changeFiles = new ArrayList<>();
        if (isHasChange) {
            for (File file : fileSet) {
                String prePath = file.getAbsolutePath();
                String postPath = outputDir + prePath.substring(prePath.indexOf("\\src\\main\\java\\") + 15); // 15个字符是\src\main\java\
                System.out.println(prePath.substring(prePath.indexOf("\\src\\main\\java\\") + 15));
                System.out.println("===========================================" + postPath);
                // todo: 仅仅只是调试需注释掉，因为会覆写源文件
                // 需要转换的临时文件存在（证明规则转换成功，并写入文件了）
                if (new File(postPath).exists() && new File(prePath).exists()) {
                    changeFiles.add(prePath);
                    writeToFile(prePath, readFile(postPath));
                }
            }
            // 初始化 Git 仓库
            try (Git git = Git.open(findGitDirectoryUpwards(new File(dirPath)))) {
                // 获取转换前的提交ID
                String preCommitId = getCurrentCommitId(git);
                // 检查是否有任何变更
                Status status = git.status().call();
                if (!status.hasUncommittedChanges()) {
                    failedTransformCommit.add(count);
                    writeErrorToCSV(error_trans_file,new String[]{String.valueOf(count), currentDir,
                             TransformationType.fromCode(Integer.parseInt(idOfRule)),idOfRule,"No changes detected---no transform"});
                    System.out.println("No changes detected. Skipping commit.");
                }else {
                    git.add().addFilepattern(".").call();
                    git.commit().setMessage("Automated conversion of"+ TransformationType.fromCode(Integer.parseInt(idOfRule))
                            +" TransformType In Class" + changeFiles.toString()).call();

                    // 获取转换后的提交ID
                    String postCommitId = getCurrentCommitId(git);

                    // 记录提交前后的提交ID对
                    System.out.println("Pre-commit ID: " + preCommitId);
                    System.out.println("Post-commit ID: " + postCommitId);

                    writeDataToCSV(success_trans_file, new String[]{String.valueOf(count), currentDir,
                            preCommitId, postCommitId, TransformationType.fromCode(Integer.parseInt(idOfRule)),idOfRule,"1","1"});
                    successTransformCommit.add(count);
                }
                // 提交转换后的代码

            } catch (GitAPIException e) {
                throw new RuntimeException(e);
            }
        }else {
            System.out.println("TransForm Fail!!!!!!!! please Check~  About: "+idOfRule+"  dirPath: "+dirPath + "  outputDir: "+outputDir);
        }

        // 写入转换后的代码覆盖原始文件


    }

    // 选择10-20个项目（webgoat可考虑？），因为研究目标主要在于提交变更的情况，项目类型本身的性质倾向要小一些（更多是体现研究项目多样性（编辑器、ide、系统...））
    // 每个项目进行50-100次转换提交，在17种转换规则中随机选择；总体（100-500-1000-2000）
    // 每次转换（变更）情况：1. 单种转换（如重命名1-3次）（70%）；2. 2-3种合适的转换类型复合；（20%）3. 少量多转换变更（10%）
    public static void parse(String str, String dirPath, String outputdir, String[] arrString, String IdOfRule, String filePath) {
        Document document = new Document(str);
        ASTParser parser = ASTParser.newParser(AST.JLS13);
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        parser.setBindingsRecovery(true);

        Map<String, String> options = JavaCore.getOptions();
        parser.setCompilerOptions(options);

        String unitName = "Apple.java"; // Just some random name.
        parser.setUnitName(unitName);

        String[] sources = {""}; // Just the file itself.
        String[] classpath = arrString;

        parser.setEnvironment(classpath, sources, new String[]{"UTF-8"}, true);
        parser.setSource(str.toCharArray());

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        String outputPath = getOutputPath(filePath, dirPath, outputdir);
//        System.out.println(outputPath);
        cu.accept(RuleSelector.create(IdOfRule, cu, document, outputPath));
    }

    public static void writeDataToCSV(String filePath, String[] data) {
        File file = new File(filePath);
        boolean fileExists = file.exists();
        CSVWriter writer = null;

        try {
            FileWriter outputFile = new FileWriter(filePath, true); // true 表示追加写入
            writer = new CSVWriter(outputFile);

            // 如果文件不存在，写入表头
            if (!fileExists) {
                // ToDO:  除了混合和变量重命名，默认RelatedMethodsNum是1；还需补充真实相关的方法（有哪些），即预期的变更影响方法集+数量
                String[] header = {"id", "projectName", "startCommit", "endCommit", "transformType","transformTypeId", "transformSum", "RelatedMethodsNum"};
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

    public static void writeErrorToCSV(String filePath, String[] data) {
        File file = new File(filePath);
        boolean fileExists = file.exists();
        CSVWriter writer = null;

        try {
            FileWriter outputFile = new FileWriter(filePath, true); // true 表示追加写入
            writer = new CSVWriter(outputFile);

            // 如果文件不存在，写入表头
            if (!fileExists) {
                // ToDO:  除了混合和变量重命名，默认RelatedMethodsNum是1；还需补充真实相关的方法（有哪些），即预期的变更影响方法集+数量
                String[] header = {"id", "projectName", "transformType","transformTypeId", "ErrorReason"};
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

    public static File findGitDirectoryUpwards(File directory) {
        File currentDir = directory;
        while (currentDir != null) {
            File gitDir = new File(currentDir, ".git");
            if (gitDir.exists() && gitDir.isDirectory()) {
                return new File(gitDir.getAbsolutePath().substring(0, gitDir.getAbsolutePath().length() - 4));
            }
            currentDir = currentDir.getParentFile();
        }
        return null; // 如果没找到 .git 目录，返回 null
    }

    public static List<String> findJavaSourceDirectories(String rootDirectory) {
        Path rootPath = Paths.get(rootDirectory);

        try (Stream<Path> paths = Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS)) {
            List<Path> result = paths.filter(Files::isDirectory)
                    .filter(path -> path.endsWith("src/main/java")).collect(Collectors.toList());
//                    .forEach(path -> System.out.println("Found: " + path.toAbsolutePath()));
            return result.stream().map(Path::toString).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return List.of(rootDirectory);
    }


    public static void randomTransformStrategy() {

    }

}

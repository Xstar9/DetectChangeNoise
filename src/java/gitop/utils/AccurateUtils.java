package gitop.utils;

import gitop.ast.MethodOverloadVisitor;
import lombok.extern.slf4j.Slf4j;

import org.apache.maven.shared.invoker.*;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;


/**
 * AccurateUtils
 *
 * @author Z.X
 */
@Slf4j
public class AccurateUtils {

//    public static void main(String[] args) {
//        operationMavenCommand("D:/maven/apache-maven-3.8.2",
//                "D:\\JavaProject\\jta\\tmp\\WebGoat\\pom.xml",
//                "package");
//        log.info(readBusinessJar("D:\\JavaProject\\jta\\tmp\\RuoYi\\ruoyi-admin\\target\\ruoyi-admin.jar")
//                .toString());
//    }

    /**
     * 获取方法、参数的全限定名
     * @param changeMethodSet 转换前的方法名格式
     * @param className 类全限定名
     * @param content1 参考文件内容1的import路径
     * @param content2 参考文件内容2的import路径
     * @param changeSet 转换后的方法名格式
     */
    public static void normalizeFqMethodName(Set<String> changeMethodSet, String className, String content1, String content2, Set<String> changeSet) {
        for (String method: changeMethodSet){
            String fullMethod = createFullMethod(method, className);
            // 填充参数为全限定名称
            String[] args = fullMethod.substring(fullMethod.lastIndexOf("(")+1, fullMethod.lastIndexOf(")")).split(",");
            List<String> argsList = new ArrayList<>();
            for (String it: args){
                if (null != it && !it.trim().isEmpty()){
                    argsList.add(it);
                }
            }
            StringBuilder agrBuilder = new StringBuilder();
            for (int ii=0; ii<argsList.size(); ++ii){
                if (ii>0){
                    agrBuilder.append(",");
                }
                String arg = argsList.get(ii);
                // 是不是自己
                if (className.endsWith("." + arg)){
                    agrBuilder.append(className);
                    continue;
                }
                // 从本地类导入的查找
                Map<String, String> imports = extractImport(content1);
                imports.putAll(extractImport(content2));
                String fullArg = imports.get(arg);
                if (null != fullArg){
                    agrBuilder.append(fullArg);
                    continue;
                }
                // 从基本类中查找
                fullArg = exchange(arg);
                if (null != fullArg){
                    agrBuilder.append(fullArg);
                    continue;
                }
                // 本类的内部类
                Set<String> innerClass = extractClass(arg);
                if (innerClass.contains(arg)){
                    agrBuilder.append(className).append(".").append(arg);
                    continue;
                }
                // 同包类
                agrBuilder.append(className.substring(0, className.lastIndexOf("."))).append(".").append(arg);
            }
            fullMethod = fullMethod.substring(0, fullMethod.lastIndexOf("(")) + "(" + agrBuilder.toString() + ")";
            changeSet.add(fullMethod);
        }
    }

    /**
     * 返回全限定形式
     *
     * @param method VulXxeApi print (String,...) 形式
     * @param className com.bci.VulXxeApi形式
     * @return com.bci.VulXxeApi:print(String,...)
     */
    private static String createFullMethod(String method, String className){
        String [] tmp = method.trim().split(" ");
        List<String> t = new ArrayList<>();
        for (String it: tmp){
            if (null != it && !it.trim().isEmpty()){
                t.add(it);
            }
        }
        StringBuilder res = new StringBuilder(className);
        res.append(":").append(t.get(1));
        for (int i=2; i< t.size(); ++i){
            res.append(t.get(i));
        }

        return res.toString();
    }

    private static Set<String> extractClass(String javaTxt){
        String[] lines = javaTxt.split("\n");
        Set<String>  rs = new HashSet<>();
        for (String line: lines){
            if (null != line &&
                    line.trim().contains("class ") &&
                    !line.contains(".") &&
                    !line.contains("\"") &&
                    !line.contains("'")){
                line = line.replace("static", "").replace("{", "");
                line = line.substring(line.indexOf("class") + 4).trim() ;
                String clsName = line.split(" ")[0];
                rs.add(clsName);
            }
        }
        return rs;
    }

    private static String exchange(String agr){
        switch (agr){
            case "byte": return "byte";
            case "Byte": return "java.lang.Byte";
            case "short": return "short";
            case "Short": return "java.lang.Short";
            case "int": return "int";
            case "Integer": return "java.lang.Integer";
            case "long": return "long";
            case "Long": return "java.lang.Long";
            case "float": return "float";
            case "Float": return "java.lang.Float";
            case "double": return "double";
            case "Double": return "java.lang.Double";
            case "boolean": return "boolean";
            case "Boolean": return "java.lang.Boolean";
            case "char": return "char";
            case "Character": return "java.lang.Character";
            case "String": return "java.lang.String";

        }
        return null;
    }

    /**
     * 提取java文件所有的import
     *
     * @param javaTxt
     * @return 本文所有的import
     */
    private static Map<String, String> extractImport(String javaTxt){
        String[] lines = javaTxt.split("\n");
        Map<String, String>  imports = new HashMap<>();
        for (String line: lines){
            if (null != line && line.trim().startsWith("import ")){
                String tmp = line.trim().substring(7).split(";")[0];
                if (!tmp.startsWith("static ")){ // 静态方法导入
                    imports.put(tmp.substring(tmp.lastIndexOf(".")+1), tmp);
                }
            }
        }
        return imports;
    }

    /**
     * 提取java文件所有的包
     *
     * @param javaTxt
     * @return 本文所有的import
     */
    private static String extractPackage(String javaTxt){
        String[] lines = javaTxt.split("\n");
        for (String line: lines){
            if (null != line && line.trim().startsWith("package ")){
                String tmp = line.trim().substring(8).split(";")[0];
                return tmp;
            }
        }
        return "";
    }

    public static void operationMavenCommand(String mavenPath, String pomPath, String command) {
        String javaHome = "C:\\Program Files\\Java\\jdk1.8.0_271";
        InvocationRequest request = new DefaultInvocationRequest();
        request.setJavaHome(new File(javaHome));
        request.setPomFile(new File(pomPath));
        request.setGoals(Collections.singletonList(command));
        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(mavenPath));
        invoker.setLogger(new PrintStreamLogger(System.err, InvokerLogger.DEBUG) {});
        invoker.setOutputHandler(System.out::println);
        try {
            InvocationResult result = invoker.execute(request);
            int exitCode = result.getExitCode();
            log.info("=========================================================================");
            if (0==exitCode){
                log.info("build process succeeds!");
            }else{
                log.info("build process fails!");
            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }
    }

    public static String getFullPath(String projectRoot, String fileName) {
        if (!fileName.startsWith("/")) {
            fileName = "/" + fileName;
        }
        if (!fileName.contains(projectRoot)) {
            fileName = projectRoot + fileName;
        }
        return fileName;
    }

    public static List<String> fileToLines(String filename) {
        List<String> lines = new LinkedList<String>();
        String line;
        try {
            InputStream is = Files.newInputStream(Paths.get(filename));
            int BUFFER_SIZE = 8192;

            BufferedReader in = new BufferedReader(new InputStreamReader(is, Charset.forName("ISO-8859-1")), BUFFER_SIZE);
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            log.error(e.toString());
        }
        return lines;
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

    /**
     * readFile
     * 封装FileReader读取文件方法
     *
     */
    public static String readFile1(String path){
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);

        } catch (IOException e) {
            e.printStackTrace(); // 可以根据实际情况处理异常
        }
        return content.toString();
    }

    /**
     * 写入内容到文件
     *
     * @param fileName 要写入的文件名
     * @param content 要写入的内容
     */
    public static void writeFile(String fileName, String content) {
        BufferedWriter writer = null;
        try {
            // 创建BufferedWriter对象
            writer = new BufferedWriter(new FileWriter(fileName));
            // 写入内容
            writer.write(content);
            System.out.println("反编译内容已成功写入文件: " + fileName);
        } catch (IOException e) {
            // 捕获IO异常并打印错误信息
            System.err.println("反编译写入文件时发生错误: " + e.getMessage());
        } finally {
            // 确保BufferedWriter在程序结束时被关闭
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("关闭BufferedWriter时发生错误: " + e.getMessage());
                }
            }
        }
    }

    public static void mapMethodInfo2File(Map<String,String> map) {
        map.forEach((k,v)->{writeFile("D:\\JavaProject\\jta\\result"+"\\"+k.replace(":",".")+".txt",v);});
    }

//    public static void main(String[] args) {
//        detectOverloadMethodInfo2File("D:\\JavaProject\\jta\\tmp-ad2e87ee4be82d34\\static-dynamic-diff-1.0-SNAPSHOT.jar.decompiled\\com\\bci\\overload\\OverLoadDemo.java");
//    }

//    public static void detectOverloadMethodInfo2File(String path) {
//        String source = readFile(path);
//        Map<String, List<String>> overloadedMethods = getOverloadedMethods(source);
//        if(overloadedMethods.isEmpty()){
//            return;
//        }
//        for (Map.Entry<String, List<String>> entry : overloadedMethods.entrySet()) {
//            log.info("Class: " + entry.getKey());
//            for (String overloadInfo : entry.getValue()) {
//                log.info(overloadInfo);
//            }
//            List<String> changedMethods = entry.getValue();
//            Set<String> changedMethodSet = new TreeSet<>(changedMethods);
//            Set<String> changedSet = new TreeSet<>();
//            normalizeFqMethodName(changedMethodSet,entry.getKey(),readFile(path),readFile(path),changedSet);
//            List<String> tmpList = new ArrayList<>(changedSet);
//            writeFile("D:\\JavaProject\\jta\\result"+"\\"
//                            + (tmpList.get(0).substring(0,tmpList.get(0).indexOf("(")+1)+")"+"-overloadList")
//                                                 .replace(":",".")+".txt"
//                            , String.join("\n", tmpList));
//        }
//
//
//    }


    public static Map<String, List<String>> getOverloadedMethods(String source) {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(source.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        MethodOverloadVisitor visitor = new MethodOverloadVisitor(cu);
        cu.accept(visitor);

        return visitor.getOverloadedMethodsMap();
    }

    /**
     * scanClassFiles
     * 扫描指定目录下的所有class文件
     *
     */
    public static List<String> scanClassFiles(String directoryPath) {
        List<String> filePaths = new ArrayList<>();
        scanClassFiles(new File(directoryPath), filePaths);
        return filePaths;
    }

    private static void scanClassFiles(File directory, List<String> filePaths) {
        // 检查目录是否存在
        if (directory.exists() && directory.isDirectory()) {
            // 获取目录中的所有文件和子目录
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        // 如果是子目录，递归调用遍历方法
                        scanClassFiles(file, filePaths);
                    } else if (file.isFile() && file.getName().endsWith(".class")) {
                        // 如果是以 ".class" 结尾的文件，将文件路径存入List
                        filePaths.add(file.getAbsolutePath());
                    }
                }
            }
        } else {
            log.info("Invalid directory path: " + directory.getPath());
        }
    }

    public static String calculateFileHash(String content) throws NoSuchAlgorithmException {

        byte[] fileBytes = content.getBytes(StandardCharsets.UTF_8);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(fileBytes);

        StringBuilder hexString = new StringBuilder();
        for (byte hashByte : hashBytes) {
            String hex = Integer.toHexString(0xff & hashByte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static void scanJavaFiles(File directory, List<String> filePaths) {
        // 检查目录是否存在
        if (directory.exists() && directory.isDirectory()) {
            // 获取目录中的所有文件和子目录
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        // 如果是子目录，递归调用遍历方法
                        scanJavaFiles(file, filePaths);
                    } else if (file.isFile() && file.getName().endsWith(".java")) {
                        // 如果是以 ".class" 结尾的文件，将文件路径存入List
                        filePaths.add(file.getAbsolutePath().replace("\\", "/").replace("//", "/"));
                    }
                }
            }
        } else {
            log.info("Invalid directory path: " + directory.getPath());
        }
    }

    public static boolean isChangeInMethodRange(int start1, int end1, int start2, int end2) {
        return start2 <= start1 && end2 >= end1 && (start1 <= end1 && start2 <= end2);
    }

    /**
     * readClassFile
     * @Output: Line Number and Line content
     *
     */
    public static String readClassFile(String path) {
        String filePath = "D:\\JavaProject\\ForMyIdea\\homework\\tmp\\refactoring-toy-example" +
                                        "\\src\\org\\DogManager.java"; // = path;

        try (LineNumberReader reader = new LineNumberReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int lineNumber = reader.getLineNumber();
                log.info("Line {} : {}", lineNumber, line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String normalize(String type) {
        return type.replace("Ljava/lang/String;", "String;")
                    .replace("Ljava/lang/int;", "int;")
                    .replace("I", "int;")
                    .replace("Z", "boolean;")
                    .replace("V", "void;")
//                    .replace("oid;","void;")// 可能是Object值传null的情况
                    .replace("Ljava/lang/void;","void;")
                    .replace("[","[]")
                    .replace("Ljava/util/Map;","Map;")
                    .replace("Ljava/util/List;","List;")
                    .replace("Ljava/util/Set;","Set;")
                    .replace("Ljava/io/File;","File;")
                    .replace("Ljava/io/int;nputStream;","inputStream");
    }

    public static void detectContainsOverLoadClass() {

    }
}

package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestMethodCounter {

    public static void main(String[] args) {
        String projectPath = "E:\\project-DataSet\\spring-hateoas";  // 设置你的项目路径
        List<String> srcLocatedList = findJavaTestSourceDirectories(projectPath);
        List<File> testFiles = new ArrayList<>();
        for (String srcLocated : srcLocatedList){
            findTestFiles(new File(srcLocated), testFiles);

            // 使用Map保存测试类及其方法数量
            Map<String, Integer> testClassMethodCount = new HashMap<>();
            Map<String,List<String>> aaa = new HashMap<>();
            int totalTestMethodCount = 0;

            // 正则表达式
            Pattern classPattern = Pattern.compile("\\bpublic\\s+class\\s+(\\w+)Test\\s+[extends\\s+TestCase\\s*]*\\{");
            Pattern methodPattern = Pattern.compile("\\bpublic\\s+void\\s+test\\w+\\s*\\(\\s*\\)\\s*\\{");
            Pattern methodPattern1 = Pattern.compile("\\bpublic\\s+void\\s+\\w+Test\\s*\\(\\s*\\)\\s*\\{");

            for (File file : testFiles) {
                totalTestMethodCount+=countTestMethodsInFile(file);
                try {
                    List<String> lines = Files.readAllLines(file.toPath());
                    String className = null;
                    int methodCount = 0;

                    for (String line : lines) {
                        Matcher classMatcher = classPattern.matcher(line);
                        Matcher methodMatcher = methodPattern.matcher(line);
                        Matcher methodMatcher1 = methodPattern1.matcher(line);

                        if (classMatcher.find()) {
                            className = classMatcher.group(1);
                        }
                        else {
                            continue;
                        }

                        if (methodMatcher.find()) {
                            methodCount++;
                            String methodName = methodMatcher.group(0);
                            if(!aaa.containsKey(className)){
                                List<String> tmp = new ArrayList<>();
                                tmp.add(methodName);
                                aaa.put(className,tmp);
                            }else {
                                aaa.get(className).add(methodName);
                            }
                        }else if(methodMatcher1.find()){
                            methodCount++;
                            String methodName = methodMatcher1.group(0);
                            if(!aaa.containsKey(className)){
                                List<String> tmp = new ArrayList<>();
                                tmp.add(methodName);
                                aaa.put(className,tmp);
                            }else {
                                aaa.get(className).add(methodName);
                            }
                        }
                    }

                    if (className != null) {
                        testClassMethodCount.put(className, methodCount);
                        totalTestMethodCount += methodCount;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 输出每个测试类的测试方法数量
            testClassMethodCount.forEach((className, count) -> {
                System.out.println("Test Class: " + className + " - Number of test methods: " + count);
                System.out.println(aaa.get(className));
            });

            // 输出测试方法的总数
            System.out.println(srcLocated + " Total number of test methods: " + totalTestMethodCount);
        }
    }


    private static int countTestMethodsInFile(File file) {
        int count = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isTest = false;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                // 检查是否包含@Test注解
                if (line.startsWith("@Test")) {
                    isTest = true;
                }

                // 如果下一个非空行是方法定义，并且之前有@Test注解，则计数
                if (isTest && line.matches("public\\s+void\\s+\\w+\\(.*\\)\\s*\\{")) {
                    count++;
                    isTest = false; // 重置，准备下一个方法的统计
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return count;
    }

    public static void findTestFiles(File root, List<File> testFiles) {
        if (root.isDirectory()) {
            for (File file : root.listFiles()) {
                findTestFiles(file, testFiles);
            }
        } else if (root.isFile() && root.getName().endsWith(".java")) {
            testFiles.add(root);
        }
    }

    public static List<String> findJavaTestSourceDirectories(String rootDirectory) {
        Path rootPath = Paths.get(rootDirectory);

        try (Stream<Path> paths = Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS)) {
            List<Path> result = paths.filter(Files::isDirectory)
                    .filter(path -> path.endsWith("src/test/java")).collect(Collectors.toList());
//                    .forEach(path -> System.out.println("Found: " + path.toAbsolutePath()));
            return result.stream().map(Path::toString).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return List.of(rootDirectory);
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
}


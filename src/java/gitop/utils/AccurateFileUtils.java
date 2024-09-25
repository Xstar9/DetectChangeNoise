package gitop.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;


/**
 * AccurateFileUtils
 *
 * @author Z.X
 */
@Slf4j
public class AccurateFileUtils {
    public static boolean IS_NEED_FILTER = true; // 可选过滤（lambda、init、clinit、非txt（如.md,或非存储jacg指定后缀的文件））

    /**
     * 扫描指定目录下的所有jacg结果文件
     *
     * @param directoryPath
     * @return 包含所有jacg结果文件路径的列表
     */
    public static List<String> scanJacgResultFiles(String directoryPath) {
        log.info("启动jacg获取调用层次图......");
        List<String> filePaths = new ArrayList<>();
        listTxtFiles(new File(directoryPath), filePaths);
        log.info("jacg获取调用层次图完毕~");
        return filePaths;
    }

    /**
     * 将一个目录及其所有内容移动到另一个目录下。
     *
     * @param sourceDirectory      源目录，需要移动的目录。
     * @param destinationDirectory 目标目录，将源目录移动到此目录下。
     */
    public static void moveDirectory(File sourceDirectory, File destinationDirectory) throws IOException {
        if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
            throw new IllegalArgumentException("Source directory does not exist or is not a directory: " + sourceDirectory);
        }
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdirs();
        }
        File[] files = sourceDirectory.listFiles();
        // 递归地移动文件和子目录
        for (File file : files) {
            File destinationFile = new File(destinationDirectory, file.getName());
            if (file.isDirectory()) {
                // 如果是子目录，递归地移动子目录
                moveDirectory(file, destinationFile);
            } else {
                // 如果是文件，移动文件
                if (!file.renameTo(destinationFile)) {
                    throw new IOException("Failed to move file: " + file.getAbsolutePath());
                }
            }
        }
        // 移动完所有文件后，删除源目录
        if (!sourceDirectory.delete()) {
            throw new IOException("Failed to delete source directory: " + sourceDirectory.getAbsolutePath());
        }
    }

    /**
     * 获取指定目录下所有.class文件的路径列表。
     *
     * @param directoryPath 指定的目录路径
     * @return 包含所有.class文件路径的列表。如果目录不存在或不是目录，则返回空列表。
     */
    public static List<String> getClassFilePaths(String directoryPath) {
        List<String> classFilePaths = new ArrayList<>();
        File directory = new File(directoryPath);

        // 检查目录是否存在
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Directory does not exist or is not a directory: " + directoryPath);
            return classFilePaths;
        }

        // 遍历目录下的所有文件和子目录
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 如果是子目录，则递归调用该方法
                    classFilePaths.addAll(getClassFilePaths(file.getAbsolutePath()));
                } else if (file.getName().endsWith(".class")) {
                    // 如果是.class文件，则将其路径添加到列表中
                    classFilePaths.add(file.getAbsolutePath());
                }
            }
        }
        return classFilePaths;
    }

    /**
     * 解压指定路径下的jar文件中的字节码文件。对的每个jar文件，解压其内部的字节码文件到指定目录。
     *
     * @param jarPath 要处理的jar文件路径。
     * @throws IOException 如果在读取或解压文件时发生IO异常。
     */
    public static void unzipByteCode(String jarPath) throws IOException {
//        packageName = "com/ruoyi";
        log.info("开始获取jar: {} 信息......", jarPath);
        List<String> targetJarList = listJarFiles(jarPath);
        if (targetJarList != null) {
            for (String entry : targetJarList) {
                String dir = entry.replace("BOOT_INF\\lib", "bytecode").replace(".jar", "");
                unzip(entry, dir);
            }
        }
        log.info("jar信息获取完毕~");
    }

    public static void unzip(String jarPath, String dest) throws IOException {
        File jar = new File(jarPath);
        if (!jar.exists()) {
            throw new NullPointerException("Jar file \"" + jarPath + "\" doesnt exist.");
        }

        try (
                FileInputStream fis = new FileInputStream(jarPath);
                BufferedInputStream bis = new BufferedInputStream(fis);
                JarInputStream jis = new JarInputStream(bis);
        ){
            Path destPath = Files.createDirectories(Paths.get(dest));
            byte[] bytes = new byte[1024];

            while (true) {
                ZipEntry entry = jis.getNextJarEntry();
                if (entry == null) {
                    break;
                }

                File file = new File(destPath.toFile(), entry.getName());

                if (entry.isDirectory()) {
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                } else {
                    File parentDir = new File(file.getParent());
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();
                    }

                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    int len = jis.read(bytes, 0, bytes.length);
                    while (len != -1) {
                        bos.write(bytes, 0, len);
                        len = jis.read(bytes, 0, bytes.length);
                    }

                    bos.flush();
                    bos.close();
                    fos.close();
                }
                jis.closeEntry();
            }

            // 解压Manifest文件
            Manifest manifest = jis.getManifest();
            if (manifest != null) {
                File manifestFile = new File(destPath.toFile(), JarFile.MANIFEST_NAME);
                if (!manifestFile.getParentFile().exists()) {
                    manifestFile.getParentFile().mkdirs();
                }
                BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(manifestFile.toPath()));
                manifest.write(out);
                out.flush();
                out.close();
            }
        }
    }



    /**
     * 列出指定目录下所有的.jar文件（包括子目录中的.jar文件）。
     *
     * @param directoryPath 指定的目录路径
     * @return 包含所有.jar文件路径的列表。如果目录不存在或不是目录，则返回null。
     */
    public static List<String> listJarFiles(String directoryPath) {
        File directory = new File(directoryPath);
        List<String> jarFiles = new ArrayList<>();
        // 检查目录是否存在
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Directory does not exist or is not a directory: " + directoryPath);
            return null;
        }
        // 获取目录下的所有文件
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                // 如果是.jar文件，则输出文件路径
                if (file.isFile() && file.getName().endsWith(".jar")) {
//                System.out.println(file.getAbsolutePath());
                    jarFiles.add(file.getAbsolutePath());
                }

                // 如果是子目录，则递归调用该方法
                if (file.isDirectory()) {
                    listJarFiles(file.getAbsolutePath());
                }
            }
        }
        return jarFiles;
    }

    /**
     * 获取指定目录下所有子目录中.class文件的路径列表。
     *
     * @param directoryPath 指定的目录路径
     * @return 键是子目录的绝对路径，值是该子目录下所有.class文件的路径列表（路径被转换为相对路径且使用斜杠分隔）
     */
    public static Map<String, List<String>> getNestedClassFilePaths(String directoryPath) {
        Map<String, List<String>> nestedClassFilePaths = new HashMap<>();
        File directory = new File(directoryPath);

        // 检查目录是否存在
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Directory does not exist or is not a directory: " + directoryPath);
            return nestedClassFilePaths;
        }

        // 获取目录下的所有子目录
        File[] subDirectories = directory.listFiles(File::isDirectory);
        if (subDirectories != null) {
            for (File subDirectory : subDirectories) {
                // 递归获取子目录下的所有.class文件路径列表
                List<String> classFilePaths = getClassFilePaths(subDirectory.getAbsolutePath());
                classFilePaths = classFilePaths.stream().map(path ->
                        path.replace(subDirectory.getPath() + "\\", "")
                                .replace("\\", "/")).collect(Collectors.toList());
                nestedClassFilePaths.put(subDirectory.getAbsolutePath(), classFilePaths);
            }
        }

        return nestedClassFilePaths;
    }


    public static void moveFile(String sourceFilePath, String destinationDirectoryPath) throws IOException {
        File sourceFile = new File(sourceFilePath);
        File destinationDirectory = new File(destinationDirectoryPath);

        // 检查源文件是否存在
        if (!sourceFile.exists()) {
            throw new FileNotFoundException("Source file not found: " + sourceFilePath);
        }

        // 检查目标目录是否存在，如果不存在，则创建它
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdirs();
        }

        // 构造目标文件路径
        String destinationFilePath = destinationDirectoryPath + File.separator + sourceFile.getName();

        // 移动文件
        if (!sourceFile.renameTo(new File(destinationFilePath))) {
            throw new IOException("Failed to move file: " + sourceFilePath);
        }
    }

    /**
     * 复制文件到指定目录。
     *
     * @param sourceFilePath       源文件路径
     * @param targetDirectoryPath 目标目录路径
     * @throws IOException 如果复制文件时发生IO
     */
    public static void copyFile(String sourceFilePath, String targetDirectoryPath) throws IOException {
        File sourceFile = new File(sourceFilePath);
        File destinationDirectory = new File(targetDirectoryPath);

        // 检查源文件是否存在
        if (!sourceFile.exists()) {
            throw new IOException("Source file not found: " + sourceFilePath);
        }
        // 检查目标目录是否存在，如果不存在，则创建它
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdirs();
        }
        String destinationFilePath = targetDirectoryPath + File.separator + sourceFile.getName();
        Path sourcePath = sourceFile.toPath();
        Path destinationPath = new File(destinationFilePath).toPath();
        Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("File copied successfully: {} -> {}", sourceFilePath,destinationFilePath);
    }


    /**
     * 遍历指定目录下的所有文件和子目录，查找并收集所有以".txt"结尾的文件路径。
     * 如果设置了isFilter为true，则对找到的文本文件进一步通过filterFile函数进行筛选。
     *
     * @param directory 指定的目录，将从此目录开始遍历查找文本文件。
     * @param filePaths 用于收集找到的文本文件的绝对路径的列表。
     */
    private static void listTxtFiles(File directory, List<String> filePaths) {
        // 检查目录是否存在
        if (directory.exists() && directory.isDirectory()) {
            // 获取目录中的所有文件和子目录
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        // 如果是子目录，递归调用遍历方法
                        listTxtFiles(file, filePaths);
                    } else if (file.isFile() && file.getName().endsWith(".txt")) {
                        if (IS_NEED_FILTER) {
                            if (filterFile(file)) {
                                filePaths.add(file.getAbsolutePath());
                            }
                        } else {
                            filePaths.add(file.getAbsolutePath());
                        }
                    }
                }
            }
        } else {
            log.info("Invalid directory path: {}", directory.getPath());
        }
    }

    public static void scan_filter_file(String path) {
//        String path = JACG_RESULT_PATH + "20231215-110117.445";
        List<String> filePathsList = scanJacgResultFiles(path);
        int count = 0;
        for (String file : filePathsList) {
            count += 1;
            log.info(file);
        }
        log.info("扫描文件数量：{}", count);
    }

    private static boolean filterFile(File file) {
        String fileName = file.getName();
        List<String> excludePatterns = new ArrayList<>();//(".*\\.bak", "temp.*"); // 替换为实际的排除文件名的正则表达式
        excludePatterns.add(".md");
        excludePatterns.add("init)");
        excludePatterns.add("lambda$");
        // 检查是否符合排除条件
        for (String pattern : excludePatterns) {
            if (fileName.contains(pattern)) {
                return false; // 文件名匹配排除条件，不包含
            }
        }
        return true; // 不匹配排除条件，包含
    }
}

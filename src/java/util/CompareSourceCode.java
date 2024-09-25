package util;


import clean.RuleSelector;
import clean.model.Change;
import gitop.ast.AnnotationVisitor;
import gitop.ast.MethodBlockVisitor;
import gitop.utils.AccurateUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import static gitop.utils.AccurateUtils.*;
import static util.Util.parseCodeLines;


/**
 * CompareMethodSet 反编译源码对比
 *
 * @author Z.X
 */
@Slf4j
public class CompareSourceCode {
    public static Map<String, String> fileCodeMap1;
    public static Map<String, String> fileCodeMap2;

    public static String currentGroupId;

    /**
     * 变更类文件：[行号：代码行内容]
     */
    public static Map<String, Map<Integer,String>> diffMap = new TreeMap<>();

    public static Change change;

    public static  Set<String> cmpTransformLinesBelongMethod(String classFile1,String classFile2) throws NoSuchAlgorithmException {
        if (classFile1 != null ) {
            File file1 = new File(classFile1);
            if (!(file1.isFile() && file1.getName().endsWith(".java"))){
                return new HashSet<>();
            }
        }
        if (classFile2 != null) {
            File file2 = new File(classFile2);
            if (!(file2.isFile() && file2.getName().endsWith(".java"))){
                return new HashSet<>();
            }
        }
        String content1 = readFile(classFile1);
        String content2 = readFile(classFile2);
        if(compareContentsHash(content1, content2)){
            return new HashSet<>();
        }
//        List<String> set1 = parseCodeLines(content1);
//        List<String> set2 = parseCodeLines(content2);
//        List<String> diffLine = new ArrayList<>(set2);
//        // diff出新版本变更的行
//        diffLine.removeAll(set1);
//        Map<Integer,String> diffLinesMap = new TreeMap<>();
//        // 列表索引+1就是行号顺序
//        diffLine.forEach(elem -> diffLinesMap.put(set2.indexOf(elem)+1, elem));
//        if(!diffLinesMap.isEmpty()){
//            diffMap.put(classFile2,diffLinesMap);
//        }
        //  if(compareLinesCount(fileCodeMap1.get(path1), fileCodeMap2.get(path2)))  break;
        Set<String> changeMethodSet = new HashSet<>();
        // 对比出源码差异方法
        boolean isChange = cmpMethodBlockContents(content1, content2, changeMethodSet);
        System.out.println(changeMethodSet);
        return changeMethodSet;
    }

    public static boolean cmpMethodBlockContents(String content1, String content2,
                                                 Set<String> changeMethods) throws NoSuchAlgorithmException {
        MethodBlockVisitor methodBlockVisitor = new MethodBlockVisitor();
        // 获取类文件下所有方法块及范围
        Map<String, List<Integer>> methodRangeMap1 = methodBlockVisitor.showClassMethod(content1);
        Map<String, List<Integer>> methodRangeMap2 = methodBlockVisitor.showClassMethod(content2);
        Set<String> methodSet1 = methodRangeMap1.keySet();
        Set<String> methodSet2 = methodRangeMap2.keySet();
        Set<String> interSections = new HashSet<>(methodSet1);
        interSections.retainAll(methodSet2); // 两个版本类文件所声明方法的交集 还应去掉上一步对比方法调用池时，分析过的重复方法
//        Set<String> changeMethods = new HashSet<>();
        for (String method : interSections) { // 交集中的方法对比
            List<Integer> methodRange1 = methodRangeMap1.get(method);
            List<Integer> methodRange2 = methodRangeMap2.get(method);
            StringBuilder slice1 = new StringBuilder();
            String[] segment1 = content1.split("\n");
            StringBuilder slice2 = new StringBuilder();
            String[] segment2 = content2.split("\n");
            // slice1、slice2为Method Block内容
            for (int i = methodRange1.get(0) - 1; i <= methodRange1.get(1) - 1; i++) { // -1是因为行号从0开始，而输入的行号从1开始
                slice1.append(segment1[i]).append("\n");
            }
            for (int j = methodRange2.get(0) - 1; j <= methodRange2.get(1) - 1; j++) {
                slice2.append(segment2[j]).append("\n");
            }

            // 判断方法块内容是否相同
            if (!compareContentsHash(slice1.toString(), slice2.toString())) {
                changeMethods.add(method);
            }
//            // 判断方法的注解是否相同
//            else if (!compareAnnotation(method, content1, content2)) {
//                changeMethods.add(method);
//            }
        }
        //有变化 ,需要把这些方法加入变更方法节点集changeMethods
        return changeMethods.isEmpty();
    }

    /**
     * 比较 两个jar文件反编译后的 源码差异，并将变更的方法名集合添加到changeSet中
     *
     * @param jarTarget1  第一个jar源文件的路径
     * @param jarTarget2  第二个jar源文件的路径
     * @return changeSet 用于存储代码变更方法名的集合
     */
    public static Set<String> compareDecompileCode(String jarTarget1,String jarTarget2) throws NoSuchAlgorithmException {
        // 设计：两个版本jar放在tmp目录的old和new目录下，然后通过反编译对比
        Set<String> changeSet = new HashSet<>();
        List<String> filePaths1 = new ArrayList<>();
        List<String> filePaths2 = new ArrayList<>();
        if (jarTarget1 != null) {
            File file1 = new File(jarTarget1);
            scanJavaFiles(file1, filePaths1);
        }
        if (jarTarget2 != null) {
            File file2 = new File(jarTarget2);
            scanJavaFiles(file2, filePaths2);
        }
        // key为文件路径// value为读取文件内容的方法返回值
        fileCodeMap1 = filePaths1.stream().collect(Collectors.toMap(path -> path, AccurateUtils::readFile));
        fileCodeMap2 = filePaths2.stream().collect(Collectors.toMap(path -> path, AccurateUtils::readFile));

        // 对比
        //String basePath1 = new File(jarTarget1).getAbsolutePath().replace("\\", "/").replace("//", "/");
        //String basePath2 = new File(jarTarget2).getAbsolutePath().replace("\\", "/").replace("//", "/");
        for (String path1 : filePaths1) {
            // 只需比较groupId前缀（或者说源工程目录的源码）的字节码，不需要比较反编译的org/springbootframework/springboot下的源码
            if(!path1.contains(currentGroupId.replace(".","/"))){
                continue;
            }
            for (String path2 : filePaths2) {
                if(!path2.contains(currentGroupId.replace(".","/"))){
                    continue;
                }
                String content1 = fileCodeMap1.get(path1);
                String content2 = fileCodeMap2.get(path2);
                // 检测类中的重载方法
//                AccurateUtils.detectOverloadMethodInfo2File(path2);
                // 从源码文件中提取package前缀后的包名
                String className01 = extractPackage(content1) + "." + path1.substring(path1.lastIndexOf("/")+1).replace(".java", "");
                String className02 = extractPackage(content2) + "." + path2.substring(path2.lastIndexOf("/")+1).replace(".java", "");
                // 找相同全限定名的类文件进行对比
                if (className01.equals(className02)) {
                    // 输入比较映射文件<文件路径,文件内容>  -->  输出映射文件<项目相对路径src/java/...|重构类型RENAME_XXX,相关行号集合>
                    // 将识别结果的行号在对比阶段前，筛出来；或者筛出重构相关的函数，在对比的时候，就忽略对比；
                    // 如果以函数块为单位，难点在于：不能保证函数块内的行为是纯重命名重构
                    // 但refactoringMiner有一个特点，ta会把重命名重构所在的函数的行号范围全部返回
                    // Threat: 如果Literal或者打印中包含\n就可能会冲突不准确 List.of(content1.split("\n"));
                    List<String> set1 = parseCodeLines(content1);
                    List<String> set2 = parseCodeLines(content2);
                    List<String> diffLine = new ArrayList<>(set2);
                    // diff出新版本变更的行
                    diffLine.removeAll(set1);
                    Map<Integer,String> diffLinesMap = new TreeMap<>();
                    // 列表索引+1就是行号顺序
                    diffLine.forEach(elem -> diffLinesMap.put(set2.indexOf(elem)+1, elem));
                    if(!diffLinesMap.isEmpty()){
                        diffMap.put(path2,diffLinesMap);
                    }

                    //  if(compareLinesCount(fileCodeMap1.get(path1), fileCodeMap2.get(path2)))  break;
                    if (compareContentsHash(content1, content2)) {// 比较源码内容hash值
                        break;
                    }
                    String className = className01;
//                    if (className.startsWith("/")){
//                        className = className.substring(1);
//                    }
//                    if (className.toLowerCase().endsWith(".java")){
//                        className = className.substring(0, className.length() - 5);
//                    }
//                    className = className.replace("/", ".");
                    Set<String> changeMethodSet = new HashSet<>();
                    // 对比出源码差异方法
                    boolean isChange = compareMethodContents(content1, content2, changeMethodSet);
                    //  修正changeMethodSet方法名称为全限定类名形式changeSet
                    normalizeFqMethodName(changeMethodSet, className, content1, content2, changeSet);
                    // 比较内容（方法为单位）
                    if (isChange) {
                        break;
                    }
                }
            }
        }
        // 记录当前变更中涉及噪声关联的行
        // TODO： 如果是重命名方法，还需要关联修改被重命名的行变更（如果存在）；变更字段也可能
        //  所以去噪是循循渐进的，对应重命名方法后，
        //  同时修改的调用了重命名方法的行也可以【进一步】做处理和识别(
        //      如重命名方法的调用处修改（内部递归）、内部添加打印、log语句且不包含其他方法调用（或排除库函数以外的调用）)，从而不去回归）
        change = RuleSelector.processRefactorRenameTypeInSrcCmp(jarTarget1, jarTarget2, diffMap);
        change.setCmpId(jarTarget1 +"_"+ jarTarget2);
        // 确认为纯噪声的噪声变更方法集
        Set<String> pureNoiseMethodList = RuleSelector.detectNoiseChangeMethod(change,jarTarget1, jarTarget2,fileCodeMap1,fileCodeMap2,diffMap);

        return changeSet;
    }

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

    static class SS{

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

    /**
     * compareMethodContents: 对比文件内容Hash, 判断是否变更
     *
     * @param content1: 源码内容1
     * @param content2: 源码内容2
     */
    public static boolean compareContentsHash(String content1, String content2) throws NoSuchAlgorithmException {
        return calculateFileHash(content1).equals(calculateFileHash(content2));
    }

    /**
     * 对比方法块内容，判断是否变更
     *
     * @param content1:      源码内容1
     * @param content2:      源码内容2
     * @param changeMethods: 对比出变更的方法
     */
    public static boolean compareMethodContents(String content1, String content2,
                                                Set<String> changeMethods) throws NoSuchAlgorithmException {
        MethodBlockVisitor methodBlockVisitor = new MethodBlockVisitor();
        // 获取类文件下所有方法块及范围
        Map<String, List<Integer>> methodRangeMap1 = methodBlockVisitor.showClassMethod(content1);
        Map<String, List<Integer>> methodRangeMap2 = methodBlockVisitor.showClassMethod(content2);
        Set<String> methodSet1 = methodRangeMap1.keySet();
        Set<String> methodSet2 = methodRangeMap2.keySet();
        Set<String> interSections = new HashSet<>(methodSet1);
        interSections.retainAll(methodSet2); // 两个版本类文件所声明方法的交集 还应去掉上一步对比方法调用池时，分析过的重复方法
//        Set<String> changeMethods = new HashSet<>();
        for (String method : interSections) { // 交集中的方法对比
            if(method.contains("template")){
                System.out.println(method);
            }
            List<Integer> methodRange1 = methodRangeMap1.get(method);
            List<Integer> methodRange2 = methodRangeMap2.get(method);
            StringBuilder slice1 = new StringBuilder();
            String[] segment1 = content1.split("\n");
            StringBuilder slice2 = new StringBuilder();
            String[] segment2 = content2.split("\n");
            // slice1、slice2为Method Block内容
            for (int i = methodRange1.get(0) - 1; i <= methodRange1.get(1) - 1; i++) { // -1是因为行号从0开始，而输入的行号从1开始
                slice1.append(segment1[i]).append("\n");
            }
            for (int j = methodRange2.get(0) - 1; j <= methodRange2.get(1) - 1; j++) {
                slice2.append(segment2[j]).append("\n");
            }

            // 判断方法块内容是否相同
            if (!compareContentsHash(slice1.toString(), slice2.toString())) {
                changeMethods.add(method);
            }
            // 判断方法的注解是否相同
            else if (!compareAnnotation(method, content1, content2)) {
                changeMethods.add(method);
            }
        }
        //有变化 ,需要把这些方法加入变更方法节点集changeMethods
        return changeMethods.isEmpty();
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

    /**
     * 对比方法注解，判断是否变更（从content中解析出注解列表，然后对比列表的数量、值isSame）
     *
     * @param methodName: 方法名
     * @param code1:  源码内容1
     * @param code2:  源码内容2
     */
    public static boolean compareAnnotation(String methodName, String code1, String code2) {
        AnnotationVisitor annotationVisitor = new AnnotationVisitor();
        List<String> annotationList1 = annotationVisitor.showMethodAnnotation(methodName, code1);
        List<String> annotationList2 = annotationVisitor.showMethodAnnotation(methodName, code2);
        boolean isSame = annotationList1.equals(annotationList2);
        if (!isSame) {
            log.info("注解存在变化：");
            log.info(annotationList1.toString());
            log.info(annotationList2.toString());
        }
        return isSame;
    }

    public void cleanNoise(){
        // 噪声去除·方式：识别出重命名行为，如果行为行属于源码对比的变化行，则认为该行为是噪声，若仍有剩余行，则把其加入到changeMethods中
                                                                            // 若没有，则不纳入变化节点
    }

    /**
     * 对比文件行数, 判断是否变更（有局限）
     *
     * @param content1: 源码内容1
     * @param content2: 源码内容2
     */
    public static boolean compareLinesCount(String content1, String content2) { // 仅判断行数是否相同，则捕捉不到changed的差异
        int len1 = content1.split("\n").length; // 并且如果Literal中包含换行符，则行数会增加误报
        int len2 = content2.split("\n").length;
        return len2 == len1;
    }

    private static void removeCheckedMethod(Set<String> changeMethods, Set<String> interSections) {
        for (String method : interSections) { // 交集中的方法对比
            String methodName = method.split(" ")[1];
            String param = method.substring(method.indexOf("(") + 1, method.indexOf(")"));
            boolean matchName = false, matchParam = false;
            for (String nodeName : changeMethods) {
                if (nodeName.substring(nodeName.indexOf(":") + 1, nodeName.indexOf("(")).equals(methodName)) { // 方法名相同
                    matchName = true;
                    if (param.split(", ").length == 1) { // 参数类型相同
                        matchParam = nodeName.contains(param); // threat: 如果参数类名相同但所属包不同也可能误匹配
                    } else {
                        boolean allParamMatch = true;
                        for (String p : param.split(", ")) {
                            allParamMatch = nodeName.contains(p) && allParamMatch; //所有参数项均匹配（TODO:似乎可以作为噪声参数调换的依据）
                        }
                        matchParam = allParamMatch;
                    }
                }
                if (matchName && matchParam) {
//                    log.info(methodName+"("+param+")"+ " ---> "  +nodeName);
                    interSections.remove(method);
                }
            }
        }
    }
}

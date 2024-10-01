package clean;


import clean.model.Change;
import clean.model.ChangeNoise;
import gitop.ast.MethodBlockVisitor;
import gitop.utils.AccurateUtils;
import gitop.utils.GitDiffUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static clean.CleanBlank.*;
import static clean.detectRule.compareStringLists;
import static gitop.utils.AccurateUtils.normalizeFqMethodName;
import static gitop.utils.AccurateUtils.scanJavaFiles;


public class RuleSelector {


    public static final String BLANK_SPACE = "rule1";
    public static final String BLANK_LINE = "BLANK_LINE";

    public static final String LINE_COMMENT = "LINE_COMMENT";
    public static final String BLOCK_COMMENT = "BLOCK_COMMENT";

    public static final String RENAME_CLASS = "RENAME_CLASS";

    public static final String RENAME_METHOD = "RENAME_METHOD";

    public static final String RENAME_VARIABLE = "RENAME_VARIABLE";

    public static final String RENAME_PARAMETER = "RENAME_PARAMETER";

    public static final String RENAME_FIELD = "RENAME_FIELD";


    public static void main(String[] args) {

    }


    /**
     * 输入：一次变更（两个不同版本的CommitID或工程目录）----> 对应0-n种（噪声---> 噪声相关（行-->内容）∈ 方法 ∈ 类文件）
     * 输出：本次变更（Change） commitId(源码本地根目录 + diff可获取具体变更块所在的文件位置)
     * /新旧工程目录（粗粒度，需要获取噪声具体触发的文件，但是重构识别结果会提供存储库根目录，只需与项目目录位置拼接）
     * thus，输入均需要3个，新旧Commit，存储库；新旧项目目录，被对比的类文件（除非文件被移动或重命名，否则被对比的项目文件根路径一致）
     * 检测到的噪声有哪些?  Change.changeNoiseList[ChangeNoise...]
     * 这些噪声对应的代码行和内容在哪？ ChangeNoise[newLines、newLineToContent]
     * 这些内容和行属于哪个方法下？ ChangeNoise[methodInfo]
     * 这个方法归属于哪个类文件（或者说找寻其全限定名） 该属性应该归属于ChangeNoise
     *
     * @param oldId
     * @param newId
     * @param localUrl
     */
    public static void entry(String oldId, String newId, String localUrl,Set<String> resultNoiseList) {
        localUrl = localUrl.replace("\\", "/");
        Change change = new Change();
        // 先获取两个版本的diff，拆分变更的位置（即类文件目录）、再与源工程存储库路径拼接
        // 或输入中提供两个新旧工程的路径、以及被对比的类文件目录 拼接
        // 后续如果需要获取新旧两个版本的依据，只需要 split(" --> ");
        // 切换到新版本
        change.setCmpId(oldId + " --> " + newId);
        GitDiffUtils util = new GitDiffUtils();
        GitDiffUtils.checkout2CommitId(localUrl, newId);
        List<Map<String, Map<Integer, String>>> diffMapList = detectRule.getDiffByJGit(localUrl, oldId, newId, "sss");
        // diff 的 文件路径 url + diffMap.keySet()
        Map<Integer, String> lineContentMaps = new TreeMap<>();
        if (diffMapList.get(0).isEmpty()) {
            return;
        }
        for (String key : diffMapList.get(0).keySet()) {
//            Map<Integer,String> lineContentMap = diffMap.get(key)
//                    .entrySet()
//                    .stream()
//                    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
            lineContentMaps.putAll(diffMapList.get(0).get(key));
            List<ChangeNoise> changeNoiseList = process4BlankAndComment(diffMapList.get(0).get(key));

            changeNoiseList = changeNoiseList.stream()
                    .filter(changeNoise -> !((changeNoise.getTypeId() == null) || changeNoise.getTypeId().isEmpty()))
                    .collect(Collectors.toList());
            // 噪声来自于哪个方法单位
            String finalLocalUrl = localUrl;
            changeNoiseList.forEach(changeNoise -> matchChangeFromMethod(key, changeNoise));
            change.getChangeNoiseList().addAll(changeNoiseList);
            // TODO: 整合判断噪声行是否是该类文件（或方法）的所有行，如果是，则该文件下的变更是无效的

        }
        change.getChangeNoiseList().addAll(processRefactorRenameType(oldId, newId, localUrl, lineContentMaps));
        List<String> filePaths2 = new ArrayList<>();
        if (localUrl != null) {
            File file2 = new File(localUrl);
            scanJavaFiles(file2, filePaths2);
        }
        Map<String, String> fileCodeMap2 = filePaths2.stream().collect(Collectors.toMap(path -> path, AccurateUtils::readFile));
        Set<String> pureNoiseMethodList = detectNoiseChangeMethod(change, localUrl, localUrl, fileCodeMap2, fileCodeMap2, diffMapList.get(0));
        Set<String> result = new HashSet<>();
        for (String key : diffMapList.get(0).keySet()){
            List<String> a = new ArrayList<>(diffMapList.get(0).get(key).values());
            if (diffMapList.get(1).containsKey(key)){
                List<String> b = new ArrayList<>(diffMapList.get(1).get(key).values());
                List<String> intersect =  new ArrayList<>(a);
                List<String> intersect1 =  new ArrayList<>(b);
//                        intersect.removeAll(b);
//                        intersect1.removeAll(a);
                result.addAll(compareStringLists(intersect, intersect1,0.50));
//                        System.out.println(intersect);
//                        System.out.println(intersect1);

            }
        }
        resultNoiseList.addAll(pureNoiseMethodList);
        resultNoiseList.addAll(result);
        // 切换到原始版本
        GitDiffUtils.checkout2CommitId(localUrl, oldId);
    }

    /**
     * 用于反编译源码对比后，识别出噪声
     */
    public static Change processRefactorRenameTypeInSrcCmp(String dir1, String dir2,
//                                                               String url,
                                                           Map<String, Map<Integer, String>> diffMap) {
        Change change = new Change();
        List<ChangeNoise> changeNoiseList = new ArrayList<>();
        // diff 的 文件路径 url + diffMap.keySet()
        Map<Integer, String> lineContentMaps = new TreeMap<>();
//        if(diffMap.isEmpty()){
//            return;
//        }
        for (String key : diffMap.keySet()) {
            lineContentMaps.putAll(diffMap.get(key));
        }
        // 执行源工程目录重命名噪声检测
        CleanRefactor cleanRefactor = new CleanRefactor();
        Map<String, List<Integer>> checkResultMap = cleanRefactor.checkRefactoringFromDirectories(dir1, dir2);
        if (checkResultMap.isEmpty()) {
            return change;
        }
        for (String key : checkResultMap.keySet()) {
            ChangeNoise changeNoise = new ChangeNoise();
            changeNoise.setTypeId(key.split("\\|")[1]);
            // 不应该存返回的重构行，因为refactoringMiner返回的是涉及重构的函数块范围行集，并不完全是变更行;
            Set<Integer> interSet = new HashSet<>(); // new TreeSet<>(checkResultMap.get(key));
            // 实际涉及重命名的行（如方法重命名），应该就是块范围行集的【第1行】(因为反编译不会包含注释，如果是原工程代码的话，块范围会包括注释行)；
            interSet.add(checkResultMap.get(key).get(0));
            interSet.retainAll(lineContentMaps.keySet());
            changeNoise.setNewRelatedLines(new ArrayList<>(interSet));
            List<String> refactorChangeContent = new ArrayList<>();
            // 找出重构行范围中，是变更的行
            for (Integer line : interSet) {
                refactorChangeContent.add(lineContentMaps.get(line));
            }
            changeNoise.setModifiedContent(refactorChangeContent);
            // 匹配变更行属于哪个函数
            matchChangeFromMethod(dir2 + "/" + key.split("\\|")[0], changeNoise);
            changeNoiseList.add(changeNoise);
        }
        change.setChangeNoiseList(changeNoiseList);
        return change;
    }

    public static void matchChangeFromMethod(String filePath, ChangeNoise changeNoise) {  // url +"\\"+ key
        MethodBlockVisitor visitor = new MethodBlockVisitor();
        Map<String, List<Integer>> fileList = visitor.showClassMethod(AccurateUtils.readFile(filePath));
        List<String> bb = new ArrayList<>(fileList.keySet());
        List<List<Integer>> aa = new ArrayList<>(fileList.values());

        for (int lineId : changeNoise.getNewRelatedLines()) {
            int i = 0;
            for (List<Integer> a : aa) {
                if (AccurateUtils.isChangeInMethodRange(lineId, lineId, a.get(0), a.get(1))) {
                    Pattern pattern = Pattern.compile("(\\w+)\\s+(\\w+)\\s+\\(([^)]+)\\)");
                    Matcher matcher = pattern.matcher(bb.get(i));
                    String params = "";
                    if (matcher.find()) {
                        params = "(" + matcher.group(3) + ")";
                    }
                    String methodInfo = filePath+"|"+bb.get(i).split(" ")[0]+":"
                            + bb.get(i).split(" ")[1] + params;
                    changeNoise.setMethodLocation(methodInfo.replace("\\", "/"));
                }
                i++;
            }
        }
    }

    /**
     * 获取当前变更(change)中涉及噪声关联的行
     *
     * @param change       该次变更
     * @param jarTarget1   源码根目录1
     * @param jarTarget2   源码根目录2
     * @param fileCodeMap1 java文件路径1：java文件内容1
     * @param fileCodeMap2 java文件路径2：java文件内容2
     * @param diffMap      对比差异行：内容 映射
     * @return
     */
    public static Set<String> detectNoiseChangeMethod(Change change,
                                                      String jarTarget1, String jarTarget2,
                                                      Map<String, String> fileCodeMap1, Map<String, String> fileCodeMap2,
                                                      Map<String, Map<Integer, String>> diffMap) {
        Set<String> pureNoiseMethodList = new HashSet<>();
        for (ChangeNoise changeNoise : change.getChangeNoiseList()) {
            String methodInfo = changeNoise.getMethodLocation();
            if(methodInfo == null &&
                    (changeNoise.getTypeId().equals(BLANK_LINE)||changeNoise.getTypeId().equals(LINE_COMMENT) || changeNoise.getTypeId().equals(BLOCK_COMMENT)))
            {
                System.out.println("非方法块内的格式噪声");
                continue;
            }else if(methodInfo == null || !methodInfo.endsWith(")")){
                System.out.println("字段、枚举等非方法属性的格式噪声");
                continue;
            }
            String[] tmpArray = methodInfo.split("\\|")[1].split(":");
            MethodBlockVisitor visitor = new MethodBlockVisitor();
            // 找出噪声相关的函数行集
            String key1 = methodInfo.split("\\|")[0];
            Map<String, List<Integer>> fileList = visitor.showClassMethod(AccurateUtils.readFile(key1));
            Set<Integer> set = new TreeSet<>(diffMap.get(key1).keySet());

            String key2 = tmpArray[0] + " " + tmpArray[1].replace("(", " (");
            List<Integer> currentMethodLineRange = fileList.get(key2);
            set = set.stream()
                    .filter(lineId -> AccurateUtils.isChangeInMethodRange(lineId, lineId, currentMethodLineRange.get(0), currentMethodLineRange.get(1)))
                    .collect(Collectors.toSet());
            // 去除属于该方法块的差异行
            changeNoise.getNewRelatedLines().forEach(set::remove);
            // 如果在被去除了噪声的方法中不包含剩余差异行
            if (set.isEmpty()) {
                // 说明关于该方法的变更差异行都是噪声差异变更
                // 匹配成changeSet中的方法名格式
                String removeStr = methodInfo.substring(methodInfo.indexOf(".java|"),methodInfo.lastIndexOf(":"));
                String tmp = methodInfo.replace(removeStr,"")
                        .replace(jarTarget2.replace("\\", "/") + "/", "")
                        .replace("/", ".");
                Set<String> before = new HashSet<>();
                before.add(tmp.substring(tmp.lastIndexOf(".") + 1, tmp.lastIndexOf(":") + 1)
                        .replace(":", " ") + tmp.substring(tmp.lastIndexOf(":") + 1).replace("(", " ("));
                Set<String> after = new HashSet<>();
                String className1 = tmp.substring(0, tmp.indexOf(":"));
                String fileName1 = jarTarget1 + "/" + className1.replace(".", "/") + ".java";
                String fileName2 = jarTarget2 + "/" + className1.replace(".", "/") + ".java";
                normalizeFqMethodName(before, className1, fileCodeMap1.get(fileName1), fileCodeMap2.get(fileName2), after);
//                changeSet.remove(noiseMethod);
                pureNoiseMethodList.addAll(after);
                changeNoise.setNoiseMethod(true);
            }
        }
        return pureNoiseMethodList;
    }

    /**
     * 以一个方法块变更为输入
     */
    private static List<ChangeNoise> process4BlankAndComment(Map<Integer, String> lineContentMap, String... args) {
        List<ChangeNoise> changeNoiseList = new ArrayList<>();
        changeNoiseList.add(processSingleComment(lineContentMap));
        changeNoiseList.add(processBlankLine(lineContentMap));
        changeNoiseList.add(processBlockComment(lineContentMap));
        return changeNoiseList;
    }

    private static ChangeNoise processBlankLine(Map<Integer, String> lineContentMap) {
        List<Integer> noiseLines = new ArrayList<>();
        List<String> noiseContents = new ArrayList<>();

        List<String> contents = new ArrayList<>(lineContentMap.values());
//        changeNoise.setMethodInfo(method);
        ChangeNoise changeNoise = new ChangeNoise();
        Integer[] keyArray = lineContentMap.keySet().toArray(new Integer[contents.size()]);
        int i = 0;
        for (String codeLine : contents) {
            i++;
            if (check_blank_line(codeLine)) {
                int currentLine = keyArray[i - 1];
                changeNoise.setTypeId(BLANK_LINE);
                noiseLines.add(currentLine);
                noiseContents.add(codeLine);
                System.out.println(currentLine + " : " + codeLine);
            }
        }
        changeNoise.setNewRelatedLines(noiseLines);
        changeNoise.setModifiedContent(noiseContents);
        return changeNoise;
    }

    private static ChangeNoise processSingleComment(Map<Integer, String> lineContentMap) {
        List<Integer> noiseLines = new ArrayList<>();
        List<String> noiseContents = new ArrayList<>();
        List<String> contents = new ArrayList<>(lineContentMap.values());
//        changeNoise.setMethodInfo(method);
        ChangeNoise changeNoise = new ChangeNoise();
        Integer[] keyArray = lineContentMap.keySet().toArray(new Integer[contents.size()]);
        int i = 0;
        for (String codeLine : contents) {
            if (check_single_comment(codeLine)) {
                changeNoise.setTypeId(LINE_COMMENT);
                int currentLine = keyArray[i];
                noiseLines.add(currentLine);
                noiseContents.add(codeLine);
                System.out.println(codeLine);
            }
        }
        i++;
        changeNoise.setNewRelatedLines(noiseLines);
        changeNoise.setModifiedContent(noiseContents);
        return changeNoise;
    }

    private static ChangeNoise processBlockComment(Map<Integer, String> lineContentMap) {
        StringBuilder comment = new StringBuilder();
        List<String> commentList = new ArrayList<>();
        List<Integer> noiseLines = new ArrayList<>();
        List<String> noiseContents = new ArrayList<>();
        boolean flag = false;
        List<String> contents = new ArrayList<>(lineContentMap.values());
        Integer[] keyArray = lineContentMap.keySet().toArray(new Integer[contents.size()]);
//        changeNoise.setMethodInfo(method);
        int i = 0;
        ChangeNoise changeNoise = new ChangeNoise();
        for (String codeLine : contents) {
            i++;
            int currentLine = keyArray[i - 1];
            if (codeLine.trim().startsWith("/*") || codeLine.trim().startsWith("/**")) {
                // 块注释起始
                flag = true;
            }
            if (codeLine.trim().endsWith("*/")) {
                // 块注释结束
                flag = false;
                comment.append(codeLine);
                commentList.add(codeLine);
                noiseLines.add(currentLine);
                if (check_javadoc_comment(comment.toString())) {
                    changeNoise.setTypeId(BLOCK_COMMENT);
                    noiseContents.addAll(commentList);
                    System.out.println(comment);
                } else {
                    // 如果读完最后还是不符合，则清空
                    commentList.clear();
                }
            }
            if (flag) {
                // 没有遇到*/，继续向下读取
                comment.append(codeLine);
                commentList.add(codeLine);
                noiseLines.add(currentLine);
                if (i == contents.size()) {
                    // 如果读完最后还是不符合，则清空
                    commentList.clear();
                }
            }
        }
        changeNoise.setNewRelatedLines(noiseLines);
        changeNoise.setModifiedContent(noiseContents);
        return changeNoise;
    }

    public static List<String> result = new ArrayList<>();

    public static List<String> result1 = new ArrayList<>();

    public static List<ChangeNoise> processRefactorRenameType(String oldId, String newId,
                                                              String url, Map<Integer, String> lineContentMap) {
        List<ChangeNoise> changeNoiseList = new ArrayList<>();
//        changeNoise.setMethodInfo(method);
        // 执行项目目录对比
        CleanRefactor cleanRefactor = new CleanRefactor();
        Map<String, List<Integer>> checkResultMap = cleanRefactor.checkRefactoringFromCommits(oldId, newId, url, url);
        if (checkResultMap.isEmpty()) {
            result.add(oldId+" "+newId+"    :");
            result1.add("NO_NOISE");
            return changeNoiseList;
        }
        result.add(oldId+" "+newId+"    :"+checkResultMap.keySet().toString());
        result1.add(checkResultMap.keySet().toString());
        for (String key : checkResultMap.keySet()) {
            if(key.contains("RENAME_ATTRIBUTE")){
                continue;
            }
            ChangeNoise changeNoise = new ChangeNoise();
            changeNoise.setTypeId(key.split("\\|")[1]);
            // 不应该存返回的重构行，因为refactoringMiner返回的是涉及重构的块范围行集，并不完全是变更行
            // changeNoise.setNewRelatedLines(checkResultMap.get(key));
            Set<Integer> interSet = new HashSet<>(); // new TreeSet<>(checkResultMap.get(key));
            // 实际涉及重命名的行（如方法重命名），应该就是块范围行集的【第1行】(因为反编译不会包含注释，如果是原工程代码的话，块范围会包括注释行)；
            interSet.add(checkResultMap.get(key).get(0));
            interSet.retainAll(lineContentMap.keySet());
            changeNoise.setNewRelatedLines(new ArrayList<>(interSet));
            List<String> refactorChangeContent = new ArrayList<>();
            // 找出重构行范围中，是变更的行
            for (Integer line : interSet) {
                refactorChangeContent.add(lineContentMap.get(line));
            }
            changeNoise.setModifiedContent(refactorChangeContent);
            // 匹配变更行属于哪个函数
            matchChangeFromMethod(url + "/" + key.split("\\|")[0], changeNoise);
            changeNoiseList.add(changeNoise);
        }
        return changeNoiseList;
    }

    /**
     * 单文件重构检测----用于反编译源码对比去噪;
     * 输入文件路径
     */
    public static void process4RefactorInCmpSrc(String... args) {
        CleanRefactor cleanRefactor = new CleanRefactor();
        Map<String, List<Integer>> checkResultMap;
        ChangeNoise changeNoise = new ChangeNoise();
        checkResultMap = cleanRefactor.checkRefactoringFromFile(args[0], args[1]);
        changeNoise.setNewRelatedLines(checkResultMap.values().stream()
                .flatMap(List::stream).collect(Collectors.toList()));

    }
}

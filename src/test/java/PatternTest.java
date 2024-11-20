import clean.CleanRefactor;
import gitop.actionApp1;
import gitop.utils.GitDiffUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static gitop.utils.GitDiffUtils.*;

public class PatternTest {

    @Test
    public void test_PP2AddAndMMtoMinus() {
        actionApp1 action = new actionApp1();
        Set<String> resultNoiseList = new TreeSet<>();
        String time = getTime();
        String project_dir = "E:\\project-DataSets20240927\\spring-hateoas";
//                String file_dir = "E:\\project-DataSet1\\";
//                String project_name = "WebGoat";
//                String prefix_path = file_dir + project_name;
//        RuleSelector.entry("00d06a37293bba3c1d309f721cfbdda98a64a4a9","d0f10e068382cc2c1aee266d7bb40fd5275d80f2", "E:\\project-DataSets20240927\\spring-hateoas", resultNoiseList); // ("049001d1c0d5af5dd50676f7e2450375533d97fc","f34896a734e893759f1ca89930b0e3e30d75dca5",project_dir);
        Set<String> changeSet = new TreeSet<>();
        GitDiffUtils util = new GitDiffUtils();
        Map<String, Map<Integer, String>> diffMap = getDiffByJGit(
                project_dir,
                "cd2af5c5fa0325ae1c65b12b3a536543356a7301"
                ,"c2be6ab9aa2eddec945359f2272303678ef71c39"
                , time);
    }

    @Test
    public void test_SwitchEqualsSide() {
        actionApp1 action = new actionApp1();
        Set<String> resultNoiseList = new TreeSet<>();
        String time = getTime();
        String project_dir = "E:\\project-DataSets20240927\\spring-hateoas";
        Set<String> changeSet = new TreeSet<>();
        GitDiffUtils util = new GitDiffUtils();
        Map<String, Map<Integer, String>> diffMap = getDiffByJGit(
                project_dir,
                "e4f675041598d3bb73310a1a4abb8d77a36af007"//"cd2af5c5fa0325ae1c65b12b3a536543356a7301"
                ,"e0128b10c3e26d25d943fb7a49fe06110afb2b2d"//"c2be6ab9aa2eddec945359f2272303678ef71c39"
                , time);
    }

    @Test
    public void test_SwitchIfElse() {
        actionApp1 action = new actionApp1();
        Set<String> resultNoiseList = new TreeSet<>();
        String time = getTime();
        String project_dir = "E:\\project-DataSets20240927\\spring-hateoas";
        Set<String> changeSet = new TreeSet<>();
        GitDiffUtils util = new GitDiffUtils();
        Map<String, Map<Integer, String>> diffMap = getDiffByJGit(
                project_dir,
                "52c6881db4ff593764e4882956d894cfc7036f6a"//"cd2af5c5fa0325ae1c65b12b3a536543356a7301"
                ,"e365250d589fc818258525b5a81ca4bfe83c26a1"//"c2be6ab9aa2eddec945359f2272303678ef71c39"
                , time);
    }

    public Map<String, Map<Integer, String>> getDiffByJGit(String projectDir, String startCommit, String endCommit, String time) {
        Map<String, Map<Integer, String>> diffMap = new HashMap<>();
        Map<String, Map<Integer, String>> diffMap_del = new HashMap<>();
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
                    System.out.println("Folder " + path + " created successfully");
                } catch (IOException e) {
                    System.out.println("Failed to create folder: " + e.getMessage());
                }
                String diffPath = folderPath + "/diff-" + time + ".diff";//getTime()

                // 保存diff文件
                FileWriter fileWriter = new FileWriter(diffPath);
                fileWriter.write(outputStream.toString());
                fileWriter.flush();
                fileWriter.close();
//                log.info(outputStream.toString());
                // Convert the diff content to a map
                diffMap = mapDiff2ContentWithLines(projectDir, outputStream.toString());
                diffMap_del = mapDiff2ContentWithLines_del(projectDir, outputStream.toString());
                for (String key : diffMap.keySet()){
                    List<String> a = new ArrayList<>(diffMap.get(key).values());
                    if (diffMap_del.containsKey(key)){
                        List<String> b = new ArrayList<>(diffMap_del.get(key).values());
                        List<String> intersect =  new ArrayList<>(a);
                        List<String> intersect1 =  new ArrayList<>(b);
//                        intersect.removeAll(b);
//                        intersect1.removeAll(a);

                        compareStringLists(intersect, intersect1,0.50);
//                        System.out.println(intersect);
//                        System.out.println(intersect1);

                    }
                }
            }
            repository.close();
            System.out.println("get Diff Success");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error getting diff: " + e.getMessage());
        }
        return diffMap;
    }
    public static Map<String, Map<Integer, String>> mapDiff2ContentWithLines(String projectPath,String diffContent) {
        Map<String, Map<Integer, String>> fileMap = new HashMap<>();
        String currentFile = null;
        boolean isJava = false;
        List<Integer> lineId = new ArrayList<>();
        for (String line : diffContent.split("\n")) {
//                log.info(line);
            if (line.startsWith("+++ ")) {
                // 提取文件路径
                String filePath = line.substring(line.indexOf('/') + 1).trim();
                if (!filePath.isEmpty()) {
                    if (!filter_not_java(filePath)) {
                        isJava = true;
                        currentFile = projectPath + "/" + filePath;
                        Map<Integer, String> line4code = new TreeMap<>();
                        fileMap.put(currentFile, line4code);
//                        modifiedClass.add(currentFile);
                    } else {
                        isJava = false;
                    }
                }
                continue;
            }
            if (isJava) {
                int baseLine, delta;
                if (line.startsWith("@@")) {
                    // 提取新增代码行，格式为 “+66,5” or "-13 +13"
                    String change = line.split("\\+")[1].split(" ")[0];
                    // 消除“+”
                    change = change.substring(0, change.length());
                    // 获取变更行号范围
                    if (change.contains(",")) {
                        baseLine = Integer.parseInt(change.split(",")[0]);
                        delta = Integer.parseInt(change.split(",")[1]);
                        for (int i = 0; i < delta; i++) {
                            if (currentFile != null) {
                                lineId.add(baseLine + i);
                            }
                        }
                    } else {
                        if (currentFile != null) {
                            lineId.add(Integer.parseInt(change));
                        }
                    }
                } else if (line.startsWith("+") && !line.startsWith("+++")) { // 新增行号与新增内容对应
                    StringBuilder stringBuilder = new StringBuilder(line);
                    String s = stringBuilder.substring(1, line.length()).replace("\t", "");
                    fileMap.get(currentFile).put(lineId.get(0), s);
                    lineId.remove(0);
//                        log.info(fileMap.get(currentFile));
                }
            }
        }
        return fileMap;
    }

    public static Map<String, Map<Integer, String>> mapDiff2ContentWithLines_del(String projectPath,String diffContent) {
        Map<String, Map<Integer, String>> fileMap = new HashMap<>();
        String currentFile = null;
        boolean isJava = false;
        List<Integer> lineId = new ArrayList<>();
        for (String line : diffContent.split("\n")) {
//                log.info(line);
            if (line.startsWith("--- ")) {
                // 提取文件路径
                String filePath = line.substring(line.indexOf('/') + 1).trim();
                if (!filePath.isEmpty()) {
                    if (!filter_not_java(filePath)) {
                        isJava = true;
                        currentFile = projectPath + "/" + filePath;
                        Map<Integer, String> line4code = new TreeMap<>();
                        fileMap.put(currentFile, line4code);
//                        modifiedClass.add(currentFile);
                    } else {
                        isJava = false;
                    }
                }
                continue;
            }
            if (isJava) {
                int baseLine, delta;
                if (line.startsWith("@@")) {
                    // 提取新增代码行，格式为 “+66,5” or "-13 +13"
                    String change = line.split("\\-")[1].split(" ")[0];
                    // 消除“+”
                    change = change.substring(0, change.length());
                    // 获取变更行号范围
                    if (change.contains(",")) {
                        baseLine = Integer.parseInt(change.split(",")[0]);
                        delta = Integer.parseInt(change.split(",")[1]);
                        for (int i = 0; i < delta; i++) {
                            if (currentFile != null) {
                                lineId.add(baseLine + i);
                            }
                        }
                    } else {
                        if (currentFile != null) {
                            lineId.add(Integer.parseInt(change));
                        }
                    }
                } else if (line.startsWith("-") && !line.startsWith("---")) { // 新增行号与新增内容对应
                    StringBuilder stringBuilder = new StringBuilder(line);
                    String s = stringBuilder.substring(1, line.length()).replace("\t", "");
                    fileMap.get(currentFile).put(lineId.get(0), s);
                    lineId.remove(0);
//                        log.info(fileMap.get(currentFile));
                }
            }
        }
        return fileMap;
    }

    public static boolean areEquivalent(String originalCode, String modifiedCode) {
        // 正则匹配变量声明和赋值操作
        String[] originalLines = originalCode.replace(","," ")
                                        .replace(";"," ").split(" ");
        String[] modifiedLines = modifiedCode.replace(","," ")
                .replace(";"," ").split(" ");
        boolean flag = false;
        // 判断是否同一类型声明
        if(originalLines[0].equals(modifiedLines[0])){
            Map<String,Integer> mapA = new HashMap<>();
            for(String line:originalLines){
                if(mapA.containsKey(line)){
                    mapA.put(line,mapA.get(line)+1);
                }else{
                    mapA.put(line,1);
                }
            }
            Map<String,Integer> mapB = new HashMap<>();
            for(String line:modifiedLines){
                if(mapB.containsKey(line)){
                    mapB.put(line,mapB.get(line)+1);
                }else{
                    mapB.put(line,1);
                }
            }
            if(Math.abs(mapA.get(originalLines[0])-mapB.get(originalLines[0]))==1){
                if(Objects.equals(mapA.get("="), mapB.get("="))){
                    flag = true;
                    for(String a:originalLines){
                        if(Objects.equals(a, "")|| Objects.equals(a, originalLines[0])){
                            continue;
                        }
                        if(!Objects.equals(mapA.get(a), mapB.get(a))){
                            flag = false;
                        }
                    }
                }
            }
            System.out.println(Arrays.stream(originalLines));
            System.out.println(Arrays.stream(modifiedLines));
        }

        return flag;
    }

    // 比较两个字符串列表的相似度和差异
    public static String compareStringLists(List<String> list1, List<String> list2, double threshold) {
//        if (list1.size() != list2.size()) {
//            System.out.println("列表大小不同");
//            return "";
//        }
        String type = "NOT_NOISE";
        List<String> inter1 = new ArrayList<>(list1);
        List<String> inter2 = new ArrayList<>(list2);
        inter1.removeAll(list2);
        inter2.removeAll(list1);
//        List<String> targetList = new ArrayList<>();
//        targetList.addAll(inter1);
//        targetList.addAll(inter2);
        boolean isSame = true;
        if(inter1.isEmpty() || inter2.isEmpty()){
            // 因为上一层已经提取出add和del的行，即是存在差异的
            // 若两个集合的交集完全重合，则为语序重排; Todo：完全包含，但不重合，则该段涉及重排/或语句移动+新增
            isSame = true;
        }else {
            isSame = false;
        }
        if(isSame){
            System.out.println("一致");
            type =  "StmtReArrangeOrder";
            return type;
        }
         isSame = checkStringMerging(list1,list2);
        if(isSame){
            System.out.println("一致");
            type =  "StringMerging";
            return type;
        }
        isSame = checkStringSplitting(list1,list2);
        if(isSame){
            System.out.println("一致");
            type =  "StringSplitting";
            return type;
        }
        isSame =  checkPRE_POST_FIX_EXPRESSION_DIVIDING(list1,list2);
        if(isSame){
            System.out.println("一致");
            type =  "SINGLE_IF_TO_CONDITIONAL_EXP";
            return type;
        }
        isSame = checkINFIX_EXPRESSION_DIVIDING(list1,list2);
        if(isSame){
            System.out.println("一致");
            type =  "INFIX_EXPRESSION_DIVIDING";
            return type;
        }

        isSame =  checkSINGLE_IF_TO_CONDITIONAL_EXP(list1,list2);
        if(isSame){
            System.out.println("一致");
            type =  "SINGLE_IF_TO_CONDITIONAL_EXP";
            return type;
        }

        // todo:或许应该分class为单位进行匹配
        isSame = checkCONDITIONAL_EXP_TO_SINGLE_IF(list1,list2);
        if(isSame){
            System.out.println("一致");
            type =  "CONDITIONAL_EXP_TO_SINGLE_IF";
            return type;
        }

        isSame = checkLOOP_IF_CONTINUE_TO_ELSE(list1,list2);
        if(isSame){
            System.out.println("一致");
            type =  "LOOP_IF_CONTINUE_TO_ELSE";
            return type;
        }
        // 块级比较
        String block1 = String.join("",list1);
        String block2= String.join("",list2);
//        String blockTmp1="", blockTmp2="";
//        if(block1.length()>)= block1.length()>=block2.length()?block1:block2;
        if(block1.length()>block2.length()){
            isSame = areEquivalent(block1,block2);
            if(isSame){
                System.out.println("一致");
                type =  "VAR_DECLARATION_MERGING";
                return type;
            }
        }else {
            isSame = areEquivalent(block2,block1);
            if(isSame){
                System.out.println("一致");
                type =  "VAR_DECLARATION_Dividing";
                return type;
            }
        }


        // 逐项比较列表中的字符串
        for (int i = 0; i < list1.size(); i++) {
            String str1 = list1.get(i);
            String str2 = list2.get(i);

            double similarity = calculateSimilarity(str1, str2);

            System.out.println("第 " + (i + 1) + " 项相似度: " + similarity * 100 + "%");

            if (similarity >= threshold) {
//                checkStmtReArrangeOrder(str1,str2,type);
                System.out.println("相似度达到阈值，进一步比较字符差异：");
                isSame = printCharacterDifferences(str1, str2) && isSame;
                if(isSame){
                    System.out.println("一致");
                    type =  "SwapIfElse";
                    break;
                }
                isSame = checkSWAP_EQUAL_SIDES(str1, str2);
                if(isSame){
                    type = "Switch_Equals_Sides";
                    break;
                }
                isSame = checkSwapEquals(str1, str2,type);
                if(isSame){
                    type = "Switch_String_Equal";
                    break;
                }
                System.out.println(type);
            } else {
                isSame = checkPP2AddAndMMtoMinus(str1, str2,type);
                if(isSame){
                    type = "PP2AddAndMMtoMinus";
                    break;
                }
                System.out.println("相似度低于阈值，不需要进一步比较。");
            }

            System.out.println("------");
        }

        return type;
    }

    // 计算字符串的相似度，基于Levenshtein距离
    public static double calculateSimilarity(String str1, String str2) {
        int maxLen = Math.max(str1.length(), str2.length());
        if (maxLen == 0) {
            return 1.0;
        }
        int editDistance = levenshteinDistance(str1, str2);
        return 1.0 - (double) editDistance / maxLen;
    }

    // Levenshtein距离算法
    public static int levenshteinDistance(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++) {
            for (int j = 0; j <= str2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int cost = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1;
                    dp[i][j] = Math.min(
                            dp[i - 1][j - 1] + cost,
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1)
                    );
                }
            }
        }
        return dp[str1.length()][str2.length()];
    }

    // 打印字符的差异
    public static boolean printCharacterDifferences(String str1, String str2) {
        List<Character> a = new ArrayList<>();
        List<Character> b =  new ArrayList<>();
        Map<Character,Integer> mapA = new HashMap<>();
        Map<Character,Integer> mapB = new HashMap<>();
        for (char ch : str1.toCharArray()) {
            a.add(ch); // 将字符添加到集合中
            mapA.put(ch,mapA.getOrDefault(ch,0)+1);
        }
        for (char ch : str2.toCharArray()) {
            b.add(ch); // 将字符添加到集合中
            mapB.put(ch,mapB.getOrDefault(ch,0)+1);
        }
        boolean flag = false;
        List<Character> diff = new ArrayList<>();
        for(Character character:mapA.keySet()){
            if (mapB.containsKey(character)){
                if(!Objects.equals(mapA.get(character), mapB.get(character))){
                    for (int i = 0; i < Math.abs(mapA.get(character)-mapB.get(character)); i++){
                        diff.add(character);
                    }
                    if(character == '!'){
                        if(Math.abs(mapA.get(character) - mapB.get(character)) % 2 == 1){
                            // swap if else 的初步特征
                            flag = true;
                            String type = "SwapIfElse";
                        }
                    }
                }
            }
        }
        List<Character> intera = new ArrayList<>(a);
        List<Character> interb =  new ArrayList<>(b);
        intera.removeAll(b);
        interb.removeAll(a);

        System.out.println("差异："+intera+" ," + interb);

        return flag;
    }

     @Test
    public void test_SWAP_EQUAL_SIDES() {
        actionApp1 action = new actionApp1();
        Set<String> resultNoiseList = new TreeSet<>();
        String time = getTime();
        String project_dir = "E:\\project-DataSets20240927\\symja-parser";
        Set<String> changeSet = new TreeSet<>();
        GitDiffUtils util = new GitDiffUtils();
        Map<String, Map<Integer, String>> diffMap = getDiffByJGit(
                project_dir,
                "03913ac92d1dd4fd34943604a7546f1416ad4353","476a0961eb380f9ad31ea822e620f4f37f495df0"//"c2be6ab9aa2eddec945359f2272303678ef71c39"
                , time);
    }


    public static boolean checkSWAP_EQUAL_SIDES(String str1, String str2) {
        boolean flag = false;
        if (str1.contains("==") && str2.contains("==")) {
            List<Character> a = new ArrayList<>();
            List<Character> b = new ArrayList<>();

            Map<Character, Integer> mapA = new HashMap<>();
            Map<Character, Integer> mapB = new HashMap<>();
            for (char ch : str1.toCharArray()) {
                a.add(ch); // 将字符添加到集合中
                mapA.put(ch, mapA.getOrDefault(ch, 0) + 1);
            }
            for (char ch : str2.toCharArray()) {
                b.add(ch); // 将字符添加到集合中
                mapB.put(ch, mapB.getOrDefault(ch, 0) + 1);
            }

            List<Character> diff = new ArrayList<>();
            for (Character character : mapA.keySet()) {
                if (mapB.containsKey(character)) {
                    if (!Objects.equals(mapA.get(character), mapB.get(character))) {
                        for (int i = 0; i < Math.abs(mapA.get(character) - mapB.get(character)); i++) {
                            diff.add(character);
                        }
                    }
                } else {
                    diff.add(character);
                }
            }
            if (diff.isEmpty()) {
                flag = true;

                // todo：添加多余的；；；；
            } else if (diff.size() == 1 && ((diff.get(0) == ' ') || (diff.get(0) == ';'))) {
                flag = true;

            }
        }
        return flag;
    }


    public static boolean checkSwapEquals(String str1, String str2, String type) {
        boolean flag = false;
        if(str1.contains("equals")&&str2.contains("equals")){
            List<Character> a = new ArrayList<>();
            List<Character> b =  new ArrayList<>();

            Map<Character,Integer> mapA = new HashMap<>();
            Map<Character,Integer> mapB = new HashMap<>();
            for (char ch : str1.toCharArray()) {
                a.add(ch); // 将字符添加到集合中
                mapA.put(ch,mapA.getOrDefault(ch,0)+1);
            }
            for (char ch : str2.toCharArray()) {
                b.add(ch); // 将字符添加到集合中
                mapB.put(ch,mapB.getOrDefault(ch,0)+1);
            }

            List<Character> diff = new ArrayList<>();
            for(Character character:mapA.keySet()){
                if (mapB.containsKey(character)){
                    if(!Objects.equals(mapA.get(character), mapB.get(character))){
                        for (int i = 0; i < Math.abs(mapA.get(character)-mapB.get(character)); i++){
                            diff.add(character);
                        }
                    }
                }else {
                    diff.add(character);
                }
            }
            if(diff.isEmpty()){
                   flag = true;
                    type = "Switch_String_Equal";
            // todo：添加多余的；；；；
            }else if(diff.size()==1&&((diff.get(0)==' ')||(diff.get(0)==';'))){
                flag = true;
                type = "Switch_String_Equal";
            }
        }
//
//        List<Character> intera = new ArrayList<>(a);
//        List<Character> interb =  new ArrayList<>(b);
//        intera.removeAll(b);
//        interb.removeAll(a);
//
//        System.out.println("差异："+intera+" ," + interb);

        return flag;
    }



    public static boolean checkPP2AddAndMMtoMinus(String str1, String str2, String type) {
        boolean flag = false;
        if(str1.contains("+") || str2.contains("-")){
            List<Character> a = new ArrayList<>();
            List<Character> b =  new ArrayList<>();

            Map<Character,Integer> mapA = new HashMap<>();
            Map<Character,Integer> mapB = new HashMap<>();
            for (char ch : str1.toCharArray()) {
                a.add(ch); // 将字符添加到集合中
                mapA.put(ch,mapA.getOrDefault(ch,0)+1);
            }
            for (char ch : str2.toCharArray()) {
                b.add(ch); // 将字符添加到集合中
                mapB.put(ch,mapB.getOrDefault(ch,0)+1);
            }
            boolean flag1 = false;
            List<Character> diff = new ArrayList<>();
            for(Character character:mapA.keySet()){
                if (mapB.containsKey(character)){
                    if(!Objects.equals(mapA.get(character), mapB.get(character))){
                        for (int i = 0; i < Math.abs(mapA.get(character)-mapB.get(character)); i++){
                            diff.add(character);
                        }
                    }
                    if(character == '+' || character == '-') {
                        if (Math.abs(mapA.get(character) - mapB.get(character)) == 1) {
                            flag1 = true;
                        }
                    }
                }else {
                    diff.add(character);
                }
                if(flag1){
                    if(diff.contains('=')){
                        if(mapA.containsKey('=') || mapB.containsKey('=')){
                            if(mapA.get('=') == 1 || mapB.get('=') ==1){
                                flag = true;
                                type = "PP2AddAndMMtoMinus";
                            }
                        }
                    }
                }
            }
            if(diff.isEmpty()){
                flag = true;
                type = "Switch_String_Equal";
                // todo：添加多余的；；；；
            }else if(diff.size()==1&&((diff.get(0)==' ')||(diff.get(0)==';'))){
                flag = true;
                type = "Switch_String_Equal";
            }
        }
//
//        List<Character> intera = new ArrayList<>(a);
//        List<Character> interb =  new ArrayList<>(b);
//        intera.removeAll(b);
//        interb.removeAll(a);
//
//        System.out.println("差异："+intera+" ," + interb);

        return flag;
    }

    public static boolean checkStmtReArrangeOrder(String str1, String str2, String type) {
        boolean flag = false;
        if(str1.contains("+") || str2.contains("-")){
            List<Character> a = new ArrayList<>();
            List<Character> b =  new ArrayList<>();

            Map<Character,Integer> mapA = new HashMap<>();
            Map<Character,Integer> mapB = new HashMap<>();
            for (char ch : str1.toCharArray()) {
                a.add(ch); // 将字符添加到集合中
                mapA.put(ch,mapA.getOrDefault(ch,0)+1);
            }
            for (char ch : str2.toCharArray()) {
                b.add(ch); // 将字符添加到集合中
                mapB.put(ch,mapB.getOrDefault(ch,0)+1);
            }
            boolean flag1 = false;
            List<Character> diff = new ArrayList<>();
            for(Character character:mapA.keySet()){
                if (mapB.containsKey(character)){
                    if(!Objects.equals(mapA.get(character), mapB.get(character))){
                        for (int i = 0; i < Math.abs(mapA.get(character)-mapB.get(character)); i++){
                            diff.add(character);
                        }
                    }
                    if(character == '+' || character == '-') {
                        if (Math.abs(mapA.get(character) - mapB.get(character)) == 1) {
                            flag1 = true;
                        }
                    }
                }else {
                    diff.add(character);
                }
                if(flag1){
                    if(diff.contains('=')){
                        if(mapA.containsKey('=') || mapB.containsKey('=')){
                            if(mapA.get('=') == 1 || mapB.get('=') ==1){
                                flag = true;
                                type = "PP2AddAndMMtoMinus";
                            }
                        }
                    }
                }
            }
            if(diff.isEmpty()){
                flag = true;
                type = "Switch_String_Equal";
                // todo：添加多余的；；；；
            }else if(diff.size()==1&&((diff.get(0)==' ')||(diff.get(0)==';'))){
                flag = true;
                type = "Switch_String_Equal";
            }
        }
        return false;
    }
    @Test
    public void test_VAR_DECLARATION_MERGING() {
        actionApp1 action = new actionApp1();
        Set<String> resultNoiseList = new TreeSet<>();
        String time = getTime();
        String project_dir = "E:\\project-DataSets20240927\\spring-hateoas";
        Set<String> changeSet = new TreeSet<>();
        GitDiffUtils util = new GitDiffUtils();
        Map<String, Map<Integer, String>> diffMap = getDiffByJGit(
                project_dir,
                "f3ced79e390e669b3f880b6dac0a131e1d090bce",
                "4c3bb15fc17544b3e6fac7f6f4eaded96b5b5326"//"c2be6ab9aa2eddec945359f2272303678ef71c39"
                , time);
    }

    @Test
    public void test_StmtReArrangeOrder() {
        actionApp1 action = new actionApp1();
        Set<String> resultNoiseList = new TreeSet<>();
        String time = getTime();
        String project_dir = "E:\\project-DataSets20240927\\symja-parser";
        Set<String> changeSet = new TreeSet<>();
        GitDiffUtils util = new GitDiffUtils();
        Map<String, Map<Integer, String>> diffMap = getDiffByJGit(
                project_dir,
                "26657a528ddb8a2cb2a7771948b3d72c9d743fe7","ad7582dad7c8d48f96ed325e0379bfde588cfdbf"//"c2be6ab9aa2eddec945359f2272303678ef71c39"
                , time);
    }

    @Test
    public void test_CONDITIONAL_EXP_TO_SINGLE_IF() {
        actionApp1 action = new actionApp1();
        Set<String> resultNoiseList = new TreeSet<>();
        String time = getTime();
        String project_dir = "E:\\project-DataSets20240927\\spring-hateoas";
        Set<String> changeSet = new TreeSet<>();
        GitDiffUtils util = new GitDiffUtils();
        Map<String, Map<Integer, String>> diffMap = getDiffByJGit(
                project_dir,
                "1550532b565ee932895943c00bb244f95490b2a6","3fd54e350c5d3d2768dcb80f855db7bfb55fd699"//"c2be6ab9aa2eddec945359f2272303678ef71c39"
                , time);
    }

    @Test
    public void test_LOOP_IF_CONTINUE_TO_ELSE() {
        actionApp1 action = new actionApp1();
        Set<String> resultNoiseList = new TreeSet<>();
        String time = getTime();
        String project_dir = "E:\\project-DataSets20240927\\spring-hateoas";
        Set<String> changeSet = new TreeSet<>();
        GitDiffUtils util = new GitDiffUtils();
        Map<String, Map<Integer, String>> diffMap = getDiffByJGit(
                project_dir,
                "66fb77ee8e6cafc8fd3b1108703022d3dae71578","9cbd14a3f8de3ebcff33c3d6dba2f075da3669e8"//"c2be6ab9aa2eddec945359f2272303678ef71c39"
                , time);
    }

//    @Test
    public static boolean checkLOOP_IF_CONTINUE_TO_ELSE(List<String> list1, List<String> list2){
        // 1. 仅针对等价转换（实验）   2. 实际场景？
        String regex = "\\s*([A-Za-z0-9 .<>()]+)\\s*=\\s*([^?]+)\\?(\\s*[^:]+):(\\s*[^;]+);";
        // 创建 Pattern 对象
        Pattern pattern = Pattern.compile(regex);

        List<String> tempList1 = new ArrayList<>();
        List<String> tempList2 = new ArrayList<>();
        boolean flag = false;
        // 创建 Matcher 对象
        for(String text:list2){
            if(text.contains("continue;") || text.replace(" ","").equals("continue;")){
                int index1 = list2.indexOf(text);
                tempList1 = list2.subList(index1,list2.size());
                for(String s:list1){
                    String ss = s.replace("{","").replace("}","")
                            .replace(" ","").replace("\t","");
                    if(ss.contains("else")){
                        int index2 = list1.indexOf(s);
                        tempList2 = list1.subList(index2,list1.size());
                        flag = true;
                    }
                }
            }
        }
//        boolean match = false;
        if(flag){
            String a = String.join(", ", tempList1);
            String b = String.join(", ", tempList2);
            if(calculateSimilarity(a,b)>0.75){
                return true;
            }else{
                List<String> intersect = new ArrayList<>(tempList1);
                List<String> diffsect = new ArrayList<>(tempList2);
                intersect.retainAll(tempList2);
                diffsect.removeAll(intersect);
                if((double) diffsect.size() /tempList2.size()<0.25){
                    return true;
                }
            }
//            for(String s:tempList1){
//                for (String t:tempList2){
//                    if(t.replace(" ","").equals(s.replace(" ",""))){
//                        match =  true;
//                    }else {
//                        match = false;
//                    }
//                }
//            }
        }

//        String text = "this.variables = variables == null ? TemplateVariables.NONE : variables;";
        return false;
    }

    public static boolean checkCONDITIONAL_EXP_TO_SINGLE_IF(List<String> list1, List<String> list2){
        String regex = "\\s*([A-Za-z0-9 .<>()]+)\\s*=\\s*([^?]+)\\?(\\s*[^:]+):(\\s*[^;]+);";
        // 创建 Pattern 对象
        Pattern pattern = Pattern.compile(regex);

        List<String> tempList = new ArrayList<>();
        // 创建 Matcher 对象
        for(String text:list2){
            Matcher matcher = pattern.matcher(text);
            if (matcher.matches()) {
                tempList.add(matcher.group(1));
                tempList.add(matcher.group(2));
                tempList.add(matcher.group(3));
                tempList.add(matcher.group(4));
            }
            if(!tempList.isEmpty()){
                List<String> matchedList = new ArrayList<>(tempList);
                boolean flag = false;
                for(String s:list1){
                    for(String s1:tempList){
                        if(!matchedList.contains(s1)){
                            continue;
                        }
                        if(s.contains(s1) ||
                                s.replace(" ", "").contains(s1.replace(" ", ""))){
                            matchedList.remove(s1);
                            flag = true;
                        } else{
                            flag = false;
                        }
                        if(matchedList.isEmpty()){
                            // key feature of single if
                            if(list1.contains("if")){
                                return true;
                            }

                        }
                    }
                }
            }
        }
//        String text = "this.variables = variables == null ? TemplateVariables.NONE : variables;";
        return false;
    }

    @Test
    public void test_SINGLE_IF_TO_CONDITIONAL_EXP() {
        actionApp1 action = new actionApp1();
        Set<String> resultNoiseList = new TreeSet<>();
        String time = getTime();
        String project_dir = "E:\\project-DataSets20240927\\spring-hateoas";
        Set<String> changeSet = new TreeSet<>();
        GitDiffUtils util = new GitDiffUtils();
        Map<String, Map<Integer, String>> diffMap = getDiffByJGit(
                project_dir,
                "e0128b10c3e26d25d943fb7a49fe06110afb2b2d","b1a43095ca3c09ca3934baf0e0180f3b94bd7d89"//"c2be6ab9aa2eddec945359f2272303678ef71c39"
                , time);
    }

    public static boolean checkSINGLE_IF_TO_CONDITIONAL_EXP(List<String> list1, List<String> list2){
        String regex = "\\s*([A-Za-z0-9 .<>()]+)\\s*=\\s*([^?]+)\\?(\\s*[^:]+):(\\s*[^;]+);";
        String regex_split1 = "\\s*([A-Za-z0-9 .<>()]+)\\s*=\\s*([^?]+)\\?(\\s*[^:]+)";
        String regex_split2 = "\\s*:(\\s*[^;]+)\\s*;";
        // 创建 Pattern 对象
        Pattern pattern = Pattern.compile(regex);
        Pattern pattern_split1 = Pattern.compile(regex_split1);
        Pattern pattern_split2 = Pattern.compile(regex_split2);
        List<String> tempList = new ArrayList<>();

        boolean isFind = false;
        boolean maySplit = false;

        for(String text:list1){
            if(!isFind){
                if(maySplit){
                    Matcher matcher1 = pattern_split2.matcher(text);
                    if(matcher1.matches()){
                        tempList.add(matcher1.group(1));
                        isFind = true;
                    }else {
                        maySplit = false;
                        tempList.clear();
                    }
                    continue;
                }
                Matcher matcher = pattern.matcher(text);
                if (matcher.matches()) {
                    tempList.add(matcher.group(1).trim());
                    tempList.add(matcher.group(2).trim());
                    tempList.add(matcher.group(3).trim());
                    tempList.add(matcher.group(4).trim());
                    isFind = true;
                }else {
                    matcher = pattern_split1.matcher(text);
                    if(matcher.matches()){
                        tempList.add(matcher.group(1).trim());
                        tempList.add(matcher.group(2).trim());
                        tempList.add(matcher.group(3).trim());
                        maySplit = true;
                    }
                }
            }
        }

        if(isFind){
            List<String> tmpList2 = new ArrayList<>();
            if(!tempList.isEmpty()){
                List<String> matchedList = new ArrayList<>(tempList);
                boolean flag = false;
                boolean flag1 = false;
                String v = "";
                for(String s:list2){
                    // 先判断变更前的if语句
                    if(s.replace(" ", "").startsWith("if")){
                        Matcher matcher2 = Pattern.compile("if\\s*(.*?)\\s*").matcher(s);
                        if(matcher2.matches()){
                            tmpList2.add(matcher2.group(1).trim());
                        }
                    }
                    Matcher matcher3 = Pattern.compile("\\{?\\s*(\\w+)\\s*=\\s*(.*?);\\s*\\}?\\s*").matcher(s);
                    if(matcher3.matches()){
                        if(flag1 && !v.isEmpty()){
                            if(v.equals(matcher3.group(1))){
                                tmpList2.add(matcher3.group(2).trim());
                                break;
                            }
                        }
                        v = matcher3.group(1);
                        tmpList2.add(0,v);
                        tmpList2.add(matcher3.group(2).trim());
                    }
                    if(s.replace(" ", "").startsWith("else")){
                        flag1 = true;
                    }
                }
                // 比较 todo: 去除刻意或者说多余的外层括号（），也算是一种【格式扰动（可读性或规范性）】
                for(String s1:tempList){
                    if(tmpList2.contains(s1.replace(" ",""))){
                        flag = true;
                    }else if(tmpList2.contains("("+s1.replace(" ", "")+")")){
                        flag = true;
                    }else {
                        flag = false;
                        break;
                    }
                }
                return flag;
            }
        }
//        String text = "this.variables = variables == null ? TemplateVariables.NONE : variables;";
        return false;
    }

    @Test
    public void test_PRE_POST_FIX_EXPRESSION_DIVIDING() {
        actionApp1 action = new actionApp1();
        Set<String> resultNoiseList = new TreeSet<>();
        String time = getTime();
        String project_dir = "E:\\project-DataSets20240927\\symja-parser";
        Set<String> changeSet = new TreeSet<>();
        GitDiffUtils util = new GitDiffUtils();
        Map<String, Map<Integer, String>> diffMap = getDiffByJGit(
                project_dir,
                "78b6ea1fd38c3c3cc26c680c0a4e5d6baf1c659a","a7f8302dbc51dd9becb44ad0d11950302ed8a661"//"c2be6ab9aa2eddec945359f2272303678ef71c39"
                , time);
    }

    // a = fun(b++) or(b--,--b,++b);  ->   a = fun(b); b++; 甚至 a+=2
    public static boolean checkPRE_POST_FIX_EXPRESSION_DIVIDING(List<String> list1, List<String> list2){
        String regex ="\\s*[A-Za-z0-9 .<>()=]*" +
                "\\(\\s*" +
                "((\\w+\\s*\\+\\+)" +
                "|(\\+\\+\\s*\\w+)" +
                "|(--\\s*\\w+)|(\\w+\\s*--)" +
                "|(\\w+\\s*[\\+\\-]=\\s*\\d+))" +
                "\\s*[A-Za-z0-9 .<>()=;]*";
        // 创建 Pattern 对象
        Pattern pattern = Pattern.compile(regex);
        String pre_post_exp = "";
        String tempStr = "";
        String pre_post = "";
        String exp = "";
        String regex1 = "\\+\\+\\s*(\\w+)|(\\w+)\\s*\\+\\+|--\\s*(\\w+)|(\\w+)\\s*--|(\\w+)\\s*[\\+\\-]=\\s*\\d+";
        Pattern pattern1 = Pattern.compile(regex1);
        // 遍历删减行list2
        for(String text:list2){
            Matcher matcher = pattern.matcher(text);
            if (matcher.matches()) {
                pre_post_exp = matcher.group(1);
                tempStr = text.replace(pre_post_exp, "?");
                Matcher matcher1 = pattern1.matcher(pre_post_exp);
                if(matcher1.matches()){
                    exp = matcher1.group(1);
                    if((exp == null || exp.isEmpty())
                            && matcher1.group(2) != null){
                        exp = matcher1.group(2);
                    }
                    pre_post = pre_post_exp.replace(" ", "").replace(exp, "");

                }
            }
            if(!pre_post_exp.isEmpty()){
                boolean flag = false;
                for(String s:list1){
                    if(s.replace(" ", "")
                            .equals(tempStr.replace(" ", "").replace("?",exp))){
                        flag = true;
                    }
                    if(flag){
                        if(s.replace(" ", "").contains(pre_post_exp.replace(" ", ""))){
                            return true;
                        }
                    }
                }
            }

        }
//        String text = " System.out.print( eol -- );";
//        Matcher matcher = pattern.matcher(text);
//        if (matcher.matches()) {
//            System.out.println(matcher.group(1));
//        }
        return false;
    }


     @Test
    public void test_VAR_DECLARATION_DIVIDING() {
        actionApp1 action = new actionApp1();
        Set<String> resultNoiseList = new TreeSet<>();
        String time = getTime();
        String project_dir = "E:\\project-DataSets20240927\\spring-hateoas";
        Set<String> changeSet = new TreeSet<>();
        GitDiffUtils util = new GitDiffUtils();
        Map<String, Map<Integer, String>> diffMap = getDiffByJGit(
                project_dir,
                "c53a8219ead66ce588e6a496c6767aa0d17c7d14","eaa968b42b60f6891257701064c4f569540fbef2"//"c2be6ab9aa2eddec945359f2272303678ef71c39"
                , time);
    }

    @Test
    public void test_INFIX_EXPRESSION_DIVIDING() {
        actionApp1 action = new actionApp1();
        Set<String> resultNoiseList = new TreeSet<>();
        String time = getTime();
        String project_dir = "E:\\project-DataSets20240927\\spring-hateoas";
        Set<String> changeSet = new TreeSet<>();
        GitDiffUtils util = new GitDiffUtils();
        Map<String, Map<Integer, String>> diffMap = getDiffByJGit(
                project_dir,
                "0fb62b3d4ac2b17385d1eaa8a4ab31a2d5b89bd6","52c6881db4ff593764e4882956d894cfc7036f6a" //"c2be6ab9aa2eddec945359f2272303678ef71c39"
                , time);
    }

    // a = fun(b++) or(b--,--b,++b);  ->   a = fun(b); b++; 甚至 a+=2
    public static boolean checkINFIX_EXPRESSION_DIVIDING(List<String> list1, List<String> list2){
        String regex = "if\\s*[(](.*)[)]";
        Pattern pattern = Pattern.compile(regex);
        List<String> tempList = new ArrayList<>();
        boolean flag = false;
        // 创建 Matcher 对象
        for(String text:list2){
            Matcher matcher = pattern.matcher(text);
            if(matcher.find()){
                String condition = matcher.group(1); // 获取捕获的条件部分
                System.out.println("完整条件表达式: " + condition);
                // 按 "&&" 或 "||" 分割条件
                List<String> conditions = extractConditions(condition);
            System.out.println("提取的条件:");
            for (String cond : conditions) {
                String regex1 = "\\s*!?\\s*[.,\\w+\\-*/%()]+\\s*" +
                        "|\\s*!?\\s*[.,\\w+\\-*/%()]+\\s*(>|<|<=|>=|==|!=)\\s*\\w+";
                Pattern pattern1 = Pattern.compile(regex1);
                Matcher matcher1 = pattern1.matcher(cond);
                if(matcher1.find()){
                   tempList.add(cond.replace(" ",""));
                }
            }
            if(!tempList.isEmpty()){
                String tmpVar = "";
                List<String> tmpList2 = new ArrayList<>();
                for(String s:list1){
                    if(s.trim().startsWith("boolean")){
                       String regex_bool =  "\\s*boolean\\s+([A-Za-z0-9 .<>()]+)\\s*=\\s*(.*)\\s*;";
                       Pattern pattern2 = Pattern.compile(regex_bool);
                       Matcher matcher2 = pattern2.matcher(s);
                       if(matcher2.matches()){
                           tmpVar = matcher2.group(1);
                           String tmpStr = matcher2.group(2);
                           if(tmpStr!=null){
                               if(tempList.contains(tmpStr.replace(" ",""))){
                                   flag = true;
                                   tmpList2.add(matcher2.group(2));
                               }
                           }
                       }
                    }
                    if(flag){
                        if(s.trim().startsWith("if")){
                            if(s.replace(" ", "").contains(tmpVar.replace(" ", ""))){
                                return true;
                            }
                        }
                    }

                }
            }
            }
        }
//        String text = "this.variables = variables == null ? TemplateVariables.NONE : variables;";
        return false;
    }
    @Test
    public void test_StringSplitting() {
        actionApp1 action = new actionApp1();
        Set<String> resultNoiseList = new TreeSet<>();
        String time = getTime();
        String project_dir = "E:\\project-DataSets20240927\\RuoYi";
        Set<String> changeSet = new TreeSet<>();
        GitDiffUtils util = new GitDiffUtils();
        Map<String, Map<Integer, String>> diffMap = getDiffByJGit(
                project_dir,
                "287774ff4ca0cb0a8ecfa4cff42a71438c02f279","d55d991be516cefdeb04aba81888d72571e0842a" //"c2be6ab9aa2eddec945359f2272303678ef71c39"
                , time);
    }

    public static boolean checkStringSplitting(List<String> list1, List<String> list2){
        // 还原重现法？即 将删除的语句还原，再对比变更后的语句，对比是否一致
        String regex = "\\s*String\\s+([a-zA-Z0-9]+)\\s*=\\s*(\"[^\"]+\")\\s*";
        Pattern pattern = Pattern.compile(regex);
        List<String> tempList = new ArrayList<>(); // 存所有符合字符串声明+赋值的语句
        boolean flag = false;
        // 创建 Matcher 对象
        for(String text:list2){
            Matcher matcher = pattern.matcher(text);
            if(matcher.find()){
                String varName = matcher.group(1); // 变量
                String literal = matcher.group(2); // 赋值
                tempList.add(text);
            if(literal != null){
                for(String s:list1){
                    String regex1 = "\\s*String\\s+([a-zA-Z0-9]+)\\s*=\\s*(\"[^\"]+\")(?:\\s*\\+\\s*(\"[^\"]+\"))*";
                    s = s.replace("\r","");
                    Pattern pattern1 = Pattern.compile(regex1);
                    Matcher matcher1 = pattern1.matcher(s);
                    List<String> matches = new ArrayList<>();
                    // 查找匹配
                    if (matcher1.find()) {
                        // 提取第一个字符串字面值
                        matches.add(matcher1.group(2).replace("\"", ""));
                        // 提取连接的字符串
                        String remaining = s.substring(matcher1.end(2)); // 剩下的部分，去掉已经匹配的部分
                        // 继续匹配加号后面的字符串
                        Pattern plusPattern = Pattern.compile("\"([^\"]+)\"");
                        Matcher plusMatcher = plusPattern.matcher(remaining);
                        while (plusMatcher.find()) {
                            matches.add(plusMatcher.group(1));  // 提取连接的字符串字面值
                        }
                    }
                    if(String.join("", matches).equals(literal.replace("\"",""))){
                        flag = true;
                    }
                }
                }
            }
        }
//        String text = "this.variables = variables == null ? TemplateVariables.NONE : variables;";
        return flag;
    }


        @Test
    public void test_StringMerging() {
        actionApp1 action = new actionApp1();
        Set<String> resultNoiseList = new TreeSet<>();
        String time = getTime();
        String project_dir = "D:\\JavaProject\\jta\\tmp\\static-dynamic-diff";
        Set<String> changeSet = new TreeSet<>();
        GitDiffUtils util = new GitDiffUtils();
        Map<String, Map<Integer, String>> diffMap = getDiffByJGit(
                project_dir,
                "9c0e384c19dc437451d03a0ec5c27b420d8f58f1","99e9c8a7766bb49cb945a47af6a5a4d9c0e8c947" //"c2be6ab9aa2eddec945359f2272303678ef71c39"
                , time);
    }

    public static boolean checkStringMerging(List<String> list1, List<String> list2){
        // 还原重现法？即 将删除的语句还原，再对比变更后的语句，对比是否一致
        String regex = "\\s*String\\s+([a-zA-Z0-9]+)\\s*=\\s*(\"[^\"]+\")(?:\\s*\\+\\s*(\"[^\"]+\"))*";
        Pattern pattern = Pattern.compile(regex);
        List<String> tempList = new ArrayList<>(); // 存所有符合字符串声明+赋值的语句
        boolean flag = false;
        // 创建 Matcher 对象
        for(String text:list2){
            text = text.replace("\\\\","\\"); // 处理路径分隔符转义
            Matcher matcher = pattern.matcher(text);
//            if(matcher.find()){
                List<String> matches = new ArrayList<>();
                // 查找匹配
                if (matcher.find()) {
                    // 提取第一个字符串字面值
                    matches.add(matcher.group(2).replace("\"", ""));
                    // 提取连接的字符串
                    String remaining = text.substring(matcher.end(2)); // 剩下的部分，去掉已经匹配的部分
                    // 继续匹配加号后面的字符串
                    Pattern plusPattern = Pattern.compile("\"([^\"]+)\"");
                    Matcher plusMatcher = plusPattern.matcher(remaining);
                    while (plusMatcher.find()) {
                        matches.add(plusMatcher.group(1));  // 提取连接的字符串字面值
                    }
                }
            if(!matches.isEmpty()){
                for(String s:list1){
                    String regex1 = "\\s*String\\s+([a-zA-Z0-9]+)\\s*=\\s*(\"[^\"]+\")\\s*";
                    Pattern pattern1 = Pattern.compile(regex1);
                    Matcher matcher1 = pattern1.matcher(s);
                    String varName = matcher1.group(1); // 变量
                    String literal = matcher1.group(2); // 赋值
                    tempList.add(s);
                    if(String.join("", matches).equals(literal.replace("\"",""))){
                        flag = true;
                    }
                }
//                }
            }
        }
//        String text = "this.variables = variables == null ? TemplateVariables.NONE : variables;";
        return flag;
    }

    @Test
    public void ee(){
         String input = "   String path = \"D:\\JavaProject\\MyIdea\\\" + \"DetectChangeNoise\";";

        // 修正后的正则表达式
        String regex = "\\s*String\\s+([a-zA-Z0-9]+)\\s*=\\s*(\"[^\"]+\")(?:\\s*\\+\\s*(\"[^\"]+\"))*";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            // 提取变量名
            System.out.println("变量名: " + matcher.group(1));
            // 提取第一个字符串字面值
            System.out.println("第一个字符串: " + matcher.group(2));

            // 如果有连接部分，提取连接的字符串字面值
            if (matcher.group(3) != null) {
                System.out.println("连接的字符串: " + matcher.group(3));
            }
        } else {
            System.out.println("没有匹配到!");
        }
//        CleanRefactor cleanRefactor = new CleanRefactor();
//        cleanRefactor.checkRefactoringFromCommits( "8d3ff25cd8ac3df9d5a29745ddbde165310b932a","65daacf37cbee16185c348ead0a280ceb322fcf7", "", "E:\\project-DataSets20240927\\spring-hateoas");
//        String code = "if (!variable.isRequired() && start < baseUriEndIndex) {";//"boolean G99xPVaX = start < baseUriEndIndex;";
////        String regex_bool =  "\\s*boolean\\s+([A-Za-z0-9 .<>()]+)\\s*=\\s*(.*)\\s*;";
////                       Pattern pattern2 = Pattern.compile(regex_bool);
////                       Matcher matcher2 = pattern2.matcher(code);
////                       if(matcher2.matches()){
////                           System.out.println(matcher2.group(1));
////                           System.out.println(matcher2.group(2));
////                       }
//        // 正则表达式用于提取 if 条件表达式
//        String regex = "if\\s*[(](.*)[)]";
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(code);
//
//        // 查找匹配的条件表达式
//        if (matcher.find()) {
//            String condition = matcher.group(1); // 获取捕获的条件部分
//            System.out.println("完整条件表达式: " + condition);
//
//            // 按 "&&" 或 "||" 分割条件
//            List<String> conditions = extractConditions(condition);
//            System.out.println("提取的条件:");
//            for (String cond : conditions) {
//                String regex1 = "\\s*!?\\s*[.,\\w+\\-*/%()]+\\s*" +
//                        "|\\s*!?\\s*[.,\\w+\\-*/%()]+\\s*(>|<|<=|>=|==|!=)\\s*\\w+";
//                Pattern pattern1 = Pattern.compile(regex1);
//                Matcher matcher1 = pattern1.matcher(cond);
//                if(matcher1.matches()){
//                    System.out.println(cond);
//                }
//            }
//        } else {
//            System.out.println("没有找到 if 语句");
//        }
    }

    // 提取条件
    private static List<String> extractConditions(String condition) {
        List<String> conditions = new ArrayList<>();
        // 使用正则提取多个条件
        String regex = "[^&|]*(?=\\s*(?:&&|\\|\\|)?\\s*(?=\\)|$|\\S))";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(condition);

        while (matcher.find()) {
            conditions.add(matcher.group().trim());
        }

        return conditions;
    }
    public static <T>java.util.List<T> minus(java.util.List<T> a, java.util.List<T> b){
        return a.stream().filter(x->!b.contains(x)).collect(Collectors.toList());
    }
}

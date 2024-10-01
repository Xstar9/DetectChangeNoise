package gitop.utils;


import gitop.ast.MethodBlockVisitor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gitop.utils.GitDiffUtils.filter_not_java;


/**
 * DiffRelatedMethodUtils  提取变更的类文件 变更内容代码行范围
 *
 * @author Z.X
 * @Date   2023/12/18
 *
 */
@Slf4j
public class DiffRelatedMethodUtils {

    /**
     * getAfterChangeRange 提取【变更后】的类文件下，变更的行范围
     * eg:   @@ -25,9 【+24,0】 @@、 @@ -25 +25 @@
     */
    public List<List<Integer>> getAfterChangeRange(String result) {
//        log.info("AfterChangeRange: ");
        List<List<Integer>> rangeList = new ArrayList<>();
        String[] lines = result.split("\r?\n");
        String regex = "@@ -([0-9]+)(,[0-9]+)? \\+([0-9]+)(,[0-9]+)? @@.*\n?";
        Pattern changeLine_pattern = Pattern.compile(regex);
        for (String line : lines) {
            Matcher m = changeLine_pattern.matcher(line);
            if (m.find()) {
                List<Integer> range = new ArrayList<>();
                if (line.contains(",")) {
                    int changeLineStart = Integer.parseInt(m.group(3));
                    String flag = m.group(4);
                    if (m.group(4) != null && !Objects.equals(flag.replace(",", ""), "0")) {
//                        log.info("Start Line : " + changeLineStart + "  change to End Line " + (Integer.parseInt(flag.replace(",","")) + changeLineStart - 1));
                        range.add(changeLineStart);
                        range.add((Integer.parseInt(flag.replace(",", "")) + changeLineStart - 1));
                    } else {
//                        log.info("Start Line : " + changeLineStart + "  change to End Line " +  changeLineStart);
                        range.add(changeLineStart);
                        range.add(changeLineStart);
                    }
                } else { //@@ -13 +13 @@
                    range.add(Integer.parseInt(m.group(3)));
                    range.add(Integer.parseInt(m.group(3)));
                }
                rangeList.add(range);
            }
        }
        return rangeList;
    }

    /**
     * getOldChangeRange
     * eg:   @@ 【-25,9】 +24,0 @@
     */
    public List<List<Integer>> getOldChangeRange(String result) {
//        log.info("OldChangeRange: ");
        List<List<Integer>> rangeList = new ArrayList<>();
        String[] lines = result.split("\r?\n");
        String regex = "@@ -([0-9]+)(,[0-9]+)? \\+([0-9]+)(,[0-9]+)? @@.*\n?";
        Pattern changeLine_pattern = Pattern.compile(regex);
        for (String line : lines) {
            Matcher m = changeLine_pattern.matcher(line);
            if (m.find()) {
                List<Integer> range = new ArrayList<>();
                if (line.contains(",")) {
                    int changeLineStart = Integer.parseInt(m.group(1));
                    String flag = m.group(2);
                    if (m.group(4) != null && !Objects.equals(flag.replace(",", ""), "0")) {
//                        log.info("Start Line : " + changeLineStart + "  change to End Line " + (Integer.parseInt(flag.replace(",","")) + changeLineStart - 1));
                        range.add(changeLineStart);
                        range.add((Integer.parseInt(flag.replace(",", "")) + changeLineStart - 1));
                    } else {
//                        log.info("Start Line : " + changeLineStart + "  change to End Line " +  changeLineStart);
                        range.add(changeLineStart);
                        range.add(changeLineStart);
                    }
                } else { //@@ -13 +13 @@
                    range.add(Integer.parseInt(m.group(1)));
                    range.add(Integer.parseInt(m.group(1)));
                }
                rangeList.add(range);
            }
        }
        return rangeList;
    }

    /**
     * getChangeFileAndLines 获取发生变更的差异区域（范围）
     */
    public Map<String, List<List<Integer>>> getChangeFileAndLines( String result) {
        String regex = "^diff --git a/\\S+ b/(\\S+)";
        Pattern fileName_pattern = Pattern.compile(regex);
        String[] segment;
        String[] lines = result.split("\r?\n");
        segment = result.split("(diff --git.*\n?)");
        List<String> change_segment = new ArrayList<>();
        List<String> change_file = new ArrayList<>();
        boolean cond = false;
        StringBuilder add_content = new StringBuilder();
        StringBuilder del_content = new StringBuilder();
        for (String line : lines) {
            Matcher m = fileName_pattern.matcher(line);
            if (m.find()) {
                if (add_content.length() > 0) {
                    change_segment.add(add_content.toString());
//                    log.info(content);
                    add_content = new StringBuilder();
                }
//                cond = true;
                String fileName = m.group(1);
                if (!filter_not_java(fileName)) { // 是java 往下继续写
                    cond = true;
                    change_file.add(fileName);
                    add_content.append(line).append("\n");
                } else {
                    cond = false; // 不是java 往下不操作
                }
//                content += line;
                //                log.info("Change File: "+fileName);
            } else if (cond) {
                if (!line.isEmpty()) {
                    add_content.append(line).append("\n"); // 继续写
                    if (line.startsWith("+") && !line.startsWith("+++")) {
//                        getChangeLinesOfMethodCall(changeSet,line);
                    }
                }
            }
        }
        change_segment.add(add_content.toString());
        log.info(change_file.toString());
        Map<String, List<List<Integer>>> methodLocMap = new TreeMap<>();
//        for (String seg :segment){
//            if(!seg.isEmpty()){
//                log.info(seg);
//                change_segment.add(seg);
//            }
//        }
        for (int i = 0; i < change_file.size(); i++) {
            methodLocMap.put(change_file.get(i), getAfterChangeRange(change_segment.get(i)));
        }
        return methodLocMap;
    }

    public void getChangeLinesOfMethodCall(Set<String> changeSet, String codeLine) {
        String patternString = "\\b(\\w+)\\.(\\w+)\\((?:.*?(\\.)*?)*?\\)";//"\\b(\\w+)\\.(\\w+)\\(.*?\\);"; Todo:暂未兼容拼接方法调用
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(codeLine);
        List<String> methodList = new ArrayList<>();
        // 执行模式匹配
        while (matcher.find()) {
            log.info(codeLine);
            String className = matcher.group(1);
            String methodName = matcher.group(2);
            log.info("类名: " + className);
            log.info("方法名: " + methodName);
            changeSet.add(methodName);// className+"."+
        }
//        ChangeLMCallMap.put(codeLine,methodList); //TODO： 后续映射以下行及其内容到数据结构
    }

    /**
     * showChangeBelongMethod
     * 利用获取的行号定位具体的方法
     */
    public void showChangeBelongMethod(String prefix_path){
        //        String prefix_path = "D:\\JavaProject\\ForMyIdea\\homework\\tmp\\refactoring-toy-example\\"; // dir_path + project_name
        Set<String> changeSet = new TreeSet<>();
        String s = AccurateUtils.readFile(prefix_path+"my_diff.diff");// 读取diff信息 prefix_path+"my_diff.diff"(diff输出文件)
        Map<String, List<List<Integer>>> resultMap = getChangeFileAndLines(s);
        //        log.info("Change info：" + resultMap);
        //        log.info("----------------------------------------------------------------------------------");
        MethodBlockVisitor visitor = new MethodBlockVisitor();
        for(String path : resultMap.keySet()){
            //            log.info("Change File(.java): " + path);
            Map<String, List<Integer>> file = visitor.showClassMethod(
                    AccurateUtils.readFile(prefix_path+path)
            );
            for(String method : file.keySet()){
                for(int i=0;i<resultMap.get(path).size();i++){
                    //                    log.info(resultMap.get(path).get(i).get(0)+" "+resultMap.get(path).get(i).get(1)+" "+ file.get(method).get(0)+" "+ file.get(method).get(1));
                    if(AccurateUtils.isChangeInMethodRange(resultMap.get(path).get(i).get(0),
                            resultMap.get(path).get(i).get(1),
                            file.get(method).get(0),
                            file.get(method).get(1))){
                        changeSet.add(method.split(" ")[1]);
                    }
                }
            }
        }
        log.info("Change Impact Set:");
        log.info(changeSet.toString());
    }
}

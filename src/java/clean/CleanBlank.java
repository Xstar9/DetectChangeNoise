package clean;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * CleanBlank 去除空白、注释、调用符.用空格隔开 等噪声
 *
 * @author Z.X
 *
 */
@Slf4j
public class CleanBlank {
    public static String project_dir = "D:\\JavaProject\\demo3\\tmp\\refactoring-toy-example";

    public static void detectNoisy(Map<String, Map<Integer, String>> diffMap) {
//        String startCommit= "1fbc115d1e94ae9d08dd933ddbe841cfb56f48e8";
//        String endCommit = "5c98bbc738989bc1b12a7cce009e9fc4f707a495";
////        String startCommit= "61d4adb4a9596ea77edf9261efde9199366c34de";
////        String endCommit = "a9463054d415ac6b168d1149c389cf3526a248dd";
//        String time = getTime();
//        GitDiffUtil util = new GitDiffUtil();
//        Map<String, Map<Integer,String>> diffMap = util.getDiffByJGit(project_dir,startCommit,endCommit,time);
        for (Map.Entry<String, Map<Integer, String>> entry : diffMap.entrySet()) {
//            String changeFile = entry.getKey();
            Map<Integer, String> codeInfo = entry.getValue();
//              人工添加换行验证
//            codeInfo.put(34, "             ");
//            codeInfo.put(35,"\n");
//            codeInfo.put(36,"\t");
//            codeInfo.put(37,"return a. get()\n");
            StringBuilder comment = new StringBuilder();
            Set<Integer> line_num = new TreeSet<>();
            //  获取 /*  .... */的内容
            boolean flag = true;
            for (Map.Entry<Integer, String> codeLine : codeInfo.entrySet()) {
                int line = codeLine.getKey();
                String code = codeLine.getValue();
//                log.info("Key: " + line + ", Value: " + code);
                if (check_blank_line(code)) {
                    log.info("删除了第 {} 行 reason: blank_line", line);
                }
                if (check_single_comment(code)) {
                    log.info("删除了第 {} 行 reason: single_comment_line", line);
                }
                if (code.startsWith("/*")) {
                    flag = true;
                    line_num.add(line);
                    comment.append(code);
                }
                if (code.endsWith("*/")) {
                    flag = false;
                    line_num.add(line);
                    comment.append(code);
                    if (check_javadoc_comment(comment.toString())) {
                        log.info("第{}行 为javadoc or MultiLine comment", line_num);
                        line_num.clear();
                    }
                }
                if (flag) {
                    line_num.add(line);
                    comment.append(code);
                }
            }
        }
        //        log.info(diffMap);
    }

    /**
     * 检测空白行
     */
    public static boolean check_blank_line(String str) {
//        String blank_line_regex = "^\\s*$";
//        Pattern pattern = Pattern.compile(blank_line_regex);
//        Matcher matcher = pattern.matcher(str);
//        return matcher.find();
        return str.trim().isEmpty();
    }

    /**
     * 检测空格
     */
    public static boolean check_blank_space(String str) {
        String blank_line_regex = "(\\.\\s*)\\n|\\.\\s+|\\s*;\\s*";
        Pattern pattern = Pattern.compile(blank_line_regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    /**
     * 检测多行注释
     */
    public static boolean check_javadoc_comment(String str) {
        // /** */ javadoc     /* */多行注释
        String blank_line_regex = "/\\*(?s).*?\\*/$";
        Pattern pattern = Pattern.compile(blank_line_regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    /**
     * 检测单行注释
     */
    public static boolean check_single_comment(String str) {
        String single_line_regex = "//.*$";
        Pattern pattern = Pattern.compile(single_line_regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

}

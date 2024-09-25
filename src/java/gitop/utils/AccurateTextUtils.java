package gitop.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AccurateTextUtils
 *
 * @author Z.X
 *
 */
public class AccurateTextUtils {
    // 获取层次编号
    public static String pattern_Hierarchy = "\\s{0,}\\[\\d+\\]#\\s*";
    public static String pattern_ClassAndLine = "\\s{0,}\\[.*:\\d+]\\s*";
    public static String pattern_removeHierarchy = "\\s{0,}(\\[\\d+\\])\\s{0,}";

    /**
     * 去除文本中的匹配的内容
     *
     * @param text 待处理的文本
     * @return 去除匹配的内容后的文本
     */
    public static String removePattern(String pattern, String text) {
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(text);
        // 替换匹配到的文本为空字符串
        return matcher.replaceAll("");
    }

    public static String getPattern(String pattern, String text) {
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(text);
        // 替换匹配到的文本为空字符串
        if (matcher.find()) {
            return matcher.group(0);
        }
        return text;
    }


    public static String getHierarchyPattern(String text) {
        // 使用正则表达式匹配类似 [2]# 的格式
        String pattern = "\\[(\\d+)\\]#";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // 返回原文本
        return text;
    }

    /**
     * 划分代码行号，避免文本字面值中的"\n"导致划分错误
     *
     * @param code 代码内容
     * @return 每行代码
     */
    public static List<String> parseCodeLines(String code) {
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        boolean inString = false;
        char stringDelimiter = '\0';

        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);

            if (c == '"' || c == '\'') {
                if (inString) {
                    if (c == stringDelimiter) {
                        inString = false;
                    }
                } else {
                    inString = true;
                    stringDelimiter = c;
                }
            }

            if (c == '\n' && !inString) {
                lines.add(currentLine.toString());
                currentLine.setLength(0);
            } else {
                currentLine.append(c);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
}

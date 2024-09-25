package processing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoadProject {
    private static final String METHOD_PATTERN = "(\\w+)\\s*\\(([^)]*)\\)";

    public static List<String> extractMethods(String text) {
        List<String> methods = new ArrayList<>();

        // 编译正则表达式
        Pattern pattern = Pattern.compile(METHOD_PATTERN);
        Matcher matcher = pattern.matcher(text);

        // 查找匹配项
        while (matcher.find()) {
            String methodName = matcher.group(1); // 方法名
//            String parameters = matcher.group(2); // 参数列表
            methods.add(methodName);// + "(" + parameters + ")");
        }

        return methods;
    }

    public static void main(String[] args) {
        String inputText = "[IntPriorityQueue offer (int), IntPriorityQueue swap (int, int)]"
                                    .replace("[","")
                                    .replace("]","");

        List<String> extractedMethods = extractMethods(inputText);

        // 输出提取到的方法
        for (String method : extractedMethods) {
            System.out.println("提取的方法: " + method);
        }

    }
}

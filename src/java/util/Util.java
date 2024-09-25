package util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    public static void main(String[] args) {
        System.out.println(extractMethodCallLineId("[1]#  [Test01:136]\tcom.bci.overload.OverLoadDemo:show(com.bci.overload.Animal)\n"));
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

    public static boolean isChangeInMethodRange(int start1, int end1, int start2, int end2) {
        return start2 <= start1 && end2 >= end1 && (start1 <= end1 && start2 <= end2);
    }
    public static String extractMethodCallLineId(String text) {
//        String text1 = readFile("D:\\JavaProject\\jta\\tmp-933ac1e7c139db18\\_jacg_o_er\\20240724-135002.336\\Test001@test01@JkK_R7kmYqzHf8rEerjjpg==#033.txt");
        // 正则表达式用于匹配 Test001:47 或 com.bci.polomoly.Animal:16

        Pattern pattern = Pattern.compile("\\[(\\w+:\\d+|[\\w\\.]+:\\d+)\\]");
//        int line = 0;
//        // 处理第一个字符串
//        for (String a:text1.split("\n")){
            Matcher matcher1 = pattern.matcher(text);
            if (matcher1.find()) {
                System.out.println("Extracted info from text: " + matcher1.group(1));
                return matcher1.group(1);
            }
            return text;
//            else {
//                line++;
//                System.out.println("第"+line+"行没有该模式");
//            }
//        }
    }

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
}

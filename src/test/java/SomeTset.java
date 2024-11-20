import cn.hutool.core.io.FileUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.*;
public class SomeTset {

    public static boolean areEquivalent(String originalCode, String modifiedCode) {
        // 创建 ASTParser 实例，用于解析 Java 代码
        ASTParser parser = ASTParser.newParser(AST.JLS8);

        // 将代码源设置为输入
        parser.setSource(originalCode.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        // 解析原始代码并获得 AST
        CompilationUnit originalAST = (CompilationUnit) parser.createAST(null);

        // 解析修改后的代码并获得 AST
        parser.setSource(modifiedCode.toCharArray());
        CompilationUnit modifiedAST = (CompilationUnit) parser.createAST(null);

        // 提取变量赋值的表达式
        Map<String, String> originalAssignments = extractAssignments(originalAST);
        Map<String, String> modifiedAssignments = extractAssignments(modifiedAST);

        // 比较两个代码的赋值是否一致
        return originalAssignments.equals(modifiedAssignments);
    }

    // 提取赋值语句
    private static Map<String, String> extractAssignments(CompilationUnit ast) {
        final Map<String, String> assignments = new HashMap<>();

        ast.accept(new ASTVisitor() {
            @Override
            public boolean visit(Assignment assignment) {
                // 获取左侧和右侧的表达式
                if (assignment.getLeftHandSide() instanceof SimpleName && assignment.getRightHandSide() instanceof MethodInvocation) {
                    SimpleName left = (SimpleName) assignment.getLeftHandSide();
                    MethodInvocation right = (MethodInvocation) assignment.getRightHandSide();

                    // 只有 Integer.parseInt 类型的赋值才会被考虑
                    if ("parseInt".equals(right.getName().getIdentifier()) &&
                            right.arguments().size() == 1 &&
                            right.arguments().get(0) instanceof SimpleName) {

                        SimpleName argument = (SimpleName) right.arguments().get(0);
                        // 记录变量和它的赋值表达式
                        assignments.put(left.getIdentifier(), "Integer.parseInt(" + argument.getIdentifier() + ")");
                    }
                }
                return super.visit(assignment);
            }
        });

        return assignments;
    }

    public static void main(String[] args) {
        String originalCode =
                "int majorVersion = Integer.parseInt(parts[8]\n" +
                        "int minorVersion = Integer.parseInt(parts[1]);";

        String modifiedCode =
                "int majorVersion = Integer.parseInt(parts[0]), minorVersion = Integer.parseInt(parts[1]);";

        boolean result = areEquivalent(originalCode, modifiedCode);
        System.out.println("Are the codes equivalent? " + result);
    }

    @Test
    public void test() {
//        readCsvFileFromResult("D:\\JavaProject\\SPAT\\trans-commits\\trans-commits-datasets20240920.csv");
        FileUtil.del("D:\\JavaProject\\MyIdea\\DetectChangeNoise\\ddd");
    }

    public static List<List<String>> readCsvFileFromResult(String filePath) {
        List<List<String>> changeSetTripleList = new ArrayList<>();
        int i = 0;
        int sum = 0;
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath));
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            Set<String> typeSet = new HashSet<>();
            for (CSVRecord record : parser) {
                sum++;
                String changeSetTriple = record.isMapped("RelatedMethodSet") ? record.get("RelatedMethodSet")  : "N/A";
                if(changeSetTriple.replace("[", "").replace("]", "").split(", ").length >6){
                    i++;
                }
                System.out.println(changeSetTriple.replace("[", "").replace("]", "").split(", ").length);
                String CaseNums = record.isMapped("RelatedMethodSet") ? record.get("RelatedMethodSet")  : "N/A";
                if(changeSetTriple.replace("[", "").replace("]", "").split(", ").length >6){
                    i++;
                }
                System.out.println(changeSetTriple.replace("[", "").replace("]", "").split(", ").length);
            }
            System.out.println(sum);
            System.out.println(i);
//            // 打印读取的数据
//            System.out.println("Noise Types:");
////            noiseTypeList.forEach(System.out::println);
//            typeSet.forEach(System.out::println);
////            System.out.println("\nChange Set Triples:");
//            changeSetTripleList.forEach(System.out::println);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return changeSetTripleList;
    }


}

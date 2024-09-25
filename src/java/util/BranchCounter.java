package util;

import org.eclipse.jdt.core.dom.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static spat.git.GitUtils.readFile;


/**
 * BranchCounter 模拟方法关联的测试用例数量
 *
 * @author xin
 * @Date   2024/9/4
 *
 */
public class BranchCounter {
    public static void main(String[] args) {
        System.out.println(countMethodBranchPaths("E:\\project-DataSets1\\RuoYi\\ruoyi-common\\src\\main\\java\\com\\ruoyi\\common\\utils\\html\\HTMLFilter.java","class"));
    }

    public static Map<String,Integer> countMethodBranchPaths(String path,String type) {
        // 假设 sourceCode 是要解析的 Java 源代码字符串
//       String path = "D:\\JavaProject\\SPAT\\src\\java\\clean\\RuleSelector.java";
        Map<String,Integer> methodBranchPathsMap = new TreeMap<>();
        String sourceCode = readFile(path);
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(sourceCode.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        String type1 = "class";
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        cu.accept(new ASTVisitor() {
            String type1 = "class";
            @Override
            public boolean visit(TypeDeclaration node) {
                if (node.isInterface()) {
                    // 如果是接口/枚举，跳过该节点
                    type1 = "interface";
                    return false;
                }
                return super.visit(node);
            }

            @Override
            public boolean visit(EnumDeclaration node) {
//                System.out.println("Found an enum: " + node.getName());
                type1 = "enum";
                return super.visit(node);
            }

            @Override
            public boolean visit(AnnotationTypeDeclaration node) {
//                System.out.println("Found an annotation: " + node.getName());
                type1 = "annotation";
                return super.visit(node);
            }

            @Override
            public boolean visit(MethodDeclaration node) {
//                System.out.println("Method: " + node.getName());
                if(node.getName().toString().equals("main")){ // 不考虑main方法
                    return super.visit(node);
                }
                int branchCount = countBranchPaths(node.getBody());
//                System.out.println("Branch paths: " + branchCount);
                methodBranchPathsMap.put(node.getName().toString(),branchCount);
                return super.visit(node);
            }
        });
        type = type1;
        return methodBranchPathsMap;
    }

    private static int countBranchPaths(Block body) {
        if (body == null) return 0;

        BranchCounterVisitor visitor = new BranchCounterVisitor();
        body.accept(visitor);
        return visitor.getBranchCount();
    }

    private static class BranchCounterVisitor extends ASTVisitor {
        private int branchCount = 1; // 初始路径数设为 1

        @Override
        public boolean visit(IfStatement node) {
            branchCount += 1; // 每个 `if` 语句都引入一个新的路径
            return super.visit(node);
        }

        @Override
        public boolean visit(SwitchStatement node) {
            List<?> statements = node.statements();
            int caseCount = (int) statements.stream().filter(stmt -> stmt instanceof SwitchCase).count();
            if (caseCount > 0) {
                branchCount += caseCount - 1; // `switch` 每个 case 分支增加
            }
            return super.visit(node);
        }

        @Override
        public boolean visit(ForStatement node) {
            branchCount += 1; // `for` 循环至少有两个路径：进入和不进入
            return super.visit(node);
        }

        @Override
        public boolean visit(WhileStatement node) {
            branchCount += 1; // `while` 循环同样有两个路径
            return super.visit(node);
        }

        @Override
        public boolean visit(DoStatement node) {
            branchCount += 1; // `do-while` 也有两个路径
            return super.visit(node);
        }

        @Override
        public boolean visit(TryStatement node) {
            if (node.catchClauses().size() > 0) {
                branchCount += 1; // `try-catch` 至少有两个路径
            }
            return super.visit(node);
        }

        @Override
        public boolean visit(MethodInvocation node) {
            // 遇到方法调用时，不继续分析，保持当前路径
            return false; // 不进入方法调用内部
        }

        public int getBranchCount() {
            return branchCount;
        }
    }


}

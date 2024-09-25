package gitop.ast;

import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.javaparser.StaticJavaParser.parse;

/**
 * JAVAParser 实现方法调用提取器
 *
 * @author Z.X
 */
@Slf4j
public class MethodCallExtractor {
    private static final List<String> EXCLUDED_METHODS = Arrays.asList("println", "log");
    public static String FILE_PATH1 = "D:\\JavaProject\\ForMyIdea\\homework\\tmp\\refactoring-toy-example\\src\\org\\animals\\DogManager.java"; // 请替换为实际的 Java 文件路径
    public static String FILE_PATH = "D:\\JavaProject\\ForMyIdea\\homework\\tmp\\mini-main\\src\\main\\java\\com\\example\\mini\\controller\\AbcController.java";

    /**
     * 获取指定文件中，指定方法中的所有内部方法调用及其位置（所在行号）
     *
     * @param filePath 文件路径
     * @param method 方法名
     */
    public Map<String, Integer> getChangeLinesOfMethodCall(String filePath, String method) throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(filePath);
        CompilationUnit compilationUnit =  parse(fileInputStream);
        Map<String, Integer> methodCallMap = new HashMap<>();
        MethodCallVisitor methodCallVisitor = new MethodCallVisitor(methodCallMap, method);
        methodCallVisitor.visit(compilationUnit, null);
//        for(String methodName : methodCallMap.keySet()){
//            log.info(methodName + " : " + methodCallMap.get(methodName));
//        }
        return methodCallMap;
    }

    private static class MethodCallVisitor extends VoidVisitorAdapter<Void> {
        public String mName;
        Map<String, Integer> methodCallMap;

        public MethodCallVisitor(Map<String, Integer> methodCallMap, String name) {
            this.mName = name;
            this.methodCallMap = methodCallMap;
        }

        @Override
        public void visit(MethodCallExpr n, Void arg) {
            if (n.getScope().isPresent() && n.getScope().get() instanceof NameExpr) {
                Position position = n.getBegin().orElse(Position.pos(0, 0));
//                log.info("Interface Call: " + n + " at " + position.line);
//                methodCallMap.put(n.getNameAsString(), position.line);
                List<Expression> arguments = n.getArguments();// 获取参数列表
                StringBuilder parameters = new StringBuilder("(");
                for (Expression argument : arguments) {
                    // 打印参数的类型
                    String argumentType = argument.toString();
//                    log.info("Argument type: " + argumentType);
                    parameters.append(argumentType).append(", ");
                }
                if (parameters.length() > 1) {
                    parameters.setLength(parameters.length() - 2); // 移除末尾的逗号和空格
                }
                parameters.append(")");
                methodCallMap.put(n.getNameAsString() + parameters, position.line);
            } else {
//                log.info("Method Call: " + n);
            }
            super.visit(n, arg);
        }


        @Override
        public void visit(MethodDeclaration n, Void arg) {
            String currentVisitMethodDecl = n.getNameAsString();
            if (currentVisitMethodDecl.equals(mName)) {
//                log.info("Method Declaration: " + currentVisitMethodDecl);
                super.visit(n, arg);
            }

        }

        @Override
        public void visit(LambdaExpr n, Void arg) {
//            log.info("Lambda Expression: " + n.toString());
            super.visit(n, arg);
        }

        @Override
        public void visit(MethodReferenceExpr n, Void arg) {
//            log.info("Method Reference: " + n.toString());
            super.visit(n, arg);
        }

        @Override
        public void visit(ObjectCreationExpr n, Void arg) {
            if (n.getType() instanceof ClassOrInterfaceType) {
//                log.info("Callback Call: " + n);
            } else {
//                log.info("Object Creation: " + n.toString());
            }
            super.visit(n, arg);
        }

        @Override
        public void visit(ClassExpr n, Void arg) {
//            log.info("Class Expr: " + n.toString());
            super.visit(n, arg);
        }

        @Override
        public void visit(Parameter n, Void arg) {
//            log.info("Parameter: " + n.getNameAsString());
            super.visit(n, arg);
        }
        // 根据需要添加其他 visit 方法来处理更多的节点类型
    }
}

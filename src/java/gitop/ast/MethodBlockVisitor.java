package gitop.ast;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * MethodBlockVisitor for Eclipse JDT ASTParser
 *
 * @author Z.X
 */
@Slf4j
public class MethodBlockVisitor extends ASTVisitor {
    // think：JDT以一段源码为输入就能访问方法块，因此一般情况下是不易获取到类所在的包（全限定）的
    // 若需要可能需要传入指定的整个程序以及依赖，然后使用JDT的求解器类ResolveBinding（）获取类所在的包

    public static String className;

    public Map<String, List<Integer>> showClassMethod(String code) {
        Map<String, List<Integer>> methodLocMap = new TreeMap<>();
        ASTParser parser = ASTParser.newParser(AST.JLS8);
//        String code = readFile("D:\\JavaProject\\ForMyIdea\\homework\\tmp\\refactoring-toy-example" + "\\src\\org\\DogManager.java");
        parser.setSource(code.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
        compilationUnit.accept(new ASTVisitor() {
            public boolean visit(TypeDeclaration node) {
                log.info("Class: {}", node.getName().getIdentifier());
                className = node.getName().getIdentifier();
                // 访问类中的所有方法
//                for (Object bodyDeclaration : node.bodyDeclarations()) {
//                    if (bodyDeclaration instanceof MethodDeclaration) {
//                        MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
////                        visitMethodDeclaration(methodDeclaration);
//                    }
//                }
                return super.visit(node);
            }

            private void visitMethodDeclaration(MethodDeclaration node) {
                log.info("  Method: {}", node.getName().getIdentifier());
                // 获取方法块
                Block methodBlock = node.getBody();
                if (methodBlock != null) {
                    log.info("    Method Block: {}", methodBlock);
                }
            }

            @Override
            public boolean visit(MethodDeclaration node) {
                // 获取方法的返回类型
                String returnType = node.getReturnType2() != null ?
                        node.getReturnType2().toString() : "void";
                // 获取方法名
                String methodName = node.getName().getIdentifier();
                // 获取方法参数
                StringBuilder parameters = new StringBuilder("(");
                for (Object parameter : node.parameters()) {
                    if (parameter instanceof SingleVariableDeclaration) {
                        SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameter;
//                            String parameterType = variableDeclaration.getType().toString();
//                            String parameterName = variableDeclaration.getName().getIdentifier();
//                            parameters.append(parameterType).append(" ").append(parameterName).append(", ");
                        String parameterModifier = variableDeclaration.getType().toString();
                        parameters.append(parameterModifier).append(", ");
                    }
                }
                if (parameters.length() > 1) {
                    parameters.setLength(parameters.length() - 2); // 移除末尾的逗号和空格
                }
                parameters.append(")");
                // 获取方法块的起始行和结束行
                int startLine = compilationUnit.getLineNumber(node.getStartPosition());
                int endLine = compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());
//                if(node.toString().contains("@Override")){
//                    startLine-=1;
//                    endLine-=1;
//                }
//                log.info("Method Name: " + node.getName());
//                log.info("Method Signature: " + returnType + " " + methodName + parameters);
//                log.info("Start Line: " + startLine);
//                log.info("End Line: " + endLine);
//                log.info("--------------------------------------------------------------");
                List<Integer> list = new ArrayList<>();
                list.add(startLine);
                list.add(endLine);
                methodLocMap.put(className + " " + methodName + " " + parameters, list);
                return super.visit(node);
            }
        });
//        log.info(methodLocMap);
        return methodLocMap;
    }
}

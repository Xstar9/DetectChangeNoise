package gitop.ast;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 方法的注解访问器，用于对比注解是否变化
 *
 * @author xin
 */
@Slf4j
public class AnnotationVisitor extends ASTVisitor {

    /**
     * 从代码内容中遍历所有方法注解（值），并获取指定方法的注解
     *
     * @param method: 指定方法，获取其注解
     * @param code:  源码内容
     * @return annotations： 该源码内容（类）下的所有注解（接口路径）属性
     */
    public List<String> showMethodAnnotation(String method, String code) {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(code.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
        List<String> annotations = new ArrayList<>();
        compilationUnit.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration node) {
                boolean isMatch = true;
                // 检查是否是指定的方法
                if (node.getName().getIdentifier().equals(method.split(" ")[1])) {
                    List<String> param = List.of(method.split(" ")[2].split(","));
                    for (Object parameter : node.parameters()) {
                        if (parameter instanceof SingleVariableDeclaration) {
                            SingleVariableDeclaration variable = (SingleVariableDeclaration) parameter;
                            String parameterString = variable.getType().toString();
                            if (param.contains(parameterString)) {
                                isMatch = param.contains(parameterString) && isMatch;
                            }
                        }
                    }
                }else {
                    isMatch = false;
                }
                if (isMatch) {
                    // 获取方法的注解
                    IExtendedModifier[] modifiers = (IExtendedModifier[]) node.modifiers().toArray(new IExtendedModifier[0]);
                    for (IExtendedModifier modifier : modifiers) {
                        if (modifier.isAnnotation()) {
                            Annotation annotation = (Annotation) modifier;
                            String path = "";
                            if (annotation.toString().endsWith(")")) {
                                String annotationValue = annotation.toString();
                                // Extract path from the annotation
                                path = annotationValue.substring(annotationValue.indexOf("(") + 1, annotationValue.lastIndexOf(")"));
                            }
                            String value = path.isEmpty() ? "" : "(" + path + ")";
                            log.info("Method 【 {} }】 has annotation: @{}", node.getName(), annotation.getTypeName() + value);
                            annotations.add("@" + annotation.getTypeName() + value);
                        }
                    }
                    return super.visit(node);
                }
                return false;
            }
        });
//        log.info(methodLocMap);
//        return methodLocMap;
        return annotations;
    }
}

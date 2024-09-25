package jdt;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class StaticAnalysisOverloadVisitor{

    public static void main(String[] args) throws IOException {
        File file =  new File("D:\\JavaProject\\jta\\tmp-8f4e857caccf63da\\static-dynamic-diff-1.0-SNAPSHOT.jar.decompiled\\com\\bci\\controller\\Test01.java");
        String source = new String(Files.readAllBytes(file.toPath()));

//        // 解析源代码
//        Map<String, List<MethodDeclaration>> overloadedMethods = parseAndFindOverloadedMethods(source);
//
//        // 打印重载方法信息
//        System.out.println("Overloaded Methods:");
//        overloadedMethods.forEach((className, methods) -> {
//            System.out.println("Class: " + className);
//            methods.forEach(method -> {
//                System.out.println("  Method: " + method.getName().toString() + " Parameters: " + getParameterTypes(method));
//            });
//        });
        String[] classPathEntries = { "D:\\JavaProject\\jta\\tmp-8f4e857caccf63da\\BOOT-INF\\classes" };
        String[] sourcePathEntries = { "D:\\JavaProject\\jta\\tmp-8f4e857caccf63da\\static-dynamic-diff-1.0-SNAPSHOT.jar.decompiled" };
        // 检查方法调用
        List<MethodInvocation> methodInvocations = findMethodInvocations(source, classPathEntries, sourcePathEntries);
        for (MethodInvocation invocation : methodInvocations) {
            if (isOverloadedInvocation(invocation)){//, overloadedMethods)) {
//                System.out.println("Overloaded method called: " + invocation.getName().toString() + " Parameters: " + getArgumentTypes(invocation));
            }
        }

        System.out.println("调用点-调用指向全限定名：");
        overloadCallSite.forEach((k,v)->{
            System.out.println(k+" -->  "+v);
        });
    }

    public static Map<String, String> showAndFindOverloadedMethods(String dir,
                                                                   String[] classPathEntries,
                                                                   String[] sourcePathEntries) {
        File file =  new File(dir);//new File("D:\\JavaProject\\jta\\tmp-933ac1e7c139db18\\static-dynamic-diff-1.0-SNAPSHOT.jar.decompiled\\com\\bci\\controller\\Test01.java");
        String source = null;
        try {
            source = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        // 解析源代码
//        Map<String, List<MethodDeclaration>> overloadedMethods = parseAndFindOverloadedMethods(source);
//
//        // 打印重载方法信息
//        System.out.println("Overloaded Methods:");
//        overloadedMethods.forEach((className, methods) -> {
//            System.out.println("Class: " + className);
//            methods.forEach(method -> {
//                System.out.println("  Method: " + method.getName().toString() + " Parameters: " + getParameterTypes(method));
//            });
//        });
//        String[] classPathEntries = { "D:\\JavaProject\\jta\\tmp\\static-dynamic-diff\\target\\classes" };
//        String[] sourcePathEntries = { "D:\\JavaProject\\jta\\tmp\\static-dynamic-diff\\src\\main\\java" };
        // 检查方法调用
        List<MethodInvocation> methodInvocations = findMethodInvocations(source, classPathEntries, sourcePathEntries);
        for (MethodInvocation invocation : methodInvocations) {
            if (isOverloadedInvocation(invocation)){//, overloadedMethods)) {
//                System.out.println("Overloaded method called: " + invocation.getName().toString() + " Parameters: " + getArgumentTypes(invocation));
            }
        }

        System.out.println("调用点-调用指向全限定名：");
        overloadCallSite.forEach((k,v)->{
            System.out.println(k+" -->  "+v);
        });

        return overloadCallSite;
    }

    /**
     * 找出当前类文件中的所有MethodCall信息 如：a.A(1)、method.invoke("a")
     * @param source 被测类文件（.java）
     * @param classPathEntries 项目编译后的字节码文件和依赖的库文件路径[数组] xxx.jar、maven、xxx/target/classes、xxx/lib(s)
     * @param sourcePathEntries 项目源代码路径[数组] xxx/src/main/java、C:/xxx（如：C:/ruoyi/com/bci/...，只需给C:/ruoyi）
     */
    private static List<MethodInvocation> findMethodInvocations(String source,
                                                                String[] classPathEntries,
                                                                String[] sourcePathEntries) {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.setCompilerOptions(JavaCore.getOptions());

        // Classpath and sourcepath
//        String[] classpathEntries = { "D:\\JavaProject\\jta\\tmp\\static-dynamic-diff\\target\\classes" };
//        String[] sourcepathEntries = { "D:\\JavaProject\\jta\\tmp\\static-dynamic-diff\\src\\main\\java" };

        parser.setEnvironment(classPathEntries, sourcePathEntries, new String[] { "UTF-8" }, true);
        parser.setUnitName("YourClass.java");

        parser.setSource(source.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        List<MethodInvocation> methodInvocations = new ArrayList<>();

        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodInvocation node) {
                methodInvocations.add(node);
                return super.visit(node);
            }
        });

        return methodInvocations;
    }

    public static Map<String, String> overloadCallSite = new TreeMap<>();
    private static boolean isOverloadedInvocation(MethodInvocation invocation){ //, Map<String, List<MethodDeclaration>> overloadedMethodsMap) {
        IMethodBinding binding = invocation.resolveMethodBinding();
        if (binding != null) {
            if(invocation.toString().contains("System.out")){
                return false;
            }
            String declaringClassName = binding.getDeclaringClass().getQualifiedName();
            String methodName = binding.getName();
            List<String> argumentTypes = getArgumentTypes(invocation);
            overloadCallSite.put(invocation.toString(), declaringClassName+":"+methodName
                    +argumentTypes.toString()
                    .replace("[","(")
                    .replace("]",")"));
            return true;
//            if (overloadedMethodsMap.containsKey(declaringClassName)) {
//                List<MethodDeclaration> methods = overloadedMethodsMap.get(declaringClassName);
//                for (MethodDeclaration method : methods) {
//                    if (method.getName().toString().equals(methodName) && getParameterTypes(method).equals(argumentTypes)) {
//                        return true;
//                    }
//                }
//            }
        }
        return false;
    }

    private static List<String> getParameterTypes(MethodDeclaration method) {
        List<String> parameterTypes = new ArrayList<>();
        for (Object parameter : method.parameters()) {
            SingleVariableDeclaration varDecl = (SingleVariableDeclaration) parameter;
            parameterTypes.add(varDecl.getType().toString());
        }
        return parameterTypes;
    }

    private static List<String> getArgumentTypes(MethodInvocation invocation) {
        List<String> argumentTypes = new ArrayList<>();
        for (Object argument : invocation.arguments()) {
            Expression expr = (Expression) argument;
            ITypeBinding binding = expr.resolveTypeBinding();
            if (binding != null) {
                argumentTypes.add(binding.getQualifiedName());
            } else {
                argumentTypes.add("Unknown");
            }
        }
        return argumentTypes;
    }


    private static Map<String, List<MethodDeclaration>> parseAndFindOverloadedMethods(String source) {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.setCompilerOptions(JavaCore.getOptions());

        // Classpath and sourcepath
//        String[] classpathEntries = { "path/to/project/bin", "path/to/project/lib/some-library.jar" };
//        String[] sourcepathEntries = { "path/to/project/src" };
        String[] classpathEntries = { "D:\\JavaProject\\jta\\tmp-933ac1e7c139db18\\BOOT-INF\\classes" };
        String[] sourcepathEntries = { "D:\\JavaProject\\jta\\tmp-933ac1e7c139db18\\static-dynamic-diff-1.0-SNAPSHOT.jar.decompiled" };

        parser.setEnvironment(classpathEntries, sourcepathEntries, new String[] { "UTF-8" }, true);
        parser.setUnitName("YourClass.java");
        parser.setSource(source.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        Map<String, List<MethodDeclaration>> overloadedMethodsMap = new HashMap<>();

        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(TypeDeclaration node) {
                if (!node.isPackageMemberTypeDeclaration()) {
                    return true; // Skip nested types
                }

                ITypeBinding binding = node.resolveBinding();
                if (binding == null) {
                    return true; // Skip if binding is not resolved
                }

                String className = binding.getQualifiedName();
                MethodDeclaration[] methods = node.getMethods();

                Map<String, List<MethodDeclaration>> methodSignatures = new HashMap<>();
                for (MethodDeclaration method : methods) {
                    String methodName = method.getName().toString();
                    methodSignatures.computeIfAbsent(methodName, k -> new ArrayList<>()).add(method);
                }

                for (Map.Entry<String, List<MethodDeclaration>> entry : methodSignatures.entrySet()) {
                    if (entry.getValue().size() > 1) {
                        overloadedMethodsMap.put(className, entry.getValue());
                    }
                }

                return super.visit(node);
            }
        });

        return overloadedMethodsMap;
    }
}

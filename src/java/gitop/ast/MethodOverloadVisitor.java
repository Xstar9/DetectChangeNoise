package gitop.ast;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodOverloadVisitor extends ASTVisitor {
    private final Map<String, Map<String, List<MethodDeclaration>>> classMethodMap = new HashMap<>();
    private final Map<String, String> importsMap = new HashMap<>();
    private final CompilationUnit compilationUnit;
    private String currentClassName;

    public MethodOverloadVisitor(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
        initImports();
    }

    private void initImports() {
        for (Object importObj : compilationUnit.imports()) {
            ImportDeclaration importDecl = (ImportDeclaration) importObj;
            String qualifiedName = importDecl.getName().getFullyQualifiedName();
            String simpleName = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
            importsMap.put(simpleName, qualifiedName);
        }
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        currentClassName = getFullyQualifiedName(node);
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        if (currentClassName != null) {
            String methodName = node.getName().getIdentifier();
            classMethodMap
                    .computeIfAbsent(currentClassName, k -> new HashMap<>())
                    .computeIfAbsent(methodName, k -> new ArrayList<>())
                    .add(node);
        }
        return super.visit(node);
    }

    @Override
    public void endVisit(TypeDeclaration node) {
        currentClassName = null;
        super.endVisit(node);
    }

    public Map<String, List<String>> getOverloadedMethodsMap() {
        Map<String, List<String>> overloadedMethodsMap = new HashMap<>();
        for (Map.Entry<String, Map<String, List<MethodDeclaration>>> classEntry : classMethodMap.entrySet()) {
            String className = classEntry.getKey();
            List<String> overloadedMethods = new ArrayList<>();
            for (Map.Entry<String, List<MethodDeclaration>> methodEntry : classEntry.getValue().entrySet()) {
                if (methodEntry.getValue().size() > 1) {
                    StringBuilder overloadInfo = new StringBuilder();
                    overloadInfo.append(methodEntry.getKey()).append(" is overloaded Method: \n");
                    for (MethodDeclaration method : methodEntry.getValue()) {
                        overloadInfo.append(methodEntry.getKey()).append(getMethodSignature(method))
                                .append(" at lines ").append(getLineRange(method)).append("\n");
                        overloadedMethods.add(className.substring(className.lastIndexOf('.') + 1) + " "
                                + methodEntry.getKey() + " "
                                + getMethodSignature(method));
                    }
//                    overloadedMethods.add(overloadInfo.toString());
                }
            }
            if (!overloadedMethods.isEmpty()) {
                overloadedMethodsMap.put(className, overloadedMethods);
            }
        }
        return overloadedMethodsMap;
    }

    private String getMethodSignature(MethodDeclaration method) {
        StringBuilder signature = new StringBuilder();
        signature.append("(");
        for (Object paramObj : method.parameters()) {
            SingleVariableDeclaration param = (SingleVariableDeclaration) paramObj;
            String typeName = getFullyQualifiedName(param.getType());
            signature.append(typeName).append(", "); //.append(param.getName()).append(", ");
        }
        if (method.parameters().size() > 0) {
            signature.setLength(signature.length() - 2);  // remove last comma(逗号) and space
        }
        signature.append(")");
        return signature.toString();
    }

    private String getFullyQualifiedName(Type type) {
        if (type.isSimpleType()) {
            SimpleType simpleType = (SimpleType) type;
            String name = simpleType.getName().getFullyQualifiedName();
            return importsMap.getOrDefault(name, name);
        } else if (type.isPrimitiveType()) {
            return type.toString();
        } else if (type.isArrayType()) {
            ArrayType arrayType = (ArrayType) type;
            return getFullyQualifiedName(arrayType.getElementType()) + "[]";
        } else if (type.isParameterizedType()) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            StringBuilder qualifiedName = new StringBuilder(getFullyQualifiedName(parameterizedType.getType()));
            qualifiedName.append("<");
            for (Object argObj : parameterizedType.typeArguments()) {
                Type argType = (Type) argObj;
                qualifiedName.append(getFullyQualifiedName(argType)).append(", ");
            }
            if (parameterizedType.typeArguments().size() > 0) {
                qualifiedName.setLength(qualifiedName.length() - 2);  // remove last comma and space
            }
            qualifiedName.append(">");
            return qualifiedName.toString();
        } else if (type.isQualifiedType()) {
            QualifiedType qualifiedType = (QualifiedType) type;
            return getFullyQualifiedName(qualifiedType.getQualifier()) + "." + qualifiedType.getName();
        } else if (type.isNameQualifiedType()) {
            NameQualifiedType nameQualifiedType = (NameQualifiedType) type;
            return nameQualifiedType.getQualifier().getFullyQualifiedName() + "." + nameQualifiedType.getName();
        } else if (type.isWildcardType()) {
            WildcardType wildcardType = (WildcardType) type;
            StringBuilder wildcardName = new StringBuilder("?");
            if (wildcardType.getBound() != null) {
                wildcardName.append(" extends ").append(getFullyQualifiedName(wildcardType.getBound()));
            }
            return wildcardName.toString();
        }
        return type.toString();  // fallback for other types
    }

    private String getFullyQualifiedName(TypeDeclaration typeDecl) {
        String packageName = compilationUnit.getPackage() != null ?
                compilationUnit.getPackage().getName().getFullyQualifiedName() : "";
        return packageName.isEmpty() ? typeDecl.getName().getFullyQualifiedName() :
                packageName + "." + typeDecl.getName().getFullyQualifiedName();
    }

    private String getLineRange(MethodDeclaration method) {
        int startLine = compilationUnit.getLineNumber(method.getStartPosition());
        int endLine = compilationUnit.getLineNumber(method.getStartPosition() + method.getLength());
        return startLine + "-" + endLine;
    }
}

package spat.rules;
import java.security.SecureRandom;
import java.util.ArrayList;
import org.eclipse.jdt.core.dom.*;

import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import spat.Utils;

public class ChangeFor2ForEach extends ASTVisitor {
    CompilationUnit cu = null;
    Document document = null;
    String outputDirPath = null;
    ArrayList<ForStatement> fors = new ArrayList<>();

    public ChangeFor2ForEach(CompilationUnit cu_, Document document_, String outputDirPath_) {
        this.cu = cu_;
        this.document = document_;
        this.outputDirPath = outputDirPath_;
    }

    public boolean visit(ForStatement node) {
        fors.add(node);
        return true;
    }

    @SuppressWarnings("unchecked")
    public void endVisit(CompilationUnit node) {
        if (fors.isEmpty()) {
            System.out.println("empty");
            return;
        }
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        for (ForStatement forer : fors) {
            // Ensure the initializer is a variable declaration
            if (forer.initializers().size() == 1 && forer.initializers().get(0) instanceof VariableDeclarationExpression) {
                VariableDeclarationExpression initExpr = (VariableDeclarationExpression) forer.initializers().get(0);
                VariableDeclarationFragment fragment = (VariableDeclarationFragment) initExpr.fragments().get(0);
                SimpleName varName = fragment.getName();

                // Determine the type of the array from the declaration
                String arrayType = getArrayType(initExpr);
                System.out.println(initExpr);
                // Ensure the condition is a comparison with array length
                if (forer.getExpression() != null && forer.getExpression().toString().contains(varName.getIdentifier() + " < ")) {
                    String arrayName = forer.getExpression().toString().split(" < ")[1].split("\\.")[0];
                    System.out.println(arrayName);

                    // Ensure the updater is a simple increment
                    if (forer.updaters().size() == 1 && forer.updaters().get(0).toString().equals(varName.getIdentifier() + "++")) {
                        // Create the enhanced for statement
                        EnhancedForStatement enhancedForStmt = ast.newEnhancedForStatement();

                        // Set the variable type and name based on the array type
                        SingleVariableDeclaration enhancedForVar = ast.newSingleVariableDeclaration();
                        if ("String".equals(arrayType)) {
                            enhancedForVar.setType(ast.newSimpleType(ast.newName("String")));
                        } else if ("int".equals(arrayType)) {
                            enhancedForVar.setType(ast.newPrimitiveType(PrimitiveType.INT));
                        } else {
                            // Default to Object if unknown type
                            enhancedForVar.setType(ast.newSimpleType(ast.newName("Object")));
                        }
                        enhancedForVar.setName(ast.newSimpleName("element")); // New variable name for each element

                        // Set the collection expression
                        enhancedForStmt.setParameter(enhancedForVar);
                        enhancedForStmt.setExpression(ast.newSimpleName(arrayName));

                        // Set the body of the enhanced for loop
                        Statement body = (Statement) ASTNode.copySubtree(ast, forer.getBody());
                        enhancedForStmt.setBody(body);

                        // Replace the old for loop with the new enhanced for loop
                        rewriter.replace(forer, enhancedForStmt, null);
                    }
                }
            }
        }

        TextEdit edits = rewriter.rewriteAST(document, null);
        Utils.applyRewrite(node, edits, document, outputDirPath);
    }

    private String getArrayType(VariableDeclarationExpression initExpr) {
        // Extract the array type information
        Type type = initExpr.getType();
        if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) type;
            Type componentType = arrayType.getComponentType();
            if (componentType instanceof PrimitiveType) {
                PrimitiveType primitiveType = (PrimitiveType) componentType;
                if (primitiveType.getPrimitiveTypeCode() == PrimitiveType.INT) {
                    return "int";
                }
            } else if (componentType instanceof SimpleType) {
                SimpleType simpleType = (SimpleType) componentType;
                String typeName = simpleType.getName().getFullyQualifiedName();
                if (typeName.equals("String")) {
                    return "String";
                }
            }
        }
        return "Object"; // Default to Object if not recognized
    }
}

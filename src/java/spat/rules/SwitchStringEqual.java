package spat.rules;

import java.security.SecureRandom;
import java.util.*;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import spat.Utils;

public class SwitchStringEqual extends ASTVisitor{
	CompilationUnit cu = null;
	Document document = null;
	String outputDirPath = null;
	List<MethodInvocation> Stringequals = new ArrayList<MethodInvocation>();
	AST ast = null;
	ASTRewrite rewriter = null;
	// TODO：加一个容器存符合转换条件的文件，随机和筛选转换则在这些文件中选择1（或至多2）个进行转换
	//  标记转换，统计转换讯息，存入csv、构建数据记录数据集；转换失败的或者空提交的则不存入，打印到日志
	public SwitchStringEqual(CompilationUnit cu_, Document document_, String outputDirPath_) {
		this.cu = cu_;
		this.document = document_;
		this.outputDirPath = outputDirPath_;
		ast = cu.getAST();
		rewriter = ASTRewrite.create(ast);
	}
	public boolean visit(MethodInvocation stringE) {
		if(stringE.resolveMethodBinding() != null 
				&& stringE.resolveMethodBinding().getMethodDeclaration().getKey().toString().equals("Ljava/lang/String;.equals(Ljava/lang/Object;)Z")
				&& stringE.arguments().size() == 1) {
			Stringequals.add(stringE);
			return false;
		}
		return true;
	}	
	
	

	public void endVisit(CompilationUnit node) {
		if (Stringequals.size() == 0) {
			System.out.println(outputDirPath+" == empty");
			return;
		}
		System.out.println(outputDirPath+" == writing !!!!!!!!!!!!!!!!");
		SecureRandom rand = new SecureRandom();
		int random = rand.nextInt(Stringequals.size());
		int i = 0;
		for(MethodInvocation stringE : Stringequals) {
			if(i!=random){
				i++;
				continue;
			}
			//get the caller
			Expression LeftOne = (Expression) ASTNode.copySubtree(ast, stringE.getExpression());
			//get the augment
			Expression RightOne = (Expression) ASTNode.copySubtree(ast,(Expression) stringE.arguments().get(0));
			if (RightOne.getNodeType() == ASTNode.INFIX_EXPRESSION) {
				 ParenthesizedExpression tmp = ast.newParenthesizedExpression();
				 tmp.setExpression((Expression) ASTNode.copySubtree(ast,RightOne));
				 RightOne = tmp;
			}
			
			//create new StringEual
			MethodInvocation newOne = (MethodInvocation) ASTNode.copySubtree(ast, stringE);
			List<Expression> args = newOne.arguments();
			args.remove(0);
			args.add(LeftOne);
			newOne.setExpression(RightOne);
			if ((RightOne.getNodeType() == ASTNode.SIMPLE_NAME 
					|| RightOne.getNodeType() == ASTNode.METHOD_INVOCATION)) {
				if (RightOne.getNodeType() == ASTNode.METHOD_INVOCATION) {
					String tmper = "hhh";
				}
				InfixExpression IENE = ast.newInfixExpression();
				IENE.setOperator(InfixExpression.Operator.NOT_EQUALS);
				IENE.setLeftOperand((Expression) ASTNode.copySubtree(ast, RightOne));
				IENE.setRightOperand(ast.newNullLiteral());
				
				InfixExpression Wraper = ast.newInfixExpression();
				Wraper.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
				Wraper.setLeftOperand(IENE);
				Wraper.setRightOperand(newOne);
				ParenthesizedExpression tmp = ast.newParenthesizedExpression();
				tmp.setExpression(Wraper);
				rewriter.replace(stringE, tmp, null);
			}
			else {
				rewriter.replace(stringE, newOne, null);
			}
			
		}
		TextEdit edits = rewriter.rewriteAST(document, null);
		Utils.applyRewrite(node, edits, document,outputDirPath);
		
	}
}
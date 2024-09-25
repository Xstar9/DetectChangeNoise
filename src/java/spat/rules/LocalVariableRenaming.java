package spat.rules;

import java.util.*;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import spat.Utils;

public class LocalVariableRenaming extends ASTVisitor{
	Map<IBinding, ArrayList<SimpleName> > bindings2names = new HashMap<>();
	CompilationUnit cu = null;
	Document document = null;
	String outputDirPath = null;
	
	public LocalVariableRenaming(CompilationUnit cu_, Document document_, String outputDirPath_) {
		this.cu = cu_;
		this.document = document_;
		this.outputDirPath = outputDirPath_;
	}
	
	public boolean visit(SimpleName node) {
		IBinding ibd = node.resolveBinding();
		if (ibd==null) {
			return true;
		}
		if (this.bindings2names.containsKey(ibd)) {
			this.bindings2names.get(ibd).add(node);
//			System.out.println("Bingding:	" + ibd.toString() + "update to: " + this.bindings2names.toString());
		}
		else if(node.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT || node.getParent().getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION) {
			ArrayList<SimpleName> tmp = new ArrayList<SimpleName>();
			tmp.add(node);
			this.bindings2names.put(ibd, tmp);
//			System.out.println("find a new binding for declaration:" + ibd.toString());
		}
		return true;
	}
	public void endVisit(CompilationUnit node) {
//		System.out.println("Whole file is parsed! begin rewriting");
		if (bindings2names.isEmpty()) {
			return;
		}
		AST ast = cu.getAST();
		ASTRewrite rewriter = ASTRewrite.create(ast);
		Set<IBinding> variableBins = this.bindings2names.keySet();
		int counter = 0;
		for (IBinding varBin : variableBins) {
			if (counter >= Utils.maxTrans) break;
			// 随机在变量后加1-3个字符
			int random = new Random(3).nextInt();
			String newName = Utils.getRandomString(random);
			ArrayList<SimpleName> vars = this.bindings2names.get(varBin);
			// 随机重命名1-2个变量即可
			int times = new Random(2).nextInt();
			for(SimpleName var: vars) {
				times--;
				System.out.println(var.getIdentifier()+"----->"+var.getIdentifier()+newName);
				System.out.println(var.getStartPosition());
				rewriter.set(var, SimpleName.IDENTIFIER_PROPERTY, var.getIdentifier()+newName, null);
				if(times==0) break;
			}
			counter += 1;
		}
//		System.out.println("begin applying edits");
		TextEdit edits = rewriter.rewriteAST(document, null);
		Utils.applyRewrite(node, edits, document,outputDirPath);
	}
}

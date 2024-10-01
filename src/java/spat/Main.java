package spat;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;

public class Main {
	//use ASTParse to parse string
	public static void parse(String str, String dirPath,String outputdir,String[] arrString, String IdofRule,String filePath) {
		Document document = new Document(str);
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		parser.setBindingsRecovery(true);

		Map<String, String> options = JavaCore.getOptions();
		parser.setCompilerOptions(options);

		String unitName = "Apple.java";// Just some random name.
		parser.setUnitName(unitName);

		//String[] sources = Utils.SingleStr2priList(dirPath);//This make things complicated,we do not need to consider the relationship between files.
		String[] sources = {""};//Just the file itself.
		String[] classpath = arrString;

		parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);
		parser.setSource(str.toCharArray());

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
//
//		if (cu.getAST().hasBindingsRecovery()) {
//			System.out.println("Binding activated.");
//		}
//		else {
//			System.out.println("Binding is not activated.");
//		}
//		System.out.println(outputdir);
		outputdir = getOutputPath(filePath, dirPath, outputdir);
		cu.accept(RuleSelector.create(IdofRule, cu, document, outputdir));


	}

	public static String getOutputPath(String filePath, String dirPath, String outputDir) {
		String relativePath = filePath.substring(dirPath.length()+1);
		return outputDir + relativePath;
	}

	// loop directory to get file list
	public static void ParseFilesInDir(String dirPath, String outputDir, String[] arrString, String idOfRule) throws IOException {
		File root = new File(dirPath);
		File[] files = Utils.folderMethod(root.getAbsolutePath(), outputDir);
		List<File> fileList = Arrays.stream(files)
				.filter(File::isFile)
				.collect(Collectors.toList());

		// 随机选择1-2个文件进行转换
//		Collections.shuffle(fileList);
//		List<File> selectedFiles = fileList.stream().limit(2).collect(Collectors.toList());
		List<File> selectedFiles = new ArrayList<>();
//		System.out.println(selectedFiles);
		Set<File> fileSet = new HashSet<>(Arrays.asList(files));
		ForkJoinPool myPool = new ForkJoinPool(32);
		String keyword = "for";
		try {
//			List<File> filesWithForLoop = fileSet.stream()
//					.filter(File::isFile)
//					.filter(file -> {
//						try {
//							return containsKeyCondition(file,keyword);
//						} catch (IOException e) {
//							e.printStackTrace();
//							return false;
//						}
//					})
//					.collect(Collectors.toList());
//			if(filesWithForLoop.isEmpty()){
//				System.out.println("This project may no exist 【"+keyword+"】 Block or Statement，Thus no transform currently~");
//				return;
//			}
//			Collections.shuffle(filesWithForLoop);
//			selectedFiles.addAll(filesWithForLoop.stream().limit(filesWithForLoop.size()>1?2:1).collect(Collectors.toList()));
//			System.out.println(selectedFiles);

//			myPool.submit(() ->
					fileSet.parallelStream().forEach(f -> {
						String filePath = f.getAbsolutePath();
						if(f.isFile()){
							//	System.out.println("Current File is: " + filePath);
							try {
//								System.out.println(outputDir);
								parse(Utils.readFileToString(filePath), dirPath,outputDir, arrString, idOfRule,filePath);
							} catch (Exception e ) {
								// TODO Auto-generated catch block
								System.out.println("trans failed:	" + filePath);
							}catch (Error s) {
								// TODO Auto-generated catch block
								System.out.println("trans failed:	" + s.toString());
							}
						}
					});
//					).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(File file : selectedFiles){
			String prePath = file.getAbsolutePath();
			String postPath = outputDir+prePath.substring(prePath.indexOf("\\src\\main\\java\\")+15); // 15个字符是\src\main\java\
			System.out.println(prePath.substring(prePath.indexOf("\\src\\main\\java\\")+15));
			System.out.println("==========================================="+postPath);
			// todo: 仅仅只是调试需注释掉，因为会覆写源文件
//			writeToFile(prePath, readFile(postPath));
		}
		// 写入转换后的代码覆盖原始文件

//		 初始化 Git 仓库
//		try (Git git = Git.open(new File(dirPath.substring(0, dirPath.indexOf("\\src"))))) {
//			// 获取转换前的提交ID
//			String preCommitId = getCurrentCommitId(git);
//			// 提交转换后的代码
//			git.add().addFilepattern(".").call();
//			git.commit().setMessage("Automated conversion of renameType").call();
//
//			// 获取转换后的提交ID
//			String postCommitId = getCurrentCommitId(git);
//
//			// 记录提交前后的提交ID对
//			System.out.println("Pre-commit ID: " + preCommitId);
//			System.out.println("Post-commit ID: " + postCommitId);
//		} catch (GitAPIException e) {
//            throw new RuntimeException(e);
//        }
    }

	public static boolean containsKeyCondition(File file,String keyword) throws IOException {
		try (Stream<String> stream = Files.lines(file.toPath())) {
			return stream.anyMatch(line -> line.contains(keyword) && !line.trim().startsWith("//") );
		}
	}

	public static void main(String[] args) throws IOException {
		int maxTrans = 3;
		args = new String[4];
//		if (args.length == 5) {
//			maxTrans = Integer.parseInt(args[4]);
//		}
		// TODO： 改为随机转换、复合的转换（即非原子变更），单一的转换
		int random = new Random(17).nextInt();
//		args[0] = String.valueOf(random);
		args[0] = "0"; // transformType
		args[1] = "D:\\下载\\static-dynamic-diff\\src\\main\\java";// "E:\\projectDataSet\\spring-hateoas\\src\\main\\java";
		args[2] = "D:\\JavaProject\\MyIdea\\DetectChangeNoise\\transform-output\\static-dynamic-diff\\";
		args[3] = "C:\\Program Files\\Java\\jdk1.8.0_271\\jre\\lib\\rt.jar";

		Utils.maxTrans = maxTrans;
		if(args.length != 4 ) { // && args.length != 5
			System.out.println("SRAT needs four arguments to run properly: "
					+ "[theIdOfTheSelectedRule] [DirPathOftheSourcefiles] [OutputDir] [PathoftheJre(rt.jar)] & [PathofotherDependentJar] "
					+ "for example \"C:\\Program Files\\Java\\jre1.8.0_211\\lib\\rt.jar\"");
			System.exit(4);
		}
		String dirOfTheFiles = args[1];
		String outputDir = args[2];
		ArrayList <String> jre_rtPath = new ArrayList<String>();
		for(int i = 3; i < args.length;i++) {
			jre_rtPath.add(args[i]);
		}
		
		ParseFilesInDir(dirOfTheFiles, outputDir, Utils.ArryStr2priStrList(jre_rtPath), args[0]);
	}
}
package util;

import data.ClassInfo;
import data.DTO;
import data.MethodInfo;
import data.Project;
import jdt.ClassMethodsMapVisitor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static spat.git.GitUtils.readFile;

public class MockTestCase {
    public static String projectPath = "D:\\JavaProject\\range-test-benchmark";//"E:\\project-DataSets1\\RuoYi";

    public static void main(String[] args) {
        mockTestCase(projectPath);
    }
    public static Project mockTestCase(String projectPath){
            List<File> a = new ArrayList<>();
            for(String s : TestMethodCounter.findJavaSourceDirectories(projectPath)){
                TestMethodCounter.findTestFiles(new File(s),a);
            }
            Project project = new Project();
            project.setProjectName(projectPath.substring(projectPath.lastIndexOf("\\")+1));
            Map<String,String> classPathMap = new HashMap<>();
            for (File file : a){
                String classname = file.getAbsolutePath().
                        substring(file.getAbsolutePath().lastIndexOf("\\")+1,file.getAbsolutePath().length()-5);
                if(!classPathMap.containsKey(classname)){
                    classPathMap.put(classname,file.getAbsolutePath());
                }else {
                    classPathMap.put(classname+"1",file.getAbsolutePath());
                }

            }
            ClassMethodsMapVisitor classMethodsMapVisitor = new ClassMethodsMapVisitor();
//        classMethodsMapVisitor.showClassMethod(readFile(a.get(1).getAbsolutePath()));
            List<Map<String, List<String>>> resultList = new ArrayList<>();
            CodesStatistics cs = new CodesStatistics(new File(projectPath));
            for (File key :a){
                ClassInfo classInfo = new ClassInfo();
                String classPath =key.getAbsolutePath();
                String classname = classPath.
                        substring(classPath.lastIndexOf("\\")+1,classPath.length()-5);
                classInfo.setClassName(classname);
                classInfo.setClassPath(classPath);
                // 遍历类中的所有方法
                Map<String, List<String>> map = classMethodsMapVisitor.showClassMethod(readFile(classPath));
                List<String> tmpList = map.get(classname);
                List<MethodInfo> methodInfoList = new ArrayList<>();
                if(tmpList==null){
                    tmpList = new ArrayList<>();
                }else {
                    tmpList.forEach(s -> {
                        MethodInfo methodInfo = new MethodInfo();
                        methodInfo.setMethodName(s);
                        methodInfoList.add(methodInfo);
                    });
                }
                map.remove(classname);
                map.put(classPathMap.get(classname),tmpList);
                String type = "class";
                Map<String,Integer> methodBranchPaths = BranchCounter.countMethodBranchPaths(key.getAbsolutePath(),type);
//                System.out.println(methodBranchPaths);
//            System.out.println(classname + " 类方法数量：" + tmpList.size());
                if(methodBranchPaths.isEmpty()){
                    classInfo.setType("interface");
                    classInfo.setMethodList(methodInfoList);
                    project.getSrcClassList().add(classInfo);
//                System.out.println("接口类，只是声明，被实现，因此一般不包含分支路径，往往作为外部调用入口");
                    continue;
                }
                classInfo.setType("class");
                methodInfoList.forEach(m->{
                    if(methodBranchPaths.containsKey(m.getMethodName())){
                        m.setTestCaseCount(methodBranchPaths.get(m.getMethodName()));
                    }
                });
//            System.out.println(map);
//            System.out.println("方法路径数（模拟-方法关联用例数）：" + methodBranchPaths);
                classInfo.setMethodList(methodInfoList);
//                System.out.println(classInfo.getMethodList().stream().map(MethodInfo::getTestCaseCount).collect(Collectors.toList()));
//            classInfo.getMethodList().forEach(m-> {
//                if(m.getTestCaseCount()>=65536){
//                    System.out.println(m.getTestCaseCount());
//                    System.out.println(classInfo.getClassPath()+": "+m.getMethodName());
//                }
//            });
//                System.out.println(classInfo.getMethodList().stream().mapToInt(MethodInfo::getTestCaseCount).sum());
                classInfo.setTestCaseCount(classInfo.getMethodList().stream().mapToInt(MethodInfo::getTestCaseCount).sum());
                project.getSrcClassList().add(classInfo);
            }
            project.setTestCaseSum(project.getSrcClassList().stream().mapToInt(ClassInfo::getTestCaseCount).sum());
            project.setTotalLines(cs.countLines());
            project.setCommentLines(cs.countCommentLines());
            project.setValidLines(cs.countValidLines());
            project.setWhiteSpaceLines(cs.countWhiteSpaceLines());
//        System.out.println(project);
            List<ClassInfo> result = DTO.querySomeTypeClasses(project, "interface");
            return project;
        }

}

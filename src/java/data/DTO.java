package data;

import java.util.ArrayList;
import java.util.List;

public class DTO {
    public static List<ClassInfo>  querySomeTypeClasses(Project project,String type) {
        List<ClassInfo> result = new ArrayList<>();
        for (ClassInfo classInfo : project.getSrcClassList()) {
            if (classInfo.getType().equals(type)) {
                result.add(classInfo);
            }
        }
        return result;
    }

    public static void showAllMethods(ClassInfo classInfo) {
        System.out.println(classInfo.getClassName()+" 类下的方法：");
        classInfo.getMethodList().forEach(methodInfo
                -> {
            System.out.println(methodInfo.getMethodName()+" 方法路径数（模拟-方法关联用例数量）: "+methodInfo.getTestCaseCount());
        });
    }
}

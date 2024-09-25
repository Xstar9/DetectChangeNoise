package data;

import java.util.ArrayList;
import java.util.List;

public class ClassInfo {
    public String className;

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String classPath;

    public String type; // interface static class abstract

    public List<MethodInfo> getMethodList() {
        return methodList;
    }

    public void setMethodList(List<MethodInfo> methodList) {
        this.methodList = methodList;
    }

    public List<MethodInfo> methodList = new ArrayList<>();

    public int constructorCount;

    public int getsetCount;

    public int getTestCaseCount() {
        return testCaseCount;
    }

    public void setTestCaseCount(int testCaseCount) {
        this.testCaseCount = testCaseCount;
    }

    public int testCaseCount;


    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public int getConstructorCount() {
        return constructorCount;
    }

    public void setConstructorCount(int constructorCount) {
        this.constructorCount = constructorCount;
    }

    public int getGetsetCount() {
        return getsetCount;
    }

    public void setGetsetCount(int getsetCount) {
        this.getsetCount = getsetCount;
    }
}

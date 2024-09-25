package data;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Project {
    public String projectName;

    public List<ClassInfo> getSrcClassList() {
        return srcClassList;
    }

    public void setSrcClassList(List<ClassInfo> srcClassList) {
        this.srcClassList = srcClassList;
    }

    public List<ClassInfo> srcClassList = new ArrayList<>();

    public int totalLines;

    public int validLines;

    public int commentLines;

    public int whiteSpaceLines;

    public int testCaseSum;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }


    public int getTotalLines() {
        return totalLines;
    }

    public void setTotalLines(int totalLines) {
        this.totalLines = totalLines;
    }

    public int getValidLines() {
        return validLines;
    }

    public void setValidLines(int validLines) {
        this.validLines = validLines;
    }

    public int getCommentLines() {
        return commentLines;
    }

    public void setCommentLines(int commentLines) {
        this.commentLines = commentLines;
    }

    public int getWhiteSpaceLines() {
        return whiteSpaceLines;
    }

    public void setWhiteSpaceLines(int whiteSpaceLines) {
        this.whiteSpaceLines = whiteSpaceLines;
    }

    public int getTestCaseSum() {
        return testCaseSum;
    }

    public void setTestCaseSum(int testCaseSum) {
        this.testCaseSum = testCaseSum;
    }

//    public Map<String, Integer> getExpandClassMapTestCases() {
//
//        return expandClassMapTestCases;
//    }

    public void setExpandClassMapTestCases(Map<String, Integer> expandClassMapTestCases) {
        this.expandClassMapTestCases = expandClassMapTestCases;
    }

    public Map<String, Integer> expandClassMapTestCases;

    public Map<String, Integer> getExpandClassMapCases(String classInfos) {
        List<ClassInfo> classInfo = this.getSrcClassList().stream().filter(clazz -> clazz.getClassName().equals(classInfos)).collect(Collectors.toList());
        Map<String, Integer> expandClassMapCases = new HashMap<>();
        if(!classInfo.isEmpty()){
            List<MethodInfo> tmp = classInfo.get(0).getMethodList();
            for(MethodInfo methodInfo: tmp){
                expandClassMapCases.put(methodInfo.getMethodName(), methodInfo.getTestCaseCount());
            }
        }
        this.expandClassMapTestCases = expandClassMapCases;
        return this.expandClassMapTestCases;
    }

    public Map<String, Integer> flattenMethodInfoMap(String projectName) {
        List<ClassInfo> classInfo = this.getSrcClassList();
        Map<String, Integer> expandClassMapCases = new HashMap<>();
        if(!classInfo.isEmpty()){
            for (ClassInfo clazz: classInfo) {
                List<MethodInfo> tmp = clazz.getMethodList();
                Map<String, Integer> tmpMap = new HashMap<>();
                for (MethodInfo methodInfo : tmp) {
                    tmpMap.put(methodInfo.getMethodName(), methodInfo.getTestCaseCount());
                }
                expandClassMapCases.putAll(tmpMap);
            }
        }
        this.expandClassMapTestCases = expandClassMapCases;
        return this.expandClassMapTestCases;
    }
}

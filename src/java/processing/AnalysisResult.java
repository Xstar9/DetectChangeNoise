package processing;

import data.Project;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import util.MockTestCase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.Math.abs;
import static processing.LoadProject.extractMethods;

public class AnalysisResult {
    public static String[] resultPaths = {"D:\\JavaProject\\SPAT\\trans-commits\\result-20240920-2.csv", // 3组
            "D:\\JavaProject\\SPAT\\trans-commits\\result-20240920-3.csv"}; // 1-3组随机

    public static String[] dataPaths = {"D:\\JavaProject\\SPAT\\trans-commits\\trans-commits-datasets20240920.csv"};


    public static void countDetectResult() {
        String[] resultPaths = {"D:\\JavaProject\\SPAT\\trans-commits\\result-20240920-2.csv", // 3组
                "D:\\JavaProject\\SPAT\\trans-commits\\result-20240920-3.csv"}; // 1-3组随机

        String[] dataPaths = {"D:\\JavaProject\\SPAT\\trans-commits\\trans-commits-datasets20240920.csv"};

        readCsvFile(resultPaths[1]);
//        readCsvFilefromData(dataPaths[0]);
    }

    public static int a = 0;
    public static int b = 0;


    /**
     * expectedSet  实际转换的函数集
     * actualSet   CIA分析得出的影响方法受提交JDT重写影响,可能就会有些许改变（如Todo注释，换行格式等）
     *
     */
    public static boolean cmpChangeSetWithExpectAndResult(List<String> expectedSet,
                                                          List<String> actualSet) {
        Set<String> expected = new HashSet<>(expectedSet);
        Set<String> actual = new HashSet<>(actualSet);
        Set<String> retainTmp = new HashSet<>(expectedSet);
        int base = abs(actual.size()-expected.size());
//        if(actual.size()>expected.size())
//        {
//            a++;
//        }else {
//            b++;
//        }
        retainTmp.retainAll(actual); // 说明预期转换的过拟合 要多于结果（影响函数分析）的过拟合
        if(retainTmp.size() == expected.size()){
//            System.out.println("ALL CORRECT");
//        }
//        if(retainTmp.size()<=10){   // a :355  b : 53
                System.out.println(retainTmp);
                a++;
                return true;
        }else if(retainTmp.size() < expected.size()){
//            System.out.println("ALL CORRECT");
//        }
//        if(retainTmp.size()<=10){   // a :355  b : 53
            System.out.println(retainTmp);
            System.out.println(expectedSet);
            a++;
            return true;
        }
//        }
        else {
            System.out.println("TOO MANY");
            b++;
        }

//        retainTmp.removeAll(expectedSet);
        return false;
    }

    public static void cmp() {
        List<List<String>> listsList1 = readCsvFileFromResult(resultPaths[1]);
        List<List<String>> listsList2 = readCsvFilefromData(dataPaths[0]);
        for (int i = 0; i < listsList2.size(); i++) {
            cmpChangeSetWithExpectAndResult(listsList2.get(i), listsList1.get(i));
        }
    }

    public static Map<String, Integer> countColumnValues2(String filePath, int columnIndex) {
        Map<String, Integer> valueCountMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                // Skip header line
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String values = line.replace("\"", "").split(",")[columnIndex - 1];
                if (!valueCountMap.containsKey(values.split("/")[0])) {
                    valueCountMap.put(values.split("/")[0], 1);
                } else {
                    valueCountMap.put(values.split("/")[0], valueCountMap.get(values.split("/")[0]) + 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return valueCountMap;
    }

    public static List<List<String>> readCsvFilefromData(String filePath) {
        List<List<String>> RelatedMethodSetList = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(Paths.get(filePath));
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            Set<String> typeSet = new HashSet<>();
            for (CSVRecord record : parser) {
                String changeSetTriple = record.isMapped("RelatedMethodSet") ? record.get("RelatedMethodSet") : "N/A";
//                System.out.println(changeSetTriple);
                RelatedMethodSetList.add(extractMethods(changeSetTriple));
//                System.out.println(extractMethods(changeSetTriple).toString());
            }
            RelatedMethodSetList.forEach(System.out::println);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return RelatedMethodSetList;
    }

    public static List<List<String>> readCsvFileFromResult(String filePath) {
        List<List<String>> changeSetTripleList = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(Paths.get(filePath));
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            Set<String> typeSet = new HashSet<>();
            for (CSVRecord record : parser) {
                String changeSetTriple = record.isMapped("changeSet-triple") ? record.get("changeSet-triple") : "N/A";
                changeSetTripleList.add(List.of(changeSetTriple.replace("[", "").replace("]", "").split(", ")));
            }
            // 打印读取的数据
            System.out.println("Noise Types:");
//            noiseTypeList.forEach(System.out::println);
            typeSet.forEach(System.out::println);
//            System.out.println("\nChange Set Triples:");
            changeSetTripleList.forEach(System.out::println);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return changeSetTripleList;
    }

    public static void readCsvFile(String filePath) {
        List<String> noiseTypeList = new ArrayList<>();
        List<String> changeSetTripleList = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(Paths.get(filePath));
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            Set<String> typeSet = new HashSet<>();
            for (CSVRecord record : parser) {
                String noiseType = record.isMapped("noiseType") ? record.get("noiseType") : "N/A";
                String tmp = noiseType.replace("[", "").replace("]", "");
                if (!tmp.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    String[] arr = tmp.split(", ");
                    if (arr.length < 1) {
                        noiseTypeList.add("");
                        typeSet.add("");
                    } else if (arr.length == 1) {
                        if (arr[0].split("\\|").length == 2) {
                            noiseTypeList.add(arr[0].split("\\|")[1]);
                            typeSet.add(arr[0].split("\\|")[1]);
                        } else {
                            noiseTypeList.add(arr[0]);
                            typeSet.add(arr[0]);
                        }

                    } else if (arr.length > 6) {
                        noiseTypeList.add("MAY_OVERRIDE_DIFFERENCE");
                        typeSet.add("MAY_OVERRIDE_DIFFERENCE");
                    } else {
                        for (String s : arr) {
                            if (s.split("\\|").length < 2) {
                                sb.append(s).append(",");
                                typeSet.add(s);
                            } else {
                                sb.append(s.split("\\|")[1]).append(",");
                                typeSet.add(s.split("\\|")[1]);
                            }
                        }

                        noiseTypeList.add(sb.toString().substring(0, sb.length() - 1));
                    }
                }


                String changeSetTriple = record.isMapped("changeSet-triple") ? record.get("changeSet-triple") : "N/A";
                changeSetTripleList.add(changeSetTriple);
            }
            ;
            // 打印读取的数据
            System.out.println("Noise Types:");
//            noiseTypeList.forEach(System.out::println);
            typeSet.forEach(System.out::println);
//            System.out.println("\nChange Set Triples:");
            changeSetTripleList.forEach(System.out::println);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> ignoreList = new ArrayList<>();
    public static void prepareData() {
        // 预期数据
        Map<String,List<Data>> dataMap =  prepareData1();
        int m = 0;
        Map<String,Project> projectMap = new HashMap<>();
        try (Reader reader = Files.newBufferedReader(Paths.get("D:\\JavaProject\\SPAT\\trans-commits\\result-20240920-3.csv"));
            CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : parser) {
                // 本次变更影响的方法集
                String changeSetTriple = record.isMapped("changeSet-triple") ? record.get("changeSet-triple") : "N/A";
                String start_end_SHA = record.isMapped("start_end_SHA") ? record.get("start_end_SHA") : "N/A";
                String project_name = record.isMapped("project_name") ? record.get("project_name") : "N/A";

                List<String> start_end_SHA_List = List.of(start_end_SHA.replace("[", "").replace("]", "").split(", "));

                Project currrentProject = new Project();
                if(!projectMap.containsKey(project_name)){
                    currrentProject = MockTestCase.mockTestCase(project_name);
                    projectMap.put(project_name,currrentProject);
                }else {
                    currrentProject = projectMap.get(project_name);
                }
                Map<String, Integer> expandClassMapTestCases = currrentProject.flattenMethodInfoMap(project_name);

                String project = project_name.substring(project_name.lastIndexOf("\\")+1);
                if(dataMap.containsKey(project)){
                    List<String> dataList = new ArrayList<>();
                    int impactCaseNum = 0;
                    int count = 0;
                    // n次模拟一次
                    if(start_end_SHA_List.size()%2==0){
                        List<String> tmpList = new ArrayList<>();
                        for(int i=0;i<start_end_SHA_List.size();i+=2){
                            String tmpSHA = start_end_SHA_List.get(i)+", "+start_end_SHA_List.get(i+1);
                            if(ignoreList.contains(tmpSHA)){
                                continue;
                            }
                            for (Data data:dataMap.get(project)){
                                if(data.SHA.equals(tmpSHA)){
                                    // 避免重复加入,因为可能转换同一个方法
                                    if(!tmpList.contains(data.RelatedMethodSet)){
                                        tmpList.add(data.RelatedMethodSet);
                                        List<String> tmp = extractMethods(data.RelatedMethodSet);
//                                    if(tmp.size()<10){
                                        dataList.addAll(tmp);
//                                    }
                                        for (String s:tmp){
                                            impactCaseNum += Integer.parseInt(data.RelatedCasesNum);
                                        }

                                    }
                                }
                            }
                        }
                        boolean flag = cmpChangeSetWithExpectAndResult(dataList,List.of(changeSetTriple.replace("[", "").replace("]", "").split(", ")));
                        if(flag){
                            for (String s:changeSetTriple.replace("[","").replace("]","").split(", ")){
                                if(s==null||s.isEmpty()){
                                    count+=0;
                                }else {
                                    if(expandClassMapTestCases.containsKey(s)){
                                        int num = expandClassMapTestCases.get(s);
                                        count += num;
                                    }
                                }
                            }
                            System.out.println("噪声变更方法影响用例数量: "+count);
                            System.out.println("预期影响用例数量: "+impactCaseNum);

                        }
                        if(count>=impactCaseNum){
                            m++;
                        }
                    }
                    System.out.println(m);
                }
            }
            System.out.println(a);
            System.out.println(b);
            // 打印读取的数据

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void cmpFP(){
        int FP = 0; // 假阳性（误报）
        int TP = 0; // 真阳性
        int FN = 0; // 假阴性（漏报）
        int TN = 0; // 真阴性
        Map<String,List<Data>> dataMap =  prepareData1();
        List<String> noiseTypeList = new ArrayList<>();
        Map<String,Project> projectMap = new HashMap<>();
        try (Reader reader = Files.newBufferedReader(Paths.get("D:\\JavaProject\\SPAT\\trans-commits\\result-20240920-3.csv"));
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : parser) {
                // 本次变更影响的方法集
                String noiseType = record.isMapped("noiseType") ? record.get("noiseType") : "N/A";
                String start_end_SHA = record.isMapped("start_end_SHA") ? record.get("start_end_SHA") : "N/A";
                String project_name = record.isMapped("project_name") ? record.get("project_name") : "N/A";

                List<String> start_end_SHA_List = List.of(start_end_SHA.replace("[", "").replace("]", "").split(", "));
                Set<String> typeSet = new HashSet<>();
                Project currrentProject = new Project();
                if(!projectMap.containsKey(project_name)){
                    currrentProject = MockTestCase.mockTestCase(project_name);
                    projectMap.put(project_name,currrentProject);
                }else {
                    currrentProject = projectMap.get(project_name);
                }
                Map<String, Integer> expandClassMapTestCases = currrentProject.flattenMethodInfoMap(project_name);

                String project = project_name.substring(project_name.lastIndexOf("\\")+1);
                if(dataMap.containsKey(project)){
                    List<String> dataList = new ArrayList<>();
                    int impactCaseNum = 0;
                    int count = 0;
                    // n次模拟一次
                    if(start_end_SHA_List.size()%2==0){
                        for(int i=0;i<start_end_SHA_List.size();i+=2){
                            String tmpSHA = start_end_SHA_List.get(i)+", "+start_end_SHA_List.get(i+1);
//                            if(ignoreList.contains(tmpSHA)){
//                                continue;
//                            }
                            for (Data data:dataMap.get(project)){
                                if(data.SHA.equals(tmpSHA)){
//                                    if(tmp.size()<10){
                                        dataList.add(data.type);
//                                    }
                                }
                            }
                        }
                        // 获取结果识别出来的类型
                        String tmp = noiseType.replace("[", "").replace("]", "");
                        if (!tmp.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            String[] arr = tmp.split(", ");
                            if (arr.length < 1) {
                                noiseTypeList.add("");
                                typeSet.add("");
                            } else if (arr.length == 1) {
                                if (arr[0].split("\\|").length == 2) {
                                    noiseTypeList.add(arr[0].split("\\|")[1]);
                                    typeSet.add(arr[0].split("\\|")[1]);
                                } else {
                                    noiseTypeList.add(arr[0]);
                                    typeSet.add(arr[0]);
                                }

                            } else if (arr.length > 6) {
                                noiseTypeList.add("MAY_OVERRIDE_DIFFERENCE");
                                typeSet.add("MAY_OVERRIDE_DIFFERENCE");
                            } else {
                                for (String s : arr) {
                                    if (s.split("\\|").length < 2) {
                                        sb.append(s).append(",");
                                        typeSet.add(s);
                                    } else {
                                        sb.append(s.split("\\|")[1]).append(",");
                                        typeSet.add(s.split("\\|")[1]);
                                    }
                                }

                                noiseTypeList.add(sb.toString().substring(0, sb.length() - 1));
                            }
                        }
                        boolean flag = true;
                        for(String type:noiseTypeList){
                            if(map.containsKey(type)){
                                type = map.get(type);
                            }
                            flag = flag && dataList.contains(type);
                        }
                        if(flag){
                            TP ++;
                        }else {
                            FP ++;
                        }
                    }

                }
            }
            System.out.println("TP:"+TP);
            System.out.println("FP:"+FP);
            // 打印读取的数据

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String,List<Data>> prepareData1() {
        Map<String,List<Data>> commitsMap = new HashMap<>();
        try (Reader reader = Files.newBufferedReader
                (Paths.get("D:\\JavaProject\\SPAT\\trans-commits\\trans-commits-datasets20240920.csv"));
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : parser) {
                String projectName = record.isMapped("projectName") ? record.get("projectName") : "N/A";
                String startCommit = record.isMapped("startCommit") ? record.get("startCommit") : "N/A";
                String endCommit = record.isMapped("endCommit") ? record.get("endCommit") : "N/A";
                String transformType = record.isMapped("transformType") ? record.get("transformType") : "N/A";
                String RelatedMethodSet = record.isMapped("RelatedMethodSet") ? record.get("RelatedMethodSet") : "N/A";
                String RelatedCasesNum = record.isMapped("RelatedCasesNum") ? record.get("RelatedCasesNum") : "N/A";
//                System.out.println(projectName.split("/")[0]);
//                System.out.println(startCommit+", "+endCommit);
                if(RelatedMethodSet.replace("[", "").replace("]", "").split(", ").length >6){
                    ignoreList.add(startCommit+", "+endCommit);
                }
                Data data = new Data(projectName.split("/")[0],startCommit+", "+endCommit,
                        transformType,RelatedMethodSet,RelatedCasesNum);
                if(commitsMap.containsKey(projectName.split("/")[0])){
                    commitsMap.get(projectName.split("/")[0].split("/")[0])
                                .add(data);
                }else{
                    List<Data> commits = new ArrayList<>();
                    commits.add(data);
                    commitsMap.put(projectName.split("/")[0],commits);
                }
            }
//            commitsMap.forEach((k,v)->{
//                System.out.println(k+" :\n "+v);
//            });
            // 打印读取的数据

        } catch (Exception e) {
            e.printStackTrace();
        }
        return commitsMap;
    }

    public static class Data{
        public Data(String projectName,String SHA,String type,String RelatedMethodSet,String RelatedCasesNum){
            this.projectName = projectName;
            this.SHA = SHA;
            this.type = type;
            this.RelatedMethodSet = RelatedMethodSet;
            this.RelatedCasesNum = RelatedCasesNum;
        }
        String projectName;
        String SHA;
        String type;
        String RelatedMethodSet;
        String RelatedCasesNum;

        public String toString(){
            return projectName+", "+SHA+", "+type+", "+RelatedMethodSet+", "+RelatedCasesNum;
        }
    }


    public static Map<String, String> map = new HashMap<>();
    public static void main(String[] args) {
        map.put("RENAME_ATTRIBUTE","Local_Var_Renaming".toUpperCase());
        map.put("RENAME_PARAMETER","Local_Var_Renaming".toUpperCase());
        map.put("RENAME_VARIABLE","Local_Var_Renaming".toUpperCase());
        map.put("RENAME_CLASS","Local_Var_Renaming".toUpperCase());
        map.put("INLINE_VARIABLE","Var_Declaration_Merging".toUpperCase());
        map.put("EXTRACT_METHOD","Var_Declaration_Dividing".toUpperCase());
        map.put("EXTRACT_VARIABLE","ADD_ASSIGNMENT_TO_EQUAL_ASSIGNMENT".toUpperCase());// netty -d9f504587398e4652c925b8a2dbb50783ff9e5e5
        map.put("SPLIT_CONDITIONAL","Conditional_Exp_To_Single_IF".toUpperCase());
        map.put("MERGE_CONDITIONAL","Conditional_Exp_To_Single_IF".toUpperCase());
//        countDetectResult();
//        cmp();
//        prepareData1();
//        prepareData();
        cmpFP();
    }


    // 计算准确率
    public static double calculateAccuracy(int truePositives, int trueNegatives, int falsePositives, int falseNegatives) {
        int total = truePositives + trueNegatives + falsePositives + falseNegatives;
        if (total == 0) {
            return 0; // 防止除以零
        }
        return (double) (truePositives + trueNegatives) / total;
    }

    // 计算召回率
    public static double calculateRecall(int truePositives, int falseNegatives) {
        int totalRelevant = truePositives + falseNegatives;
        if (totalRelevant == 0) {
            return 0; // 防止除以零
        }
        return (double) truePositives / totalRelevant;
    }
}

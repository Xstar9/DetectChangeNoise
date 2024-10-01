import cn.hutool.core.io.FileUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SomeTset {
    @Test
    public void test() {
//        readCsvFileFromResult("D:\\JavaProject\\SPAT\\trans-commits\\trans-commits-datasets20240920.csv");
        FileUtil.del("D:\\JavaProject\\MyIdea\\DetectChangeNoise\\ddd");
    }

    public static List<List<String>> readCsvFileFromResult(String filePath) {
        List<List<String>> changeSetTripleList = new ArrayList<>();
        int i = 0;
        int sum = 0;
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath));
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            Set<String> typeSet = new HashSet<>();
            for (CSVRecord record : parser) {
                sum++;
                String changeSetTriple = record.isMapped("RelatedMethodSet") ? record.get("RelatedMethodSet")  : "N/A";
                if(changeSetTriple.replace("[", "").replace("]", "").split(", ").length >6){
                    i++;
                }
                System.out.println(changeSetTriple.replace("[", "").replace("]", "").split(", ").length);
                String CaseNums = record.isMapped("RelatedMethodSet") ? record.get("RelatedMethodSet")  : "N/A";
                if(changeSetTriple.replace("[", "").replace("]", "").split(", ").length >6){
                    i++;
                }
                System.out.println(changeSetTriple.replace("[", "").replace("]", "").split(", ").length);
            }
            System.out.println(sum);
            System.out.println(i);
//            // 打印读取的数据
//            System.out.println("Noise Types:");
////            noiseTypeList.forEach(System.out::println);
//            typeSet.forEach(System.out::println);
////            System.out.println("\nChange Set Triples:");
//            changeSetTripleList.forEach(System.out::println);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return changeSetTripleList;
    }


}

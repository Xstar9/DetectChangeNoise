package clean;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import gr.uom.java.xmi.diff.CodeRange;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * testRefactoringMiner 重构检测工具常用API操作
 *
 * @author Z.X
 * @since 2023-12-18
 */
@Slf4j
public class testRefactoringMiner {

    public static void main(String[] args) throws Exception {

//        String startCommit = "9a5c33b16d07d62651ea80552e8782974c96bb8a";
//        String endCommit = "d4bce13a443cf12da40a77c16c1e591f4f985b47";
        String Commit2 = "1fbc115d1e94ae9d08dd933ddbe841cfb56f48e8";
        String Commit1= "5c98bbc738989bc1b12a7cce009e9fc4f707a495";
        String startCommit1 = "9f03084f68b770025bc578bcbd81363515b3cf0d";
        String endCommit1 = "b1f276d5dd79192054b718e0d4fc9f749ec30de7";
        String startTag = "1.0";
        String endTag = "1.1";
        String atCommitSHA = "05c1e773878bbacae64112f70964f4f2f7944398";
        String startCommit= "d4bce13a443cf12da40a77c16c1e591f4f985b47";
        String endCommit = "9a5c33b16d07d62651ea80552e8782974c96bb8a";

        String tmp_path = "D:\\JavaProject\\ForMyIdea\\homework\\tmp\\refactoring-toy-example";
        String gitUrl = "https://gitee.com/star-zheng/refactoring-toy-example.git";

        GitService gitService = new GitServiceImpl();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
        // 下载到本地比较
        Repository repo = gitService.cloneIfNotExists(tmp_path, gitUrl);
        log.info("Download complete, next Step");

//        testRefactoringMiner test = new testRefactoringMiner();
//        test.detectBetweenCommits(repo,Commit1,Commit2, miner);
        miner.detectBetweenCommits(repo,  endCommit,startCommit, new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                log.info("Refactorings at {}", commitId);
                for (Refactoring ref : refactorings) {
                    log.info("{} : {}", ref.getRefactoringType().toString(), ref.rightSide().toString());
                    for(CodeRange codeRange:ref.rightSide()){
                        log.info("{}, {}, {}", codeRange.getStartLine(), codeRange.getEndLine(), codeRange.getCodeElement());
                    }
                }
            }
        });
    }

    public void detectAll(Repository repo,  GitHistoryRefactoringMiner miner) throws Exception {
        /**
         *  比较整个项目的重构行为 master/origin
         **/
        List<String> list = new ArrayList<>();
        miner.detectAll(repo, "master", new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                log.info("Refactorings at {}", commitId);
                if(refactorings.isEmpty()) {
                    list.add(commitId + "\t " + "\t "  +"\t " + "");
                }
                else{
                    for (Refactoring ref : refactorings) {
//                            log.info(ref.toString());
                        list.add(commitId+"\t" + ref +"\t"+ref.getRefactoringType());

                    }
                }
            }
        });
        try {
            write2csv(list, "All");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void detectAtCommit(Repository repo, String commitSHA, GitHistoryRefactoringMiner miner){
        /**
         *  05c1e773878bbacae64112f70964f4f2f7944398
         *  指定commitSHA的重构行为
         * */
        List<String> list = new ArrayList<>();
        miner.detectAtCommit(repo, commitSHA, new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                log.info("Refactorings at {}", commitId);
                if(refactorings.isEmpty()) {
                    list.add(commitId + "\t " + "\t "  +"\t " + "");
                }
                else{
                    for (Refactoring ref : refactorings) {
//                            log.info(ref.toString());
                        list.add(commitId+"\t" + ref +"\t"+ref.getRefactoringType());
                    }
                }
            }
        });
        try {
            write2csv(list, "AtCommit");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void detectBetweenTags(Repository repo, String versionTag1, String versionTag2, GitHistoryRefactoringMiner miner) throws Exception {
        /**
         * start tag: 1.0
         *  end tag: 1.1
         *  给定两个版本之间的相互比较
         * */
        List<String> list = new ArrayList<>();
        miner.detectBetweenTags(repo, versionTag1, versionTag2, new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                log.info("Refactorings at " + commitId);
                if(refactorings.size() == 0) {
                    list.add(commitId + "\t " + "\t "  +"\t " + "");
                }
                else{
                    for (Refactoring ref : refactorings) {
//                            log.info(ref.toString());
                        list.add(commitId+"\t" + ref +"\t"+ref.getRefactoringType());
                    }
                }
            }
        });
        try {
            write2csv(list, "BetweenTags");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void detectBetweenCommits(Repository repo, String startCommit, String endCommit, GitHistoryRefactoringMiner miner) throws Exception {
        /**
         * start commit: 819b202bfb09d4142dece04d4039f1708735019b
         * end commit: d4bce13a443cf12da40a77c16c1e591f4f985b47
         * 遍历从开始提交/标记到结束提交/标记的所有非合并提交:  即给定两个提交之间的相互比较
         **/
        List<String> list = new ArrayList<>();
        miner.detectBetweenCommits(repo, startCommit, endCommit, new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                log.info("Refactorings at " + commitId);
                if(refactorings.size() == 0) {
                    list.add(commitId + "\t " + "\t "  +"\t " + "");
                }
                else{
                    for (Refactoring ref : refactorings) {
//                            log.info(ref.toString());
                        list.add(commitId+"\t" + ref +"\t"+ref.getRefactoringType());
                    }
                }
            }
        });
        try {
            write2csv(list, "BetweenCommits");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readCSV(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();

            for (String[] record : records) {
                for (String field : record) {
                    System.out.print(field + " ");
                }
                System.out.println();
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public void write2csv(List<String> result,String mode) throws IOException {

        String path = "./output/detectResult4" + mode + "-" + getTime() + ".csv";
        try{
            FileWriter fileWriter = new FileWriter(path, true);
            CSVWriter writer = new CSVWriter(fileWriter);
            String[] headers = {"CommitId", "RefactorAction", "Description", "Type"};
            writer.writeNext(headers);
            for(String res : result) {
                log.info(res);
                String[] resList = res.split("\t");
                writer.writeNext(resList);
            }
            writer.close();
            fileWriter.close();
        }catch (FileNotFoundException e){
            log.info("没有找到指定文件");
        }catch (IOException e) {
            log.info("文件读写出错");
        }
    }

    public String getTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        return sdf.format(date);
    }
}

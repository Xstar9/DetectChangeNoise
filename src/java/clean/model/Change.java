package clean.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class Change {
    /**
     * commitId1_commitId2 或 dir1_dir2 的形式
     * 变更形式：commit 、 目录（文件）
     */
    public String cmpId;

    /**
     * 触发噪声的类文件路径
     */
    public String url;

    public List<ChangeNoise> changeNoiseList = new ArrayList<>();

}

package clean.model;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChangeNoise {

    public String typeId;

    public String methodLocation;

//    public String methodName;

    public List<Integer> oldRelatedLines;

    public List<Integer> newRelatedLines;

    public List<String> originalContent;

    public List<String> modifiedContent;

    public boolean isNoiseMethod;

}

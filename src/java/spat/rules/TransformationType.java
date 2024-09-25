package spat.rules;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum TransformationType {
    LOCAL_VAR_RENAMING(0),
    FOR_TO_WHILE(1),
    WHILE_TO_FOR(2),
    REVERSE_IF_ELSE(3),
    SINGLE_IF_TO_CONDITIONAL_EXP(4),
    CONDITIONAL_EXP_TO_SINGLE_IF(5),
    PP_TO_ADD_ASSIGNMENT(6),
    ADD_ASSIGNMENT_TO_EQUAL_ASSIGNMENT(7),
    INFIX_EXPRESSION_DIVIDING(8),
    IF_DIVIDING(9),
    STATEMENTS_ORDER_REARRANGEMENT(10),
    LOOP_IF_CONTINUE_TO_ELSE(11),
    VAR_DECLARATION_MERGING(12),
    VAR_DECLARATION_DIVIDING(13),
    SWITCH_EQUAL_SIDES(14),
    SWITCH_STRING_EQUAL(15),
    PRE_POST_FIX_EXPRESSION_DIVIDING(16),
    CASE_TO_IF_ELSE(17),
    CASE_TO_FOR_FOREACH(18),

    // Wrong transformations
    REVERSE_IF_ELSE_WRONGLY(-3),
    FOR_TO_WHILE_WRONGLY(-1),
    STATEMENTS_ORDER_REARRANGEMENT_WRONGLY(-10),
    LOCAL_VAR_RENAMING_WRONGLY(-99);

    private final int code;

    TransformationType(int code) {
        this.code = code;
    }

    // Static map to hold the relationship between code and enum
    private static final Map<Integer, TransformationType> map = new HashMap<>();

    static {
        for (TransformationType type : TransformationType.values()) {
            map.put(type.getCode(), type);
        }
    }

    public static String fromCode(int code) {
        return map.get(code).toString();
    }
}

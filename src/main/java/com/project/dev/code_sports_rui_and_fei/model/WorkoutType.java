package com.project.dev.code_sports_rui_and_fei.model;

/**
 * 运动类型枚举
 */
public enum WorkoutType {
    RUNNING("跑步"),
    STRENGTH("力量训练"),
    YOGA("瑜伽"),
    SWIMMING("游泳"),
    CYCLING("骑行"),
    HIIT("HIIT高强度"),
    WALKING("健走"),
    DANCE("舞蹈"),
    BADMINTON("羽毛球"),
    BASKETBALL("篮球"),
    OTHER("其他");

    private final String displayName;

    WorkoutType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

package com.ck.enums;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 码表
 */
@Data
@Accessors(chain = true)
public class DictionaryData implements Serializable {

    private static final long serialVersionUID = 5999791587974140346L;

    public DictionaryData(String code, String name) {
        this.code = code;
        this.name = name;
    }

    private String code;                                //资源代码

    private String name;                                //资源名称

    private String level;

    private String parentCode;                          //父级code

    private List<DictionaryData> children;              //关联子资源列表

    public DictionaryData() {
        super();
    }

    public void addChild(DictionaryData dictionaryData) {
        if (dictionaryData == null) {
            return;
        }
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(dictionaryData);
    }

}

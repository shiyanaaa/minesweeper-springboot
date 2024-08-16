package com.minesweeper.Domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Set;

@Data
public class Cell {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    private Integer open;
    private Integer row;
    private Integer col;
    private Integer value;
    private String ids;
    @TableField(exist = false)
    private Set<String> idsSet;
}

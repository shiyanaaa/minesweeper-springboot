package com.minesweeper.Domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Cell {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    private boolean open;
    private Integer row;
    private Integer col;
    private Integer value;
}

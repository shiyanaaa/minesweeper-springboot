package com.minesweeper.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.minesweeper.Domain.Cell;

import java.util.List;

public interface CellService extends IService<Cell> {
    void removeAll();
    boolean init(Integer row, Integer col);

    List<Cell> region(Integer startX, Integer startY, Integer rowNum, Integer colNum);


    boolean updateOpen(String id, Integer set, Integer get);
}

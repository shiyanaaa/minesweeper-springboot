package com.minesweeper.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minesweeper.Domain.Cell;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CellMapper extends BaseMapper<Cell> {
}

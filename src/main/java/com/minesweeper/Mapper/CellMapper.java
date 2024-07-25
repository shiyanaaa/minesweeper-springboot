package com.minesweeper.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minesweeper.Domain.Cell;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CellMapper extends BaseMapper<Cell> {
    @Insert({
            "<script>",
            "insert",
            "into cell(id,row,col,open,value) values",
            "<foreach collection='list' item='item' separator=',' >",
            "(#{item.id},#{item.row},#{item.col},#{item.open},#{item.value})",
            "</foreach>",
            "</script>"
    })
    void insertBatch(@Param("list") List<Cell> list);
}

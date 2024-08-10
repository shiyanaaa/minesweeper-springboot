package com.minesweeper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.minesweeper.Domain.Cell;
import com.minesweeper.Mapper.CellMapper;
import com.minesweeper.Service.CellService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MinesweeperSpringbootApplicationTests {
    @Resource
    private CellMapper cellMapper;
    @Test
    void contextLoads() {
        QueryWrapper<Cell> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.select("max(row)");

        Cell entity = cellMapper.selectOne(queryWrapper1 );
    }

}

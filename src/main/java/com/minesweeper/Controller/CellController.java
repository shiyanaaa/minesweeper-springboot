package com.minesweeper.Controller;

import com.minesweeper.Domain.Cell;
import com.minesweeper.Domain.Res;
import com.minesweeper.Enum.CellOpenEnum;
import com.minesweeper.Service.CellService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CellController {
    @Resource
    private CellService cellService;

    @GetMapping("/init")
    public Res init() {
        cellService.removeAll();

        return cellService.init(1000, 1000) ? Res.success() : Res.fail();
    }

    @GetMapping("/region/{startX}-{startY}/{rowNum}-{colNum}")
    public Res region(@PathVariable Integer colNum, @PathVariable Integer rowNum, @PathVariable Integer startX, @PathVariable Integer startY) {
        List<Cell> cells = cellService.region(startX, startY, rowNum, colNum);

        return cells.size() > 0 ? Res.success(cells) : Res.fail();
    }

    @PostMapping("/open/{id}")
    public Res open(@PathVariable String id) {


        return cellService.updateOpen(id, CellOpenEnum.OPEN, CellOpenEnum.CLOSE) ? Res.success() : Res.fail("更新失败",cellService.getById(id));
    }

    @PostMapping("/flag/{id}")
    public Res flag(@PathVariable String id) {
        return cellService.updateOpen(id, CellOpenEnum.Flag, CellOpenEnum.CLOSE) ? Res.success() : Res.fail("更新失败",cellService.getById(id));
    }

    @PostMapping("/cancel_flag/{id}")
    public Res cancelFlag(@PathVariable String id) {
        return cellService.updateOpen(id, CellOpenEnum.CLOSE, CellOpenEnum.Flag) ? Res.success() : Res.fail("更新失败",cellService.getById(id));

    }

}

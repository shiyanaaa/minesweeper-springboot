package com.minesweeper.Service.impl;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minesweeper.Controller.EchoChannel;
import com.minesweeper.Domain.Cell;
import com.minesweeper.Enum.CellOpenEnum;
import com.minesweeper.Enum.CellValueEnum;
import com.minesweeper.Mapper.CellMapper;
import com.minesweeper.Service.CellService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;

import java.util.*;

@Service
public class CellServiceImpl extends ServiceImpl<CellMapper, Cell> implements CellService {
    private final HashSet<String> openIdList = new HashSet<>();

    @Override
    public void removeAll() {
        super.remove(null);
    }

    @Resource
    EchoChannel echoChannel;
    @Resource
    CellMapper cellMapper;

    @Override
    public boolean init(Integer row, Integer col) {
        List<List<Cell>> cells = new ArrayList<>();
        for (int i = 0; i < row; i++) {
            List<Cell> cellList = new ArrayList<>();
            for (int j = 0; j < col; j++) {
                Cell cell = new Cell();
                cell.setId(IdWorker.getIdStr());
                cell.setCol(j);
                cell.setRow(i);
                cell.setValue(CellValueEnum.Empty);
                cell.setOpen(CellOpenEnum.CLOSE);
                cell.setId(Long.toString(IdWorker.getId(cell)));
                cellList.add(cell);
                cell.setIdsSet(new HashSet<>());
            }
            cells.add(cellList);
        }
        this.bomb(cells, row, col, row * col / 5);
        this.retrieval(cells);
        this.setIdsSet(cells);

        List<Cell> allList = this.flatten(cells);
        this.setIds(allList);
        List<List<Cell>> partition = ListUtil.partition(allList, 10000);
        partition.forEach(list -> cellMapper.insertBatch(list));
        return true;

    }

    private void setIds(List<Cell> allList) {
        for (Cell cell : allList) {
            cell.setIds(String.join(",", cell.getIdsSet()));
        }
    }

    private void setIdsSet(List<List<Cell>> cells) {
        for(int i=0;i<cells.size();i++){
            for(int j=0;j<cells.get(i).size();j++){
                if(cells.get(i).get(j).getValue()==0)
                    findAdjacentZeros(cells,i,j);
            }
        }
    }

    private void findAdjacentZeros(List<List<Cell>> cells,int row,int col) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (newRow >= 0 && newRow < cells.size() && newCol >= 0 && newCol < cells.get(0).size() && cells.get(newRow).get(newCol).getValue()== 0) {
                Set<String> idsSet = cells.get(row).get(col).getIdsSet();
                idsSet.add(cells.get(newRow).get(newCol).getId());
                idsSet.addAll(cells.get(newRow).get(newCol).getIdsSet());
                cells.get(row).get(col).setIdsSet(idsSet);
            }
        }
    }

    @Override
    public List<Cell> region(Integer startX, Integer startY, Integer rowNum, Integer colNum) {
        QueryWrapper<Cell> wrapper = new QueryWrapper<>();
        wrapper.between("row", startX, startX + rowNum);
        wrapper.between("col", startY, startY + colNum);
        return super.list(wrapper);

    }


    @Override
    public boolean updateOpen(String id, Integer set, Integer get, boolean isRoot) {
        QueryWrapper<Cell> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        queryWrapper.eq("open", get);
        Cell cell=super.getOne(queryWrapper);
        if(cell==null) return false;
        if(cell.getValue()!=0){
            UpdateWrapper<Cell> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id);
            updateWrapper.set("open", set);
            return super.update(updateWrapper);
        }
        UpdateWrapper<Cell> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", Arrays.asList(cell.getIds().split(",")));
        updateWrapper.set("open", set);

        return super.update(updateWrapper);
    }

    //    连锁开启，当当前格子value为0是，需要开启周围的格子
    private void chainOpen(Cell cell) {
        if (cell.getValue() != 0) return;
        int r = cell.getRow();
        int c = cell.getCol();
        QueryWrapper<Cell> wrapper = new QueryWrapper<>();
        wrapper.between("row", r - 1, r + 1);
        wrapper.between("col", c - 1, c + 1);
        wrapper.ne("id", cell.getId());
        wrapper.eq("open", CellOpenEnum.CLOSE);
        List<Cell> list = super.list(wrapper);
        List<Cell> openList = new ArrayList<>();
        list.forEach(cellItem -> {
            if (cellItem.getValue() == 0) {
                openList.add(cellItem);
            } else {
                openIdList.add(cellItem.getId());
            }
        });


        openList.forEach(cellItem -> {
            if (!openIdList.contains(cellItem.getId()))
                updateOpen(cellItem.getId(), CellOpenEnum.OPEN, CellOpenEnum.CLOSE, false);
        });
    }

    private void bomb(List<List<Cell>> cells, Integer row, Integer col, Integer num) {
        while (num > 0) {
            int r = (int) (Math.random() * row);
            int c = (int) (Math.random() * col);
            if (cells.get(r).get(c).getValue() == 0) {
                cells.get(r).get(c).setValue(-1);
                num--;
            }
        }
    }

    private void retrieval(List<List<Cell>> cells) {
//    对空白单元格周围的单元格进行遍历，周围有几颗炸弹，赋值value为几
        for (int i = 0; i < cells.size(); i++) {
            for (int j = 0; j < cells.get(i).size(); j++) {
                if (cells.get(i).get(j).getValue() == 0) {
                    int count = 0;
                    for (int row = i - 1; row <= i + 1; row++) {
                        for (int col = j - 1; col <= j + 1; col++) {
                            if (row >= 0 && row < cells.size() && col >= 0 && col < cells.get(i).size()) {
                                if (cells.get(row).get(col).getValue() == -1) {
                                    count++;
                                }
                            }
                        }
                    }
                    cells.get(i).get(j).setValue(count);
                }

            }
        }

    }

    //    展开List<List<Cell>>为List<Cell>
    private List<Cell> flatten(List<List<Cell>> cells) {
        List<Cell> cellList = new ArrayList<>();
        for (List<Cell> cell : cells) {
            cellList.addAll(cell);
        }
        return cellList;
    }

}

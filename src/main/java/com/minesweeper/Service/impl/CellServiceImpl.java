package com.minesweeper.Service.impl;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
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
                cellList.add(cell);
            }
            cells.add(cellList);
        }
        this.bomb(cells, row, col, row * col / 5);
        this.retrieval(cells);
        List<Cell> allList = this.flatten(cells);
        List<List<Cell>> partition = ListUtil.partition(allList, 10000);
        partition.forEach(list -> cellMapper.insertBatch(list));
        return true;

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

        if(isRoot)
            openIdList.clear();
        if(openIdList.contains(id)) return false;
        QueryWrapper<Cell> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        queryWrapper.eq("open", get);
        Cell cell;
        try {
            cell = super.getOne(queryWrapper);
            if(cell==null) return true;
        }catch (StackOverflowError e){
            System.out.println("StackOverflowError");
            System.out.println(id);
            return false;
        }

        openIdList.add(id);
        if(!Objects.isNull(cell)&& Objects.equals(set, CellOpenEnum.OPEN)&& cell.getValue() == 0)
            chainOpen(cell);

        if(openIdList.size()!=0&&isRoot){
            UpdateWrapper<Cell> updateWrapper = new UpdateWrapper<>();
            updateWrapper.in("id", openIdList);
            updateWrapper.set("open", set);
            Map<String, Object> map = new HashMap<>();
            if(openIdList.size()==1){
                map.put("startRow", cell.getRow());
                map.put("startCol", cell.getCol());
                map.put("endRow", cell.getRow());
                map.put("endCol", cell.getCol());
            }else{
                QueryWrapper<Cell> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.in("id", openIdList);
                queryWrapper1.select("min(row) as row","min(col) as col");
                Cell min= super.getOne(queryWrapper1);
                map.put("startRow", min.getRow());
                map.put("startCol", min.getCol());
                QueryWrapper<Cell> queryWrapper2 = new QueryWrapper<>();
                queryWrapper2.in("id", openIdList);
                queryWrapper2.select("max(row) as row","max(col) as col");
                Cell max= super.getOne(queryWrapper2);
                map.put("endRow", max.getRow());
                map.put("endCol", max.getCol());
            }
            echoChannel.sendtoAll(JSON.toJSONString(map));
            return super.update(updateWrapper);
        }

        return false;
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
            if(!openIdList.contains(cellItem.getId()))
                updateOpen(cellItem.getId(), CellOpenEnum.OPEN, CellOpenEnum.CLOSE,false);
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

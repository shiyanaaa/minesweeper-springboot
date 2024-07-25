package com.minesweeper.Service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minesweeper.Domain.Cell;
import com.minesweeper.Mapper.CellMapper;
import com.minesweeper.Service.CellService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class CellServiceImpl extends ServiceImpl<CellMapper, Cell> implements CellService {

    @Override
    public void removeAll() {
        super.remove(null);
    }

    @Override
    public boolean init(Integer row, Integer col) {
//        创建row*col个cell
        List<List<Cell>> cells = new ArrayList<>();
        for (int i = 0; i < row; i++) {
            List<Cell> cellList = new ArrayList<>();
            for (int j = 0; j < col; j++) {
                Cell cell=new Cell();
                cell.setCol(j);
                cell.setRow(i);
                cell.setValue(0);
                cell.setOpen(false);
                cellList.add(cell);
            }
            cells.add(cellList);
        }
        this.bomb(cells, row, col, row * col / 10);
        this.retrieval(cells);
        Collection<Cell> allList= this.flatten(cells);
        return super.saveBatch(allList);

    }

    @Override
    public List<Cell> region(Integer startX, Integer startY, Integer rowNum, Integer colNum) {
        QueryWrapper<Cell> wrapper=new QueryWrapper<>();
        wrapper.between("row",startX,startX+rowNum);
        wrapper.between("col",startY,startY+colNum);
        return super.list(wrapper);

    }

    private void bomb(List<List<Cell>> cells, Integer row, Integer col, Integer num){
        while(num>0){
            int r=(int)(Math.random()*row);
            int c=(int)(Math.random()*col);
            if(cells.get(r).get(c).getValue()==0){
                cells.get(r).get(c).setValue(-1);
                num--;
            }
        }
    }

    private void retrieval(List<List<Cell>> cells){
//    对空白单元格周围的单元格进行遍历，周围有几颗炸弹，赋值value为几
        for(int i=0;i<cells.size();i++){
            for(int j=0;j<cells.get(i).size();j++){
                if(cells.get(i).get(j).getValue()==0){
                    int count=0;
                    for(int row=i-1;row<=i+1;row++){
                        for(int col=j-1;col<=j+1;col++){
                            if(row>=0&&row<cells.size()&&col>=0&&col<cells.get(i).size()){
                                if(cells.get(row).get(col).getValue()==-1){
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
    private Collection<Cell> flatten(List<List<Cell>> cells){
        Collection<Cell> cellList=new ArrayList<>();
        for (List<Cell> cell : cells) {
            cellList.addAll(cell);
        }
        return cellList;
    }

}

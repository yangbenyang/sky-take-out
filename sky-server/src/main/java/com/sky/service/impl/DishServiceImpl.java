package com.sky.service.impl;

import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品
     * @param dish
     */
    @Transactional
    public void save(Dish dish) {
        dishMapper.insert(dish);
    }

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        // 1. 创建Dish对象
        Dish dish = Dish.builder()
                .name(dishDTO.getName())
                .categoryId(dishDTO.getCategoryId())
                .price(dishDTO.getPrice())
                .image(dishDTO.getImage())
                .description(dishDTO.getDescription())
                .status(dishDTO.getStatus())
                .build();
        
        // 2. 保存菜品基本信息
        dishMapper.insert(dish);
        
        // 3. 获取菜品口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            // 4. 设置菜品id
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dish.getId());
            }
            // 5. 保存菜品口味数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        // 1. 分页查询菜品
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        List<Dish> dishes = dishMapper.pageQuery(dishPageQueryDTO);
        
        // 2. 查询总记录数
        int total = dishMapper.countByPage(dishPageQueryDTO);
        
        // 3. 封装PageResult对象
        return new PageResult(total, dishes);
    }
} 
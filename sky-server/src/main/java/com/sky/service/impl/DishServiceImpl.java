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
import java.util.Arrays;

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

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        // 1. 分页查询菜品
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        List<Dish> dishes = dishMapper.pageQuery(dishPageQueryDTO);
        
        // 2. 查询总记录数
        int total = dishMapper.countByPage(dishPageQueryDTO);
        
        // 3. 封装PageResult对象
        return new PageResult(total, dishes);
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 1. 判断当前菜品是否能够删除---是否存在起售中的菜品
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish == null) {
                throw new RuntimeException("菜品不存在");
            }
            if (dish.getStatus() == 1) {
                // 当前菜品处于起售中，不能删除
                throw new RuntimeException("菜品" + dish.getName() + "正在售卖中，不能删除");
            }
        }

        // 2. 删除菜品表中的菜品数据
        dishMapper.deleteByIds(ids);

        // 3. 删除菜品关联的口味数据
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */
    public DishDTO getByIdWithFlavor(Long id) {
        // 1. 查询菜品基本信息
        Dish dish = dishMapper.getById(id);
        if (dish == null) {
            throw new RuntimeException("菜品不存在");
        }

        // 2. 查询菜品口味数据
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);

        // 3. 封装DishDTO对象
        DishDTO dishDTO = new DishDTO();
        dishDTO.setId(dish.getId());
        dishDTO.setName(dish.getName());
        dishDTO.setCategoryId(dish.getCategoryId());
        dishDTO.setPrice(dish.getPrice());
        dishDTO.setImage(dish.getImage());
        dishDTO.setDescription(dish.getDescription());
        dishDTO.setStatus(dish.getStatus());
        dishDTO.setFlavors(flavors);

        return dishDTO;
    }

    /**
     * 修改菜品和对应的口味数据
     * @param dishDTO
     */
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        // 1. 修改菜品基本信息
        Dish dish = Dish.builder()
                .id(dishDTO.getId())
                .name(dishDTO.getName())
                .categoryId(dishDTO.getCategoryId())
                .price(dishDTO.getPrice())
                .image(dishDTO.getImage())
                .description(dishDTO.getDescription())
                .status(dishDTO.getStatus())
                .build();
        dishMapper.update(dish);

        // 2. 删除原有的口味数据
        dishFlavorMapper.deleteByDishIds(Arrays.asList(dishDTO.getId()));

        // 3. 重新插入口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishDTO.getId());
            }
            dishFlavorMapper.insertBatch(flavors);
        }
    }
} 
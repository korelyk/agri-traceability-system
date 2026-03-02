package com.agritrace.mapper;

import com.agritrace.entity.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Select("SELECT current_status as status, COUNT(*) as count FROM products GROUP BY current_status")
    List<Map<String, Object>> countByStatus();

    @Select("SELECT product_category as category, COUNT(*) as count FROM products GROUP BY product_category")
    List<Map<String, Object>> countByCategory();
}

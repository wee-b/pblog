package com.pblog.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pblog.common.domain.entity.Category;
import com.pblog.common.domain.vo.CategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("""
        SELECT 
            c.id,
            c.category_name AS categoryName,
            c.parent_id AS parentId,
            c.order_num AS orderNum,
            c.description,
            COUNT(ac.article_id) AS articleCount
        FROM 
            pb_category c
        LEFT JOIN 
            pb_ac_relation ac ON c.id = ac.category_id
        -- 关键：添加 GROUP BY，包含所有非聚合字段
        GROUP BY 
            c.id, c.category_name, c.parent_id, c.order_num, c.description
        -- 可选：排序（按父分类→显示顺序）
        ORDER BY 
            c.parent_id ASC, c.order_num ASC
    """)
    List<CategoryVO> selectCategoryVO();

    /**
     * 查询一级分类，并将分类自身及所有下级分类关联的文章数汇总到一级分类。
     */
    @Select("""
        WITH RECURSIVE category_tree AS (
            SELECT id AS root_id, id AS category_id
            FROM pb_category
            WHERE parent_id = 0
            UNION ALL
            SELECT tree.root_id, child.id
            FROM category_tree tree
            INNER JOIN pb_category child ON child.parent_id = tree.category_id
        )
        SELECT
            root.id,
            root.category_name AS categoryName,
            root.parent_id AS parentId,
            root.order_num AS orderNum,
            root.description,
            COUNT(DISTINCT relation.article_id) AS articleCount
        FROM pb_category root
        LEFT JOIN category_tree tree ON tree.root_id = root.id
        LEFT JOIN pb_ac_relation relation ON relation.category_id = tree.category_id
        WHERE root.parent_id = 0
        GROUP BY root.id, root.category_name, root.parent_id, root.order_num, root.description
        ORDER BY root.order_num ASC, root.id ASC
    """)
    List<CategoryVO> selectRootCategoryVO();


}

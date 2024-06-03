package com.leyou.item.service;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface CategoryService {

    public List<Category> queryCategoriesByPid(Long pid);

    public List<Category> queryByBid(Long bid);

    public List<String> queryNamesByIds(List<Long> ids);

    public List<Category> queryAllByCid3(Long id);

    public void addCategories(Category category);
    public void updataCategories(Category category);
    public void deleteCategories(Long id);

    public List<Category> queryByBrandId(Long bid);
}

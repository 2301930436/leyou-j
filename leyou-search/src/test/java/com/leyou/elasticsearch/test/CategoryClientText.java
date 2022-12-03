package com.leyou.elasticsearch.test;

import com.leyou.LeyouSearchApplication;
import com.leyou.search.client.CategoryClient;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeyouSearchApplication.class)
public class CategoryClientText {
    @Autowired
    CategoryClient categoryClient;

    @Test
    public void textQueryCategories() {
        List<String> strings = categoryClient.queryNamesByIds(Arrays.asList(1L, 2L, 3L));
        System.out.println(strings);
    }
}
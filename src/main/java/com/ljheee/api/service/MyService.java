package com.ljheee.api.service;

import com.ljheee.api.core.ApiMapping;
import org.springframework.stereotype.Service;

/**
 * 直接把 service服务 暴露成http 接口
 */
@Service
public class MyService {


    /**
     * http://localhost:8087/api?method=com.ljheee.api.MyService.addDoods&params={%22goods%22:{%22code%22:%22test_code%22,%22size%22:13},%22id%22:12}
     * @param goods
     * @param id
     * @return
     */
    @ApiMapping("com.ljheee.api.service.MyService.addDoods")
    public Goods addDoods(Goods goods, Integer id){
        return goods;
    }

    /**
     * http://localhost:8087/api?method=com.ljheee.api.MyService.adds&params={%22id%22:[1,2,3]}
     * @param id
     * @return
     */
    @ApiMapping("com.ljheee.api.service.MyService.adds")
    public Object addArray(Integer[] id){
        return id;
    }

}

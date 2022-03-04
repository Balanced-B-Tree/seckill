package com.myoung.seckill.mapper;

import com.myoung.seckill.pojo.Goods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myoung.seckill.vo.GoodsVo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author jobob
 * @since 2021-08-08
 */
@Component
public interface GoodsMapper extends BaseMapper<Goods> {
    GoodsVo findGoodsVoByGoodsId(Long goodsId);

    List<GoodsVo> findGoodsVo();

    /**
     * 获取商品列表
     */

}

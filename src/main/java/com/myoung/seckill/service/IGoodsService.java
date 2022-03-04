package com.myoung.seckill.service;

import com.myoung.seckill.pojo.Goods;
import com.baomidou.mybatisplus.extension.service.IService;
import com.myoung.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jobob
 * @since 2021-08-08
 */
public interface IGoodsService extends IService<Goods> {

    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}

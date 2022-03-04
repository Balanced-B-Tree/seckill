package com.myoung.seckill.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 
 * </p>
 *
 * @author jobob
 * @since 2021-08-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_goods")
public class Goods implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称
     */
    private String goodName;

    /**
     * 商品标题
     */
    private String goodTitle;

    /**
     * 商品图片
     */
    private String goodImg;

    /**
     * 商品详情
     */
    private String goodDetail;

    /**
     * 商品价格
     */
    private BigDecimal goodPrice;

    /**
     * 商品库存，-1表示没有限制
     */
    private Integer goodStock;


}

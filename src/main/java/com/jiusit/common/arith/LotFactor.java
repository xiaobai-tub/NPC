package com.jiusit.common.arith;

import java.math.BigDecimal;

public class LotFactor {

    /**
     * 抽奖物品或道具唯一标识
     */
    private String lotFactorId;

	/**
     * 中奖概率
     */
    private BigDecimal probability;

    /**
     * 道具数量
     */
    private Integer quantity;
    
    public LotFactor(){
    	
    }
    
    public LotFactor(String lotFactorId, BigDecimal probability, Integer quantity) {
    	this.lotFactorId = lotFactorId;
    	this.probability = probability;
    	this.quantity = quantity;
    }
    
    public String getLotFactorId() {
		return lotFactorId;
	}

	public void setLotFactorId(String lotFactorId) {
		this.lotFactorId = lotFactorId;
	}


    public BigDecimal getProbability() {
        return probability;
    }

    public void setProbability(BigDecimal probability) {
        this.probability = probability;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

}

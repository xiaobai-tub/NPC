package com.jiusit.common.arith;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class LotteryArithTest extends TestCase{
	private static final  Logger log  =  LoggerFactory.getLogger(LotteryArithTest.class);
	
	/**
	 * 抽奖算法-测试1
	 * 一组奖品抽奖测试中奖概率分布
	 */
	@Test
    public void testLotteryList() {
        List<LotFactor> lotFactors = new ArrayList<LotFactor>();
        LotFactor lot1 = new LotFactor();
        lot1.setLotFactorId("p1");
        lot1.setProbability(new BigDecimal(0.05));
        lot1.setQuantity(1);
        lotFactors.add(lot1);

        LotFactor lot2 = new LotFactor();
        lot2.setLotFactorId("p2");
        lot2.setProbability(new BigDecimal(0.10));
        lot2.setQuantity(10);
        lotFactors.add(lot2);

        LotFactor lot3 = new LotFactor();
        lot3.setLotFactorId("p3");
        lot3.setProbability(new BigDecimal(0.15));
        lot3.setQuantity(20);
        lotFactors.add(lot3);

        LotFactor lot4 = new LotFactor();
        lot4.setLotFactorId("p4");
        lot4.setProbability(new BigDecimal(0.20));
        lot4.setQuantity(50);
        lotFactors.add(lot4);

        LotFactor lot5 = new LotFactor();
        lot5.setLotFactorId("p5");
        lot5.setProbability(new BigDecimal(0.30));
        lot5.setQuantity(200);
        lotFactors.add(lot5);
        
        int prize1GetTimes = 0;
        int prize2GetTimes = 0;
        int prize3GetTimes = 0;
        int prize4GetTimes = 0;
        int prize5GetTimes = 0;
        int prizeUnGetTimes = 0;
        
        LotteryArith arithmetic = new LotteryArith();
        int times = 10000;
        for (int i = 0; i < times; i++) {
            String lotId = arithmetic.pay(lotFactors);
            log.info("luckId "+i+" is "+lotId);
            if ("p1".equals(lotId)) {
            	prize1GetTimes++;
            } else if ("p2".equals(lotId)) {
            	prize2GetTimes++;
            } else if ("p3".equals(lotId)) {
            	prize3GetTimes++;
            } else if ("p4".equals(lotId)) {
            	prize4GetTimes++;
            } else if ("p5".equals(lotId)) {
            	prize5GetTimes++;
            } else {
            	prizeUnGetTimes++;
            } 
        }
        
        log.debug("抽奖次数" + times);
        log.debug("未中奖次数" + prizeUnGetTimes);
        log.debug("lot1中奖次数" + prize1GetTimes);
        log.debug("lot2中奖次数" + prize2GetTimes);
        log.debug("lot3中奖次数" + prize3GetTimes);
        log.debug("lot4中奖次数" + prize4GetTimes);
        log.debug("lot5中奖次数" + prize5GetTimes);
    }
    
	/**
	 * 抽奖算法-测试2
	 * 单个奖品抽奖测试中奖概率
	 */
	@Test
    public void testLotteryOne() {
        List<LotFactor> lotFactors = new ArrayList<LotFactor>();
        LotFactor lot1 = new LotFactor();
        lot1.setLotFactorId("p1");
        lot1.setProbability(new BigDecimal(0.05));
        lot1.setQuantity(1);
        lotFactors.add(lot1);

        int prize1GetTimes = 0;
        int prizeUnGetTimes = 0;
        
        LotteryArith arithmetic = new LotteryArith();
        int times = 1000;
        for (int i = 0; i < times; i++) {
            String lotId = arithmetic.pay(lotFactors);
            log.info("luckId "+i+" is "+lotId);
            if ("p1".equals(lotId)) {
            	prize1GetTimes++;
            } else {
            	prizeUnGetTimes++;
            } 
        }
        
        log.debug("抽奖次数" + times);
        log.debug("未中奖次数" + prizeUnGetTimes);
        log.debug("lot1中奖次数" + prize1GetTimes);
       
    }
}
package com.jiusit.common.arith;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

public class LotteryArith {
    // 放大倍数
    private static final int mulriple = 1000000;

    public String pay(List<LotFactor> lotFactors) {
        int lastScope = 0;
        // 洗牌，打乱奖品次序
        Collections.shuffle(lotFactors);
        Map<String, int[]> lotScopes = new HashMap<String, int[]>();
        Map<String, Integer> lotQuantity = new HashMap<String, Integer>();
        for (LotFactor lotFactor : lotFactors) {
            String lotFactorId = lotFactor.getLotFactorId();
            // 划分区间
            int currentScope = lastScope + lotFactor.getProbability().multiply(new BigDecimal(mulriple)).intValue();
            lotScopes.put(lotFactorId, new int[] { lastScope + 1, currentScope });
            lotQuantity.put(lotFactorId, lotFactor.getQuantity());
            lastScope = currentScope;
        }

        // 获取1-1000000之间的一个随机数
        int luckyNumber = new Random().nextInt(mulriple);
        String luckyLotId = "";
        // 查找随机数所在的区间
        if ((null != lotScopes) && !lotScopes.isEmpty()) {
            Set<Entry<String, int[]>> entrySets = lotScopes.entrySet();
            for (Map.Entry<String, int[]> m : entrySets) {
                String key = m.getKey();
                if (luckyNumber >= m.getValue()[0] && luckyNumber <= m.getValue()[1] && lotQuantity.get(key) > 0) {
                	luckyLotId = key;
                    break;
                }
            }
        }

        if (!luckyLotId.equals("")) {	
        	// 奖品库存减一  
        }
        	
        return luckyLotId;
    }
}
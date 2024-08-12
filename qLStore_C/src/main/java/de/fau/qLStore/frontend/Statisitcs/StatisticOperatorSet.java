package de.fau.qLStore.frontend.Statisitcs;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticOperatorSet {

    public String source;
    public int[] total;
    public Hashtable<String, int[]> operatorSet;

    public List<StatisticHelper> toOperatorSetGridItemList(){
        List<StatisticHelper> result = new ArrayList<>();
        DecimalFormat formatter = new DecimalFormat("##.##");
        for (String key : operatorSet.keySet()){
            result.add(new StatisticHelper(key, operatorSet.get(key)[0], formatter.format((double) operatorSet.get(key)[0]/total[0]*100), operatorSet.get(key)[1], formatter.format((double) operatorSet.get(key)[1]/total[1]*100)));
        }

        return result;
    }
}

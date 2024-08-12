package de.fau.qLStore.frontend.Statisitcs;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class StatisticKeywordCount {

    public String source;

    public int totalValid;
    public int totalUnique;
    public int selectValid;
    public int selectUnique;
    public int askValid;
    public int askUnique;
    public int describeValid;
    public int describeUnique;
    public int constructValid;
    public int constructUnique;
    public int updateValid;
    public int updateUnique;
    public int andValid;
    public int andUnique;
    public int filterValid;
    public int filterUnique;
    public int optionalValid;
    public int optionalUnique;
    public int unionValid;
    public int unionUnique;

    //TODO: add more

    public void setKeywords(List<int[]> keywordCounter){
        for (int i = 0; i < keywordCounter.size(); i++){
            switch (i) {
                case 0:
                    totalValid = keywordCounter.get(i)[0];
                    totalUnique = keywordCounter.get(i)[1];
                    break;
                case 1:
                    selectValid = keywordCounter.get(i)[0];
                    selectUnique = keywordCounter.get(i)[1];
                    break;
                case 2:
                    askValid = keywordCounter.get(i)[0];
                    askUnique = keywordCounter.get(i)[1];
                    break;
                case 3:
                    describeValid = keywordCounter.get(i)[0];
                    describeUnique = keywordCounter.get(i)[1];
                    break;
                case 4:
                    constructValid = keywordCounter.get(i)[0];
                    constructUnique = keywordCounter.get(i)[1];
                    break;
                case 5:
                    updateValid = keywordCounter.get(i)[0];
                    updateUnique = keywordCounter.get(i)[1];
                    break;
                case 6:
                    andValid = keywordCounter.get(i)[0];
                    andUnique = keywordCounter.get(i)[1];
                    break;
                case 7:
                    filterValid = keywordCounter.get(i)[0];
                    filterUnique = keywordCounter.get(i)[1];
                    break;
                case 8:
                    optionalValid = keywordCounter.get(i)[0];
                    optionalUnique = keywordCounter.get(i)[1];
                    break;
                case 9:
                    unionValid = keywordCounter.get(i)[0];
                    unionUnique = keywordCounter.get(i)[1];
                    break;
            }
        }
    }

    public List<StatisticHelper> toKeywordCountGridItemList(){
        List<StatisticHelper> result = new ArrayList<>();
        DecimalFormat formatter = new DecimalFormat("##.##");
        result.add(new StatisticHelper("Select", selectValid, formatter.format((double) selectValid/totalValid*100), selectUnique, formatter.format((double) selectUnique/totalUnique*100)));
        result.add(new StatisticHelper("Ask", askValid, formatter.format((double) askValid/totalValid*100), askUnique, formatter.format((double) askUnique/totalUnique*100)));
        result.add(new StatisticHelper("Describe", describeValid, formatter.format((double) describeValid/totalValid*100), describeUnique, formatter.format((double) describeUnique/totalUnique*100)));
        result.add(new StatisticHelper("Construct", constructValid, formatter.format((double) constructValid/totalValid*100), constructUnique, formatter.format((double) constructUnique/totalUnique*100)));
        result.add(new StatisticHelper("Update", updateValid, formatter.format((double) updateValid/totalValid*100), updateUnique, formatter.format((double) updateUnique/totalUnique*100)));
        result.add(new StatisticHelper("And", andValid, formatter.format((double) andValid/totalValid*100), andUnique, formatter.format((double) andUnique/totalUnique*100)));
        result.add(new StatisticHelper("Filter", filterValid, formatter.format((double) filterValid/totalValid*100), filterUnique, formatter.format((double) filterUnique/totalUnique*100)));
        result.add(new StatisticHelper("Optional", optionalValid, formatter.format((double) optionalValid/totalValid*100), optionalUnique, formatter.format((double) optionalUnique/totalUnique*100)));
        result.add(new StatisticHelper("Union", unionValid, formatter.format((double) unionValid/totalValid*100), unionUnique, formatter.format((double) unionUnique/totalUnique*100)));
        return result;
    }
}

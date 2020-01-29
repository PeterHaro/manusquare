package matchmaking.models;

import java.util.ArrayList;
import java.util.List;

public class Buyer {
    private int BuyerId;
    private List<TransactionalData> HistoricalData;

    public Buyer(int buyerId)  {
        BuyerId = buyerId;
    }

    public int getBuyerId() {
        return BuyerId;
    }

    public void addTransactionalData(TransactionalData datum) {
        if (HistoricalData == null) {
            HistoricalData = new ArrayList<>();
        }
        HistoricalData.add(datum);
    }

    public List<TransactionalData> getHistoricalData() {
        return HistoricalData;
    }

    public void setHistoricalData(List<TransactionalData> historicalData) {
        HistoricalData = historicalData != null ? historicalData : new ArrayList<>();
    }
}

package matchmaking.models;

import java.util.ArrayList;
import java.util.List;

public class Buyer {
    private int id;
    private List<TransactionalData> HistoricalData;
    //FIXME: Consider removing these fields
    int averageQuality;
    int averageDeliveryTime;
    int averagePacking;
    int averageReponseRate;
    int averageOverallSatesfaction;

    public Buyer(int id)  {
        this.id = id;
    }

    public int getId() {
        return id;
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

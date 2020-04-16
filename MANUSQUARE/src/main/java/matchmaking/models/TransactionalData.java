package matchmaking.models;

public class TransactionalData {
    private int TransactionId;
    private int SupplierId;
    private int BuyerId;
    //private int PriceInEuros;
    private PriceClassification priceClassification;
    private int QualityLikert;
    private int DeliveryTimeLikert;
    private int PackagingLikert;
    private int ResponseRateLikert;
    private int OverallSatesfactionLikert;


    public TransactionalData() {

    }

    //__GENERATED ->

    public int getQualityLikert() {
        return QualityLikert;
    }

    public void setQualityLikert(int qualityLikert) {
        QualityLikert = qualityLikert;
    }

    public int getDeliveryTimeLikert() {
        return DeliveryTimeLikert;
    }

    public void setDeliveryTimeLikert(int deliveryTimeLikert) {
        DeliveryTimeLikert = deliveryTimeLikert;
    }

    public int getPackagingLikert() {
        return PackagingLikert;
    }

    public void setPackagingLikert(int packagingLikert) {
        PackagingLikert = packagingLikert;
    }

    public int getResponseRateLikert() {
        return ResponseRateLikert;
    }

    public void setResponseRateLikert(int responseRateLikert) {
        ResponseRateLikert = responseRateLikert;
    }

    public int getOverallSatesfactionLikert() {
        return OverallSatesfactionLikert;
    }

    public void setOverallSatesfactionLikert(int overallSatesfactionLikert) {
        OverallSatesfactionLikert = overallSatesfactionLikert;
    }

    public int getSupplierId() {
        return SupplierId;
    }

    public void setSupplierId(int supplierId) {
        SupplierId = supplierId;
    }

    public int getBuyerId() {
        return BuyerId;
    }

    public void setBuyerId(int buyerId) {
        BuyerId = buyerId;
    }

  /*  public int getPriceInEuros() {
        return PriceInEuros;
    }

    public void setPriceInEuros(int priceInEuros) {
        PriceInEuros = priceInEuros;
    }
   */

    public PriceClassification getPriceClassification() {
        return priceClassification;
    }

    public void setPriceClassification(PriceClassification priceClassification) {
        this.priceClassification = priceClassification;
    }

    public int getTransactionId() {
        return TransactionId;
    }

    public void setTransactionId(int transactionId) {
        TransactionId = transactionId;
    }
}

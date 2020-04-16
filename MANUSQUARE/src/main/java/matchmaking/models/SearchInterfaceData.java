package matchmaking.models;

public class SearchInterfaceData {
    private int QualityOfProjectResultsImportance;
    private int OnTimeDeliveryImportance;
    private int CommunicationAndCollaberationEffectivenessImportance;
    private int SearchRadius;
    private int ProfileRankingImportance;
    private int SustainabilityRanking;
    private int PriceRange;

    public SearchInterfaceData(int qualityOfProjectResultsImportance, int onTimeDeliveryImportance, int communicationAndCollaberationEffectivenessImportance, int searchRadius, int profileRankingImportance, int sustainabilityRanking, int priceRange) {
        QualityOfProjectResultsImportance = qualityOfProjectResultsImportance;
        OnTimeDeliveryImportance = onTimeDeliveryImportance;
        CommunicationAndCollaberationEffectivenessImportance = communicationAndCollaberationEffectivenessImportance;
        SearchRadius = searchRadius;
        ProfileRankingImportance = profileRankingImportance;
        SustainabilityRanking = sustainabilityRanking;
        PriceRange = priceRange;
    }
}

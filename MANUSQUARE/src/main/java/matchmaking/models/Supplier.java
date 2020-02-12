package matchmaking.models;

public class Supplier {
    private int id;
    private int ProfileRanking;
    private int SustainabilityRanking;
    private int DistanceFromBuyerInKm; //TODO: FIXME: I am just mocking this for now as I dont bother setting up random areas in some GEO area and converting coords to km etc etc

    public Supplier(int supplierId, int profileRanking, int sustainabilityRanking, int distanceFromBuyerInKm) {
        id = supplierId;
        ProfileRanking = profileRanking;
        SustainabilityRanking = sustainabilityRanking;
        DistanceFromBuyerInKm = distanceFromBuyerInKm;
    }

    public String getSupplierInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Dumping information for supplier: ").append(getId()).append(System.lineSeparator());
        sb.append("\t Profile ranking: ").append(ProfileRanking).append(System.lineSeparator());
        sb.append("\t SustainabilityRanking: ").append(SustainabilityRanking).append(System.lineSeparator());
        sb.append("\t Distance from buyer in km: ").append(DistanceFromBuyerInKm);
        return sb.toString();
    }

    public int getId() {
        return id;
    }

    public int getProfileRanking() {
        return ProfileRanking;
    }

    public int getSustainabilityRanking() {
        return SustainabilityRanking;
    }

    public int getDistanceFromBuyerInKm() {
        return DistanceFromBuyerInKm;
    }
}

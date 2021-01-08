package matchmaking.models;

import utilities.MathUtilities;

import java.security.InvalidParameterException;

public class SearchInterfaceDataBuilder {
    private int qualityOfProjectResultsImportance;
    private int onTimeDeliveryImportance;
    private int communicationAndCollaberationEffectivenessImportance;
    private int searchRadius;
    private int profileRankingImportance; //TODO: CONSIDER CLASSIFYING THIS
    private int sustainabilityRanking; // TODO: CONMSIDERING CLASSYFING THISA
    private int priceRange; // TODO: CONSIDER CLASSIFY YO

    public SearchInterfaceDataBuilder setQualityOfProjectResultsImportance(int qualityOfProjectResultsImportance) {
        if(MathUtilities.inRange(qualityOfProjectResultsImportance, 0, 5)) {
            this.qualityOfProjectResultsImportance = qualityOfProjectResultsImportance;
            return this;
        }
        throw new InvalidParameterException("Quality of project result importance must be from 0 to 5");
    }

    public SearchInterfaceDataBuilder setOnTimeDeliveryImportance(int onTimeDeliveryImportance) {
        if(MathUtilities.inRange(onTimeDeliveryImportance, 0, 5)) {
            this.onTimeDeliveryImportance = onTimeDeliveryImportance;
            return this;
        }
        throw new InvalidParameterException("On time delivery importance must be from 0 to 5");
    }

    public SearchInterfaceDataBuilder setCommunicationAndCollaberationEffectivenessImportance(int communicationAndCollaberationEffectivenessImportance) {
        if(MathUtilities.inRange(communicationAndCollaberationEffectivenessImportance, 0, 5)) {
            this.communicationAndCollaberationEffectivenessImportance = communicationAndCollaberationEffectivenessImportance;
            return this;
        }
        throw new InvalidParameterException("Communication and collaboration importance must be from 0 to 5");
    }

    public SearchInterfaceDataBuilder setSearchRadius(int searchRadius) {
        this.searchRadius = searchRadius;
        return this;
    }

    public SearchInterfaceDataBuilder setProfileRankingImportance(int profileRankingImportance) {
        if(MathUtilities.inRange(profileRankingImportance, 0, 100)) {
            this.profileRankingImportance = profileRankingImportance;
            return this;
        }
        throw new InvalidParameterException("Profile ranking can only be from 0 to 100");
    }

    public SearchInterfaceDataBuilder setSustainabilityRanking(int sustainabilityRanking) {
        if(MathUtilities.inRange(sustainabilityRanking, 0, 100)) {
            this.sustainabilityRanking = sustainabilityRanking;
            return this;
        }
        throw new InvalidParameterException("Sustainability ranking can only be from 0 to 100");
    }

    public SearchInterfaceDataBuilder setPriceRange(int priceRange) {
        this.priceRange = priceRange;
        return this;
    }

    public SearchInterfaceData createSearchInterfaceData() {
        return new SearchInterfaceData(qualityOfProjectResultsImportance, onTimeDeliveryImportance, communicationAndCollaberationEffectivenessImportance, searchRadius, profileRankingImportance, sustainabilityRanking, priceRange);
    }
}
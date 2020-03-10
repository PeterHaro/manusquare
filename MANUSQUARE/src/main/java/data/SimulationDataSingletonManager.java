package data;

import matchmaking.models.*;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SimulationDataSingletonManager {
    private static final int NUMBER_OF_HISTORICAL_DATA_ENTRIES =75000;
    private static final int NUMBER_OF_BUYERS = 35;
    private static final int NUMBER_OF_SUPPLIERS = 110;
    private static final DataGenerationMethodology dataGenerationMethodology = DataGenerationMethodology.RANDOM;
    private static final DataGenerationMethodology buyerGenerationMethodology = DataGenerationMethodology.WEIGHTED_FIRST_25_PERCENTILE;
    private static final DataGenerationMethodology qualityGenerationMethodology = DataGenerationMethodology.WEIGHTED_LAST_25_PERCENTILE;
    private static final DataGenerationMethodology deliveryTimeGenerationMethodology = DataGenerationMethodology.WEIGHTED_FIRST_25_PERCENTILE;
    private static final DataGenerationMethodology packagingGenerationMethodology = DataGenerationMethodology.RANDOM;
    private static final DataGenerationMethodology responseRateGenerationMethodology = DataGenerationMethodology.RANDOM;
    private static final DataGenerationMethodology overallSatesfactionGenerationMethodology = DataGenerationMethodology.RANDOM;

    // __BEGIN_DATA_
    private static List<HistoricalDataContainer> _historicalData = null;
    private static List<TransactionalData> _transactionalData = null;
    private static List<Supplier> _suppliers = null;
    private static List<Buyer> _buyers = null;
    private static Map<Integer, List<Offer>> _offersForOrder = new HashMap<>();
    // __END_DATA_

    private static final SimulationDataSingletonManager instance = new SimulationDataSingletonManager();

    public static AtomicInteger lastOfferId = new AtomicInteger();

    public static SimulationDataSingletonManager getInstance() {
        return instance;
    }

    public void addOffersForOrderId(int orderId, List<Offer> offers) {
        if (_offersForOrder.containsKey(orderId)) {
            _offersForOrder.get(orderId).addAll(offers);
        } else {
            _offersForOrder.put(orderId, offers);
        }
    }

    public List<Offer> getOffersForOrderId(int orderId) {
        return _offersForOrder.get(orderId);
    }

    public boolean hasOffers(int orderId) {
        return _offersForOrder.containsKey(orderId);
    }

    public Map<Integer, List<Offer>> getOffers() {
        return _offersForOrder;
    }

    public List<Buyer> getBuyers() {
        return _buyers;
    }

    public List<Supplier> getSuppliers() {
        return _suppliers;
    }

    public List<TransactionalData> getTransactionalData() {
        return _transactionalData;
    }

    private SimulationDataSingletonManager() {
        List<HistoricalDataContainer> historicalData = new ArrayList<>();
        List<TransactionalData> transactionalData = new ArrayList<>();
        List<Supplier> suppliers = IntStream.range(0, NUMBER_OF_SUPPLIERS).mapToObj(i -> new Supplier(i, GenerateRandomNumberInRange(0, 100), GenerateRandomNumberInRange(0, 100), GenerateRandomNumberInRange(2, 500))).collect(Collectors.toList());
        List<Buyer> buyers = IntStream.range(0, NUMBER_OF_BUYERS).mapToObj(Buyer::new).collect(Collectors.toList());
        for (int i = 0; i < NUMBER_OF_HISTORICAL_DATA_ENTRIES; i++) {
            TransactionalData datum = new TransactionalData();
            datum.setTransactionId(i);
            datum.setSupplierId(generate_supplier_id(i));
            datum.setBuyerId(generateBuyerId(i));
            datum.setPriceClassification(getRandomPrice(i));
            datum.setQualityLikert(generateQualityLikertValues(i));
            datum.setDeliveryTimeLikert(generateDeliveryTimeLikertValue(i));
            datum.setPackagingLikert(generatePackagingLikertvalue(i));
            datum.setResponseRateLikert(generateResponseRateLikertValue(i));
            datum.setOverallSatesfactionLikert(generateOverallSatesfactionLikertValue(i));
            transactionalData.add(datum);

            // Add reference to buyer for his supplier
            for (Buyer buyer : buyers) {
                if (buyer.getId() == datum.getBuyerId()) {
                    buyer.addTransactionalData(datum);
                }
            }
        }

        System.out.println("Number of transactions: " + NUMBER_OF_HISTORICAL_DATA_ENTRIES);
        System.out.println("Number of buyers: " + NUMBER_OF_BUYERS);
        System.out.println("Number of suppliers: " + NUMBER_OF_SUPPLIERS);
     //   System.out.println("========== dumping supplier info ==========");
    //    for (Supplier supplier : suppliers) {
      //      System.out.println(supplier.getSupplierInfo());
      //  }

        for (Buyer buyer : buyers) {
            System.out.println("Dumping information for buyer: " + buyer.getId());
            int averageQuality = 0;
            int averageDeliveryTime = 0;
            int averagePacking = 0;
            int averageReponseRate = 0;
            int averageOverallSatesfaction = 0;

            for (TransactionalData datum : buyer.getHistoricalData()) {
                averageQuality += datum.getQualityLikert();
                averageDeliveryTime += datum.getDeliveryTimeLikert();
                averagePacking += datum.getPackagingLikert();
                averageReponseRate += datum.getResponseRateLikert();
                averageOverallSatesfaction += datum.getOverallSatesfactionLikert();
            }
            // Calculate avg
            averageQuality = averageQuality / buyer.getHistoricalData().size();
            averageDeliveryTime = averageDeliveryTime / buyer.getHistoricalData().size();
            averagePacking = averagePacking / buyer.getHistoricalData().size();
            averageReponseRate = averageReponseRate / buyer.getHistoricalData().size();
            averageOverallSatesfaction = averageOverallSatesfaction / buyer.getHistoricalData().size();

            System.out.println("\tAverage Quality is: " + averageQuality);
            System.out.println("\tAverage Delivery Time is: " + averageDeliveryTime);
            System.out.println("\tAverage packing is: " + averagePacking);
            System.out.println("\tAverage response rate is: " + averageReponseRate);
            System.out.println("\tAverage overall satesfaction is: " + averageOverallSatesfaction);
            System.out.println("\tContains: " + buyer.getHistoricalData().size() + " historical entries");
            System.out.println("\n");
        }
        _historicalData = historicalData;
        _transactionalData = transactionalData;
        _suppliers = suppliers;
        _buyers = buyers;
    }

    private static List<Offer> generateOffers(int n, int orderId) {
        List<Offer> retval = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Offer offer = new Offer();
            offer.orderId = orderId;
            offer.offerId = i;
            offer.distanceInKm = GenerateRandomNumberInRange(15, 25);
            offer.price = GenerateRandomNumberInRange(0, 2); // TODO: TRANSFORM TOP CLASSIFICATION
            offer.semanticSimilarity = ThreadLocalRandom.current().nextDouble(0.25, 1.00);
        }
        return retval;
    }

    // TODO: MAKE DIFFERENT WEIGHTED MODES
    private static PriceClassification getRandomPrice(int seed) {
        int rand = GenerateRandomNumberInRange(0, 2);
        switch (rand) {
            case 1:
                return PriceClassification.MEDIAN;
            case 2:
                return PriceClassification.HIGH;
            case 0:
            default:
                return PriceClassification.LOW;
        }
    }

    private static int generateOverallSatesfactionLikertValue(int seed) {
        return generateRandomLikertNumberFromMethodology(seed, overallSatesfactionGenerationMethodology);
    }

    private static int generateResponseRateLikertValue(int seed) {
        return generateRandomLikertNumberFromMethodology(seed, responseRateGenerationMethodology);
    }

    private static int generateDeliveryTimeLikertValue(int seed) {
        return generateRandomLikertNumberFromMethodology(seed, deliveryTimeGenerationMethodology);
    }

    private static int generatePackagingLikertvalue(int seed) {
        return generateRandomLikertNumberFromMethodology(seed, packagingGenerationMethodology);
    }

    private static int generateQualityLikertValues(int seed) {
        return generateRandomLikertNumberFromMethodology(seed, qualityGenerationMethodology);
    }

    private static int generateRandomLikertNumberFromMethodology(int seed, DataGenerationMethodology methodology) {
        switch (methodology) {
            case SEQUENTIAL:
                if (seed == 0) {
                    return 1;
                }
                return seed % 5;
            case RANDOM:
                return GenerateRandomNumberInRange(1, 5);
            case WEIGHTED_FIRST_25_PERCENTILE:
                int random_num = GenerateRandomNumberInRange(1, 5);
                int bottom_weight = 3;
                if (random_num <= bottom_weight) {
                    return GenerateRandomNumberInRange(1, 2);
                }
                return GenerateRandomNumberInRange(1, 5);
            case WEIGHTED_LAST_25_PERCENTILE:
                int random_number = GenerateRandomNumberInRange(1, 5);
                int top_wiegth = 3;
                if (random_number >= top_wiegth) {
                    return GenerateRandomNumberInRange(4, 5);
                }
                return GenerateRandomNumberInRange(1, 5);
        }
        return seed;
    }

    private static int generate_supplier_id(int seed) {
        return GenerateRandomNumberFromGivenDistribution(seed, NUMBER_OF_SUPPLIERS);
    }

    private static int generateBuyerId(int seed) {
        return GenerateWeightedBuyer(seed, NUMBER_OF_BUYERS);
    }

    private static int GenerateWeightedBuyer(int seed, int max_range) {
        switch (buyerGenerationMethodology) {
            case SEQUENTIAL:
                return seed;
            case RANDOM:
                return GenerateRandomNumberInRange(0, (max_range - 1));
            case WEIGHTED_FIRST_25_PERCENTILE:
                int twentyFifthPercentile = Math.round(max_range / 4);
                int random_num = GenerateRandomNumberInRange(0, max_range);
                if (random_num < twentyFifthPercentile) {
                    return GenerateRandomNumberInRange(0, twentyFifthPercentile);
                }
                return GenerateRandomNumberInRange(0, (max_range - 1));
            default:
                throw new InvalidParameterException("The data generation methodology requires a fixed method");
        }
    }

    private static int GenerateRandomNumberFromGivenDistribution(int seed, int max_range) {
        switch (dataGenerationMethodology) {
            case SEQUENTIAL:
                return seed;
            case RANDOM:
                return GenerateRandomNumberInRange(0, (max_range - 1));
            case WEIGHTED_FIRST_25_PERCENTILE:
                int twentyFifthPercentile = Math.round(max_range / 4);
                int random_num = GenerateRandomNumberInRange(0, max_range);
                if (random_num < twentyFifthPercentile) {
                    return GenerateRandomNumberInRange(0, twentyFifthPercentile);
                }
                return GenerateRandomNumberInRange(0, (max_range - 1));
            default:
                throw new InvalidParameterException("The data generation methodology requires a fixed method");
        }
    }

    private static int GenerateRandomNumberInRange(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}

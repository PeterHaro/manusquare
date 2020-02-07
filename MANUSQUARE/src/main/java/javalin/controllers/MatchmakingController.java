package javalin.controllers;

import data.SimulationDataSingletonManager;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import javalin.models.ErrorResponse;
import matchmaking.models.Buyer;
import matchmaking.models.Offer;
import matchmaking.models.Supplier;
import matchmaking.models.TransactionalData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class MatchmakingController {

    static SimulationDataSingletonManager manager = SimulationDataSingletonManager.getInstance();

    @OpenApi(
            summary = "Get all buyers",
            operationId = "getAllBuyers",
            path = "/matchmaking",
            method = HttpMethod.GET,
            tags = {"Buyers"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Buyer[].class)})
            }
    )
    public static void getAllBuyers(Context ctx) {
        ctx.json(manager.getBuyers());
    }

    @OpenApi(
            summary = "Get all suppliers",
            operationId = "getAllSuppliers",
            path = "/matchmaking",
            method = HttpMethod.GET,
            tags = {"Suppliers"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Supplier[].class)})
            }
    )
    public static void getAllSuppliers(Context ctx) {
        ctx.json(manager.getSuppliers());
    }

    @OpenApi(
            summary = "Get all transactional data",
            operationId = "getAllTransactionalData",
            path = "/matchmaking",
            method = HttpMethod.GET,
            tags = {"Transactional data", "Historical transactions", "Historical Data"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = TransactionalData[].class)})
            }
    )
    public static void getAllTransactionalData(Context ctx) {
        ctx.json(manager.getTransactionalData());
    }

    // TODO: Reworke me for prod. This is generating fake offers for the mm algo
    @OpenApi(
            summary = "Get user by orderId",
            operationId = "getByOrderId",
            path = "/matchmaking/:orderId",
            method = HttpMethod.GET,
            pathParams = {@OpenApiParam(name = "orderId", type = Integer.class, description = "The order ID")},
            tags = {"Offers"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Offer[].class)}),
                    @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public static void getOffers(Context ctx) {
        int orderId = validPathParamUserId(ctx);
        if (manager.hasOffers(orderId)) {
            ctx.json(manager.getOffersForOrderId(orderId));
            return;
        }

        List<Offer> retval = new ArrayList<>();
        int length = GenerateRandomNumberInRange(3, 10);
        List<Integer> suppliersId = manager.getSuppliers().stream().map(Supplier::getSupplierId).collect(Collectors.toList());
        // Offers doesnt exist, so fetch em!
        for (int i = 0; i < length; i++) {
            Offer offer = new Offer();
            offer.offerId = SimulationDataSingletonManager.lastOfferId.incrementAndGet();
            offer.orderId = orderId;
            offer.supplierId = GetSupplierId(suppliersId.size());
            offer.distanceInKm = GenerateRandomNumberInRange(15, 105);
            offer.price = GenerateRandomNumberInRange(0, 2);
            offer.semanticSimilarity = Math.random();
            retval.add(offer);
        }
        manager.addOffersForOrderId(orderId, retval);
        ctx.json(retval);
    }

    // Call python API
    public static void PerformMatchmaking(Context ctx) {

    }

    private static int GetSupplierId(int amountOfSuppliers) {
        return GenerateRandomNumberInRange(0, amountOfSuppliers - 1);
    }

    // Prevent duplicate validation of orderID
    private static int validPathParamUserId(Context ctx) {
        return ctx.pathParam("orderId", Integer.class).check(id -> id > 0).get();
    }


    private static int GenerateRandomNumberInRange(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}

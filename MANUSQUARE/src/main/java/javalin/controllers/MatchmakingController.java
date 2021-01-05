package javalin.controllers;

import ch.qos.logback.classic.sift.GSiftingAppender;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import data.SimulationDataSingletonManager;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.plugin.json.JavalinJson;
import io.javalin.plugin.openapi.annotations.*;
import javalin.models.ErrorResponse;
import json.RequestForQuotation;
import matchmaking.models.Buyer;
import matchmaking.models.Offer;
import matchmaking.models.Supplier;
import matchmaking.models.TransactionalData;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import query.CSQuery;
import semanticmatching.CSSemanticMatching;
import validation.JSONValidator;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class MatchmakingController {
    private static final String MatchmakingEndpoint = "http://localhost:1335/mm";
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
    // Call python API

    @OpenApi(
            summary = "Get offers by user ID",
            operationId = "getOffersById",
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

        List<Offer> retval = GenerateOffers(orderId);
        ctx.json(retval);
    }

    private static List<Offer> GenerateOffers(int orderId) {
        List<Offer> retval = new ArrayList<>();
        int length = GenerateRandomNumberInRange(3, 10);
        List<Integer> suppliersId = manager.getSuppliers().stream().map(Supplier::getId).collect(Collectors.toList());
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
        return retval;
    }

    public static void PerformMatchmaking(Context ctx) throws IOException, OWLOntologyStorageException, OWLOntologyCreationException {
        String rfq = ctx.pathParam(("rfq")); // Change if needed!
        if (rfq.isEmpty()) {
            throw new BadRequestResponse();
        } else {
            String jsonInput = Objects.requireNonNull(ctx.formParam("rfq"));
            if (JSONValidator.isJSONValid(jsonInput)) {
                RequestForQuotation requestForQuotation = new Gson().fromJson(jsonInput, RequestForQuotation.class);
                if (requestForQuotation.customer == null) {
                    throw new BadRequestResponse("Invalid customer info. Please insert a valid customer in the request for quotation");
                }
            }
            StringWriter sw = new StringWriter();
            BufferedWriter writer = new BufferedWriter(sw);
            CSSemanticMatching.performSemanticMatching(jsonInput, 10, writer, false, true, 0.9);
            System.out.println(sw.toString());

            String MatchmakingResults = CallMatchmaking();

            // AWAIT INFO HERE TO TO NEXT PART
            //ctx.json(sw.toString());


            ctx.status(200);
        }
    }

    private static String CallMatchmaking() throws IOException {
        final URL matchmakingUrl = new URL(MatchmakingEndpoint);
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = (HttpURLConnection) matchmakingUrl.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        String offers = new Gson().toJson(GenerateOffers(1));
        try(OutputStream os = con.getOutputStream()){
            byte[] input = offers.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        //TODO: Multiple actions depending on MM status code!
        int code = con.getResponseCode();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))){
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
        }
        return response.toString();
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

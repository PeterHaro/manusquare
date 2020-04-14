package javalin.controllers;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.util.Objects;

import com.google.gson.Gson;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.annotations.*;
import javalin.models.ErrorResponse;
import javalin.models.Rfq;
import json.RequestForQuotation;
import ui.SemanticMatching_MVP;
import validation.JSONValidation;

public class SemanticMatchingController {
    @OpenApi(
            method = HttpMethod.GET,
            description = "This endpoint performs the Semantic matching from a given RFQ.",
            operationId = "PerformSemanticMatching",
            summary = "This methodc performs the semantic matching based on the RFQ",
            deprecated = false,
            tags = {"Semantic matching"},

            //requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Rfq.class)),
            //queryParams = @OpenApiParam(name="rfq", type=Rfq.class),
            //requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Rfq.class, type = "applicatiton/json")),
            formParams = @OpenApiFormParam(name = "rfq", type = Rfq.class),
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Rfq[].class)}),
                    @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(type = "application/json")})
            }

    )
    // VALIDATE CUSTOMER
    public static Handler PerformSemanticMatching = ctx -> {
        String jsonInput = Objects.requireNonNull(ctx.formParam("rfq")); // SWITCH DO QUERY PARAMS
        if (JSONValidation.isJSONValid(jsonInput)) {
            RequestForQuotation rfq = new Gson().fromJson(jsonInput, RequestForQuotation.class);
            if (rfq.customer == null) {
                throw new BadRequestResponse("Invalid customer info. Please insert a valid customer in the request for quotation");
            }
        }
        StringWriter sw = new StringWriter();
        BufferedWriter writer = new BufferedWriter(sw);

        SemanticMatching_MVP.performSemanticMatching(jsonInput, 10, writer, false, true, 0.9);
        System.out.println(sw.toString());
        ctx.json(sw.toString());
    };
}

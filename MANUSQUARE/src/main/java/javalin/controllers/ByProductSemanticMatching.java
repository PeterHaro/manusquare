package javalin.controllers;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.util.Objects;

import com.google.gson.Gson;

import io.javalin.http.Handler;
import io.javalin.plugin.openapi.annotations.HttpMethod;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiFormParam;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;
import javalin.models.ErrorResponse;
import javalin.models.Rfq;
import json.RequestForQuotation;
import semanticmatching.BPSemanticMatching;
import validation.JSONValidator;


public class ByProductSemanticMatching {
    @OpenApi(
            method = HttpMethod.GET,
            description = "This endpoint performs the matchmaking for by-products.",
            operationId = "PerformMatching",
            summary = "This method performs the semantic matching based on a by-product sharing request from a consumer.",
            deprecated = false,
            tags = {"By-product matching"},

            formParams = @OpenApiFormParam(name = "rfq", type = Rfq.class),
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Rfq[].class)}),
                    @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(type = "application/json")})
            }
    )
    public static Handler performSemanticMatchingOnByProducts = ctx -> {
        String jsonInput = Objects.requireNonNull(ctx.formParam("rfq"));
        if (JSONValidator.isJSONValid(jsonInput)) {
            RequestForQuotation rfq = new Gson().fromJson(jsonInput, RequestForQuotation.class);
            //if (rfq.customer == null) {
             //   throw new BadRequestResponse("Invalid customer info. Please insert a valid customer in the request for quotation");
            //}
        }
        StringWriter sw = new StringWriter();
        BufferedWriter writer = new BufferedWriter(sw);

        BPSemanticMatching.performByProductMatching(jsonInput, 10, writer, false, true, 0.9);
        System.out.println(sw.toString());
        ctx.json(sw.toString());
    };
}

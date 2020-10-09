package javalin.controllers;

import com.google.gson.Gson;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.annotations.*;
import javalin.models.ErrorResponse;
import javalin.models.Rfq;
import json.RequestForQuotation;
import ui.SemanticMatching_BP;
import ui.SemanticMatching_IM;
import ui.SemanticMatching_MVP;
import validation.JSONValidation;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.util.Objects;


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
        if (JSONValidation.isJSONValid(jsonInput)) {
            RequestForQuotation rfq = new Gson().fromJson(jsonInput, RequestForQuotation.class);
            //if (rfq.customer == null) {
             //   throw new BadRequestResponse("Invalid customer info. Please insert a valid customer in the request for quotation");
            //}
        }
        StringWriter sw = new StringWriter();
        BufferedWriter writer = new BufferedWriter(sw);

        SemanticMatching_BP.performSemanticMatching_BP(jsonInput, 10, writer, false, true, 0.9);
        System.out.println(sw.toString());
        ctx.json(sw.toString());
    };
}

package javalin.controllers;

import com.google.gson.Gson;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.annotations.*;
import javalin.models.ErrorResponse;
import javalin.models.Rfq;
import json.RequestForQuotation;
import semanticmatching.CSSemanticMatching;
import semanticmatching.IMSemanticMatching;
import validation.JSONValidator;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.util.Objects;


public class InnovationManagerSemanticMatching {
    @OpenApi(
            method = HttpMethod.GET,
            description = "This endpoint performs the Semantic matching from a given RFQ with the innovation manager",
            operationId = "PerformMatching",
            summary = "This methodc performs the semantic matching based on the RFQ with the innovation manager",
            deprecated = false,
            tags = {"Innovation manager"},

            formParams = @OpenApiFormParam(name = "rfq", type = Rfq.class),
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Rfq[].class)}),
                    @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(type = "application/json")})
            }
    )
    public static Handler performSemanticMatchingOnInnovationManager = ctx -> {
        String jsonInput = Objects.requireNonNull(ctx.formParam("rfq"));
        if (JSONValidator.isJSONValid(jsonInput)) {
            RequestForQuotation rfq = new Gson().fromJson(jsonInput, RequestForQuotation.class);
            //if (rfq.customer == null) {
             //   throw new BadRequestResponse("Invalid customer info. Please insert a valid customer in the request for quotation");
            //}
        }
        StringWriter sw = new StringWriter();
        BufferedWriter writer = new BufferedWriter(sw);

        IMSemanticMatching.performSemanticMatching_IM(jsonInput, 10, writer, false, true, 0.9);
        System.out.println(sw.toString());
        ctx.json(sw.toString());
    };
}

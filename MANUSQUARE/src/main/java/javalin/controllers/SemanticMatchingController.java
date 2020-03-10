package javalin.controllers;

import io.javalin.http.Handler;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiFormParam;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;
import javalin.models.Rfq;
import ui.SemanticMatching_MVP;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.util.Objects;

public class SemanticMatchingController {
    @OpenApi(
            description = "This endpoint performs the Semantic matching from a given RFQ.",
            operationId = "PerformSemanticMatching",
            summary = "This methodc performs the semantic matching based on the RFQ",
            deprecated = false,
            tags = {"Semantic matching"},

            //requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Rfq.class)),
            //queryParams = @OpenApiParam(name="rfq", type=Rfq.class),
            //requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Rfq.class, type = "applicatiton/json")),
            formParams = @OpenApiFormParam(name="rfq", type = Rfq.class),
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = Rfq.class)),
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json"))
            }

    )
    public static Handler PerformSemanticMatching = ctx -> {
        String jsonInput = Objects.requireNonNull(ctx.formParam("rfq"));
        StringWriter sw = new StringWriter();
        BufferedWriter writer = new BufferedWriter(sw);

        SemanticMatching_MVP.performSemanticMatching(jsonInput, 10, writer, false, true, 0.9);
        System.out.println(sw.toString());
        ctx.json(sw.toString());
    };
}

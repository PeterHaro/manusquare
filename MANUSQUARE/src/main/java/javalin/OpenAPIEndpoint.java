package javalin;

import data.EmbeddingSingletonDataManager;
import data.SimulationDataSingletonManager;
import io.javalin.Javalin;
import io.javalin.plugin.openapi.InitialConfigurationCreator;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.jackson.JacksonModelConverterFactory;
import io.javalin.plugin.openapi.jackson.JacksonToJsonMapper;
import io.javalin.plugin.openapi.ui.ReDocOptions;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import javalin.controllers.InnovationManagerSemanticMatching;
import javalin.controllers.MatchmakingController;
import javalin.controllers.SemanticMatchingController;
import javalin.models.ErrorResponse;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;


public class OpenAPIEndpoint {
    public static void main(String[] args) {
        // seed the fake data
        SimulationDataSingletonManager manager = SimulationDataSingletonManager.getInstance(); // FLYTT VEKTOR HIT
        EmbeddingSingletonDataManager embeddingManager = EmbeddingSingletonDataManager.getInstance();


        Javalin app = Javalin.create(config -> {
            //config.enableWebjars();
            //config.addStaticFiles("/public");
            config.registerPlugin(getConfiguredOpenApiPlugin());
            config.defaultContentType = "application/json";
        }).routes(() -> {
            path("matchmaking", () -> {
                path("/getAllBuyers", () -> {
                    get(MatchmakingController::getAllBuyers);
                });
                path("/getAllSuppliers", () -> {
                    get(MatchmakingController::getAllSuppliers);
                });
                path("/getAllTransactionalData", () -> {
                    get(MatchmakingController::getAllTransactionalData);
                });
                path(":orderId", () -> {
                    get(MatchmakingController::getOffers);
                });
            });
        });

        //OpenApiDocumentation performMatchmakingDocumentation = OpenApiBuilder.document().body(Rfq.class).json("200", Rfq.class);

        app.post("/matching", SemanticMatchingController.PerformSemanticMatching);
        app.post("/matchmaking", SemanticMatchingController.PerformSemanticMatching);
        app.post("/innovationMatching", InnovationManagerSemanticMatching.performSemanticMatchingOnInnovationManager);
        app.start(7000);

    }

    private static OpenApiPlugin getConfiguredOpenApiPlugin() {
        Info info = new Info().version("1.0").title("Manusquare Matching service").description("Manusquare Matchmaking Service");
        OpenApiOptions options = new OpenApiOptions(info)
                .activateAnnotationScanningFor("javalin.controllers")
                .path("/swagger-docs") // endpoint for OpenAPI json
                .swagger(new SwaggerOptions("/swagger-ui")) // endpoint for swagger-ui
                .reDoc(new ReDocOptions("/redoc")) // endpoint for redoc
                .defaultDocumentation(doc -> {
                    doc.json("500", ErrorResponse.class);
                    doc.json("503", ErrorResponse.class);
                });
        return new OpenApiPlugin(options);
    }


    private static OpenApiOptions getOpenApiOptions() {
     /*   InitialConfigurationCreator initialConfigurationCreator = () -> new OpenAPI()
                .info(new Info().version("1.0").description("Manusquare Matchmaking Service"));
        //.addServersItem(new Server().url("http://my-server.com").description("My Server"));

        OpenApiOptions opts = new OpenApiOptions(initialConfigurationCreator)
                .path("/swagger-docs") // Activate the open api endpoint
                // .defaultDocumentation(doc -> { doc.json("500", MyError.class); }) // Lambda that will be applied to every documentation
                .activateAnnotationScanningFor("javalin.controllers") // Activate annotation scanning (Required for annotation api with static java methods)
                .toJsonMapper(JacksonToJsonMapper.INSTANCE) // Custom json mapper
                .modelConverterFactory(JacksonModelConverterFactory.INSTANCE) // Custom OpenApi model converter
                .swagger(new SwaggerOptions("/swagger").title("Manusquare matchmaking")) // Activate the swagger ui
                .reDoc(new ReDocOptions("/redoc").title("Manusquare matchmaking ReDoc Documentation"));// Active the ReDoc UI
        return opts;*/
     return null;
    }
}


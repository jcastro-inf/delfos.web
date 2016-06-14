/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delfos.web.database.ratings;

import delfos.CommandLineParametersError;
import delfos.ConsoleParameters;
import delfos.Constants;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import delfos.main.managers.database.DatabaseManager;
import static delfos.web.Configuration.DATABASE_CONFIG_FILE;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author jcastro
 */
@Path("/Database/PrintRatings")
public class PrintRatings {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getAsText() {
        return getAsJson().toString();

    }

    // This method is called if HTML is request
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonValue getAsJson() {
        Constants.setExitOnFail(false);
        return getRatingsJson();
    }

    public JsonValue getRatingsJson() {
        ChangeableDatasetLoader changeableDatasetHandler;
        try {
            ConsoleParameters consoleParameters = ConsoleParameters.parseArguments(
                    DatabaseManager.MODE_PARAMETER,
                    DatabaseManager.MANAGE_RATING_DATABASE_CONFIG_XML, DATABASE_CONFIG_FILE);

            changeableDatasetHandler = DatabaseManager.extractChangeableDatasetHandler(consoleParameters);

        } catch (CommandLineParametersError ex) {
            return Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Malformed command line parameters")
                    .build();
        }

        JsonArrayBuilder users = Json.createArrayBuilder();
        changeableDatasetHandler.getUsersDataset().stream().forEach(user -> {

            JsonObjectBuilder userBuilder = Json.createObjectBuilder();

            userBuilder.add("idUser", user.getId());

            JsonArrayBuilder userRatingsBuilder = Json.createArrayBuilder();
            changeableDatasetHandler.getRatingsDataset().getUserRatingsRated(user.getId()).values()
                    .forEach(rating -> {
                        JsonObjectBuilder ratingBuilder = Json.createObjectBuilder();
                        ratingBuilder.add("idItem", rating.getItem().getId());
                        ratingBuilder.add("ratingValue", rating.getRatingValue().doubleValue());

                        userRatingsBuilder.add(ratingBuilder.build());
                    });

            userBuilder.add("ratings", userRatingsBuilder.build());
            users.add(userBuilder.build());
        });

        return users.build();
    }
}

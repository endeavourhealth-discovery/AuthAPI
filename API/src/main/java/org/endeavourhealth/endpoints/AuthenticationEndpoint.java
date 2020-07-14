package org.endeavourhealth.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.http.HttpStatus;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.models.UserAuth;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.client.Entity.form;

@Path("/authenticate")
@Api(description = "API endpoint related to the Cohorts")
public final class AuthenticationEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationEndpoint.class);


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="AuthAPI.CohortEndpoint.Post")
    @Path("/$gettoken")
    @ApiOperation(value = "Get a valid keycloak token for subsequent requests")
    @RequiresAdmin
    public Response getToken(@ApiParam(value = "Body of the request") UserAuth authBody
    ) throws Exception {

        LOG.info("Auth getToken API request received ");

        System.out.println(authBody.getClientSecret());

        clearLogbackMarkers();

        return processGetTokenRequest(authBody);
    }

    private Response processGetTokenRequest(UserAuth authBody) {
        try {
            Client client = ClientBuilder.newClient();
            String url = "https://devauth.discoverydataservice.net/";
            String path = "auth/realms/endeavour2/protocol/openid-connect/token";

            WebTarget target = client.target(url).path(path);

            try {
                Form form = null;

                form = new Form()
                        .param("grant_type", authBody.getGrantType())
                        .param("client_id", authBody.getClientId())
                        .param("client_secret", authBody.getClientSecret());

                Response response = target
                        .request()
                        .post(form(form));

                if (response.getStatus() == HttpStatus.SC_OK) { // user is authenticated in keycloak, so get the user's access token
                    String loginResponse = response.readEntity(String.class);
                    JSONParser parser = new JSONParser();

                    JSONObject jsonobj = (JSONObject) parser.parse(loginResponse);
                    jsonobj.remove("refresh_token");
                    jsonobj.remove("refresh_expires_in");

                    return Response
                            .ok()
                            .entity(jsonobj)
                            .build();
                } else { // user is not authenticated in Keycloak
                    throw new RuntimeException("Unauthorized Client: "+authBody.getClientId()+", httpStatus: "+response.getStatus()); // Not authenticated so send 401/403 Unauthorized response to the client
                }

            } catch (Exception ex) {
                throw new RuntimeException("Keycloak error: "+ex.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("Resource error:" + e);
        }
    }


}


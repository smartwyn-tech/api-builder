package io.apicurio.hub.api.rest;

import io.apicurio.hub.api.beans.CodegenSettings;
import io.apicurio.hub.core.exceptions.NotFoundException;
import io.apicurio.hub.core.exceptions.ServerError;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * The interface that defines how to interact with API Code in the hub API.
 * 
 * @author usman.shb013@gmail.com
 */
@Path("code")
public interface ICodeResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("{designId}")
    public Response getCode(@PathParam("designId") String designId, Optional<CodegenSettings> codegenSettings) throws ServerError, NotFoundException;

}

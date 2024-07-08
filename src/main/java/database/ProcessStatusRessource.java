/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package database;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path("ProcessStatus") // Base path for all status endpoints
@Produces("application/json")
@Consumes("application/json")
public class ProcessStatusRessource {

    private static final Logger LOGGER = Logger.getLogger(ProcessStatusRessource.class.getName());

    @GET
    public List<ProcessStatus> list() {
        return ProcessStatus.listAll();
    }

    @GET
    @Path("/{id}")
    public ProcessStatus get(String id) {
        return ProcessStatus.findById(id);
    }

    @POST
    @Transactional
    public Response create(ProcessStatus processStatus) {

        ProcessStatus entity = ProcessStatus.findById(processStatus.id);

        if (entity != null) {
            throw new EntityExistsException("BusinessKey existiert bereits");
        }
        processStatus.created = (new Date(System.currentTimeMillis()));
        processStatus.updated = (new Date(System.currentTimeMillis()));
        processStatus.persist();
        return Response.created(URI.create("/ProcessStatus/" + processStatus.id)).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public ProcessStatus update(String id, ProcessStatus processStatus) {
        ProcessStatus entity = ProcessStatus.findById(id);

        if (entity == null) {
            throw new NotFoundException();
        }
        // map all fields from the person parameter to the existing entity
        entity.status = processStatus.status;
        entity.substatus = processStatus.substatus;
        entity.updated = (new Date(System.currentTimeMillis()));
        return entity;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public void delete(String id) {
        ProcessStatus entity = ProcessStatus.findById(id);
        if (entity == null) {
            throw new NotFoundException();
        }
        entity.delete();
    }

}

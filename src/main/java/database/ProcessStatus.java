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

import java.util.Date;
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.QueryHint;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@Entity
@NamedQuery(name = "ProcessStatus.findAll", query = "SELECT f FROM ProcessStatus f ORDER BY f.id", hints = @QueryHint(name = "org.hibernate.cacheable", value = "true"))
@Cacheable
@Table(name = "ProcessStatus")
public class ProcessStatus extends PanacheEntityBase {

    @Id
    public String id;

    //@Column(name = "status", nullable = false)
    public Integer status;

    // @Column(name = "substatus", nullable = false)
    public Integer substatus;

    //@CreationTimestamp
    //@Column(name = "created", nullable = false)
    public Date created;

    //@UpdateTimestamp
    // @Column(name = "updated", nullable = false)
    public Date updated;

    @Transactional
    public static ProcessStatus findById(String id) {
        return find("id", id).firstResult();
    }

    public static List<ProcessStatus> findStatusComplete() {
        return list("status", "5");
    }

    public static void deleteIDs(String id) {
        delete("id", id);
    }

    @Transactional
    public static ProcessStatus create(ProcessStatus processStatus) {

        ProcessStatus entity = ProcessStatus.findById(processStatus.id);

        if (entity != null) {
            throw new EntityExistsException("BusinessKey existiert bereits");
        }
        processStatus.created = (new Date(System.currentTimeMillis()));
        processStatus.updated = (new Date(System.currentTimeMillis()));
        processStatus.persist();
        return entity;

    }

    @Transactional
    public static ProcessStatus update(String id, ProcessStatus processStatus) {
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

}

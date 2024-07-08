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
import java.util.HashMap;
import java.util.Map;

import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.kie.api.event.process.ProcessNodeEvent;
import org.mvel2.MVEL;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityExistsException;

public class ProcessStatusListenerService {

    public static void UpdateProcessStatus(ProcessNodeEvent event, Map<String, Object> metadata) {
        String BusinessKey = SearchBusinessKey(event, metadata);

        //alle drei Properties müssen übergeben werden -> Status, SubStatus und Businesskey (ist der DB Key). 
        //Sofern Status nicht übergeben wird, wird alles übersprungen da davon ausgegangen wird das ein Status gespeichert wird. 

        if (metadata.containsKey("Status")) {

            if (metadata.get("Status").toString().isEmpty() || (metadata.get("Status").toString().isBlank())) {
                throw new RuntimeException("Status ist angebeben aber leer");
            }

            if (metadata.containsKey("SubStatus")) {

                if (metadata.get("SubStatus").toString().isEmpty() || (metadata.get("SubStatus").toString().isBlank())) {
                    throw new RuntimeException("SubStatus ist angebeben aber leer");
                }

                if (SearchBusinessKey(event, metadata) == null) {
                    throw new RuntimeException("Kein Businesskey vorhanden");
                } else {

                    //ORM Methode aufrufen 
                    ProcessStatus entity = ProcessStatus.findById(BusinessKey);

                    if (entity == null) {
                        throw new RuntimeException("Kein Datenbankeintrag zum Businesskey gefunden");
                    }
                    //Entity updaten und persitieren
                    entity.status = Integer.parseInt(metadata.get("Status").toString());
                    entity.substatus = Integer.parseInt(metadata.get("SubStatus").toString());
                    entity.updated = (new Date(System.currentTimeMillis()));
                    ProcessStatus.update(BusinessKey, entity);
                }
            }
        }
    }

    public static String SearchBusinessKey(ProcessNodeEvent event, Map<String, Object> metadata) {

        RuleFlowProcessInstance processInstance = (RuleFlowProcessInstance) event.getNodeInstance().getProcessInstance();

        //CorrelationKey ist technisch der Businesskey den man z.B. über den Webservice setzen kann 
        if (processInstance.getCorrelationKey() == null || processInstance.getCorrelationKey().isEmpty() || (processInstance.getCorrelationKey().isBlank())) {

            //es gibt zwar diese Methode, aber ich weiß nicht ob die jemals befüllt ist. Hab sie aber implementiert vorsichtshalber
            if (processInstance.getBusinessKey() == null || processInstance.getBusinessKey().isEmpty() || (processInstance.getBusinessKey().isBlank())) {

                //  wenn beides nicht befüllt ist (Correlation oder Businesskey) dann noch überprüfen ob der Businesskey in den Metadaten mit übergeben wurde als "BusinessKey"
                if (metadata.get("BusinessKey") == null || metadata.get("BusinessKey").toString().isEmpty() || (metadata.get("BusinessKey").toString().isBlank())) {
                    return null;
                } else {
                    return metadata.get("BusinessKey").toString();
                }
            } else {
                return processInstance.getBusinessKey();
            }
        } else {
            return processInstance.getCorrelationKey();
        }
    }

    public static void SetBusinessKey(ProcessNodeEvent event, Map<String, Object> metadata) {

        RuleFlowProcessInstance processInstance = (RuleFlowProcessInstance) event.getNodeInstance().getProcessInstance();
        String BusinessKey = SearchBusinessKey(event, metadata);

        if (BusinessKey == null || BusinessKey.isEmpty() || BusinessKey.isBlank()) {
            BusinessKey = metadata.get("setBusinessKey").toString();
        } else {
            System.out.println("Methode setBusinessKey wurde übersprungen, da der Busineskey bereits gesetzt wurde");
            return;
        }

        if (BusinessKey.startsWith("#{")) {

            String Expression = BusinessKey;
            Expression = Expression.replace("#{", "");
            Expression = Expression.replace("}", "");

            int iend = Expression.indexOf(".");
            String VariableName = new String();
            ObjectMapper mapper = new ObjectMapper();
            Object result;

            if (iend != -1) {

                VariableName = Expression.substring(0, iend);

                Map<String, Object> map = mapper.convertValue(processInstance.getVariable(VariableName), new TypeReference<Map<String, Object>>() {
                });
                Map<String, Object> finalmap = new HashMap();
                finalmap.put(VariableName, map);

                //mvel expression
                result = MVEL.eval(Expression, finalmap);
                processInstance.setCorrelationKey(result.toString());

            } else {

                Map<String, Object> map = mapper.convertValue(processInstance.getVariable(VariableName), new TypeReference<Map<String, Object>>() {
                });
                Map<String, Object> finalmap = new HashMap();
                finalmap.put(VariableName, map);
                processInstance.setCorrelationKey(BusinessKey);
                result = MVEL.eval(Expression, finalmap);
                processInstance.setCorrelationKey(result.toString());
            }

        } else {
            processInstance.setCorrelationKey(BusinessKey);
        }
    }

    public static void CurrentProcessStatusToVariable(ProcessNodeEvent event, Map<String, Object> metadata) {

        RuleFlowProcessInstance processInstance = (RuleFlowProcessInstance) event.getNodeInstance().getProcessInstance();
        String BusinessKey = SearchBusinessKey(event, metadata);
        ProcessStatus entity = ProcessStatus.findById(BusinessKey);

        if (entity == null) {
            throw new RuntimeException("Kein Datenbankeintrag zum Businesskey gefunden");
        }

        if (processInstance.getVariables().containsKey("fallStatus")) {

            FallStatus fallStatus = new FallStatus();
            fallStatus.status = entity.status;
            fallStatus.substatus = entity.substatus;
            processInstance.setVariable("fallStatus", fallStatus);
        } else {
            throw new RuntimeException("Die Variable-> fallStatus mit typ database.FallStatus <- muss für diese Methode gesetzt sein");
        }

    }

    public static void InitProcessStatus(ProcessNodeEvent event, Map<String, Object> metadata) {

        String BusinessKey = SearchBusinessKey(event, metadata);
        ProcessStatus processStatus = new ProcessStatus();
        ProcessStatus entity = ProcessStatus.findById(BusinessKey);
        RuleFlowProcessInstance processInstance = (RuleFlowProcessInstance) event.getNodeInstance().getProcessInstance();

        Integer status = 0;
        Integer substatus = 0;

        if (entity != null) {
            if (metadata.get("InitFallStatus").toString() == "CatchEntityExistsException") {
                System.out.println(
                        "EntityExistsException wurde durch CatchEntityExistsException in Metadata InitFallStatus abgefangen. Ursache: es ist bereits eine ID der Process_Status Tabelle enthalten");
                return;
            }
            throw new EntityExistsException("BusinessKey existiert bereits");
        }

        if (processInstance.getVariables().containsKey("fallStatus")) {
            if (metadata.get("Status") == null || metadata.get("Status").toString().isEmpty() || (metadata.get("Status").toString().isBlank())) {
                status = 0;
            } else {
                status = Integer.parseInt(metadata.get("Status").toString());
            }
        }

        if (processInstance.getVariables().containsKey("fallStatus")) {
            if (metadata.get("SubStatus") == null || metadata.get("SubStatus").toString().isEmpty() || (metadata.get("SubStatus").toString().isBlank())) {
                substatus = 0;
            } else {
                substatus = Integer.parseInt(metadata.get("SubStatus").toString());
            }
        }

        processStatus.id = BusinessKey;
        processStatus.status = status;
        processStatus.substatus = substatus;
        ProcessStatus.create(processStatus);
    }

}

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

public class FallStatus {

    public Integer status;
    public Integer substatus;
   

    // Getter für status
    public Integer getStatus() {
        return status;
    }

    // Setter für status
    public void setStatus(Integer status) {
        this.status = status;
    }

    // Getter für substatus
    public Integer getSubstatus() {
        return substatus;
    }

    // Setter für substatus
    public void setSubstatus(Integer substatus) {
        this.substatus = substatus;
    }

}

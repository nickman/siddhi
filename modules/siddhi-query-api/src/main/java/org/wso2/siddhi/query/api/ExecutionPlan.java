/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.siddhi.query.api;

import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.FunctionDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.exception.DuplicateDefinitionException;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.exception.FunctionAlreadyExistException;
import org.wso2.siddhi.query.api.execution.ExecutionElement;
import org.wso2.siddhi.query.api.execution.partition.Partition;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.util.AnnotationHelper;
import org.wso2.siddhi.query.api.util.SiddhiConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecutionPlan {

    private Map<String, StreamDefinition> streamDefinitionMap = new HashMap<String, StreamDefinition>();
    private Map<String, TableDefinition> tableDefinitionMap = new HashMap<String, TableDefinition>();
    private List<ExecutionElement> executionElementList = new ArrayList<ExecutionElement>();
    private List<String> executionElementNameList = new ArrayList<String>();
    private List<Annotation> annotations = new ArrayList<Annotation>();

    public Map<String, FunctionDefinition> getFunctionDefinitionMap() {
        return functionDefinitionMap;
    }

    private Map<String, FunctionDefinition> functionDefinitionMap = new HashMap<String, FunctionDefinition>();

    public ExecutionPlan(String name) {
        annotations.add(Annotation.annotation("info").element("name", name));
    }

    public ExecutionPlan(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public ExecutionPlan() {
    }

    public static ExecutionPlan executionPlan(String name) {
        return new ExecutionPlan(name);
    }

    public static ExecutionPlan executionPlan() {
        return new ExecutionPlan();
    }

    public ExecutionPlan defineStream(StreamDefinition streamDefinition) {
        if (streamDefinition == null) {
            throw new ExecutionPlanValidationException("Stream Definition should not be null");
        } else if (streamDefinition.getId() == null) {
            throw new ExecutionPlanValidationException("Stream Id should not be null for Stream Definition");
        }
        checkDuplicateDefinition(streamDefinition);
        this.streamDefinitionMap.put(streamDefinition.getId(), streamDefinition);
        return this;
    }

    public ExecutionPlan removeStream(String streamId) {
        if (streamId == null) {
            throw new ExecutionPlanValidationException("Stream Id should not be null");
        }
        this.streamDefinitionMap.remove(streamId);
        return this;
    }

    public ExecutionPlan defineTable(TableDefinition tableDefinition) {
        if (tableDefinition == null) {
            throw new ExecutionPlanValidationException("Table Definition should not be null");
        } else if (tableDefinition.getId() == null) {
            throw new ExecutionPlanValidationException("Table Id should not be null for Table Definition");
        }
        checkDuplicateDefinition(tableDefinition);
        this.tableDefinitionMap.put(tableDefinition.getId(), tableDefinition);
        return this;
    }

    private void checkDuplicateDefinition(AbstractDefinition definition) {
        TableDefinition existingTableDefinition = tableDefinitionMap.get(definition.getId());
        if (existingTableDefinition != null && (!existingTableDefinition.equals(definition) || definition instanceof StreamDefinition)) {
            throw new DuplicateDefinitionException("Table Definition with same Stream Id '" +
                    definition.getId() + "' already exist : " + existingTableDefinition +
                    ", hence cannot add " + definition);
        }
        StreamDefinition existingStreamDefinition = streamDefinitionMap.get(definition.getId());
        if (existingStreamDefinition != null && (!existingStreamDefinition.equals(definition) || definition instanceof TableDefinition)) {
            throw new DuplicateDefinitionException("Stream Definition with same Stream Id '" +
                    definition.getId() + "' already exist : " + existingStreamDefinition +
                    ", hence cannot add " + definition);
        }
    }

    public ExecutionPlan addQuery(Query query) {
        if (query == null) {
            throw new ExecutionPlanValidationException("Query should not be null");
        }
        String name = null;
        Element element = AnnotationHelper.getAnnotationElement(SiddhiConstants.ANNOTATION_INFO, SiddhiConstants.ANNOTATION_ELEMENT_NAME, query.getAnnotations());
        if (element != null) {
            name = element.getValue();
        }
        if (name != null && executionElementNameList.contains(name)) {
            throw new ExecutionPlanValidationException("Cannot add Query as another Execution Element already uses its name=" + name);
        }
        executionElementNameList.add(name);
        this.executionElementList.add(query);
        return this;
    }

    public ExecutionPlan addPartition(Partition partition) {
        if (partition == null) {
            throw new ExecutionPlanValidationException("Partition should not be null");
        }
        String name = null;
        Element element = AnnotationHelper.getAnnotationElement(SiddhiConstants.ANNOTATION_INFO, SiddhiConstants.ANNOTATION_ELEMENT_NAME, partition.getAnnotations());
        if (element != null) {
            name = element.getValue();
        }
        if (name != null && executionElementNameList.contains(name)) {
            throw new ExecutionPlanValidationException("Cannot add Partition as another Execution Element already uses its name=" + name);
        }
        executionElementNameList.add(name);
        this.executionElementList.add(partition);
        return this;
    }

    public ExecutionPlan annotation(Annotation annotation) {
        annotations.add(annotation);
        return this;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public List<ExecutionElement> getExecutionElementList() {
        return executionElementList;
    }

    public Map<String, StreamDefinition> getStreamDefinitionMap() {
        return streamDefinitionMap;
    }

    public Map<String, TableDefinition> getTableDefinitionMap() {
        return tableDefinitionMap;
    }

    @Override
    public String toString() {
        return "ExecutionPlan{" +
                "streamDefinitionMap=" + streamDefinitionMap +
                ", tableDefinitionMap=" + tableDefinitionMap +
                ", executionElementList=" + executionElementList +
                ", executionElementNameList=" + executionElementNameList +
                ", annotations=" + annotations +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExecutionPlan)) {
            return false;
        }

        ExecutionPlan that = (ExecutionPlan) o;

        if (annotations != null ? !annotations.equals(that.annotations) : that.annotations != null) {
            return false;
        }
        if (executionElementList != null ? !executionElementList.equals(that.executionElementList) : that.executionElementList != null) {
            return false;
        }
        if (executionElementNameList != null ? !executionElementNameList.equals(that.executionElementNameList) : that.executionElementNameList != null) {
            return false;
        }
        if (streamDefinitionMap != null ? !streamDefinitionMap.equals(that.streamDefinitionMap) : that.streamDefinitionMap != null) {
            return false;
        }
        if (tableDefinitionMap != null ? !tableDefinitionMap.equals(that.tableDefinitionMap) : that.tableDefinitionMap != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = streamDefinitionMap != null ? streamDefinitionMap.hashCode() : 0;
        result = 31 * result + (tableDefinitionMap != null ? tableDefinitionMap.hashCode() : 0);
        result = 31 * result + (executionElementList != null ? executionElementList.hashCode() : 0);
        result = 31 * result + (executionElementNameList != null ? executionElementNameList.hashCode() : 0);
        result = 31 * result + (annotations != null ? annotations.hashCode() : 0);
        return result;
    }

    public void defineFunction(FunctionDefinition functionDefinition) {
        if (functionDefinition == null) {
            throw new ExecutionPlanValidationException("Function Definition should not be null");
        } else if (functionDefinition.getFunctionID() == null) {
            throw new ExecutionPlanValidationException("Function Id should not be null for Function Definition");
        }
        checkDuplicateFunctionExist(functionDefinition);
        this.functionDefinitionMap.put(functionDefinition.getFunctionID(), functionDefinition);
    }

    private void checkDuplicateFunctionExist(FunctionDefinition functionDefinition) {
        if(this.functionDefinitionMap.get(functionDefinition.getFunctionID()) != null ) {
            throw new FunctionAlreadyExistException("The function definition with the same functionID exists " + functionDefinition.getFunctionID());
        }
    }
}

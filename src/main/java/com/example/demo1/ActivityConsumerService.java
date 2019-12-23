package com.example.demo1;


import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.task.Task;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
public interface ActivityConsumerService {
    /**
     * 流程demo
     * @return
     */
    @RequestMapping(value="/activitiDemo",method=RequestMethod.GET)
    public boolean startActivityDemo();

    List<Task> findTasksByUserId(String userId);

    void completeTask(String taskId,String userId,String result);

    //void updateBizStatus(DelegateExecution execution, String status);

    List<String> findUsersForSL(DelegateExecution execution);

    List<String> findUsersForSP(DelegateExecution execution);

    void queryProImg(String processInstanceId) throws Exception;

    /*void queryProHighLighted(String processInstanceId) throws Exception;

    List<String> getHighLightedFlows(
            ProcessDefinitionEntity processDefinitionEntity,
            List<HistoricActivityInstance> historicActivityInstances);*/



}

package com.example.demo1;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.image.ProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;


@Service("activityService")
public class ActivityConsumerServiceImpl implements ActivityConsumerService {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ProcessEngineConfigurationImpl processEngineConfiguration;

    /**
     * 启动流程  流程图id，业务表id
     * @param bizId 业务id
     */
    public void startProcesses(String bizId) {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("demo5", bizId);
        System.out.println("流程启动成功，流程id:"+pi.getId());
    }

    @Override
    public boolean startActivityDemo() {
        System.out.println("method startActivityDemo begin....");

        System.out.println( "调用流程存储服务，查询部署数量："
                + repositoryService.createDeploymentQuery().count());



        Map<String,Object> map = new HashMap<String,Object>();
        map.put("apply","zhangsan");
        map.put("approve","lisi");

        //流程启动
        ExecutionEntity pi1 = (ExecutionEntity) runtimeService.startProcessInstanceByKey("leave",map);

        List<Task> tq=taskService.createTaskQuery().taskAssignee("zhangsan").list();
        System.out.println(tq.size());
        String assignee = "zhangsan";//当前任务办理人
        List<Task> tasks = taskService//与任务相关的Service
                .createTaskQuery()//创建一个任务查询对象
                .taskAssignee(assignee)
                .list();
        if(tasks !=null && tasks.size()>0){
            for(Task task:tasks){
                System.out.println("任务ID:"+task.getId());
                System.out.println("任务的办理人:"+task.getAssignee());
                System.out.println("任务名称:"+task.getName());
                System.out.println("任务的创建时间:"+task.getCreateTime());

                System.out.println("流程实例ID:"+task.getProcessInstanceId());
                System.out.println("#####################################");
            }
        }

        System.out.println("method startActivityDemo end....");
        return false;
    }

    /**
     *
     * <p>描述: 根据用户id查询待办任务列表</p>
     * @author 范相如
     * @date 2018年2月25日
     */
    @Override
    public List<Task> findTasksByUserId(String userId) {
        List<Task> resultTask = taskService.createTaskQuery().processDefinitionKey("demo5").taskCandidateOrAssigned(userId).list();
        return resultTask;
    }

    /**
     *
     * <p>描述:任务审批 	（通过/拒接） </p>
     * @author 范相如
     * @date 2018年2月25日
     * @param taskId 任务id
     * @param userId 用户id
     * @param result false OR true
     */
    @Override
    public void completeTask(String taskId,String userId,String result) {
        //获取流程实例
        taskService.claim(taskId, userId);

        Map<String,Object> vars = new HashMap<String,Object>();
        vars.put("sign", "true");
        taskService.complete(taskId, vars);
    }

    /**
     * 更改业务流程状态#{ActivityDemoServiceImpl.updateBizStatus(execution,"tj")}
     * @param execution
     * @param //status
     */
    /*@Override
    public void updateBizStatus(DelegateExecution execution, String status) {
        String bizId = execution.getProcessBusinessKey();
        //根据业务id自行处理业务表
        System.out.println("业务表["+bizId+"]状态更改成功，状态更改为："+status);
    }*/


    //流程节点权限用户列表${ActivityDemoServiceImpl.findUsers(execution,sign)}
    @Override
    public List<String> findUsersForSL(DelegateExecution execution) {
        return Arrays.asList("sly1","sly2");
    }

    //流程节点权限用户列表${ActivityDemoServiceImpl.findUsers(execution,sign)}
    @Override
    public List<String> findUsersForSP(DelegateExecution execution){
        return Arrays.asList("spy1","uspy2");
    }

    /**
     *
     * <p>描述:  生成流程图
     * 首先启动流程，获取processInstanceId，替换即可生成</p>
     * @author 范相如
     * @date 2018年2月25日
     * @param processInstanceId
     * @throws Exception
     */
    @Override
    public void queryProImg(String processInstanceId) throws Exception {
        //获取历史流程实例
        HistoricProcessInstance processInstance =  historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        //根据流程定义获取输入流
        InputStream is = repositoryService.getProcessDiagram(processInstance.getProcessDefinitionId());
        BufferedImage bi = ImageIO.read(is);
        File file = new File("demo2.png");
        if(!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(file);
        ImageIO.write(bi, "png", fos);
        fos.close();
        is.close();
        System.out.println("图片生成成功");

        List<Task> tasks = taskService.createTaskQuery().taskCandidateUser("userId").list();
        for(Task t : tasks) {
            System.out.println(t.getName());
        }
    }


    /**
     * 流程图高亮显示
     * 首先启动流程，获取processInstanceId，替换即可生成
     * @throws Exception
     */
    /*@Override
    public void queryProHighLighted(String processInstanceId) throws Exception {
        //获取历史流程实例
        HistoricProcessInstance processInstance =  historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());

        ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
        ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity)repositoryService.getProcessDefinition(processInstance.getProcessDefinitionId());

        List<HistoricActivityInstance> highLightedActivitList =  historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).list();
        //高亮环节id集合
        List<String> highLightedActivitis = new ArrayList<String>();

        //高亮线路id集合
        List<String> highLightedFlows = getHighLightedFlows(definitionEntity,highLightedActivitList);

        for(HistoricActivityInstance tempActivity : highLightedActivitList){
            String activityId = tempActivity.getActivityId();
            highLightedActivitis.add(activityId);
        }
        //配置字体
        InputStream imageStream = diagramGenerator.generateDiagram(bpmnModel, "png", highLightedActivitis, highLightedFlows,"宋体","微软雅黑","黑体",null,2.0);
        BufferedImage bi = ImageIO.read(imageStream);
        File file = new File("demo2.png");
        if(!file.exists()) file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        ImageIO.write(bi, "png", fos);
        fos.close();
        imageStream.close();
        System.out.println("图片生成成功");
    }*/
    /**
     * 获取需要高亮的线
     * @param processDefinitionEntity
     * @param historicActivityInstances
     * @return
     */
    /*@Override
    private List<String> getHighLightedFlows(
            ProcessDefinitionEntity processDefinitionEntity,
            List<HistoricActivityInstance> historicActivityInstances) {

        List<String> highFlows = new ArrayList<String>();// 用以保存高亮的线flowId
        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {// 对历史流程节点进行遍历
            ActivityImpl activityImpl = processDefinitionEntity
                    .findActivity(historicActivityInstances.get(i)
                            .getActivityId());// 得到节点定义的详细信息
            List<ActivityImpl> sameStartTimeNodes = new ArrayList<ActivityImpl>();// 用以保存后需开始时间相同的节点
            ActivityImpl sameActivityImpl1 = processDefinitionEntity
                    .findActivity(historicActivityInstances.get(i + 1)
                            .getActivityId());
            // 将后面第一个节点放在时间相同节点的集合里
            sameStartTimeNodes.add(sameActivityImpl1);
            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
                HistoricActivityInstance activityImpl1 = historicActivityInstances
                        .get(j);// 后续第一个节点
                HistoricActivityInstance activityImpl2 = historicActivityInstances
                        .get(j + 1);// 后续第二个节点
                if (activityImpl1.getStartTime().equals(
                        activityImpl2.getStartTime())) {
                    // 如果第一个节点和第二个节点开始时间相同保存
                    ActivityImpl sameActivityImpl2 = processDefinitionEntity
                            .findActivity(activityImpl2.getActivityId());
                    sameStartTimeNodes.add(sameActivityImpl2);
                } else {
                    // 有不相同跳出循环
                    break;
                }
            }
            List<PvmTransition> pvmTransitions = activityImpl
                    .getOutgoingTransitions();// 取出节点的所有出去的线
            for (PvmTransition pvmTransition : pvmTransitions) {
                // 对所有的线进行遍历
                ActivityImpl pvmActivityImpl = (ActivityImpl) pvmTransition
                        .getDestination();
                // 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
                if (sameStartTimeNodes.contains(pvmActivityImpl)) {
                    highFlows.add(pvmTransition.getId());
                }
            }
        }
        return highFlows;
    }*/
}

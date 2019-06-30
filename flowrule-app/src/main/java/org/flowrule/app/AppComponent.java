/*
 * Copyright 2019-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* edited by hdy
如果直接使用该案例需要删去中文的注释
*/
package org.flowrule.app;

import org.apache.felix.scr.annotations.*;//导入java注解模块
import org.onlab.packet.Ethernet;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.MacAddress;
import org.onlab.packet.TpPort;//以上为导入数据包结构模块
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.*;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.slf4j.LoggerFactory.getLogger;



/** * Skeletal ONOS application component. 这里用@component 来注解这个app的组件，并表明他是立刻启动 */
@Component(immediate = true)
public class AppComponent {

    String device_pica8 = "of:d8e1486e73000247";
    private static final String APP_TEST = "org.flowrule.app";
    private final Logger log = getLogger(getClass());
    /* 这块用了@Reference，是Java的一种注解，ONOS Wiki上是这样写的：Annotations (e.g. @Reference) within the code -      interdependencies between various
services, used by Karaf to resolve module dependencies during system startup      and when loading bundles at runtime
*/
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowObjectiveService flowObjectiveService;

    private ApplicationId appId;
    private DeviceId deviceId;
    private int flowPriority = 60;
    private int flowTimeout = 10000;

    @Activate /*利用@active声明在app启用的时候直接调用该函数*/
    protected void activate() {
        log.info("Started");
        //log.info(appId.hashCode());
        sendFlowRule(1,3,device_pica8);
        sendFlowRule(7,9,device_pica8);
        sendFlowRule(33,25,device_pica8);
        sendFlowRule(43,35,device_pica8);
    }


    @Deactivate/*利用@deactive声明在app关闭的时候直接调用该函数*/
    protected void deactivate() {
        log.info("Stopped");
    }

    public void sendFlowRule(int in_port, int out_port, String deviceId_str){
        appId = coreService.registerApplication(APP_TEST);
        log.info(appId.toString());
        log.info("bulid matching");//构造匹配项
        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
        selectorBuilder.matchInPort(PortNumber.portNumber(in_port));
        /*这里不同的匹配需求可以添加相应的匹配项。需要注意的是匹配IPv4的时候一定要先匹配相应的matchEthType == Ethernet.TYPE_IPV4        其他协议同理。        */
      /* selectorBuilder.matchEthDst(MacAddress.valueOf("00:11:22:33:44:55"))
                .matchEthSrc(MacAddress.valueOf("66:55:44:33:22:11"))
                .matchEthType(Ethernet.TYPE_IPV4)
                /*  Malformed IP prefix string Address                ** must take form \"x.x.x.x/y\" or " +                ** \"xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx/y\"";*/
              /*  .matchIPSrc(IpPrefix.valueOf("10.0.0.2/24"))
                .matchIPDst(IpPrefix.valueOf("10.0.0.1/24"))
                .matchUdpSrc(TpPort.tpPort(5050));  */


        log.info("bulid treatment");
        /*构造instruction和action*/
        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .setOutput(PortNumber.portNumber(out_port))
                .build();

        log.info("bulid rule");/*构造整个flow rule的结构，这里还可以添加其他的参数，在接口里有详细说明*/

        ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                .withSelector(selectorBuilder.build())
                .withTreatment(treatment)
                .withPriority(flowPriority)
                .withFlag(ForwardingObjective.Flag.VERSATILE)
                .fromApp(appId)
                .makeTemporary(flowTimeout)
                .add();

        log.info("send rule");/*发送flow rule到指定的device ID*/

        /*此处得到device id的方法是从控制器存储的设备中（存在一个迭代器中），随机拿了一个。用户可以根据自己的需求去自己配置*/
        //deviceId = deviceService.getAvailableDevices().iterator().next().id();
        deviceId = DeviceId.deviceId(deviceId_str);
        flowObjectiveService.forward(deviceId,
                forwardingObjective);

        log.info("send rule successfully???");
    }
}

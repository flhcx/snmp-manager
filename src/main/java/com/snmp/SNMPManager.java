package com.snmp;

import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

import java.io.IOException;
import java.util.List;

public class SNMPManager {
    private Snmp snmp;
    private String targetAddress;
    private String community;

    /**
     * 构造函数：初始化SNMP管理器
     * @param targetIp 目标设备IP地址
     * @param community SNMP团体名（默认public）
     */
    public SNMPManager(String targetIp, String community) {
        this.targetAddress = "udp:" + targetIp + "/161";
        this.community = community;
        try {
            TransportMapping<?> transport = new DefaultUdpTransportMapping();
            this.snmp = new Snmp(transport);
            transport.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * SNMP GET操作：获取单个OID的值
     * @param oid 对象标识符（如1.3.6.1.2.1.1.1.0表示系统描述）
     * @return OID对应的值
     */
    public String snmpGet(String oid) {
        try {
            // 创建目标对象
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(GenericAddress.parse(targetAddress));
            target.setRetries(2);
            target.setTimeout(5000);
            target.setVersion(SnmpConstants.version2c);

            // 创建PDU请求
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid)));
            pdu.setType(PDU.GET);

            // 发送请求
            ResponseEvent response = snmp.send(pdu, target);
            if (response != null && response.getResponse() != null) {
                PDU responsePDU = response.getResponse();
                if (responsePDU.getErrorStatus() == PDU.noError) {
                    return responsePDU.get(0).getVariable().toString();
                } else {
                    return "Error: " + responsePDU.getErrorStatusText();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * SNMP SET操作：设置OID的值
     * @param oid 对象标识符
     * @param value 要设置的值
     * @return 是否成功
     */
    public boolean snmpSet(String oid, String value) {
        try {
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString("private")); // SET需要写权限
            target.setAddress(GenericAddress.parse(targetAddress));
            target.setRetries(2);
            target.setTimeout(5000);
            target.setVersion(SnmpConstants.version2c);

            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid), new OctetString(value)));
            pdu.setType(PDU.SET);

            ResponseEvent response = snmp.send(pdu, target);
            if (response != null && response.getResponse() != null) {
                return response.getResponse().getErrorStatus() == PDU.noError;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * SNMP WALK操作：遍历OID子树
     * @param rootOid 根OID（如1.3.6.1.2.1.1表示system组）
     */
    public void snmpWalk(String rootOid) {
        try {
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(GenericAddress.parse(targetAddress));
            target.setRetries(2);
            target.setTimeout(5000);
            target.setVersion(SnmpConstants.version2c);

            TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
            List<TreeEvent> events = treeUtils.getSubtree(target, new OID(rootOid));

            if (events != null) {
                for (TreeEvent event : events) {
                    if (event != null && !event.isError()) {
                        VariableBinding[] vbs = event.getVariableBindings();
                        if (vbs != null) {
                            for (VariableBinding vb : vbs) {
                                System.out.println(vb.getOid() + " = " + vb.getVariable());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭SNMP会话
     */
    public void close() {
        try {
            if (snmp != null) {
                snmp.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

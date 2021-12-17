package com.pactera.fix.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * MarketDataRequest's commit info
 */
public class MDR implements Serializable {
    private static final long serialVersionUID = 3779870409867285944L;
    private Set<RS> rsGroup=new HashSet<>();
    private String mdReqID;
    private String partyID;
    private String account;
    private Integer applSeqNum;
    private String settlType;
    private Character subscriptionRequestType;

    public Set<RS> getRsGroup() {
        return rsGroup;
    }

    public void setRsGroup(Set<RS> rsGroup) {
        this.rsGroup = rsGroup;
    }

    public String getMdReqID() {
        return mdReqID;
    }

    public void setMdReqID(String mdReqID) {
        this.mdReqID = mdReqID;
    }

    public String getPartyID() {
        return partyID;
    }

    public void setPartyID(String partyID) {
        this.partyID = partyID;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Integer getApplSeqNum() {
        return applSeqNum;
    }

    public void setApplSeqNum(Integer applSeqNum) {
        this.applSeqNum = applSeqNum;
    }

    public String getSettlType() {
        return settlType;
    }

    public void setSettlType(String settlType) {
        this.settlType = settlType;
    }

    public Character getSubscriptionRequestType() {
        return subscriptionRequestType;
    }

    public void setSubscriptionRequestType(Character subscriptionRequestType) {
        this.subscriptionRequestType = subscriptionRequestType;
    }

    @Override
    public String toString() {
        return "MarketDataRequest{" +
                "rsGroup=" + rsGroup +
                ", mdReqID='" + mdReqID + '\'' +
                ", partyID='" + partyID + '\'' +
                ", account='" + account + '\'' +
                ", applSeqNum=" + applSeqNum +
                ", settlType='" + settlType + '\'' +
                ", subscriptionRequestType='" + subscriptionRequestType + '\'' +
                '}';
    }
}

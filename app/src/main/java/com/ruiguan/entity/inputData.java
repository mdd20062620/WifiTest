package com.ruiguan.entity;

public class inputData {

    private String str_company;
    private String str_number;
    private String str_type;

    private String str_companyMB;
    private String str_humanB;

    private String str_ratedspeed;
    private String str_ratedLoad;//平衡系数
    private String str_control;
    private String str_machineType;
    private String str_ratio;

    private String str_direct;
    private String str_addLoad;
    private String str_runspeed;

    private String str_ropeTotal;//钢丝绳张紧力
    private String str_ropeCur;
    private String str_ropeDir;

    private String str_speedMax;
    private boolean speedFlag;
    private boolean speedFlagUn;

    private String str_downDisMax;
    private String str_upDisMax;

    public String getSpeedMax() {
        return str_speedMax;
    }
    public void setSpeedMax(String str_speedMax) {
        this.str_speedMax=str_speedMax;
    }

    public String getCom() {
        return str_company;
    }
    public void setCom(String str_company) {
        this.str_company=str_company;
    }

    public String getNumber() {
        return str_number;
    }
    public void setNumber(String str_number) {
        this.str_number=str_number;
    }

    public String getType() {
        return str_type;
    }
    public void setType(String str_type) {
        this.str_type=str_type;
    }

    public String getCompanyMB() {
        return str_companyMB;
    }
    public void setCompanyMB(String str_companyMB) { this.str_companyMB=str_companyMB; }

    public String getHumanB() {
        return str_humanB;
    }
    public void setHumanB(String str_humanB) {
        this.str_humanB=str_humanB;
    }

    public String getRatedspeed() {
        return str_ratedspeed;
    }
    public void setRatedspeed(String str_ratedspeed) {
        this.str_ratedspeed=str_ratedspeed;
    }

    public String getRatedLoad() {
        return str_ratedLoad;
    }//平衡系数
    public void setRatedLoad(String str_ratedLoad) {
        this.str_ratedLoad=str_ratedLoad;
    }

    public String getControl() {
        return str_control;
    }
    public void setControl(String str_control) {
        this.str_control=str_control;
    }

    public String getMachineType() {
        return str_machineType;
    }
    public void setMachineType(String str_machineType) {
        this.str_machineType=str_machineType;
    }

    public String getRatio() {
        return str_ratio;
    }
    public void setRatio(String str_ratio) {
        this.str_ratio=str_ratio;
    }

    public String getdirect() {
        return str_direct;
    }
    public void setdirect(String str_direct) {
        this.str_direct=str_direct;
    }

    public String getAddLoad() {
        return str_addLoad;
    }
    public void setAddLoad(String str_addLoad) {
        this.str_addLoad=str_addLoad;
    }

    public boolean getSpeed() {
        return speedFlag;
    }
    public void setSpeed(boolean speedFlag) {
        this.speedFlag=speedFlag;
    }

    public boolean getSpeedUn() {
        return speedFlagUn;
    }
    public void setSpeedUn(boolean speedFlagUn) {
        this.speedFlagUn=speedFlagUn;
    }

    public String getRopeTotal() {
        return str_ropeTotal;
    }//钢丝绳张紧力
    public void setRopeTotal(String str_RopeTotal) {
        this.str_ropeTotal=str_RopeTotal;
    }

    public String getRopeCur() {
        return str_ropeCur;
    }
    public void setRopeCur(String str_ropeCur) {
        this.str_ropeCur=str_ropeCur;
    }

    public String getRopeDir() {
        return str_ropeDir;
    }
    public void setRopeDir(String str_ropeDir) {
        this.str_ropeDir=str_ropeDir;
    }

    public String getUpDisMax() {
        return str_upDisMax;
    }
    public void setUpDisMax(String str_upDisMax) {
        this.str_upDisMax=str_upDisMax;
    }

    public String getDownDisMax() {
        return str_downDisMax;
    }
    public void setDownDisMax(String str_downDisMax) {
        this.str_downDisMax=str_downDisMax;
    }
    public String getRunspeed() {
        return str_runspeed;
    }
    public void setRunspeed(String str_runspeed) {
        this.str_runspeed=str_runspeed;
    }
}

package com.candyacademia.spsslite;
import javax.swing.table.DefaultTableModel;
public final class SampleData{
 private SampleData(){}
 public static DefaultTableModel survey(){String[]h={"ID","Age","Satisfaction","ServiceQuality","Loyalty","Group"};Object[][]d={{1,22,3,4,3,"Control"},{2,29,4,5,4,"Control"},{3,34,2,3,2,"Control"},{4,41,5,5,5,"Treatment"},{5,38,4,4,5,"Treatment"},{6,27,3,4,4,"Treatment"},{7,45,5,5,5,"Treatment"},{8,31,2,2,3,"Control"}};return new DefaultTableModel(d,h);}
 public static DefaultTableModel survival(){String[]h={"Patient","Time","Event","Treatment","Age","RiskScore"};Object[][]d={{1,5,1,"A",54,.72},{2,8,0,"A",48,.35},{3,12,1,"A",67,.81},{4,16,0,"A",59,.44},{5,4,1,"B",62,.88},{6,10,1,"B",51,.61},{7,15,0,"B",46,.29},{8,20,0,"B",43,.20}};return new DefaultTableModel(d,h);}
 public static DefaultTableModel timeSeries(){String[]h={"Period","Sales","Advertising","Demand"};Object[][]d=new Object[24][4];for(int i=0;i<24;i++){d[i][0]=i+1;d[i][1]=120+4*i+15*Math.sin(i*Math.PI/6);d[i][2]=20+i%6*2;d[i][3]=80+3*i+8*Math.cos(i*Math.PI/6);}return new DefaultTableModel(d,h);}
}

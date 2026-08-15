package com.candyacademia.spsslite;
import java.util.*;
public final class PhaseSixStatistics{
 private PhaseSixStatistics(){}
 public record Friedman(int subjects,int conditions,double chiSquare,int df,double p,double kendallsW,double[]meanRanks){}
 public record McNemar(int discordant01,int discordant10,double chiSquare,double p){}
 public static Friedman friedman(double[][]x){if(x.length<2||x[0].length<3)throw new IllegalArgumentException("At least 2 subjects and 3 related conditions are required.");int n=x.length,k=x[0].length;double[]sum=new double[k];for(double[]row:x){if(row.length!=k)throw new IllegalArgumentException("Rows have inconsistent lengths.");double[]rank=ranks(row);for(int j=0;j<k;j++)sum[j]+=rank[j];}double q=0;for(double s:sum)q+=s*s;q=12*q/(n*k*(k+1))-3*n*(k+1);double[]means=new double[k];for(int j=0;j<k;j++)means[j]=sum[j]/n;return new Friedman(n,k,q,k-1,Statistics.chiSquareSurvival(q,k-1),q/(n*(k-1)),means);}
 public static McNemar mcnemar(int[]a,int[]b){if(a.length!=b.length||a.length<2)throw new IllegalArgumentException("Binary pairs must have matching observations.");int b01=0,b10=0;for(int i=0;i<a.length;i++){if((a[i]!=0&&a[i]!=1)||(b[i]!=0&&b[i]!=1))throw new IllegalArgumentException("Both measurements must be coded 0 and 1.");if(a[i]==0&&b[i]==1)b01++;if(a[i]==1&&b[i]==0)b10++;}int d=b01+b10;double chi=d==0?0:Math.pow(Math.abs(b01-b10)-1,2)/d;return new McNemar(b01,b10,chi,Statistics.chiSquareSurvival(chi,1));}
 private static double[]ranks(double[]a){Integer[]idx=new Integer[a.length];for(int i=0;i<a.length;i++)idx[i]=i;Arrays.sort(idx,Comparator.comparingDouble(i->a[i]));double[]r=new double[a.length];for(int i=0;i<a.length;){int j=i+1;while(j<a.length&&Double.compare(a[idx[i]],a[idx[j]])==0)j++;double rank=(i+1+j)/2.0;for(int h=i;h<j;h++)r[idx[h]]=rank;i=j;}return r;}
}

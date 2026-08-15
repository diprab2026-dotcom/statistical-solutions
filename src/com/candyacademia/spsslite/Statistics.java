package com.candyacademia.spsslite;

import java.util.*;

public final class Statistics {
    private Statistics() {}
    public record Desc(int n, int missing, double mean, double median, double sd, double min, double max) {}
    public record Regression(int n, double intercept, double slope, double r, double r2, double se, double t, double p) {}
    public record TTest(int n1, int n2, double mean1, double mean2, double t, double df, double p) {}
    public record ChiSquare(double statistic, int df, double p, String[] rows, String[] cols, int[][] observed) {}

    public static Desc describe(List<Double> values, int total) {
        double[] a = values.stream().filter(Objects::nonNull).mapToDouble(Double::doubleValue).sorted().toArray();
        if (a.length == 0) return new Desc(0, total, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        double sum = Arrays.stream(a).sum(), mean = sum / a.length;
        double ss = 0; for (double v : a) ss += (v-mean)*(v-mean);
        double median = a.length%2==1 ? a[a.length/2] : (a[a.length/2-1]+a[a.length/2])/2;
        return new Desc(a.length, total-a.length, mean, median, a.length>1?Math.sqrt(ss/(a.length-1)):0, a[0], a[a.length-1]);
    }

    public static double correlation(double[] x, double[] y) {
        int n=x.length; if(n<2) return Double.NaN;
        double sx=0,sy=0; for(int i=0;i<n;i++){sx+=x[i];sy+=y[i];}
        double mx=sx/n,my=sy/n,num=0,dx=0,dy=0;
        for(int i=0;i<n;i++){double a=x[i]-mx,b=y[i]-my;num+=a*b;dx+=a*a;dy+=b*b;}
        return num/Math.sqrt(dx*dy);
    }

    public static Regression regression(double[] x, double[] y) {
        int n=x.length; if(n<3) throw new IllegalArgumentException("At least 3 paired observations are required.");
        double mx=Arrays.stream(x).average().orElseThrow(), my=Arrays.stream(y).average().orElseThrow();
        double sxx=0,sxy=0; for(int i=0;i<n;i++){sxx+=(x[i]-mx)*(x[i]-mx);sxy+=(x[i]-mx)*(y[i]-my);}
        if(sxx==0) throw new IllegalArgumentException("The predictor has no variation.");
        double slope=sxy/sxx, intercept=my-slope*mx, sse=0;
        for(int i=0;i<n;i++){double e=y[i]-(intercept+slope*x[i]);sse+=e*e;}
        double se=Math.sqrt(sse/(n-2)), slopeSe=se/Math.sqrt(sxx), t=slope/slopeSe;
        double r=correlation(x,y), p=twoTailedTP(t,n-2);
        return new Regression(n,intercept,slope,r,r*r,se,t,p);
    }

    public static TTest welch(double[] a,double[] b){
        if(a.length<2||b.length<2) throw new IllegalArgumentException("Each group needs at least 2 observations.");
        double m1=Arrays.stream(a).average().orElseThrow(),m2=Arrays.stream(b).average().orElseThrow();
        double v1=variance(a,m1),v2=variance(b,m2), q1=v1/a.length,q2=v2/b.length;
        double t=(m1-m2)/Math.sqrt(q1+q2), df=(q1+q2)*(q1+q2)/(q1*q1/(a.length-1)+q2*q2/(b.length-1));
        return new TTest(a.length,b.length,m1,m2,t,df,twoTailedTP(t,df));
    }
    private static double variance(double[] a,double m){double s=0;for(double v:a)s+=(v-m)*(v-m);return s/(a.length-1);}

    public static ChiSquare chiSquare(List<String> rowValues,List<String> colValues){
        List<String> rs=new ArrayList<>(new TreeSet<>(rowValues)), cs=new ArrayList<>(new TreeSet<>(colValues));
        int[][] o=new int[rs.size()][cs.size()]; for(int i=0;i<rowValues.size();i++)o[rs.indexOf(rowValues.get(i))][cs.indexOf(colValues.get(i))]++;
        int[] rt=new int[rs.size()],ct=new int[cs.size()];int n=0;for(int i=0;i<rs.size();i++)for(int j=0;j<cs.size();j++){rt[i]+=o[i][j];ct[j]+=o[i][j];n+=o[i][j];}
        double stat=0;for(int i=0;i<rs.size();i++)for(int j=0;j<cs.size();j++){double e=(double)rt[i]*ct[j]/n;if(e>0)stat+=(o[i][j]-e)*(o[i][j]-e)/e;}
        int df=(rs.size()-1)*(cs.size()-1);return new ChiSquare(stat,df,chiSquareSurvival(stat,df),rs.toArray(String[]::new),cs.toArray(String[]::new),o);
    }

    public static double twoTailedTP(double t,double df){return regularizedBeta(df/(df+t*t),df/2,0.5);}
    public static double chiSquareSurvival(double x,int df){return regularizedGammaQ(df/2.0,x/2.0);}
    private static double regularizedBeta(double x,double a,double b){
        if(x<=0)return 0;if(x>=1)return 1;double bt=Math.exp(logGamma(a+b)-logGamma(a)-logGamma(b)+a*Math.log(x)+b*Math.log1p(-x));
        return x<(a+1)/(a+b+2)?bt*betaFraction(x,a,b)/a:1-bt*betaFraction(1-x,b,a)/b;
    }
    private static double betaFraction(double x,double a,double b){double qab=a+b,qap=a+1,qam=a-1,c=1,d=1-qab*x/qap;if(Math.abs(d)<1e-30)d=1e-30;d=1/d;double h=d;for(int m=1;m<=200;m++){int m2=2*m;double aa=m*(b-m)*x/((qam+m2)*(a+m2));d=1+aa*d;if(Math.abs(d)<1e-30)d=1e-30;c=1+aa/c;if(Math.abs(c)<1e-30)c=1e-30;d=1/d;h*=d*c;aa=-(a+m)*(qab+m)*x/((a+m2)*(qap+m2));d=1+aa*d;if(Math.abs(d)<1e-30)d=1e-30;c=1+aa/c;if(Math.abs(c)<1e-30)c=1e-30;d=1/d;double del=d*c;h*=del;if(Math.abs(del-1)<3e-12)break;}return h;}
    private static double regularizedGammaQ(double a,double x){if(x<0||a<=0)return Double.NaN;if(x==0)return 1;if(x<a+1){double ap=a,sum=1/a,del=sum;for(int n=1;n<200;n++){ap++;del*=x/ap;sum+=del;if(Math.abs(del)<Math.abs(sum)*1e-14)break;}return 1-sum*Math.exp(-x+a*Math.log(x)-logGamma(a));}double b=x+1-a,c=1/1e-30,d=1/b,h=d;for(int i=1;i<200;i++){double an=-i*(i-a);b+=2;d=an*d+b;if(Math.abs(d)<1e-30)d=1e-30;c=b+an/c;if(Math.abs(c)<1e-30)c=1e-30;d=1/d;double del=d*c;h*=del;if(Math.abs(del-1)<1e-14)break;}return Math.exp(-x+a*Math.log(x)-logGamma(a))*h;}
    private static double logGamma(double x){double[] c={676.5203681218851,-1259.1392167224028,771.32342877765313,-176.61502916214059,12.507343278686905,-0.13857109526572012,9.9843695780195716e-6,1.5056327351493116e-7};if(x<.5)return Math.log(Math.PI)-Math.log(Math.sin(Math.PI*x))-logGamma(1-x);x-=1;double a=.99999999999980993;for(int i=0;i<c.length;i++)a+=c[i]/(x+i+1);double t=x+c.length-.5;return .5*Math.log(2*Math.PI)+(x+.5)*Math.log(t)-t+Math.log(a);}
}

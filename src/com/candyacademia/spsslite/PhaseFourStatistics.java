package com.candyacademia.spsslite;

import java.util.*;

/** Phase 4: forecasting, survival analysis and binary predictive modelling. */
public final class PhaseFourStatistics {
    private PhaseFourStatistics() {}
    public record Forecast(int n,double level,double trend,double alpha,double beta,double rmse,double[] fitted,double[] forecasts,double[] acf) {}
    public record SurvivalPoint(double time,int atRisk,int events,int censored,double survival) {}
    public record KaplanMeier(int n,int events,int censored,double medianSurvival,SurvivalPoint[] points) {}
    public record LogRank(int groups,int events,double chiSquare,int df,double p) {}
    public record Logistic(int n,int predictors,int iterations,boolean converged,double logLikelihood,double nullLogLikelihood,double modelChiSquare,double modelP,double pseudoR2,double accuracy,double[] coefficients,double[] standardErrors,double[] wald,double[] p,double[] oddsRatios) {}

    public static Forecast holt(double[] values,int periods,double alpha,double beta,int maxLag){
        if(values.length<3)throw new IllegalArgumentException("At least 3 time-ordered observations are required.");
        if(periods<1||periods>1000)throw new IllegalArgumentException("Forecast periods must be between 1 and 1000.");
        if(!(alpha>0&&alpha<=1&&beta>0&&beta<=1))throw new IllegalArgumentException("Alpha and beta must be in (0, 1].");
        for(double v:values)if(!Double.isFinite(v))throw new IllegalArgumentException("Series contains a non-finite value.");
        double level=values[0],trend=values[1]-values[0],sse=0;double[]fitted=new double[values.length];fitted[0]=values[0];
        for(int t=1;t<values.length;t++){fitted[t]=level+trend;double error=values[t]-fitted[t];sse+=error*error;double old=level;level=alpha*values[t]+(1-alpha)*(level+trend);trend=beta*(level-old)+(1-beta)*trend;}
        double[]forecasts=new double[periods];for(int h=1;h<=periods;h++)forecasts[h-1]=level+h*trend;
        int lags=Math.max(0,Math.min(maxLag,values.length-1));double[]acf=new double[lags];double mean=Arrays.stream(values).average().orElseThrow(),den=0;for(double v:values)den+=(v-mean)*(v-mean);
        for(int lag=1;lag<=lags;lag++){double num=0;for(int t=lag;t<values.length;t++)num+=(values[t]-mean)*(values[t-lag]-mean);acf[lag-1]=den==0?Double.NaN:num/den;}
        return new Forecast(values.length,level,trend,alpha,beta,Math.sqrt(sse/(values.length-1)),fitted,forecasts,acf);
    }

    public static KaplanMeier kaplanMeier(double[]time,int[]event){
        if(time.length!=event.length||time.length<1)throw new IllegalArgumentException("Time and status must contain matching observations.");
        record Obs(double time,int event){}List<Obs>obs=new ArrayList<>();int totalEvents=0;
        for(int i=0;i<time.length;i++){if(!Double.isFinite(time[i])||time[i]<0)throw new IllegalArgumentException("Survival times must be finite and non-negative.");if(event[i]!=0&&event[i]!=1)throw new IllegalArgumentException("Status must be coded 1=event and 0=censored.");obs.add(new Obs(time[i],event[i]));totalEvents+=event[i];}
        obs.sort(Comparator.comparingDouble(Obs::time));List<SurvivalPoint>out=new ArrayList<>();double survival=1,median=Double.NaN;int atRisk=obs.size();
        for(int i=0;i<obs.size();){double t=obs.get(i).time();int d=0,c=0,j=i;while(j<obs.size()&&Double.compare(obs.get(j).time(),t)==0){if(obs.get(j).event()==1)d++;else c++;j++;}if(d>0)survival*=1-(double)d/atRisk;out.add(new SurvivalPoint(t,atRisk,d,c,survival));if(Double.isNaN(median)&&survival<=.5)median=t;atRisk-=d+c;i=j;}
        return new KaplanMeier(obs.size(),totalEvents,obs.size()-totalEvents,median,out.toArray(SurvivalPoint[]::new));
    }

    public static LogRank logRank(double[]time,int[]event,String[]group){
        if(time.length!=event.length||time.length!=group.length)throw new IllegalArgumentException("All survival inputs must have matching lengths.");
        List<String>levels=new ArrayList<>(new TreeSet<>(Arrays.asList(group)));if(levels.size()!=2)throw new IllegalArgumentException("Log-rank currently requires exactly two groups.");
        TreeSet<Double>eventTimes=new TreeSet<>();int events=0;for(int i=0;i<time.length;i++)if(event[i]==1){eventTimes.add(time[i]);events++;}
        double oe=0,var=0;String g0=levels.get(0);for(double t:eventTimes){int n=0,n0=0,d=0,d0=0;for(int i=0;i<time.length;i++){if(time[i]>=t){n++;if(group[i].equals(g0))n0++;}if(time[i]==t&&event[i]==1){d++;if(group[i].equals(g0))d0++;}}if(n>0){oe+=d0-(double)n0*d/n;if(n>1)var+=(double)n0*(n-n0)*d*(n-d)/(n*n*(n-1));}}
        double chi=var>0?oe*oe/var:0;return new LogRank(2,events,chi,1,Statistics.chiSquareSurvival(chi,1));
    }

    public static Logistic logistic(double[][]x,int[]y,int maxIterations){
        if(x.length!=y.length||x.length<4)throw new IllegalArgumentException("At least 4 complete observations are required.");int n=x.length,k=x[0].length+1;
        for(int i=0;i<n;i++){if(x[i].length!=k-1)throw new IllegalArgumentException("Predictor rows have inconsistent lengths.");for(double v:x[i])if(!Double.isFinite(v))throw new IllegalArgumentException("Predictors must be finite.");if(y[i]!=0&&y[i]!=1)throw new IllegalArgumentException("Outcome must be coded 0 and 1.");}
        int sum=Arrays.stream(y).sum();if(sum==0||sum==n)throw new IllegalArgumentException("Outcome must contain both 0 and 1.");double[]b=new double[k];boolean converged=false;int iterations=0;double[][]information=null;
        for(int it=0;it<maxIterations;it++){iterations=it+1;double[]score=new double[k];information=new double[k][k];for(int i=0;i<n;i++){double eta=b[0];for(int j=1;j<k;j++)eta+=b[j]*x[i][j-1];double p=sigmoid(eta),w=Math.max(1e-9,p*(1-p));for(int a=0;a<k;a++){double za=a==0?1:x[i][a-1];score[a]+=za*(y[i]-p);for(int c=0;c<k;c++){double zc=c==0?1:x[i][c-1];information[a][c]+=w*za*zc;}}}double[]step=multiply(inverse(information),score);double largest=0;for(int j=0;j<k;j++){b[j]+=step[j];largest=Math.max(largest,Math.abs(step[j]));}if(largest<1e-8){converged=true;break;}}
        double ll=0;int correct=0;for(int i=0;i<n;i++){double eta=b[0];for(int j=1;j<k;j++)eta+=b[j]*x[i][j-1];double p=Math.min(1-1e-15,Math.max(1e-15,sigmoid(eta)));ll+=y[i]*Math.log(p)+(1-y[i])*Math.log(1-p);if((p>=.5?1:0)==y[i])correct++;}
        double p0=(double)sum/n,nullLL=sum*Math.log(p0)+(n-sum)*Math.log(1-p0),chi=2*(ll-nullLL);double[][]cov=inverse(information);double[]se=new double[k],wald=new double[k],pv=new double[k],or=new double[k];for(int j=0;j<k;j++){se[j]=Math.sqrt(Math.max(0,cov[j][j]));wald[j]=se[j]>0?(b[j]/se[j])*(b[j]/se[j]):Double.NaN;pv[j]=Double.isFinite(wald[j])?Statistics.chiSquareSurvival(wald[j],1):Double.NaN;or[j]=Math.exp(Math.min(700,b[j]));}
        return new Logistic(n,k-1,iterations,converged,ll,nullLL,chi,Statistics.chiSquareSurvival(chi,k-1),1-ll/nullLL,(double)correct/n,b,se,wald,pv,or);
    }
    private static double sigmoid(double x){if(x>=0){double z=Math.exp(-x);return 1/(1+z);}double z=Math.exp(x);return z/(1+z);}
    private static double[]multiply(double[][]a,double[]b){double[]r=new double[a.length];for(int i=0;i<a.length;i++)for(int j=0;j<b.length;j++)r[i]+=a[i][j]*b[j];return r;}
    private static double[][]inverse(double[][]a){int n=a.length;double[][]m=new double[n][2*n];for(int i=0;i<n;i++){System.arraycopy(a[i],0,m[i],0,n);m[i][n+i]=1;}for(int c=0;c<n;c++){int p=c;for(int r=c+1;r<n;r++)if(Math.abs(m[r][c])>Math.abs(m[p][c]))p=r;if(Math.abs(m[p][c])<1e-12)throw new IllegalArgumentException("Model matrix is singular; remove redundant predictors.");double[]tmp=m[c];m[c]=m[p];m[p]=tmp;double q=m[c][c];for(int j=0;j<2*n;j++)m[c][j]/=q;for(int r=0;r<n;r++)if(r!=c){q=m[r][c];for(int j=0;j<2*n;j++)m[r][j]-=q*m[c][j];}}double[][]inv=new double[n][n];for(int i=0;i<n;i++)System.arraycopy(m[i],n,inv[i],0,n);return inv;}
}

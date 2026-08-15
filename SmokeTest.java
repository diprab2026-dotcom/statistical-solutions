import com.candyacademia.spsslite.Statistics;
import com.candyacademia.spsslite.PhaseOneStatistics;
import com.candyacademia.spsslite.PhaseTwoStatistics;
import com.candyacademia.spsslite.PhaseThreeStatistics;
import com.candyacademia.spsslite.PhaseFourStatistics;
import java.util.List;
import java.util.Arrays;

public class SmokeTest {
    public static void main(String[] args) {
        var d = Statistics.describe(List.of(1.0, 2.0, 3.0, 4.0, 5.0), 5);
        if (d.n() != 5 || Math.abs(d.mean() - 3.0) > 1e-12 || Math.abs(d.sd() - Math.sqrt(2.5)) > 1e-12)
            throw new AssertionError("Descriptive statistics failed");
        var r = Statistics.regression(new double[]{1,2,3,4,5}, new double[]{2,4,5,4,5});
        if (r.n() != 5 || !Double.isFinite(r.slope()) || !Double.isFinite(r.p()))
            throw new AssertionError("Regression failed");
        var t = Statistics.welch(new double[]{1,2,3,4}, new double[]{5,6,7,8});
        if (!Double.isFinite(t.t()) || !Double.isFinite(t.df()) || !Double.isFinite(t.p()))
            throw new AssertionError("T-test failed");
        var c = Statistics.chiSquare(List.of("A","A","B","B"), List.of("Y","Y","N","N"));
        if (c.df() != 1 || !Double.isFinite(c.statistic()) || !Double.isFinite(c.p()))
            throw new AssertionError("Chi-square failed");
        var e = PhaseOneStatistics.explore(List.of(1.0,2.0,3.0,4.0,5.0), 6);
        if (e.n() != 5 || e.missing() != 1 || Math.abs(e.median()-3.0) > 1e-12 || Math.abs(e.q1()-2.0) > 1e-12)
            throw new AssertionError("Explore failed");
        var one = PhaseOneStatistics.oneSample(new double[]{1,2,3,4,5}, 0);
        if (one.n() != 5 || !Double.isFinite(one.p()) || !Double.isFinite(one.cohenD()))
            throw new AssertionError("One-sample t-test failed");
        var paired = PhaseOneStatistics.paired(new double[]{3,4,5,7}, new double[]{2,2,4,4});
        if (paired.n() != 4 || !Double.isFinite(paired.p()) || !Double.isFinite(paired.cohenDz()))
            throw new AssertionError("Paired t-test failed");
        if (Math.abs(PhaseTwoStatistics.spearman(new double[]{1,2,3,4},new double[]{10,20,30,40})-1) > 1e-12)
            throw new AssertionError("Spearman failed");
        var anova = PhaseTwoStatistics.anova(List.of(new double[]{1,2,3},new double[]{5,6,7},new double[]{9,10,11}));
        if (anova.groups()!=3 || !Double.isFinite(anova.f()) || !Double.isFinite(anova.p()))
            throw new AssertionError("ANOVA failed");
        var mw = PhaseTwoStatistics.mannWhitney(new double[]{1,2,3},new double[]{7,8,9});
        if (!Double.isFinite(mw.statistic()) || !Double.isFinite(mw.p()))
            throw new AssertionError("Mann-Whitney failed");
        var mr = PhaseTwoStatistics.multipleRegression(new double[][]{{1,2},{2,1},{3,4},{4,3},{5,7},{6,5}},new double[]{3,3,7,7,12,11});
        if (mr.predictors()!=2 || !Double.isFinite(mr.r2()) || !Double.isFinite(mr.modelP()))
            throw new AssertionError("Multiple regression failed");
        double[][] multi = {{1,2,1},{2,3,2},{3,4,3},{4,4,5},{5,6,5},{6,7,7},{7,8,8},{8,9,9}};
        var rel = PhaseThreeStatistics.reliability(multi);
        if (rel.items()!=3 || !Double.isFinite(rel.alpha()))
            throw new AssertionError("Reliability failed");
        var pca = PhaseThreeStatistics.pca(multi);
        if (pca.variables()!=3 || pca.eigenvalues()[0] < pca.eigenvalues()[1])
            throw new AssertionError("PCA failed");
        var factor = PhaseThreeStatistics.factor(multi,2);
        if (factor.factors()!=2 || factor.rotated().length!=3)
            throw new AssertionError("Factor analysis failed");
        var km = PhaseThreeStatistics.kmeans(multi,2,100);
        if (km.clusters()!=2 || Arrays.stream(km.sizes()).sum()!=multi.length)
            throw new AssertionError("K-means failed");
        var forecast = PhaseFourStatistics.holt(new double[]{10,12,13,15,18,20,23,24},3,.3,.2,4);
        if (forecast.forecasts().length!=3 || !Double.isFinite(forecast.rmse()) || forecast.acf().length!=4)
            throw new AssertionError("Forecasting failed");
        var survival = PhaseFourStatistics.kaplanMeier(new double[]{1,2,3,4,5},new int[]{1,0,1,1,0});
        if (survival.n()!=5 || survival.events()!=3 || survival.points().length!=5)
            throw new AssertionError("Kaplan-Meier failed");
        var logRank = PhaseFourStatistics.logRank(new double[]{1,2,3,4,2,3,5,6},new int[]{1,1,0,1,1,0,1,0},new String[]{"A","A","A","A","B","B","B","B"});
        if (!Double.isFinite(logRank.chiSquare()) || !Double.isFinite(logRank.p()))
            throw new AssertionError("Log-rank failed");
        var logistic = PhaseFourStatistics.logistic(new double[][]{{0},{1},{2},{3},{4},{5},{6},{7},{8},{9}},new int[]{0,0,0,0,0,1,1,1,1,1},100);
        if (logistic.predictors()!=1 || !Double.isFinite(logistic.modelP()) || logistic.accuracy()<.5)
            throw new AssertionError("Logistic regression failed");
        System.out.println("All statistical smoke tests passed.");
    }
}

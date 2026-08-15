import com.candyacademia.spsslite.Statistics;
import com.candyacademia.spsslite.PhaseOneStatistics;
import com.candyacademia.spsslite.PhaseTwoStatistics;
import com.candyacademia.spsslite.PhaseThreeStatistics;
import com.candyacademia.spsslite.PhaseFourStatistics;
import com.candyacademia.spsslite.PhaseFiveStatistics;
import com.candyacademia.spsslite.PdfReportExporter;
import com.candyacademia.spsslite.UserAccountStore;
import com.candyacademia.spsslite.PhaseSixStatistics;
import com.candyacademia.spsslite.DataExchange;
import com.candyacademia.spsslite.AppTheme;
import java.util.List;
import java.util.Arrays;

public class SmokeTest {
    public static void main(String[] args) throws Exception {
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
        var repeated = PhaseFiveStatistics.repeatedMeasures(new double[][]{{10,12,14},{9,11,15},{8,12,13},{11,13,16}});
        if (repeated.conditions()!=3 || !Double.isFinite(repeated.f()) || !Double.isFinite(repeated.p()))
            throw new AssertionError("Repeated-measures ANOVA failed");
        var roc = PhaseFiveStatistics.roc(new double[]{.1,.2,.35,.4,.6,.7,.8,.95},new int[]{0,0,0,1,0,1,1,1});
        if (roc.auc()<.5 || roc.points().length<3 || !Double.isFinite(roc.optimalThreshold()))
            throw new AssertionError("ROC failed");
        var poisson = PhaseFiveStatistics.poisson(new double[][]{{0},{1},{2},{3},{4},{5},{6},{7}},new int[]{1,1,2,2,3,4,5,7},100);
        if (poisson.predictors()!=1 || !Double.isFinite(poisson.modelP()) || !Double.isFinite(poisson.deviance()))
            throw new AssertionError("Poisson regression failed");
        var diag = PhaseFiveStatistics.regressionDiagnostics(new double[][]{{1},{2},{3},{4},{5},{6}},new double[]{2,4,5,8,10,11});
        if (diag.n()!=6 || diag.cooksDistance().length!=6 || !Double.isFinite(diag.r2()))
            throw new AssertionError("Regression diagnostics failed");
        byte[] pdf = PdfReportExporter.create("STATISTICAL SOLUTIONS\nTest output χ² = 4.25\n" + "A long report line ".repeat(20));
        if (pdf.length<500 || !new String(pdf,0,8,java.nio.charset.StandardCharsets.ISO_8859_1).startsWith("%PDF-1.4"))
            throw new AssertionError("PDF report export failed");
        var accountFile = java.nio.file.Files.createTempDirectory("statistical-solutions-auth").resolve("accounts.properties");
        var accounts = new UserAccountStore(accountFile);
        accounts.create("analyst1","Secure123".toCharArray(),"Favourite book?","Research".toCharArray());
        if (!accounts.hasUsers() || !accounts.authenticate("analyst1","Secure123".toCharArray()))
            throw new AssertionError("Account creation or authentication failed");
        accounts.changePassword("analyst1","Secure123".toCharArray(),"Changed456".toCharArray());
        if (!accounts.authenticate("analyst1","Changed456".toCharArray()))
            throw new AssertionError("Password change failed");
        accounts.resetPassword("analyst1","research".toCharArray(),"Reset789".toCharArray());
        if (!accounts.authenticate("analyst1","Reset789".toCharArray()))
            throw new AssertionError("Password recovery failed");
        var friedman = PhaseSixStatistics.friedman(new double[][]{{1,2,3},{2,3,4},{1,4,5},{2,5,6}});
        if (friedman.conditions()!=3 || !Double.isFinite(friedman.p()) || friedman.kendallsW()<0)
            throw new AssertionError("Friedman test failed");
        var mcnemar = PhaseSixStatistics.mcnemar(new int[]{0,0,1,1,0,1},new int[]{1,0,0,1,1,1});
        if (!Double.isFinite(mcnemar.p()) || mcnemar.discordant01()!=2)
            throw new AssertionError("McNemar test failed");
        var excelFile = java.nio.file.Files.createTempDirectory("statistical-solutions-xlsx").resolve("roundtrip.xlsx");
        var sample = com.candyacademia.spsslite.SampleData.survey();
        DataExchange.writeXlsx(sample,excelFile);
        var imported = DataExchange.readXlsx(excelFile);
        if (imported.getRowCount()!=sample.getRowCount() || imported.getColumnCount()!=sample.getColumnCount())
            throw new AssertionError("Excel round-trip failed");
        javax.swing.JButton readable = AppTheme.button("Readable action",AppTheme.BLUE);
        if (AppTheme.contrastRatio(readable.getForeground(),readable.getBackground())<4.5 || readable.getPreferredSize().height<44)
            throw new AssertionError("Button readability requirements failed");
        System.out.println("All statistical smoke tests passed.");
    }
}

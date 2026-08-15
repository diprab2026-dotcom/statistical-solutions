package com.candyacademia.spsslite;
import javax.swing.*;import javax.swing.border.EmptyBorder;import java.awt.*;
public final class OfflineHelp{
 private OfflineHelp(){}
 public static void show(Component parent){JDialog d=new JDialog(SwingUtilities.getWindowAncestor(parent),"Statistical Solutions — Offline Help",Dialog.ModalityType.MODELESS);JEditorPane p=new JEditorPane("text/html",html());p.setEditable(false);p.setBorder(new EmptyBorder(18,24,18,24));d.add(new JScrollPane(p));d.setSize(850,650);d.setLocationRelativeTo(parent);d.setVisible(true);}
 private static String html(){return """
 <html><style>body{font-family:Segoe UI,Arial;background:#0f172a;color:#f8fafc;line-height:1.5}h1{color:#67e8f9}h2{color:#60a5fa;border-bottom:1px solid #334155;padding-bottom:5px}b{color:#ffffff}code{background:#1e293b;color:#f8fafc;padding:3px}</style>
 <h1>Statistical Solutions — Offline Guide</h1><p>This guide is available without an internet connection.</p>
 <h2>Getting started</h2><ol><li>Create or sign in to a local account.</li><li>Open CSV, XLSX or SPSS SAV data from File.</li><li>Check variable names in the first row and cases below.</li><li>Select a procedure from Analyze, Models, Scale &amp; Multivariate, or Forecasting &amp; Survival.</li><li>Export results as text or PDF.</li></ol>
 <h2>Data preparation</h2><p>Use one row per case and one column per variable. Blank cells are treated as missing. Binary outcomes should use 0 and 1. Survival status uses 1 for event and 0 for censored.</p>
 <h2>Procedure selection</h2><ul><li><b>Descriptive:</b> Explore, frequencies, missing-data summary.</li><li><b>Group comparisons:</b> t tests, ANOVA, repeated measures, Mann-Whitney, Wilcoxon, Kruskal-Wallis, Friedman and McNemar.</li><li><b>Relationships:</b> Pearson, Spearman, Kendall, chi-square and regression.</li><li><b>Scale and structure:</b> reliability, PCA, factor and cluster analysis.</li><li><b>Prediction:</b> linear, logistic and Poisson models, ROC/AUC and diagnostics.</li><li><b>Time/event:</b> Holt forecasts, autocorrelation, Kaplan-Meier and log-rank.</li></ul>
 <h2>Interpretation reminders</h2><p>Report estimates, confidence intervals, effect sizes and p-values together. Statistical significance does not guarantee practical importance. Review assumptions, missingness and influential observations before drawing conclusions.</p>
 <h2>File exchange</h2><p>XLSX files are read and written directly. SAV import reads compressed and uncompressed SPSS system files. SPSS export creates a CSV plus an SPS syntax file; open the SPS file in SPSS and run it to recreate the dataset.</p>
 <h2>Reports</h2><p>Use <b>Build PDF report</b> to add a title, analyst, project and interpretation notes before exporting the current output.</p></html>""";}
}

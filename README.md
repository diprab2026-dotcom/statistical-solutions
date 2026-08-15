# Statistical Solutions — SPSS-like Core Java Desktop Software

Statistical Solutions is a desktop statistics application written with Java Swing and only the Java standard library.

## Included features

- Spreadsheet-style editable data grid
- CSV import and export (first row is treated as variable names)
- Add cases and variables; rename variables
- Descriptive statistics: N, missing, mean, median, sample SD, minimum, maximum
- Pearson correlation
- Simple linear regression with coefficients, R, R², standard error, t statistic and p-value
- Welch independent-samples t-test with degrees of freedom and two-tailed p-value
- Chi-square test of independence with contingency table and p-value
- Histogram viewer
- Plain-text analysis output and report export

## Requirements

- JDK 17 or newer

## Compile and run

Windows:

```bat
run.bat
```

To create the standalone Windows application, install a Windows JDK 17 or newer and run:

```bat
build-exe.bat
```

The finished launcher will be created at:

```text
installer\Statistical Solutions\Statistical Solutions.exe
```

The generated application includes a private Java runtime, so end users do not need to install Java.

Linux/macOS:

```bash
chmod +x run.sh
./run.sh
```

Or manually:

```bash
javac -d out src/com/candyacademia/spsslite/*.java
java -cp out com.candyacademia.spsslite.Main
```

Load `sample-data/demo.csv`, then use the Analyze and Graphs menus.

## Important scope note

This is a functional MVP, not a complete reimplementation of IBM SPSS Statistics. A production equivalent would also require a variable-view metadata system, syntax language, data transformations, weighting, split-file support, pivot tables, advanced models, database connectors, project persistence, undo/redo, accessibility, localization, comprehensive validation, and extensive numerical verification.

## Suggested next phase

1. Variable View: labels, types, value labels, missing-value rules, measures
2. `.statdesk` project format and analysis history
3. Frequencies, ANOVA, paired t-test, non-parametric tests and multiple regression
4. Scatter, box, bar, Q-Q and residual plots
5. Filter/select cases, recode, compute variable, sort, merge and aggregate
6. Automated unit tests against trusted statistical reference datasets

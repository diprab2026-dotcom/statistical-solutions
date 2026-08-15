package com.candyacademia.spsslite;

import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public final class DataSet {
    public static DefaultTableModel blank(int rows,int cols){String[] h=new String[cols];for(int i=0;i<cols;i++)h[i]="VAR"+(i+1);return new DefaultTableModel(h,rows);}
    public static DefaultTableModel readCsv(Path p)throws IOException{
        List<List<String>> rows=new ArrayList<>();try(BufferedReader r=Files.newBufferedReader(p,StandardCharsets.UTF_8)){String line;while((line=r.readLine())!=null)rows.add(parseCsv(line));}
        if(rows.isEmpty())return blank(25,5);String[] h=rows.remove(0).toArray(String[]::new);DefaultTableModel m=new DefaultTableModel(h,0);for(List<String> row:rows){while(row.size()<h.length)row.add("");m.addRow(row.subList(0,h.length).toArray());}return m;
    }
    public static void writeCsv(DefaultTableModel m,Path p)throws IOException{try(BufferedWriter w=Files.newBufferedWriter(p,StandardCharsets.UTF_8)){for(int c=0;c<m.getColumnCount();c++){if(c>0)w.write(',');w.write(quote(m.getColumnName(c)));}w.newLine();for(int r=0;r<m.getRowCount();r++){for(int c=0;c<m.getColumnCount();c++){if(c>0)w.write(',');Object v=m.getValueAt(r,c);w.write(quote(v==null?"":v.toString()));}w.newLine();}}}
    private static String quote(String s){return s.indexOf(',')>=0||s.indexOf('"')>=0||s.indexOf('\n')>=0?'"'+s.replace("\"","\"\"")+'"':s;}
    private static List<String> parseCsv(String line){List<String> out=new ArrayList<>();StringBuilder b=new StringBuilder();boolean q=false;for(int i=0;i<line.length();i++){char ch=line.charAt(i);if(ch=='"'){if(q&&i+1<line.length()&&line.charAt(i+1)=='"'){b.append('"');i++;}else q=!q;}else if(ch==','&&!q){out.add(b.toString());b.setLength(0);}else b.append(ch);}out.add(b.toString());return out;}
    public static List<Double> numeric(DefaultTableModel m,int col){List<Double> out=new ArrayList<>();for(int r=0;r<m.getRowCount();r++){String s=text(m,r,col);if(!s.isBlank())try{out.add(Double.parseDouble(s));}catch(NumberFormatException ignored){}}return out;}
    public static double[][] paired(DefaultTableModel m,int a,int b){List<Double>x=new ArrayList<>(),y=new ArrayList<>();for(int r=0;r<m.getRowCount();r++)try{String sa=text(m,r,a),sb=text(m,r,b);if(!sa.isBlank()&&!sb.isBlank()){x.add(Double.parseDouble(sa));y.add(Double.parseDouble(sb));}}catch(NumberFormatException ignored){}return new double[][]{x.stream().mapToDouble(Double::doubleValue).toArray(),y.stream().mapToDouble(Double::doubleValue).toArray()};}
    public static String text(DefaultTableModel m,int r,int c){Object v=m.getValueAt(r,c);return v==null?"":v.toString().trim();}
}

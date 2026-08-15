package com.candyacademia.spsslite;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Minimal dependency-free PDF writer for paginated statistical output. */
public final class PdfReportExporter {
    private static final int LINES_PER_PAGE=50,CHARS_PER_LINE=88;
    private PdfReportExporter(){}
    public static void write(Path path,String report)throws IOException{Files.write(path,create(report));}
    public static byte[] create(String report)throws IOException{
        List<String>lines=wrap(normalize(report));if(lines.isEmpty())lines=List.of("No analysis output available.");int pages=(lines.size()+LINES_PER_PAGE-1)/LINES_PER_PAGE,fontObject=3+pages*2,objectCount=fontObject;
        List<byte[]>objects=new ArrayList<>();objects.add(bytes("<< /Type /Catalog /Pages 2 0 R >>"));StringBuilder kids=new StringBuilder();for(int p=0;p<pages;p++)kids.append(3+p*2).append(" 0 R ");objects.add(bytes("<< /Type /Pages /Count "+pages+" /Kids ["+kids+"] >>"));
        for(int p=0;p<pages;p++){int pageObject=3+p*2,contentObject=pageObject+1;objects.add(bytes("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 "+fontObject+" 0 R >> >> /Contents "+contentObject+" 0 R >>"));StringBuilder stream=new StringBuilder("BT\n/F1 9 Tf\n45 752 Td\n12 TL\n");int start=p*LINES_PER_PAGE,end=Math.min(lines.size(),start+LINES_PER_PAGE);for(int i=start;i<end;i++)stream.append('(').append(escape(lines.get(i))).append(") Tj\nT*\n");stream.append("ET\n");byte[]data=bytes(stream.toString());ByteArrayOutputStream body=new ByteArrayOutputStream();body.write(bytes("<< /Length "+data.length+" >>\nstream\n"));body.write(data);body.write(bytes("endstream"));objects.add(body.toByteArray());}
        objects.add(bytes("<< /Type /Font /Subtype /Type1 /BaseFont /Courier /Encoding /WinAnsiEncoding >>"));ByteArrayOutputStream pdf=new ByteArrayOutputStream();pdf.write(new byte[]{'%','P','D','F','-','1','.','4','\n','%',(byte)0xE2,(byte)0xE3,(byte)0xCF,(byte)0xD3,'\n'});long[]offsets=new long[objectCount+1];for(int i=0;i<objects.size();i++){offsets[i+1]=pdf.size();pdf.write(bytes((i+1)+" 0 obj\n"));pdf.write(objects.get(i));pdf.write(bytes("\nendobj\n"));}long xref=pdf.size();pdf.write(bytes("xref\n0 "+(objectCount+1)+"\n0000000000 65535 f \n"));for(int i=1;i<=objectCount;i++)pdf.write(bytes(String.format(Locale.ROOT,"%010d 00000 n \n",offsets[i])));pdf.write(bytes("trailer\n<< /Size "+(objectCount+1)+" /Root 1 0 R >>\nstartxref\n"+xref+"\n%%EOF\n"));return pdf.toByteArray();
    }
    private static List<String>wrap(String text){List<String>out=new ArrayList<>();for(String raw:text.split("\\n",-1)){String line=raw.replace("\t","    ");if(line.isEmpty()){out.add("");continue;}while(line.length()>CHARS_PER_LINE){int cut=line.lastIndexOf(' ',CHARS_PER_LINE);if(cut<20)cut=CHARS_PER_LINE;out.add(line.substring(0,cut));line=line.substring(cut).stripLeading();}out.add(line);}return out;}
    private static String normalize(String s){return (s==null?"":s).replace("χ","chi").replace("²","^2").replace("–","-").replace("—","-").replace("−","-").replace("→","->").replace("ρ","rho").replace("τ","tau").replace("α","alpha").replace("β","beta").replace("✓","Yes");}
    private static String escape(String s){StringBuilder b=new StringBuilder();for(char c:s.toCharArray()){if(c=='('||c==')'||c=='\\')b.append('\\').append(c);else if(c>=32&&c<=255)b.append(c);else b.append('?');}return b.toString();}
    private static byte[]bytes(String s){return s.getBytes(StandardCharsets.ISO_8859_1);}
}

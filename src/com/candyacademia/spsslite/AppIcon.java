package com.candyacademia.spsslite;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/** Loads the Statistical Solutions brand mark packaged with the application. */
public final class AppIcon {
 private static final ImageIcon LOGO=loadLogo();
 private AppIcon(){}
 private static ImageIcon loadLogo(){URL url=AppIcon.class.getResource("/assets/statistical-solutions-logo.png");return url==null?new ImageIcon():new ImageIcon(url);}
 public static Image image(){return LOGO.getImage();}
 public static Icon sized(int size){Image image=LOGO.getImage();return image.getWidth(null)<1?UIManager.getIcon("OptionPane.informationIcon"):new ImageIcon(image.getScaledInstance(size,size,Image.SCALE_SMOOTH));}
 public static JLabel label(int size){JLabel label=new JLabel(sized(size));label.setPreferredSize(new Dimension(size,size));return label;}
}

package ise.antelope.plugin;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import org.gjt.sp.jedit.OptionPane;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import ise.library.KappaLayout;
import ise.antelope.common.AntUtils;
import ise.antelope.common.Constants;
import ise.antelope.common.OptionSettings;

/**
 * That's right, three classes with the same name in three different packages!
 * Talk about being unoriginal!
 * @author Dale Anson
 */
public class AntelopeOptions implements OptionPane {
   private JPanel panel = null;
   private View view = null;

   public AntelopeOptions( View view ) {
      this.view = view;
   }

   public void init() {
      if ( panel != null )
         return ;
      panel = new JPanel( new KappaLayout() );

      JLabel label = new JLabel( "<html><b>Ant Home</b><br>You may change the location of Ant here. jEdit will need<br>to be restarted for the change to take effect." );

      final JTextField ant_home_field = new JTextField( 25 );
      ant_home_field.setEditable( false );
      ant_home_field.setText( AntUtils.getAntHome() );

      JButton choose_btn = new JButton( "Choose..." );


      panel.add( label, "0, 0, 4, 1, W, , 5" );
      panel.add( ant_home_field, "0, 1, 3, 1, 0, wh, 5" );
      panel.add( choose_btn, "3, 1, 1, 1, 0, wh, 5" );

      choose_btn.addActionListener( new ActionListener() {
               public void actionPerformed( ActionEvent ae ) {
                  String ant_home = AntUtils.getAntHome();
                  if ( ant_home.equals( "" ) ) {
                     ant_home = System.getProperty( "user.home" );
                  }
                  JFileChooser chooser = new JFileChooser( ant_home );
                  chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
                  chooser.setDialogTitle( "Select Ant home directory" );
                  int rtn = chooser.showOpenDialog( null );
                  if ( rtn == JFileChooser.APPROVE_OPTION ) {
                     try {
                        File f = chooser.getSelectedFile();
                        if ( !f.exists() ) {
                           JOptionPane.showMessageDialog( null, "Directory " + f.toString() + " does not exist.", "Error", JOptionPane.ERROR_MESSAGE );
                           return ;
                        }
                        File ant_jar = new File( f.getAbsolutePath(), "ant.jar" );
                        if ( !ant_jar.exists() ) {
                           ant_jar = new File( f.getAbsolutePath() + File.separator + "lib", "ant.jar" );
                           if ( !ant_jar.exists() ) {
                              JOptionPane.showMessageDialog( null, "Directory " + f.toString() + " does not appear to contain Ant.", "Error", JOptionPane.ERROR_MESSAGE );
                              return ;
                           }
                        }

                        if ( f.equals( new File( ant_home ) ) ){
                           return ;
                        }
                        ant_home = f.getAbsolutePath();
                        Constants.PREFS.put( Constants.ANT_HOME, ant_home );
                        Constants.PREFS.flush();
                        ant_home_field.setText( ant_home );
                        JOptionPane.showMessageDialog( null, "You must restart jEdit for this change to take effect.", "Warning", JOptionPane.WARNING_MESSAGE );
                     }
                  catch ( Exception e ) {}
                  }
               }
            }
                                  );
   }

   public void save() {}

   public Component getComponent() {
      if ( panel == null )
         init();
      return panel;
   }

   public String getName() {
      return "antelope";
   }

}

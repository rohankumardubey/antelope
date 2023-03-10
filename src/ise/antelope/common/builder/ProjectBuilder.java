package ise.antelope.common.builder;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import ise.library.*;
import org.apache.tools.ant.taskdefs.AntStructure;

import com.wutka.dtd.*;

public class ProjectBuilder extends JPanel {

   private String name = "";
   private String defaultTarget = "";
   private String basedir = ".";

   private JSplitPane splitpane = null;
   private JSplitPane right_pane = null;

   public ProjectBuilder() {
      init();
   }

   private void init() {
      try {
         /*
         final class AntStructurer extends AntStructure {
            public AntStructurer() {
               project = new org.apache.tools.ant.Project();
               project.init();
               taskType = "antstructure";
               taskName = "antstructure";
               target = new org.apache.tools.ant.Target();
            }
      }
         AntStructurer as = new AntStructurer();
         File tmp = File.createTempFile( "ant", ".dtd" );
         System.out.println( tmp );
         as.setOutput( tmp );
         as.execute();
         */

         // make a tree:
         // root node is project
         // a node for target
         // a node for task
         //    with a subnode for each task
         //       with a subnode for each child
         // a node for type
         //    with a subnode for each type
         //       with a subnode for each child

         DefaultMutableTreeNode root_node = new DefaultMutableTreeNode( DNDConstants.PROJECT );
         DroppableTreeNode target_node = new DroppableTreeNode( DNDConstants.TARGET, true );
         DefaultMutableTreeNode task_node = new DefaultMutableTreeNode( DNDConstants.TASK );
         DefaultMutableTreeNode type_node = new DefaultMutableTreeNode( DNDConstants.TYPE );
         root_node.add( target_node );
         root_node.add( task_node );
         root_node.add( type_node );


         Hashtable elements = DNDConstants.ANT_DTD.elements;
         Hashtable entities = DNDConstants.ANT_DTD.entities;

         DTDEntity task_entity = ( DTDEntity ) entities.get( "tasks" );
         DTDEntity type_entity = ( DTDEntity ) entities.get( "types" );

         // read and sort the tasks by name
         String task_entity_value = task_entity.getValue();
         StringTokenizer st = new StringTokenizer( task_entity_value, "|" );
         TreeSet task_list = new TreeSet();
         while ( st.hasMoreTokens() ) {
            task_list.add( st.nextToken().trim() );
         }

         // add the tasks to the task node
         Iterator it = task_list.iterator();
         while ( it.hasNext() ) {
            DroppableTreeNode node = new DroppableTreeNode( it.next(), true );
            task_node.add( node );
            addChildren( node );
         }

         // read and sort the types by name
         String type_entity_value = type_entity.getValue();
         st = new StringTokenizer( type_entity_value, "|" );
         TreeSet type_list = new TreeSet();
         while ( st.hasMoreTokens() ) {
            type_list.add( st.nextToken().trim() );
         }

         // add the types to the types node
         it = type_list.iterator();
         while ( it.hasNext() ) {
            DroppableTreeNode node = new DroppableTreeNode( it.next(), true );
            type_node.add( node );
            addChildren( node );
         }

         // fill the tree
         JTree project_tree = new JTree( root_node );
         //project_tree.addMouseListener( new AttributeViewer( project_tree ) );
         project_tree.setDragEnabled( true );

         ElementPanel project_panel = new ElementPanel( project_tree.getPathForRow( 0 ) );
         ProjectTreePanel project_tree_panel = new ProjectTreePanel();
         right_pane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, true, new JScrollPane( project_tree_panel ), new JScrollPane( project_panel ) );
         splitpane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,
               true,
               new JScrollPane( project_tree ),
               right_pane );
         setLayout( new LambdaLayout() );
         add( splitpane, "0, 0, 1, 1, 0, wh, 3" );
         project_tree.setDragEnabled( true );
         project_tree.setTransferHandler( new TreeTransferHandler() );
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }

   }

   public void initSplitters() {
      right_pane.setDividerLocation( 0.33 );
      splitpane.setDividerLocation( 0.25 );
   }

   private void addChildren( DefaultMutableTreeNode node ) {
      String name = node.toString();
      DTDElement element = ( DTDElement ) DNDConstants.ANT_DTD.elements.get( name );
      DTDItem content = element.getContent();
      if ( content instanceof DTDContainer ) {
         DTDItem[] contents = ( ( DTDContainer ) content ).getItem();
         for ( int i = 0; i < contents.length; i++ ) {
            if ( contents[ i ] instanceof DTDName ) {
               String child_name = ( ( DTDName ) contents[ i ] ).getValue();
               DroppableTreeNode child = new DroppableTreeNode( child_name, true );
               node.add( child );
            }
         }
      }
   }

   public void setName( String name ) {
      if ( name == null )
         throw new IllegalArgumentException( "name cannot be null" );
      this.name = name;
   }

   public void setDefaultTarget( String targetName ) {
      if ( targetName == null )
         throw new IllegalArgumentException( "targetName cannot be null" );
      defaultTarget = targetName;
   }

   public void setBaseDir( String basedir ) {
      if ( basedir == null )
         throw new IllegalArgumentException( "basedir cannot be null" );
      this.basedir = basedir;
   }


   /**
    * Shows a popup.   
    */
   public class AttributeViewer extends MouseAdapter {
      private JTree tree = null;
      private JTextArea ta;
      private JPopupMenu pm;
      public AttributeViewer( JTree tree ) {
         this.tree = tree;
         ta = new JTextArea( 10, 40 );
         ta.setLineWrap( true );
         ta.setEditable( false );
      }
      public void mousePressed( MouseEvent me ) {
         doPopup( me );
      }
      public void mouseReleased( MouseEvent me ) {
         doPopup( me );
      }
      private void doPopup( MouseEvent me ) {
         if ( me.isPopupTrigger() ) {
            TreePath tp = tree.getPathForLocation( me.getX(), me.getY() );
            if ( tp != null ) {
               Object value = tp.getLastPathComponent();
               if ( value != null ) {
                  JPanel top = new JPanel( new KappaLayout() );
                  DTDElement el = ( DTDElement ) DNDConstants.ANT_DTD.elements.get( value.toString() );
                  if ( el != null ) {
                     Hashtable attrs = el.attributes;
                     Object[] attr_names = attrs.keySet().toArray();
                     Arrays.sort( attr_names );
                     KappaLayout.Constraints c = KappaLayout.createConstraint();
                     c.p = 3;
                     c.a = KappaLayout.W;
                     String item_name = value.toString();
                     item_name = item_name.substring( 0, 1 ).toUpperCase() + item_name.substring( 1 );

                     top.add( new JLabel( "<html><b>" + item_name + "</b></html>" ), c );
                     c.a = KappaLayout.E;
                     ++ c.y;
                     for ( int i = 0; i < attr_names.length; i++ ) {
                        String name = attr_names[ i ].toString();
                        if ( name.equals( "taskname" ) )
                           continue;
                        DTDAttribute attribute = ( DTDAttribute ) attrs.get( name );
                        Object type = attribute.getType();
                        JComponent comp = null;
                        if ( type instanceof DTDEnumeration || type instanceof DTDNotationList ) {
                           String[] items = ( ( DTDEnumeration ) type ).getItem();
                           comp = new JComboBox( items );
                           ( ( JComboBox ) comp ).setEditable( false );
                        }
                        else {
                           // assume String type
                           comp = new JTextField( 25 );
                           ( ( JTextField ) comp ).setText( type.toString() );
                        }

                        JLabel label = new JLabel( name + ":" );
                        top.add( label, c );
                        c.x = 1;
                        top.add( comp, c );
                        c.x = 0;
                        ++c.y;
                     }

                     pm = new JPopupMenu();
                     pm.add( new JScrollPane( top ) );
                     ta.setText( value.toString() );
                     GUIUtils.showPopupMenu( pm, tree, me.getX(), me.getY() );
                  }
               }
            }
         }
      }
   }


   public static void main ( String[] args ) {
      javax.swing.SwingUtilities.invokeLater( new Runnable() {
               public void run() {
                  JFrame.setDefaultLookAndFeelDecorated( true );
                  JFrame frame = new JFrame();
                  ProjectBuilder pb = new ProjectBuilder();
                  pb.setOpaque( true );
                  frame.setContentPane( pb );
                  frame.setSize( 600, 600 );
                  frame.setVisible( true );
                  frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
                  pb.initSplitters();
               }
            }
                                            );
   }
}

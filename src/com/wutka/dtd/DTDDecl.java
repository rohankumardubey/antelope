package com.wutka.dtd;

import java.io.*;

/** Represents the possible values for an attribute declaration
 *
 * @author Mark Wutka
 * @version $Revision$ $Date$ by $Author$
 */
public class DTDDecl implements DTDOutput, java.io.Serializable {
   public static final DTDDecl FIXED = new DTDDecl( 0, "FIXED" );
   public static final DTDDecl REQUIRED = new DTDDecl( 1, "REQUIRED" );
   public static final DTDDecl IMPLIED = new DTDDecl( 2, "IMPLIED" );
   public static final DTDDecl VALUE = new DTDDecl( 3, "VALUE" );

   public int type;
   public String name;

   public DTDDecl( int aType, String aName ) {
      type = aType;
      name = aName;
   }
   
   public String toString() {
      switch(type) {
         case 0: return "FIXED";
         case 1: return "REQUIRED";
         case 2: return "IMPLIED";
      }
      return "";
   }

   public boolean equals( Object ob ) {
      if ( ob == this )
         return true;
      if ( !( ob instanceof DTDDecl ) )
         return false;

      DTDDecl other = ( DTDDecl ) ob;
      return other.type == type;
   }

   public void write( PrintWriter out )
   throws IOException {
      if ( this == FIXED ) {
         out.print( " #FIXED" );
      }
      else if ( this == REQUIRED ) {
         out.print( " #REQUIRED" );
      }
      else if ( this == IMPLIED ) {
         out.print( " #IMPLIED" );
      }
      // Don't do anything for value since there is no associated DTD keyword
   }
}

/*
*  The Apache Software License, Version 1.1
*
*  Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
*  reserved.
*
*  Redistribution and use in source and binary forms, with or without
*  modification, are permitted provided that the following conditions
*  are met:
*
*  1. Redistributions of source code must retain the above copyright
*  notice, this list of conditions and the following disclaimer.
*
*  2. Redistributions in binary form must reprodconnectione the above copyright
*  notice, this list of conditions and the following disclaimer in
*  the documentation and/or other materials provided with the
*  distribution.
*
*  3. The end-user documentation included with the redistribution, if
*  any, must include the following acknowlegement:
*  "This prodconnectiont includes software developed by the
*  Apache Software Foundation (http://www.apache.org/)."
*  Alternately, this acknowlegement may appear in the software itself,
*  if and wherever sconnectionh third-party acknowlegements normally appear.
*
*  4. The names "The Jakarta Project", "Ant", and "Apache Software
*  Foundation" must not be used to endorse or promote prodconnectionts derived
*  from this software without prior written permission. For written
*  permission, please contact apache@apache.org.
*
*  5. Prodconnectionts derived from this software may not be called "Apache"
*  nor may "Apache" appear in their names without prior written
*  permission of the Apache Group.
*
*  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
*  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
*  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
*  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
*  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
*  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
*  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
*  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
*  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
*  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
*  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
*  SUCH DAMAGE.
*  ====================================================================
*
*  This software consists of voluntary contributions made by many
*  individuals on behalf of the Apache Software Foundation.  For more
*  information on the Apache Software Foundation, please see
*  <http://www.apache.org/>.
*/
package ise.antelope.tasks;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;

import java.rmi.server.UID;

import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;

import ise.antelope.tasks.typedefs.Prop;

/**
 * This task does an http post. Name/value pairs for the post can be set in
 * either or both of two ways, by nested Prop elements and/or by a file
 * containing properties. Nested Prop elements are automatically configured by
 * Ant. Properties from a file are configured by code borrowed from Property so
 * all Ant property constructs (like ${somename}) are resolved prior to the
 * post. This means that a file can be set up in advance of running the build
 * and the appropriate property values will be filled in at run time.
 *
 * @author    Dale Anson, danson@germane-software.com
 * @version   $Revision$
 */
public class PostTask extends Task {

    /** Storage for name/value pairs to send. */
    private Hashtable props = new Hashtable();
    /** URL to send the name/value pairs to. */
    private URL to = null;
    /** File to read name/value pairs from. */
    private File propsFile = null;
    /** storage for Ant properties */
    private String textProps = null;
    /** encoding to use for the name/value pairs */
    private String encoding = "UTF-8";
    /** where to store the server response */
    private File log = null;
    /** append to the log? */
    private boolean append = true;
    /** verbose? */
    private boolean verbose = true;
    /** want to keep the server response? */
    private boolean wantResponse = true;
    /** store output in this property */
    private String property = null;
    /** where to write downloads */
    private File outdir = null;

    /** how long to wait for a response from the server */
    private long maxwait = 180000;   // units for maxwait is milliseconds
    /** fail on error? */
    private boolean failOnError = false;

    // storage for cookies, static so that cookies are shared between instances
    // of Post. This allows a <post> to follow a previous <post> and use the
    // cookies that may have been set in the first post.
    private static Hashtable cookieStorage = new Hashtable();

    /** connection to the server */
    private URLConnection connection = null;
    
    /** for thread handling */
    private Thread currentRunner = null;



    /**
     * Set the url to post to. Required.
     *
     * @param name  the url to post to.
     */
    public void setTo( URL name ) {
        to = name;
    }

    
	/**
	 * Set a directory to store files that may be returned from the post.
	 * @param dir the directory
	 */
    public void setOutdir( File dir) {
        outdir = dir;
        if (outdir != null && outdir.isFile()) {
            outdir = outdir.getParentFile();
        }
    }


    /**
     * Set the name of a file to read a set of properties from.
     *
     * @param f  the file
     */
    public void setFile( File f ) {
        propsFile = f;
    }


    /**
     * Set the name of a file to save the response to. Optional. Ignored if
     * "want response" is false.
     *
     * @param f  the file
     */
    public void setLogfile( File f ) {
        log = f;
    }

    
    /**
     * Should the log file be appended to or overwritten? Default is true,
     * append to the file.
     *
     * @param b  append or not
     */
    public void setAppend( boolean b ) {
        append = b;
    }


    /**
     * If true, progress messages and returned data from the post will be
     * displayed. Default is true.
     *
     * @param b  true = verbose
     */
    public void setVerbose( boolean b ) {
        verbose = b;
    }


    /**
     * Default is true, get the response from the post. Can be set to false for
     * "fire and forget" messages.
     *
     * @param b  print/log server response
     */
    public void setWantresponse( boolean b ) {
        wantResponse = b;
    }

    /**
     * Set the name of a property to save the response to. Optional. Ignored if
     * "wantResponse" is false.
     * @param name the name to use for the property
     */
    public void setProperty( String name ) {
        property = name;
    }

    /**
     * Sets the encoding of the outgoing properties, default is UTF-8.
     *
     * @param encoding  The new encoding value
     */
    public void setEncoding( String encoding ) {
        this.encoding = encoding;
    }


    /**
     * How long to wait on the remote server. As a post is generally a two part
     * process (sending and receiving), maxwait is applied separately to each
     * part, that is, if 180 is passed as the wait parameter, this task will
     * spend at most 3 minutes to connect to the remote server and at most
     * another 3 minutes waiting on a response after the post has been sent.
     * This means that the wait period could total as much as 6 minutes (or 360
     * seconds). <p>
     *
     * The default wait period is 3 minutes (180 seconds).
     *
     * @param wait  time to wait in seconds, set to 0 to wait forever.
     */
    public void setMaxwait( int wait ) {
        maxwait = wait * 1000;
    }


    /**
     * Should the build fail if the post fails?
     *
     * @param fail  true = fail the build, default is false
     */
    public void setFailonerror( boolean fail ) {
        failOnError = fail;
    }

    /**
     * Adds a name/value pair to post. Optional.
     *
     * @param p                   A property pair to send as part of the post.
     * @exception BuildException  When name and/or value are missing.
     */
    public void addConfiguredProp( Prop p ) throws BuildException {
        String name = p.getName();
        if ( name == null ) {
            throw new BuildException( "name is null", getLocation() );
        }
        String value = p.getValue();
        if ( value == null ) {
            value = getProject().getProperty( name );
        }
        if ( value == null ) {
            throw new BuildException( "value is null", getLocation() );
        }
        props.put( name, value );
    }


    /**
     * Adds a feature to the Text attribute of the PostTask object
     *
     * @param text  The feature to be added to the Text attribute
     */
    public void addText( String text ) {
        textProps = text;
    }


    /**
     * Do the post.
     *
     * @exception BuildException  On any error.
     */
    public void execute() throws BuildException {
        if ( to == null ) {
            throw new BuildException( "'to' attribute is required", getLocation() );
        }
        final String content = getContent();
        try {
            if ( verbose )
                log( "Opening connection for post to " + to.toString() + "..." );

            // do the POST
            Thread runner =
                new Thread() {
                    public void run() {
                        DataOutputStream out = null;
                        try {
                            // set the url connection properties
                            connection = to.openConnection();
                            connection.setDoInput( true );
                            connection.setDoOutput( true );
                            connection.setUseCaches( false );
                            connection.setRequestProperty(
                                "Content-Type",
                                "application/x-www-form-urlencoded" );

                            // check if there are cookies to be included
                            for ( Iterator it = cookieStorage.keySet().iterator(); it.hasNext(); ) {
                                StringBuffer sb = new StringBuffer();
                                Object name = it.next();
                                if ( name != null ) {
                                    String key = name.toString();
                                    Cookie cookie = ( Cookie ) cookieStorage.get( name );
                                    if ( to.getPath().startsWith( cookie.getPath() ) ) {
                                        connection.addRequestProperty( "Cookie", cookie.toString() );
                                        if (verbose)
                                            log("Added cookie: " + cookie.toString());
                                    }
                                }
                            }

                            // do the post
                            if ( verbose ) {
                                log( "Connected, sending data..." );
                            }
                            out = new DataOutputStream( connection.getOutputStream() );
                            if ( verbose ) {
                                log( content );
                            }
                            out.writeBytes( content );
                            out.flush();
                            if ( verbose ) {
                                log( "Data sent." );
                            }
                        }
                        catch ( Exception e ) {
                            if ( failOnError ) {
                                throw new BuildException( e, getLocation() );
                            }
                        }
                        finally {
                            try {
                                out.close();
                            }
                            catch ( Exception e ) {
                                // ignored
                            }
                        }
                    }
                }
                ;
            runner.start();
            runner.join( maxwait );
            if ( runner.isAlive() ) {
                runner.interrupt();
                if ( failOnError ) {
                    throw new BuildException( "maxwait exceeded, unable to send data", getLocation() );
                }
                return ;
            }

            // read the response, if any, optionally writing it to a file
            if ( wantResponse ) {
                if ( verbose ) {
                    log( "Waiting for response..." );
                }
                runner =
                    new Thread() {
                        public void run() {
                            PrintWriter fw = null;
                            StringWriter sw = null;
                            PrintWriter pw = null;
                            FileOutputStream dw = null;
                            BufferedInputStream in = null;
                            byte[] buffer = new byte[8192];
                            File downloadFile = null;
                            try {
                                if ( connection instanceof HttpURLConnection ) {
                                    // read and store cookies
                                    Map map = ( ( HttpURLConnection ) connection ).getHeaderFields();
                                    for ( Iterator it = map.keySet().iterator(); it.hasNext(); ) {
                                        String name = ( String ) it.next();
                                        if ( name != null && name.equals( "Set-Cookie" ) ) {
                                            List cookies = ( List ) map.get( name );
                                            for ( Iterator c = cookies.iterator(); c.hasNext(); ) {
                                                String raw = ( String ) c.next();
                                                Cookie cookie = new Cookie( raw );
                                                cookieStorage.put( cookie.getId(), cookie );
                                            }
                                        }
                                    }

                                    // maybe log response headers
                                    if ( verbose ) {
                                        log( String.valueOf( ( ( HttpURLConnection ) connection ).getResponseCode() ) );
                                        log( ( ( HttpURLConnection ) connection ).getResponseMessage() );
                                        map = ( ( HttpURLConnection ) connection ).getHeaderFields();
                                        StringBuffer sb = new StringBuffer();
                                        for ( Iterator it = map.keySet().iterator(); it.hasNext(); ) {
                                            String name = ( String ) it.next();
                                            sb.append( name ).append( "=" );
                                            List values = ( List ) map.get( name );
                                            if ( values != null ) {
                                                if ( values.size() == 1 )
                                                    sb.append( values.get( 0 ) );
                                                else if ( values.size() > 1 ) {
                                                    sb.append( "[" );
                                                    for ( Iterator v = values.iterator(); v.hasNext(); ) {
                                                        sb.append( v.next() ).append( "," );
                                                    }
                                                    sb.append( "]" );
                                                }
                                            }
                                            sb.append( "\n" );
                                            log( sb.toString() );
                                        }
                                    }
                                    
                                    // might have a download
                                    map = ( ( HttpURLConnection ) connection ).getHeaderFields();
                                    List values = (List)map.get("Content-Type");
                                    String content_type = null;
                                    if (values != null)
                                        content_type = (String)values.get(0);
                                    if (content_type != null && content_type.equals("application/download")) {
                                        if (outdir == null) {
                                            outdir = new File(getProject().getProperty("basedir"));   
                                        }
                                        String cd = (String)(((List)map.get("Content-Disposition")).get(0));
                                        int index = cd.indexOf("filename=");
                                        if (index >= 0) {
                                            String filename = cd.substring(index + "filename=".length());
                                            filename = filename.replaceAll("\"", "");
                                            downloadFile = new File(outdir, filename);
                                        }
                                    }
                                }
                                in = new BufferedInputStream( connection.getInputStream() );
                                if ( log != null ) {
                                    // user wants output stored to a file
                                    fw = new PrintWriter( new FileWriter( log, append ) );
                                }
                                if ( property != null ) {
                                    // user wants output stored in a property
                                    sw = new StringWriter();
                                    pw = new PrintWriter( sw );
                                }
                                if (downloadFile != null) {
                                    dw = new FileOutputStream( downloadFile );
                                }
                                int byte_count = in.read(buffer, 0, buffer.length);
                                while ( byte_count > -1 ) {
                                    String line = new String( buffer, 0, byte_count );
                                    if ( currentRunner != this ) {
                                        break;
                                    }
                                    if ( verbose ) {
                                        log( line );
                                    }
                                    if ( fw != null ) {
                                        // write response to a file
                                        fw.print( line );
                                    }
                                    if ( pw != null ) {
                                        // write response to a property
                                        pw.print( line );
                                    }
                                    if (dw != null) {
                                        dw.write( buffer, 0, byte_count );   
                                    }
                                    byte_count = in.read(buffer, 0, buffer.length);
                                }
                            }
                            catch ( Exception e ) {
                                e.printStackTrace();
                                if ( failOnError ) {
                                    throw new BuildException( e, getLocation() );
                                }
                            }
                            finally {
                                try {
                                    in.close();
                                }
                                catch ( Exception e ) {
                                    // ignored
                                }
                                try {
                                    if ( fw != null ) {
                                        fw.flush();
                                        fw.close();
                                    }
                                }
                                catch ( Exception e ) {
                                    // ignored
                                }
                            }
                            if ( property != null && sw != null ) {
                                // save property
                                getProject().setProperty( property, sw.toString() );
                            }
                        }
                    };
                currentRunner = runner;
                runner.start();
                runner.join( maxwait );
                if ( runner.isAlive() ) {
                    currentRunner = null;
                    runner.interrupt();
                    if ( failOnError ) {
                        throw new BuildException( "maxwait exceeded, unable to receive data", getLocation() );
                    }
                }
            }
            if ( verbose )
                log( "Post complete." );
        }
        catch ( Exception e ) {
            if ( failOnError ) {
                throw new BuildException( e );
            }
        }
    }


    /**
     * Borrowed from Property -- load variables from a file
     *
     * @param file                file to load
     * @exception BuildException  Description of the Exception
     */
    private void loadFile( File file ) throws BuildException {
        Properties fileprops = new Properties();
        try {
            if ( file.exists() ) {
                FileInputStream fis = new FileInputStream( file );
                try {
                    fileprops.load( fis );
                }
                finally {
                    if ( fis != null ) {
                        fis.close();
                    }
                }
                addProperties( fileprops );
            }
            else {
                log( "Unable to find property file: " + file.getAbsolutePath(),
                     Project.MSG_VERBOSE );
            }
            log( "Post complete." );
        }
        catch ( Exception e ) {
            if ( failOnError ) {
                throw new BuildException( e );
            }
        }
    }


    /**
     * Builds and formats the message to send to the server. Message is UTF-8
     * encoded unless encoding has been explicitly set.
     *
     * @return   the message to send to the server.
     */
    private String getContent() {
        if ( propsFile != null ) {
            loadFile( propsFile );
        }

        if ( textProps != null ) {
            loadTextProps( textProps );
        }

        StringBuffer content = new StringBuffer();
        try {
            Enumeration en = props.keys();
            while ( en.hasMoreElements() ) {
                String name = ( String ) en.nextElement();
                String value = ( String ) props.get( name );
                content.append( URLEncoder.encode( name, encoding ) );
                content.append( "=" );
                content.append( URLEncoder.encode( value, encoding ) );
                if ( en.hasMoreElements() ) {
                    content.append( "&" );
                }
            }
        }
        catch ( IOException ex ) {
            if ( failOnError ) {
                throw new BuildException( ex, getLocation() );
            }
        }
        return content.toString();
    }


    /**
     * Description of the Method
     *
     * @param tp
     */
    private void loadTextProps( String tp ) {
        Properties p = new Properties();
        Project project = getProject();
        StringTokenizer st = new StringTokenizer( tp, "$" );
        while ( st.hasMoreTokens() ) {
            String token = st.nextToken();
            int start = token.indexOf( "{" );
            int end = token.indexOf( "}" );
            if ( start > -1 && end > -1 && end > start ) {
                String name = token.substring( start + 1, end - start );
                String value = project.getProperty( name );
                if ( value != null )
                    p.setProperty( name, value );
            }
        }
        addProperties( p );
    }


    /**
     * Borrowed from Property -- iterate through a set of properties, resolve
     * them, then assign them
     *
     * @param fileprops  The feature to be added to the Properties attribute
     */
    private void addProperties( Properties fileprops ) {
        resolveAllProperties( fileprops );
        Enumeration e = fileprops.keys();
        while ( e.hasMoreElements() ) {
            String name = ( String ) e.nextElement();
            String value = fileprops.getProperty( name );
            props.put( name, value );
        }
    }


    /**
     * Borrowed from Property -- resolve properties inside a properties
     * hashtable
     *
     * @param fileprops           Description of the Parameter
     * @exception BuildException  Description of the Exception
     */
    private void resolveAllProperties( Properties fileprops ) throws BuildException {
        for ( Enumeration e = fileprops.keys(); e.hasMoreElements(); ) {
            String name = ( String ) e.nextElement();
            String value = fileprops.getProperty( name );

            boolean resolved = false;
            while ( !resolved ) {
                Vector fragments = new Vector();
                Vector propertyRefs = new Vector();
                /// this is the Ant 1.5 way of doing it. The Ant 1.6 PropertyHelper
                /// should be used -- eventually.
                ProjectHelper.parsePropertyString( value, fragments,
                        propertyRefs );

                resolved = true;
                if ( propertyRefs.size() != 0 ) {
                    StringBuffer sb = new StringBuffer();
                    Enumeration i = fragments.elements();
                    Enumeration j = propertyRefs.elements();
                    while ( i.hasMoreElements() ) {
                        String fragment = ( String ) i.nextElement();
                        if ( fragment == null ) {
                            String propertyName = ( String ) j.nextElement();
                            if ( propertyName.equals( name ) ) {
                                throw new BuildException( "Property " + name
                                        + " was circularly "
                                        + "defined." );
                            }
                            fragment = getProject().getProperty( propertyName );
                            if ( fragment == null ) {
                                if ( fileprops.containsKey( propertyName ) ) {
                                    fragment = fileprops.getProperty( propertyName );
                                    resolved = false;
                                }
                                else {
                                    fragment = "${" + propertyName + "}";
                                }
                            }
                        }
                        sb.append( fragment );
                    }
                    value = sb.toString();
                    fileprops.put( name, value );
                }
            }
        }
    }

    /**
     * Represents a cookie.  See RFC 2109 and 2965.
     */
    public class Cookie {
        private String name;
        private String value;
        private String domain;
        private String path = "/";
        private String id;

        /**
         * @param raw the raw string abstracted from the header of an http response
         * for a single cookie.
         */
        public Cookie( String raw ) {
            String[] args = raw.split( "[;]" );
            for ( int i = 0; i < args.length; i++ ) {
                String part = args[ i ];
                int eq_index = part.indexOf("=");
                if (eq_index == -1)
                    continue;
                String first_part = part.substring(0, eq_index).trim();
                String second_part = part.substring(eq_index + 1);
                if ( i == 0 ) {
                    name = first_part;
                    value = second_part;
                }
                else if ( first_part.equalsIgnoreCase( "Path" ) )
                    path = second_part;
                else if ( first_part.equalsIgnoreCase( "Domain" ) )
                    domain = second_part;
            }
            if (name == null)
                throw new IllegalArgumentException("Raw cookie does not contain a cookie name.");
            if (path == null)
                path = "/";
            setId(path, name);
        }

        /**
         * @param name name of the cookie
         * @param value the value of the cookie
         */
        public Cookie( String name, String value ) {
            if (name == null)
                throw new IllegalArgumentException("Cookie name may not be null.");
            
            this.name = name;
            this.value = value;
            setId(name);
        }

        /**
         * @return the id of the cookie, used internally by Post to store the cookie
         * in a hashtable.
         */
        public String getId() {
            if (id == null)
                setId(path, name);
            return id.toString();
        }
        
        private void setId(String name) {
            setId(path, name);    
        }
        
        private void setId(String path, String name) {
            if (name == null)
                name = "";
            id = path + name;   
        }

        /**
         * @return the name of the cookie        
         */
        public String getName() {
            return name;
        }

        /**
         * @return the value of the cookie
         */
        public String getValue() {
            return value;
        }

        /**
         * @param domain the domain of the cookie        
         */
        public void setDomain( String domain ) {
            this.domain = domain;
        }

        /**
         * @return the domain of the cookie        
         */
        public String getDomain() {
            return domain;
        }

        /**
         * @param path the path of the cookie        
         */
        public void setPath( String path ) {
            this.path = path;
        }

        /**
         * @return the path of the cookie        
         */
        public String getPath() {
            return path;
        }

        /**
         * @return a Cookie formatted as a Cookie Version 1 string.  The returned
         * string is suitable for including with an http request.
         */
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append( name ).append( "=" ).append( value ).append( ";" );
            if ( domain != null )
                sb.append( "Domain=" ).append( domain ).append( ";" );
            if ( path != null )
                sb.append( "Path=" ).append( path ).append( ";" );
            sb.append( "Version=\"1\";" );
            return sb.toString();
        }
    }
}


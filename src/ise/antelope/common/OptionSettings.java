package ise.antelope.common;

import java.io.File;
import java.util.prefs.Preferences;

import org.apache.tools.ant.Project;

public class OptionSettings implements Constants {

   private File _build_file;
   private Preferences _prefs;
   private boolean _save_before_run = true;
   private boolean _show_all_targets = false;
   private boolean _show_targets_wo_desc = false;
   private boolean _show_targets_w_dot = false;
   private boolean _show_targets_w_dash = false;
   private int _message_level = Project.MSG_INFO;
   private boolean _show_build_events = true;
   private boolean _show_target_events = false;
   private boolean _show_task_events = false;
   private boolean _show_log_messages = true;
   private boolean _use_error_parsing = true;
   private boolean _show_performance_output = false;
   private boolean _auto_reload = true;

   /**
    * Loads the option settings for the given build file from the persistent
    * storage for settings. 
    */
   public OptionSettings( File build_file ) {
      if ( build_file == null )
         throw new IllegalArgumentException( "build file is null" );
      _build_file = build_file;
      load();
   }

   public void load() {
      int hashCode = _build_file.hashCode();
      _prefs = PREFS.node( String.valueOf( hashCode ) );
      _save_before_run = _prefs.getBoolean( SAVE_BEFORE_RUN, true );
      _show_all_targets = _prefs.getBoolean( SHOW_ALL_TARGETS, false );
      _show_targets_wo_desc = _prefs.getBoolean( SHOW_TARGETS_WO_DESC, false );
      _show_targets_w_dot = _prefs.getBoolean( SHOW_TARGETS_W_DOTS, false );
      _show_targets_w_dash = _prefs.getBoolean( SHOW_TARGETS_W_DASH, false );
      _message_level = _prefs.getInt( MSG_LEVEL, Project.MSG_INFO );
      _show_build_events = _prefs.getBoolean( SHOW_BUILD_EVENTS, true );
      _show_target_events = _prefs.getBoolean( SHOW_TARGET_EVENTS, false );
      _show_task_events = _prefs.getBoolean( SHOW_TASK_EVENTS, false );
      _show_log_messages = _prefs.getBoolean( SHOW_LOG_MSGS, true );
      _use_error_parsing = _prefs.getBoolean( USE_ERROR_PARSING, true );
      _show_performance_output = _prefs.getBoolean( SHOW_PERFORMANCE_OUTPUT, false );
      _auto_reload = _prefs.getBoolean( AUTO_RELOAD, false );
   }

   public void save() {
      if ( _prefs == null )
         return ;
      _prefs.putBoolean( SAVE_BEFORE_RUN, _save_before_run );
      _prefs.putInt( MSG_LEVEL, _message_level );
      _prefs.putBoolean( SHOW_BUILD_EVENTS, _show_build_events );
      _prefs.putBoolean( SHOW_LOG_MSGS, _show_log_messages );
      _prefs.putBoolean( SHOW_PERFORMANCE_OUTPUT, _show_performance_output );
      _prefs.putBoolean( SHOW_TARGET_EVENTS, _show_target_events );
      _prefs.putBoolean( SHOW_ALL_TARGETS, _show_all_targets );
      _prefs.putBoolean( SHOW_TARGETS_W_DASH, _show_targets_w_dash );
      _prefs.putBoolean( SHOW_TARGETS_W_DOTS, _show_targets_w_dot );
      _prefs.putBoolean( SHOW_TARGETS_WO_DESC, _show_targets_wo_desc );
      _prefs.putBoolean( SHOW_TASK_EVENTS, _show_task_events );
      _prefs.putBoolean( USE_ERROR_PARSING, _use_error_parsing );
      _prefs.putBoolean( AUTO_RELOAD, _auto_reload );
   }

   /**
    * Gets the preferences for the current build file.
    *
    * @return   the preferences for the current build file
    */
   public Preferences getPrefs() {
      return _prefs;
   }

   public void setSaveBeforeRun( boolean b ) {
      _save_before_run = b;
   }

   public boolean getSaveBeforeRun() {
      return _save_before_run;
   }

   public void setUseErrorParsing( boolean b ) {
      _use_error_parsing = b;
   }

   public void setShowPerformanceOutput( boolean b ) {
      _show_performance_output = b;
   }

   public boolean getUseErrorParsing() {
      return _use_error_parsing;
   }

   public boolean getShowPerformanceOutput() {
      return _show_performance_output;
   }

   public boolean getAutoReload() {
      return _auto_reload;
   }

   public void setAutoReload( boolean b ) {
      _auto_reload = b;
   }

   public void setShowAllTargets( boolean b ) {
      _show_all_targets = b;
   }

   public boolean getShowAllTargets() {
      return _show_all_targets;
   }

   public void setShowTargetsWODesc( boolean b ) {
      _show_targets_wo_desc = b;
   }

   public boolean getShowTargetsWODesc() {
      return _show_targets_wo_desc;
   }

   public void setShowTargetsWDot( boolean b ) {
      _show_targets_w_dot = b;
   }

   public void setShowTargetsWDash( boolean b ) {
      _show_targets_w_dash = b;
   }

   public boolean getShowTargetsWDot() {
      return _show_targets_w_dot;
   }

   public boolean getShowTargetsWDash() {
      return _show_targets_w_dash;
   }

   public void setMessageOutputLevel( int level ) {
      _message_level = level;
   }

   public int getMessageOutputLevel() {
      return _message_level;
   }

   public void setShowBuildEvents( boolean b ) {
      _show_build_events = b;
   }

   public boolean getShowBuildEvents() {
      return _show_build_events;
   }

   public void setShowTargetEvents( boolean b ) {
      _show_target_events = b;
   }

   public boolean getShowTargetEvents() {
      return _show_target_events;
   }

   public void setShowTaskEvents( boolean b ) {
      _show_task_events = b;
   }

   public boolean getShowTaskEvents() {
      return _show_task_events;
   }

   public void setShowLogMessages( boolean b ) {
      _show_log_messages = b;
   }

   public boolean getShowLogMessages() {
      return _show_log_messages;
   }
}
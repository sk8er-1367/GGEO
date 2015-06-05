/*
 * This file is part of DBasePsql package that converts dBase to PostgreSQL
 * Copyright (C) 2006 Tomasz Judycki, www.tv.com.pl
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * http://www.gnu.org/copyleft/gpl.html
 */

import com.theorem.misc.dbase.*;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This is a set of static methods to convert databases 
 * DBase II, DBase III+, DBase IV and FoxProV2 into PostgreSQL.
 * <p>
 * Package Simple DBase SQL by Michael Lecuyer is used for
 * accessing .dbf files.
 * <p>
 * Each .dbf file contains one table. For such table two files
 * are created: one with suffix _cr.sql containing table structure
 * in form of SQL statement CREATE TABLE and second with suffix _in.sql
 * with data in form of COPY statement (COPY is much faster than INSERT).
 * It is possible to modify table name and field names. Refer to
 * properties file (default: dbasepsql.properties) for details.
 * <p>
 * Way of usage is described by example in DbasePsqlUse.java
 */
public class DbasePsql {

    // copied from Dbase.Record
	/**
	* Field type: Memo (converted to TEXT)
	*/
    public static final char MEMO = 'M';		// Memo type field.
	/**
	* Field type: Char (converted to VARCHAR)
	*/
    public static final char CHAR = 'C';		// Character field.
	/**
	* Field type: Numeric (converted to INTEGER)
	*/
    public static final char NUMERIC = 'N';	// Numeric
	/**
	* Field type: Floating (converted to FLOAT)
	*/
    public static final char FLOATING = 'F';	// Floating point
	/**
	* Field type: Date (converted to DATE)
	*/
    public static final char DATE = 'D';		// Date
	/**
	* Field type: Logical (converted to BOOL)
	*/
    public static final char LOGICAL='L';	// Logical - ? Y y N n T t F f (? when not initialized).

	/**
	* Export all .dbf files from one directory.
	*
	* @param inputDir Path to the directory holding dbase tables.
	* @param handler Memo handler class name.
	* @param dbEncoding Encoding of dbase files.
	* @param locale Locale for decimal symbol. Pass null for default settings.
	* @param outputDir Path for creating .sql files.
	* @param fileEncoding Encoding of .sql files
	* @param rb Resource bundle for renaming tables and fields
	* @throws SQLException
	* @throws IOException
	*/
	public static void exportDir(String inputDir,
                                 String handler, String dbEncoding,  Locale locale,
                                 String outputDir, String fileEncoding,
                                 ResourceBundle rb)
                                 throws SQLException, IOException {
        String extName = ".dbf";
        File impDir = new File( inputDir );
        if( !impDir.exists() ) {
            System.err.println( "Directory " + inputDir + " does not exist." );
        } else if( !impDir.isDirectory() ) {
            System.err.println( inputDir + " is not a directory." );
        } else {
            NameExtFilter filter = new NameExtFilter( extName );
            File impFile[] = impDir.listFiles(filter);
            for( int i = 0; i < impFile.length; i++ ) {
                if( impFile[i].isFile() && !impFile[i].isHidden() ) {
                    String table = impFile[i].getName();
                    table = table.substring( 0, table.length() - extName.length() );
                    exportTable(inputDir, table,
                                handler, dbEncoding, locale,
                                outputDir, fileEncoding, rb);
                }
            }
        }
    }

	/**
	* Export single .dbf file.
	*
	* @param inputDir Path to the directory holding dbase tables.
	* @param table Name of .dbf file (no extenstion)
	* @param handler Memo handler class name.
	* @param dbEncoding Encoding of dbase files.
	* @param locale Locale for decimal symbol. Pass null for default settings.
	* @param outputDir Path for creating .sql files.
	* @param fileEncoding Encoding of .sql files
	* @param rb Resource bundle for renaming tables and fields
	* @throws SQLException
	* @throws IOException
	*/
	public static void exportTable(String inputDir, String table,
                                   String handler, String dbEncoding, Locale locale,
                                   String outputDir, String fileEncoding,
                                   ResourceBundle rb)
                                   throws SQLException, IOException {

		DBase db = new DBase(inputDir,locale);
		db.setMemoHandler(handler);
		String sql = "select * from " + table;
		db.exec(sql);

        // export structure
        FileOutputStream fos = new FileOutputStream(new File( outputDir, table + "_cr.sql" ));
        OutputStreamWriter osw = new OutputStreamWriter(fos, fileEncoding);
        PrintWriter out = new PrintWriter(osw);

        exportCreateTable( db, table, out, fileEncoding, rb );

        out.close();

        // export data
        fos = new FileOutputStream(new File( outputDir, table + "_in.sql" ));
        osw = new OutputStreamWriter(fos, fileEncoding);
        out = new PrintWriter(osw);

        exportCopy( db, table, dbEncoding, out, fileEncoding, rb );

        out.close();

		db.closeTable();
	}

	/**
	* Export table structure in form of CREATE TABLE statement.
	*
	* @param db DBase object for exported table.
	* @param table Name of exported table.
	* @param out PrintWriter to print SQL statement.
	* @param fileEncoding Encoding of .sql files
	* @param rb Resource bundle for renaming tables and fields
	* @throws SQLException
	*/
	public static void exportCreateTable( DBase db, String table,
                                          PrintWriter out, String fileEncoding,
                                          ResourceBundle rb ) throws SQLException {
		char [] colType = db.getColumnTypes();
		int [] colWidth = db.getColumnWidths();
		String [] colName = db.getColumnNames();

        out.println("SET client_encoding = '" + fileEncoding + "';");
        out.println();
        out.println("CREATE TABLE " + getNewTableName( table, rb ) + " (");
        for (int i = 0; i < colName.length; i++) {
            out.print("  " + getNewFieldName(table,colName[i],rb) + " " );
            switch (colType[i]) {
            case CHAR:
                out.print("VARCHAR(" + colWidth[i] + ")" );
                break;
            case MEMO:
                out.print("TEXT" );
                break;
            case DATE:
                out.print("DATE" );
                break;
            case NUMERIC:
                out.print("INTEGER" );
                break;
            case FLOATING:
                out.print("FLOAT" );
                break;
            case LOGICAL:
                out.print("BOOL" );
                break;
            default:
                out.print("Unrecognized type: " + colType[i] );
                break;
            } // switch
            out.print(" NULL");
            if (i < colName.length - 1) out.print(",");
            out.println();
        }
        out.println(");");
    }

	/**
	* Export records in form of COPY statement.
	*
	* @param db DBase object for exported table.
	* @param table Name of exported table.
	* @param dbEncoding Encoding of dbase files.
	* @param out PrintWriter to print SQL statement.
	* @param fileEncoding Encoding of .sql files
	* @param rb Resource bundle for renaming tables and fields
	* @throws SQLException
	*/
	public static void exportCopy( DBase db, String table, String dbEncoding,
                                   PrintWriter out, String fileEncoding,
                                   ResourceBundle rb ) throws SQLException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        int rowCount = 0;

		char [] colType = db.getColumnTypes();
		int [] colWidth = db.getColumnWidths();
		String [] colName = db.getColumnNames();

        out.println("SET client_encoding = '" + fileEncoding + "';");
        out.println();
        out.print("COPY " + getNewTableName( table, rb ) + "(");
        for (int i = 0; i < colName.length; i++) {
            if (i > 0) out.print(", ");
            out.print(getNewFieldName(table,colName[i],rb));
        }
        out.println(") FROM stdin;");

        System.out.println("Table: " + table);
		while (db.next()) {
            if (rowCount % 1000 == 0) System.out.print("\rRecords: " + rowCount );
            rowCount++;
			for (int i = 0; i < colName.length; i++) {
                String valueStr;
                switch (colType[i]) {
                case CHAR:
                    valueStr = db.getString(i);
                    valueStr = replaceSpecialChars( valueStr );
                    valueStr = convertCodePage( valueStr, dbEncoding, fileEncoding );
                    break;
                case MEMO:
                    valueStr = db.getMemo(colName[i]);
                    valueStr = replaceSpecialChars( valueStr );
                    valueStr = convertCodePage( valueStr, dbEncoding, fileEncoding );
                    break;
                case DATE:
                    Timestamp date = db.getDate(i);
                    if (date == null)
                        valueStr = null;
                    else
                        valueStr = "'" + formatter.format(date.getTime()) + "'";
                    break;
                case NUMERIC:
                    valueStr = String.valueOf(db.getInt(i));
                    break;
                case FLOATING:
                    valueStr = String.valueOf(db.getDouble(i));
                    break;
                case LOGICAL:
                    valueStr = String.valueOf(db.getBoolean(i));
                    break;
                default:
                    valueStr = "'Unrecognized type: " + colType[i] + "'";
                    break;
                } // switch

                if (i > 0) out.print("\t");
                if (valueStr == null)
                    out.print("\\N");
                else
                    out.print(valueStr);
            } // for
			out.println();
		} // while
		out.println("\\.");
        System.out.println("\rRecords: " + rowCount );
    }

	/**
	* Replace some special chars: CR, LF, TAB etc.
	*
	* @param str String to be converted.
	* @return Converted string.
	*/
    public static String replaceSpecialChars( String str ) {
        if (str != null && !str.equals("")) {
            // replace: \ -> \\
            str = str.replace("\\", "\\\\");
            // replace: CR+LF -> \r\n
            str = str.replace("\r\n", "\\r\\n");
            // replace: CR -> \r
            str = str.replace("\r", "\\r");
            // replace: LF -> \n
            str = str.replace("\n", "\\n");
            // replace: TAB -> \t
            str = str.replace("\t", "\\t");
            // replace: ' -> \'
            str = str.replace("'", "\\'");

            // replace special chars < 32 into space
            char space = 32;
            for (char c = 0; c < 32; c++) {
                str = str.replace( c, space );
            }
        }
        return str;
    }

	/**
	* Performs character encoding. This is weird as .dbf file is
    * opened using default system settings while real encoding
    * for this file could be very strange. For example in Poland
    * it is usually Mazovia: non-standard code page.
    * <p>
    * If dbEncoding is the same as fileEncoding then no conversion is made.
    * Supported conversions:<br>
    * mazovia -> windows-1250
	*
	* @param str String to be converted.
	* @param dbEncoding Encoding of dbase files.
	* @param fileEncoding Encoding of .sql files
	* @return Converted string.
	*/
    public static String convertCodePage( String str, String dbEncoding, String fileEncoding ) {
        if (str != null && !str.equals("") &&
            !dbEncoding.equals(fileEncoding)) {
            if (dbEncoding.equalsIgnoreCase("mazovia") &&
                fileEncoding.equalsIgnoreCase("windows-1250")) {
                //                O   o   N   n   A    a   L    l     S   s   X   x  Z   z    E   e   C   c   
              //char [] from = {163,162,165,164,143, 134,156, 146,  152,158,160,166,161,167,  144, 145, 149,141}; // mazovia ascii
                char [] from = {321,728,260,164,377,8224,347,8217,65533,382,160,166,711,167,65533,8216,8226,356}; // mazovia semi-unicode
              //char [] to   = {211,243,209,241,165,185,163,179,156,156,143,159,175,191,202,234,198,230}; // 1250
                char [] to   = {211,243,323,324,260,261,321,322,346,347,377,378,379,380,280,281,262,263}; // 1250 unicode
                str = convertStr( str, from, to );
            } else {
                str = "Cannot convert from " + dbEncoding + " to " + fileEncoding;
            }
        }
        return str;
    }

	/**
	* Performs actual conversion.
    *
	* @param str String to be converted.
	* @param from chars to be converted.
	* @param to To that values chars would be converted.
	* @return Converted string.
	*/
    public static String convertStr( String str, char [] from, char [] to) {
        if (str != null && !str.equals("")) {
            for (int i = 0; i < from.length; i++) {
                str = str.replace( from[i], to[i] );
            }
        }
        return str;
    }

	/**
	* Returns new table name. If no rename was required then doesn't change it.
    *
	* @param tableName Old table name.
	* @param rb Resource bundle for renaming tables and fields
	* @return Converted string.
	*/
    public static String getNewTableName( String tableName, ResourceBundle rb ) {
        String newName = getString(rb, tableName);
        if (newName == null) newName = tableName;
        return newName;
    }

	/**
	* Returns new field name. If no rename was required then doesn't change it.
    *
	* @param tableName Old table name.
	* @param colName Old field name.
	* @param rb Resource bundle for renaming tables and fields
	* @return Converted string.
	*/
    public static String getNewFieldName( String tableName, String colName, ResourceBundle rb ) {
        String newName = getString(rb, tableName + "." + colName);
        if (newName == null) newName = colName;
        return newName;
    }

	/**
	* Get string from resource bundle but return null instead of throwing
    * exception in case of failure.
    *
	* @param rb Resource bundle
	* @param key Key name.
	* @return Value for a key or null.
	*/
    public static String getString( ResourceBundle rb, String key ) {
        String value;
        try {
            value = rb.getString(key);
        } catch (MissingResourceException e) {
            value = null;
        }
        return value;
    }

	/**
	* Filter for files with given extension.
	*/
	static class NameExtFilter implements FilenameFilter {
		private String nameExt;

		public NameExtFilter( String nameExt ) {
			this.nameExt = nameExt;             
		}

		public boolean accept(File dir, String name) {
			return (name.endsWith(this.nameExt));
		}
	}



}


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
import java.util.*;
import java.text.*;

/**
 * Way of usage:
 * <p>
 * java -Dfile.encoding=windows-1250 -cp .;dbasepsql.jar DbasePsqlUse [propname]
 * <p>
 * (Under Unix/Linux use colon ':' instead of semicolon ';' in -cp)
 * <p>
 * By default parameters are read from <i>dbasepsql.properties</i> but you may
 * pass another property file as a parameter
 * 
 */
public class DbasePsqlUse {

	public static void main(String arg[]) {

        String propName;
        if (arg.length < 1)
            propName = "dbasepsql";
        else
            propName = arg[0];

        ResourceBundle rb = ResourceBundle.getBundle(propName);

        String inputDir = rb.getString("dbasepsqlInputDir");
        String outputDir = rb.getString("dbasepsqlOutputDir");
        String handler = rb.getString("dbasepsqlMemoHandler");
        String dbEncoding = rb.getString("dbasepsqlDbEncoding");
        String fileEncoding = rb.getString("dbasepsqlFileEncoding");
        boolean europeanDec = rb.getString("dbasepsqlEuropeanDec").equalsIgnoreCase("yes");

        String singleTable = DbasePsql.getString(rb, "dbasepsqlTable");

        Locale locale = europeanDec ? Locale.GERMAN : Locale.US;

        try {
            if (singleTable == null) {
                DbasePsql.exportDir( inputDir,
                                     handler, dbEncoding, locale,
                                     outputDir, fileEncoding, rb);
            } else {
                DbasePsql.exportTable( inputDir, singleTable,
                                       handler, dbEncoding, locale,
                                       outputDir, fileEncoding, rb);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
	}
}


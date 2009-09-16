/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.shell.impl;


import java.io.PrintStream;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.felix.shell.Command;


/**
 * Command to display, set and modify system properties
 * Usage:
 * sysprop                 -> displays all the system properties
 * sysprop [key]           -> displays the [key] property 
 * sysprop [key] [value]   -> set the property [key] to [value]
 */
public class SystemPropertiesCommandImpl implements Command
{

    public void execute( String line, PrintStream out, PrintStream err )
    {
        StringTokenizer st = new StringTokenizer( line );
        int tokens = st.countTokens();

        if ( tokens == 1 )
        {
            printAll( out );
        }
        else
        {
            st.nextToken();
            String propertyKey = st.nextToken();

            if ( tokens == 2 )
            {
                out.println( propertyKey + "=" + System.getProperty( propertyKey ) );
            }
            else
            {
                String value = st.nextToken();
                System.setProperty( propertyKey, value );
                out.println( "Set " + propertyKey + "=" + value );
            }
        }
    }


    private void printAll( PrintStream out )
    {
        out.println( "-------System properties------" );
        for ( Iterator keyIterator = System.getProperties().keySet().iterator(); keyIterator.hasNext(); )
        {
            Object key = keyIterator.next();
            out.println( key.toString() + "=" + System.getProperty( key.toString() ) );
        }
    }


    public String getName()
    {
        return "sysprop";
    }


    public String getShortDescription()
    {
        return "Display, set and modify system properties";
    }


    public String getUsage()
    {
        return "sysprop [<key>] [<value>]";
    }

}

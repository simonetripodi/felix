/*
 *   Copyright 2006 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.apache.felix.framework.searchpolicy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.felix.framework.Logger;
import org.apache.felix.moduleloader.*;

public class ContentLoaderImpl implements IContentLoader
{
    private Logger m_logger = null;
    private IContent m_content = null;
    private IContent[] m_contentPath = null;
    private ISearchPolicy m_searchPolicy = null;
    private IURLPolicy m_urlPolicy = null;
    private ContentClassLoader m_classLoader = null;

    public ContentLoaderImpl(Logger logger, IContent content, IContent[] contentPath)
    {
        m_logger = logger;
        m_content = content;
        m_contentPath = contentPath;
    }

    public Logger getLogger()
    {
        return m_logger;
    }

    public void open()
    {
        m_content.open();
        for (int i = 0; (m_contentPath != null) && (i < m_contentPath.length); i++)
        {
            m_contentPath[i].open();
        }
    }

    public void close()
    {
        m_content.close();
        for (int i = 0; (m_contentPath != null) && (i < m_contentPath.length); i++)
        {
            m_contentPath[i].close();
        }
    }

    public IContent getContent()
    {
        return m_content;
    }

    public IContent[] getClassPath()
    {
        return m_contentPath;
    }

    public void setSearchPolicy(ISearchPolicy searchPolicy)
    {
        m_searchPolicy = searchPolicy;
    }

    public ISearchPolicy getSearchPolicy()
    {
        return m_searchPolicy;
    }

    public void setURLPolicy(IURLPolicy urlPolicy)
    {
        m_urlPolicy = urlPolicy;
    }

    public IURLPolicy getURLPolicy()
    {
        return m_urlPolicy;
    }

    public Class getClass(String name)
    {
        if (m_classLoader == null)
        {
            m_classLoader = new ContentClassLoader(this);
        }

        try
        {
            return m_classLoader.loadClassFromModule(name);
        }
        catch (ClassNotFoundException ex)
        {
            return null;
        }
    }

    public URL getResource(String name)
    {
        URL url = null;
        // Remove leading slash, if present.
        if (name.startsWith("/"))
        {
            name = name.substring(1);
        }

        // Check the module class path.
        for (int i = 0;
            (url == null) &&
            (i < getClassPath().length); i++)
        {
            if (getClassPath()[i].hasEntry(name))
            {
                url = getURLPolicy().createURL((i + 1) + "/" + name);
            }
        }

        return url;
    }

    protected Enumeration getResources(String name)
    {
        Vector v = new Vector();

        // Remove leading slash, if present.
        if (name.startsWith("/"))
        {
            name = name.substring(1);
        }

        // Check the module class path.
        for (int i = 0; i < getClassPath().length; i++)
        {
            if (getClassPath()[i].hasEntry(name))
            {
                // Use the class path index + 1 for creating the path so
                // that we can differentiate between module content URLs
                // (where the path will start with 0) and module class
                // path URLs.
                v.addElement(getURLPolicy().createURL((i + 1) + "/" + name));
            }
        }

        return v.elements();
    }

    // TODO: API: Investigate making this an API call.
    public URL getResourceFromContent(String name)
    {
        URL url = null;

        // Remove leading slash, if present.
        if (name.startsWith("/"))
        {
            name = name.substring(1);
        }

        // Check the module content.
        if (getContent().hasEntry(name))
        {
            // Module content URLs start with 0, whereas module
            // class path URLs start with the index into the class
            // path + 1.
            url = getURLPolicy().createURL("0/" + name);
        }

        return url;
    }

    public InputStream getResourceAsStream(String name)
        throws IOException
    {
        if (name.startsWith("/"))
        {
            name = name.substring(1);
        }
        // The name is the path contructed like this:
        // <index> / <relative-resource-path>
        // where <index> == 0 is the module content
        // and <index> > 0 is the index into the class
        // path - 1.
        int idx = Integer.parseInt(name.substring(0, name.indexOf('/')));
        name = name.substring(name.indexOf('/') + 1);
        if (idx == 0)
        {
            return m_content.getEntryAsStream(name);
        }
        return m_contentPath[idx - 1].getEntryAsStream(name);
    }

    public String toString()
    {
        return m_searchPolicy.toString();
    }
}
/*
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jitsi.meet;

import org.jitsi.impl.neomedia.*;
import org.jitsi.service.configuration.*;
import org.jitsi.util.*;

import java.io.*;
import java.util.*;

/**
 * The class describes the list of OSGi bundle activators and their order of
 * startup. The list returned by class implementation can be overridden by
 * placing "bundles.txt" file in "home directory" specified by
 * SC_HOME_DIR_LOCATION/SC_HOME_DIR_NAME config properties.
 *
 * @author Lyubomir Marinov
 * @author Pawel Domas
 * @author George Politis
 */
public abstract class OSGiBundleConfig
{
    /**
     * The default filename of the bundles launch sequence file. This class
     * expects to find that file in SC_HOME_DIR_LOCATION/SC_HOME_DIR_NAME.
     */
    private static final String BUNDLES_FILE = "bundles.txt";

    /**
     * Loads list of OSGi bundles to run from specified file.
     *
     * @param filename the name of the file that contains a list of OSGi
     * {@code BundleActivator} classes. Full class names should be placed on
     * separate lines.
     * @return the array of OSGi {@code BundleActivator} class names to be
     * started in order. Single class name per {@code String} array.
     */
    protected String[][] loadBundlesFromFile(String filename)
    {
        File file = ConfigUtils.getAbsoluteFile(filename, null);

        if (file == null || !file.exists())
        {
            return null;
        }

        List<String[]> lines = new ArrayList<String[]>();

        Scanner input = null;
        try
        {
            input = new Scanner(file);

            while(input.hasNextLine())
            {
                String line = input.nextLine();
                if (!StringUtils.isNullOrEmpty(line))
                {
                    lines.add(new String[] { line.trim() });
                }
            }
        }
        catch (FileNotFoundException e)
        {
            return null;
        }
        finally
        {
            if (input != null)
            {
                input.close();
            }
        }

        return lines.isEmpty()
            ? null : lines.toArray(new String[lines.size()][]);
    }

    /**
     * Gets the list of the OSGi bundles to launch. It either loads that list
     * from SC_HOME_DIR_LOCATION/SC_HOME_DIR_NAME/BUNDLES_FILE, or, if that file
     * doesn't exist, from the result of {@link #getBundlesImpl()}.
     *
     * @return  The locations of the OSGi bundles (or rather of the class files
     * of their <tt>BundleActivator</tt> implementations). An element of the
     * <tt>BUNDLES</tt> array is an array of <tt>String</tt>s and represents an
     * OSGi start level.
     */
    public String[][] getBundles()
    {
        String[][] bundlesFromFile = loadBundlesFromFile(BUNDLES_FILE);
        if (bundlesFromFile != null)
        {
            return bundlesFromFile;
        }
        else
        {
            return getBundlesImpl();
        }
    }

    /**
     * Returns default list of OSGi bundle activator classes.
     *
     * @return The locations of the OSGi bundles (or rather of the class files
     * of their <tt>BundleActivator</tt> implementations). An element of the
     * <tt>BUNDLES</tt> array is an array of <tt>String</tt>s and represents an
     * OSGi start level.
     */
    protected abstract String[][] getBundlesImpl();

    /**
     * Returns a map which contains default system properties map common for all
     * server components. Currently we have the following values there:
     * <li>{@link ConfigurationService#PNAME_CONFIGURATION_FILE_IS_READ_ONLY}
     * = true</li>
     * <li>{@link MediaServiceImpl#DISABLE_AUDIO_SUPPORT_PNAME} = true</li>
     * <li>{@link MediaServiceImpl#DISABLE_VIDEO_SUPPORT_PNAME} = true</li>
     */
    protected Map<String, String> getSystemPropertyDefaults()
    {
        /*
         * XXX A default System property value specified bellow will eventually
         * be set only if the System property in question does not have a value
         * set yet.
         */

        Map<String,String> defaults = new HashMap<String,String>();
        String true_ = Boolean.toString(true);
        //String false_ = Boolean.toString(false);

        /*
         * The design at the time of this writing considers the configuration
         * file read-only (in a read-only directory) and provides only manual
         * editing for it.
         */
        defaults.put(
            ConfigurationService.PNAME_CONFIGURATION_FILE_IS_READ_ONLY,
            true_);

        defaults.put(
            MediaServiceImpl.DISABLE_AUDIO_SUPPORT_PNAME,
            true_);

        defaults.put(
            MediaServiceImpl.DISABLE_VIDEO_SUPPORT_PNAME,
            true_);

        return defaults;
    }

    /**
     * Sets default system properties required to run Jitsi libraries inside of
     * a server component. The purpose of that is to disable audio/video input
     * devices etc.
     */
    public void setSystemPropertyDefaults()
    {
        Map<String, String> defaults = getSystemPropertyDefaults();
        for (Map.Entry<String,String> e : defaults.entrySet())
        {
            String key = e.getKey();

            if (System.getProperty(key) == null)
                System.setProperty(key, e.getValue());
        }
    }
}

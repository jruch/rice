/*
 * Copyright 2005-2007 The Kuali Foundation
 * 
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.config.Config;
import org.kuali.rice.core.config.ConfigContext;
import org.kuali.rice.core.exception.RiceRuntimeException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;

/**
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class RiceUtilities {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(RiceUtilities.class);
	private static final String[] TRUE_VALUES = new String[] { "true", "yes", "t", "y" };
	
	private static String instanceIpAddress = null;
	private static String instanceHostName = null;
	
	public static boolean getBooleanValueForString(String value, boolean defaultValue) {
		if (!StringUtils.isBlank(value)) {
			for (String trueValue : TRUE_VALUES) {
				if (value.equalsIgnoreCase(trueValue)) {
					return true;
				}
			}
			return false;
		}
		return defaultValue;
	}
	
    public static String collectStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }
    
    public static String getIpNumber() {
        if ( instanceIpAddress == null ) {
            // protect from running upon startup
            if ( ConfigContext.getCurrentContextConfig() != null ) {
                // attempt to load from environment
                String ip = System.getProperty("host.ip");
                if ( StringUtils.isBlank(ip) ) {
                    ip = ConfigContext.getCurrentContextConfig().getProperty("host.ip");
                }
                // if not set at all just return
                if ( StringUtils.isBlank(ip) ) {                    
                    return getCurrentEnvironmentNetworkIp();
                } else { 
                    // ok - it was set in configuration or by this method, set it permanently for this instance
                    instanceIpAddress = ip;
                }
            } else {
                // prior to startup, just return it
                return getCurrentEnvironmentNetworkIp();
            }
        }
        return instanceIpAddress;
    }

    /** * @return the current environment's IP address, taking into account the Internet connection to any of the available
	 *         machine's Network interfaces. Examples of the outputs can be in octatos or in IPV6 format.
	 *
	 *         fec0:0:0:9:213:e8ff:fef1:b717%4 siteLocal: true isLoopback: false isIPV6: true
	 *         ============================================ 130.212.150.216 <<<<<<<<<<<------------- This is the one we
	 *         want to grab so that we can. siteLocal: false address the DSP on the network. isLoopback: false isIPV6:
	 *         false ==> lo ============================================ 0:0:0:0:0:0:0:1%1 siteLocal: false isLoopback:
	 *         true isIPV6: true ============================================ 127.0.0.1 siteLocal: false isLoopback:
	 *         true isIPV6: false
	 */
	public static String getCurrentEnvironmentNetworkIp() {
	     Enumeration<NetworkInterface> netInterfaces = null;
	     try {
	          netInterfaces = NetworkInterface.getNetworkInterfaces();
	     } catch (SocketException e) {
	          LOG.error("Somehow we have a socket error...",e);
	          return "127.0.0.1";
	     }

	     while (netInterfaces.hasMoreElements()) {
	          NetworkInterface ni = netInterfaces.nextElement();
	          Enumeration<InetAddress> address = ni.getInetAddresses();
	          while (address.hasMoreElements()) {
	               InetAddress addr = address.nextElement();
	               if (!addr.isLoopbackAddress() && !addr.isSiteLocalAddress()
	                         && !(addr.getHostAddress().indexOf(":") > -1)) {
	                    return addr.getHostAddress();
	               }
	          }
	     }
	     try {
	          return InetAddress.getLocalHost().getHostAddress();
	     } catch (UnknownHostException e) {
	          return "127.0.0.1";
	     }
	}
	
	
	public static String getHostName() {
        if ( instanceHostName == null ) {
            try {
                // protect from running upon startup
                if ( ConfigContext.getCurrentContextConfig() != null ) {
                    String host = System.getProperty("host.name");
                    if ( StringUtils.isBlank(host) ) {
                        host = ConfigContext.getCurrentContextConfig().getProperty("host.name");
                    }
                    // if not set at all just return
                    if ( StringUtils.isBlank(host) ) {
                        return InetAddress.getByName( getCurrentEnvironmentNetworkIp() ).getHostName();
                    } else { 
                        // ok - it was set in configuration or by this method, set it permanently for this instance
                        instanceHostName = host;
                    }
                } else {
                    // prior to startup, just return it
                    return InetAddress.getByName( getCurrentEnvironmentNetworkIp() ).getHostName();
                }
            } catch ( Exception ex ) {
                return "localhost";
            }
        }
        return instanceHostName;
	}

	/**
	 * The standard Spring FileSystemResourceLoader does not support normal absolute file paths
	 * for historical backwards-compatibility reasons.  This class simply circumvents that behavior
	 * to allow proper interpretation of absolute paths (i.e. not stripping a leading slash)  
	 */
	private static class AbsoluteFileSystemResourceLoader extends FileSystemResourceLoader {
        @Override
        protected Resource getResourceByPath(String path) {
            return new FileSystemResource(path);
        }
	}

	/**
	 * Attempts to retrieve the resource stream.
	 * 
	 * @param resourceLoc resource location; syntax supported by {@link DefaultResourceLoader} 
	 * @return the resource stream or null if the resource could not be obtained
	 * @throws MalformedURLException
	 * @throws IOException
	 * @see DefaultResourceLoader
	 */
	public static InputStream getResourceAsStream(String resourceLoc) throws MalformedURLException, IOException {
	    AbsoluteFileSystemResourceLoader rl = new AbsoluteFileSystemResourceLoader();
	    rl.setClassLoader(Thread.currentThread().getContextClassLoader());
	    Resource r = rl.getResource(resourceLoc);
	    if (r.exists()) {
	        return r.getInputStream();
	    } else {
	        return null;
	    }
//	    
//        if (resourceLoc.lastIndexOf("classpath:") > -1) {
//            String configName = resourceLoc.split("classpath:")[1];
//            /*ClassPathResource cpr = new  ClassPathResource(configName, Thread.currentThread().getContextClassLoader());
//            if (cpr.exists()) {
//                return cpr.getInputStream();
//            } else {
//                return null;
//            }*/
//            return Thread.currentThread().getContextClassLoader().getResourceAsStream(configName);
//        } else if (resourceLoc.lastIndexOf("http://") > -1 || resourceLoc.lastIndexOf("file:/") > -1) {
//            return new URL(resourceLoc).openStream();
//        } else {
//            try {
//                return new FileInputStream(resourceLoc);
//            } catch (FileNotFoundException e) {
//                return null; // logged by caller
//            }
//        }
    }

	/**
     * This method searches for an exception of the specified type in the stack trace of the given
     * exception.
     * @param topLevelException the exception whose stack to traverse
     * @param exceptionClass the exception class to look for
     * @return the first instance of an exception of the specified class if found, or null otherwise
     */
    public static <T extends Throwable> T findExceptionInStack(Throwable topLevelException, Class<T> exceptionClass) {
        Throwable t = topLevelException;
        while (t != null) {
            if (exceptionClass.isAssignableFrom(t.getClass())) return (T) t;
            t = t.getCause();
        }
        return null;
    }
}
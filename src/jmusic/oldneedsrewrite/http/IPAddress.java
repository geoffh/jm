package jmusic.oldneedsrewrite.http;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Vector;

public class IPAddress {
    public static final String sPreferredInterface = "mediacenter.interface";
    private static String sPreferredIPAddress;

    public static synchronized String getIPAddress() {
        if ( sPreferredIPAddress == null ) {
            sPreferredIPAddress = getPreferredIPAddress();
            if ( sPreferredIPAddress == null ) {
                sPreferredIPAddress = getAddress();
            }
        }
        return sPreferredIPAddress;
    }
    
    public static Vector< String > getInterfaceNames() {
        Vector< String > theInterfaceNames = new Vector<>();
        try {
            Enumeration<NetworkInterface> theInterfaces =
                    NetworkInterface.getNetworkInterfaces();
            while ( theInterfaces.hasMoreElements() ) {
                NetworkInterface theInterface = theInterfaces.nextElement();
                if ( theInterface.isPointToPoint() || theInterface.isLoopback() ) {
                    continue;
                }
                theInterfaceNames.add( theInterface.getName() );
            }
        } catch( Exception theException ) {
            theException.printStackTrace();
        }
        return theInterfaceNames;
    }
    
    public static String getPreferredInterfaceName() {
        return null;
    }
    
    private static String getPreferredIPAddress() {
        String thePreferredIPAddress = null;
        String thePreferredInterfaceName = getPreferredInterfaceName();
        if ( thePreferredInterfaceName != null ) {
            thePreferredInterfaceName = thePreferredInterfaceName.trim();
            if ( thePreferredInterfaceName.length() > 0 ) {
                try {
                    Enumeration<NetworkInterface> theInterfaces =
                            NetworkInterface.getNetworkInterfaces();
                    while ( theInterfaces.hasMoreElements() ) {
                        NetworkInterface theInterface = theInterfaces.nextElement();
                        if ( theInterface.isPointToPoint() || theInterface.isLoopback() ) {
                            continue;
                        }
                        if ( thePreferredInterfaceName.equals( theInterface.getName() ) ) {
                            Enumeration< InetAddress > theAddresses =
                                theInterface.getInetAddresses();
                            while ( theAddresses.hasMoreElements() ) {
                                InetAddress theAddress = theAddresses.nextElement();
                                String theIPAddress = theAddress.getHostAddress();
                                if ( theIPAddress.indexOf( "." ) != -1 &&
                                    ! "127.0.0.1".equals( theIPAddress ) ) {
                                    thePreferredIPAddress = theIPAddress;
                                    break;
                                }
                            }
                            if ( thePreferredIPAddress != null ) {
                                break;
                            }
                        }
                    }
                } catch( Exception theException ) {
                    theException.printStackTrace();
                }
            }
        }
        return thePreferredIPAddress;
    }

    private static String getAddress() {
        try {
            Enumeration<NetworkInterface> theInterfaces =
                    NetworkInterface.getNetworkInterfaces();
            while ( theInterfaces.hasMoreElements() ) {
                NetworkInterface theInterface = theInterfaces.nextElement();
                if ( theInterface.isPointToPoint() ) {
                    continue;
                }
                Enumeration<InetAddress> theAddresses =
                        theInterface.getInetAddresses();
                while ( theAddresses.hasMoreElements() ) {
                    InetAddress theAddress = theAddresses.nextElement();
                    String theIPAddress = theAddress.getHostAddress();
                    if ( theIPAddress.indexOf( "." ) != -1 &&
                            ! "127.0.0.1".equals( theIPAddress ) ) {
                        return theIPAddress;
                    }
                }
            }
        } catch( Exception theException ) {
            return "0.0.0.0";
        }
        return null;
    }
}
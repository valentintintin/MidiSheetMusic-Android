package com.midisheetmusic;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

// Use UDP with WARLS packets => https://kno.wled.ge/interfaces/udp-realtime/#udp-realtime
public class WLed {

    private final InetAddress inetAddress;
    private final int port;
    private final int startPos;
    private final byte[] ledAffectation;
    
    public WLed(String host, int port, int startPos, byte[] ledAffectation) throws UnknownHostException {
        this.inetAddress = Inet4Address.getByName(host);
        this.port = port;
        this.startPos = startPos;
        this.ledAffectation = ledAffectation;
    }

    public void showNote(int note, boolean pressed) {
        byte[] colorLight = new byte[] { 50, 25, 100 };
        byte[] colorNight = new byte[] { 0, 0, 0 };
        byte[] color = pressed ? colorLight : colorNight;
        
        /*
        Byte 0 : 1 for WARLS
        Byte 1 : 5s before timeout WLed
        Byte 2 : Led N
        Byte 3-5 : Color R G B
         */
        sendToWLed(new byte[] { 1, 5, (byte) note, color[0], color[1], color[2]});
    }
    
    private void sendToWLed(final byte[] data) {
        Thread thread = new Thread(() -> {
            try (DatagramSocket ds = new DatagramSocket()) {
                DatagramPacket dp = new DatagramPacket(data, 0, data.length, inetAddress, port);

                Log.i(WLed.class.getName(), "Send packet to " + inetAddress.getHostAddress() + ':' + port + ". Note number : " + data[2]);
                
                ds.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }
}

package com.midisheetmusic;

import android.graphics.Color;
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
    private final Number[] ledAffectation;
    private int[] noteColors;
    private final int colorBlack = Color.BLACK;

    public WLed(String host, int port, Number[] ledAffectation, int[] noteColors) throws UnknownHostException {
        this.inetAddress = Inet4Address.getByName(host);
        this.port = port;
        this.ledAffectation = ledAffectation;
        this.noteColors = noteColors;
    }

    public void showNote(int note, boolean pressed) {
        Number noteLed = ledAffectation[note - 21]; // 21 first note in Midi
        
        if (noteLed == null) {
            return;
        }

        int noteOctave = (note - 21) % noteColors.length;

        int color = pressed ? noteColors[noteOctave] : colorBlack;
        
        /*
        Byte 0 : 1 for WARLS
        Byte 1 : seconds before timeout WLed
        Byte 2 : Led N
        Byte 3-5 : Color R G B
         */
        sendToWLed(new byte[] { 
                1, 
                2, 
                noteLed.byteValue(),
                (byte) (Color.red(color) / 20),
                (byte) (Color.green(color) / 20),
                (byte) (Color.blue(color) / 20)
        });
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

package pob.pob_project.gui;

import pob.pob_project.crc.CRCUtil;

import java.util.zip.CRC32;

public class MainApp {

    public static void main(String[] args) {

        System.out.println("Hello World");

        String message = "Hello World";
        String poly = "10101";

        CRCUtil crcUtil = new CRCUtil();

        System.out.println(crcUtil.appendCRC(message, poly));

    }
}

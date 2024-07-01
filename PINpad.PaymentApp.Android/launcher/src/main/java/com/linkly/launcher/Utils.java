package com.linkly.launcher;

import com.pax.api.PortException;
import com.pax.api.PortManager;

import timber.log.Timber;

/**
 * THIS ENTIRE FILE WAS PROVIDED BY PAX. apologies for magic numbers.
 * Refer to PDF "Wireless Base PIN Port Communication Protocol" attached to Jira case IAAS-1644 for details of these commands
 */

public class Utils {
    private static String TAG = "PAX";

    public static class CmdResult {
        public boolean result;
        public String errString = "";
        public String display = "";
        public byte[] all;
        public byte[] data;
    }

    public static CmdResult sendCmd(int cmd1, int cmd2) {

        CmdResult cmdResult = new CmdResult();
        cmdResult.result = false;
        cmdResult.display += "sendcmd: " + cmd1 + "," + cmd2 + "\n";

        byte[] head = null;
        try {
            PortManager port = PortManager.getInstance();
            port.portOpen((byte) 0, "115200,8,n,1");

            byte[] cmd = new byte[9];
            cmd[0] = 0x08;
            cmd[1] = (byte) 0xFF;
            cmd[2] = (byte) 0xAA;
            cmd[3] = 0x55;
            cmd[4] = (byte) cmd1;
            cmd[5] = (byte) cmd2;
            cmd[6] = 0x00;
            cmd[7] = 0x00;
            cmd[8] = ByteUtils.getLrc(cmd, 8);

            cmdResult.display += "cmd=" + ByteUtils.bytes2HexString(cmd, cmd.length) + "\n";

            port.portReset((byte) 0);
            port.portSends((byte) 0, cmd);

            long endTime = System.currentTimeMillis() + 2000;
            while (endTime > System.currentTimeMillis()) {
                head = port.portRecvs((byte) 0, 1, 1000);
                if (head != null && head.length > 0 && head[0] == 0x08) {
                    head = port.portRecvs((byte) 0, 7, 1000);
                    break;
                }
            }

            if (head == null) {
                cmdResult.errString = "head is null";
                return cmdResult;
            }

            cmdResult.display += "head=" + ByteUtils.bytes2HexString(head, head.length) + "\n";

            //0x08 0xFF 0xAA 0x55 0x90 0x00 0x00 0x00
            if (head.length != 7) {
                cmdResult.errString = "head length is error:" + head.length;
                return cmdResult;
            }

            if (head[3] != (byte) 0x90 || head[4] != 0x00) {
                cmdResult.errString = String.format("head result is error:%02x,%02x", head[3], head[4]);
                return cmdResult;
            }

            int len = ((head[5] & 0x0ff) << 8) | (head[6] & 0x0ff);
            cmdResult.display += "datalen=" + len + "\n";

            byte[] data = null;
            if (len > 0) {
                data = port.portRecvs((byte) 0, len, 2000);
                if (data.length != len) {
                    cmdResult.errString = String.format("expect length is %d,recv %d", len, data.length);
                    return cmdResult;
                }
            }

            byte[] lrc = port.portRecvs((byte) 0, 1, 500);
            byte[] all = new byte[8 + len];
            all[0] = 0x08;
            System.arraycopy(head, 0, all, 1, head.length);
            if (data != null) {
                System.arraycopy(data, 0, all, 8, data.length);
            }
            byte lrx = ByteUtils.getLrc(all);
            if (lrc[0] != lrx) {
                cmdResult.errString = String.format("LRC=0x%02x,0x%02x", lrc[0], lrx);
                return cmdResult;
            }

            cmdResult.result = true;
            cmdResult.all = all;
            cmdResult.data = data;
            return cmdResult;

        } catch (PortException e) {
            Timber.w(e);
            cmdResult.errString = "PortException:" + e.exceptionCode;
            return cmdResult;
        }

    }
}

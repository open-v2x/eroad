package cn.eroad.rad.util;

/**
 * <p>
 * （1）、预置1个16位的寄存器为十六进制FFFF（即全为1），称此寄存器为CRC寄存器；
 * （2）、把第一个8位二进制数据（既通讯信息帧的第一个字节）与16位的CRC寄存器的低8位相异或，把结果放于CRC寄存器，高八位数据不变；
 * （3）、把CRC寄存器的内容右移一位（朝低位）用0填补最高位，并检查右移后的移出位；
 * （4）、如果移出位为0：重复第3步（再次右移一位）；如果移出位为1，CRC寄存器与多项式A001（1010 0000 0000 0001）进行异或；
 * （5）、重复步骤3和4，直到右移8次，这样整个8位数据全部进行了处理；
 * （6）、重复步骤2到步骤5，进行通讯信息帧下一个字节的处理；
 * （7）、将该通讯信息帧所有字节按上述步骤计算完成后，得到的16位CRC寄存器的高、低字节进行交换；
 * （8）、最后得到的CRC寄存器内容即为：CRC码。
 * <p>
 * Author:Water
 * Time:2018/11/19 0019 15:03
 */
public class CRC16 {

    // CRC16 MODUS本文采用这个
    public static String getCRC16Result(String s) {
        int crc = 0x0000ffff;
        for (int i = 0; i < s.length(); i = i + 2) {
            int crcL = crc & 0x000000FF;//低八位
            int crcH = crc & 0x0000FF00;//高八位
            String CRCIn = s.substring(i, i + 2);

            int a = Integer.parseInt(CRCIn, 16);//待处理数据转16进制
            crc = crcH + crcL ^ a;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x0001) == 0) {
                    crc = crc >> 1;
                } else {
                    crc >>= 1;
                    crc = crc ^ 0xA001;
                }
            }
        }
        return Integer.toHexString(crc);
    }

}

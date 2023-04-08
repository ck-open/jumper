package com.ck.qr.swetake.util;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class Qrcode {
    // 数据包地址
    static final String QRCODE_DATA_PATH = "qrcode_data";
    char qrcodeErrorCorrect = 'M';
    char qrcodeEncodeMode = 'B';
    int qrcodeVersion = 0;
    int qrcodeStructureappendN = 0;
    int qrcodeStructureappendM = 0;
    int qrcodeStructureappendParity = 0;
    String qrcodeStructureappendOriginaldata = "";

    public Qrcode() {
    }

    public void setQrcodeErrorCorrect(char ecc) {
        this.qrcodeErrorCorrect = ecc;
    }

    public char getQrcodeErrorCorrect() {
        return this.qrcodeErrorCorrect;
    }

    public int getQrcodeVersion() {
        return this.qrcodeVersion;
    }

    public void setQrcodeVersion(int ver) {
        if (ver >= 0 && ver <= 40) {
            this.qrcodeVersion = ver;
        }

    }

    public void setQrcodeEncodeMode(char encMode) {
        this.qrcodeEncodeMode = encMode;
    }

    public char getQrcodeEncodeMode() {
        return this.qrcodeEncodeMode;
    }

    public void setStructureappend(int m, int n, int p) {
        if (n > 1 && n <= 16 && m > 0 && m <= 16 && p >= 0 && p <= 255) {
            this.qrcodeStructureappendM = m;
            this.qrcodeStructureappendN = n;
            this.qrcodeStructureappendParity = p;
        }

    }

    public int calStructureappendParity(byte[] originaldata) {
        int i = 0;
        int originaldataLength = originaldata.length;
        int structureappendParity;
        if (originaldataLength > 1) {
            for (structureappendParity = 0; i < originaldataLength; ++i) {
                structureappendParity ^= originaldata[i] & 255;
            }
        } else {
            structureappendParity = -1;
        }

        return structureappendParity;
    }

    public boolean[][] calQrcode(byte[] qrcodeData) {
        int dataCounter = 0;
        int dataLength = qrcodeData.length;
        int[] dataValue = new int[dataLength + 32];
        byte[] dataBits = new byte[dataLength + 32];
        if (dataLength <= 0) {
            boolean[][] ret = new boolean[][]{new boolean[1]};
            return ret;
        } else {
            if (this.qrcodeStructureappendN > 1) {
                dataValue[0] = 3;
                dataBits[0] = 4;
                dataValue[1] = this.qrcodeStructureappendM - 1;
                dataBits[1] = 4;
                dataValue[2] = this.qrcodeStructureappendN - 1;
                dataBits[2] = 4;
                dataValue[3] = this.qrcodeStructureappendParity;
                dataBits[3] = 8;
                dataCounter = 4;
            }

            dataBits[dataCounter] = 4;
            int[] codewordNumPlus;
            int codewordNumCounterValue;
            int totalDataBits;
            switch (this.qrcodeEncodeMode) {
                case 'A':
                    codewordNumPlus = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4};
                    dataValue[dataCounter] = 2;
                    dataCounter = dataCounter + 1;
                    dataValue[dataCounter] = dataLength;
                    dataBits[dataCounter] = 9;
                    codewordNumCounterValue = dataCounter++;

                    for (totalDataBits = 0; totalDataBits < dataLength; ++totalDataBits) {
                        char chr = (char) qrcodeData[totalDataBits];
                        byte chrValue = 0;
                        if (chr >= '0' && chr < ':') {
                            chrValue = (byte) (chr - 48);
                        } else if (chr >= 'A' && chr < '[') {
                            chrValue = (byte) (chr - 55);
                        } else {
                            if (chr == ' ') {
                                chrValue = 36;
                            }

                            if (chr == '$') {
                                chrValue = 37;
                            }

                            if (chr == '%') {
                                chrValue = 38;
                            }

                            if (chr == '*') {
                                chrValue = 39;
                            }

                            if (chr == '+') {
                                chrValue = 40;
                            }

                            if (chr == '-') {
                                chrValue = 41;
                            }

                            if (chr == '.') {
                                chrValue = 42;
                            }

                            if (chr == '/') {
                                chrValue = 43;
                            }

                            if (chr == ':') {
                                chrValue = 44;
                            }
                        }

                        if (totalDataBits % 2 == 0) {
                            dataValue[dataCounter] = chrValue;
                            dataBits[dataCounter] = 6;
                        } else {
                            dataValue[dataCounter] = dataValue[dataCounter] * 45 + chrValue;
                            dataBits[dataCounter] = 11;
                            if (totalDataBits < dataLength - 1) {
                                ++dataCounter;
                            }
                        }
                    }

                    ++dataCounter;
                    break;
                case 'N':
                    codewordNumPlus = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4};
                    dataValue[dataCounter] = 1;
                    dataCounter = dataCounter + 1;
                    dataValue[dataCounter] = dataLength;
                    dataBits[dataCounter] = 10;
                    codewordNumCounterValue = dataCounter++;

                    for (totalDataBits = 0; totalDataBits < dataLength; ++totalDataBits) {
                        if (totalDataBits % 3 == 0) {
                            dataValue[dataCounter] = qrcodeData[totalDataBits] - 48;
                            dataBits[dataCounter] = 4;
                        } else {
                            dataValue[dataCounter] = dataValue[dataCounter] * 10 + (qrcodeData[totalDataBits] - 48);
                            if (totalDataBits % 3 == 1) {
                                dataBits[dataCounter] = 7;
                            } else {
                                dataBits[dataCounter] = 10;
                                if (totalDataBits < dataLength - 1) {
                                    ++dataCounter;
                                }
                            }
                        }
                    }

                    ++dataCounter;
                    break;
                default:
                    codewordNumPlus = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8};
                    dataValue[dataCounter] = 4;
                    dataCounter = dataCounter + 1;
                    dataValue[dataCounter] = dataLength;
                    dataBits[dataCounter] = 8;
                    codewordNumCounterValue = dataCounter++;

                    for (totalDataBits = 0; totalDataBits < dataLength; ++totalDataBits) {
                        dataValue[totalDataBits + dataCounter] = qrcodeData[totalDataBits] & 255;
                        dataBits[totalDataBits + dataCounter] = 8;
                    }

                    dataCounter += dataLength;
            }

            totalDataBits = 0;

            for (int i = 0; i < dataCounter; ++i) {
                totalDataBits += dataBits[i];
            }

            byte ec;
            switch (this.qrcodeErrorCorrect) {
                case 'H':
                    ec = 2;
                    break;
                case 'L':
                    ec = 1;
                    break;
                case 'Q':
                    ec = 3;
                    break;
                default:
                    ec = 0;
            }

            int[][] maxDataBitsArray = new int[][]{{0, 128, 224, 352, 512, 688, 864, 992, 1232, 1456, 1728, 2032, 2320, 2672, 2920, 3320, 3624, 4056, 4504, 5016, 5352, 5712, 6256, 6880, 7312, 8000, 8496, 9024, 9544, 10136, 10984, 11640, 12328, 13048, 13800, 14496, 15312, 15936, 16816, 17728, 18672}, {0, 152, 272, 440, 640, 864, 1088, 1248, 1552, 1856, 2192, 2592, 2960, 3424, 3688, 4184, 4712, 5176, 5768, 6360, 6888, 7456, 8048, 8752, 9392, 10208, 10960, 11744, 12248, 13048, 13880, 14744, 15640, 16568, 17528, 18448, 19472, 20528, 21616, 22496, 23648}, {0, 72, 128, 208, 288, 368, 480, 528, 688, 800, 976, 1120, 1264, 1440, 1576, 1784, 2024, 2264, 2504, 2728, 3080, 3248, 3536, 3712, 4112, 4304, 4768, 5024, 5288, 5608, 5960, 6344, 6760, 7208, 7688, 7888, 8432, 8768, 9136, 9776, 10208}, {0, 104, 176, 272, 384, 496, 608, 704, 880, 1056, 1232, 1440, 1648, 1952, 2088, 2360, 2600, 2936, 3176, 3560, 3880, 4096, 4544, 4912, 5312, 5744, 6032, 6464, 6968, 7288, 7880, 8264, 8920, 9368, 9848, 10288, 10832, 11408, 12016, 12656, 13328}};
            int maxDataBits = 0;
            if (this.qrcodeVersion == 0) {
                this.qrcodeVersion = 1;

                for (int i = 1; i <= 40; ++i) {
                    if (maxDataBitsArray[ec][i] >= totalDataBits + codewordNumPlus[this.qrcodeVersion]) {
                        maxDataBits = maxDataBitsArray[ec][i];
                        break;
                    }

                    ++this.qrcodeVersion;
                }
            } else {
                maxDataBits = maxDataBitsArray[ec][this.qrcodeVersion];
            }

            totalDataBits += codewordNumPlus[this.qrcodeVersion];
            dataBits[codewordNumCounterValue] = (byte) (dataBits[codewordNumCounterValue] + codewordNumPlus[this.qrcodeVersion]);
            int[] maxCodewordsArray = new int[]{0, 26, 44, 70, 100, 134, 172, 196, 242, 292, 346, 404, 466, 532, 581, 655, 733, 815, 901, 991, 1085, 1156, 1258, 1364, 1474, 1588, 1706, 1828, 1921, 2051, 2185, 2323, 2465, 2611, 2761, 2876, 3034, 3196, 3362, 3532, 3706};
            int maxCodewords = maxCodewordsArray[this.qrcodeVersion];
            int maxModules1side = 17 + (this.qrcodeVersion << 2);
            int[] matrixRemainBit = new int[]{0, 0, 7, 7, 7, 7, 7, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0};
            int byte_num = matrixRemainBit[this.qrcodeVersion] + (maxCodewords << 3);
            byte[] matrixX = new byte[byte_num];
            byte[] matrixY = new byte[byte_num];
            byte[] maskArray = new byte[byte_num];
            byte[] formatInformationX2 = new byte[15];
            byte[] formatInformationY2 = new byte[15];
            byte[] rsEccCodewords = new byte[1];
            byte[] rsBlockOrderTemp = new byte[128];

            try {
                String filename = QRCODE_DATA_PATH+"/qrv" + Integer.toString(this.qrcodeVersion) + "_" + Integer.toString(ec) + ".dat";
                InputStream fis = Qrcode.class.getResourceAsStream(filename);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(matrixX);
                bis.read(matrixY);
                bis.read(maskArray);
                bis.read(formatInformationX2);
                bis.read(formatInformationY2);
                bis.read(rsEccCodewords);
                bis.read(rsBlockOrderTemp);
                bis.close();
                fis.close();
            } catch (Exception var44) {
                var44.printStackTrace();
            }

            byte rsBlockOrderLength = 1;

            for (byte i = 1; i < 128; ++i) {
                if (rsBlockOrderTemp[i] == 0) {
                    rsBlockOrderLength = i;
                    break;
                }
            }

            byte[] rsBlockOrder = new byte[rsBlockOrderLength];
            System.arraycopy(rsBlockOrderTemp, 0, rsBlockOrder, 0, rsBlockOrderLength);
            byte[] formatInformationX1 = new byte[]{0, 1, 2, 3, 4, 5, 7, 8, 8, 8, 8, 8, 8, 8, 8};
            byte[] formatInformationY1 = new byte[]{8, 8, 8, 8, 8, 8, 8, 8, 7, 5, 4, 3, 2, 1, 0};
            int maxDataCodewords = maxDataBits >> 3;
            int modules1Side = 4 * this.qrcodeVersion + 17;
            int matrixTotalBits = modules1Side * modules1Side;
            byte[] frameData = new byte[matrixTotalBits + modules1Side];

            try {
                String filename = QRCODE_DATA_PATH+"/qrvfr" + Integer.toString(this.qrcodeVersion) + ".dat";
                InputStream fis = Qrcode.class.getResourceAsStream(filename);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(frameData);
                bis.close();
                fis.close();
            } catch (Exception var43) {
                var43.printStackTrace();
            }

            if (totalDataBits <= maxDataBits - 4) {
                dataValue[dataCounter] = 0;
                dataBits[dataCounter] = 4;
            } else if (totalDataBits < maxDataBits) {
                dataValue[dataCounter] = 0;
                dataBits[dataCounter] = (byte) (maxDataBits - totalDataBits);
            } else if (totalDataBits > maxDataBits) {
                System.out.println("overflow");
            }

            byte[] dataCodewords = divideDataBy8Bits(dataValue, dataBits, maxDataCodewords);
            byte[] codewords = calculateRSECC(dataCodewords, rsEccCodewords[0], rsBlockOrder, maxDataCodewords, maxCodewords);
            byte[][] matrixContent = new byte[modules1Side][modules1Side];

            int remainBitTemp;
            for (int i = 0; i < modules1Side; ++i) {
                for (remainBitTemp = 0; remainBitTemp < modules1Side; ++remainBitTemp) {
                    matrixContent[remainBitTemp][i] = 0;
                }
            }

            byte maskContent;
            for (int i = 0; i < maxCodewords; ++i) {
                maskContent = codewords[i];

                for (int j = 7; j >= 0; --j) {
                    int codewordBitsNumber = i * 8 + j;
                    matrixContent[matrixX[codewordBitsNumber] & 255][matrixY[codewordBitsNumber] & 255] = (byte) (255 * (maskContent & 1) ^ maskArray[codewordBitsNumber]);
                    maskContent = (byte) ((maskContent & 255) >>> 1);
                }
            }

            for (int i = matrixRemainBit[this.qrcodeVersion]; i > 0; --i) {
                remainBitTemp = i + maxCodewords * 8 - 1;
                matrixContent[matrixX[remainBitTemp] & 255][matrixY[remainBitTemp] & 255] = (byte) (255 ^ maskArray[remainBitTemp]);
            }

            byte maskNumber = selectMask(matrixContent, matrixRemainBit[this.qrcodeVersion] + maxCodewords * 8);
            maskContent = (byte) (1 << maskNumber);
            byte formatInformationValue = (byte) (ec << 3 | maskNumber);
            String[] formatInformationArray = new String[]{"101010000010010", "101000100100101", "101111001111100", "101101101001011", "100010111111001", "100000011001110", "100111110010111", "100101010100000", "111011111000100", "111001011110011", "111110110101010", "111100010011101", "110011000101111", "110001100011000", "110110001000001", "110100101110110", "001011010001001", "001001110111110", "001110011100111", "001100111010000", "000011101100010", "000001001010101", "000110100001100", "000100000111011", "011010101011111", "011000001101000", "011111100110001", "011101000000110", "010010010110100", "010000110000011", "010111011011010", "010101111101101"};

            for (int i = 0; i < 15; ++i) {
                byte content = Byte.parseByte(formatInformationArray[formatInformationValue].substring(i, i + 1));
                matrixContent[formatInformationX1[i] & 255][formatInformationY1[i] & 255] = (byte) (content * 255);
                matrixContent[formatInformationX2[i] & 255][formatInformationY2[i] & 255] = (byte) (content * 255);
            }

            boolean[][] out = new boolean[modules1Side][modules1Side];
            int c = 0;

            for (int i = 0; i < modules1Side; ++i) {
                for (int j = 0; j < modules1Side; ++j) {
                    if ((matrixContent[j][i] & maskContent) == 0 && frameData[c] != 49) {
                        out[j][i] = false;
                    } else {
                        out[j][i] = true;
                    }

                    ++c;
                }

                ++c;
            }

            return out;
        }
    }

    private static byte[] divideDataBy8Bits(int[] data, byte[] bits, int maxDataCodewords) {
        int l1 = bits.length;
        int codewordsCounter = 0;
        int remainingBits = 8;
        int max = 0;
        int var10000 = data.length;

        for (int i = 0; i < l1; ++i) {
            max += bits[i];
        }

        int l2 = (max - 1) / 8 + 1;
        byte[] codewords = new byte[maxDataCodewords];

        int i;
        for (i = 0; i < l2; ++i) {
            codewords[i] = 0;
        }

        boolean flag;
        for (i = 0; i < l1; ++i) {
            int buffer = data[i];
            int bufferBits = bits[i];
            flag = true;
            if (bufferBits == 0) {
                break;
            }

            while (flag) {
                if (remainingBits > bufferBits) {
                    codewords[codewordsCounter] = (byte) (codewords[codewordsCounter] << bufferBits | buffer);
                    remainingBits -= bufferBits;
                    flag = false;
                } else {
                    bufferBits -= remainingBits;
                    codewords[codewordsCounter] = (byte) (codewords[codewordsCounter] << remainingBits | buffer >> bufferBits);
                    if (bufferBits == 0) {
                        flag = false;
                    } else {
                        buffer &= (1 << bufferBits) - 1;
                        flag = true;
                    }

                    ++codewordsCounter;
                    remainingBits = 8;
                }
            }
        }

        if (remainingBits != 8) {
            codewords[codewordsCounter] = (byte) (codewords[codewordsCounter] << remainingBits);
        } else {
            --codewordsCounter;
        }

        if (codewordsCounter < maxDataCodewords - 1) {
            for (flag = true; codewordsCounter < maxDataCodewords - 1; flag = !flag) {
                ++codewordsCounter;
                if (flag) {
                    codewords[codewordsCounter] = -20;
                } else {
                    codewords[codewordsCounter] = 17;
                }
            }
        }

        return codewords;
    }

    private static byte[] calculateRSECC(byte[] codewords, byte rsEccCodewords, byte[] rsBlockOrder, int maxDataCodewords, int maxCodewords) {
        byte[][] rsCalTableArray = new byte[256][rsEccCodewords];

        try {
            String filename = QRCODE_DATA_PATH+"/rsc" + Byte.toString(rsEccCodewords) + ".dat";
            InputStream fis = Qrcode.class.getResourceAsStream(filename);
            BufferedInputStream bis = new BufferedInputStream(fis);

            for (int i = 0; i < 256; ++i) {
                bis.read(rsCalTableArray[i]);
            }

            bis.close();
            fis.close();
        } catch (Exception var17) {
            var17.printStackTrace();
        }

        int j = 0;
        int rsBlockNumber = 0;
        byte[][] rsTemp = new byte[rsBlockOrder.length][];
        byte[] res = new byte[maxCodewords];
        System.arraycopy(codewords, 0, res, 0, codewords.length);

        int i;
        for (i = 0; i < rsBlockOrder.length; ++i) {
            rsTemp[i] = new byte[(rsBlockOrder[i] & 255) - rsEccCodewords];
        }

        for (i = 0; i < maxDataCodewords; ++i) {
            rsTemp[rsBlockNumber][j] = codewords[i];
            ++j;
            if (j >= (rsBlockOrder[rsBlockNumber] & 255) - rsEccCodewords) {
                j = 0;
                ++rsBlockNumber;
            }
        }

        for (rsBlockNumber = 0; rsBlockNumber < rsBlockOrder.length; ++rsBlockNumber) {
            byte[] rsTempData = (byte[]) rsTemp[rsBlockNumber].clone();
            int rsCodewords = rsBlockOrder[rsBlockNumber] & 255;
            int rsDataCodewords = rsCodewords - rsEccCodewords;

            for (j = rsDataCodewords; j > 0; --j) {
                byte first = rsTempData[0];
                byte[] rsTempNew;
                if (first != 0) {
                    rsTempNew = new byte[rsTempData.length - 1];
                    System.arraycopy(rsTempData, 1, rsTempNew, 0, rsTempData.length - 1);
                    byte[] cal = rsCalTableArray[first & 255];
                    rsTempData = calculateByteArrayBits(rsTempNew, cal, "xor");
                } else if (rsEccCodewords < rsTempData.length) {
                    rsTempNew = new byte[rsTempData.length - 1];
                    System.arraycopy(rsTempData, 1, rsTempNew, 0, rsTempData.length - 1);
                    rsTempData = (byte[]) rsTempNew.clone();
                } else {
                    rsTempNew = new byte[rsEccCodewords];
                    System.arraycopy(rsTempData, 1, rsTempNew, 0, rsTempData.length - 1);
                    rsTempNew[rsEccCodewords - 1] = 0;
                    rsTempData = (byte[]) rsTempNew.clone();
                }
            }

            System.arraycopy(rsTempData, 0, res, codewords.length + rsBlockNumber * rsEccCodewords, rsEccCodewords);
        }

        return res;
    }

    private static byte[] calculateByteArrayBits(byte[] xa, byte[] xb, String ind) {
        byte[] xl;
        byte[] xs;
        if (xa.length > xb.length) {
            xl = (byte[]) xa.clone();
            xs = (byte[]) xb.clone();
        } else {
            xl = (byte[]) xb.clone();
            xs = (byte[]) xa.clone();
        }

        int ll = xl.length;
        int ls = xs.length;
        byte[] res = new byte[ll];

        for (int i = 0; i < ll; ++i) {
            if (i < ls) {
                if (ind == "xor") {
                    res[i] = (byte) (xl[i] ^ xs[i]);
                } else {
                    res[i] = (byte) (xl[i] | xs[i]);
                }
            } else {
                res[i] = xl[i];
            }
        }

        return res;
    }

    private static byte selectMask(byte[][] matrixContent, int maxCodewordsBitWithRemain) {
        int l = matrixContent.length;
        int[] d1 = new int[8];
        int[] d2 = new int[8];
        int[] d3 = new int[8];
        int[] d4 = new int[8];
        int d2And = 0;
        int d2Or = 0;
        int[] d4Counter = new int[8];

        int y;
        int[] yData;
        for (y = 0; y < l; ++y) {
            int[] xData = new int[8];
            yData = new int[8];
            boolean[] xD1Flag = new boolean[8];
            boolean[] yD1Flag = new boolean[8];

            for (int x = 0; x < l; ++x) {
                if (x > 0 && y > 0) {
                    d2And = matrixContent[x][y] & matrixContent[x - 1][y] & matrixContent[x][y - 1] & matrixContent[x - 1][y - 1] & 255;
                    d2Or = matrixContent[x][y] & 255 | matrixContent[x - 1][y] & 255 | matrixContent[x][y - 1] & 255 | matrixContent[x - 1][y - 1] & 255;
                }

                for (int maskNumber = 0; maskNumber < 8; ++maskNumber) {
                    xData[maskNumber] = (xData[maskNumber] & 63) << 1 | (matrixContent[x][y] & 255) >>> maskNumber & 1;
                    yData[maskNumber] = (yData[maskNumber] & 63) << 1 | (matrixContent[y][x] & 255) >>> maskNumber & 1;
                    int var10002;
                    if ((matrixContent[x][y] & 1 << maskNumber) != 0) {
                        var10002 = d4Counter[maskNumber]++;
                    }

                    if (xData[maskNumber] == 93) {
                        d3[maskNumber] += 40;
                    }

                    if (yData[maskNumber] == 93) {
                        d3[maskNumber] += 40;
                    }

                    if (x > 0 && y > 0) {
                        if ((d2And & 1) != 0 || (d2Or & 1) == 0) {
                            d2[maskNumber] += 3;
                        }

                        d2And >>= 1;
                        d2Or >>= 1;
                    }

                    if ((xData[maskNumber] & 31) != 0 && (xData[maskNumber] & 31) != 31) {
                        xD1Flag[maskNumber] = false;
                    } else if (x > 3) {
                        if (xD1Flag[maskNumber]) {
                            var10002 = d1[maskNumber]++;
                        } else {
                            d1[maskNumber] += 3;
                            xD1Flag[maskNumber] = true;
                        }
                    }

                    if ((yData[maskNumber] & 31) != 0 && (yData[maskNumber] & 31) != 31) {
                        yD1Flag[maskNumber] = false;
                    } else if (x > 3) {
                        if (yD1Flag[maskNumber]) {
                            var10002 = d1[maskNumber]++;
                        } else {
                            d1[maskNumber] += 3;
                            yD1Flag[maskNumber] = true;
                        }
                    }
                }
            }
        }

        y = 0;
        byte res = 0;
        yData = new int[]{90, 80, 70, 60, 50, 40, 30, 20, 10, 0, 0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 90};

        for (int maskNumber = 0; maskNumber < 8; ++maskNumber) {
            d4[maskNumber] = yData[20 * d4Counter[maskNumber] / maxCodewordsBitWithRemain];
            int demerit = d1[maskNumber] + d2[maskNumber] + d3[maskNumber] + d4[maskNumber];
            if (demerit < y || maskNumber == 0) {
                res = (byte) maskNumber;
                y = demerit;
            }
        }

        return res;
    }
}

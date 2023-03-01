package com.ck.qr_code.swetake.jp.sourceforge.qrcode.reader;

import com.ck.qr_code.swetake.jp.sourceforge.qrcode.QRCodeDecoder;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.exception.InvalidDataBlockException;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.util.DebugCanvas;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


public class QRCodeDataBlockReader {
    int[] blocks;
    int dataLengthMode;
    int blockPointer = 0;
    int bitPointer = 7;
    int dataLength = 0;
    int numErrorCorrectionCode;
    DebugCanvas canvas;
    static final int MODE_NUMBER = 1;
    static final int MODE_ROMAN_AND_NUMBER = 2;
    static final int MODE_8BIT_BYTE = 4;
    static final int MODE_KANJI = 8;
    final int[][] sizeOfDataLengthInfo = new int[][]{{10, 9, 8, 8}, {12, 11, 16, 10}, {14, 13, 16, 12}};

    public QRCodeDataBlockReader(int[] blocks, int version, int numErrorCorrectionCode) {
        this.blocks = blocks;
        this.numErrorCorrectionCode = numErrorCorrectionCode;
        if (version <= 9) {
            this.dataLengthMode = 0;
        } else if (version >= 10 && version <= 26) {
            this.dataLengthMode = 1;
        } else if (version >= 27 && version <= 40) {
            this.dataLengthMode = 2;
        }

        this.canvas = QRCodeDecoder.getCanvas();
    }

    int getNextBits(int numBits) throws ArrayIndexOutOfBoundsException {
        int mask1;
        int mask3;
        int bits;
        if (numBits < this.bitPointer + 1) {
            mask1 = 0;

            for(mask3 = 0; mask3 < numBits; ++mask3) {
                mask1 += 1 << mask3;
            }

            mask1 <<= this.bitPointer - numBits + 1;
            bits = (this.blocks[this.blockPointer] & mask1) >> this.bitPointer - numBits + 1;
            this.bitPointer -= numBits;
            return bits;
        } else if (numBits < this.bitPointer + 1 + 8) {
            mask1 = 0;

            for(mask3 = 0; mask3 < this.bitPointer + 1; ++mask3) {
                mask1 += 1 << mask3;
            }

            bits = (this.blocks[this.blockPointer] & mask1) << numBits - (this.bitPointer + 1);
            ++this.blockPointer;
            bits += this.blocks[this.blockPointer] >> 8 - (numBits - (this.bitPointer + 1));
            this.bitPointer -= numBits % 8;
            if (this.bitPointer < 0) {
                this.bitPointer += 8;
            }

            return bits;
        } else if (numBits >= this.bitPointer + 1 + 16) {
            System.out.println("ERROR!");
            return 0;
        } else {
            mask1 = 0;
            mask3 = 0;

            int bitsFirstBlock;
            for(bitsFirstBlock = 0; bitsFirstBlock < this.bitPointer + 1; ++bitsFirstBlock) {
                mask1 += 1 << bitsFirstBlock;
            }

            bitsFirstBlock = (this.blocks[this.blockPointer] & mask1) << numBits - (this.bitPointer + 1);
            ++this.blockPointer;
            int bitsSecondBlock = this.blocks[this.blockPointer] << numBits - (this.bitPointer + 1 + 8);
            ++this.blockPointer;

            int bitsThirdBlock;
            for(bitsThirdBlock = 0; bitsThirdBlock < numBits - (this.bitPointer + 1 + 8); ++bitsThirdBlock) {
                mask3 += 1 << bitsThirdBlock;
            }

            mask3 <<= 8 - (numBits - (this.bitPointer + 1 + 8));
            bitsThirdBlock = (this.blocks[this.blockPointer] & mask3) >> 8 - (numBits - (this.bitPointer + 1 + 8));
            bits = bitsFirstBlock + bitsSecondBlock + bitsThirdBlock;
            this.bitPointer -= (numBits - 8) % 8;
            if (this.bitPointer < 0) {
                this.bitPointer += 8;
            }

            return bits;
        }
    }

    int getNextMode() throws ArrayIndexOutOfBoundsException {
        return this.blockPointer > this.blocks.length - this.numErrorCorrectionCode - 2 ? 0 : this.getNextBits(4);
    }

    int guessMode(int mode) {
        switch(mode) {
        case 3:
            return 1;
        case 4:
        case 8:
        default:
            return 8;
        case 5:
            return 4;
        case 6:
            return 4;
        case 7:
            return 4;
        case 9:
            return 8;
        case 10:
            return 8;
        case 11:
            return 8;
        case 12:
            return 4;
        case 13:
            return 4;
        case 14:
            return 4;
        case 15:
            return 4;
        }
    }

    int getDataLength(int modeIndicator) throws ArrayIndexOutOfBoundsException {
        int index;
        for(index = 0; modeIndicator >> index != 1; ++index) {
        }

        return this.getNextBits(this.sizeOfDataLengthInfo[this.dataLengthMode][index]);
    }

    public byte[] getDataByte() throws InvalidDataBlockException {
        this.canvas.println("Reading data blocks.");
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            while(true) {
                int mode = this.getNextMode();
                if (mode == 0) {
                    if (output.size() <= 0) {
                        throw new InvalidDataBlockException("Empty data block");
                    }

                    return output.toByteArray();
                }

                if (mode != 1 && mode != 2 && mode != 4 && mode != 8) {
                    throw new InvalidDataBlockException("Invalid mode: " + mode + " in (block:" + this.blockPointer + " bit:" + this.bitPointer + ")");
                }

                this.dataLength = this.getDataLength(mode);
                if (this.dataLength < 1) {
                    throw new InvalidDataBlockException("Invalid data length: " + this.dataLength);
                }

                switch(mode) {
                case 1:
                    output.write(this.getFigureString(this.dataLength).getBytes());
                    break;
                case 2:
                    output.write(this.getRomanAndFigureString(this.dataLength).getBytes());
                case 3:
                case 5:
                case 6:
                case 7:
                default:
                    break;
                case 4:
                    output.write(this.get8bitByteArray(this.dataLength));
                    break;
                case 8:
                    output.write(this.getKanjiString(this.dataLength).getBytes());
                }
            }
        } catch (ArrayIndexOutOfBoundsException var3) {
            throw new InvalidDataBlockException("Data Block Error in (block:" + this.blockPointer + " bit:" + this.bitPointer + ")");
        } catch (IOException var4) {
            throw new InvalidDataBlockException(var4.getMessage());
        }
    }

    public String getDataString() throws ArrayIndexOutOfBoundsException {
        this.canvas.println("Reading data blocks...");
        String dataString = "";

        while(true) {
            int mode = this.getNextMode();
            this.canvas.println("mode: " + mode);
            if (mode == 0) {
                System.out.println("");
                return dataString;
            }

            if (mode != 1 && mode != 2 && mode != 4) {
            }

            this.dataLength = this.getDataLength(mode);
            this.canvas.println(Integer.toString(this.blocks[this.blockPointer]));
            System.out.println("length: " + this.dataLength);
            switch(mode) {
            case 1:
                dataString = dataString + this.getFigureString(this.dataLength);
                break;
            case 2:
                dataString = dataString + this.getRomanAndFigureString(this.dataLength);
            case 3:
            case 5:
            case 6:
            case 7:
            default:
                break;
            case 4:
                dataString = dataString + this.get8bitByteString(this.dataLength);
                break;
            case 8:
                dataString = dataString + this.getKanjiString(this.dataLength);
            }
        }
    }

    String getFigureString(int dataLength) throws ArrayIndexOutOfBoundsException {
        int length = dataLength;
        int intData = 0;
        String strData = "";

        do {
            if (length >= 3) {
                intData = this.getNextBits(10);
                if (intData < 100) {
                    strData = strData + "0";
                }

                if (intData < 10) {
                    strData = strData + "0";
                }

                length -= 3;
            } else if (length == 2) {
                intData = this.getNextBits(7);
                if (intData < 10) {
                    strData = strData + "0";
                }

                length -= 2;
            } else if (length == 1) {
                intData = this.getNextBits(4);
                --length;
            }

            strData = strData + Integer.toString(intData);
        } while(length > 0);

        return strData;
    }

    String getRomanAndFigureString(int dataLength) throws ArrayIndexOutOfBoundsException {
        int length = dataLength;
        String strData = "";
        char[] tableRomanAndFigure = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', ' ', '$', '%', '*', '+', '-', '.', '/', ':'};

        do {
            int intData;
            if (length > 1) {
                intData = this.getNextBits(11);
                int firstLetter = intData / 45;
                int secondLetter = intData % 45;
                strData = strData + String.valueOf(tableRomanAndFigure[firstLetter]);
                strData = strData + String.valueOf(tableRomanAndFigure[secondLetter]);
                length -= 2;
            } else if (length == 1) {
                intData = this.getNextBits(6);
                strData = strData + String.valueOf(tableRomanAndFigure[intData]);
                --length;
            }
        } while(length > 0);

        return strData;
    }

    public byte[] get8bitByteArray(int dataLength) throws ArrayIndexOutOfBoundsException {
        int length = dataLength;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        do {
            int intData = this.getNextBits(8);
            output.write((byte)intData);
            --length;
        } while(length > 0);

        return output.toByteArray();
    }

    String get8bitByteString(int dataLength) throws ArrayIndexOutOfBoundsException {
        int length = dataLength;
        String strData = "";

        do {
            int intData = this.getNextBits(8);
            strData = strData + (char)intData;
            --length;
        } while(length > 0);

        return strData;
    }

    String getKanjiString(int dataLength) throws ArrayIndexOutOfBoundsException {
        int length = dataLength;
        String unicodeString = "";

        do {
            int intData = this.getNextBits(13);
            int lowerByte = intData % 192;
            int higherByte = intData / 192;
            int tempWord = (higherByte << 8) + lowerByte;
            int shiftjisWord;
            if (tempWord + '腀' <= 40956) {
                shiftjisWord = tempWord + '腀';
            } else {
                shiftjisWord = tempWord + '셀';
            }

            byte[] tempByte = new byte[]{(byte)(shiftjisWord >> 8), (byte)(shiftjisWord & 255)};

            try {
                unicodeString = unicodeString + new String(tempByte, "Shift_JIS");
            } catch (UnsupportedEncodingException var11) {
                var11.printStackTrace();
            }

            --length;
        } while(length > 0);

        return unicodeString;
    }
}

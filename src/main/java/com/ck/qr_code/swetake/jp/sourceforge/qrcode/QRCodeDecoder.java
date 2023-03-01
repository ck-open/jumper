package com.ck.qr_code.swetake.jp.sourceforge.qrcode;

import com.ck.qr_code.swetake.jp.sourceforge.qrcode.data.QRCodeImage;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.data.QRCodeSymbol;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.exception.DecodingFailedException;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.exception.InvalidDataBlockException;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.exception.SymbolNotFoundException;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.geom.Point;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.reader.QRCodeDataBlockReader;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.reader.QRCodeImageReader;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.util.DebugCanvas;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.util.DebugCanvasAdapter;
import com.ck.qr_code.swetake.jp.sourceforge.reedsolomon.RsDecode;


import java.util.Vector;


public class QRCodeDecoder {
    int numTryDecode = 0;
    QRCodeSymbol qrCodeSymbol;
    Vector results = new Vector();
    Vector lastResults = new Vector();
    static DebugCanvas canvas;
    QRCodeImageReader imageReader;
    int numLastCorrectionFailures;

    public static void setCanvas(DebugCanvas canvas) {
        QRCodeDecoder.canvas = canvas;
    }

    public static DebugCanvas getCanvas() {
        return canvas;
    }

    public QRCodeDecoder() {
        canvas = new DebugCanvasAdapter();
    }

    public byte[] decode(QRCodeImage qrCodeImage) throws DecodingFailedException {
        Point[] adjusts = this.getAdjustPoints();
        Vector results = new Vector();
        this.numTryDecode = 0;

        while(this.numTryDecode < adjusts.length) {
            try {
                QRCodeDecoder.DecodeResult result = this.decode(qrCodeImage, adjusts[this.numTryDecode]);
                if (result.isCorrectionSucceeded()) {
                    byte[] var6 = result.getDecodedBytes();
                    return var6;
                }

                results.addElement(result);
                canvas.println("Decoding succeeded but could not correct");
                canvas.println("all errors. Retrying..");
            } catch (DecodingFailedException var10) {
                if (var10.getMessage().indexOf("Finder Pattern") >= 0) {
                    throw var10;
                }
            } finally {
                ++this.numTryDecode;
            }
        }

        if (results.size() == 0) {
            throw new DecodingFailedException("Give up decoding");
        } else {
            int minErrorIndex = -1;
            int minError = 2147483647;

            for(int i = 0; i < results.size(); ++i) {
                QRCodeDecoder.DecodeResult result = (QRCodeDecoder.DecodeResult)results.elementAt(i);
                if (result.getNumCorrectuionFailures() < minError) {
                    minError = result.getNumCorrectuionFailures();
                    minErrorIndex = i;
                }
            }

            canvas.println("All trials need for correct error");
            canvas.println("Reporting #" + minErrorIndex + " that,");
            canvas.println("corrected minimum errors (" + minError + ")");
            canvas.println("Decoding finished.");
            return ((QRCodeDecoder.DecodeResult)results.elementAt(minErrorIndex)).getDecodedBytes();
        }
    }

    Point[] getAdjustPoints() {
        Vector adjustPoints = new Vector();

        int lastX;
        for(lastX = 0; lastX < 4; ++lastX) {
            adjustPoints.addElement(new Point(1, 1));
        }

        lastX = 0;
        int lastY = 0;

        int x;
        for(int y = 0; y > -4; --y) {
            for(x = 0; x > -4; --x) {
                if (x != y && (x + y) % 2 == 0) {
                    adjustPoints.addElement(new Point(x - lastX, y - lastY));
                    lastX = x;
                    lastY = y;
                }
            }
        }

        Point[] adjusts = new Point[adjustPoints.size()];

        for(x = 0; x < adjusts.length; ++x) {
            adjusts[x] = (Point)adjustPoints.elementAt(x);
        }

        return adjusts;
    }

    QRCodeDecoder.DecodeResult decode(QRCodeImage qrCodeImage, Point adjust) throws DecodingFailedException {
        try {
            if (this.numTryDecode == 0) {
                canvas.println("Decoding started");
                int[][] intImage = this.imageToIntArray(qrCodeImage);
                this.imageReader = new QRCodeImageReader();
                this.qrCodeSymbol = this.imageReader.getQRCodeSymbol(intImage);
            } else {
                canvas.println("--");
                canvas.println("Decoding restarted #" + this.numTryDecode);
                this.qrCodeSymbol = this.imageReader.getQRCodeSymbolWithAdjustedGrid(adjust);
            }
        } catch (SymbolNotFoundException var6) {
            throw new DecodingFailedException(var6.getMessage());
        }

        canvas.println("Created QRCode symbol.");
        canvas.println("Reading symbol.");
        canvas.println("Version: " + this.qrCodeSymbol.getVersionReference());
        canvas.println("Mask pattern: " + this.qrCodeSymbol.getMaskPatternRefererAsString());
        int[] blocks = this.qrCodeSymbol.getBlocks();
        canvas.println("Correcting data errors.");
        blocks = this.correctDataBlocks(blocks);

        try {
            byte[] decodedByteArray = this.getDecodedByteArray(blocks, this.qrCodeSymbol.getVersion(), this.qrCodeSymbol.getNumErrorCollectionCode());
            return new QRCodeDecoder.DecodeResult(decodedByteArray, this.numLastCorrectionFailures);
        } catch (InvalidDataBlockException var5) {
            canvas.println(var5.getMessage());
            throw new DecodingFailedException(var5.getMessage());
        }
    }

    int[][] imageToIntArray(QRCodeImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] intImage = new int[width][height];

        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                intImage[x][y] = image.getPixel(x, y);
            }
        }

        return intImage;
    }

    int[] correctDataBlocks(int[] blocks) {
        int numSucceededCorrections = 0;
        int numCorrectionFailures = 0;
        int dataCapacity = this.qrCodeSymbol.getDataCapacity();
        int[] dataBlocks = new int[dataCapacity];
        int numErrorCollectionCode = this.qrCodeSymbol.getNumErrorCollectionCode();
        int numRSBlocks = this.qrCodeSymbol.getNumRSBlocks();
        int eccPerRSBlock = numErrorCollectionCode / numRSBlocks;
        int lengthShorterRSBlock;
        if (numRSBlocks == 1) {
            RsDecode corrector = new RsDecode(eccPerRSBlock / 2);
            lengthShorterRSBlock = corrector.decode(blocks);
            if (lengthShorterRSBlock > 0) {
                int var10000 = numSucceededCorrections + lengthShorterRSBlock;
            } else if (lengthShorterRSBlock < 0) {
                ++numCorrectionFailures;
            }

            return blocks;
        } else {
            int numLongerRSBlocks = dataCapacity % numRSBlocks;
            int numShorterRSBlocks;
            if (numLongerRSBlocks == 0) {
                lengthShorterRSBlock = dataCapacity / numRSBlocks;
                int[][] RSBlocks = new int[numRSBlocks][lengthShorterRSBlock];

                int i;
                int j;
                for(numShorterRSBlocks = 0; numShorterRSBlocks < numRSBlocks; ++numShorterRSBlocks) {
                    for(i = 0; i < lengthShorterRSBlock; ++i) {
                        RSBlocks[numShorterRSBlocks][i] = blocks[i * numRSBlocks + numShorterRSBlocks];
                    }

                    canvas.println("eccPerRSBlock=" + eccPerRSBlock);
                    RsDecode corrector = new RsDecode(eccPerRSBlock / 2);
                    j = corrector.decode(RSBlocks[numShorterRSBlocks]);
                    if (j > 0) {
                        numSucceededCorrections += j;
                    } else if (j < 0) {
                        ++numCorrectionFailures;
                    }
                }

                numShorterRSBlocks = 0;

                for(i = 0; i < numRSBlocks; ++i) {
                    for(j = 0; j < lengthShorterRSBlock - eccPerRSBlock; ++j) {
                        dataBlocks[numShorterRSBlocks++] = RSBlocks[i][j];
                    }
                }
            } else {
                lengthShorterRSBlock = dataCapacity / numRSBlocks;
                int lengthLongerRSBlock = dataCapacity / numRSBlocks + 1;
                numShorterRSBlocks = numRSBlocks - numLongerRSBlocks;
                int[][] shorterRSBlocks = new int[numShorterRSBlocks][lengthShorterRSBlock];
                int[][] longerRSBlocks = new int[numLongerRSBlocks][lengthLongerRSBlock];

                int i;
                int j;
                for(i = 0; i < numRSBlocks; ++i) {
                    int ret;
                    RsDecode corrector;
                    if (i < numShorterRSBlocks) {
                        i = 0;

                        for(j = 0; j < lengthShorterRSBlock; ++j) {
                            if (j == lengthShorterRSBlock - eccPerRSBlock) {
                                i = numLongerRSBlocks;
                            }

                            shorterRSBlocks[i][j] = blocks[j * numRSBlocks + i + i];
                        }

                        canvas.println("eccPerRSBlock(shorter)=" + eccPerRSBlock);
                        corrector = new RsDecode(eccPerRSBlock / 2);
                        ret = corrector.decode(shorterRSBlocks[i]);
                        if (ret > 0) {
                            numSucceededCorrections += ret;
                        } else if (ret < 0) {
                            ++numCorrectionFailures;
                        }
                    } else {
                        i = 0;

                        for(j = 0; j < lengthLongerRSBlock; ++j) {
                            if (j == lengthShorterRSBlock - eccPerRSBlock) {
                                i = numShorterRSBlocks;
                            }

                            longerRSBlocks[i - numShorterRSBlocks][j] = blocks[j * numRSBlocks + i - i];
                        }

                        canvas.println("eccPerRSBlock(longer)=" + eccPerRSBlock);
                        corrector = new RsDecode(eccPerRSBlock / 2);
                        ret = corrector.decode(longerRSBlocks[i - numShorterRSBlocks]);
                        if (ret > 0) {
                            numSucceededCorrections += ret;
                        } else if (ret < 0) {
                            ++numCorrectionFailures;
                        }
                    }
                }

                i = 0;

                for(i = 0; i < numRSBlocks; ++i) {
                    if (i < numShorterRSBlocks) {
                        for(j = 0; j < lengthShorterRSBlock - eccPerRSBlock; ++j) {
                            dataBlocks[i++] = shorterRSBlocks[i][j];
                        }
                    } else {
                        for(j = 0; j < lengthLongerRSBlock - eccPerRSBlock; ++j) {
                            dataBlocks[i++] = longerRSBlocks[i - numShorterRSBlocks][j];
                        }
                    }
                }
            }

            if (numSucceededCorrections > 0) {
                canvas.println(String.valueOf(numSucceededCorrections) + " data errors corrected successfully.");
            } else {
                canvas.println("No errors found.");
            }

            this.numLastCorrectionFailures = numCorrectionFailures;
            return dataBlocks;
        }
    }

    byte[] getDecodedByteArray(int[] blocks, int version, int numErrorCorrectionCode) throws InvalidDataBlockException {
        QRCodeDataBlockReader reader = new QRCodeDataBlockReader(blocks, version, numErrorCorrectionCode);

        try {
            byte[] byteArray = reader.getDataByte();
            return byteArray;
        } catch (InvalidDataBlockException var7) {
            throw var7;
        }
    }

    class DecodeResult {
        int numCorrectionFailures;
        byte[] decodedBytes;

        public DecodeResult(byte[] decodedBytes, int numCorrectionFailures) {
            this.decodedBytes = decodedBytes;
            this.numCorrectionFailures = numCorrectionFailures;
        }

        public byte[] getDecodedBytes() {
            return this.decodedBytes;
        }

        public int getNumCorrectuionFailures() {
            return this.numCorrectionFailures;
        }

        public boolean isCorrectionSucceeded() {
            return QRCodeDecoder.this.numLastCorrectionFailures == 0;
        }
    }
}

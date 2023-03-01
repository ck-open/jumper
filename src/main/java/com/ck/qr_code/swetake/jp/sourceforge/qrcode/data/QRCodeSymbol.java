package com.ck.qr_code.swetake.jp.sourceforge.qrcode.data;

import com.ck.qr_code.swetake.jp.sourceforge.qrcode.ecc.BCH15_5;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.geom.Point;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.pattern.LogicalSeed;

import java.util.Vector;

public class QRCodeSymbol {
    int version;
    int errorCollectionLevel;
    int maskPattern;
    int dataCapacity;
    boolean[][] moduleMatrix;
    int width;
    int height;
    Point[][] alignmentPattern;
    final int[][] numErrorCollectionCode = new int[][]{{7, 10, 13, 17}, {10, 16, 22, 28}, {15, 26, 36, 44}, {20, 36, 52, 64}, {26, 48, 72, 88}, {36, 64, 96, 112}, {40, 72, 108, 130}, {48, 88, 132, 156}, {60, 110, 160, 192}, {72, 130, 192, 224}, {80, 150, 224, 264}, {96, 176, 260, 308}, {104, 198, 288, 352}, {120, 216, 320, 384}, {132, 240, 360, 432}, {144, 280, 408, 480}, {168, 308, 448, 532}, {180, 338, 504, 588}, {196, 364, 546, 650}, {224, 416, 600, 700}, {224, 442, 644, 750}, {252, 476, 690, 816}, {270, 504, 750, 900}, {300, 560, 810, 960}, {312, 588, 870, 1050}, {336, 644, 952, 1110}, {360, 700, 1020, 1200}, {390, 728, 1050, 1260}, {420, 784, 1140, 1350}, {450, 812, 1200, 1440}, {480, 868, 1290, 1530}, {510, 924, 1350, 1620}, {540, 980, 1440, 1710}, {570, 1036, 1530, 1800}, {570, 1064, 1590, 1890}, {600, 1120, 1680, 1980}, {630, 1204, 1770, 2100}, {660, 1260, 1860, 2220}, {720, 1316, 1950, 2310}, {750, 1372, 2040, 2430}};
    final int[][] numRSBlocks = new int[][]{{1, 1, 1, 1}, {1, 1, 1, 1}, {1, 1, 2, 2}, {1, 2, 2, 4}, {1, 2, 4, 4}, {2, 4, 4, 4}, {2, 4, 6, 5}, {2, 4, 6, 6}, {2, 5, 8, 8}, {4, 5, 8, 8}, {4, 5, 8, 11}, {4, 8, 10, 11}, {4, 9, 12, 16}, {4, 9, 16, 16}, {6, 10, 12, 18}, {6, 10, 17, 16}, {6, 11, 16, 19}, {6, 13, 18, 21}, {7, 14, 21, 25}, {8, 16, 20, 25}, {8, 17, 23, 25}, {9, 17, 23, 34}, {9, 18, 25, 30}, {10, 20, 27, 32}, {12, 21, 29, 35}, {12, 23, 34, 37}, {12, 25, 34, 40}, {13, 26, 35, 42}, {14, 28, 38, 45}, {15, 29, 40, 48}, {16, 31, 43, 51}, {17, 33, 45, 54}, {18, 35, 48, 57}, {19, 37, 51, 60}, {19, 38, 53, 63}, {20, 40, 56, 66}, {21, 43, 59, 70}, {22, 45, 62, 74}, {24, 47, 65, 77}, {25, 49, 68, 81}};

    public boolean getElement(int x, int y) {
        return this.moduleMatrix[x][y];
    }

    public int getNumErrorCollectionCode() {
        return this.numErrorCollectionCode[this.version - 1][this.errorCollectionLevel];
    }

    public int getNumRSBlocks() {
        return this.numRSBlocks[this.version - 1][this.errorCollectionLevel];
    }

    public QRCodeSymbol(boolean[][] moduleMatrix) {
        this.moduleMatrix = moduleMatrix;
        this.width = moduleMatrix.length;
        this.height = moduleMatrix[0].length;
        this.initialize();
    }

    void initialize() {
        this.version = (this.width - 17) / 4;
        Point[][] alignmentPattern = new Point[1][1];
        int[] logicalSeeds = new int[1];
        if (this.version >= 2 && this.version <= 40) {
            logicalSeeds = LogicalSeed.getSeed(this.version);
            alignmentPattern = new Point[logicalSeeds.length][logicalSeeds.length];
        }

        for (int col = 0; col < logicalSeeds.length; ++col) {
            for (int row = 0; row < logicalSeeds.length; ++row) {
                alignmentPattern[row][col] = new Point(logicalSeeds[row], logicalSeeds[col]);
            }
        }

        this.alignmentPattern = alignmentPattern;
        this.dataCapacity = this.calcDataCapacity();
        boolean[] formatInformation = this.readFormatInformation();
        this.decodeFormatInformation(formatInformation);
        this.unmask();
    }

    public int getVersion() {
        return this.version;
    }

    public String getVersionReference() {
        char[] versionReferenceCharacter = new char[]{'L', 'M', 'Q', 'H'};
        return Integer.toString(this.version) + "-" + versionReferenceCharacter[this.errorCollectionLevel];
    }

    public Point[][] getAlignmentPattern() {
        return this.alignmentPattern;
    }

    boolean[] readFormatInformation() {
        boolean[] modules = new boolean[15];


        for (int i = 0; i <= 5; ++i) {
            modules[i] = this.getElement(8, i);
        }

        modules[6] = this.getElement(8, 7);
        modules[7] = this.getElement(8, 8);
        modules[8] = this.getElement(7, 8);

        for (int i = 9; i <= 14; ++i) {
            modules[i] = this.getElement(14 - i, 8);
        }

        int maskPattern = 21522;

        for (int i = 0; i <= 14; ++i) {
            boolean xorBit = false;
            if ((maskPattern >>> i & 1) == 1) {
                xorBit = true;
            } else {
                xorBit = false;
            }

            if (modules[i] == xorBit) {
                modules[i] = false;
            } else {
                modules[i] = true;
            }
        }

        BCH15_5 corrector = new BCH15_5(modules);
        boolean[] output = corrector.correct();
        boolean[] formatInformation = new boolean[5];

        for (int i = 0; i < 5; ++i) {
            formatInformation[i] = output[10 + i];
        }

        return formatInformation;
    }

    void unmask() {
        boolean[][] maskPattern = this.generateMaskPattern();
        int size = this.getWidth();

        for (int y = 0; y < size; ++y) {
            for (int x = 0; x < size; ++x) {
                if (maskPattern[x][y]) {
                    this.reverseElement(x, y);
                }
            }
        }

    }

    boolean[][] generateMaskPattern() {
        int maskPatternReferer = this.getMaskPatternReferer();
        int width = this.getWidth();
        int height = this.getHeight();
        boolean[][] maskPattern = new boolean[width][height];

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                if (!this.isInFunctionPattern(x, y)) {
                    switch (maskPatternReferer) {
                        case 0:
                            if ((y + x) % 2 == 0) {
                                maskPattern[x][y] = true;
                            }
                            break;
                        case 1:
                            if (y % 2 == 0) {
                                maskPattern[x][y] = true;
                            }
                            break;
                        case 2:
                            if (x % 3 == 0) {
                                maskPattern[x][y] = true;
                            }
                            break;
                        case 3:
                            if ((y + x) % 3 == 0) {
                                maskPattern[x][y] = true;
                            }
                            break;
                        case 4:
                            if ((y / 2 + x / 3) % 2 == 0) {
                                maskPattern[x][y] = true;
                            }
                            break;
                        case 5:
                            if (y * x % 2 + y * x % 3 == 0) {
                                maskPattern[x][y] = true;
                            }
                            break;
                        case 6:
                            if ((y * x % 2 + y * x % 3) % 2 == 0) {
                                maskPattern[x][y] = true;
                            }
                            break;
                        case 7:
                            if ((y * x % 3 + (y + x) % 2) % 2 == 0) {
                                maskPattern[x][y] = true;
                            }
                    }
                }
            }
        }

        return maskPattern;
    }

    private int calcDataCapacity() {

        int version = this.getVersion();
        byte numFormatAndVersionInfoModule;
        if (version <= 6) {
            numFormatAndVersionInfoModule = 31;
        } else {
            numFormatAndVersionInfoModule = 67;
        }

        int sqrtCenters = version / 7 + 2;
        int modulesLeft = version == 1 ? 192 : 192 + (sqrtCenters * sqrtCenters - 3) * 25;
        int numFunctionPatternModule = modulesLeft + 8 * version + 2 - (sqrtCenters - 2) * 10;
        int dataCapacity = (this.width * this.width - numFunctionPatternModule - numFormatAndVersionInfoModule) / 8;
        return dataCapacity;
    }

    public int getDataCapacity() {
        return this.dataCapacity;
    }

    void decodeFormatInformation(boolean[] formatInformation) {
        if (!formatInformation[4]) {
            if (formatInformation[3]) {
                this.errorCollectionLevel = 0;
            } else {
                this.errorCollectionLevel = 1;
            }
        } else if (formatInformation[3]) {
            this.errorCollectionLevel = 2;
        } else {
            this.errorCollectionLevel = 3;
        }

        for (int i = 2; i >= 0; --i) {
            if (formatInformation[i]) {
                this.maskPattern += 1 << i;
            }
        }

    }

    public int getErrorCollectionLevel() {
        return this.errorCollectionLevel;
    }

    public int getMaskPatternReferer() {
        return this.maskPattern;
    }

    public String getMaskPatternRefererAsString() {
        String maskPattern = Integer.toString(this.getMaskPatternReferer(), 2);
        int length = maskPattern.length();

        for (int i = 0; i < 3 - length; ++i) {
            maskPattern = "0" + maskPattern;
        }

        return maskPattern;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int[] getBlocks() {
        int width = this.getWidth();
        int height = this.getHeight();
        int x = width - 1;
        int y = height - 1;
        Vector codeBits = new Vector();
        Vector codeWords = new Vector();
        int tempWord = 0;
        int figure = 7;
        int isNearFinish = 0;
        boolean READ_UP = true;
        boolean READ_DOWN = false;
        boolean direction = true;

        do {
            codeBits.addElement(new Boolean(this.getElement(x, y)));
            if (this.getElement(x, y)) {
                tempWord += 1 << figure;
            }

            --figure;
            if (figure == -1) {
                codeWords.addElement(new Integer(tempWord));
                figure = 7;
                tempWord = 0;
            }

            do {
                if (direction) {
                    if ((x + isNearFinish) % 2 == 0) {
                        --x;
                    } else if (y > 0) {
                        ++x;
                        --y;
                    } else {
                        --x;
                        if (x == 6) {
                            --x;
                            isNearFinish = 1;
                        }

                        direction = false;
                    }
                } else if ((x + isNearFinish) % 2 == 0) {
                    --x;
                } else if (y < height - 1) {
                    ++x;
                    ++y;
                } else {
                    --x;
                    if (x == 6) {
                        --x;
                        isNearFinish = 1;
                    }

                    direction = true;
                }
            } while (this.isInFunctionPattern(x, y));
        } while (x != -1);

        int[] gotWords = new int[codeWords.size()];

        for (int i = 0; i < codeWords.size(); ++i) {
            Integer temp = (Integer) codeWords.elementAt(i);
            gotWords[i] = temp;
        }

        return gotWords;
    }

    public void reverseElement(int x, int y) {
        this.moduleMatrix[x][y] = !this.moduleMatrix[x][y];
    }

    public boolean isInFunctionPattern(int targetX, int targetY) {
        if (targetX < 9 && targetY < 9) {
            return true;
        } else if (targetX > this.getWidth() - 9 && targetY < 9) {
            return true;
        } else if (targetX < 9 && targetY > this.getHeight() - 9) {
            return true;
        } else {
            if (this.version >= 7) {
                if (targetX > this.getWidth() - 12 && targetY < 6) {
                    return true;
                }

                if (targetX < 6 && targetY > this.getHeight() - 12) {
                    return true;
                }
            }

            if (targetX != 6 && targetY != 6) {
                Point[][] alignmentPattern = this.getAlignmentPattern();
                int sideLength = alignmentPattern.length;

                for (int y = 0; y < sideLength; ++y) {
                    for (int x = 0; x < sideLength; ++x) {
                        if ((x != 0 || y != 0) && (x != sideLength - 1 || y != 0) && (x != 0 || y != sideLength - 1) && Math.abs(alignmentPattern[x][y].getX() - targetX) < 3 && Math.abs(alignmentPattern[x][y].getY() - targetY) < 3) {
                            return true;
                        }
                    }
                }

                return false;
            } else {
                return true;
            }
        }
    }
}

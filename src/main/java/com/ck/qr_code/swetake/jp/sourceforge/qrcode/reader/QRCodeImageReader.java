package com.ck.qr_code.swetake.jp.sourceforge.qrcode.reader;

import java.util.Vector;

import com.ck.qr_code.swetake.jp.sourceforge.qrcode.QRCodeDecoder;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.data.QRCodeSymbol;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.exception.*;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.geom.Axis;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.geom.Line;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.geom.Point;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.geom.SamplingGrid;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.pattern.AlignmentPattern;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.pattern.FinderPattern;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.util.DebugCanvas;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.util.QRCodeUtility;


public class QRCodeImageReader {
    DebugCanvas canvas = QRCodeDecoder.getCanvas();
    public static int DECIMAL_POINT = 21;
    public static final boolean POINT_DARK = true;
    public static final boolean POINT_LIGHT = false;
    SamplingGrid samplingGrid;
    boolean[][] bitmap;

    public QRCodeImageReader() {
    }

    boolean[][] applyMedianFilter(boolean[][] image, int threshold) {
        boolean[][] filteredMatrix = new boolean[image.length][image[0].length];

        for(int y = 1; y < image[0].length - 1; ++y) {
            for(int x = 1; x < image.length - 1; ++x) {
                int numPointDark = 0;

                for(int fy = -1; fy < 2; ++fy) {
                    for(int fx = -1; fx < 2; ++fx) {
                        if (image[x + fx][y + fy]) {
                            ++numPointDark;
                        }
                    }
                }

                if (numPointDark > threshold) {
                    filteredMatrix[x][y] = true;
                }
            }
        }

        return filteredMatrix;
    }

    boolean[][] applyCrossMaskingMedianFilter(boolean[][] image, int threshold) {
        boolean[][] filteredMatrix = new boolean[image.length][image[0].length];

        for(int y = 2; y < image[0].length - 2; ++y) {
            for(int x = 2; x < image.length - 2; ++x) {
                int numPointDark = 0;

                for(int f = -2; f < 3; ++f) {
                    if (image[x + f][y]) {
                        ++numPointDark;
                    }

                    if (image[x][y + f]) {
                        ++numPointDark;
                    }
                }

                if (numPointDark > threshold) {
                    filteredMatrix[x][y] = true;
                }
            }
        }

        return filteredMatrix;
    }

    boolean[][] filterImage(int[][] image) {
        this.imageToGrayScale(image);
        boolean[][] bitmap = this.grayScaleToBitmap(image);
        return bitmap;
    }

    void imageToGrayScale(int[][] image) {
        for(int y = 0; y < image[0].length; ++y) {
            for(int x = 0; x < image.length; ++x) {
                int r = image[x][y] >> 16 & 255;
                int g = image[x][y] >> 8 & 255;
                int b = image[x][y] & 255;
                int m = (r * 30 + g * 59 + b * 11) / 100;
                image[x][y] = m;
            }
        }

    }

    boolean[][] grayScaleToBitmap(int[][] grayScale) {
        int[][] middle = this.getMiddleBrightnessPerArea(grayScale);
        int sqrtNumArea = middle.length;
        int areaWidth = grayScale.length / sqrtNumArea;
        int areaHeight = grayScale[0].length / sqrtNumArea;
        boolean[][] bitmap = new boolean[grayScale.length][grayScale[0].length];

        for(int ay = 0; ay < sqrtNumArea; ++ay) {
            for(int ax = 0; ax < sqrtNumArea; ++ax) {
                for(int dy = 0; dy < areaHeight; ++dy) {
                    for(int dx = 0; dx < areaWidth; ++dx) {
                        bitmap[areaWidth * ax + dx][areaHeight * ay + dy] = grayScale[areaWidth * ax + dx][areaHeight * ay + dy] < middle[ax][ay];
                    }
                }
            }
        }

        return bitmap;
    }

    int[][] getMiddleBrightnessPerArea(int[][] image) {
        int areaWidth = image.length / 4;
        int areaHeight = image[0].length / 4;
        int[][][] minmax = new int[4][4][2];

        int ax;
        int dy;
        for(int ay = 0; ay < 4; ++ay) {
            for(ax = 0; ax < 4; ++ax) {
                minmax[ax][ay][0] = 255;

                for(dy = 0; dy < areaHeight; ++dy) {
                    for(int dx = 0; dx < areaWidth; ++dx) {
                        int target = image[areaWidth * ax + dx][areaHeight * ay + dy];
                        if (target < minmax[ax][ay][0]) {
                            minmax[ax][ay][0] = target;
                        }

                        if (target > minmax[ax][ay][1]) {
                            minmax[ax][ay][1] = target;
                        }
                    }
                }
            }
        }

        int[][] middle = new int[4][4];

        for(ax = 0; ax < 4; ++ax) {
            for(dy = 0; dy < 4; ++dy) {
                middle[dy][ax] = (minmax[dy][ax][0] + minmax[dy][ax][1]) / 2;
            }
        }

        return middle;
    }

    public QRCodeSymbol getQRCodeSymbol(int[][] image) throws SymbolNotFoundException {
        int longSide = image.length < image[0].length ? image[0].length : image.length;
        DECIMAL_POINT = 23 - QRCodeUtility.sqrt(longSide / 256);
        this.bitmap = this.filterImage(image);
        this.canvas.println("Drawing matrix.");
        this.canvas.drawMatrix(this.bitmap);
        this.canvas.println("Scanning Finder Pattern.");
        FinderPattern finderPattern = null;

        try {
            finderPattern = FinderPattern.findFinderPattern(this.bitmap);
        } catch (VersionInformationException | FinderPatternNotFoundException var17) {
            throw new SymbolNotFoundException(var17.getMessage());
        }

        this.canvas.println("FinderPattern at");
        String finderPatternCoordinates = finderPattern.getCenter(0).toString() + finderPattern.getCenter(1).toString() + finderPattern.getCenter(2).toString();
        this.canvas.println(finderPatternCoordinates);
        int[] sincos = finderPattern.getAngle();
        this.canvas.println("Angle*4098: Sin " + Integer.toString(sincos[0]) + "  " + "Cos " + Integer.toString(sincos[1]));
        int version = finderPattern.getVersion();
        this.canvas.println("Version: " + Integer.toString(version));
        if (version >= 1 && version <= 40) {
            AlignmentPattern alignmentPattern = null;

            try {
                alignmentPattern = AlignmentPattern.findAlignmentPattern(this.bitmap, finderPattern);
            } catch (AlignmentPatternNotFoundException var13) {
                throw new SymbolNotFoundException(var13.getMessage());
            }

            int matrixLength = alignmentPattern.getCenter().length;
            this.canvas.println("AlignmentPatterns at");

            for(int y = 0; y < matrixLength; ++y) {
                String alignmentPatternCoordinates = "";

                for(int x = 0; x < matrixLength; ++x) {
                    alignmentPatternCoordinates = alignmentPatternCoordinates + alignmentPattern.getCenter()[x][y].toString();
                }

                this.canvas.println(alignmentPatternCoordinates);
            }

            this.canvas.println("Creating sampling grid.");
            this.samplingGrid = this.getSamplingGrid(finderPattern, alignmentPattern);
            this.canvas.println("Reading grid.");
            boolean[][] qRCodeMatrix = (boolean[][])null;

            try {
                qRCodeMatrix = this.getQRCodeMatrix(this.bitmap, this.samplingGrid);
            } catch (ArrayIndexOutOfBoundsException var12) {
                throw new SymbolNotFoundException("Sampling grid exceeded image boundary");
            }

            return new QRCodeSymbol(qRCodeMatrix);
        } else {
            throw new InvalidVersionException("Invalid version: " + version);
        }
    }

    public QRCodeSymbol getQRCodeSymbolWithAdjustedGrid(Point adjust) throws IllegalStateException, SymbolNotFoundException {
        if (this.bitmap != null && this.samplingGrid != null) {
            this.samplingGrid.adjust(adjust);
            this.canvas.println("Sampling grid adjusted d(" + adjust.getX() + "," + adjust.getY() + ")");
            boolean[][] qRCodeMatrix = (boolean[][])null;

            try {
                qRCodeMatrix = this.getQRCodeMatrix(this.bitmap, this.samplingGrid);
            } catch (ArrayIndexOutOfBoundsException var4) {
                throw new SymbolNotFoundException("Sampling grid exceeded image boundary");
            }

            return new QRCodeSymbol(qRCodeMatrix);
        } else {
            throw new IllegalStateException("This method must be called after QRCodeImageReader.getQRCodeSymbol() called");
        }
    }

    SamplingGrid getSamplingGrid(FinderPattern finderPattern, AlignmentPattern alignmentPattern) {
        Point[][] centers = alignmentPattern.getCenter();
        int version = finderPattern.getVersion();
        int sqrtCenters = version / 7 + 2;
        centers[0][0] = finderPattern.getCenter(0);
        centers[sqrtCenters - 1][0] = finderPattern.getCenter(1);
        centers[0][sqrtCenters - 1] = finderPattern.getCenter(2);
        int sqrtNumArea = sqrtCenters - 1;
        SamplingGrid samplingGrid = new SamplingGrid(sqrtNumArea);
        Axis axis = new Axis(finderPattern.getAngle(), finderPattern.getModuleSize());

        for(int ay = 0; ay < sqrtNumArea; ++ay) {
            for(int ax = 0; ax < sqrtNumArea; ++ax) {
                QRCodeImageReader.ModulePitch modulePitch = new QRCodeImageReader.ModulePitch();
                Line baseLineX = new Line();
                Line baseLineY = new Line();
                axis.setModulePitch(finderPattern.getModuleSize());
                Point[][] logicalCenters = AlignmentPattern.getLogicalCenter(finderPattern);
                Point upperLeftPoint = centers[ax][ay];
                Point upperRightPoint = centers[ax + 1][ay];
                Point lowerLeftPoint = centers[ax][ay + 1];
                Point lowerRightPoint = centers[ax + 1][ay + 1];
                Point logicalUpperLeftPoint = logicalCenters[ax][ay];
                Point logicalUpperRightPoint = logicalCenters[ax + 1][ay];
                Point logicalLowerLeftPoint = logicalCenters[ax][ay + 1];
                Point logicalLowerRightPoint = logicalCenters[ax + 1][ay + 1];
                if (ax == 0 && ay == 0) {
                    if (sqrtNumArea == 1) {
                        upperLeftPoint = axis.translate(upperLeftPoint, -3, -3);
                        upperRightPoint = axis.translate(upperRightPoint, 3, -3);
                        lowerLeftPoint = axis.translate(lowerLeftPoint, -3, 3);
                        lowerRightPoint = axis.translate(lowerRightPoint, 6, 6);
                        logicalUpperLeftPoint.translate(-6, -6);
                        logicalUpperRightPoint.translate(3, -3);
                        logicalLowerLeftPoint.translate(-3, 3);
                        logicalLowerRightPoint.translate(6, 6);
                    } else {
                        upperLeftPoint = axis.translate(upperLeftPoint, -3, -3);
                        upperRightPoint = axis.translate(upperRightPoint, 0, -6);
                        lowerLeftPoint = axis.translate(lowerLeftPoint, -6, 0);
                        logicalUpperLeftPoint.translate(-6, -6);
                        logicalUpperRightPoint.translate(0, -6);
                        logicalLowerLeftPoint.translate(-6, 0);
                    }
                } else if (ax == 0 && ay == sqrtNumArea - 1) {
                    upperLeftPoint = axis.translate(upperLeftPoint, -6, 0);
                    lowerLeftPoint = axis.translate(lowerLeftPoint, -3, 3);
                    lowerRightPoint = axis.translate(lowerRightPoint, 0, 6);
                    logicalUpperLeftPoint.translate(-6, 0);
                    logicalLowerLeftPoint.translate(-6, 6);
                    logicalLowerRightPoint.translate(0, 6);
                } else if (ax == sqrtNumArea - 1 && ay == 0) {
                    upperLeftPoint = axis.translate(upperLeftPoint, 0, -6);
                    upperRightPoint = axis.translate(upperRightPoint, 3, -3);
                    lowerRightPoint = axis.translate(lowerRightPoint, 6, 0);
                    logicalUpperLeftPoint.translate(0, -6);
                    logicalUpperRightPoint.translate(6, -6);
                    logicalLowerRightPoint.translate(6, 0);
                } else if (ax == sqrtNumArea - 1 && ay == sqrtNumArea - 1) {
                    lowerLeftPoint = axis.translate(lowerLeftPoint, 0, 6);
                    upperRightPoint = axis.translate(upperRightPoint, 6, 0);
                    lowerRightPoint = axis.translate(lowerRightPoint, 6, 6);
                    logicalLowerLeftPoint.translate(0, 6);
                    logicalUpperRightPoint.translate(6, 0);
                    logicalLowerRightPoint.translate(6, 6);
                } else if (ax == 0) {
                    upperLeftPoint = axis.translate(upperLeftPoint, -6, 0);
                    lowerLeftPoint = axis.translate(lowerLeftPoint, -6, 0);
                    logicalUpperLeftPoint.translate(-6, 0);
                    logicalLowerLeftPoint.translate(-6, 0);
                } else if (ax == sqrtNumArea - 1) {
                    upperRightPoint = axis.translate(upperRightPoint, 6, 0);
                    lowerRightPoint = axis.translate(lowerRightPoint, 6, 0);
                    logicalUpperRightPoint.translate(6, 0);
                    logicalLowerRightPoint.translate(6, 0);
                } else if (ay == 0) {
                    upperLeftPoint = axis.translate(upperLeftPoint, 0, -6);
                    upperRightPoint = axis.translate(upperRightPoint, 0, -6);
                    logicalUpperLeftPoint.translate(0, -6);
                    logicalUpperRightPoint.translate(0, -6);
                } else if (ay == sqrtNumArea - 1) {
                    lowerLeftPoint = axis.translate(lowerLeftPoint, 0, 6);
                    lowerRightPoint = axis.translate(lowerRightPoint, 0, 6);
                    logicalLowerLeftPoint.translate(0, 6);
                    logicalLowerRightPoint.translate(0, 6);
                }

                if (ax == 0) {
                    logicalUpperRightPoint.translate(1, 0);
                    logicalLowerRightPoint.translate(1, 0);
                } else {
                    logicalUpperLeftPoint.translate(-1, 0);
                    logicalLowerLeftPoint.translate(-1, 0);
                }

                if (ay == 0) {
                    logicalLowerLeftPoint.translate(0, 1);
                    logicalLowerRightPoint.translate(0, 1);
                } else {
                    logicalUpperLeftPoint.translate(0, -1);
                    logicalUpperRightPoint.translate(0, -1);
                }

                int logicalWidth = logicalUpperRightPoint.getX() - logicalUpperLeftPoint.getX();
                int logicalHeight = logicalLowerLeftPoint.getY() - logicalUpperLeftPoint.getY();
                if (version < 7) {
                    logicalWidth += 3;
                    logicalHeight += 3;
                }

                modulePitch.top = this.getAreaModulePitch(upperLeftPoint, upperRightPoint, logicalWidth - 1);
                modulePitch.left = this.getAreaModulePitch(upperLeftPoint, lowerLeftPoint, logicalHeight - 1);
                modulePitch.bottom = this.getAreaModulePitch(lowerLeftPoint, lowerRightPoint, logicalWidth - 1);
                modulePitch.right = this.getAreaModulePitch(upperRightPoint, lowerRightPoint, logicalHeight - 1);
                baseLineX.setP1(upperLeftPoint);
                baseLineY.setP1(upperLeftPoint);
                baseLineX.setP2(lowerLeftPoint);
                baseLineY.setP2(upperRightPoint);
                samplingGrid.initGrid(ax, ay, logicalWidth, logicalHeight);

                int i;
                for(i = 0; i < logicalWidth; ++i) {
                    Line gridLineX = new Line(baseLineX.getP1(), baseLineX.getP2());
                    axis.setOrigin(gridLineX.getP1());
                    axis.setModulePitch(modulePitch.top);
                    gridLineX.setP1(axis.translate(i, 0));
                    axis.setOrigin(gridLineX.getP2());
                    axis.setModulePitch(modulePitch.bottom);
                    gridLineX.setP2(axis.translate(i, 0));
                    samplingGrid.setXLine(ax, ay, i, gridLineX);
                }

                for(i = 0; i < logicalHeight; ++i) {
                    Line gridLineY = new Line(baseLineY.getP1(), baseLineY.getP2());
                    axis.setOrigin(gridLineY.getP1());
                    axis.setModulePitch(modulePitch.left);
                    gridLineY.setP1(axis.translate(0, i));
                    axis.setOrigin(gridLineY.getP2());
                    axis.setModulePitch(modulePitch.right);
                    gridLineY.setP2(axis.translate(0, i));
                    samplingGrid.setYLine(ax, ay, i, gridLineY);
                }
            }
        }

        return samplingGrid;
    }

    int getAreaModulePitch(Point start, Point end, int logicalDistance) {
        Line tempLine = new Line(start, end);
        int realDistance = tempLine.getLength();
        int modulePitch = (realDistance << DECIMAL_POINT) / logicalDistance;
        return modulePitch;
    }

    boolean[][] getQRCodeMatrix(boolean[][] image, SamplingGrid gridLines) throws ArrayIndexOutOfBoundsException {
        int gridSize = gridLines.getTotalWidth();
        this.canvas.println("gridSize=" + gridSize);
        Point bottomRightPoint = null;
        boolean[][] sampledMatrix = new boolean[gridSize][gridSize];

        for(int ay = 0; ay < gridLines.getHeight(); ++ay) {
            for(int ax = 0; ax < gridLines.getWidth(); ++ax) {
                new Vector();

                for(int y = 0; y < gridLines.getHeight(ax, ay); ++y) {
                    for(int x = 0; x < gridLines.getWidth(ax, ay); ++x) {
                        int x1 = gridLines.getXLine(ax, ay, x).getP1().getX();
                        int y1 = gridLines.getXLine(ax, ay, x).getP1().getY();
                        int x2 = gridLines.getXLine(ax, ay, x).getP2().getX();
                        int y2 = gridLines.getXLine(ax, ay, x).getP2().getY();
                        int x3 = gridLines.getYLine(ax, ay, y).getP1().getX();
                        int y3 = gridLines.getYLine(ax, ay, y).getP1().getY();
                        int x4 = gridLines.getYLine(ax, ay, y).getP2().getX();
                        int y4 = gridLines.getYLine(ax, ay, y).getP2().getY();
                        int e = (y2 - y1) * (x3 - x4) - (y4 - y3) * (x1 - x2);
                        int f = (x1 * y2 - x2 * y1) * (x3 - x4) - (x3 * y4 - x4 * y3) * (x1 - x2);
                        int g = (x3 * y4 - x4 * y3) * (y2 - y1) - (x1 * y2 - x2 * y1) * (y4 - y3);
                        sampledMatrix[gridLines.getX(ax, x)][gridLines.getY(ay, y)] = image[f / e][g / e];
                        if (ay == gridLines.getHeight() - 1 && ax == gridLines.getWidth() - 1 && y == gridLines.getHeight(ax, ay) - 1 && x == gridLines.getWidth(ax, ay) - 1) {
                            bottomRightPoint = new Point(f / e, g / e);
                        }
                    }
                }
            }
        }

        if (bottomRightPoint == null || bottomRightPoint.getX() <= image.length - 1 && bottomRightPoint.getY() <= image[0].length - 1) {
            this.canvas.drawPoint(bottomRightPoint, 8947967);
            return sampledMatrix;
        } else {
            throw new ArrayIndexOutOfBoundsException("Sampling grid pointed out of image");
        }
    }

    protected class ModulePitch {
        public int top;
        public int left;
        public int bottom;
        public int right;

        protected ModulePitch() {
        }
    }
}

package com.ck.qr_code.swetake.jp.sourceforge.qrcode.pattern;

import java.util.Vector;

import com.ck.qr_code.swetake.jp.sourceforge.qrcode.QRCodeDecoder;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.exception.FinderPatternNotFoundException;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.exception.InvalidVersionException;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.exception.InvalidVersionInfoException;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.exception.VersionInformationException;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.geom.Axis;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.geom.Line;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.geom.Point;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.reader.QRCodeImageReader;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.util.DebugCanvas;

public class FinderPattern {
    public static final int UL = 0;
    public static final int UR = 1;
    public static final int DL = 2;
    static final int[] VersionInfoBit = new int[]{31892, 34236, 39577, 42195, 48118, 51042, 55367, 58893, 63784, 68472, 70749, 76311, 79154, 84390, 87683, 92361, 96236, 102084, 102881, 110507, 110734, 117786, 119615, 126325, 127568, 133589, 136944, 141498, 145311, 150283, 152622, 158308, 161089, 167017};
    static DebugCanvas canvas = QRCodeDecoder.getCanvas();
    Point[] center;
    int version;
    int[] sincos;
    int[] width;
    int[] moduleSize;

    public static FinderPattern findFinderPattern(boolean[][] image) throws FinderPatternNotFoundException, VersionInformationException {
        Line[] lineAcross = findLineAcross(image);
        Line[] lineCross = findLineCross(lineAcross);
        Point[] center = (Point[])null;

        try {
            center = getCenter(lineCross);
        } catch (FinderPatternNotFoundException var10) {
            throw var10;
        }

        int[] sincos = getAngle(center);
        center = sort(center, sincos);
        int[] width = getWidth(image, center, sincos);
        int[] moduleSize = new int[]{(width[0] << QRCodeImageReader.DECIMAL_POINT) / 7, (width[1] << QRCodeImageReader.DECIMAL_POINT) / 7, (width[2] << QRCodeImageReader.DECIMAL_POINT) / 7};
        int version = calcRoughVersion(center, width);
        if (version > 6) {
            try {
                version = calcExactVersion(center, sincos, moduleSize, image);
            } catch (VersionInformationException var9) {
            }
        }

        return new FinderPattern(center, version, sincos, width, moduleSize);
    }

    FinderPattern(Point[] center, int version, int[] sincos, int[] width, int[] moduleSize) {
        this.center = center;
        this.version = version;
        this.sincos = sincos;
        this.width = width;
        this.moduleSize = moduleSize;
    }

    public Point[] getCenter() {
        return this.center;
    }

    public Point getCenter(int position) {
        return position >= 0 && position <= 2 ? this.center[position] : null;
    }

    public int getWidth(int position) {
        return this.width[position];
    }

    public int[] getAngle() {
        return this.sincos;
    }

    public int getVersion() {
        return this.version;
    }

    public int getModuleSize() {
        return this.moduleSize[0];
    }

    public int getModuleSize(int place) {
        return this.moduleSize[place];
    }

    public int getSqrtNumModules() {
        return 17 + 4 * this.version;
    }

    static Line[] findLineAcross(boolean[][] image) {

        int imageWidth = image.length;
        int imageHeight = image[0].length;
        Point current = new Point();
        Vector lineAcross = new Vector();
        int[] lengthBuffer = new int[5];
        int bufferPointer = 0;
        boolean direction = false;
        boolean lastElement = false;

        while(true) {
            boolean currentElement = image[current.getX()][current.getY()];
            int i;
            if (currentElement == lastElement) {
                int var10002 = lengthBuffer[bufferPointer]++;
            } else {
                if (!currentElement && checkPattern(lengthBuffer, bufferPointer)) {
                    int y1;
                    int x2;
                    int y2;
                    int j;
                    if (!direction) {
                        i = current.getX();

                        for(j = 0; j < 5; ++j) {
                            i -= lengthBuffer[j];
                        }

                        x2 = current.getX() - 1;
                        y1 = y2 = current.getY();
                    } else {
                        i = x2 = current.getX();
                        y1 = current.getY();

                        for(j = 0; j < 5; ++j) {
                            y1 -= lengthBuffer[j];
                        }

                        y2 = current.getY() - 1;
                    }

                    lineAcross.addElement(new Line(i, y1, x2, y2));
                }

                bufferPointer = (bufferPointer + 1) % 5;
                lengthBuffer[bufferPointer] = 1;
                lastElement = !lastElement;
            }

            if (!direction) {
                if (current.getX() < imageWidth - 1) {
                    current.translate(1, 0);
                } else if (current.getY() < imageHeight - 1) {
                    current.set(0, current.getY() + 1);
                    lengthBuffer = new int[5];
                } else {
                    current.set(0, 0);
                    lengthBuffer = new int[5];
                    direction = true;
                }
            } else if (current.getY() < imageHeight - 1) {
                current.translate(0, 1);
            } else {
                if (current.getX() >= imageWidth - 1) {
                    Line[] foundLines = new Line[lineAcross.size()];

                    for(i = 0; i < foundLines.length; ++i) {
                        foundLines[i] = (Line)lineAcross.elementAt(i);
                    }

                    canvas.drawLines(foundLines, 12320699);
                    return foundLines;
                }

                current.set(current.getX() + 1, 0);
                lengthBuffer = new int[5];
            }
        }
    }

    static boolean checkPattern(int[] buffer, int pointer) {
        int[] modelRatio = new int[]{1, 1, 3, 1, 1};
        int baselength = 0;

        int i;
        for(i = 0; i < 5; ++i) {
            baselength += buffer[i];
        }

        baselength <<= QRCodeImageReader.DECIMAL_POINT;
        baselength /= 7;

        for(i = 0; i < 5; ++i) {
            int leastlength = baselength * modelRatio[i] - baselength / 2;
            int mostlength = baselength * modelRatio[i] + baselength / 2;
            int targetlength = buffer[(pointer + i + 1) % 5] << QRCodeImageReader.DECIMAL_POINT;
            if (targetlength < leastlength || targetlength > mostlength) {
                return false;
            }
        }

        return true;
    }

    static Line[] findLineCross(Line[] lineAcross) {
        Vector crossLines = new Vector();
        Vector lineNeighbor = new Vector();
        Vector lineCandidate = new Vector();

        int i;
        for(i = 0; i < lineAcross.length; ++i) {
            lineCandidate.addElement(lineAcross[i]);
        }

        int j;
        for(i = 0; i < lineCandidate.size() - 1; ++i) {
            lineNeighbor.removeAllElements();
            lineNeighbor.addElement(lineCandidate.elementAt(i));

            for(j = i + 1; j < lineCandidate.size(); ++j) {
                Line compareLine;
                int k;
                if (Line.isNeighbor((Line)lineNeighbor.lastElement(), (Line)lineCandidate.elementAt(j))) {
                    lineNeighbor.addElement(lineCandidate.elementAt(j));
                    compareLine = (Line)lineNeighbor.lastElement();
                    if (lineNeighbor.size() * 5 > compareLine.getLength() && j == lineCandidate.size() - 1) {
                        crossLines.addElement(lineNeighbor.elementAt(lineNeighbor.size() / 2));

                        for(k = 0; k < lineNeighbor.size(); ++k) {
                            lineCandidate.removeElement(lineNeighbor.elementAt(k));
                        }
                    }
                } else if (cantNeighbor((Line)lineNeighbor.lastElement(), (Line)lineCandidate.elementAt(j)) || j == lineCandidate.size() - 1) {
                    compareLine = (Line)lineNeighbor.lastElement();
                    if (lineNeighbor.size() * 6 > compareLine.getLength()) {
                        crossLines.addElement(lineNeighbor.elementAt(lineNeighbor.size() / 2));

                        for(k = 0; k < lineNeighbor.size(); ++k) {
                            lineCandidate.removeElement(lineNeighbor.elementAt(k));
                        }
                    }
                    break;
                }
            }
        }

        Line[] foundLines = new Line[crossLines.size()];

        for(j = 0; j < foundLines.length; ++j) {
            foundLines[j] = (Line)crossLines.elementAt(j);
        }

        return foundLines;
    }

    static boolean cantNeighbor(Line line1, Line line2) {
        if (Line.isCross(line1, line2)) {
            return true;
        } else {
            return line1.isHorizontal() ? Math.abs(line1.getP1().getY() - line2.getP1().getY()) > 1 : Math.abs(line1.getP1().getX() - line2.getP1().getX()) > 1;
        }
    }

    static int[] getAngle(Point[] centers) {
        Line[] additionalLine = new Line[3];

        for(int i = 0; i < additionalLine.length; ++i) {
            additionalLine[i] = new Line(centers[i], centers[(i + 1) % additionalLine.length]);
        }

        Line remoteLine = Line.getLongest(additionalLine);
        Point originPoint = new Point();

        for(int i = 0; i < centers.length; ++i) {
            if (!remoteLine.getP1().equals(centers[i]) && !remoteLine.getP2().equals(centers[i])) {
                originPoint = centers[i];
                break;
            }
        }

        canvas.println("originPoint is: " + originPoint);
        new Point();
        Point remotePoint;
        if (originPoint.getY() <= remoteLine.getP1().getY() & originPoint.getY() <= remoteLine.getP2().getY()) {
            if (remoteLine.getP1().getX() < remoteLine.getP2().getX()) {
                remotePoint = remoteLine.getP2();
            } else {
                remotePoint = remoteLine.getP1();
            }
        } else if (originPoint.getX() >= remoteLine.getP1().getX() & originPoint.getX() >= remoteLine.getP2().getX()) {
            if (remoteLine.getP1().getY() < remoteLine.getP2().getY()) {
                remotePoint = remoteLine.getP2();
            } else {
                remotePoint = remoteLine.getP1();
            }
        } else if (originPoint.getY() >= remoteLine.getP1().getY() & originPoint.getY() >= remoteLine.getP2().getY()) {
            if (remoteLine.getP1().getX() < remoteLine.getP2().getX()) {
                remotePoint = remoteLine.getP1();
            } else {
                remotePoint = remoteLine.getP2();
            }
        } else if (remoteLine.getP1().getY() < remoteLine.getP2().getY()) {
            remotePoint = remoteLine.getP1();
        } else {
            remotePoint = remoteLine.getP2();
        }

        int r = (new Line(originPoint, remotePoint)).getLength();
        int[] angle = new int[]{(remotePoint.getY() - originPoint.getY() << QRCodeImageReader.DECIMAL_POINT) / r, (remotePoint.getX() - originPoint.getX() << QRCodeImageReader.DECIMAL_POINT) / r};
        return angle;
    }

    static Point[] getCenter(Line[] crossLines) throws FinderPatternNotFoundException {
        Vector centers = new Vector();

        for(int i = 0; i < crossLines.length - 1; ++i) {
            Line compareLine = crossLines[i];

            for(int j = i + 1; j < crossLines.length; ++j) {
                Line comparedLine = crossLines[j];
                if (Line.isCross(compareLine, comparedLine)) {
                    int x;
                    int y;
                    if (compareLine.isHorizontal()) {
                        x = compareLine.getCenter().getX();
                        y = comparedLine.getCenter().getY();
                    } else {
                        x = comparedLine.getCenter().getX();
                        y = compareLine.getCenter().getY();
                    }

                    centers.addElement(new Point(x, y));
                }
            }
        }

        Point[] foundPoints = new Point[centers.size()];

        for(int i = 0; i < foundPoints.length; ++i) {
            foundPoints[i] = (Point)centers.elementAt(i);
        }

        if (foundPoints.length == 3) {
            canvas.drawPolygon(foundPoints, 267946120);
            return foundPoints;
        } else {
            throw new FinderPatternNotFoundException("Invalid number of Finder Pattern detected");
        }
    }

    static Point[] sort(Point[] centers, int[] angle) {
        Point[] sortedCenters = new Point[3];
        int quadrant = getURQuadrant(angle);
        switch(quadrant) {
        case 1:
            sortedCenters[1] = getPointAtSide(centers, 1, 2);
            sortedCenters[2] = getPointAtSide(centers, 2, 4);
            break;
        case 2:
            sortedCenters[1] = getPointAtSide(centers, 2, 4);
            sortedCenters[2] = getPointAtSide(centers, 8, 4);
            break;
        case 3:
            sortedCenters[1] = getPointAtSide(centers, 4, 8);
            sortedCenters[2] = getPointAtSide(centers, 1, 8);
            break;
        case 4:
            sortedCenters[1] = getPointAtSide(centers, 8, 1);
            sortedCenters[2] = getPointAtSide(centers, 2, 1);
        }

        for(int i = 0; i < centers.length; ++i) {
            if (!centers[i].equals(sortedCenters[1]) && !centers[i].equals(sortedCenters[2])) {
                sortedCenters[0] = centers[i];
            }
        }

        return sortedCenters;
    }

    static int getURQuadrant(int[] angle) {
        int sin = angle[0];
        int cos = angle[1];
        if (sin >= 0 && cos > 0) {
            return 1;
        } else if (sin > 0 && cos <= 0) {
            return 2;
        } else if (sin <= 0 && cos < 0) {
            return 3;
        } else {
            return sin < 0 && cos >= 0 ? 4 : 0;
        }
    }

    static Point getPointAtSide(Point[] points, int side1, int side2) {
        new Point();
        int x = side1 != 1 && side2 != 1 ? 2147483647 : 0;
        int y = side1 != 2 && side2 != 2 ? 2147483647 : 0;
        Point sidePoint = new Point(x, y);

        for(int i = 0; i < points.length; ++i) {
            switch(side1) {
            case 1:
                if (sidePoint.getX() < points[i].getX()) {
                    sidePoint = points[i];
                } else if (sidePoint.getX() == points[i].getX()) {
                    if (side2 == 2) {
                        if (sidePoint.getY() < points[i].getY()) {
                            sidePoint = points[i];
                        }
                    } else if (sidePoint.getY() > points[i].getY()) {
                        sidePoint = points[i];
                    }
                }
                break;
            case 2:
                if (sidePoint.getY() < points[i].getY()) {
                    sidePoint = points[i];
                } else if (sidePoint.getY() == points[i].getY()) {
                    if (side2 == 1) {
                        if (sidePoint.getX() < points[i].getX()) {
                            sidePoint = points[i];
                        }
                    } else if (sidePoint.getX() > points[i].getX()) {
                        sidePoint = points[i];
                    }
                }
            case 3:
            case 5:
            case 6:
            case 7:
            default:
                break;
            case 4:
                if (sidePoint.getX() > points[i].getX()) {
                    sidePoint = points[i];
                } else if (sidePoint.getX() == points[i].getX()) {
                    if (side2 == 2) {
                        if (sidePoint.getY() < points[i].getY()) {
                            sidePoint = points[i];
                        }
                    } else if (sidePoint.getY() > points[i].getY()) {
                        sidePoint = points[i];
                    }
                }
                break;
            case 8:
                if (sidePoint.getY() > points[i].getY()) {
                    sidePoint = points[i];
                } else if (sidePoint.getY() == points[i].getY()) {
                    if (side2 == 1) {
                        if (sidePoint.getX() < points[i].getX()) {
                            sidePoint = points[i];
                        }
                    } else if (sidePoint.getX() > points[i].getX()) {
                        sidePoint = points[i];
                    }
                }
            }
        }

        return sidePoint;
    }

    static int[] getWidth(boolean[][] image, Point[] centers, int[] sincos) throws ArrayIndexOutOfBoundsException {
        int[] width = new int[3];

        for(int i = 0; i < 3; ++i) {
            boolean flag = false;
            int y = centers[i].getY();

            int lx;
            for(lx = centers[i].getX(); lx >= 0; --lx) {
                if (image[lx][y] && !image[lx - 1][y]) {
                    if (flag) {
                        break;
                    }

                    flag = true;
                }
            }

            flag = false;

            int rx;
            for(rx = centers[i].getX(); rx < image.length; ++rx) {
                if (image[rx][y] && !image[rx + 1][y]) {
                    if (flag) {
                        break;
                    }

                    flag = true;
                }
            }

            width[i] = rx - lx + 1;
        }

        return width;
    }

    static int calcRoughVersion(Point[] center, int[] width) {
        int dp = QRCodeImageReader.DECIMAL_POINT;
        int lengthAdditionalLine = (new Line(center[0], center[1])).getLength() << dp;
        int avarageWidth = (width[0] + width[1] << dp) / 14;
        int roughVersion = (lengthAdditionalLine / avarageWidth - 10) / 4;
        if ((lengthAdditionalLine / avarageWidth - 10) % 4 >= 2) {
            ++roughVersion;
        }

        return roughVersion;
    }

    static int calcExactVersion(Point[] centers, int[] angle, int[] moduleSize, boolean[][] image) throws InvalidVersionInfoException, InvalidVersionException {
        boolean[] versionInformation = new boolean[18];
        Point[] points = new Point[18];
        Axis axis = new Axis(angle, moduleSize[1]);
        axis.setOrigin(centers[1]);

        Point target;
        int exactVersion;
        for(exactVersion = 0; exactVersion < 6; ++exactVersion) {
            for(int x = 0; x < 3; ++x) {
                target = axis.translate(x - 7, exactVersion - 3);
                versionInformation[x + exactVersion * 3] = image[target.getX()][target.getY()];
                points[x + exactVersion * 3] = target;
            }
        }

        canvas.drawPoints(points, 267946120);
        boolean var14 = false;

        try {
            exactVersion = checkVersionInfo(versionInformation);
        } catch (InvalidVersionInfoException var13) {
            canvas.println("Version info error. now retry with other place one.");
            axis.setOrigin(centers[2]);
            axis.setModulePitch(moduleSize[2]);

            for(int x = 0; x < 6; ++x) {
                for(int y = 0; y < 3; ++y) {
                    target = axis.translate(x - 3, y - 7);
                    versionInformation[y + x * 3] = image[target.getX()][target.getY()];
                    points[x + y * 3] = target;
                }
            }

            canvas.drawPoints(points, 267946120);

            try {
                exactVersion = checkVersionInfo(versionInformation);
            } catch (VersionInformationException var12) {
                throw var12;
            }
        }

        return exactVersion;
    }

    static int checkVersionInfo(boolean[] target) throws InvalidVersionInfoException {
        int errorCount = 0;

        int versionBase;
        for(versionBase = 0; versionBase < VersionInfoBit.length; ++versionBase) {
            errorCount = 0;

            for(int j = 0; j < 18; ++j) {
                if (target[j] ^ (VersionInfoBit[versionBase] >> j) % 2 == 1) {
                    ++errorCount;
                }
            }

            if (errorCount <= 3) {
                break;
            }
        }

        if (errorCount <= 3) {
            return 7 + versionBase;
        } else {
            throw new InvalidVersionInfoException("Too many errors in version information");
        }
    }
}

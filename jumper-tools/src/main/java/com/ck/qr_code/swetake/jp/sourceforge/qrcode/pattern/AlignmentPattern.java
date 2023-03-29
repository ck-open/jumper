package com.ck.qr_code.swetake.jp.sourceforge.qrcode.pattern;

import com.ck.qr_code.swetake.jp.sourceforge.qrcode.QRCodeDecoder;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.exception.AlignmentPatternNotFoundException;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.exception.InvalidVersionException;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.geom.Axis;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.geom.Line;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.geom.Point;
import com.ck.qr_code.swetake.jp.sourceforge.qrcode.util.DebugCanvas;


public class AlignmentPattern {
    static final int RIGHT = 1;
    static final int BOTTOM = 2;
    static final int LEFT = 3;
    static final int TOP = 4;
    static DebugCanvas canvas = QRCodeDecoder.getCanvas();
    Point[][] center;
    int patternDistance;

    AlignmentPattern(Point[][] center, int patternDistance) {
        this.center = center;
        this.patternDistance = patternDistance;
    }

    public static AlignmentPattern findAlignmentPattern(boolean[][] image, FinderPattern finderPattern) throws AlignmentPatternNotFoundException, InvalidVersionException {
        Point[][] logicalCenters = getLogicalCenter(finderPattern);
        int logicalDistance = logicalCenters[1][0].getX() - logicalCenters[0][0].getX();
        Point[][] centers = (Point[][])null;
        centers = getCenter(image, finderPattern, logicalCenters);
        return new AlignmentPattern(centers, logicalDistance);
    }

    public Point[][] getCenter() {
        return this.center;
    }

    public void setCenter(Point[][] center) {
        this.center = center;
    }

    public int getLogicalDistance() {
        return this.patternDistance;
    }

    static Point[][] getCenter(boolean[][] image, FinderPattern finderPattern, Point[][] logicalCenters) throws AlignmentPatternNotFoundException {
        int moduleSize = finderPattern.getModuleSize();
        Axis axis = new Axis(finderPattern.getAngle(), moduleSize);
        int sqrtCenters = logicalCenters.length;
        Point[][] centers = new Point[sqrtCenters][sqrtCenters];
        axis.setOrigin(finderPattern.getCenter(0));
        centers[0][0] = axis.translate(3, 3);
        canvas.drawCross(centers[0][0], 8947967);
        axis.setOrigin(finderPattern.getCenter(1));
        centers[sqrtCenters - 1][0] = axis.translate(-3, 3);
        canvas.drawCross(centers[sqrtCenters - 1][0], 8947967);
        axis.setOrigin(finderPattern.getCenter(2));
        centers[0][sqrtCenters - 1] = axis.translate(3, -3);
        canvas.drawCross(centers[0][sqrtCenters - 1], 8947967);
        Point tmpPoint = centers[0][0];

        for(int y = 0; y < sqrtCenters; ++y) {
            for(int x = 0; x < sqrtCenters; ++x) {
                if ((x != 0 || y != 0) && (x != 0 || y != sqrtCenters - 1) && (x != sqrtCenters - 1 || y != 0)) {
                    Point target = null;
                    Point precisionCenter;
                    if (y == 0) {
                        if (x > 0 && x < sqrtCenters - 1) {
                            target = axis.translate(centers[x - 1][y], logicalCenters[x][y].getX() - logicalCenters[x - 1][y].getX(), 0);
                            centers[x][y] = new Point(target.getX(), target.getY());
                            canvas.drawCross(centers[x][y], 267946120);
                        }
                    } else if (x == 0) {
                        if (y > 0 && y < sqrtCenters - 1) {
                            target = axis.translate(centers[x][y - 1], 0, logicalCenters[x][y].getY() - logicalCenters[x][y - 1].getY());
                            centers[x][y] = new Point(target.getX(), target.getY());
                            canvas.drawCross(centers[x][y], 267946120);
                        }
                    } else {
                        precisionCenter = axis.translate(centers[x - 1][y], logicalCenters[x][y].getX() - logicalCenters[x - 1][y].getX(), 0);
                        Point t2 = axis.translate(centers[x][y - 1], 0, logicalCenters[x][y].getY() - logicalCenters[x][y - 1].getY());
                        centers[x][y] = new Point((precisionCenter.getX() + t2.getX()) / 2, (precisionCenter.getY() + t2.getY()) / 2 + 1);
                    }

                    if (finderPattern.getVersion() > 1) {
                        precisionCenter = getPrecisionCenter(image, centers[x][y]);
                        canvas.drawCross(centers[x][y], 267946120);
                        int dx = precisionCenter.getX() - centers[x][y].getX();
                        int dy = precisionCenter.getY() - centers[x][y].getY();
                        canvas.println("Adjust AP(" + x + "," + y + ") to d(" + dx + "," + dy + ")");
                        centers[x][y] = precisionCenter;
                    }

                    canvas.drawCross(centers[x][y], 8947967);
                    canvas.drawLine(new Line(tmpPoint, centers[x][y]), 12303359);
                    tmpPoint = centers[x][y];
                }
            }
        }

        return centers;
    }

    static Point getPrecisionCenter(boolean[][] image, Point targetPoint) throws AlignmentPatternNotFoundException {
        int tx = targetPoint.getX();
        int ty = targetPoint.getY();
        if (tx >= 0 && ty >= 0 && tx <= image.length - 1 && ty <= image[0].length - 1) {
            int scope;
            int dy;
            int dx;
            int x;
            int y;
            if (!image[targetPoint.getX()][targetPoint.getY()]) {
                scope = 0;
                boolean found = false;

                while(!found) {
                    ++scope;

                    for(dy = scope; dy > -scope; --dy) {
                        for(dx = scope; dx > -scope; --dx) {
                            x = targetPoint.getX() + dx;
                            y = targetPoint.getY() + dy;
                            if (x < 0 || y < 0 || x > image.length - 1 || y > image[0].length - 1) {
                                throw new AlignmentPatternNotFoundException("Alignment Pattern finder exceeded out of image");
                            }

                            if (image[x][y]) {
                                targetPoint = new Point(targetPoint.getX() + dx, targetPoint.getY() + dy);
                                canvas.drawPoint(targetPoint, 267946120);
                                found = true;
                            }
                        }
                    }
                }
            }

            int lx;
            scope = lx = dy = targetPoint.getX();

            for(dx = x = y = targetPoint.getY(); lx >= 1 && !targetPointOnTheCorner(image, lx, dx, lx - 1, dx); --lx) {
            }

            while(dy < image.length - 1 && !targetPointOnTheCorner(image, dy, dx, dy + 1, dx)) {
                ++dy;
            }

            while(x >= 1 && !targetPointOnTheCorner(image, scope, x, scope, x - 1)) {
                --x;
            }

            while(y < image[0].length - 1 && !targetPointOnTheCorner(image, scope, y, scope, y + 1)) {
                ++y;
            }

            return new Point((lx + dy + 1) / 2, (x + y + 1) / 2);
        } else {
            throw new AlignmentPatternNotFoundException("Alignment Pattern finder exceeded out of image");
        }
    }

    static boolean targetPointOnTheCorner(boolean[][] image, int x, int y, int nx, int ny) {
        if (x >= 0 && y >= 0 && nx >= 0 && ny >= 0 && x <= image.length && y <= image[0].length && nx <= image.length && ny <= image[0].length) {
            return !image[x][y] && image[nx][ny];
        } else {
            throw new AlignmentPatternNotFoundException("Alignment Pattern Finder exceeded image edge");
        }
    }

    public static Point[][] getLogicalCenter(FinderPattern finderPattern) {
        int version = finderPattern.getVersion();
        Point[][] logicalCenters = new Point[1][1];
        int[] logicalSeeds = new int[1];
        logicalSeeds = LogicalSeed.getSeed(version);
        logicalCenters = new Point[logicalSeeds.length][logicalSeeds.length];

        for(int col = 0; col < logicalCenters.length; ++col) {
            for(int row = 0; row < logicalCenters.length; ++row) {
                logicalCenters[row][col] = new Point(logicalSeeds[row], logicalSeeds[col]);
            }
        }

        return logicalCenters;
    }
}

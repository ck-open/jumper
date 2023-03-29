package com.ck.qr_code.swetake.jp.sourceforge.qrcode.geom;

public class SamplingGrid {
    private AreaGrid[][] grid;

    public SamplingGrid(int sqrtNumArea) {
        this.grid = new AreaGrid[sqrtNumArea][sqrtNumArea];
    }

    public void initGrid(int ax, int ay, int width, int height) {
        this.grid[ax][ay] = new AreaGrid(width, height);
    }

    public void setXLine(int ax, int ay, int x, Line line) {
        this.grid[ax][ay].setXLine(x, line);
    }

    public void setYLine(int ax, int ay, int y, Line line) {
        this.grid[ax][ay].setYLine(y, line);
    }

    public Line getXLine(int ax, int ay, int x) throws ArrayIndexOutOfBoundsException {
        return this.grid[ax][ay].getXLine(x);
    }

    public Line getYLine(int ax, int ay, int y) throws ArrayIndexOutOfBoundsException {
        return this.grid[ax][ay].getYLine(y);
    }

    public Line[] getXLines(int ax, int ay) {
        return this.grid[ax][ay].getXLines();
    }

    public Line[] getYLines(int ax, int ay) {
        return this.grid[ax][ay].getYLines();
    }

    public int getWidth() {
        return this.grid[0].length;
    }

    public int getHeight() {
        return this.grid.length;
    }

    public int getWidth(int ax, int ay) {
        return this.grid[ax][ay].getWidth();
    }

    public int getHeight(int ax, int ay) {
        return this.grid[ax][ay].getHeight();
    }

    public int getTotalWidth() {
        int total = 0;

        for(int i = 0; i < this.grid.length; ++i) {
            total += this.grid[i][0].getWidth();
            if (i > 0) {
                --total;
            }
        }

        return total;
    }

    public int getTotalHeight() {
        int total = 0;

        for(int i = 0; i < this.grid[0].length; ++i) {
            total += this.grid[0][i].getHeight();
            if (i > 0) {
                --total;
            }
        }

        return total;
    }

    public int getX(int ax, int x) {
        int total = x;

        for(int i = 0; i < ax; ++i) {
            total += this.grid[i][0].getWidth() - 1;
        }

        return total;
    }

    public int getY(int ay, int y) {
        int total = y;

        for(int i = 0; i < ay; ++i) {
            total += this.grid[0][i].getHeight() - 1;
        }

        return total;
    }

    public void adjust(Point adjust) {
        int dx = adjust.getX();
        int dy = adjust.getY();

        for(int ay = 0; ay < this.grid[0].length; ++ay) {
            for(int ax = 0; ax < this.grid.length; ++ax) {
                int j;
                for(j = 0; j < this.grid[ax][ay].xLine.length; ++j) {
                    this.grid[ax][ay].xLine[j].translate(dx, dy);
                }

                for(j = 0; j < this.grid[ax][ay].yLine.length; ++j) {
                    this.grid[ax][ay].yLine[j].translate(dx, dy);
                }
            }
        }

    }

    private class AreaGrid {
        protected Line[] xLine;
        protected Line[] yLine;

        public AreaGrid(int width, int height) {
            this.xLine = new Line[width];
            this.yLine = new Line[height];
        }

        public int getWidth() {
            return this.xLine.length;
        }

        public int getHeight() {
            return this.yLine.length;
        }

        public Line getXLine(int x) throws ArrayIndexOutOfBoundsException {
            return this.xLine[x];
        }

        public Line getYLine(int y) throws ArrayIndexOutOfBoundsException {
            return this.yLine[y];
        }

        public Line[] getXLines() {
            return this.xLine;
        }

        public Line[] getYLines() {
            return this.yLine;
        }

        public void setXLine(int x, Line line) {
            this.xLine[x] = line;
        }

        public void setYLine(int y, Line line) {
            this.yLine[y] = line;
        }
    }
}

package org.wowtools.fasttiff.core;

import java.awt.image.BufferedImage;

/**
 * 用rgb数组描述的切片
 *
 * @author liuyu
 * @date 2018/2/27
 */
public class RgbTile {
    /**
     * 图片宽度 像素
     */
    private int tileWidth;
    /**
     * 图片高度 像素
     */
    private int tileHeight;
    /**
     * the starting X coordinate
     */
    private int startX;
    /**
     * the starting Y coordinate
     */
    private int startY;
    /**
     * width of the region
     */
    private int w;
    /**
     * height of the region
     */
    private int h;
    /**
     * the rgb pixels
     */
    private int[] rgbArray;
    /**
     * offset into the <code>rgbArray</code>
     */
    private int offset;
    /**
     * scanline stride for the <code>rgbArray</code>
     */
    private int scansize;

    public int getTileWidth() {
        return tileWidth;
    }

    public void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int[] getRgbArray() {
        return rgbArray;
    }

    public void setRgbArray(int[] rgbArray) {
        this.rgbArray = rgbArray;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getScansize() {
        return scansize;
    }

    public void setScansize(int scansize) {
        this.scansize = scansize;
    }

    /**
     * 填充此切片的图片中
     *
     * @param img
     */
    public void fillImg(BufferedImage img) {
        img.setRGB(startX, startY, w, h, rgbArray, offset, scansize);
    }
}

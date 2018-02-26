package org.wowtools.fasttiff.core;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 可切片的tiff
 *
 * @author liuyu
 * @date 2018/2/23
 */
public class TileableTiff {
    static {
        gdal.AllRegister();
    }

    public static void main(String[] args) {
        TileableTiff tf = new TileableTiff("e:/_tmp/500kV大宝II回线179-192-3793-4109.tif");
        /** 1、将层级行列号转为与tiff相同坐标系的bbox **/
        double[] bbox3857 = Lrc2Bbox.toBbox3857(16,14087*2,25595*2);
        //TODO dataset.GetProjection()获取tiff坐标系，并转为3857,这里直接默认了tiff坐标系为4326
        double[] lu = tf.mercator2lonLat(bbox3857[0], bbox3857[1]);//左上角
        double[] rd = tf.mercator2lonLat(bbox3857[2], bbox3857[3]);//右下角
        double xmin = lu[0], ymin = rd[1], xmax = rd[0], ymax = lu[1];

        /** 2、计算getTileRgb方法所需参数 **/
        //TODO 没有考虑geoTransform的旋转参数
        double x0 = tf.geoTransform[0], dx = tf.geoTransform[1], y0 = tf.geoTransform[3], dy = tf.geoTransform[5];
        int startX = (int) ((xmin - x0) / dx);
        int startY = (int) ((ymax - y0) / dy);
        int endX = (int) ((xmax - x0) / dx + 0.5);
        int endY = (int) ((ymin - y0) / dy + 0.5);
        int tiffWidth = endX - startX;
        int tiffHeight = startY - endY;
        System.out.println();
    }

    private final String tiffPath;
    private final int hashCode;

    private final Dataset dataset;

    private final Band bandA;
    private final Band bandR;
    private final Band bandG;
    private final Band bandB;

    private final double[] geoTransform;//获取tiff左上角坐标、每个像素宽度等

    private final int iXSize;//tiff宽(像素)
    private final int iYSize;//tiff高(像素)

    /**
     * @param tiffPath tiff文件绝对路径,如 e:/test.t.ff
     */
    public TileableTiff(String tiffPath) {
        dataset = gdal.Open(tiffPath, gdalconstConstants.GA_ReadOnly);
        if (dataset == null) {
            throw new RuntimeException("读取tiff文件异常:" + gdal.GetLastErrorMsg());
        }
        this.tiffPath = tiffPath;
        hashCode = tiffPath.hashCode();
        Band[] argb = getArgbBands(dataset);
        bandA = argb[0];
        bandR = argb[1];
        bandG = argb[2];
        bandB = argb[3];

        geoTransform = dataset.GetGeoTransform();

        iXSize = dataset.getRasterXSize();
        iYSize = dataset.getRasterYSize();
    }

    /**
     * 按照google标准的层级按规范，从tiff文件上复制出一块切片，并生成一个BufferedImage对象
     *
     * @param level      层级
     * @param row        行号
     * @param column     列号
     * @param tileWidth  生成的BufferedImage的宽度
     * @param tileHeight 生成的BufferedImage的高度
     * @return
     */
    public BufferedImage getTile(int level, int row, int column, int tileWidth, int tileHeight) {
        /** 1、将层级行列号转为与tiff相同坐标系的bbox **/
        double[] bbox3857 = Lrc2Bbox.toBbox3857(level, row, column);
        //TODO dataset.GetProjection()获取tiff坐标系，并转为3857,这里直接默认了tiff坐标系为4326
        double[] low = mercator2lonLat(bbox3857[0], bbox3857[1]);//左下角
        double[] up = mercator2lonLat(bbox3857[2], bbox3857[3]);//右上角
        double xmin = low[0], ymin = low[1], xmax = up[0], ymax = up[1];

        /** 2、计算getTileRgb方法所需参数 **/
        //TODO 没有考虑geoTransform的旋转参数
        double x0 = geoTransform[0], dx = geoTransform[1], y0 = geoTransform[3], dy = geoTransform[5];
        int startX = (int) ((xmin - x0) / dx);
        int startY = (int) ((ymax - y0) / dy);
        int endX = (int) ((xmax - x0) / dx + 0.5);
        int endY = (int) ((ymin - y0) / dy + 0.5);
        int tiffWidth = endX - startX;
        int tiffHeight = endY -startY;
        /** 3、得到rgbArr并转换为img**/
        BufferedImage img = new BufferedImage(tileWidth, tileHeight, Transparency.TRANSLUCENT);
        if (startX > iXSize || endY > iYSize || endX < 0 || startY < 0) {
            return img;//bbox完全落在图片外，直接返回一张透明图片
        }
        int drawStartX = 0, drawStartY = 0;
//        if (startX < 0) {
//            startX = 0;
//            double w0 = tiffWidth;
//            tiffWidth = endX;
//            tileWidth = (int) (tiffWidth / w0 * tileWidth);
//        } else if (endX > iXSize) {
//            endX = iXSize - 1;
//            double w0 = tiffWidth;
//            tiffWidth = endX - startX;
//            tileWidth = (int) (tiffWidth / w0 * tileWidth);
//            drawStartX = (int) (w0 - tileWidth);
//        }
//        if (startY < 0) {
//            startY = 0;
//            double h0 = tiffHeight;
//            tiffHeight = endY;
//            tileHeight = (int) (tiffHeight / h0 * tileHeight);
//        } else if (endY > iYSize) {
//
//        }
        //TODO bbox不完全落在tiff范围内的处理
        int[] rgbArr = getTileRgb(startX, startY, tiffWidth, tiffHeight, tileWidth, tileHeight);
        img.setRGB(drawStartX, 0, tileWidth, tileHeight, rgbArr, 0, tileWidth);
        return img;
    }

    private double[] mercator2lonLat(double mercatorX, double mercatorY) {
        double[] xy = new double[2];
        double x = mercatorX / 20037508.34 * 180;
        double y = mercatorY / 20037508.34 * 180;
        y = 180 / Math.PI * (2 * Math.atan(Math.exp(y * Math.PI / 180)) - Math.PI / 2);
        xy[0] = x;
        xy[1] = y;
        return xy;
    }

    /**
     * 从tiff文件上复制出一块切片，并生成一个BufferedImage对象的rgb数组
     *
     * @param startX     在tiff上取切片的左上角x坐标
     * @param startY     在tiff上取切片的左上角y坐标
     * @param tiffWidth  在tiff上取切片的宽度(像素)
     * @param tiffHeight 在tiff上取切片的高度(像素)
     * @param tileWidth  生成的BufferedImage的宽度
     * @param tileHeight 生成的BufferedImage的高度
     * @return
     */
    private int[] getTileRgb(int startX, int startY, int tiffWidth, int tiffHeight, int tileWidth, int tileHeight) {
        int size = tileWidth * tileHeight;
        int[] rgbArr = new int[size];
        //取出rgba分量，再合并成rgb int放入数组
        int[] rasterA = new int[size];
        int[] rasterR = new int[size];
        int[] rasterG = new int[size];
        int[] rasterB = new int[size];
        synchronized (this){
            //异步操作会引起jni error
            bandA.ReadRaster(startX, startY, tiffWidth, tiffHeight, tileWidth, tileHeight, gdalconstConstants.GDT_Int32, rasterA);
            bandR.ReadRaster(startX, startY, tiffWidth, tiffHeight, tileWidth, tileHeight, gdalconstConstants.GDT_Int32, rasterR);
            bandG.ReadRaster(startX, startY, tiffWidth, tiffHeight, tileWidth, tileHeight, gdalconstConstants.GDT_Int32, rasterG);
            bandB.ReadRaster(startX, startY, tiffWidth, tiffHeight, tileWidth, tileHeight, gdalconstConstants.GDT_Int32, rasterB);
        }

        for (int i = 0; i < size; i++) {
            int v = (rasterR[i] << 16) + (rasterG[i] << 8) + rasterB[i];
            //去除黑边
            if (0 == v) {
                rgbArr[i] = 0xffffff;
            } else {
                rgbArr[i] = (rasterA[i] << 24) + v;
            }
        }

        return rgbArr;
    }

    /**
     * 获取tiff的rgba band，默认认为tiff的四个band依次为r,g,b,a，若tiff格式不同，请覆写此方法
     *
     * @param hDataset
     * @return
     */
    protected Band[] getArgbBands(Dataset hDataset) {
        return new Band[]{
                hDataset.GetRasterBand(4),
                hDataset.GetRasterBand(1),
                hDataset.GetRasterBand(2),
                hDataset.GetRasterBand(3)
        };
    }

    public double[] getGeoTransform() {
        return geoTransform;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TileableTiff) {
            return tiffPath.equals(((TileableTiff) obj).tiffPath);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    protected void finalize() throws Throwable {
        bandA.delete();
        bandR.delete();
        bandG.delete();
        bandB.delete();
        dataset.delete();
        super.finalize();
    }
}

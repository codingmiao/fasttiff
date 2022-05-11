package org.wowtools.fasttiff.core;

/**
 * 将层级行列转换为3857坐标系下的bbox
 *
 * @author liuyu
 * @date 2018/2/23
 */
public class Lrc2Bbox {

    private static final int[] pow = new int[30];

    static {
        int n = 1;
        for (int i = 0; i < pow.length; i++) {
            pow[i] = n;
            n = n * 2;
        }
    }

    private static final double E_r = 20037508.342787001D;

    /**
     * 将层级行列转换为3857坐标系下的bbox
     *
     * @param level
     * @param row
     * @param column
     * @return
     */
    public static double[] toBbox3857(int level, int row, int column) {
        int m = pow[level];
        return new double[]{-E_r + E_r * 2.0D / m * column, E_r - E_r * 2.0D / m * (row + 1),
                -E_r + E_r * 2.0D / m * (column + 1), E_r - E_r * 2.0D / m * row};
    }

    /**
     * 将3857坐标系下的x、y转为行列号
     *
     * @param level
     * @param x
     * @param y
     * @return
     */
    public static int[] xy2Lrc(int level, double x, double y) {
        int m = pow[level];
        int column1 = (int) ((E_r + x) / (E_r * 2.0D / m));
        int row1 = (int) ((E_r - y) / (E_r * 2.0D / m));
        return new int[]{row1, column1};
    }


}

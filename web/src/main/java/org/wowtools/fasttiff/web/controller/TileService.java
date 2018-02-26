package org.wowtools.fasttiff.web.controller;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.wowtools.common.utils.AsyncTaskUtil;
import org.wowtools.fasttiff.core.TileableTiff;
import org.wowtools.fasttiff.web.service.TileConfig;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 切片服务，负责管理TileableTiff,并提供对应切片
 *
 * @author liuyu
 * @date 2018/2/26
 */
@Service
@Order(value=2)
public class TileService implements CommandLineRunner {
    private final AtomicInteger _idx = new AtomicInteger(0);//用于均衡地选取TileableTiff
    private final int tileWidth = 256;

    private TileableTiff[][] allTiff;

    /**
     * 街区所有tiff，合并为一个切片图片
     *
     * @param level
     * @param row
     * @param col
     * @return
     */
    public BufferedImage getTile(int level, int row, int col) {
        int idx = Math.abs(_idx.addAndGet(1));
        BufferedImage res = new BufferedImage(tileWidth, tileWidth, Transparency.TRANSLUCENT);
        Graphics g = res.getGraphics();
        List<Runnable> tasks = new ArrayList<>(allTiff.length);
        for (TileableTiff[] tileableTiffs : allTiff) {
            TileableTiff tileableTiff = tileableTiffs[idx % tileableTiffs.length];
            tasks.add(() -> {
                BufferedImage subImg = tileableTiff.getTile(level, row, col, tileWidth, tileWidth);
                if (null != g) {
                    synchronized (g) {
                        g.drawImage(subImg, 0, 0, null);
                    }
                }
            });
        }
        AsyncTaskUtil.executeAsyncTasks(tasks, true);
        g.dispose();
        return res;
    }

    @Override
    public void run(String... args) throws Exception {
        String tiffRoot = TileConfig.install.getTiffRoot();
        int coreSize = TileConfig.install.getCoreSize();
        File root = new File(tiffRoot);
        Stack<File> stack = new Stack<>();
        stack.push(root);
        LinkedList<TileableTiff[]> list = new LinkedList<>();
        while (!stack.empty()) {
            File f = stack.pop();
            if (f.isDirectory()) {
                for (File sub : f.listFiles()) {
                    stack.push(sub);
                }
            } else {
                String fileName = f.getName();
                fileName = fileName.substring(fileName.lastIndexOf(".") + 1);
                if ("tif".equals(fileName) || "tiff".equals(fileName)) {
                    String path = f.getPath();
                    TileableTiff[] tileableTiffs = new TileableTiff[coreSize];
                    for (int i = 0; i < coreSize; i++) {
                        tileableTiffs[i] = new TileableTiff(path);
                    }
                    list.add(tileableTiffs);
                }
            }
        }
        allTiff = new TileableTiff[list.size()][];
        list.toArray(allTiff);
    }
}

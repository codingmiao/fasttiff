package org.wowtools.fasttiff.web.controller;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wowtools.fasttiff.core.TileableTiff;
import org.wowtools.fasttiff.web.service.MapServerMeta;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 切片服务Controller
 *
 * @author liuyu
 * @date 2018/2/24
 */
@RestController()
@RequestMapping("/tiled")
public class TileServiceController {
    private final TileableTiff tileableTiff = new TileableTiff("e:/_tmp/500kV大宝II回线179-192-3793-4109.tif");


    @RequestMapping({"/{layer}"})
    public String getilemetainfo(@PathVariable("layer") String layer, @RequestParam("f") String form,
                                 HttpServletResponse response) {
        MapServerMeta meta = new MapServerMeta(layer);
        return meta.getMapServerMetainfo().toString();
    }


    @RequestMapping({"/{layer}/tile/{level}/{row}/{col}"})
    public void getile(@PathVariable("layer") String layer, @PathVariable("level") int level, @PathVariable("row") int row, @PathVariable("col") int col, HttpServletResponse response) {
        BufferedImage img = tileableTiff.getTile(level, row, col, 256, 256);
        response.setContentType("image/png");
        OutputStream os = null;
        try {
            os = response.getOutputStream();
            ImageIO.write(img,"png",os);
            os.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally{
            if(null!=os){
                try {
                    os.close();
                } catch (IOException e) {
                }
            }

        }
    }

    @RequestMapping({"/{layer}/tilemap/{level}/{row}/{col}/{width}/{height}"})
    public String getileN(@PathVariable("layer") String layer, @PathVariable("level") int level, @PathVariable("row") int row,
                          @PathVariable("col") int col, @PathVariable("width") int w, @PathVariable("height") int h,
                          HttpServletResponse response) {
        JSONObject jo = new JSONObject();
        jo.put("valid", true);
        JSONObject joLocation = new JSONObject();
        joLocation.put("left", col);
        joLocation.put("top", row);
        joLocation.put("width", w);
        joLocation.put("height", h);
        int n = w * h;
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = 1;
        }
        jo.put("location", joLocation);
        jo.put("data", arr);
        return jo.toString();
    }
}

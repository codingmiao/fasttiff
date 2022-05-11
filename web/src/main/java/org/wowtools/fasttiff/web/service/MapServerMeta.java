package org.wowtools.fasttiff.web.service;

import org.json.JSONArray;
import org.json.JSONObject;

public class MapServerMeta {
    private static class LayerExtent {
        int minLevel;
        int maxLevel;
        double[] minxy;
        double[] maxxy;
    }


    private String layername;

    private int dpi = 96;

    private double[] originlonlat;
    private JSONArray lyrs = new JSONArray();

    private double minScale = 0;
    private double maxScale = 0;
    private int minLevel;
    private int maxLevel;

    double[] minxy;
    double[] maxxy;

    private String layerName;

    public MapServerMeta(String layerName) {
        this.originlonlat = new double[]{-2.0037508342787E7, 2.0037508342787E7};
        this.layerName = layerName;
        minxy = new double[]{10848780.278959094, 2364183.440747929};
        maxxy = new double[]{11841311.259604478, 3462945.6138322023};
        minLevel = 0;
        maxLevel = 29;
    }

    public JSONObject getMapServerMetainfo() {

        JSONObject data = new JSONObject();

        data.put("currentVersion", "10.3");
        data.put("serviceDescription", layerName);
        data.put("mapName", layerName);
        data.put("description", "xxxxxxxxx");
        data.put("copyrightText", "xxxxxxxx");
        data.put("supportsDynamicLayers", false);

        JSONObject spatialReference = new JSONObject();
        spatialReference.put("wkid", 102100);
        spatialReference.put("latestWkid", 3857);
        data.put("spatialReference", spatialReference);

        data.put("singleFusedMapCache", true);

        data.put("layers", getlayers());

        data.put("tileInfo", gettileinfo());

        data.put("initialExtent", getinitialExtent());

        data.put("fullExtent", getfullExtent());

        data.put("minScale", getminScale());

        data.put("maxScale", getmaxScale());

        data.put("units", getunits());

        data.put("supportedImageFormatTypes", supportedImageFormatTypes());

        data.put("documentInfo", getdocumentInfo());

        data.put("capabilities", "Map,Tilemap");

        data.put("supportedQueryFormats", "JSON");

        data.put("exportTilesAllowed", false);

        data.put("maxRecordCount", 1000);
        data.put("maxImageHeight", 4096);
        data.put("maxImageWidth", 4096);

        return data;

    }

    private JSONObject gettileinfo() {
        JSONObject tileInfo = new JSONObject();

        tileInfo.put("rows", 256);
        tileInfo.put("cols", 256);
        tileInfo.put("dpi", this.dpi);
        tileInfo.put("format", "JPEG");
        tileInfo.put("compressionQuality", 90);

        JSONObject origin = new JSONObject();
        origin.put("x", this.originlonlat[0]);
        origin.put("y", this.originlonlat[1]);
        tileInfo.put("origin", origin);

        JSONObject spatialReference2 = new JSONObject();
        spatialReference2.put("wkid", 102100);
        spatialReference2.put("latestWkid", 3857);
        tileInfo.put("spatialReference", spatialReference2);

        tileInfo.put("lods", getlods());

        return tileInfo;
    }

    private JSONArray getlods() {
        double resolution = 156543.03392800014;
        double scale = 5.91657527591555E8;

        for (int i1 = minLevel; i1 <= maxLevel; i1++) {
            JSONObject lod = new JSONObject();
            lod.put("level", i1);
            lod.put("resolution", resolution / (Math.pow(2, i1)));
            lod.put("scale", scale / (Math.pow(2, i1)));
            lyrs.put(lod);
            double Scale = scale / (Math.pow(2, i1));
            minScale = minScale > 0 ? Math.max(minScale, Scale) : Scale;
            maxScale = maxScale > 0 ? Math.min(maxScale, Scale) : Scale;
        }
        return lyrs;
    }

    private JSONArray getlayers() {
        JSONArray lyrs = new JSONArray();

        JSONObject layerinfo = new JSONObject();
        layerinfo.put("id", 0);
        layerinfo.put("name", this.layername);
        layerinfo.put("parentLayerId", -1);
        layerinfo.put("defaultVisibility", true);
        layerinfo.put("subLayerIds", JSONObject.NULL);
        layerinfo.put("minScale", 0);
        layerinfo.put("maxScale", 0);
        lyrs.put(0, layerinfo);
        return lyrs;

    }

    private JSONObject getinitialExtent() {
        JSONObject data = new JSONObject();
        data.put("xmin", this.minxy[0]);
        data.put("ymin", this.minxy[1]);
        data.put("xmax", this.maxxy[0]);
        data.put("ymax", this.maxxy[1]);
        JSONObject spatialReference = new JSONObject();
        spatialReference.put("wkid", 102100);
        spatialReference.put("latestWkid", 3857);
        data.put("spatialReference", spatialReference);

        return data;

    }

    private JSONObject getfullExtent() {

        JSONObject data = new JSONObject();
        data.put("xmin", this.minxy[0]);
        data.put("ymin", this.minxy[1]);
        data.put("xmax", this.maxxy[0]);
        data.put("ymax", this.maxxy[1]);

        JSONObject spatialReference = new JSONObject();
        spatialReference.put("wkid", 102100);
        spatialReference.put("latestWkid", 3857);
        data.put("spatialReference", spatialReference);
        return data;

    }

    private double getminScale() {
        return minScale;
    }

    private double getmaxScale() {
        return maxScale;
    }

    private String getunits() {
        return "esriMeters";
    }

    private String supportedImageFormatTypes() {
        return "PNG";
    }

    private JSONObject getdocumentInfo() {
        JSONObject data = new JSONObject();
        data.put("Author", "xxxxxx");
        data.put("Comments", "");
        data.put("Subject", "");
        data.put("Category", "");
        data.put("AntialiasingMode", "None");
        data.put("TextAntialiasingMode", "Force");

        return data;
    }

}

package org.wowtools.fasttiff.web.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author liuyu
 * @date 2018/2/26
 */
@Component
@PropertySource(value = "classpath:/tile.properties",encoding="utf-8")
@Order(value=1)
public class TileConfig implements CommandLineRunner {
    public static TileConfig install;
    @Value("${tiffRoot}")
    private String tiffRoot;
    @Value("${coreSize}")
    private int coreSize;

    public String getTiffRoot() {
        return tiffRoot;
    }

    public int getCoreSize() {
        return coreSize;
    }

    @Override
    public void run(String... args) throws Exception {
        install = this;
    }
}

package org.n3r.diamond.client.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.base.Optional;
import com.google.common.primitives.UnsignedLongs;
import org.apache.commons.io.FileUtils;
import org.n3r.diamond.client.DiamondStone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import static java.io.File.separator;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.n3r.diamond.client.impl.Constants.*;

public class SnapshotMiner {
    private Logger log = LoggerFactory.getLogger(SnapshotMiner.class);
    private final String dir;

    public SnapshotMiner(DiamondManagerConf managerConfig) {
        dir = managerConfig.getFilePath() + separator + SNAPSHOT_DIR;
        File file = new File(dir);
        file.mkdirs();
    }

    public String getSnapshot(DiamondStone.DiamondAxis diamondAxis) throws IOException {
        return getFileContent(diamondAxis, DIAMOND_STONE_EXT);
    }

    private String getFileContent(DiamondStone.DiamondAxis diamondAxis, String extension) throws IOException {
        File file = new File(dir + separator + diamondAxis.getGroup()
                + separator + diamondAxis.getDataId() + extension);
        if (!file.exists()) return null;

        return FileUtils.readFileToString(file, ENCODING);
    }

    public void saveSnaptshot(DiamondStone.DiamondAxis diamondAxis, String content) {
        if (content == null) return;

        try {
            File file = getOrCreateDiamondFile(diamondAxis, DIAMOND_STONE_EXT);
            FileUtils.writeStringToFile(file, defaultIfEmpty(content, ""), ENCODING);
        } catch (IOException e) {
            log.error("save snapshot error {} by {}", diamondAxis, content, e);
        }
    }

    public void removeSnapshot(DiamondStone.DiamondAxis diamondAxis) {
        removeSnapshot(diamondAxis, DIAMOND_STONE_EXT);
    }

    private void removeSnapshot(DiamondStone.DiamondAxis diamondAxis, String extension) {
        String path = dir + separator + diamondAxis.getGroup();
        File dir = new File(path);
        if (!dir.exists()) return;

        File file = new File(path + separator + diamondAxis.getDataId() + extension);
        if (!file.exists()) return;

        file.delete();

        if (dir.list().length == 0) dir.delete();
    }

    private void removeAllSnapshot(DiamondStone.DiamondAxis diamondAxis, final String extension) {
        String path = dir + separator + diamondAxis.getGroup();
        File dir = new File(path);
        if (!dir.exists()) return;

        final String prefix = diamondAxis.getDataId() + extension;
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(prefix);
            }
        });

        for (File file : files)
            file.delete();

        if (dir.list().length == 0) dir.delete();
    }

    private File getOrCreateDiamondFile(DiamondStone.DiamondAxis diamondAxis, String extension) throws IOException {
        String path = dir + separator + diamondAxis.getGroup();
        File dir = new File(path);
        if (!dir.exists()) dir.mkdir();

        File file = new File(path + separator + diamondAxis.getDataId() + extension);
        if (!file.exists()) file.createNewFile();

        return file;
    }

    public void saveCache(DiamondStone.DiamondAxis diamondAxis, Object diamondCache, int dynamicsHasCode) {
        String json = JSON.toJSONString(diamondCache, SerializerFeature.WriteClassName);
        try {
            File file = getOrCreateDiamondFile(diamondAxis, getDynamicCacheExtension(dynamicsHasCode));
            FileUtils.writeStringToFile(file, json, ENCODING);
        } catch (IOException e) {
            log.error("save {} cache snaptshot error", diamondAxis, e);
        }
    }


    public Optional<Object> getCache(DiamondStone.DiamondAxis diamondAxis, int dynamicsHasCode) {
        try {
            String fileContent = getFileContent(diamondAxis, getDynamicCacheExtension(dynamicsHasCode));
            if (fileContent == null) return Optional.absent();

            return Optional.fromNullable(JSON.parse(fileContent));
        } catch (IOException e) {
            log.error("read cache snapshot {} failed {}", e.getMessage());
        }

        return null;
    }

    public void removeCache(DiamondStone.DiamondAxis diamondAxis, int dynamicsHasCode) {
        removeSnapshot(diamondAxis, getDynamicCacheExtension(dynamicsHasCode));
    }

    public void removeAllCache(DiamondStone.DiamondAxis diamondAxis) {
        removeAllSnapshot(diamondAxis, DIAMOND_CACHE_EXT);
    }


    private String getDynamicCacheExtension(int dynamicsHasCode) {
        return DIAMOND_CACHE_EXT + UnsignedLongs.toString(dynamicsHasCode);
    }
}

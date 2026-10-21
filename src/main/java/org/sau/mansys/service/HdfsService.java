package org.sau.mansys.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class HdfsService {

    @Value("${hdfs.uri}")
    private String hdfsUri;

    @Value("${hdfs.images}")
    private String imagesDir;

    private FileSystem fs() throws IOException, InterruptedException {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", hdfsUri);
        conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
        conf.setBoolean("fs.hdfs.impl.disable.cache", true);
        return FileSystem.get(URI.create(hdfsUri), conf, "hadoop");
    }

    /** Read one image out of HDFS and return its bytes. */
    public byte[] read(String fileName) {
        Path path = new Path(imagesDir + "/" + fileName);
        try (FileSystem fs = fs(); FSDataInputStream in = fs.open(path)) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("HDFS read failed for " + path, e);
        }
    }

    /** Write (or overwrite) one image into HDFS. */
    public void write(String fileName, byte[] content) {
        Path path = new Path(imagesDir + "/" + fileName);
        try (FileSystem fs = fs(); FSDataOutputStream out = fs.create(path, true)) {
            out.write(content);
        } catch (Exception e) {
            throw new RuntimeException("HDFS write failed for " + path, e);
        }
    }

    /** List what is currently stored in the images directory. */
    public List<String> list() {
        try (FileSystem fs = fs()) {
            List<String> names = new ArrayList<>();
            for (FileStatus st : fs.listStatus(new Path(imagesDir))) {
                names.add(st.getPath().getName() + " (" + st.getLen() + " bytes)");
            }
            return names;
        } catch (Exception e) {
            throw new RuntimeException("HDFS list failed for " + imagesDir, e);
        }
    }

    public boolean exists(String fileName) {
        try (FileSystem fs = fs()) {
            return fs.exists(new Path(imagesDir + "/" + fileName));
        } catch (Exception e) {
            return false;
        }
    }
}

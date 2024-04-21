package com.programming.streaming.service;

import com.programming.streaming.entity.Video;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.sql.Timestamp;

@Service
public class VideoService {

    @Autowired
    private GridFsTemplate template;

    @Autowired
    private GridFsOperations operations;

    public String addVideo(MultipartFile upload, String userID, byte[] thumbnail, Timestamp timestamp)
            throws IOException {
        DBObject metadata = new BasicDBObject();
        metadata.put("fileSize", upload.getSize());
        metadata.put("userID", userID);
        metadata.put("videoId", new ObjectId().toString());
        metadata.put("timestamp", timestamp.toString());
        metadata.put("likes", 0);
        metadata.put("dislikes", 0);
        metadata.put("views", 0);
        Object fileID = template.store(upload.getInputStream(), upload.getOriginalFilename(), upload.getContentType(),
                metadata);

        template.store(new ByteArrayInputStream(thumbnail), upload.getOriginalFilename() + "_thumbnail", "image/png",
                metadata);
        return fileID.toString();
    }

    public List<String> getAllVideoIDs() {
        Query query = new Query(); // Tạo một truy vấn mới
        return template.find(query)
                .map(GridFSFile::getObjectId)
                .map(ObjectId::toString)
                .into(new ArrayList<>());
    }

    public Video getVideo(String id) throws IOException {

        // search file
        GridFSFile gridFSFile = template.findOne(new Query(Criteria.where("_id").is(id)));

        // convert uri to byteArray
        // save data to LoadFile class
        Video loadFile = new Video();

        if (gridFSFile != null && gridFSFile.getMetadata() != null) {
            loadFile.setFilename(gridFSFile.getFilename());

            loadFile.setFileType(gridFSFile.getMetadata().get("_contentType").toString());

            loadFile.setFileSize(gridFSFile.getMetadata().get("fileSize").toString());
            loadFile.setFile(IOUtils.toByteArray(operations.getResource(gridFSFile).getInputStream()));
        }

        return loadFile;
    }
    

    
}
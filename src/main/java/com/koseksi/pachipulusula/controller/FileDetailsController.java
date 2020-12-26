package com.koseksi.pachipulusula.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.koseksi.app.modals.FileDetails;
import com.koseksi.app.models.FileUploadResponce;
import com.koseksi.app.repository.FileRepository;
import com.koseksi.app.service.DBFileStorageService;

@RestController
@RequestMapping("/files/db/")
public class FileDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(FileDetailsController.class);

    @Autowired
    private DBFileStorageService dbFileStorageService;
    
    @Autowired
    private FileRepository fileRepository;

    @PostMapping("/uploadFile")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,@RequestParam("userId") int userId) {
        FileDetails fileDetails;
		try {
			fileDetails = dbFileStorageService.storeFile(file,userId);
			String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
					.path("/files/db/downloadFile/")
					.path(fileDetails.getFileId())
					.toUriString();
			
			return new ResponseEntity<>(new FileUploadResponce(fileDetails.getFileName(), fileDownloadUri, fileDetails.getFileId(), fileDetails.getFileType(), fileDetails.getData(),fileDetails.getUserId()),HttpStatus.OK);
		} catch (Exception e) {
			logger.info(e.getLocalizedMessage());
			logger.error(e.getMessage());
			return new ResponseEntity<>(e.getLocalizedMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}

    }

    @PostMapping("/uploadMultipleFiles")
    public List<?> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files,@RequestParam("userId") int userId) {
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file,userId))
                .collect(Collectors.toList());
    }

    @GetMapping("/downloadFile/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
    	FileDetails fileDetails;
		try {
			fileDetails = dbFileStorageService.getFile(fileId);
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(fileDetails.getFileType()))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDetails.getFileName() + "\"")
					.body(new ByteArrayResource(fileDetails.getData()));
		} catch (Exception e) {
			logger.info(e.getLocalizedMessage());
			logger.error(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

    }
    
    @GetMapping(path="/all/files", produces="application/json")
    public List<FileDetails> getAllFiles() {
    	return fileRepository.findAll(); 	
	}
    
    @GetMapping(path ="/file/{fileId}", produces="application/json")
    public ResponseEntity<?> getFile(@PathVariable String fileId) {
    	FileDetails fileDetails;
		try {
			fileDetails = dbFileStorageService.getFile(fileId);
			return new ResponseEntity<>(fileDetails,HttpStatus.OK);
		} catch (Exception e) {
			logger.info(e.getLocalizedMessage());
			logger.error(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }
    
    @DeleteMapping(path ="/file/{fileId}", produces="application/json")
    public ResponseEntity<?> deleteFile(@PathVariable String fileId) {
    	FileDetails fileDetails;
		try {
			fileRepository.deleteById(fileId);
			return new ResponseEntity<>("File Deleted Successfully with id::"+fileId,HttpStatus.OK);
		} catch (Exception e) {
			logger.info(e.getLocalizedMessage());
			logger.error(e.getMessage());
			return new ResponseEntity<>("File Deleted Successfully with id::"+fileId,HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }
    

}
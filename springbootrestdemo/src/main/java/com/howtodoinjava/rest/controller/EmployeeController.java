package com.howtodoinjava.rest.controller;

import com.howtodoinjava.rest.dao.EmployeeDAO;
import com.howtodoinjava.rest.model.Employee;
import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(path = "/")
public class EmployeeController 
{
    @Autowired
    private EmployeeDAO employeeDao;
    
    @GetMapping(path="/", produces = "application/json")
    public String getEmployees(@RequestParam("data") List<String> nameList) throws IOException
    {

        System.out.println("List "+nameList);
//./src/main/resources/

        Path fileStorageLocation = Paths.get("./src/main/resources/").toAbsolutePath().normalize();
        Path uploadExcelLogFilePath = fileStorageLocation.resolve("data.csv");

        if (!uploadExcelLogFilePath.toFile().exists() && !uploadExcelLogFilePath.toFile().isDirectory()) {
            Files.createFile(uploadExcelLogFilePath);

            String header = "first,second,third";
            String comma = ",";

            try (CSVWriter writer = new CSVWriter(new FileWriter(uploadExcelLogFilePath.toFile(), true))) {
                writer.writeNext(header.split(comma));
                writer.writeNext(nameList.toArray(new String[0]));

            }

        } else {
            try (CSVWriter writer = new CSVWriter(new FileWriter(uploadExcelLogFilePath.toFile(), true))) {
                writer.writeNext(nameList.toArray(new String[0]));
            }
        }






        //return employeeDao.getAllEmployees();
        return "Hey Nirav :) ..! Your request has been successfully received..! Your data has been uploaded..!";
    }



    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) throws IOException {
        // Load file as Resource
        //Resource resource = fileStorageService.loadFileAsResource(fileName);


        Path fileStorageLocation = Paths.get("./src/main/resources/").toAbsolutePath().normalize();

        Path filePath = fileStorageLocation.resolve(fileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());


        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
          //  logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }
    
    @PostMapping(path= "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> addEmployee(
                        @RequestHeader(name = "X-COM-PERSIST", required = true) String headerPersist,
                        @RequestHeader(name = "X-COM-LOCATION", required = false, defaultValue = "ASIA") String headerLocation,
                        @RequestBody Employee employee) 
                 throws Exception 
    {       
        //Generate resource id
        Integer id = employeeDao.getAllEmployees().getEmployeeList().size() + 1;
        employee.setId(id);
        
        //add resource
        employeeDao.addEmployee(employee);
        
        //Create resource location
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                                    .path("/{id}")
                                    .buildAndExpand(employee.getId())
                                    .toUri();
        
        //Send location in response
        return ResponseEntity.created(location).build();
    }
}

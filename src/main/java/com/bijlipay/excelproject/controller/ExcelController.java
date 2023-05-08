package com.bijlipay.excelproject.controller;

import com.bijlipay.excelproject.service.ExcelService;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.util.Pair;

import java.io.IOException;
import java.util.ArrayList;



@RestController
public class ExcelController {

    private static final Logger log = LoggerFactory.getLogger(ExcelController.class);

    @Autowired
    private ExcelService excelservice;

    @PostMapping("/readexcel")
    public ResponseEntity<?> readExcel(@RequestParam("file") MultipartFile file) throws IOException, JSONException {
       // log.info("api called", file);
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please upload excel file ");
        } else {
            ArrayList<ArrayList<String>> excelList = excelservice.save(file);
            //log.info("address list{}", addresslist);

            if (excelList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("List is Empty");
            } else {
                ArrayList<ArrayList<String>> tempArrayList = new ArrayList<>();
                //log.info("entered into whileloop{}", excelList);

                for (int i = 1; i < excelList.size(); i++) {
                    ArrayList<String> item = excelList.get(i);

                    if (item.size() >= 2) {
                        String address = item.get(1);
                        Pair<Double, Double> latlonglist = excelservice.getLatLongCoordinates(address);
                       // log.info("latlonglist{}", latlonglist);
                        if (latlonglist.getFirst() != null) {
                            item.add(latlonglist.getFirst().toString());
                        }else {
                            item.add("");
                        }if (latlonglist.getSecond() != null) {
                            item.add(latlonglist.getSecond().toString());
                        }else {
                            item.add("");
                        }
                    }
                    tempArrayList.add(item);
                }

                Workbook finalexcel = excelservice.writeExcel(tempArrayList);
                return ResponseEntity.status(HttpStatus.OK).body("Latitude and Longitude is created");
            }

        }
    }
}







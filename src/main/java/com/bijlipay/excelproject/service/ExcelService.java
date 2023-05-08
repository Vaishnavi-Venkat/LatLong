package com.bijlipay.excelproject.service;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.google.common.net.UrlEscapers;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;


@Service
public class ExcelService {

    private static final Logger log = LoggerFactory.getLogger(ExcelService.class);

    public ArrayList<ArrayList<String>> save(MultipartFile file) throws IOException {

        ArrayList<String> addresslist=new ArrayList<>();

        ArrayList<ArrayList<String>> excelList =  new ArrayList<>();
        File xlsxfile=new File("D:\\excelproject\\geotagatpaymentissue060523.xlsx");
        FileInputStream inputStream=new FileInputStream(xlsxfile);
        XSSFWorkbook workbook=new XSSFWorkbook(inputStream);
        XSSFSheet sheet=workbook.getSheetAt(0);

        Iterator<Row> iterator= sheet.iterator();
        while(iterator.hasNext()){
            Row nextrow= iterator.next();
            Iterator<Cell> cellIterator= nextrow.cellIterator();
            addresslist = new ArrayList<>();
            while (cellIterator.hasNext()){

                Cell cell= cellIterator.next();

                switch (cell.getCellType()) {
                    case STRING:
                        //log.info("String type", cell.getStringCellValue());
                        System.out.print(cell.getStringCellValue());
                        if (!cell.getStringCellValue().isEmpty()) {
                            addresslist.add(cell.getStringCellValue());
                           // log.info("List added successfully", addresslist);
                        }
                        break;
                    /*case BOOLEAN:
                        log.info("boolean type", cell.getBooleanCellValue());
                        System.out.print(cell.getBooleanCellValue());

                        break;*/
                    /*case NUMERIC:
                        log.info("numeric type", cell.getNumericCellValue());
                        System.out.print(cell.getNumericCellValue());
                        idlist.add(cell.getNumericCellValue());
                        log.info("Id added successfully",idlist);
                        break;*/
                    default:
                        break;
                }
                log.info("file reading done successfully");

            }
            excelList.add(addresslist);
        }
        workbook.close();
        inputStream.close();

        return excelList;
    }


   public Pair<Double,Double> getLatLongCoordinates(String address) throws IOException, JSONException {

        //log.info("lat long conversion starts",address);
        String encAddress = UrlEscapers.urlFragmentEscaper().escape(address);
             // String encAddress= address.replaceAll(" ","%20");
        log.info("The device address is : {}", encAddress);
        URL url = new URL("https://maps.google.com/maps/api/geocode/json?address=" + encAddress + "&key=AIzaSyCdJVSYfUG4AbyH6XxzSHxTcXXbNmbGGkY");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        //check connection is made
        int responsecode = conn.getResponseCode();
       // log.info("The response code is {}", responsecode);
        if (responsecode == 200) {
            //get the Json Object
            StringBuilder information = new StringBuilder();
            Scanner sc = new Scanner(url.openStream());
            while (sc.hasNext()) {
                information.append(sc.nextLine());
            }
            sc.close();
           // log.info("The Information is{}", information);
            JSONObject mainObject = new JSONObject(information.toString());
            if (mainObject.has("results")) {
                JSONArray resultsArray = mainObject.getJSONArray("results");
                if (resultsArray.length() > 0) {
                    JSONObject resultsObject = resultsArray.getJSONObject(0);
                    if (resultsObject.has("geometry")) {
                        JSONObject geometryObject = resultsObject.getJSONObject("geometry");
                        if (geometryObject.has("location")) {
                            JSONObject locationObject = geometryObject.getJSONObject("location");
                            double lat = locationObject.getDouble("lat");
                            double lng = locationObject.getDouble("lng");
                           // log.info("the latitude is{}..", lat);
                           // log.info("the longitude is{}..", lng);
                            return Pair.of(lat, lng);
                        }
                        //log.info("The lat,lng get successfully");
                    }
                }else{
                    return Pair.of(0.00, 0.00);
                }
            }
        }
        return null;
    }


    public Workbook writeExcel(ArrayList<ArrayList<String>> list) throws IOException {

        //log.info("write starts{}", list);


        HSSFWorkbook workbook = new HSSFWorkbook();
        FileOutputStream outputStream = new FileOutputStream("D:\\excelproject\\LatLong.xlsx");
        //log.info("excel created successfully", outputStream);
        //workbook.write(outputStream);
        //creating the worksheet
        HSSFSheet sheet = workbook.createSheet("LatLong Records");
        //log.info("sheet created", sheet);
        String[] headers = {"TID", "Address", "Latitude", "Longitude"};

        Row headerRow = sheet.createRow(0);
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(font);

       for (int i = 0; i < headers.length; i++) {
           Cell cell = headerRow.createCell(i, CellType.STRING);
           cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
       }


        for(int i=0;i< list.size();i++) {
            ArrayList<String> item = list.get(i);
            HSSFRow row = sheet.createRow(i+1);
            for (int j=0;j< item.size();j++){
                row.createCell(j).setCellValue(item.get(j));
            }

      }
            workbook.write(outputStream);
            outputStream.close();
            workbook.close();
            log.info("Excel Sheet is written successfully");
            return null;
        }
    }







package com.generator;

import com.google.gson.JsonObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.tomcat.util.codec.binary.Base64;
import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code128.Code128Constants;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api")
public class RestController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UsageRepository usageRepository;

    @RequestMapping("/generate")
    public String response(@RequestParam(defaultValue = "") String token,
                           @RequestParam(defaultValue = "") String text,
                           @RequestParam(defaultValue = "Barcode") String type) throws IOException
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date();
        String now = df.format(d);


        JsonObject response = new JsonObject();

        response.addProperty("status", "failed");
        response.addProperty("status_code", "SC00");
        response.addProperty("status_message", "Unknown error occurred");

        if (token.equals(""))
        {
            response.addProperty("status", "failed");
            response.addProperty("status_code", "SC01");
            response.addProperty("status_message", "No token provided");

            return response.toString();
        }

        User user = userRepository.findByToken(token);
        Usage usage = new Usage();
        usage.setDatetime(now);

        if (user != null)
        {
            Integer userId = user.getId();
            usage.setUser_id(userId);

            if (text.equals(""))
            {
                response.addProperty("status", "failed");
                response.addProperty("status_code", "SC03");
                response.addProperty("status_message", "No text provided");

                return response.toString();
            }

            usage.setText(text);
            usage.setFormat("Base64");

            if (type.equals("Barcode"))
            {
                usage.setType(type);

                Code128Bean barcode128Bean = new Code128Bean();
                barcode128Bean.setCodeset(Code128Constants.CODESET_B);
                final int dpi = 100;

                barcode128Bean.setBarHeight(15.0);
                barcode128Bean.setFontSize(8);
                barcode128Bean.setQuietZone(5.0);
                barcode128Bean.doQuietZone(true);
                barcode128Bean.setModuleWidth(UnitConv.in2mm(3.2f / dpi));
                barcode128Bean.setMsgPosition(HumanReadablePlacement.HRP_BOTTOM);

                File outputFile = new File("target/classes/static/" + "barcode.png");
                outputFile.createNewFile();
                OutputStream out = new FileOutputStream(outputFile);
                try {
                    BitmapCanvasProvider canvasProvider = new BitmapCanvasProvider(
                            out, "image/x-png", dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);
                    barcode128Bean.generateBarcode(canvasProvider, text);
                    canvasProvider.finish();

                    String encodeString = encodeFileToBase64Binary(outputFile);

                    usage.setCode(encodeString);
                    usageRepository.save(usage);

                    response.addProperty("status", "success");
                    response.addProperty("status_code", "SC04");
                    response.addProperty("status_message", "Barcode successfully created");
                    response.addProperty("text", text);
                    response.addProperty("code_type", type);
                    response.addProperty("output_format", "Base64");
                    response.addProperty("code", encodeString);

                    return response.toString();

                } catch (FileNotFoundException e) {
                    System.out.println("Exception: " + e.toString());
                } catch (RuntimeException e) {
                    System.out.println("Exception: " + e.toString());
                } finally {
                    out.close();
                }
            }else if (type.equals("QRCode"))
            {
                usage.setType(type);

                String encodeString = this.qrCode(text);

                usage.setCode(encodeString);
                usageRepository.save(usage);

                response.addProperty("status", "success");
                response.addProperty("status_code", "SC05");
                response.addProperty("status_message", "QR Code successfully created");
                response.addProperty("text", text);
                response.addProperty("code_type", type);
                response.addProperty("output_format", "Base64");
                response.addProperty("code", encodeString);

                return response.toString();
            }else
            {
                response.addProperty("status", "failed");
                response.addProperty("status_code", "SC06");
                response.addProperty("status_message", "Invalid type");

                return response.toString();
            }


        }else {
            response.addProperty("status", "failed");
            response.addProperty("status_code", "SC02");
            response.addProperty("status_message", "Invalid token");

            return response.toString();
        }

        response.addProperty("status", "failed");
        response.addProperty("status_code", "SC07");
        response.addProperty("status_message", "Unknown error occurred");

        return response.toString();
    }



    public String qrCode(String value) throws IOException {
        String myCodeText = value.trim();
        int size = 250;
        File myFile = new File("target/classes/static/" + "qrcode.png");
        myFile.createNewFile();
        try {

            Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            hintMap.put(EncodeHintType.MARGIN, 2); /* default = 4 */
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(myCodeText, BarcodeFormat.QR_CODE, size,
                    size, hintMap);
            int width = byteMatrix.getWidth();
            BufferedImage image = new BufferedImage(width, width,
                    BufferedImage.TYPE_INT_RGB);
            image.createGraphics();

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, width);
            graphics.setColor(Color.BLACK);

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < width; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            ImageIO.write(image, "png", myFile);
            String encodeString = encodeFileToBase64Binary(myFile);

            return encodeString;
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            System.out.println("Exception: " + e.toString());
        }
        System.out.println("\n\nYou have successfully created QR Code.");

        return null;
    }

    private static String encodeFileToBase64Binary(File file){
        String encodedfile = null;
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int)file.length()];
            fileInputStreamReader.read(bytes);
            encodedfile = new String(Base64.encodeBase64(bytes), "UTF-8");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return encodedfile;
    }

}

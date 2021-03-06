package info.novatec.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import jakarta.inject.Singleton;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Hashtable;

/**
 * @author Stefan Schultz
 * <p>
 * class to generate a qr code and encode it as base64 string
 */
@Singleton
public class QRCodeService {

    public static final int WIDTH = 125;
    public static final int HEIGHT = WIDTH;

    public String generateQRCode(String ticketId) throws IOException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix byteMatrix;
        try {
            byteMatrix = qrCodeWriter.encode(ticketId, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hintMap);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
        // Make the BufferedImage that are to hold the QRCode
        int matrixWidth = byteMatrix.getWidth();
        int matrixHeight = byteMatrix.getWidth();
        BufferedImage image = new BufferedImage(matrixWidth, matrixHeight, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        fillGraphics(byteMatrix, image);
        ImageIO.write(image, "png", os);
        return Base64.getEncoder().encodeToString(os.toByteArray());

        // return qrToConsole(byteMatrix);
    }

    public String qrToConsole(BitMatrix byteMatrix) {
        StringBuilder stringBuilder = new StringBuilder();
        int matrixWidth = byteMatrix.getWidth();
        for (int i = 0; i < matrixWidth; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                if (byteMatrix.get(i, j)) {
                    stringBuilder.append("x");
                } else {
                    stringBuilder.append(" ");
                }
            }
            stringBuilder.append("\n\r");
        }
        return stringBuilder.toString();
    }

    public void fillGraphics(BitMatrix byteMatrix, BufferedImage image) {
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, byteMatrix.getWidth(), byteMatrix.getHeight());
        graphics.setColor(Color.BLACK);

        for (int i = 0; i < byteMatrix.getWidth(); i++) {
            for (int j = 0; j < byteMatrix.getWidth(); j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }
    }
}

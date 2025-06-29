package io.github.susimsek.springbootjweauthjpademo.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import io.github.susimsek.springbootjweauthjpademo.dto.response.QrCodeDTO;
import io.github.susimsek.springbootjweauthjpademo.exception.QRCodeGenerationException;
import io.github.susimsek.springbootjweauthjpademo.config.resolver.QrCodeSize;
import io.github.susimsek.springbootjweauthjpademo.security.HashUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QrCodeService {

    private final QRCodeWriter qrCodeWriter;

    private static final Map<EncodeHintType, Object> QR_HINTS = new EnumMap<>(EncodeHintType.class);
    static {
        QR_HINTS.put(EncodeHintType.MARGIN, 1);
        QR_HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
    }


    public QrCodeDTO generateQrCode(String data, QrCodeSize size) {
        byte[] pngBytes = generateRawPng(data, size);
        String hash = HashUtils.sha256Hex(pngBytes);
        return new QrCodeDTO(pngBytes, hash);
    }

    private byte[] generateRawPng(String data, QrCodeSize size) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            BitMatrix matrix = qrCodeWriter.encode(
                data, BarcodeFormat.QR_CODE,
                size.width(), size.height(), QR_HINTS
            );
            BufferedImage image = toImage(matrix);
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (WriterException e) {
            throw new QRCodeGenerationException(
                "Error while encoding QR code: " + e.getMessage(), e
            );
        } catch (Exception e) {
            throw new QRCodeGenerationException(
                "Failed to generate QR code PNG", e
            );
        }
    }

    private BufferedImage toImage(BitMatrix matrix) {
        int w = matrix.getWidth();
        int h = matrix.getHeight();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.BLACK);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (matrix.get(x, y)) {
                    g.fillRect(x, y, 1, 1);
                }
            }
        }
        g.dispose();
        return img;
    }
}

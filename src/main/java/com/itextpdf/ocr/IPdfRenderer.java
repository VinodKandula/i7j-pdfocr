package com.itextpdf.ocr;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Interface for PDF Renderer classes.
 * <p>
 * The IPdfRenderer provides possibilities to set list of input images
 * to be used for OCR, to set scaling mode for images, color of text in output
 * PDF document,  set fixed size of the PDF document
 * and to perform OCR using given images and return PDFDocument as result
 */
public interface IPdfRenderer {

    /**
     * Enum describing possible scale modes for images.
     *
     *
     * <li>{@link #keepOriginalSize}</li>
     * <li>{@link #scaleWidth}</li>
     * <li>{@link #scaleHeight}</li>
     * <li>{@link #scaleToFit}</li>
     */
    enum ScaleMode {
        /**
         * keepOriginalSize (default value).
         * <p>
         * the size of every page of
         * the output PDF document will match the size of the
         * corresponding input image
         */
        keepOriginalSize,
        /**
         * scaleWidth.
         * <p>
         * Only width of the image will be proportionally scaled
         */
        scaleWidth,
        /**
         * scaleHeight.
         * <p>
         * Only height of the image will be proportionally scaled
         */
        scaleHeight,
        /**
         * scaleToFit.
         * <p>
         * the size of every page of the output PDF document
         * will match the values set using "setPdfSize()" method
         */
        scaleToFit
    }

    /**
     * Supported image formats.
     */
    enum ImageFormat {
        /**
         * BMP file format.
         */
        bmp,
        /**
         * PNG file format.
         */
        png,
        /**
         * PNM file format.
         */
        pnm,
        /**
         * PGM file format.
         */
        pgm,
        /**
         * PPM file format.
         */
        ppm,
        /**
         * PBM file format.
         */
        pbm,
        /**
         * TIFF file format.
         */
        tiff,
        /**
         * TIF file format.
         */
        tif,
        /**
         * JPEG file format.
         */
        jpeg,
        /**
         * JPG file format.
         */
        jpg,
        /**
         * JPE file format.
         */
        jpe,
        /**
         * JFIF file format.
         */
        jfif
    }

    /**
     * Set list of input images for OCR.
     *
     * @param images a {@link java.util.List} object.
     */
    void setInputImages(List<File> images);

    /**
     * Get list of provided input images for OCR.
     *
     * @return a {@link java.util.List} object.
     */
    List<File> getInputImages();

    /**
     * Set text color (should be CMYK) in output PDF document.
     *
     * @param newColor a {@link com.itextpdf.kernel.colors.Color} object.
     */
    void setTextColor(Color newColor);

    /**
     * Get text color in output PDF document.
     *
     * @return a {@link com.itextpdf.kernel.colors.Color} object.
     */
    Color getTextColor();

    /**
     * Set scale mode for input images using available options
     * from ScaleMode enum.
     *
     * @param mode a {@link com.itextpdf.ocr.IPdfRenderer.ScaleMode} object.
     */
    void setScaleMode(ScaleMode mode);

    /**
     * Get scale mode for input images.
     *
     * @return a {@link com.itextpdf.ocr.IPdfRenderer.ScaleMode} object.
     */
    ScaleMode getScaleMode();

    /**
     * Set fixed size for output PDF document.
     * (this parameter is used only is ScaleMode is set as "fitToSize")
     *
     * @param pdfSize a {@link com.itextpdf.kernel.geom.Rectangle} object.
     */
    void setPageSize(Rectangle pdfSize);

    /**
     * Get size for output document.
     *
     * @return a {@link com.itextpdf.kernel.geom.Rectangle} object.
     */
    Rectangle getPageSize();

    /**
     * Set name for the image layer.
     * (of by default it is "Image layer")
     *
     * @param name layer's name
     */
    void setImageLayerName(String name);

    /**
     * Get name of image layer.
     *
     * @return layer's name that was manually set or
     * the default one (="Image layer")
     */
    String getImageLayerName();

    /**
     * Set name for the text layer.
     * (of by default it is "Text layer")
     *
     * @param name layer's name
     */
    void setTextLayerName(String name);

    /**
     * @return layer's name that was manually set or
     * the default one (="Text layer")
     */
    String getTextLayerName();

    /**
     * Specify pdf natural language, and optionally locale.
     *
     * @param lang String
     */
    void setPdfLang(String lang);

    /**
     * @return pdf document lang
     */
    String getPdfLang();

    /**
     * Set pdf document title.
     *
     * @param name String
     */
    void setTitle(String name);

    /**
     * @return pdf document title
     */
    String getTitle();

    /**
     * Set path to font to be used in pdf document.
     *
     * @param name String
     */
    void setFontPath(String name);

    /**
     * @return path to font
     */
    String getFontPath();

    /**
     * Perform OCR using provided pdfWriter and pdfOutputIntent.
     * PDF/A-3u document will be created if pdfOutputIntent is not null
     *
     * @param pdfWriter PdfWriter
     * @param pdfOutputIntent PdfOutputIntent
     * @return a {@link com.itextpdf.kernel.pdf.PdfDocument} object - PDF/A-3u document
     * @throws IOException if provided font or output intent is incorrect
     */
    PdfDocument doPdfOcr(PdfWriter pdfWriter, PdfOutputIntent pdfOutputIntent) throws IOException;

    /**
     * Perform OCR using provided pdfWriter.
     *
     * @param pdfWriter provided pdfWriter
     * @return a {@link com.itextpdf.kernel.pdf.PdfDocument} object
     * @throws IOException if provided font or output intent is incorrect
     */
    PdfDocument doPdfOcr(PdfWriter pdfWriter) throws IOException;

    /**
     * Perform OCR using provided pdfWriter.
     *
     * @param path path to text file to be created
     * @throws IOException
     */
    void doPdfOcr(String path) throws IOException;
}

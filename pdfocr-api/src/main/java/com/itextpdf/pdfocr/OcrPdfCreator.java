package com.itextpdf.pdfocr;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.otf.Glyph;
import com.itextpdf.io.font.otf.GlyphLine;
import com.itextpdf.io.font.otf.GlyphLine.GlyphLinePart;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.io.util.ResourceUtil;
import com.itextpdf.io.util.StreamUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfTrueTypeFont;
import com.itextpdf.kernel.font.PdfType0Font;
import com.itextpdf.kernel.font.PdfType1Font;
import com.itextpdf.kernel.font.PdfType3Font;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfViewerPreferences;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.PdfCanvasConstants.TextRenderingMode;
import com.itextpdf.kernel.pdf.layer.PdfLayer;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.pdfa.PdfADocument;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link OcrPdfCreator} is the class that creates PDF documents containing input
 * images and text that was recognized using provided {@link IOcrEngine}.
 *
 * {@link OcrPdfCreator} provides possibilities to set list of input images to
 * be used for OCR, to set scaling mode for images, to set color of text in
 * output PDF document, to set fixed size of the PDF document's page and to
 * perform OCR using given images and to return
 * {@link com.itextpdf.kernel.pdf.PdfDocument} as result.
 * OCR is based on the provided {@link IOcrEngine}
 * (e.g. tesseract reader). This parameter is obligatory and it should be
 * provided in constructor
 * or using setter.
 */
public class OcrPdfCreator {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(OcrPdfCreator.class);

    /**
     * Selected {@link IOcrEngine}.
     */
    private IOcrEngine ocrEngine;

    /**
     * Set of properties.
     */
    private OcrPdfCreatorProperties ocrPdfCreatorProperties;

    /**
     * Creates a new {@link OcrPdfCreator} instance.
     *
     * @param ocrEngine {@link IOcrEngine} selected OCR Reader
     */
    public OcrPdfCreator(final IOcrEngine ocrEngine) {
        this(ocrEngine, new OcrPdfCreatorProperties());
    }

    /**
     * Creates a new {@link OcrPdfCreator} instance.
     *
     * @param ocrEngine selected OCR Reader {@link IOcrEngine}
     * @param ocrPdfCreatorProperties set of properties for {@link OcrPdfCreator}
     */
    public OcrPdfCreator(final IOcrEngine ocrEngine,
            final OcrPdfCreatorProperties ocrPdfCreatorProperties) {
        setOcrEngine(ocrEngine);
        setOcrPdfCreatorProperties(ocrPdfCreatorProperties);
    }

    /**
     * Gets properties for {@link OcrPdfCreator}.
     *
     * @return set properties {@link OcrPdfCreatorProperties}
     */
    public final OcrPdfCreatorProperties getOcrPdfCreatorProperties() {
        return ocrPdfCreatorProperties;
    }

    /**
     * Sets properties for {@link OcrPdfCreator}.
     *
     * @param ocrPdfCreatorProperties set of properties
     * {@link OcrPdfCreatorProperties} for {@link OcrPdfCreator}
     */
    public final void setOcrPdfCreatorProperties(
            final OcrPdfCreatorProperties ocrPdfCreatorProperties) {
        this.ocrPdfCreatorProperties = ocrPdfCreatorProperties;
    }

    /**
     * Performs OCR with set parameters using provided {@link IOcrEngine} and
     * creates PDF using provided {@link com.itextpdf.kernel.pdf.PdfWriter} and
     * {@link com.itextpdf.kernel.pdf.PdfOutputIntent}.
     * PDF/A-3u document will be created if
     * provided {@link com.itextpdf.kernel.pdf.PdfOutputIntent} is not null.
     *
     * @param inputImages {@link java.util.List} of images to be OCRed
     * @param pdfWriter the {@link com.itextpdf.kernel.pdf.PdfWriter} object
     *                  to write final PDF document to
     * @param pdfOutputIntent {@link com.itextpdf.kernel.pdf.PdfOutputIntent}
     *                        for PDF/A-3u document
     * @return result PDF/A-3u {@link com.itextpdf.kernel.pdf.PdfDocument}
     * object
     * @throws OcrException if it was not possible to read provided or
     * default font
     */
    public final PdfDocument createPdfA(final List<File> inputImages,
            final PdfWriter pdfWriter,
            final PdfOutputIntent pdfOutputIntent)
            throws OcrException {
        LOGGER.info(MessageFormatUtil.format(
                PdfOcrLogMessageConstant.START_OCR_FOR_IMAGES,
                inputImages.size()));

        // map contains:
        // keys: image files
        // values:
        // map pageNumber -> retrieved text data(text and its coordinates)
        Map<File, Map<Integer, List<TextInfo>>> imagesTextData =
                new LinkedHashMap<File, Map<Integer, List<TextInfo>>>();
        for (File inputImage : inputImages) {
            imagesTextData.put(inputImage,
                    ocrEngine.doImageOcr(inputImage));
        }

        // create PdfDocument
        return createPdfDocument(pdfWriter, pdfOutputIntent, imagesTextData);
    }

    /**
     * Performs OCR with set parameters using provided {@link IOcrEngine} and
     * creates PDF using provided {@link com.itextpdf.kernel.pdf.PdfWriter}.
     *
     * @param inputImages {@link java.util.List} of images to be OCRed
     * @param pdfWriter the {@link com.itextpdf.kernel.pdf.PdfWriter} object
     *                  to write final PDF document to
     * @return result {@link com.itextpdf.kernel.pdf.PdfDocument} object
     * @throws OcrException if provided font is incorrect
     */
    public final PdfDocument createPdf(final List<File> inputImages,
            final PdfWriter pdfWriter)
            throws OcrException {
        return createPdfA(inputImages, pdfWriter, null);
    }

    /**
     * Gets used {@link IOcrEngine}.
     *
     * Returns {@link IOcrEngine} reader object to perform OCR.
     * @return selected {@link IOcrEngine} instance
     */
    public final IOcrEngine getOcrEngine() {
        return ocrEngine;
    }

    /**
     * Sets {@link IOcrEngine} reader object to perform OCR.
     * @param reader selected {@link IOcrEngine} instance
     */
    public final void setOcrEngine(final IOcrEngine reader) {
        ocrEngine = reader;
    }

    /**
     * Gets font as a byte array using provided fontp ath or the default one.
     *
     * @return selected font as byte[]
     */
    private byte[] getFont() {
        if (ocrPdfCreatorProperties.getFontPath() != null
                && !ocrPdfCreatorProperties.getFontPath().isEmpty()) {
            try {
                return Files.readAllBytes(java.nio.file.Paths
                        .get(ocrPdfCreatorProperties.getFontPath()));
            } catch (IOException | OutOfMemoryError e) {
                LOGGER.error(MessageFormatUtil.format(
                        PdfOcrLogMessageConstant.CANNOT_READ_PROVIDED_FONT,
                        e.getMessage()));
                return getDefaultFont();
            }
        } else {
            return getDefaultFont();
        }
    }

    /**
     * Gets default font as a byte array.
     *
     * @return default font as byte[]
     */
    private byte[] getDefaultFont() {
        try (InputStream stream = ResourceUtil
                .getResourceStream(getOcrPdfCreatorProperties()
                        .getDefaultFontName())) {
            return StreamUtil.inputStreamToArray(stream);
        } catch (IOException e) {
            LOGGER.error(MessageFormatUtil.format(
                    PdfOcrLogMessageConstant.CANNOT_READ_DEFAULT_FONT,
                    e.getMessage()));
            return new byte[0];
        }
    }

    /**
     * Adds image (or its one page) and text that was found there to canvas.
     *
     * @param pdfDocument result {@link com.itextpdf.kernel.pdf.PdfDocument}
     * @param font font for the placed text (could be custom or default)
     * @param imageSize size of the image according to the selected
     *                  {@link ScaleMode}
     * @param pageText text that was found on this image (or on this page)
     * @param imageData input image if it is a single page or its one page if
     *                 this is a multi-page image
     * @param createPdfA3u true if PDF/A3u document is being created
     * @throws OcrException if PDF/A3u document is being created and provided
     * font contains notdef glyphs
     */
    private void addToCanvas(final PdfDocument pdfDocument, final PdfFont font,
            final com.itextpdf.kernel.geom.Rectangle imageSize,
            final List<TextInfo> pageText, final ImageData imageData,
            final boolean createPdfA3u) throws OcrException {
        com.itextpdf.kernel.geom.Rectangle rectangleSize =
                ocrPdfCreatorProperties.getPageSize() == null
                        ? imageSize : ocrPdfCreatorProperties.getPageSize();
        PageSize size = new PageSize(rectangleSize);
        PdfPage pdfPage = pdfDocument.addNewPage(size);
        PdfCanvas canvas = new NotDefCheckingPdfCanvas(pdfPage, createPdfA3u);

        PdfLayer imageLayer = new PdfLayer(
                ocrPdfCreatorProperties.getImageLayerName(), pdfDocument);
        PdfLayer textLayer = new PdfLayer(
                ocrPdfCreatorProperties.getTextLayerName(), pdfDocument);

        canvas.beginLayer(imageLayer);
        addImageToCanvas(imageData, imageSize, canvas);
        canvas.endLayer();

        // how much the original image size changed
        float multiplier = imageData == null
                ? 1 : imageSize.getWidth()
                / PdfCreatorUtil.getPoints(imageData.getWidth());
        canvas.beginLayer(textLayer);

        try {
            addTextToCanvas(imageSize, pageText, canvas, font,
                    multiplier, pdfPage.getMediaBox());
        } catch (OcrException e) {
            LOGGER.error(MessageFormatUtil.format(
                    OcrException.CANNOT_CREATE_PDF_DOCUMENT,
                    e.getMessage()));
            throw new OcrException(OcrException.CANNOT_CREATE_PDF_DOCUMENT)
                    .setMessageParams(e.getMessage());
        }
        canvas.endLayer();
    }

    /**
     * Creates a new PDF document using provided properties, adds images with
     * recognized text.
     *
     * @param pdfWriter the {@link com.itextpdf.kernel.pdf.PdfWriter} object
     *                  to write final PDF document to
     * @param pdfOutputIntent {@link com.itextpdf.kernel.pdf.PdfOutputIntent}
     *                        for PDF/A-3u document
     * @param imagesTextData Map<File, Map<Integer, List<TextInfo>>> -
     *                       map that contains input image files as keys,
     *                       and as value: map pageNumber -> text for the page
     * @return result {@link com.itextpdf.kernel.pdf.PdfDocument} object
     */
    private PdfDocument createPdfDocument(final PdfWriter pdfWriter,
            final PdfOutputIntent pdfOutputIntent,
            final Map<File, Map<Integer, List<TextInfo>>> imagesTextData) {
        PdfDocument pdfDocument;
        boolean createPdfA3u = pdfOutputIntent != null;
        if (createPdfA3u) {
            pdfDocument = new PdfADocument(pdfWriter,
                    PdfAConformanceLevel.PDF_A_3U, pdfOutputIntent);
        } else {
            pdfDocument = new PdfDocument(pdfWriter);
        }

        // add metadata
        pdfDocument.getCatalog()
                .setLang(new PdfString(ocrPdfCreatorProperties.getPdfLang()));
        pdfDocument.getCatalog().setViewerPreferences(
                new PdfViewerPreferences().setDisplayDocTitle(true));
        PdfDocumentInfo info = pdfDocument.getDocumentInfo();
        info.setTitle(ocrPdfCreatorProperties.getTitle());

        // create PdfFont
        PdfFont defaultFont = null;
        try {
            defaultFont = PdfFontFactory.createFont(getFont(),
                    PdfEncodings.IDENTITY_H, true);
        } catch (com.itextpdf.io.IOException | IOException e) {
            LOGGER.error(MessageFormatUtil.format(
                    PdfOcrLogMessageConstant.CANNOT_READ_PROVIDED_FONT,
                    e.getMessage()));
            try {
                defaultFont = PdfFontFactory.createFont(getDefaultFont(),
                        PdfEncodings.IDENTITY_H, true);
            } catch (com.itextpdf.io.IOException
                    | IOException | NullPointerException ex) {
                LOGGER.error(MessageFormatUtil.format(
                        PdfOcrLogMessageConstant.CANNOT_READ_DEFAULT_FONT,
                        ex.getMessage()));
                throw new OcrException(OcrException.CANNOT_READ_FONT);
            }
        }
        addDataToPdfDocument(imagesTextData, pdfDocument, defaultFont,
                createPdfA3u);

        return pdfDocument;
    }

    /**
     * Places provided images and recognized text to the result PDF document.
     *
     * @param imagesTextData Map<File, Map<Integer, List<TextInfo>>> -
     *                       map that contains input image
     *                       files as keys, and as value:
     *                       map pageNumber -> text for the page
     * @param pdfDocument result {@link com.itextpdf.kernel.pdf.PdfDocument}
     * @param font font for the placed text (could be custom or default)
     * @param createPdfA3u true if PDF/A3u document is being created
     * @throws OcrException if input image cannot be read or provided font
     * contains NOTDEF glyphs
     */
    private void addDataToPdfDocument(
            final Map<File, Map<Integer, List<TextInfo>>> imagesTextData,
            final PdfDocument pdfDocument,
            final PdfFont font,
            final boolean createPdfA3u) throws OcrException {
        for (Map.Entry<File, Map<Integer, List<TextInfo>>> entry
                : imagesTextData.entrySet()) {
            try {
                File inputImage = entry.getKey();
                List<ImageData> imageDataList =
                        PdfCreatorUtil.getImageData(inputImage);
                LOGGER.info(MessageFormatUtil.format(
                        PdfOcrLogMessageConstant.NUMBER_OF_PAGES_IN_IMAGE,
                        inputImage.toString(), imageDataList.size()));

                Map<Integer, List<TextInfo>> imageTextData = entry.getValue();
                if (imageTextData.keySet().size() > 0) {
                    for (int page = 0; page < imageDataList.size(); ++page) {
                        ImageData imageData = imageDataList.get(page);
                        com.itextpdf.kernel.geom.Rectangle imageSize =
                                PdfCreatorUtil.calculateImageSize(
                                        imageData,
                                        ocrPdfCreatorProperties.getScaleMode(),
                                        ocrPdfCreatorProperties.getPageSize());

                        if (imageTextData.containsKey(page + 1)) {
                            addToCanvas(pdfDocument, font, imageSize,
                                    imageTextData.get(page + 1),
                                    imageData, createPdfA3u);
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.error(MessageFormatUtil.format(
                        PdfOcrLogMessageConstant.CANNOT_ADD_DATA_TO_PDF_DOCUMENT,
                        e.getMessage()));
            }
        }
    }

    /**
     * Places given image to canvas to background to a separate layer.
     *
     * @param imageData input image as {@link java.io.File}
     * @param imageSize size of the image according to the selected
     *                  {@link ScaleMode}
     * @param pdfCanvas canvas to place the image
     */
    private void addImageToCanvas(final ImageData imageData,
            final com.itextpdf.kernel.geom.Rectangle imageSize,
            final PdfCanvas pdfCanvas) {
        if (imageData != null) {
            if (ocrPdfCreatorProperties.getPageSize() == null) {
                pdfCanvas.addImage(imageData, imageSize, false);
            } else {
                com.itextpdf.kernel.geom.Point coordinates =
                        PdfCreatorUtil.calculateImageCoordinates(
                        ocrPdfCreatorProperties.getPageSize(), imageSize);
                com.itextpdf.kernel.geom.Rectangle rect =
                        new com.itextpdf.kernel.geom.Rectangle(
                                (float)coordinates.x, (float)coordinates.y,
                                imageSize.getWidth(), imageSize.getHeight());
                pdfCanvas.addImage(imageData, rect, false);
            }
        }
    }

    /**
     * Places retrieved text to canvas to a separate layer.
     *
     * @param imageSize size of the image according to the selected
     *                  {@link ScaleMode}
     * @param pageText text that was found on this image (or on this page)
     * @param pdfCanvas canvas to place the text
     * @param font font for the placed text (could be custom or default)
     * @param multiplier coefficient to adjust text placing on canvas
     * @param pageMediaBox page parameters
     * @throws OcrException if PDF/A3u document is being created and provided
     * font contains notdef glyphs
     */
    private void addTextToCanvas(
            final com.itextpdf.kernel.geom.Rectangle imageSize,
            final List<TextInfo> pageText,
            final PdfCanvas pdfCanvas,
            final PdfFont font,
            final float multiplier,
            final com.itextpdf.kernel.geom.Rectangle pageMediaBox)
            throws OcrException {
        if (pageText == null || pageText.size() == 0) {
            pdfCanvas.beginText().setFontAndSize(font, 1);
        } else {
            com.itextpdf.kernel.geom.Point imageCoordinates =
                    PdfCreatorUtil.calculateImageCoordinates(
                    ocrPdfCreatorProperties.getPageSize(), imageSize);
            for (TextInfo item : pageText) {
                String line = item.getText();
                List<Float> coordinates = item.getBbox();
                final float left = coordinates.get(0) * multiplier;
                final float right = (coordinates.get(2) + 1) * multiplier - 1;
                final float top = coordinates.get(1) * multiplier;
                final float bottom = (coordinates.get(3) + 1) * multiplier - 1;

                float bboxWidthPt = PdfCreatorUtil.getPoints(right - left);
                float bboxHeightPt = PdfCreatorUtil.getPoints(bottom - top);
                if (!line.isEmpty() && bboxHeightPt > 0 && bboxWidthPt > 0) {
                    // Scale the text width to fit the OCR bbox
                    float fontSize = PdfCreatorUtil.calculateFontSize(
                            new Document(pdfCanvas.getDocument()),
                            line, font, bboxHeightPt, bboxWidthPt);
                    float lineWidth = font.getWidth(line, fontSize);

                    float deltaX = PdfCreatorUtil.getPoints(left);
                    float deltaY = imageSize.getHeight()
                            - PdfCreatorUtil.getPoints(bottom);

                    Canvas canvas = new Canvas(pdfCanvas, pageMediaBox);

                    Text text = new Text(line)
                            .setHorizontalScaling(bboxWidthPt / lineWidth);

                    Paragraph paragraph = new Paragraph(text)
                            .setMargin(0)
                            .setMultipliedLeading(1.2f);
                    paragraph.setFont(font)
                            .setFontSize(fontSize);
                    paragraph.setWidth(bboxWidthPt * 1.5f);

                    if (ocrPdfCreatorProperties.getTextColor() != null) {
                        paragraph.setFontColor(
                                ocrPdfCreatorProperties.getTextColor());
                    } else {
                        paragraph.setTextRenderingMode(
                                TextRenderingMode.INVISIBLE);
                    }

                    canvas.showTextAligned(paragraph,
                            deltaX + (float)imageCoordinates.x,
                            deltaY + (float)imageCoordinates.y,
                            TextAlignment.LEFT);
                    canvas.close();
                }
            }
        }
    }

    /**
     * A handler for PDF canvas that validates existing glyphs.
     */
    private static class NotDefCheckingPdfCanvas extends PdfCanvas {
        private static final long serialVersionUID = 708713860707664107L;
        private final boolean createPdfA3u;
        public NotDefCheckingPdfCanvas(PdfPage page, boolean createPdfA3u) {
            super(page);
            this.createPdfA3u = createPdfA3u;
        }

        @Override
        public PdfCanvas showText(GlyphLine text,
                Iterator<GlyphLinePart> iterator) {
            PdfFont currentFont = getGraphicsState().getFont();
            boolean notDefGlyphsExists = false;
            // default value for error message, it'll be updated with the
            // unicode of the not found glyph
            String message = PdfOcrLogMessageConstant
                    .COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER;
            for (int i = text.start; i < text.end; i++) {
                if (isNotDefGlyph(currentFont, text.get(i))) {
                    notDefGlyphsExists = true;
                    message = MessageFormatUtil.format(PdfOcrLogMessageConstant
                            .COULD_NOT_FIND_CORRESPONDING_GLYPH_TO_UNICODE_CHARACTER,
                            text.get(i).getUnicode());
                    if (this.createPdfA3u) {
                        // exception is thrown only if PDF/A document is
                        // being created
                        throw new OcrException(message);
                    }
                }
            }
            // Warning is logged if not PDF/A document is being created
            if (notDefGlyphsExists) {
                LOGGER.warn(message);
            }
            return super.showText(text, iterator);
        }

        private static boolean isNotDefGlyph(PdfFont font, Glyph glyph) {
            if (font instanceof PdfType0Font
                    || font instanceof PdfTrueTypeFont) {
                return glyph.getCode() == 0;
            } else if (font instanceof PdfType1Font
                    || font instanceof PdfType3Font) {
                return glyph.getCode() == -1;
            }
            return false;
        }
    }
}
